/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.service.BaseService;
import it.usr.web.usromniapp.controller.AccessDeniedException;
import static it.usr.web.usromniapp.domain.Tables.*;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.domain.tables.records.VProcAclAttiviRecord;
import it.usr.web.usromniapp.model.ACL;
import it.usr.web.usromniapp.model.Utente;
import it.usr.web.usromniapp.producer.DSLCtx;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SecurityService extends BaseService {
    @Inject
    @DSLCtx
    DSLContext ctx;
    @Inject
    @AppLogger
    Logger logger;
    
    /**
     * Verifica che la funzione sia associata all'utente con le autorizzazioni richieste.
     * Il metodo estrae la prima riga della query attuando la seguente politica di estrazione:
     * 
     *  - prima i dinieghi
     *  - prima i permessi personali e poi quelli all'ufficio
     *  - prima le autorizzazioni con bit A (assegnazione)
     *  - prima le autorizzazioni con bit M (modifica)
     *  - il resto
     * 
     * La prima riga risultate verrà poi confrontata con il permesso richiesto e verrà
     * restituito true solo se contemplato. False altrimenti.
     * 
     * @param function
     * @param utente
     * @param autorizzazioni
     * @return 
     */
    public boolean checkFunction(String function, Utente utente, int autorizzazioni) {
        List<Integer> idUff = utente.getUffici().stream().map(u -> u.getIdUfficio()).collect(Collectors.toList());
        org.jooq.Record r = ctx.select(V_PROC_AUTH_FUN_ATTIVI.fields()).from(V_PROC_AUTH_FUN_ATTIVI)
                            .join(PROC_FUN).on(V_PROC_AUTH_FUN_ATTIVI.ID_PROC_FUN.eq(PROC_FUN.ID_PROC_FUN))
                            .leftJoin(RUOLI_UTENTE).on(V_PROC_AUTH_FUN_ATTIVI.ID_RUOLI_UTENTE.eq(RUOLI_UTENTE.ID_RUOLI_UTENTE))
                                .where(PROC_FUN.CODICE_FUN.eq(function).and(RUOLI_UTENTE.ID_UTENTE.eq(utente.getUtente().getIdUtente())
                                   //.or(V_PROC_AUTH_FUN_ATTIVI.ID_UFFICIO.eq(utente.getUfficio().getIdUfficio()))))
                                    .or(V_PROC_AUTH_FUN_ATTIVI.ID_UFFICIO.in(idUff))))
                                .orderBy(
                                        V_PROC_AUTH_FUN_ATTIVI.AUTORIZZAZIONI.bitAnd(8).desc(), 
                                        RUOLI_UTENTE.ID_UTENTE.isNotNull().desc(),
                                        V_PROC_AUTH_FUN_ATTIVI.AUTORIZZAZIONI.bitAnd(1).desc(),  
                                        V_PROC_AUTH_FUN_ATTIVI.AUTORIZZAZIONI.bitAnd(2).desc()
                                )
                .fetchAny();
        return (r!=null) ? (r.getValue(V_PROC_AUTH_FUN_ATTIVI.AUTORIZZAZIONI) & autorizzazioni)==autorizzazioni : false;
    }
    
    /**
     * Restituisce l'elenco delle ACL (Access Control List) per l'utente e le operazioni indicate.
     * Le ACL contentono gli elenchi di tipi di procedura ammessi e negati e le singole procedure
     * ammesse e indicate.
     * 
     * @param utente l'utente
     * @param op le operazioni
     * @return l'ACL di risultato
     */
    public ACL getACL(Utente utente, TipoOperazioneEnum[] op) {
        final int id_utente = utente.getUtente().getIdUtente();
        final int autorizzazioni = TipoOperazioneEnum.combina(op);
        final int diniego = TipoOperazioneEnum.X.getOperazione();
        //final int id_ufficio = utente.getUfficio().getIdUfficio();
        final List<Integer> idUffici = utente.getUffici().stream().map(u -> u.getIdUfficio()).collect(Collectors.toList());
        
        Condition[] cond = { DSL.noCondition() };
                       
        // verifica per ogni tipo procedura se l'utente disponde dei diritti 
        utente.getDeleghe().forEach((id_tipo_proc, id_delega) -> {
            cond[0] = cond[0]
                        .or(                    
                                 V_PROC_ACL_ATTIVI.ID_TIPO_PROC.eq(id_tipo_proc)
                            .and(V_PROC_ACL_ATTIVI.ID_UTENTE.eq(id_delega)
                            .and(
                                        V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(autorizzazioni).eq(autorizzazioni)
                                .andNot(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(diniego).eq(diniego))                            
                                )
                            )
                        );
        });
        
        // verifica per ogni tipo procedura se l'ufficio a cui appartiene l'utente disponde dei diritti 
        utente.getDeleghe().forEach((id_tipo_proc, id_delega) -> {
            cond[0] = cond[0]
                        .or(                    
                                 V_PROC_ACL_ATTIVI.ID_TIPO_PROC.eq(id_tipo_proc)
                            //.and(V_PROC_ACL_ATTIVI.ID_UFFICIO.eq(id_ufficio)
                            .and(V_PROC_ACL_ATTIVI.ID_UFFICIO.in(idUffici)
                            .and(
                                        V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(autorizzazioni).eq(autorizzazioni)
                                .andNot(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(diniego).eq(diniego))                            
                                )
                            )
                        );
        });
        
        // aggiunge le singole procedure ESPLICITAMENTE AMMESSE
        cond[0] = cond[0]
                        .or(
                                V_PROC_ACL_ATTIVI.ID_PROC.isNotNull()
                                .and(V_PROC_ACL_ATTIVI.ID_UTENTE.eq(id_utente)
                                    //.or(V_PROC_ACL_ATTIVI.ID_UFFICIO.eq(id_ufficio))
                                    .or(V_PROC_ACL_ATTIVI.ID_UFFICIO.in(idUffici))    
                                )
                                .and(
                                        V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(autorizzazioni).eq(autorizzazioni)
                                    .and(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(diniego).ne(diniego))
                                )
                        );
        
        
        List<VProcAclAttiviRecord> lAmmessi = ctx.selectFrom(V_PROC_ACL_ATTIVI).where(cond[0]).fetch();
        
        // tipi procedure ecluse esplicitamente all'utente
        cond[0] = DSL.noCondition();        
        utente.getDeleghe().forEach((id_tipo_proc, id_delega) -> {
            cond[0] = cond[0]
                        .or(                    
                                 V_PROC_ACL_ATTIVI.ID_TIPO_PROC.eq(id_tipo_proc)
                            .and(V_PROC_ACL_ATTIVI.ID_UTENTE.eq(id_utente)
                            .and(
                                         V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(autorizzazioni).ne(autorizzazioni)
                                    .and(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(diniego).eq(diniego))                            
                                )
                            )
                        );
        });
        
        List<VProcAclAttiviRecord> lTipiProcNegate = ctx.selectFrom(V_PROC_ACL_ATTIVI).where(cond[0]).fetch();
        
        // Singole procedure escluse esplicitamente all'utente
        cond[0] = DSL.noCondition();        
        cond[0] = cond[0]
                        .or(
                                V_PROC_ACL_ATTIVI.ID_PROC.isNotNull()
                                .and(V_PROC_ACL_ATTIVI.ID_UTENTE.eq(id_utente)
                                    //.or(V_PROC_ACL_ATTIVI.ID_UFFICIO.eq(id_ufficio))
                                    .or(V_PROC_ACL_ATTIVI.ID_UFFICIO.in(idUffici))
                                )
                                .and(
                                        V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(autorizzazioni).ne(autorizzazioni)
                                    .and(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(diniego).eq(diniego))
                                )
                        );
        List<VProcAclAttiviRecord> lProcNegate = ctx.selectFrom(V_PROC_ACL_ATTIVI).where(cond[0]).fetch();
        
        // Costruisce l'elenco delle procedure/tipi di procedura
        ACL acl = new ACL();        
        lAmmessi.forEach(ammesso -> {
            if(ammesso.getIdProc()!=null) {
                acl.getProcedureAmmesse().add(ammesso.getIdProc());
            }
            else {
                acl.getTipiProcedureAmmesse().add(ammesso.getIdTipoProc());
            }
        });
        
        lTipiProcNegate.forEach(tipoProc -> {
            acl.getTipiProcedureEscluse().add(tipoProc.getIdTipoProc()); 
        });
        
        lProcNegate.forEach(proc -> {
            acl.getProcedureEscluse().add(proc.getIdProc());
        });
        
        //logger.info("Elenco di visibilità per l'utente/ufficio id=[{}/{}] con autorizzazioni [{}]: {}", id_utente, id_ufficio, autorizzazioni, acl);
        logger.info("Elenco di visibilità per l'utente/uffici id=[{}/{}] con autorizzazioni [{}]: {}", id_utente, idUffici, autorizzazioni, acl);
        return acl;
    }
    
    /**
     * Verifica l'accessibilità da parte dell'utente specificato alla procedura e con i diritti di
     * accesso indicati. Il metodo rialza un'eccezione che porterà l'applicazione nella
     * pagina di "access denied".
     * 
     * @param proc la procedura
     * @param utente l'utente
     * @param op i tipi di diritti di accesso
     */
    public void verificaAccessibilitaProcedimento(ProcRecord proc, Utente utente, TipoOperazioneEnum[] op) {
        ACL acl = getACL(utente, op);
        int idProc = proc.getIdProc(); 
        int idTipoProc = proc.getIdTipoProc();
         
        // La procedura è tra quelle esplicitamente concesse?
        // OPPURE
        // La procedura è del tipo ammesso ma non è tra quelle esplicitamente escluse?
        boolean res = (acl.getProcedureAmmesse().contains(idProc) || (acl.getTipiProcedureAmmesse().contains(idTipoProc) && !acl.getProcedureEscluse().contains(idProc)));
        if(!res) {
            String msg = String.format("L'utente [%s] non ha la visibilitità [%s] per il procedimento [%s].", utente.getUtente().getUtente(), Arrays.toString(op), proc.getIdProc());
            logger.info(msg);
            throw new AccessDeniedException(msg);
        }                
    }
}
