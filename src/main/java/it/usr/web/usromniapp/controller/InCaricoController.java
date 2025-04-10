/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.SecurityService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
    List<ProcRecord> assegnazioni;
    ProcRecord assegnazioneSelezionata;
    Map<Integer, LTipoProcRecord> mTipiProc;
    
    public String init() {        
       if(!ss.canOpen(getUtenteOmniapp(), "A")) {
           logger.warn("L'utente [{}] non ha alcun accesso alla pagina assegnazioni. Invio pagina di notifica.", getUtenteOmniapp().getUtente().getNomeUtente());
           return "/access";
       }
       
       assegnazioni = ps.getAssegnazioni(getUtenteOmniapp());
       mTipiProc = ps.getTipiProcedimentoMap();
       assegnazioneSelezionata = null;
       return SAME_VIEW;
    }

    public List<ProcRecord> getAssegnazioni() {
        return assegnazioni;
    }

    public Map<Integer, LTipoProcRecord> getmTipiProc() {
        return mTipiProc;
    }
        
    public ProcRecord getAssegnazioneSelezionata() {
        return assegnazioneSelezionata;
    }

    public void setAssegnazioneSelezionata(ProcRecord assegnazioneSelezionata) {
        this.assegnazioneSelezionata = assegnazioneSelezionata;
    }   
    
    public LTipoProcRecord decodificatProcedimento(int idTipoProc) {
        return mTipiProc.get(idTipoProc);
    }
}
