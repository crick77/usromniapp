/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;


import it.usr.web.service.BaseService;
import static it.usr.web.usromniapp.domain.Tables.*;
import it.usr.web.usromniapp.domain.tables.records.LRuoloRecord;
import it.usr.web.usromniapp.domain.tables.records.LRuoloTecnicoRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcAclIstruttoriRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcAclRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcCatRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcIncarichiRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcIterRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcPassoInizialeRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcTecniciRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.domain.tables.records.VProcAclAttiviRecord;
import it.usr.web.usromniapp.interceptor.LogDatabaseOperation;
import it.usr.web.usromniapp.model.ACL;
import it.usr.web.usromniapp.model.ProcAssegnata;
import it.usr.web.usromniapp.model.StatoDiFatto;
import it.usr.web.usromniapp.model.TecnicoRuolo;
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
public class ProcedimentoService extends BaseService {
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
    
    public Map<Integer, LTipoProcRecord> getTipiProcedimentoMap() {
        return ctx.selectFrom(L_TIPO_PROC).fetchMap(L_TIPO_PROC.ID_TIPO_PROC);        
    }
           
    public LTipoProcRecord getTipoProcedimentoById(int id) {
        return ctx.selectFrom(L_TIPO_PROC).where(L_TIPO_PROC.ID_TIPO_PROC.eq(id)).fetchSingle();
    }

    public LTipoProcRecord getTipoProcedimentoByNome(String procedimento) {
        return ctx.selectFrom(L_TIPO_PROC).where(L_TIPO_PROC.TIPO_PROC.eq(procedimento)).fetchSingle();
    }
       
