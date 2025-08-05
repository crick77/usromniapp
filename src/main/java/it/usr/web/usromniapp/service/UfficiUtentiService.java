/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;

import static it.usr.web.usromniapp.domain.Tables.*;
import it.usr.web.usromniapp.domain.routines.SpAggiornaUfficio;
import it.usr.web.usromniapp.domain.tables.Uffici;
import it.usr.web.usromniapp.domain.tables.Utenti;
import it.usr.web.usromniapp.domain.tables.records.UfficiRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.interceptor.LogDatabaseOperation;
import it.usr.web.usromniapp.producer.DSLCtx;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UfficiUtentiService {    
    @Inject
    @DSLCtx
    DSLContext ctx;
    
    public List<UfficiRecord> getUffici() {
        return ctx.selectFrom(UFFICI).where(UFFICI.ATTIVO.eq(1)).orderBy(UFFICI.ID_UFFICIO_SOVRAORDINATO.asc()).fetch();
    }
    
    public Map<Integer, UtentiRecord> getUtentiMap() {
        return ctx.selectFrom(UTENTI).fetch().intoMap(UTENTI.ID_UTENTE);
    }
    
    @LogDatabaseOperation
    public void inserisci(UfficiRecord ufficio) throws DatabaseException {
        try {
            ctx.transaction(trx -> {
                if(ufficio.getIdUfficioSovraordinato()==null) {
                    int top = trx.dsl().fetchCount(trx.dsl().selectFrom(UFFICI).where(UFFICI.ID_UFFICIO_SOVRAORDINATO.isNull()));
                    if(top>0) throw new DatabaseException("Non si può avere più di un ufficio radice.");
                }
                
                trx.dsl().insertInto(UFFICI).set(ufficio).execute();               
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }
    }
    
    @LogDatabaseOperation
    public void modifica(UfficiRecord ufficio, String estremiProvv) throws DatabaseException {
        try {
            ctx.transaction(trx -> {
                /*if(ufficio.getIdUfficioSovraordinato()==null) {
                    int top = trx.dsl().fetchCount(trx.dsl().selectFrom(UFFICI).where(UFFICI.ID_UFFICIO_SOVRAORDINATO.isNull()));
                    if(top>0) throw new DatabaseException("Non si può avere più di un ufficio radice.");
                }*/
                
                SpAggiornaUfficio sp = new SpAggiornaUfficio();
                sp.setIdUfficio(ufficio.getIdUfficio());
                if(Objects.equals(ufficio.getUfficio(), ufficio.original().getUfficio())) {
                    sp.setNomeufficio(null);
                }
                else {
                    sp.setNomeufficio(ufficio.getUfficio());
                }
                sp.setIdUtenteResp(ufficio.getIdUtente());
                sp.setIdUtenteRespPrec(ufficio.original().getIdUtente()); 
                sp.setEstremiProvv(estremiProvv);
                
                sp.execute(trx.dsl().configuration());
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }
    }
    
    @LogDatabaseOperation
    public void disattiva(UfficiRecord ufficio, String estremiProvv) throws DatabaseException {
        try {
            LocalDateTime now = LocalDateTime.now();
            final String note = truncate(ufficio.getNote()+" "+estremiProvv, 255);
            final String auth = "%A%";
            
            ctx.transaction(trx -> {
                trx.dsl().update(UFFICI).set(UFFICI.ATTIVO, 0).where(UFFICI.ID_UFFICIO.eq(ufficio.getIdUfficio())).execute();
                
                trx.dsl().update(PROC_ASS).set(PROC_ASS.DATA_RIMOZIONE, now).set(PROC_ASS.NOTE, note).where(PROC_ASS.ID_UTENTE.eq(ufficio.getIdUtente())).and(PROC_ASS.AUTORIZZAZIONI.like(auth)).and(PROC_ASS.DATA_RIMOZIONE.isNull()).execute();
                
                trx.dsl().update(TIPO_PROC_ASS).set(TIPO_PROC_ASS.DATA_RIMOZIONE, now).set(TIPO_PROC_ASS.NOTE, note).where(TIPO_PROC_ASS.ID_UTENTE.eq(ufficio.getIdUtente())).and(TIPO_PROC_ASS.AUTORIZZAZIONI.like(auth)).and(TIPO_PROC_ASS.DATA_RIMOZIONE.isNull()).execute();
                
                trx.dsl().update(TIPO_PROC_ASS).set(TIPO_PROC_ASS.DATA_RIMOZIONE, now).set(TIPO_PROC_ASS.NOTE, note).where(TIPO_PROC_ASS.ID_UFFICIO.eq(ufficio.getIdUfficio())).and(TIPO_PROC_ASS.DATA_RIMOZIONE.isNull()).execute();
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }
    }
    
    public String truncate(String s, int len) {
        return (s==null || s.length()<len) ? s : s.substring(0, len);
    }
}
