/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;


import static it.usr.web.usromniapp.domain.Tables.*;
import it.usr.web.usromniapp.domain.tables.records.DelegaRecord;
import it.usr.web.usromniapp.domain.tables.records.LRuoloRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcAssAttualiRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcAssRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcCatRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcIterRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcPassoInizialeRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.domain.tables.records.VAssegnantiAttiviRecord;
import it.usr.web.usromniapp.interceptor.LogDatabaseOperation;
import it.usr.web.usromniapp.model.ElencoVisibili;
import it.usr.web.usromniapp.model.ProcAssegnata;
import it.usr.web.usromniapp.model.TipoProcAssModel;
import it.usr.web.usromniapp.model.Utente;
import it.usr.web.usromniapp.model.UtenteRuolo;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import org.jooq.DSLContext;
import it.usr.web.usromniapp.producer.DSLCtx;
import it.usr.web.usromniapp.record.RProc;
import it.usr.web.usromniapp.record.RProcIter;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jooq.Condition;
import org.jooq.Records;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) 
public class ProcedimentoService {
    public final static String TOOL_AUTOMATICO = "tool.automatico";
    @Inject
    @DSLCtx
    DSLContext ctx;
    @Inject
    CodiceService cs;
    @Inject 
    SecurityService ss;

    public List<LTipoProcRecord> getTipiProcedimento() {
        return ctx.selectFrom(L_TIPO_PROC).orderBy(L_TIPO_PROC.TIPO_PROC).fetchInto(L_TIPO_PROC);
    }
    
    public ProcAssRecord getAssegnazionebyId(int idProcAss) {
        return ctx.selectFrom(PROC_ASS).where(PROC_ASS.ID_PROC_ASS.eq(idProcAss)).fetchSingle();
    }
    
    public List<LTipoProcRecord> getTipiProcedimento(Utente utente) {
        LocalDateTime now = LocalDateTime.now();
        return ctx.select(L_TIPO_PROC.fields())
                .from(L_TIPO_PROC)
                .join(TIPO_PROC_ASS)
                .on(L_TIPO_PROC.ID_TIPO_PROC.eq(TIPO_PROC_ASS.ID_TIPO_PROC))
                .join(DELEGA)
                .on(DELEGA.ID_DELEGANTE.eq(TIPO_PROC_ASS.ID_UTENTE).and(DELEGA.ID_TIPO_PROC.eq(TIPO_PROC_ASS.ID_TIPO_PROC)))
                .where(DELEGA.ID_UTENTE.eq(utente.getUtente().getIdUtente()))
                .and(TIPO_PROC_ASS.DATA_ASSEGNAZIONE.le(now).and(TIPO_PROC_ASS.DATA_RIMOZIONE.isNull()))
                .or(DSL.val(now).between(TIPO_PROC_ASS.DATA_ASSEGNAZIONE, TIPO_PROC_ASS.DATA_RIMOZIONE))
                .fetchInto(L_TIPO_PROC);
    }

    public LTipoProcRecord getTipoProcedimento(int id) {
        return ctx.selectFrom(L_TIPO_PROC).where(L_TIPO_PROC.ID_TIPO_PROC.eq(id)).fetchSingle();
    }

    public LTipoProcRecord getTipoProcedimento(String procedimento) {
        return ctx.selectFrom(L_TIPO_PROC).where(L_TIPO_PROC.TIPO_PROC.eq(procedimento)).fetchSingle();
    }

    public Map<Integer, LTipoProcRecord> getTipiProcedimentoMap() {
        Map<Integer, LTipoProcRecord> mMap = new HashMap<>();
        ctx.selectFrom(L_TIPO_PROC).fetch().forEach(e -> mMap.put(e.getIdTipoProc(), e));
        return mMap;
    }

