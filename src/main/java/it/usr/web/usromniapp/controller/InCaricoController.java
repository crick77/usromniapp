/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.controller.Redirector;
import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.GisCentroidiRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoPassoRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcEsitiRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.record.RProc;
import it.usr.web.usromniapp.service.CodiceService;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.SecurityService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class InCaricoController extends OmniappBaseController {
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ProcedimentoService ps;
    @Inject
    SecurityService ss;
    @Inject
    CodiceService cs;
    List<RProc> assegnazioni;
    List<RProc> assegnazioniFiltrate;
    RProc assegnazioneSelezionata;
    Map<Integer, LTipoProcRecord> mTipiProc;
    Map<Integer, LTipoPassoRecord> mTipiPasso;
    Map<Integer, ProcEsitiRecord> mEsiti;
    Map<Integer, GisCentroidiRecord> mCentroidi;
    
    public String init() {        
       if(!ss.canOpen(getUtenteOmniapp(), "A")) {
           logger.warn("L'utente [{}] non ha alcun accesso alla pagina assegnazioni. Invio pagina di notifica.", getUtenteOmniapp().getUtente().getNomeUtente());
           return "/access";
       }
       
       assegnazioni = ps.getAssegnazioni(getUtenteOmniapp());
       mTipiProc = ps.getTipiProcedimentoMap();
       mTipiPasso = cs.getTipiPassoMap();
       mEsiti = cs.getProcEsitiMap();
       mCentroidi = cs.getCentroidiMap();
       assegnazioneSelezionata = null;
       assegnazioniFiltrate = null;
       return SAME_VIEW;
    }

    public List<RProc> getAssegnazioniFiltrate() {
        return assegnazioniFiltrate;
    }

    public void setAssegnazioniFiltrate(List<RProc> assegnazioniFiltrate) {
        this.assegnazioniFiltrate = assegnazioniFiltrate;
    }
         
    public List<RProc> getAssegnazioni() {
        return assegnazioni;
    }
    

    public Map<Integer, LTipoProcRecord> getmTipiProc() {
        return mTipiProc;
    }

    public Collection<LTipoProcRecord> getTipiProcedimento() {
        return mTipiProc.values();
    }
    
    public Map<Integer, LTipoPassoRecord> getmTipiPasso() { 
        return mTipiPasso;
    }

    public Collection<LTipoPassoRecord> getTipiPasso() {
        return mTipiPasso.values();
    }
    
    public Map<Integer, ProcEsitiRecord> getmEsiti() {
        return mEsiti;
    } 
       
    public Collection<ProcEsitiRecord> getEsiti() {
        return mEsiti.values();
    }
    
    public RProc getAssegnazioneSelezionata() {
        return assegnazioneSelezionata;
    }

    public void setAssegnazioneSelezionata(RProc assegnazioneSelezionata) {
        this.assegnazioneSelezionata = assegnazioneSelezionata;
    }
    
    
    public LTipoProcRecord decodificatProcedimento(int idTipoProc) {
        return mTipiProc.get(idTipoProc);
    }
    
    public LTipoPassoRecord decodificaPasso(int codicePasso) {
        return mTipiPasso.get(codicePasso);
    }
    
    public ProcEsitiRecord decodificaEsito(int idProcEsiti) {
        return mEsiti.get(idProcEsiti);
    }
    
    public GisCentroidiRecord decodificaCentroide(int codiceCom) {
        return mCentroidi.get(codiceCom);
    }
    
    public String mostraIter(RProc proc) {
        return Redirector.toPath("iter").withParam("idproc", proc.getIdProc()).withRedirect().go();
    }
}
  