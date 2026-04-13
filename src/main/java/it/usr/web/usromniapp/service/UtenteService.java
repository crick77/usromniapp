/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;

import it.usr.web.service.BaseService;
import static it.usr.web.usromniapp.domain.Tables.*;
import it.usr.web.usromniapp.domain.tables.records.UfficiRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.model.UtenteRuolo;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.jooq.DSLContext;
import it.usr.web.usromniapp.producer.DSLCtx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jooq.impl.DSL;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UtenteService extends BaseService {   
    @Inject
    @DSLCtx 
    DSLContext ctx;
        
    /**
     * Restituisce tutti gli utenti indipendentemente dallo stato (Attivo/Disattivo)
     * 
     * @return 
     */
    public List<UtentiRecord> getUtenti() {
        return ctx.selectFrom(UTENTI).fetch();
    } 
    
    /**
     * Restituisce l'elenco degli Utente corrispondendi agli id indicati
     * 
     * @param idUtenti gli id
     * @return 
     */
    public List<UtentiRecord> getUtentiById(List<Integer> idUtenti) {
        return ctx.selectFrom(UTENTI).where(UTENTI.ID_UTENTE.in(idUtenti)).fetch();
    }
    
    /**
     * Restituisce l'Utente dal suo username
     * NB. L'utente DEVE essere fisico
     * 
     * @param username lo username
     * @return 
     */
    public UtentiRecord getUtenteByUsername(String username) {
        return ctx.selectFrom(UTENTI).where(UTENTI.UTENTE.eq(username).and(UTENTI.UTENTE_FISICO.eq(1))).fetchOne();
    }
     
    /**
     * Restituisce l'utente dal suo id
     * NB. L'utente deve essere fisico
     * 
     * @param idUtente l'id
     * @return 
     */
    public UtentiRecord getUtenteById(int idUtente) {
        return ctx.selectFrom(UTENTI).where(UTENTI.ID_UTENTE.eq(idUtente).and(UTENTI.UTENTE_FISICO.eq(1))).fetchOne();
    }
     
    /**
     * Restituisce gli uffici di cui l'utente passato per argomento è membro ATTIVO.
     * 
     * @param u l'utente
     * @return 
     */
    public List<UfficiRecord> getUfficiUtente(UtentiRecord u) {
        //return ctx.select(UFFICI.fields()).from(UFFICI).join(ISTRUTTORI_UFFICI).on(UFFICI.ID_UFFICIO.eq(ISTRUTTORI_UFFICI.ID_UFFICIO)).where(ISTRUTTORI_UFFICI.ID_UTENTE.eq(u.getIdUtente())).fetchInto(UFFICI);
        return ctx.selectDistinct(UFFICI.fields()).from(UFFICI).join(V_RUOLIUTENTE_ATTIVI).on(UFFICI.ID_UFFICIO.eq(V_RUOLIUTENTE_ATTIVI.ID_UFFICIO)).where(V_RUOLIUTENTE_ATTIVI.ID_UTENTE.eq(u.getIdUtente())).fetchInto(UFFICI);
    }
        
    /**
     * Restituisce tutti i ruoli utente che gli uffici indicati hanno per per la tipologia 
     * di procedura indicata.
     * 
     * @param uffici elenco degli uffici
     * @param idTipoProc la tipologia di procedura
     * @return 
     */
    public List<UtenteRuolo> getUtentiRuoloUfficio(List<UfficiRecord> uffici, int idTipoProc) {        
        String sql = """
                     SELECT distinct
                            v_ruoliutente_attivi.id_ruoli_utente,
                            v_ruoliutente_attivi.id_utente,
                            v_ruoliutente_attivi.id_ruolo,
                            v_ruoliutente_attivi.id_ufficio,
                            v_utenti_attivi.nome_utente,
                            l_ruolo.ruolo,
                            uffici.ufficio,
                            0 AS tipo
                     FROM v_ruoliutente_attivi
                            JOIN v_utenti_attivi
                                ON v_ruoliutente_attivi.id_utente = v_utenti_attivi.id_utente
                            JOIN l_ruolo
                                ON v_ruoliutente_attivi.id_ruolo = l_ruolo.id_ruolo
                            JOIN uffici
                                ON v_ruoliutente_attivi.id_utente = uffici.id_utente
                            JOIN tipo_proc_uffici
                               ON tipo_proc_uffici.id_ufficio = v_ruoliutente_attivi.id_ufficio
                     WHERE 
                            tipo_proc_uffici.id_ufficio IN (SELECT id_ufficio FROM uffici WHERE id_ufficio_sovraordinato IN ({0})) 
                        AND l_ruolo.codice = 'RP' 
                        AND id_tipo_proc = {1}
                     UNION
                     SELECT 
                            v_ruoliutente_attivi.id_ruoli_utente, 
                            v_ruoliutente_attivi.id_utente, 
                            v_ruoliutente_attivi.id_ruolo, 
                            v_ruoliutente_attivi.id_ufficio,
                            v_utenti_attivi.nome_utente, 
                            l_ruolo.ruolo,
                            uffici.ufficio,
                            1 AS tipo
                     FROM v_ruoliutente_attivi 
                            JOIN v_utenti_attivi  
                                ON v_ruoliutente_attivi.id_utente = v_utenti_attivi.id_utente 
                            JOIN l_ruolo 
                                ON v_ruoliutente_attivi.id_ruolo = l_ruolo.id_ruolo
                            JOIN uffici
                                ON v_ruoliutente_attivi.id_ufficio = uffici.id_ufficio
                     WHERE 
                            l_ruolo.codice = 'ISTR' 
                        AND v_ruoliutente_attivi.id_ufficio IN ({0})
                     ORDER BY
                            tipo, nome_utente 
                     """;

        List<Integer> idUff = uffici.stream().map(u -> u.getIdUfficio()).collect(Collectors.toList());
        return ctx.fetch(sql, DSL.list(idUff.stream().map(DSL::val).collect(Collectors.toList())), idTipoProc).into(UtenteRuolo.class);
    } 
     
    /**
     * Restituisce tutte le informazioni del ruolo utente per l'ufficio indicato
     * 
     * @param idRuoloUtente
     * @param idUfficio
     * @return 
     */
    public UtenteRuolo getUtenteRuoloUfficio(int idRuoloUtente, int idUfficio) {
        return ctx.selectDistinct(V_RUOLIUTENTE_ATTIVI.ID_RUOLI_UTENTE, V_RUOLIUTENTE_ATTIVI.ID_UTENTE, V_RUOLIUTENTE_ATTIVI.ID_RUOLO, V_RUOLIUTENTE_ATTIVI.ID_UFFICIO, UTENTI.NOME_UTENTE, L_RUOLO.RUOLO, UFFICI.UFFICIO)
                .from(V_RUOLIUTENTE_ATTIVI)
                    .join(UTENTI).on(V_RUOLIUTENTE_ATTIVI.ID_UTENTE.eq(UTENTI.ID_UTENTE))
                    .join(L_RUOLO).on(V_RUOLIUTENTE_ATTIVI.ID_RUOLO.eq(L_RUOLO.ID_RUOLO))
                    //.join(ISTRUTTORI_UFFICI).on(V_RUOLIUTENTE_ATTIVI.ID_UTENTE.eq(ISTRUTTORI_UFFICI.ID_UTENTE))
                    .join(UFFICI).on(V_RUOLIUTENTE_ATTIVI.ID_UFFICIO.eq(UFFICI.ID_UFFICIO))
                .where(V_RUOLIUTENTE_ATTIVI.ID_RUOLI_UTENTE.eq(idRuoloUtente))
                    .and(V_RUOLIUTENTE_ATTIVI.ID_UFFICIO.eq(idUfficio))
                .fetchOneInto(UtenteRuolo.class);
    }
    
    /**
     * Effettua il login con la username e la password indicata.
     * NB. L'utente deve essere ATTIVO e FISICO.
     * 
     * @param userName
     * @param password
     * @return 
     */
    public UtentiRecord login(String userName, String password) {
        return ctx.selectFrom(UTENTI).where(UTENTI.UTENTE.eq(userName)).and(UTENTI.PASSWORD.eq(DSL.md5(password))).and(UTENTI.ATTIVO.eq(1)).and(UTENTI.UTENTE_FISICO.eq(1)).fetchOne();
    }
    
    /**    
     * Restituisce l'elenco completo delle delege di tipo procedura per l'utente indicato.
     * Se la delega non è stata indicata nel database verrà utilizzato l'utente
     * argomento (in sostanza si è delegati di sé stessi).
     * 
     * @param utente l'utente
     * @return 
     */
    public Map<Integer, Integer> getDelegheComplete(UtentiRecord utente) {
        int idUte = utente.getIdUtente();
        return ctx.select(L_TIPO_PROC.ID_TIPO_PROC, DSL.nvl(V_DELEGHE_ATTIVE.ID_DELEGANTE, DSL.val(idUte)).as(V_DELEGHE_ATTIVE.ID_DELEGANTE)).from(L_TIPO_PROC).leftJoin(V_DELEGHE_ATTIVE).on(L_TIPO_PROC.ID_TIPO_PROC.eq(V_DELEGHE_ATTIVE.ID_TIPO_PROC)).
                where(V_DELEGHE_ATTIVE.ID_UTENTE.eq(idUte)).or(V_DELEGHE_ATTIVE.ID_UTENTE.isNull()).fetch().intoMap(L_TIPO_PROC.ID_TIPO_PROC, V_DELEGHE_ATTIVE.ID_DELEGANTE);
    }
    
    /**
     * Restitusice tutti gli idutente RP che sono gerarchicamente sopposti all'idUtente
     * e per il tipo procedura fornito per argomento.
     * 
     * @param idTipoProc tipo procedura
     * @param utente utente testa della gerarchia
     * @return 
     */
    public List<Integer> getDelegheGerarchiche(int idTipoProc, UtentiRecord utente) {
        String sql = """
                     WITH RECURSIVE q AS (
                     		SELECT  uffici.* 
                     		FROM    uffici
                                JOIN 	v_deleghe_attive
                                    ON	uffici.id_utente = v_deleghe_attive.id_delegante
                     		WHERE   v_deleghe_attive.id_utente = {0} AND id_tipo_proc = {1}
                     	UNION ALL 
                     		SELECT  u.* 
                     		FROM    uffici AS u 
                     		JOIN    q 
                                    ON  u.id_ufficio_sovraordinato = q.id_ufficio
                     		JOIN 	v_deleghe_attive
                             ON 		q.id_utente = v_deleghe_attive.id_delegante
                             WHERE   u.attivo = 1 AND id_tipo_proc = {1}
                     ) SELECT DISTINCT q.id_utente FROM q
                     """;
        return ctx.fetch(sql, utente.getIdUtente(), idTipoProc).into(Integer.class);
    }
    
    /**     
     * Restituisce la gerarchia di utenti delegati dall'utente passato per argomento
     * per ogni tipo procedura.
     * 
     * @param idUtente l'utente
     * @return 
     */
    public Map<Integer, List<Integer>> getGerarchiaRP(Integer idUtente) {        
        List<Integer> lTP = ctx.select(L_TIPO_PROC.ID_TIPO_PROC).from(L_TIPO_PROC).fetchInto(Integer.class);

        String sql = """
                   WITH RECURSIVE q AS (
                            SELECT  uffici.* 
                            FROM    uffici
                           JOIN 	v_deleghe_attive
                           ON 		uffici.id_utente = v_deleghe_attive.id_delegante
                            WHERE   v_deleghe_attive.id_utente = {0} AND id_tipo_proc = {1}
                    UNION ALL 
                            SELECT  u.* 
                            FROM    uffici AS u 
                            JOIN    q 
                            ON      u.id_ufficio_sovraordinato = q.id_ufficio
                            JOIN 	v_deleghe_attive
                           ON 		q.id_utente = v_deleghe_attive.id_delegante
                           WHERE   u.attivo = 1 AND id_tipo_proc = {1}
                   ) SELECT DISTINCT q.id_utente FROM q
                   """;
        Map<Integer, List<Integer>> mGer = new HashMap<>();
        lTP.forEach(idProc -> {
            List<Integer> lGer = ctx.fetch(sql, idUtente, idProc).into(Integer.class);

            mGer.put(idProc, lGer);
        });

        return mGer;            
    }
    
    /*
     *
     * POTENZIALMENTE RIMOVIBILI
     *
    */
    
    /*public List<UtentiRecord> getUtentiUfficio(int idUfficio) {
        return ctx.select().from(UTENTI).join(ISTRUTTORI_UFFICI).on(UTENTI.ID_UTENTE.eq(ISTRUTTORI_UFFICI.ID_UTENTE)).where(ISTRUTTORI_UFFICI.ID_UFFICIO.eq(idUfficio)).fetchInto(UtentiRecord.class);
    }*/ 

    /*public UtentiRecord getUtenteDelegato(UtentiRecord utente, int idTipoProc) {
        String sql = "SELECT u.* from utenti u join delega d on u.id_utente = d.id_delegante where d.id_utente = {0} and d.id_tipo_proc = {1} and " +
                     "((data_inizio <= now() and data_fine is null) or (now() between data_inizio and data_fine))";
         
        return ctx.fetchSingle(sql, utente.getIdUtente(), idTipoProc).into(UtentiRecord.class);
    }*/    

    
}   