    @LogDatabaseOperation
    public int inserisci(ProcRecord proc, List<ProcCatRecord> procCat, ProcIterRecord procIter) throws DatabaseException {
        try {
            AtomicInteger ai = new AtomicInteger();
            ctx.transaction(trx -> {
                proc.setCodice(cs.generaCodiceVLO(trx, proc.getIdTipoProc()));
                trx.dsl().insertInto(PROC).set(proc).execute();
                int idProc = trx.dsl().lastID().intValue();

                procCat.forEach(pc -> {
                    pc.setIdProc(idProc);
                    trx.dsl().insertInto(PROC_CAT).set(pc).execute();
                });

                ProcPassoInizialeRecord passoIniz = trx.dsl().selectFrom(PROC_PASSO_INIZIALE).where(PROC_PASSO_INIZIALE.ID_TIPO_PROC.eq(proc.getIdTipoProc())).fetchOne();
                if (passoIniz == null) {
                    throw new DatabaseException("Impossibile determinare il passo di iter iniziale per la procedura.");
                }

                procIter.setIdProc(idProc);
                procIter.setCodicePasso(passoIniz.getCodicePasso());
                trx.dsl().insertInto(PROC_ITER).set(procIter).execute();
                int idProcIter = trx.dsl().lastID().intValue();

                //int idTool = getToolAutomatico(trx.dsl()).getIdUtente();
                //int idRuoloAss = trx.dsl().selectFrom(L_RUOLO).where(L_RUOLO.CODICE.eq("ASS")).fetchSingle().getIdRuolo();
                
                // inserire tante righe in proc_ass pari al risultato di v_assegnanti_attivi con id_tipo_proc indicato e autorizzazione LA
                List<VAssegnantiAttiviRecord> lAssegnanti = trx.dsl().selectFrom(V_ASSEGNANTI_ATTIVI).where(V_ASSEGNANTI_ATTIVI.ID_TIPO_PROC.eq(proc.getIdTipoProc())).fetch();
                
                LocalDateTime dataAss = LocalDateTime.now();
                for(var ass : lAssegnanti) {                                    
                    ProcAssRecord pas = new ProcAssRecord();
                    pas.setIdProc(idProc);
                    pas.setIdUtente(ass.getIdUtente());
                    pas.setIdRuolo(ass.getIdRuolo());
                    pas.setAutorizzazioni(ass.getAutorizzazioni());
                    pas.setIdUtenteAssegnante(ass.getIdUtenteAssegnante());
                    pas.setIdUtenteDelegante(ass.getIdDelegante());
                    pas.setDataAssegnazione(dataAss); 
                    
                    // gatantisce l'ordine di assegnazione
                    dataAss = dataAss.plusSeconds(1L);

                    trx.dsl().insertInto(PROC_ASS).set(pas).execute();
                }
                
                // aggiorna gli assegnatari attuali
                if(!lAssegnanti.isEmpty()) {
                    var ass = lAssegnanti.get(lAssegnanti.size()-1);
                    ProcAssAttualiRecord paar = new ProcAssAttualiRecord();
                    paar.setIdProc(idProc);
                    paar.setIdUtente(ass.getIdUtente());
                    paar.setIdRuolo(ass.getIdRuolo());

                    trx.dsl().insertInto(PROC_ASS_ATTUALI).set(paar).execute();
                }

                // aggiorna il collegamento conl'iter iniziale
                trx.dsl().update(PROC).set(PROC.ID_PROC_ITER_ULTIMO, idProcIter).where(PROC.ID_PROC.eq(idProc)).execute();

                ai.set(idProc);
            });

            return ai.get();
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }
    }
        