    @LogDatabaseOperation
    public int inserisciProcedimento(ProcRecord proc, List<ProcCatRecord> procCat, List<TecnicoRuolo> procTecnici, ProcIterRecord procIter) throws DatabaseException {
        try {
            int A = TipoOperazioneEnum.A.getOperazione();
            AtomicInteger ai = new AtomicInteger();
            ctx.transaction(trx -> {
                // Determina il passo iniziale del tipo procedimento in oggetto
                ProcPassoInizialeRecord passoIniz = trx.dsl().selectFrom(PROC_PASSO_INIZIALE).where(PROC_PASSO_INIZIALE.ID_TIPO_PROC.eq(proc.getIdTipoProc())).fetchOne();
                if (passoIniz == null) {
                    throw new DatabaseException("Impossibile determinare il passo di iter iniziale per la procedura.");
                }
                
                // Genera il codice del nuovo procedimento
                proc.setCodice(cs.generaCodiceVLO(trx, proc.getIdTipoProc()));
                trx.dsl().insertInto(PROC).set(proc).execute();
                int idProc = trx.dsl().lastID().intValue();

                // Associa il nuovo procedimento a tutti i record di catastali
                procCat.forEach(pc -> {
                    pc.setIdProc(idProc);
                    trx.dsl().insertInto(PROC_CAT).set(pc).execute();
                });
                 
                // Aggiorna il primo passo di iter
                procIter.setIdProc(idProc);
                procIter.setCodicePasso(passoIniz.getCodicePasso());
                trx.dsl().insertInto(PROC_ITER).set(procIter).execute();
                int idProcIter = trx.dsl().lastID().intValue();

                //int idTool = getToolAutomatico(trx.dsl()).getIdUtente();
                //int idRuoloAss = trx.dsl().selectFrom(L_RUOLO).where(L_RUOLO.CODICE.eq("ASS")).fetchSingle().getIdRuolo();
                
                // inserire tante righe in proc_acl pari al risultato di v_proc_acl_attivi con id_tipo_proc indicato e autorizzazione LA
                List<VProcAclAttiviRecord> lAssegnanti = trx.dsl().selectFrom(V_PROC_ACL_ATTIVI).where(V_PROC_ACL_ATTIVI.ID_TIPO_PROC.eq(proc.getIdTipoProc()).and(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(A).eq(A))).fetch();
                
                LocalDateTime dataAss = LocalDateTime.now();
                for(var ass : lAssegnanti) {                                    
                    ProcAclRecord par = new ProcAclRecord();
                    par.setIdProc(idProc);
                    par.setIdUtente(ass.getIdUtente()); 
                    par.setIdUfficio(ass.getIdUfficio());
                    par.setIdRuolo(ass.getIdRuolo());
                    par.setAutorizzazioni(ass.getAutorizzazioni());
                    par.setIdUtenteAssegnante(ass.getIdUtenteAssegnante());
                    par.setIdUtenteDelegante(ass.getIdUtenteDelegante());
                    par.setDataAssegnazione(dataAss); 
                    
                    // gatantisce l'ordine di assegnazione
                    dataAss = dataAss.plusSeconds(1L);

                    trx.dsl().insertInto(PROC_ACL).set(par).execute();
                }
                
                // aggiorna gli assegnatari attuali
                if(!lAssegnanti.isEmpty()) {
                    var ass = lAssegnanti.get(lAssegnanti.size()-1);
                    ProcAclIstruttoriRecord paar = new ProcAclIstruttoriRecord();
                    paar.setIdProc(idProc);
                    paar.setIdUtente(ass.getIdUtente());
                    paar.setIdUfficio(ass.getIdUfficio());
                    paar.setIdRuolo(ass.getIdRuolo());

                    trx.dsl().insertInto(PROC_ACL_ISTRUTTORI).set(paar).execute();
                }

                // Inserisce gli incarichi e relativi ruoli connessi al procedimento
                procTecnici.forEach(pt -> {
                    ProcIncarichiRecord pir = new ProcIncarichiRecord();
                    pir.setIdProc(idProc);
                    pir.setIdTecnico(pt.getTecnico().getIdTecnico()); 
                    pir.setIdRuoloTecnico(pt.getRuolo().getIdRuoloTecnico());
                    
                    trx.dsl().insertInto(PROC_INCARICHI).set(pir).execute();
                });
                
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
    
    public List<RProc> getAssegnazioni(Utente utente, boolean soloPersonali) {
        ACL ev = ss.getACL(utente, new TipoOperazioneEnum[] { TipoOperazioneEnum.M });
        Condition condAmmesse = DSL.noCondition();
        Condition condNegate = DSL.noCondition(); 

        // Permette di vedere le sole tipologie di procedure ammesse
        if (!ev.getTipiProcedureAmmesse().isEmpty() && !soloPersonali) {
            condAmmesse = condAmmesse.and(PROC.ID_TIPO_PROC.in(ev.getTipiProcedureAmmesse()));
        }      
        // Mostra solo le procedure permesse
        if (!ev.getProcedureAmmesse().isEmpty()) {
            condAmmesse = condAmmesse.or(PROC.ID_PROC.in(ev.getProcedureAmmesse()));
        }
        
        // Esclude le tipologie espressamente vietate
        if(!ev.getTipiProcedureEscluse().isEmpty()) {
            condNegate = condNegate.and(PROC.ID_TIPO_PROC.notIn(ev.getTipiProcedureEscluse()));
        }
        
        // Escludi quelle espressamente negate
        if (!ev.getProcedureEscluse().isEmpty()) {
            condNegate = condNegate.and(PROC.ID_PROC.notIn(ev.getProcedureEscluse()));
        }
              
        var PROC_ITER_ESITO = PROC_ITER.as("PROC_ITER_ESITO");
        ctx.settings().withRenderGroupConcatMaxLenSessionVariable(false);
        return ctx.select(
                    PROC.ID_PROC, PROC.ID_TIPO_PROC, PROC.DATAORA, PROC.LAT, PROC.LON, PROC.CODICE, PROC.RICHIEDENTE, PROC.INDIRIZZO, PROC.CODICE_COM, PROC.DESCRIZIONE, PROC.NOTE, PROC.ID_PROC_ITER_ULTIMO, PROC.ID_PROC_ITER_ESITO,
                    DSL.groupConcatDistinct(PROC_CAT.FOGLIO).as("fogli"),
                    DSL.groupConcat(PROC_CAT.PARTICELLA).as("particelle"),
                    DSL.row(PROC_ITER.ID_PROC_ITER, PROC_ITER.ID_PROC, PROC_ITER.DATAORA, 
                            PROC_ITER.ATTIVO, PROC_ITER.MODIFICABILE, PROC_ITER.ID_UTENTE, 
                            PROC_ITER.CODICE_PASSO, PROC_ITER.PROT, PROC_ITER.DATA_PROT,
                            PROC_ITER.N_MUDE, PROC_ITER.ANNOTAZIONI, PROC_ITER.ID_ESITO,
                            PROC_ITER.ID_PROC_ITER_LINK, PROC_ITER.GIORNI_SOSPENSIONE).mapping(RProcIter::new),
                    DSL.row(PROC_ITER_ESITO.ID_PROC_ITER, PROC_ITER_ESITO.ID_PROC, PROC_ITER_ESITO.DATAORA, 
                            PROC_ITER_ESITO.ATTIVO, PROC_ITER_ESITO.MODIFICABILE, V_PROC_ACL_ATTIVI.ID_UTENTE, 
                            PROC_ITER_ESITO.CODICE_PASSO, PROC_ITER_ESITO.PROT, PROC_ITER_ESITO.DATA_PROT,
                            PROC_ITER_ESITO.N_MUDE, PROC_ITER_ESITO.ANNOTAZIONI, PROC_ITER_ESITO.ID_ESITO,
                            PROC_ITER_ESITO.ID_PROC_ITER_LINK, PROC_ITER_ESITO.GIORNI_SOSPENSIONE).mapping(RProcIter::new)
                )
                .from(PROC) 
                .join(PROC_ITER).on(PROC.ID_PROC_ITER_ULTIMO.eq(PROC_ITER.ID_PROC_ITER))
                .leftJoin(PROC_ITER_ESITO).on(PROC.ID_PROC_ITER_ESITO.eq(PROC_ITER_ESITO.ID_PROC_ITER))
                .leftJoin(PROC_CAT).on(PROC.ID_PROC.eq(PROC_CAT.ID_PROC))
                .where(condAmmesse).and(condNegate) 
                .groupBy(PROC.ID_PROC)
                .fetch(Records.mapping(RProc::new));        
    } 
  
    /**
     * Restituisce l'elenco delle pratiche in carico per un utente della tipologia specificata
     * 
     * @param utente utente destinatario delle pratiche
     * @param idTipoProc tipo procedura
     * @param privilegi privilegi richiesti
     * @param tipo tipologia di lavorazione (0 = da istruire, 1 = istruite, 2 = integrate, 3 = alla firma, 4 = concluse, 5 = concluse-revisione)
     * @return 
     */
    public List<RProc> getPraticheInCarico(Utente utente, Integer idTipoProc, TipoOperazioneEnum[] privilegi, int tipo) {
        //int LM = TipoOperazioneEnum.combina(TipoOperazioneEnum.L, TipoOperazioneEnum.M);
        int iPriv = TipoOperazioneEnum.combina(privilegi);
                        
        Condition cond = DSL.noCondition();
        
        if(idTipoProc!=null) cond = cond.and(PROC.ID_TIPO_PROC.eq(idTipoProc));
        
        switch(tipo) {
            case 0 -> { cond = cond.and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.isNull()); }
            case 1 -> { cond = cond.and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.isNotNull()).and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.eq(PROC.ID_PROC_ITER_ULTIMO)).and(PROC.ID_PROC_ITER_ESITO.isNull()); }
            case 2 -> { cond = cond.and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.isNotNull()).and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.ne(PROC.ID_PROC_ITER_ULTIMO)).and(PROC.ID_PROC_ITER_ESITO.isNull()); }
            case 3 -> { cond = cond.and(PROC.ID_PROC_ITER_FIRMA.isNotNull()); }
            case 4 -> { cond = cond.and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.isNotNull()).and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.eq(PROC.ID_PROC_ITER_ULTIMO)).and(PROC.ID_PROC_ITER_ESITO.isNotNull()); }
            case 5 -> { cond = cond.and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.isNotNull()).and(PROC.ID_PROC_ITER_ULTIMO_UFFICIO.ne(PROC.ID_PROC_ITER_ULTIMO)).and(PROC.ID_PROC_ITER_ESITO.isNotNull()); }
            default -> throw new IllegalArgumentException("Valore tipo non ammesso: "+tipo);
        }
        
        String sql = """
                        select distinct id_utente from (
                     		(select u.id_utente from utenti u join istruttori_uffici iu on u.id_utente = iu.id_utente where iu.id_ufficio in (SELECT id_ufficio FROM decreti.uffici where id_utente = {0}) and u.attivo = 1)
                     	union
                     		(select {0} as id_utente from dual)
                     	) as t;
                     """;
        List<Integer> lIds = ctx.fetch(sql, utente.getUtente().getIdUtente()).into(Integer.class);
                
        var PROC_ITER_ESITO = PROC_ITER.as("PROC_ITER_ESITO");
        ctx.settings().withRenderGroupConcatMaxLenSessionVariable(false);
        return ctx.select( 
                    PROC.ID_PROC, PROC.ID_TIPO_PROC, PROC.DATAORA, PROC.LAT, PROC.LON, PROC.CODICE, PROC.RICHIEDENTE, PROC.INDIRIZZO, PROC.CODICE_COM, PROC.DESCRIZIONE, PROC.NOTE, PROC.ID_PROC_ITER_ULTIMO, PROC.ID_PROC_ITER_ESITO,
                    DSL.groupConcatDistinct(PROC_CAT.FOGLIO).as("fogli"),
                    DSL.groupConcat(PROC_CAT.PARTICELLA).as("particelle"),
                    DSL.row(PROC_ITER.ID_PROC_ITER, PROC_ITER.ID_PROC, PROC_ITER.DATAORA, 
                            PROC_ITER.ATTIVO, PROC_ITER.MODIFICABILE, V_PROC_ACL_ATTIVI.ID_UTENTE, 
                            PROC_ITER.CODICE_PASSO, PROC_ITER.PROT, PROC_ITER.DATA_PROT,
                            PROC_ITER.N_MUDE, PROC_ITER.ANNOTAZIONI, PROC_ITER.ID_ESITO,
                            PROC_ITER.ID_PROC_ITER_LINK, PROC_ITER.GIORNI_SOSPENSIONE).mapping(RProcIter::new),
                    DSL.row(PROC_ITER_ESITO.ID_PROC_ITER, PROC_ITER_ESITO.ID_PROC, PROC_ITER_ESITO.DATAORA, 
                            PROC_ITER_ESITO.ATTIVO, PROC_ITER_ESITO.MODIFICABILE, V_PROC_ACL_ATTIVI.ID_UTENTE, 
                            PROC_ITER_ESITO.CODICE_PASSO, PROC_ITER_ESITO.PROT, PROC_ITER_ESITO.DATA_PROT,
                            PROC_ITER_ESITO.N_MUDE, PROC_ITER_ESITO.ANNOTAZIONI, PROC_ITER_ESITO.ID_ESITO,
                            PROC_ITER_ESITO.ID_PROC_ITER_LINK, PROC_ITER_ESITO.GIORNI_SOSPENSIONE).mapping(RProcIter::new)
                )
                .from(PROC)
                .join(PROC_ITER).on(PROC.ID_PROC_ITER_ULTIMO.eq(PROC_ITER.ID_PROC_ITER))
                .join(V_PROC_ACL_ATTIVI).on(PROC.ID_PROC.eq(V_PROC_ACL_ATTIVI.ID_PROC))
                .leftJoin(PROC_ITER_ESITO).on(PROC.ID_PROC_ITER_ESITO.eq(PROC_ITER_ESITO.ID_PROC_ITER))
                .leftJoin(PROC_CAT).on(PROC.ID_PROC.eq(PROC_CAT.ID_PROC))
                //.where(V_PROC_ACL_ATTIVI.ID_PROC.isNotNull()).and(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.eq(iPriv)).and(V_PROC_ACL_ATTIVI.ID_UTENTE.eq(utente.getUtente().getIdUtente())).and(cond)
                .where(V_PROC_ACL_ATTIVI.ID_PROC.isNotNull()).and(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.eq(iPriv)).and(V_PROC_ACL_ATTIVI.ID_UTENTE.in(lIds)).and(cond)
                .groupBy(PROC.ID_PROC)
                .fetch(Records.mapping(RProc::new));        
    }
    
    /*
    public ProcAssRecord getAssegnazioneById(int idProcAss) {
        return ctx.selectFrom(PROC_ASS).where(PROC_ASS.ID_PROC_ASS.eq(idProcAss)).fetchSingle();
    }
    */
        
    /**
     * Restituisce le procedure assegnate o da assegnare in base al tipo di output richiesto 
     * per l'utente e il tipo procedura indicato.
     * La procedura risulta assegnata o non assegnata se esiste (o non esiste) una QUALSIASI
     * assegnazione fatta anche a istruttori non appartenenti gerarchicamente all'utente 
     * specificato con accesso LM(6).
     * 
     * @param <T> tipo di risultato (determina se sono "Assegnate" o "da assegnare"
     * @param utente utente per cui verificare le assegnazioni
     * @param idTipoProc tipo procedura
     * @param output tipologia di output
     * @return elenco delle pratiche
     */
    public <T> List<T> getProcedureAssegnazione(Utente utente, int idTipoProc, Class<T> output) { 
        boolean assegnate = output.equals(ProcAssegnata.class);
        String sql = """
                     select distinct p.*, gc.comune, group_concat(concat(ut.nome_utente, ' (', lr.ruolo, ')') separator '|') as assegnatari
                     from proc p 
                     join v_proc_acl_attivi pa 
                        on p.id_proc = pa.id_proc 
                     join gis_centroidi gc 
                        on gc.codice_com = p.codice_com     
                     left join proc_acl_istruttori paa 
                        on p.id_proc = paa.id_proc      
                     left join utenti ut 
                        on paa.id_utente = ut.id_utente 
                     left join l_ruolo lr 
                        on paa.id_ruolo = lr.id_ruolo     
                     where  
                         (pa.id_utente = {0}) and 
                         -- (pa.id_utente in ({0})) and                     
                         (p.id_tipo_proc = {1}) and 
                         (pa.autorizzazioni & 1 = 1)  
                     and %s exists (
                        select pass.id_proc 
                         from v_proc_acl_attivi pass  
                         where 
                            pass.id_proc = p.id_proc and 
                            -- pass.id_utente_delegante = {0} and                             
                            (pass.autorizzazioni = 6))        
                     group by id_proc
                     """.formatted(assegnate ? "" : "not"); 
 
        //List<Integer> deleghe = utente.getDelegheGerarchiche().get(idTipoProc);
        //if(deleghe.isEmpty()) deleghe.add(utente.getDeleghe().get(idTipoProc));
        return ctx.fetch(sql, utente.getDeleghe().get(idTipoProc), idTipoProc).into(output);
        //return ctx.fetch(sql, DSL.list(deleghe.stream().map(DSL::val).collect(Collectors.toList())), idTipoProc).into(output);
    }
    
    public List<StatoDiFatto> getStatoDiFatto(Utente utente, int idTipoProc) {
        String sql = """                     
                        select distinct p.*, gc.comune, group_concat(concat(ut.nome_utente, ' (', lr.ruolo, ')') separator '|') as assegnatari, pi.codice_passo, pi.data_prot,
                               statoPratica(p.id_proc_iter_ultimo, p.id_proc_iter_ultimo_ufficio, p.id_proc_iter_esito, p.id_proc_iter_firma) as stato           
                                          from proc p 
                                          join v_proc_acl_attivi pa 
                                             on p.id_proc = pa.id_proc 
                                          join gis_centroidi gc 
                                             on gc.codice_com = p.codice_com     
                                          left join proc_acl_istruttori paa 
                                             on p.id_proc = paa.id_proc      
                                          left join utenti ut 
                                             on paa.id_utente = ut.id_utente 
                                          left join l_ruolo lr 
                                             on paa.id_ruolo = lr.id_ruolo
                                          left join proc_iter pi
                                             on p.id_proc_iter_ultimo = pi.id_proc_iter
                                          where  
                                              (pa.id_utente = {0}) and 
                                              -- (pa.id_utente in ({0})) and                     
                                              (p.id_tipo_proc = {1}) and 
                                              (pa.autorizzazioni & 1 = 1)  
                                          and exists (
                                             select pass.id_proc 
                                              from v_proc_acl_attivi pass  
                                              where 
                                                 pass.id_proc = p.id_proc and 
                                                 -- pass.id_utente_delegante = {0} and                             
                                                 (pass.autorizzazioni = 6))        
                                          group by id_proc
                     union                      
                        select distinct p.*, gc.comune, null as assegnatari, pi.codice_passo, pi.data_prot,
                               statoPratica(p.id_proc_iter_ultimo, p.id_proc_iter_ultimo_ufficio, p.id_proc_iter_esito, p.id_proc_iter_firma) as stato
                                          from proc p 
                                          join v_proc_acl_attivi pa 
                                             on p.id_proc = pa.id_proc 
                                          join gis_centroidi gc 
                                             on gc.codice_com = p.codice_com     
                                          left join proc_acl_istruttori paa 
                                             on p.id_proc = paa.id_proc      
                                          left join utenti ut 
                                             on paa.id_utente = ut.id_utente 
                                          left join l_ruolo lr 
                                             on paa.id_ruolo = lr.id_ruolo
                                          left join proc_iter pi
                                             on p.id_proc_iter_ultimo = pi.id_proc_iter
                                          where  
                                              (pa.id_utente = {0}) and 
                                              -- (pa.id_utente in ({0})) and                     
                                              (p.id_tipo_proc = {1}) and 
                                              (pa.autorizzazioni & 1 = 1)  
                                          and not exists (
                                             select pass.id_proc 
                                              from v_proc_acl_attivi pass  
                                              where 
                                                 pass.id_proc = p.id_proc and 
                                                 -- pass.id_utente_delegante = {0} and                             
                                                 (pass.autorizzazioni = 6))        
                                          group by id_proc
                     """;
        return ctx.fetch(sql, utente.getDeleghe().get(idTipoProc), idTipoProc).into(StatoDiFatto.class);
    } 
