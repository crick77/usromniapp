/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;

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
import java.util.List;
import org.jooq.impl.DSL;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UtenteService {   
    @Inject
    @DSLCtx 
    DSLContext ctx;
        
    public List<UtentiRecord> getUtenti() {
        return ctx.selectFrom(UTENTI).fetch();
    } 
    
    public List<UtentiRecord> getUtenti(List<Integer> idUtenti) {
        return ctx.selectFrom(UTENTI).where(UTENTI.ID_UTENTE.in(idUtenti)).fetch();
    }
    
    public UtentiRecord getUtente(String username) {
        return ctx.selectFrom(UTENTI).where(UTENTI.UTENTE.eq(username).and(UTENTI.UTENTE_FISICO.eq(1))).fetchOne();
    }
     
    public UtentiRecord getUtente(int idUtente) {
        return ctx.selectFrom(UTENTI).where(UTENTI.ID_UTENTE.eq(idUtente).and(UTENTI.UTENTE_FISICO.eq(1))).fetchOne();
    }
    
    public UfficiRecord getUfficioUtente(UtentiRecord u) {
        return ctx.select().from(UFFICI).join(ISTRUTTORI_UFFICI).on(UFFICI.ID_UFFICIO.eq(ISTRUTTORI_UFFICI.ID_UFFICIO)).where(ISTRUTTORI_UFFICI.ID_UTENTE.eq(u.getIdUtente())).fetchOneInto(UFFICI);        
    }
    
    public List<UtentiRecord> getUtentiUfficio(int idUfficio) {
        return ctx.select().from(UTENTI).join(ISTRUTTORI_UFFICI).on(UTENTI.ID_UTENTE.eq(ISTRUTTORI_UFFICI.ID_UTENTE)).where(ISTRUTTORI_UFFICI.ID_UFFICIO.eq(idUfficio)).fetchInto(UtentiRecord.class);
    }
    
    public List<UtenteRuolo> getUtentiRuoloUfficio(int idUfficio, int idTipoProc) {
        /*return ctx.select(V_RUOLIUTENTE_ATTIVI.ID_RUOLI_UTENTE, V_RUOLIUTENTE_ATTIVI.ID_UTENTE, V_RUOLIUTENTE_ATTIVI.ID_RUOLO, UTENTI.NOME_UTENTE, L_RUOLO.RUOLO).from(V_RUOLIUTENTE_ATTIVI)
                .join(UTENTI).on(V_RUOLIUTENTE_ATTIVI.ID_UTENTE.eq(UTENTI.ID_UTENTE))
                .join(L_RUOLO).on(V_RUOLIUTENTE_ATTIVI.ID_RUOLO.eq(L_RUOLO.ID_RUOLO))
                .join(ISTRUTTORI_UFFICI).on(V_RUOLIUTENTE_ATTIVI.ID_UTENTE.eq(ISTRUTTORI_UFFICI.ID_UTENTE))
                .where(ISTRUTTORI_UFFICI.ID_UFFICIO.eq(idUfficio)).fetchInto(UtenteRuolo.class);*/
        String sql = """
                     select 
                        v_ruoliutente_attivi.id_ruoli_utente, 
                        v_ruoliutente_attivi.id_utente, 
                        v_ruoliutente_attivi.id_ruolo, 
                        v_utenti_attivi.nome_utente, 
                        l_ruolo.ruolo,
                        0 as tipo
                     FROM v_ruoliutente_attivi 
                        join v_utenti_attivi 
                            on v_ruoliutente_attivi.id_utente = v_utenti_attivi.id_utente 
                        join l_ruolo 
                            on v_ruoliutente_attivi.id_ruolo = l_ruolo.id_ruolo 
                        join istruttori_uffici 
                            on v_ruoliutente_attivi.id_utente = istruttori_uffici.id_utente 
                        join uffici
                             on uffici.id_utente = v_ruoliutente_attivi.id_utente
                        join tipo_proc_uffici
                             on tipo_proc_uffici.id_ufficio = uffici.id_ufficio
                     where tipo_proc_uffici.id_ufficio in (select id_ufficio from uffici where id_ufficio_sovraordinato = {0}) AND  l_ruolo.codice = 'RP' and id_tipo_proc = {1} 
                     union
                     select 
                        v_ruoliutente_attivi.id_ruoli_utente, 
                        v_ruoliutente_attivi.id_utente, 
                        v_ruoliutente_attivi.id_ruolo, 
                        v_utenti_attivi.nome_utente, 
                        l_ruolo.ruolo,
                        1 as tipo
                     from v_ruoliutente_attivi 
                        join v_utenti_attivi 
                            on v_ruoliutente_attivi.id_utente = v_utenti_attivi.id_utente 
                        join l_ruolo 
                            on v_ruoliutente_attivi.id_ruolo = l_ruolo.id_ruolo 
                        join istruttori_uffici 
                        on v_ruoliutente_attivi.id_utente = istruttori_uffici.id_utente 
                     where istruttori_uffici.id_ufficio = {0} AND  l_ruolo.codice = 'ISTR'
                     order by tipo, nome_utente 
                     """;
        return ctx.fetch(sql, idUfficio, idTipoProc).into(UtenteRuolo.class);
    }
    
    public UtenteRuolo getUtenteRuoloUfficio(int idRuoloUtente) {
        return ctx.select(V_RUOLIUTENTE_ATTIVI.ID_RUOLI_UTENTE, V_RUOLIUTENTE_ATTIVI.ID_UTENTE, V_RUOLIUTENTE_ATTIVI.ID_RUOLO, UTENTI.NOME_UTENTE, L_RUOLO.RUOLO).from(V_RUOLIUTENTE_ATTIVI)
                .join(UTENTI).on(V_RUOLIUTENTE_ATTIVI.ID_UTENTE.eq(UTENTI.ID_UTENTE))
                .join(L_RUOLO).on(V_RUOLIUTENTE_ATTIVI.ID_RUOLO.eq(L_RUOLO.ID_RUOLO))
                .join(ISTRUTTORI_UFFICI).on(V_RUOLIUTENTE_ATTIVI.ID_UTENTE.eq(ISTRUTTORI_UFFICI.ID_UTENTE))
                .where(V_RUOLIUTENTE_ATTIVI.ID_RUOLI_UTENTE.eq(idRuoloUtente)).fetchOneInto(UtenteRuolo.class);
    }
    
    public UtentiRecord login(String userName, String password) {
        return ctx.selectFrom(UTENTI).where(UTENTI.UTENTE.eq(userName)).and(UTENTI.PASSWORD.eq(DSL.md5(password))).and(UTENTI.ATTIVO.eq(1)).and(UTENTI.UTENTE_FISICO.eq(1)).fetchOne();
    }
    
    public UtentiRecord getUtenteDelegato(UtentiRecord utente, int idTipoProc) {
        String sql = "SELECT u.* from utenti u join delega d on u.id_utente = d.id_delegante where d.id_utente = {0} and d.id_tipo_proc = {1} and " +
                     "((data_inizio <= now() and data_fine is null) or (now() between data_inizio and data_fine))";
        
        return ctx.fetchSingle(sql, utente.getIdUtente(), idTipoProc).into(UtentiRecord.class);
    }
}  