    public List<RProc> getAssegnazioni(Utente utente) {
        ElencoVisibili ev = ss.getElencoVisibili(utente, TipoOperazioneEnum.M);
        Condition tipoProcCond = DSL.noCondition();
        Condition procCond = DSL.noCondition(); 

        // Permette di vedere le sole tipologie di procedure ammesse
        if (!ev.getIdTipiProc().isEmpty()) {
            tipoProcCond = tipoProcCond.and(PROC.ID_TIPO_PROC.in(ev.getIdTipiProc()));
        }        
        // Mostra solo le procedure permesse
        if (!ev.getIdProcVisibili().isEmpty()) {
            procCond = procCond.and(PROC.ID_PROC.in(ev.getIdProcVisibili()));
        }
        // Escludi quelle espressamente negate
        if (!ev.getIdProcEsclusi().isEmpty()) {
            procCond = procCond.and(PROC.ID_PROC.notIn(ev.getIdProcEsclusi()));
        }
                        
        return ctx.select(
                    PROC.ID_PROC, PROC.ID_TIPO_PROC, PROC.DATAORA, PROC.LAT, PROC.LON, PROC.CODICE, PROC.RICHIEDENTE, PROC.INDIRIZZO, PROC.CODICE_COM, PROC.DESCRIZIONE, PROC.NOTE, PROC.ID_PROC_ITER_ULTIMO,
                    DSL.row(PROC_ITER.ID_PROC_ITER, PROC_ITER.ID_PROC, PROC_ITER.DATAORA, 
                            PROC_ITER.ATTIVO, PROC_ITER.MODIFICABILE, PROC_ITER.ID_UTENTE, 
                            PROC_ITER.CODICE_PASSO, PROC_ITER.PROT, PROC_ITER.DATA_PROT,
                            PROC_ITER.N_MUDE, PROC_ITER.ANNOTAZIONI, PROC_ITER.ID_ESITO,
                            PROC_ITER.ID_PROC_ITER_LINK, PROC_ITER.GIORNI_SOSPENSIONE).mapping(RProcIter::new)
                )
                .from(PROC).join(PROC_ITER).on(PROC.ID_PROC_ITER_ULTIMO.eq(PROC_ITER.ID_PROC_ITER)).where(tipoProcCond).or(procCond).fetch(Records.mapping(RProc::new));        
    }

    public List<ProcRecord> getProcedureDaAssegnare(int idDelegato, int idTipoProc, LocalDateTime when) {
        String sql = """
                     select distinct p.*, gc.comune, group_concat(concat(ut.nome_utente, ' (', lr.ruolo, ')') separator '|') as assegnatari
                     from proc p 
                     join proc_ass pa 
                        on p.id_proc = pa.id_proc 
                     join gis_centroidi gc 
                        on gc.codice_com = p.codice_com     
                     left join proc_ass_attuali paa 
                        on p.id_proc = paa.id_proc     
                     left join utenti ut 
                        on paa.id_utente = ut.id_utente 
                     left join l_ruolo lr 
                        on paa.id_ruolo = lr.id_ruolo     
                     where  
                        (pa.id_utente = {0}) and 
                         (p.id_tipo_proc = {1}) and 
                         (pa.autorizzazioni like '%M%' or pa.autorizzazioni like '%A%') and 
                         ((pa.data_assegnazione <= {2} and pa.data_rimozione is null) or ({2} between pa.data_assegnazione and pa.data_rimozione))      
                     and not exists (
                        select pass.id_proc 
                         from proc_ass pass 
                         where 
                            pass.id_proc = p.id_proc and 
                            pass.id_utente_delegante = {0} and 
                            ((pass.data_assegnazione <= {2} and pass.data_rimozione is null) or ({2} between pass.data_assegnazione and pass.data_rimozione)) and 
                            (pass.autorizzazioni like '%M%' or pass.autorizzazioni like '%A%'))        
                     group by id_proc
                     """;

        return ctx.fetch(sql, idDelegato, idTipoProc, when).into(ProcRecord.class);
    }