/* 
    -- inglobata in quella sopra
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
 */
    
    @LogDatabaseOperation
    public void inserisciAssegnazioni(List<ProcRecord> procedimentiSelezionati, List<UtenteRuolo> target, int idTipoProc, Utente utente) throws DatabaseException {
        try {
            int idDelega = utente.getDeleghe().get(idTipoProc);
            
            LocalDateTime now = LocalDateTime.now();
            ctx.transaction(trx -> {
                procedimentiSelezionati.forEach(proc -> {
                    // ripulisce assegnazioni attuali
                    trx.dsl().deleteFrom(PROC_ACL_ISTRUTTORI).where(PROC_ACL_ISTRUTTORI.ID_PROC.eq(proc.getIdProc())).execute();
                    
                    // Per ogni assegnazione...
                    target.forEach(ass -> {
                        // ...la inserisce
                        ProcAclRecord par = new ProcAclRecord();
                        par.setIdProc(proc.getIdProc());
                        par.setIdUtenteAssegnante(utente.getUtente().getIdUtente());
                        par.setIdUtenteDelegante(idDelega);
                        par.setDataAssegnazione(now);

                        LRuoloRecord lr = trx.dsl().selectFrom(L_RUOLO).where(L_RUOLO.ID_RUOLO.eq(ass.getIdRuolo())).fetchSingle();

                        par.setIdUtente(ass.getIdUtente());
                        par.setIdUfficio(ass.getIdUfficio()); // modifica del 09/10/25 - aggiungo l'ufficio per disattivazione rapida dello stesso
                        par.setIdRuolo(ass.getIdRuolo());
                        par.setAutorizzazioni(lr.getPermessiPredefiniti());

                        trx.dsl().insertInto(PROC_ACL).set(par).execute();
                               
                        // Aggiorna le assegnazioni più recenti
                        ProcAclIstruttoriRecord pair = new ProcAclIstruttoriRecord();
                        pair.setIdProc(proc.getIdProc());
                        pair.setIdUtente(ass.getIdUtente());
                        pair.setIdUfficio(ass.getIdUfficio()); // modifica del 09/10/25 - aggiungo l'ufficio per disattivazione rapida dello stesso
                        pair.setIdRuolo(ass.getIdRuolo());
                        
                        trx.dsl().insertInto(PROC_ACL_ISTRUTTORI).set(pair).execute();
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
    public void modificaAssegnazioni(ProcAssegnata proc, List<UtenteRuolo> daRimuovere, List<UtenteRuolo> daAggiungere, UtentiRecord utente, int delegato, LocalDateTime dataOra) throws DatabaseException {
         try {
            int A = TipoOperazioneEnum.A.getOperazione();
            int M = TipoOperazioneEnum.M.getOperazione();
            LocalDateTime now = dataOra;
            
            ctx.transaction(trx -> {
                int idToolAutomatico = getToolAutomatico(trx.dsl()).getIdUtente();
                
                daRimuovere.forEach(remove -> {
                    // Conserva l'autorizzazione che si sta rimuovendo
                    VProcAclAttiviRecord procAss = trx.dsl().selectFrom(V_PROC_ACL_ATTIVI)
                            .where(V_PROC_ACL_ATTIVI.ID_PROC.eq(proc.getIdProc()))
                            .and(V_PROC_ACL_ATTIVI.ID_UTENTE.eq(remove.getIdUtente()))
                            .and(V_PROC_ACL_ATTIVI.ID_UFFICIO.eq(remove.getIdUfficio())) // modifica del 09/10/25 - aggiungo l'ufficio per disattivazione rapida dello stesso
                            .and(V_PROC_ACL_ATTIVI.ID_RUOLO.eq(remove.getIdRuolo()))
                            .fetchSingle();
                    
                    // Rimuove l'autorizzazione
                    trx.dsl().update(PROC_ACL)
                                .set(PROC_ACL.DATA_RIMOZIONE, now) 
                                .set(PROC_ACL.ID_UTENTE_RIMOZIONE, utente.getIdUtente())
                            .where(PROC_ACL.ID_PROC.eq(proc.getIdProc()))
                            .and(PROC_ACL.ID_UTENTE.eq(remove.getIdUtente()))  // non è meglio utilizzare procAss.getIdProcAcl()?
                            .and(PROC_ACL.ID_UFFICIO.eq(remove.getIdUfficio())) // modifica del 09/10/25 - aggiungo l'ufficio per disattivazione rapida dello stesso
                            .and(PROC_ACL.ID_RUOLO.eq(remove.getIdRuolo()))
                            .execute();
                    
                    // la precedente autorizzazione era una "A" (assegnante)? Si, allora rimuovi a cascata tutte le autorizzazioni fatte a quell'utente
                    if((procAss.getAutorizzazioni() & A) == A) {
                        // elenca tutte le autorizzazioni figlie di quella rimossa
                        /*String sql = "WITH RECURSIVE q AS (" +
                                    "        SELECT  * " +
                                    "        FROM    proc_acl " +
                                    "        WHERE   id_proc_acl = {0}" +
                                    "	UNION ALL " +
                                    "        SELECT  m.* " +
                                    "        FROM    proc_acl AS m " +
                                    "        JOIN    q " +
                                    "        ON      m.id_utente_delegante = q.id_utente " +
                                    //"   UNION ALL " +
                                    //"       SELECT  n.* " +
                                    //"        FROM    proc_ass AS n " +
                                    //"        JOIN    q " +
                                    //"        ON      n.id_utente_assegnante = q.id_utente " +
                                    ") SELECT DISTINCT q.id_proc_acl FROM q";
                        List<Integer> procAssUpd = trx.dsl().fetch(sql, procAss.getIdProcAcl()).into(Integer.class);
                        
                        // effettua aggiornamento
                        trx.dsl().update(PROC_ACL)
                                .set(PROC_ACL.DATA_RIMOZIONE, now)
                                .set(PROC_ACL.ID_UTENTE_RIMOZIONE, utente.getIdUtente())
                                .where(PROC_ACL.ID_PROC_ACL.in(procAssUpd))
                                .and(PROC_ACL.DATA_RIMOZIONE.isNull())
                                .execute();                        */
                        
                        // aggiorna le assegnazioni successive non rimosse
                        trx.dsl().update(PROC_ACL)
                                .set(PROC_ACL.DATA_RIMOZIONE, now)
                                .set(PROC_ACL.ID_UTENTE_RIMOZIONE, utente.getIdUtente())
                                .where(PROC_ACL.ID_PROC.eq(proc.getIdProc()))
                                .and(PROC_ACL.DATA_RIMOZIONE.isNull())
                                .and(PROC_ACL.DATA_ASSEGNAZIONE.ge(procAss.getDataAssegnazione()))
                                .execute();                        
                                
                    }
                });
                
                // inserisci le nuove
                daAggiungere.forEach(ass -> {
                    ProcAclRecord par = new ProcAclRecord();
                    par.setIdProc(proc.getIdProc());
                    par.setIdUtenteAssegnante(utente.getIdUtente());
                    par.setIdUtenteDelegante(delegato);
                    par.setDataAssegnazione(now);

                    LRuoloRecord lr = trx.dsl().selectFrom(L_RUOLO).where(L_RUOLO.ID_RUOLO.eq(ass.getIdRuolo())).fetchSingle();

                    par.setIdUtente(ass.getIdUtente());
                    par.setIdUfficio(ass.getIdUfficio()); // modifica del 09/10/25 - aggiungo l'ufficio per disattivazione rapida dello stesso
                    par.setIdRuolo(ass.getIdRuolo());
                    par.setAutorizzazioni(lr.getPermessiPredefiniti());

                    trx.dsl().insertInto(PROC_ACL).set(par).execute();                   
                });
                
                // ripulisce assegnazioni attuali
                trx.dsl().deleteFrom(PROC_ACL_ISTRUTTORI).where(PROC_ACL_ISTRUTTORI.ID_PROC.eq(proc.getIdProc())).execute();
                
                // incrementa di 1 secondo per escludere quelli appena inseriti
                LocalDateTime nowcheck = now.plusSeconds(1);
                // inserisce le nuove assegnazioni più recenti - parte dalle "M"
                List<ProcAclRecord> lNew = trx.dsl().selectFrom(PROC_ACL)
                                                    .where(PROC_ACL.ID_PROC.eq(proc.getIdProc()))
                                                    .and(PROC_ACL.AUTORIZZAZIONI.bitAnd(M).eq(M))
                                                    .and(PROC_ACL.DATA_ASSEGNAZIONE.le(nowcheck).and(PROC_ACL.DATA_RIMOZIONE.isNull()))
                                                    .or(DSL.val(nowcheck).between(PROC_ACL.DATA_ASSEGNAZIONE, PROC_ACL.DATA_RIMOZIONE))
                                                    .and(PROC_ACL.ID_UTENTE_DELEGANTE.ne(idToolAutomatico))
                                                    .orderBy(PROC_ACL.DATA_ASSEGNAZIONE.desc())
                                                    .fetch();
                lNew.forEach(ass -> {
                    ProcAclIstruttoriRecord pair = new ProcAclIstruttoriRecord();
                    pair.setIdProc(proc.getIdProc());
                    pair.setIdUtente(ass.getIdUtente());
                    pair.setIdUfficio(ass.getIdUfficio()); // modifica del 09/10/25 - aggiungo l'ufficio per disattivazione rapida dello stesso
                    pair.setIdRuolo(ass.getIdRuolo());
                    
                    trx.dsl().insertInto(PROC_ACL_ISTRUTTORI).set(pair).execute();
                });
                
                // nessuna M inserita?
                if(lNew.isEmpty()) {
                    // Procede con le "A"
                    lNew = trx.dsl().selectFrom(PROC_ACL)
                                    .where(PROC_ACL.ID_PROC.eq(proc.getIdProc()))
                                    .and(PROC_ACL.AUTORIZZAZIONI.bitAnd(A).eq(A))
                                    //.and(PROC_ASS.ID_UTENTE.ne(delegato.getIdUtente())) // evita l'estrazione di se stesso
                                    .and(PROC_ACL.DATA_ASSEGNAZIONE.le(nowcheck).and(PROC_ACL.DATA_RIMOZIONE.isNull()))
                                    .or(DSL.val(nowcheck).between(PROC_ACL.DATA_ASSEGNAZIONE, PROC_ACL.DATA_RIMOZIONE))
                                    .orderBy(PROC_ACL.DATA_ASSEGNAZIONE.desc())
                                    .fetch(); 
                    // Nulla? 
                    if(!lNew.isEmpty()) {
                        // Inserisci solo la più recente delle "A"
                        ProcAclRecord ass = lNew.get(0);
                         
                        ProcAclIstruttoriRecord pair = new ProcAclIstruttoriRecord();
                        pair.setIdProc(proc.getIdProc());
                        pair.setIdUtente(ass.getIdUtente());
                        pair.setIdUfficio(ass.getIdUfficio()); // modifica del 09/10/25 - aggiungo l'ufficio per disattivazione rapida dello stesso
                        pair.setIdRuolo(ass.getIdRuolo());
                    
                        trx.dsl().insertInto(PROC_ACL_ISTRUTTORI).set(pair).execute();
                    }
                    /*else {
                        // condizione di guardia...non *DOVREBBE* mai verificarsi...effettuare dei log
                        throw new DataAccessException("Non risulta nessuna assegnazione recente per la procedura indicata.");
                    }*/                    
                }
                
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }         
    }
    
    /**
     * Restituisce le assegnazioni in corso di validità per la procedura indicata.
     * Opzionalmente se viene fornita anche la delega il risultato sarà limitato
     * ad essa.
     * 
     * @param idProc id procedura
     * @param idDelega id delega - opzionale (null)
     * @return  
     */
    public List<UtenteRuolo> getAssegnazioniAttualiProcedura(int idProc, Integer idDelega) {
        int M = TipoOperazioneEnum.M.getOperazione();
        Condition condDelega = (idDelega!=null) ? DSL.and(V_PROC_ACL_ATTIVI.ID_UTENTE_DELEGANTE.eq(idDelega)) : DSL.noCondition();
                
        return ctx.select(RUOLI_UTENTE.ID_RUOLI_UTENTE, V_PROC_ACL_ATTIVI.ID_UTENTE, V_PROC_ACL_ATTIVI.ID_RUOLO, V_PROC_ACL_ATTIVI.ID_UFFICIO, UTENTI.NOME_UTENTE, L_RUOLO.RUOLO, UFFICI.UFFICIO)
                .from(V_PROC_ACL_ATTIVI)
                .join(RUOLI_UTENTE) 
                    .on(RUOLI_UTENTE.ID_UTENTE.eq(V_PROC_ACL_ATTIVI.ID_UTENTE).and(RUOLI_UTENTE.ID_RUOLO.eq(V_PROC_ACL_ATTIVI.ID_RUOLO)))
                .join(UTENTI)
                    .on(V_PROC_ACL_ATTIVI.ID_UTENTE.eq(UTENTI.ID_UTENTE))
                .join(L_RUOLO)
                    .on(V_PROC_ACL_ATTIVI.ID_RUOLO.eq(L_RUOLO.ID_RUOLO))
                .join(UFFICI)
                    .on(V_PROC_ACL_ATTIVI.ID_UFFICIO.eq(UFFICI.ID_UFFICIO))
                //.where(V_PROC_ACL_ATTIVI.ID_PROC.eq(idProc).and(V_PROC_ACL_ATTIVI.AUTORIZZAZIONI.bitAnd(M).eq(M)).and(condDelega))
                .where(V_PROC_ACL_ATTIVI.ID_PROC.eq(idProc).and(condDelega)) // 13/10/2025 NON MI INTERESSA SE M, A O QUELLO CHE E'
              .fetchInto(UtenteRuolo.class); 
    }
     
    /**
     * Restituisce l'utente del tool automatico. L'argomento è il context attuale
     * di JOOQ permettendo la partecipazione ad una transazione attiva.
     * 
     * @param _ctx contesto di JOOQ attuale
     * @return 
     */
    public UtentiRecord getToolAutomatico(DSLContext _ctx) {
        return _ctx.selectFrom(UTENTI).where(UTENTI.UTENTE.eq(TOOL_AUTOMATICO)).fetchSingle();
    } 
    
    /**
     * Restituisce l'utente del tool automatico
     * 
     * @return 
     */
    public UtentiRecord getToolAutomatico() {
        return getToolAutomatico(ctx);
    }
    
    /**
     * Restitusce l'elenco dei tipi procedura per cui si è autorizzati in assegnazione sia direttamente
     * che per delega alla data attuale
     * 
     * @param utente
     * @param ops
     * @return 
     */ 
    public List<LTipoProcRecord> getTipiProcedureAutorizzate(Utente utente, TipoOperazioneEnum[] ops) {
        // Recupera le acl dell'utente per le operazioni indicate
        ACL acl = ss.getACL(utente, ops);
        Condition cond;
        if(!acl.getTipiProcedureAmmesse().isEmpty()) {
            cond = DSL.and(L_TIPO_PROC.ID_TIPO_PROC.in(acl.getTipiProcedureAmmesse()));
             
            // se sono state aggiunte i tipi procedure ammessi, includere quelli esclusi
            if(!acl.getTipiProcedureEscluse().isEmpty()) {
                cond = cond.and(L_TIPO_PROC.ID_TIPO_PROC.notIn(acl.getTipiProcedureEscluse()));
            }
        }
        else { 
            // nessuna procedura ammessa, imposta una condizione sempre false per far restituire un set vuoto
            cond = DSL.and(DSL.falseCondition());
        }
        
        return ctx.selectFrom(L_TIPO_PROC).where(cond).fetch();
    }      
    
    /**
     * Restituisce il procedimento con l'id indicato
     * 
     * @param idProc
     * @return 
     */
    public ProcRecord getProcedimentoById(int idProc) {
        return ctx.selectFrom(PROC).where(PROC.ID_PROC.eq(idProc)).fetchOne();
    }

    /**
     * Restituisce l'elenco dei ruoli tecnici come mappa id->ruolo
     * 
     * @return 
     */
    public Map<Integer, LRuoloTecnicoRecord> getRuoliTecniciMap() {
        return ctx.selectFrom(L_RUOLO_TECNICO).fetchMap(L_RUOLO_TECNICO.ID_RUOLO_TECNICO);
    }
/*    
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
    
    public List<DelegaRecord> getDeleghe(int idTipoProc, int idDelegante) {
        return ctx.selectFrom(DELEGA).where(DELEGA.ID_TIPO_PROC.eq(idTipoProc)).and(DELEGA.ID_DELEGANTE.eq(idDelegante)).fetch();
    }
 */   

    public List<ProcTecniciRecord> getProcTecnici() {
        return ctx.selectFrom(PROC_TECNICI).orderBy(PROC_TECNICI.COGNOME_RAG_SOC.asc(), PROC_TECNICI.NOME.asc()).fetch();
    }

    public ProcTecniciRecord getProcTecnicoById(int idTecnico) {
        return ctx.selectFrom(PROC_TECNICI).where(PROC_TECNICI.ID_TECNICO.eq(idTecnico)).fetchSingle();
    }

    public LRuoloTecnicoRecord getRuoloTecnicoById(int idRuoloTecnico) {
        return ctx.selectFrom(L_RUOLO_TECNICO).where(L_RUOLO_TECNICO.ID_RUOLO_TECNICO.eq(idRuoloTecnico)).fetchSingle();
    }
    
    @LogDatabaseOperation
    public int inserisciTecnico(ProcTecniciRecord tecnico) throws DatabaseException {
        try {
            AtomicInteger ai = new AtomicInteger(0);
            ctx.transaction(trx -> {
                trx.dsl().insertInto(PROC_TECNICI).set(tecnico).execute();
                ai.set(trx.dsl().lastID().intValue());
            });
            
            return ai.get();
        }
        catch(DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }
    }
}
 