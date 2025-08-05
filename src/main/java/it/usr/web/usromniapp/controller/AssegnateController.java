/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.GisCentroidiRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.interceptor.RequiredAuthorization;
import it.usr.web.usromniapp.interceptor.SecurityCheck;
import it.usr.web.usromniapp.model.ProcAssegnata;
import it.usr.web.usromniapp.model.UtenteRuolo;
import it.usr.web.usromniapp.service.CodiceService;
import it.usr.web.usromniapp.service.DatabaseException;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class AssegnateController extends OmniappBaseController {
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ProcedimentoService ps;
    @Inject
    UtenteService us;
    @Inject
    CodiceService cs;
    List<LTipoProcRecord> tipiProcedimento;
    LTipoProcRecord tipoProcedimentoSelezionato;
    List<ProcAssegnata> procedimenti;
    List<ProcAssegnata> procedimentiFiltrati;
    ProcAssegnata procedimentoSelezionato;
    DualListModel<UtenteRuolo> utenti;
    DualListModel<UtenteRuolo> utentiOriginali;
    Map<Integer, LTipoProcRecord> mTipiProc;
    Map<Integer, GisCentroidiRecord> mCentroidi;

    @SecurityCheck
    @RequiredAuthorization(TipoOperazioneEnum.A)
    public String init() {
        mTipiProc = ps.getTipiProcedimentoMap();
        utenti = new DualListModel<>();
        aggiorna();

        return SAME_VIEW;
    }

    public List<ProcAssegnata> getProcedimenti() {
        return procedimenti;
    }

    public void setProcedimenti(List<ProcAssegnata> procedimenti) {
        this.procedimenti = procedimenti;
    }

    public List<ProcAssegnata> getProcedimentiFiltrati() {
        return procedimentiFiltrati;
    }

    public void setProcedimentiFiltrati(List<ProcAssegnata> procedimentiFiltrati) {
        this.procedimentiFiltrati = procedimentiFiltrati;
    }
        
    public ProcAssegnata getProcedimentoSelezionato() {
        return procedimentoSelezionato;
    }

    public void setProcedimentoSelezionato(ProcAssegnata procedimentoSelezionato) {
        this.procedimentoSelezionato = procedimentoSelezionato;
    } 
     
    public DualListModel<UtenteRuolo> getUtenti() { 
        return utenti; 
    }

    public void setUtenti(DualListModel<UtenteRuolo> utenti) {
        this.utenti = utenti;
    }

    public GisCentroidiRecord decodificaCentroide(int codiceCom) {
        return mCentroidi.get(codiceCom);
    }
    
    public GisCentroidiRecord decodificaCentroide(String codiceCom) {
        try {
            return mCentroidi.get(Integer.valueOf(codiceCom));
        }
        catch(NumberFormatException nfe) {
            logger.warn("Ricerca di codice comune con valore non inter [{}].", codiceCom);
            return null; 
        }
    }
    
    public LTipoProcRecord decodificaProcedimento(int idTipoProc) {
        return mTipiProc.get(idTipoProc);
    }

    public List<LTipoProcRecord> getTipiProcedimento() {
        return tipiProcedimento;
    }

    public LTipoProcRecord getTipoProcedimentoSelezionato() {
        return tipoProcedimentoSelezionato;
    }

    public void setTipoProcedimentoSelezionato(LTipoProcRecord tipoProcedimentoSelezionato) {
        this.tipoProcedimentoSelezionato = tipoProcedimentoSelezionato;
    }
        
    public void aggiorna() {
        tipiProcedimento = ps.getTipiProcedureAutorizzate(getUtenteOmniapp());
        mCentroidi = cs.getCentroidiMap();
        tipoProcedimentoSelezionato = null;
        procedimenti = null;
        procedimentoSelezionato = null;   
        procedimentiFiltrati = null;
    }

    public void aggiornaProcedimenti() {
        UtentiRecord delegato = us.getUtenteDelegato(getUtenteOmniapp().getUtente(), tipoProcedimentoSelezionato.getIdTipoProc());        
        procedimenti = ps.getProcedureAssegnate(delegato.getIdUtente(), tipoProcedimentoSelezionato.getIdTipoProc(), LocalDateTime.now());
        procedimentoSelezionato = null;
    }
    
    public void preparaAssegnazione(ProcAssegnata proc) {
        procedimentoSelezionato = proc;
        List<UtenteRuolo> sorgente = us.getUtentiRuoloUfficio(getUtenteOmniapp().getUfficio().getIdUfficio(), proc.getIdTipoProc());
        UtentiRecord delegato = us.getUtenteDelegato(getUtenteOmniapp().getUtente(), tipoProcedimentoSelezionato.getIdTipoProc());
        List<UtenteRuolo> destinazione = ps.getAssegnazioniAttualiProcedura(delegato.getIdUtente(), procedimentoSelezionato.getIdProc());
        sorgente.removeAll(destinazione);
        utentiOriginali = new DualListModel<>(sorgente, destinazione);        
        utenti = new DualListModel<>(new ArrayList<>(sorgente), new ArrayList<>(destinazione));
    }

    public void salva() {
        try {
            // copia per evitare interferenze con la pagina in caso di errore
            List<UtenteRuolo> source = new ArrayList<>(utenti.getSource());
            List<UtenteRuolo> target = new ArrayList<>(utenti.getTarget()); 
            
            source.removeAll(utentiOriginali.getSource()); // da rimuovere
            target.removeAll(utentiOriginali.getTarget()); // da aggiungere
            
            UtentiRecord delegato = us.getUtenteDelegato(getUtenteOmniapp().getUtente(), tipoProcedimentoSelezionato.getIdTipoProc());
            ps.modificaAssegnazioni(procedimentoSelezionato, source, target, getUtenteOmniapp().getUtente(), delegato, LocalDateTime.now());

            aggiornaProcedimenti();

            PrimeFaces.current().executeScript("PF('wvUtentiDialog').hide();");
            addMessage(Message.info("Assegnazione modificata."));
        } catch (DatabaseException dbe) {
            addMessage(Message.error(dbe.toString()));
        }

    }
            
    public String[] splitAssegnatari(String ass) {
        return (ass!=null) ? ass.split("\\|") : new String[0];
    }
}