    public List<ProcAssegnata> getProcedureAssegnate(int idDelegato, int idTipoProc, LocalDateTime when) {
        String sql = """
                     select distinct p.*, gc.comune, group_concat(concat(ut.nome_utente, ' (', lr.ruolo, ')') separator '|') as assegnatari
                     from proc p 
                     join proc_ass pa 
                        on p.id_proc = pa.id_proc 
                     join gis_centroidi gc 
                        on gc.codice_com = p.codice_com     
                     left join proc_ass_attuali paa 
                        on p.id_proc = paa.id_proc     
                     left join utenti ut 
                        on paa.id_utente = ut.id_utente 
                     left join l_ruolo lr 
                        on paa.id_ruolo = lr.id_ruolo     
                     where 
                        (pa.id_utente = {0}) and 
                         (p.id_tipo_proc = {1}) and 
                         (pa.autorizzazioni like '%A%') and 
                         ((pa.data_assegnazione <= {2} and pa.data_rimozione is null) or ({2} between pa.data_assegnazione and pa.data_rimozione))      
                     and exists (
                        select pass.id_proc 
                         from proc_ass pass 
                         where 
                            pass.id_proc = p.id_proc and 
                            pass.id_utente_delegante = {0} and 
                            ((pass.data_assegnazione <= {2} and pass.data_rimozione is null) or ({2} between pass.data_assegnazione and pass.data_rimozione)) and 
                            (pass.autorizzazioni like '%M%' or pass.autorizzazioni like '%A%'))        
                     group by id_proc
                     """;

        return ctx.fetch(sql, idDelegato, idTipoProc, when).into(ProcAssegnata.class); 
    }
 
