/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;

import it.usr.web.producer.AppLogger;
import static it.usr.web.usromniapp.domain.Tables.*;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.model.Autorizzazione;
import it.usr.web.usromniapp.model.ElencoVisibili;
import it.usr.web.usromniapp.model.Utente;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import it.usr.web.usromniapp.producer.DSLCtx;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SecurityService {
    @Inject
    @DSLCtx
    DSLContext ctx;
    @Inject
    @AppLogger
    Logger logger;

    /**
     * Restituisce true se l'utente indicato ha i diritti di assegnazione
     * 
     * @param user
     * @param auth
     * @return 
     */
    //@SuppressWarnings("empty-statement")
    public boolean canOpen(Utente user, String auth) {
        if(auth==null || auth.trim().length()==0) {
            throw new IllegalArgumentException("Le autorizzazioni non possono essere vuote o null.");
        }
        
        // Estrae tutti i deleganti attivi alla data attuale, indipendentemente dalla procedura
        LocalDateTime now = LocalDateTime.now();
        String sql = """
                     SELECT distinct u.id_utente from utenti u join delega d on u.id_utente = d.id_delegante where d.id_utente = {0} and 
                     ((data_inizio <= {1} and data_fine is null) or ({1} between data_inizio and data_fine))
                     """;
        List<Integer> lDeleganti = ctx.fetch(sql, user.getUtente().getIdUtente(), now).into(Integer.class);
        logger.info("L'utente [{}] è stato delegato dagli utenti [{}].", user.getUtente().getIdUtente(), lDeleganti);
        
        // Controlla la possibilità di accesso per ciascun delegante
        sql =    "((SELECT id_tipo_proc, null as id_proc, null as id_tipo_proc_rif, id_utente, id_ufficio, autorizzazioni FROM decreti.tipo_proc_ass where " +
                        "(id_utente = {0} or " +
                        "(id_ufficio in (select id_ufficio from istruttori_uffici where id_utente = {0}))) " +
                        "and ((data_assegnazione <= {1} and data_rimozione is null) or ({1} between data_assegnazione and data_rimozione)) " +
                        "and (autorizzazioni like {2}) "+
                        "order by id_utente, id_ufficio) " +
                        "union " +
                        "(SELECT null as id_tipo_proc, id_proc, (select p.id_tipo_proc from proc p where p.id_proc = pa.id_proc) as id_tipo_proc_rif, id_utente, id_ufficio, autorizzazioni FROM decreti.proc_ass pa where " +
                        "(id_utente_delegante = {0} or " +
                        "(id_ufficio in (select id_ufficio from istruttori_uffici where id_utente = {0}))) " +
                        "and ((data_assegnazione <= {1} and data_rimozione is null) or ({1} between data_assegnazione and data_rimozione)) " +
                        "and (autorizzazioni like {2}) "+
                        "order by id_utente desc, id_ufficio desc)) as x " +
                        "order by id_tipo_proc is null, id_tipo_proc, " +
                        "id_proc is null, id_proc, " +
                        "id_utente is null, id_utente, " +
                        "id_ufficio is null, id_ufficio, " +
                        "autorizzazioni<>'X', autorizzazioni";        
        for(int idDelegante : lDeleganti) {
            List<Autorizzazione> lAuth = ctx.selectFrom(sql, idDelegante, now, fixSecurityAuth(auth)).fetchInto(Autorizzazione.class);

            int tipoProcCnt = 0;
            int procCnt = 0;
            for(int i=0;i<lAuth.size();i++) {
                Autorizzazione a = lAuth.get(i);
                if(a.getIdTipoProc()!=null) {
                    if(a.getAutorizzazioni().contains("X")) {                    
                        while(i<lAuth.size() && a.getIdTipoProc().equals(lAuth.get(i).getIdTipoProc())) i++;
                        i--;
                    }
                    else {
                        tipoProcCnt++;
                    }
                }    
                else {
                    if(a.getAutorizzazioni().contains("X")) {                                        
                        while(i<lAuth.size() && a.getIdProc().equals(lAuth.get(i).getIdProc())) i++;
                        i--;
                    }
                    else {
                        procCnt++; 
                    }
                }
            } 

            // Se sono presenti autorizzazioni attive, conferma (ne è sufficiente 1 sola di uno qualsiasi dei deleganti)
            if((tipoProcCnt+procCnt)>0) return true;
        }
        
        // nessuna autorizzazione nemmeno per delega
        return false;
    }
        
    public ElencoVisibili getElencoVisibili(Utente user, TipoOperazioneEnum tipoOp) {
        return getElencoVisibili(user, new TipoOperazioneEnum[]{tipoOp});
    }
    
    public ElencoVisibili getElencoVisibili(Utente user, TipoOperazioneEnum[] tipoOp) {
        String likeOp = fixSecurityAuth(arrayToString(tipoOp));
        String sql =    "((SELECT id_tipo_proc, null as id_proc, null as id_tipo_proc_rif, id_utente, id_ufficio, autorizzazioni FROM decreti.tipo_proc_ass where " +
                        "((id_utente = {0} or " +
                        "(id_ufficio in (select id_ufficio from istruttori_uffici where id_utente = {0}))) " +
                        "and ((data_assegnazione <= {1} and data_rimozione is null) or ({1} between data_assegnazione and data_rimozione)) " +
                        "and (autorizzazioni like {2}) " +
                        "order by id_utente, id_ufficio) " +
                        "union " +
                        "(SELECT null as id_tipo_proc, id_proc, (select p.id_tipo_proc from proc p where p.id_proc = pa.id_proc) as id_tipo_proc_rif, id_utente, id_ufficio, autorizzazioni FROM decreti.proc_ass pa where " +
                        "(id_utente = {0} or " +
                        "(id_ufficio in (select id_ufficio from istruttori_uffici where id_utente = {0}))) " +
                        "and ((data_assegnazione <= {1} and data_rimozione is null) or ({1} between data_assegnazione and data_rimozione)) " +
                        "and (autorizzazioni like {2}) " +
                        "order by id_utente desc, id_ufficio desc)) as x " +
                        "order by id_tipo_proc is null, id_tipo_proc, " +
                        "id_proc is null, id_proc, " +
                        "id_utente is null, id_utente, " +
                        "id_ufficio is null, id_ufficio, " +
                        "autorizzazioni<>'X', autorizzazioni";
        List<Autorizzazione> lAuth = ctx.selectFrom(sql, user.getUtente().getIdUtente(), LocalDateTime.now(), likeOp).fetchInto(Autorizzazione.class);
        
        ElencoVisibili ev = new ElencoVisibili();        
        List<Autorizzazione> lTipoProcNotIn = new ArrayList<>(); 
        for (int i = 0; i < lAuth.size(); i++) {
            Autorizzazione a = lAuth.get(i);
            if (a.getIdTipoProc() != null) {
                if (safeContains(a.getAutorizzazioni(), "X")) {
                    lTipoProcNotIn.add(a);
                    while (i < lAuth.size() && a.getIdTipoProc().equals(lAuth.get(i).getIdTipoProc())) i++;
                    i--;
                } else {
                    ev.getIdTipiProc().add(a.getIdTipoProc());
                }
            } else {
                if (safeContains(a.getAutorizzazioni(), "X")) {
                    ev.getIdProcEsclusi().add(a.getIdProc());
                    while (i < lAuth.size() && a.getIdProc().equals(lAuth.get(i).getIdProc())) i++;
                    i--;
                } else {
                    if(!negata(lTipoProcNotIn, a)) {
                        ev.getIdProcVisibili().add(a.getIdProc());
                    }
                }
            }
        }
        
        return ev;
    }
    
    private boolean safeContains(String where, String what) {
        what = (what!=null) ? what.toUpperCase() : null;
        return (where!=null) ? where.toUpperCase().contains(what) : false;
    }
    
    private boolean negata(List<Autorizzazione> esclusi, Autorizzazione a) {
        if(esclusi.isEmpty()) return false; // nessuna esclusione, procedura ammessa
        
        for(Autorizzazione e : esclusi) {
            if(Objects.equals(e.getIdTipoProc(), a.getIdTipoProcRif())) {
                if(e.getIdUtente()!=null && a.getIdUtente()!=null) return true;  // esclude la singola procedura per l'utente specifico
                if(e.getIdUtente()!=null && a.getIdUtente()==null) return true;  // il tipo procedura è escluso per l'utente
                if(e.getIdUtente()==null && a.getIdUtente()!=null) return false; // esclusione a livello di ufficio ma non di utenza
                if(e.getIdUtente()==null && a.getIdUtente()==null) return true;  // esclusde la singola procedura per l'ufficio
            }
        }
        
        return true; // negazione per default
    }
    
    public boolean isAuthorized(Utente user, LTipoProcRecord tipoProc, TipoOperazioneEnum tipoOp) {
        return isAuthorized(user, tipoProc, new TipoOperazioneEnum[]{tipoOp});
    }
    
    public boolean isAuthorized(Utente user, LTipoProcRecord tipoProc, TipoOperazioneEnum[] tipoOp) {
        String likeOp = fixSecurityAuth(arrayToString(tipoOp));
        LocalDateTime now = LocalDateTime.now();
        return ctx.select(DSL.count())
                .from(L_TIPO_PROC)
                .join(TIPO_PROC_ASS)
                    .on(L_TIPO_PROC.ID_TIPO_PROC.eq(TIPO_PROC_ASS.ID_TIPO_PROC))
                .join(DELEGA)
                    .on(TIPO_PROC_ASS.ID_UTENTE.eq(DELEGA.ID_DELEGANTE).and(TIPO_PROC_ASS.ID_TIPO_PROC.eq(DELEGA.ID_TIPO_PROC)))
                .where(DELEGA.ID_UTENTE.eq(user.getUtente().getIdUtente()))
                    .and(L_TIPO_PROC.ID_TIPO_PROC.eq(tipoProc.getIdTipoProc()))
                    .and(TIPO_PROC_ASS.AUTORIZZAZIONI.like(likeOp))
                    .and(TIPO_PROC_ASS.DATA_ASSEGNAZIONE.le(now).and(TIPO_PROC_ASS.DATA_RIMOZIONE.isNull()))
                    .or(DSL.val(now).between(TIPO_PROC_ASS.DATA_ASSEGNAZIONE, TIPO_PROC_ASS.DATA_RIMOZIONE))
                .fetchOneInto(Long.class)>0;
    }

    public boolean isAuthorized(Utente user, TipoOperazioneEnum tipoOp) {
        return isAuthorized(user, new TipoOperazioneEnum[]{tipoOp});
    }
    
    public boolean isAuthorized(Utente user, TipoOperazioneEnum[] tipoOp) {
        String likeOp = fixSecurityAuth(arrayToString(tipoOp));
        LocalDateTime now = LocalDateTime.now();
        return ctx.select(DSL.count())
                .from(L_TIPO_PROC)
                .join(TIPO_PROC_ASS)
                    .on(L_TIPO_PROC.ID_TIPO_PROC.eq(TIPO_PROC_ASS.ID_TIPO_PROC))                
                .join(DELEGA)
                    .on(TIPO_PROC_ASS.ID_UTENTE.eq(DELEGA.ID_DELEGANTE).and(TIPO_PROC_ASS.ID_TIPO_PROC.eq(DELEGA.ID_TIPO_PROC)))    
                .where(DELEGA.ID_UTENTE.eq(user.getUtente().getIdUtente()))                    
                    .and(TIPO_PROC_ASS.AUTORIZZAZIONI.like(likeOp))
                    .and(TIPO_PROC_ASS.DATA_ASSEGNAZIONE.le(now).and(TIPO_PROC_ASS.DATA_RIMOZIONE.isNull()))
                    .or(DSL.val(now).between(TIPO_PROC_ASS.DATA_ASSEGNAZIONE, TIPO_PROC_ASS.DATA_RIMOZIONE))
                .fetchOneInto(Long.class)>0;
    }
    
    public boolean isAuthorized(Utente user, ProcRecord proc, TipoOperazioneEnum tipoOp) {
        return isAuthorized(user, proc, new TipoOperazioneEnum[]{tipoOp});
    }
    
    public boolean isAuthorized(Utente user, ProcRecord proc, TipoOperazioneEnum[] tipoOp) {
        String likeOp = fixSecurityAuth(arrayToString(tipoOp));
        LocalDateTime now = LocalDateTime.now();
        return ctx.select(DSL.count())
                .from(PROC)
                .join(PROC_ASS)
                    .on(PROC.ID_PROC.eq(PROC_ASS.ID_PROC))                
                .where(PROC_ASS.ID_UTENTE.eq(user.getUtente().getIdUtente()))
                    .and(PROC_ASS.AUTORIZZAZIONI.like(likeOp))
                    .and(PROC_ASS.DATA_ASSEGNAZIONE.le(now).and(PROC_ASS.DATA_RIMOZIONE.isNull()))
                    .or(DSL.val(now).between(PROC_ASS.DATA_ASSEGNAZIONE, PROC_ASS.DATA_RIMOZIONE))
                .fetchOneInto(Long.class)>0;
    }
    
    public String fixSecurityAuth(String auth) {
        final String[] order = {"L", "A", "M", "E", "X"};
        List<String> lAuth = new ArrayList<>();
        for(String c : order) {
            for(int i=0;i<auth.toUpperCase().length();i++) {
                String s = String.valueOf(auth.charAt(i));
                if(c.equals(s) && !lAuth.contains(s)) lAuth.add(s);
            }
        }
        StringJoiner sj = new StringJoiner("%", "%", "%");
        lAuth.forEach(a -> sj.add(a));
        return sj.toString();
    }
    
    public String arrayToString(TipoOperazioneEnum[] ops) {
        if(ops==null) return "";
        LinkedHashSet<String> las = new LinkedHashSet<>();
        for(TipoOperazioneEnum v : ops) las.add(v.toString());
        StringJoiner sj = new StringJoiner("");
        for(String s : las) sj.add(s);
        return sj.toString();
    } 
}
