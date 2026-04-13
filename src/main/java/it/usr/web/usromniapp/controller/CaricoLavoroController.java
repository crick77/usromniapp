/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import static it.usr.web.controller.BaseController.SAME_VIEW;
import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.interceptor.RequiredAuthorization;
import it.usr.web.usromniapp.interceptor.SecurityCheck;
import it.usr.web.usromniapp.model.Carico;
import it.usr.web.usromniapp.record.RProc;
import it.usr.web.usromniapp.service.CodiceService;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.SecurityService;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class CaricoLavoroController extends OmniappBaseController {
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ProcedimentoService ps;
    @Inject
    SecurityService ss;
    @Inject
    CodiceService cs;
    @Inject
    UtenteService us;
    List<LTipoProcRecord> tipiProcedimento; 
    LTipoProcRecord tipoProcedimentoSelezionato;
    Map<Integer, UtentiRecord> utenti;
    Map<Integer, Carico> carichiLavoro;
    int totalePratiche;
    TipoOperazioneEnum[] LM = new TipoOperazioneEnum[] { TipoOperazioneEnum.L, TipoOperazioneEnum.M };
    
    @SecurityCheck
    @RequiredAuthorization(TipoOperazioneEnum.M)
    public String init() {       
        tipiProcedimento = ps.getTipiProcedureAutorizzate(getUtenteOmniapp(), new TipoOperazioneEnum[] { TipoOperazioneEnum.M });  
        tipoProcedimentoSelezionato = null;
        totalePratiche = 0;
        utenti = us.getUtenti().stream().collect(Collectors.toMap(UtentiRecord::getIdUtente, Function.identity()));
                 
        return SAME_VIEW;
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
            
    public UtentiRecord decodeUtente(int idUtente) {
        return utenti.get(idUtente);
    }
    
    public Carico caricoLavoroUtente(int idUtente) {
        return carichiLavoro.get(idUtente);
    }
    
    public Set<Integer> getUtenti() {
        return carichiLavoro.keySet();
    }
 
    public int getTotalePratiche() {
        return totalePratiche;
    }
    
    public int calcolaPercentuale(int pratiche) {
        return Math.round(((float)pratiche/(float)totalePratiche)*100.0f);
    }
    
    public void ricalcolaCarichi() {
        totalePratiche = 0;
        int idTipoProc = tipoProcedimentoSelezionato.getIdTipoProc();
        List<RProc> daIstruire = ps.getPraticheInCarico(getUtenteOmniapp(), idTipoProc, LM, 0);
        List<RProc> istruite = ps.getPraticheInCarico(getUtenteOmniapp(), idTipoProc, LM, 1);
        List<RProc> integrate = ps.getPraticheInCarico(getUtenteOmniapp(), idTipoProc, LM, 2);
        List<RProc> allaFirma = ps.getPraticheInCarico(getUtenteOmniapp(), idTipoProc, LM, 3);
        List<RProc> concluse = ps.getPraticheInCarico(getUtenteOmniapp(), idTipoProc, LM, 4);
        List<RProc> inRevisione = ps.getPraticheInCarico(getUtenteOmniapp(), idTipoProc, LM, 5);

        carichiLavoro = new HashMap<>();

        daIstruire.forEach(p -> {   
            int idUtente = p.getIter().getIdUtente();
            Carico c;
            if(!carichiLavoro.containsKey(idUtente)) {
                c = new Carico();
                carichiLavoro.put(idUtente, c);
            }
            else {
                c = carichiLavoro.get(idUtente);
            }
            
            c.incrementaTotaleDaIstruire();  
            totalePratiche++;
        });
        
        istruite.forEach(p -> {   
            int idUtente = p.getIter().getIdUtente();
            Carico c;
            if(!carichiLavoro.containsKey(idUtente)) {
                c = new Carico();
                carichiLavoro.put(idUtente, c);
            }
            else {
                c = carichiLavoro.get(idUtente);
            }
            
            c.incrementaTotaleIstruite();
            totalePratiche++;
        });
        
        integrate.forEach(p -> {   
            int idUtente = p.getIter().getIdUtente();
            Carico c;
            if(!carichiLavoro.containsKey(idUtente)) {
                c = new Carico();
                carichiLavoro.put(idUtente, c);
            }
            else {
                c = carichiLavoro.get(idUtente);
            }
            
            c.incrementaTotaleIntegrate();
            totalePratiche++;
        });
        
        allaFirma.forEach(p -> {   
            int idUtente = p.getIter().getIdUtente();
            Carico c;
            if(!carichiLavoro.containsKey(idUtente)) {
                c = new Carico();
                carichiLavoro.put(idUtente, c);
            }
            else {
                c = carichiLavoro.get(idUtente);
            }
            
            c.incrementaTotaleAllaFirma();
        });
        
        concluse.forEach(p -> {   
            int idUtente = p.getIter().getIdUtente();
            Carico c;
            if(!carichiLavoro.containsKey(idUtente)) {
                c = new Carico();
                carichiLavoro.put(idUtente, c);
            }
            else {
                c = carichiLavoro.get(idUtente);
            }
            
            c.incrementaTotaleConcluse();
            totalePratiche++;
        });
        
        inRevisione.forEach(p -> {   
            int idUtente = p.getIter().getIdUtente();
            Carico c;
            if(!carichiLavoro.containsKey(idUtente)) {
                c = new Carico();
                carichiLavoro.put(idUtente, c);
            }
            else {
                c = carichiLavoro.get(idUtente);
            }
            
            c.incrementaTotaleInRevisione();
            totalePratiche++;
        });               
    }
}
