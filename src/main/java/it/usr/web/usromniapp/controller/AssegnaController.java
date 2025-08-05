/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.GisCentroidiRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.interceptor.RequiredAuthorization;
import it.usr.web.usromniapp.interceptor.SecurityCheck;
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
public class AssegnaController extends OmniappBaseController {
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
    List<ProcRecord> procedimenti;
    List<ProcRecord> procedimentiSelezionati;
    List<ProcRecord> procedimentiFiltrati;
    DualListModel<UtenteRuolo> utenti;
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

    public List<ProcRecord> getProcedimenti() {
        return procedimenti;
    }

    public void setProcedimenti(List<ProcRecord> procedimenti) {
        this.procedimenti = procedimenti;
    }

    public List<ProcRecord> getProcedimentiSelezionati() {
        return procedimentiSelezionati;
    }

    public void setProcedimentiSelezionati(List<ProcRecord> procedimentiSelezionati) {
        this.procedimentiSelezionati = procedimentiSelezionati;
    }

    public DualListModel<UtenteRuolo> getUtenti() {
        return utenti;
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

    public List<ProcRecord> getProcedimentiFiltrati() {
        return procedimentiFiltrati;
    }

    public void setProcedimentiFiltrati(List<ProcRecord> procedimentiFiltrati) {
        this.procedimentiFiltrati = procedimentiFiltrati;
    }
                
    public void setUtenti(DualListModel<UtenteRuolo> utenti) {
        this.utenti = utenti;
    }
            
    public LTipoProcRecord decodificatProcedimento(int idTipoProc) {
        return mTipiProc.get(idTipoProc);
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
    
    public void aggiorna() {
        tipiProcedimento = ps.getTipiProcedureAutorizzate(getUtenteOmniapp());
        mCentroidi = cs.getCentroidiMap();
        tipoProcedimentoSelezionato = null;
        procedimenti = null;
        procedimentiSelezionati = null;
        procedimentiFiltrati = null;
    }
    
    public void aggiornaProcedimenti() {
        UtentiRecord delegato = us.getUtenteDelegato(getUtenteOmniapp().getUtente(), tipoProcedimentoSelezionato.getIdTipoProc());
        procedimenti = ps.getProcedureDaAssegnare(delegato.getIdUtente(), tipoProcedimentoSelezionato.getIdTipoProc(), LocalDateTime.now()); 
        procedimentiSelezionati = null;
    }
    
    public void preparaAssegnazione() {        
        utenti.setSource(us.getUtentiRuoloUfficio(getUtenteOmniapp().getUfficio().getIdUfficio(), tipoProcedimentoSelezionato.getIdTipoProc()));
        utenti.setTarget(new ArrayList<>());
    }
     
    public void preparaAssegnazione(ProcRecord proc) {        
        procedimentiSelezionati = new ArrayList<>();
        procedimentiSelezionati.add(proc);
         
        preparaAssegnazione();
    }
    
    public void salva() {
        try {
            UtentiRecord delegato = us.getUtenteDelegato(getUtenteOmniapp().getUtente(), tipoProcedimentoSelezionato.getIdTipoProc());
            ps.inserisciAssegnazioni(procedimentiSelezionati, utenti.getTarget(), getUtenteOmniapp().getUtente(), delegato);
 
            aggiorna();            
            
            PrimeFaces.current().executeScript("PF('wvUtentiDialog').hide();");
            addMessage(Message.info("Assegnazione effettuata."));
        }
        catch(DatabaseException dbe) {
            addMessage(Message.error(dbe.toString()));
        }
    }
}
