/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.GisCentroidiRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcCatRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcIterRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.service.CodiceService;
import it.usr.web.usromniapp.service.DatabaseException;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.SecurityService;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class InserisciVloController extends OmniappBaseController {
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ProcedimentoService ps;        
    @Inject
    SecurityService ss;
    @Inject
    CodiceService cs;
    ProcRecord proc;
    ProcIterRecord procIter;
    List<ProcCatRecord> procCat;
    List<GisCentroidiRecord> centroidi;
    
    public String init() {
        proc = null;
        procIter = null;
        procCat = null;
        
        LTipoProcRecord tipoProc = ps.getTipoProcedimento("VLO");
        if(!ss.isAuthorized(getUtenteOmniapp(), tipoProc, TipoOperazioneEnum.M)) {
            return redirect("/access");
        }
        
        centroidi = cs.getCentroidi();
        proc = new ProcRecord();
        proc.setIdTipoProc(tipoProc.getIdTipoProc());
        procIter = new ProcIterRecord();
        procCat = new ArrayList<>();
        return SAME_VIEW;
    }

    public ProcRecord getProc() {
        return proc;
    }

    public List<ProcCatRecord> getProcCat() {
        return procCat;
    }
       
    public GisCentroidiRecord getCodiceCom() {
        return proc.getCodiceCom()!=null ? cs.getCentroide(proc.getCodiceCom()) : null;
    } 
    
    public void setCodiceCom(GisCentroidiRecord cr) {
        proc.setCodiceCom(cr.getCodiceCom());
    }
    
    public void salva() { 
        if(procCat.isEmpty()) {
            addMessage(Message.warn("Inserire almeno un dato catastale."));
            return;
        }
        
        try {                        
            procIter.setIdUtente(getUtenteOmniapp().getUtente().getIdUtente());                        
            int idVlo = ps.inserisci(proc, procCat, procIter);
            logger.info("Procedura VLO inserita con ID=[{}]", idVlo);
            addMessage(Message.info("VLO inserita correttamente."));
        }
        catch(DatabaseException dbe) {
            logger.error("Inserimento procedura VLO non andata a buon fine a causa di: {}", dbe);            
            logInnerField(logger, this);
            addMessage(Message.error("Inserimento VLO non andata a buon fine a causa di: "+dbe.getMessage()));
        }
    }
    
    public void aggiungiProcCat() { 
        ProcCatRecord pcr = new ProcCatRecord();
        pcr.setAttivo(1);
        pcr.setIstatComune(proc.getCodiceCom()!=null ? String.valueOf(proc.getCodiceCom()) : "ISTAT");
        pcr.setSezione("0"); 
        pcr.setFoglio("0");
        pcr.setParticella("0"); 
        procCat.add(pcr); 
    } 
    
    public void rimuoviProcCat(ProcCatRecord pcr) {
        procCat.remove(pcr);
    }
    
    public List<GisCentroidiRecord> completaComune(String text) {
       final String qText = (text!=null) ? text.toLowerCase() : null;
       return centroidi.stream().filter(c -> (c.getComune()+" "+c.getCodiceCom()).contains(qText)).collect(Collectors.toList()); 
    }
}