    @LogDatabaseOperation
    public void inserisciAssegnazioni(List<ProcRecord> procedimentiSelezionati, List<UtenteRuolo> target, UtentiRecord user, UtentiRecord delegato) throws DatabaseException {
        try {
            LocalDateTime now = LocalDateTime.now();
            ctx.transaction(trx -> {
                procedimentiSelezionati.forEach(proc -> {
                    // ripulisce assegnazioni attuali
                    trx.dsl().deleteFrom(PROC_ASS_ATTUALI).where(PROC_ASS_ATTUALI.ID_PROC.eq(proc.getIdProc())).execute();
                    
                    target.forEach(ass -> {
                        ProcAssRecord par = new ProcAssRecord();
                        par.setIdProc(proc.getIdProc());
                        par.setIdUtenteAssegnante(user.getIdUtente());
                        par.setIdUtenteDelegante(delegato.getIdUtente());
                        par.setDataAssegnazione(now);

                        LRuoloRecord lr = trx.dsl().selectFrom(L_RUOLO).where(L_RUOLO.ID_RUOLO.eq(ass.getIdRuolo())).fetchSingle();

                        par.setIdUtente(ass.getIdUtente());
                        par.setIdRuolo(ass.getIdRuolo());
                        par.setAutorizzazioni(lr.getPermessiPredefiniti());

                        trx.dsl().insertInto(PROC_ASS).set(par).execute();
                                                                        
                        ProcAssAttualiRecord paar = new ProcAssAttualiRecord();
                        paar.setIdProc(proc.getIdProc());
                        paar.setIdUtente(ass.getIdUtente());
                        paar.setIdRuolo(ass.getIdRuolo());
                        
                        trx.dsl().insertInto(PROC_ASS_ATTUALI).set(paar).execute();
                    });
                }); 
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }
    }
    
    @LogDatabaseOperation
    public void modificaAssegnazioni(ProcAssegnata proc, List<UtenteRuolo> daRimuovere, List<UtenteRuolo> daAggiungere, UtentiRecord utente, UtentiRecord delegato, LocalDateTime dataOra) throws DatabaseException {
         try {
            LocalDateTime now = dataOra;
            
            ctx.transaction(trx -> {
                daRimuovere.forEach(remove -> {
                    // Conserva l'autorizzazione che si sta rimuovendo
                    ProcAssRecord procAss = trx.dsl().selectFrom(PROC_ASS)
                            .where(PROC_ASS.ID_PROC.eq(proc.getIdProc()))
                            .and(PROC_ASS.ID_UTENTE.eq(remove.getIdUtente()))
                            .and(PROC_ASS.ID_RUOLO.eq(remove.getIdRuolo()))
                            .and(PROC_ASS.DATA_ASSEGNAZIONE.le(now).and(PROC_ASS.DATA_RIMOZIONE.isNull()))
                            .or(DSL.val(now).between(PROC_ASS.DATA_ASSEGNAZIONE, PROC_ASS.DATA_RIMOZIONE))
                            .fetchSingle();
                    
                    // Rimuove l'autorizzazione
                    trx.dsl().update(PROC_ASS)
                                .set(PROC_ASS.DATA_RIMOZIONE, now) 
                                .set(PROC_ASS.ID_UTENTE_RIMOZIONE, utente.getIdUtente())
                            .where(PROC_ASS.ID_PROC.eq(proc.getIdProc()))
                            .and(PROC_ASS.ID_UTENTE.eq(remove.getIdUtente()))
                            .and(PROC_ASS.ID_RUOLO.eq(remove.getIdRuolo()))
                            .execute();
                    
                    // la precedente autorizzazione era una "A" (assegnante)? Si, allora rimuovi a cascata tutte le autorizzazioni fatte a quell'utente
                    if(procAss.getAutorizzazioni().contains("A")) {
                        // elenca tutte le autorizzazioni figlie di quella rimossa
                        String sql = "WITH RECURSIVE q AS (" +
                                    "        SELECT  * " +
                                    "        FROM    proc_ass " +
                                    "        WHERE   id_proc_ass = {0}" +
                                    "	UNION ALL " +
                                    "        SELECT  m.* " +
                                    "        FROM    proc_ass AS m " +
                                    "        JOIN    q " +
                                    "        ON      m.id_utente_delegante = q.id_utente " +
                                    //"   UNION ALL " +
                                    //"       SELECT  n.* " +
                                    //"        FROM    proc_ass AS n " +
                                    //"        JOIN    q " +
                                    //"        ON      n.id_utente_assegnante = q.id_utente " +
                                    ") SELECT DISTINCT q.id_proc_ass FROM q";
                        List<Integer> procAssUpd = trx.dsl().fetch(sql, procAss.getIdProcAss()).into(Integer.class);
                        
                        // effettua aggiornamento
                        trx.dsl().update(PROC_ASS)
                                .set(PROC_ASS.DATA_RIMOZIONE, now)
                                .set(PROC_ASS.ID_UTENTE_RIMOZIONE, utente.getIdUtente())
                                .where(PROC_ASS.ID_PROC_ASS.in(procAssUpd))
                                .and(PROC_ASS.DATA_RIMOZIONE.isNull())
                                .execute();                        
                    }
                });
                
                // inserisci le nuove
                daAggiungere.forEach(ass -> {
                    ProcAssRecord par = new ProcAssRecord();
                    par.setIdProc(proc.getIdProc());
                    par.setIdUtenteAssegnante(utente.getIdUtente());
                    par.setIdUtenteDelegante(delegato.getIdUtente());
                    par.setDataAssegnazione(now);

                    LRuoloRecord lr = trx.dsl().selectFrom(L_RUOLO).where(L_RUOLO.ID_RUOLO.eq(ass.getIdRuolo())).fetchSingle();

                    par.setIdUtente(ass.getIdUtente());
                    par.setIdRuolo(ass.getIdRuolo());
                    par.setAutorizzazioni(lr.getPermessiPredefiniti());

                    trx.dsl().insertInto(PROC_ASS).set(par).execute();                   
                });
                
                // ripulisce assegnazioni attuali
                trx.dsl().deleteFrom(PROC_ASS_ATTUALI).where(PROC_ASS_ATTUALI.ID_PROC.eq(proc.getIdProc())).execute();
                
                // incrementa di 1 secondo per escludere quelli appena inseriti
                LocalDateTime nowcheck = now.plusSeconds(1);
                // inserisce le nuove assegnazioni più recenti - parte dalle "M"
                List<ProcAssRecord> lNew = trx.dsl().selectFrom(PROC_ASS)
                                                    .where(PROC_ASS.ID_PROC.eq(proc.getIdProc()))
                                                    .and(PROC_ASS.AUTORIZZAZIONI.like("%M%"))
                                                    .and(PROC_ASS.DATA_ASSEGNAZIONE.le(nowcheck).and(PROC_ASS.DATA_RIMOZIONE.isNull()))
                                                    .or(DSL.val(nowcheck).between(PROC_ASS.DATA_ASSEGNAZIONE, PROC_ASS.DATA_RIMOZIONE))
                                                    .orderBy(PROC_ASS.DATA_ASSEGNAZIONE.desc())
                                                    .fetch();
                lNew.forEach(ass -> {
                    ProcAssAttualiRecord paar = new ProcAssAttualiRecord();
                    paar.setIdProc(proc.getIdProc());
                    paar.setIdUtente(ass.getIdUtente());
                    paar.setIdRuolo(ass.getIdRuolo());
                    
                    trx.dsl().insertInto(PROC_ASS_ATTUALI).set(paar).execute();
                });
                
                // nessuna M inserita?
                if(lNew.isEmpty()) {
                    // Procede con le "A"
                    lNew = trx.dsl().selectFrom(PROC_ASS)
                                    .where(PROC_ASS.ID_PROC.eq(proc.getIdProc()))
                                    .and(PROC_ASS.AUTORIZZAZIONI.like("%A%"))
                                    //.and(PROC_ASS.ID_UTENTE.ne(delegato.getIdUtente())) // evita l'estrazione di se stesso
                                    .and(PROC_ASS.DATA_ASSEGNAZIONE.le(nowcheck).and(PROC_ASS.DATA_RIMOZIONE.isNull()))
                                    .or(DSL.val(nowcheck).between(PROC_ASS.DATA_ASSEGNAZIONE, PROC_ASS.DATA_RIMOZIONE))
                                    .orderBy(PROC_ASS.DATA_ASSEGNAZIONE.desc())
                                    .fetch();
                    // Nulla? 
                    if(!lNew.isEmpty()) {
                        // Inserisci solo la più recente delle "A"
                        ProcAssRecord ass = lNew.get(0);
                        
                        ProcAssAttualiRecord paar = new ProcAssAttualiRecord();
                        paar.setIdProc(proc.getIdProc());
                        paar.setIdUtente(ass.getIdUtente());
                        paar.setIdRuolo(ass.getIdRuolo());
                    
                        trx.dsl().insertInto(PROC_ASS_ATTUALI).set(paar).execute();
                    }
                    else {
                        // condizione di guardia...non *DOVREBBE* mai verificarsi...effettuare dei log
                        throw new DataAccessException("Non risulta nessuna assegnazione recente per la procedura indicata.");
                    }
                }
                
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }         
    }
    
    public List<UtenteRuolo> getAssegnazioniAttualiProcedura(int idDelegante, int idProc) {
        /*return ctx.select(RUOLI_UTENTE.ID_RUOLI_UTENTE, RUOLI_UTENTE.ID_UTENTE, RUOLI_UTENTE.ID_RUOLO, UTENTI.NOME_UTENTE, L_RUOLO.RUOLO).from(RUOLI_UTENTE)
                .join(PROC_ASS_ATTUALI).on(RUOLI_UTENTE.ID_UTENTE.eq(PROC_ASS_ATTUALI.ID_UTENTE)).and(RUOLI_UTENTE.ID_RUOLO.eq(PROC_ASS_ATTUALI.ID_RUOLO))
                .join(UTENTI).on(RUOLI_UTENTE.ID_UTENTE.eq(UTENTI.ID_UTENTE))
                .join(L_RUOLO).on(RUOLI_UTENTE.ID_RUOLO.eq(L_RUOLO.ID_RUOLO))                
                .where(PROC_ASS_ATTUALI.ID_PROC.eq(idProc)).fetchInto(UtenteRuolo.class);*/
        String sql = """
                     SELECT ru.id_ruoli_utente, pa.id_utente, pa.id_ruolo, u.nome_utente, r.ruolo 
                     FROM proc_ass pa 
                        join ruoli_utente ru 
                          on (ru.id_ruolo = pa.id_ruolo and ru.id_utente = pa.id_utente) 
                        join utenti u 
                          on pa.id_utente = u.id_utente 
                        join l_ruolo r 
                          on pa.id_ruolo = r.id_ruolo     
                     where 
                         -- pa.id_utente_delegante = {0} 
                         pa.autorizzazioni like '%M%'
                         and pa.id_proc = {1} 
                         and ((pa.data_assegnazione <= now() and pa.data_rimozione is null) or (now() between pa.data_assegnazione and pa.data_rimozione))""";
        return ctx.fetch(sql, idDelegante, idProc).into(UtenteRuolo.class);
    }
    
    public UtentiRecord getToolAutomatico(DSLContext _ctx) {
        return _ctx.selectFrom(UTENTI).where(UTENTI.UTENTE.eq(TOOL_AUTOMATICO)).fetchSingle();
    } 
    
    public UtentiRecord getToolAutomatico() {
        return getToolAutomatico(ctx);
    }
    
    /**
     * Restitusce l'elenco dei tipi procedura per cui si è autorizzati in assegnazione sia direttamente
     * che per delega alla data attuale
     * 
     * @param utente
     * @return 
     */
    public List<LTipoProcRecord> getTipiProcedureAutorizzate(Utente utente) {
        String sql = "SELECT distinct tp.* FROM l_tipo_proc tp join decreti.tipo_proc_ass pa on tp.id_tipo_proc = pa.id_tipo_proc where " +
               "pa.id_utente = {0} or id_utente in (" +
               "select id_delegante from delega d where d.id_utente = {0} and d.id_tipo_proc = id_tipo_proc " +
               "and ((pa.data_assegnazione <= {1} and pa.data_rimozione is null) or ({1} between pa.data_assegnazione and pa.data_rimozione)) " +
               ") " +
               "and (pa.autorizzazioni like '%A%') " +
               "and ((pa.data_assegnazione <= {1} and pa.data_rimozione is null) or ({1} between pa.data_assegnazione and pa.data_rimozione))";
        
        return ctx.fetch(sql, utente.getUtente().getIdUtente(), LocalDateTime.now()).into(LTipoProcRecord.class);
    }      
    
    public List<TipoProcAssModel> getTipiProcAssModel(int idTipoProc) {
        String sql = """
                     select tpa.id_tipo_proc_ass, tpa.id_utente, tpa.id_ruolo, tpa.id_ufficio, tpa.id_utente_assegnante, ut.nome_utente, r.ruolo, uf.ufficio, tpa.autorizzazioni, ut2.nome_utente as assegnante, tpa.data_assegnazione, tpa.note
                     from tipo_proc_ass tpa 
                     left join utenti ut
                        on tpa.id_utente = ut.id_utente
                     left join uffici uf
                        on tpa.id_ufficio = uf.id_ufficio
                     left join l_ruolo r
                        on tpa.id_ruolo = r.id_ruolo
                     inner join utenti ut2
                        on tpa.id_utente_assegnante = ut2.id_utente
                     where id_tipo_proc = {0}
                     """;
        return ctx.fetch(sql, idTipoProc).into(TipoProcAssModel.class);
    }

    public ProcRecord getProcedimentoById(Integer idProc) {
        return ctx.selectFrom(PROC).where(PROC.ID_PROC.eq(idProc)).fetchOne();
    }
    
    public List<DelegaRecord> getDeleghe(int idTipoProc, int idDelegante) {
        return ctx.selectFrom(DELEGA).where(DELEGA.ID_TIPO_PROC.eq(idTipoProc)).and(DELEGA.ID_DELEGANTE.eq(idDelegante)).fetch();
    }
}
 