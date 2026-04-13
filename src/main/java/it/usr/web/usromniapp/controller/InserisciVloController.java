/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.ProcTecnici;
import it.usr.web.usromniapp.domain.tables.records.GisCentroidiRecord;
import it.usr.web.usromniapp.domain.tables.records.LRuoloTecnicoRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcCatRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcIterRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcTecniciRecord;
import it.usr.web.usromniapp.model.TecnicoRuolo;
import it.usr.web.usromniapp.service.CodiceService;
import it.usr.web.usromniapp.service.DatabaseException;
import it.usr.web.usromniapp.service.DuplicationException;
import it.usr.web.usromniapp.service.ProcedimentoService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.primefaces.PrimeFaces;
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
    CodiceService cs;
    ProcRecord proc;
    ProcIterRecord procIter;
    List<ProcCatRecord> procCat;
    List<GisCentroidiRecord> centroidi;
    Map<Integer, LRuoloTecnicoRecord> ruoliTecnici;
    List<ProcTecniciRecord> tecnici;
    List<TecnicoRuolo> procTecnici;
    TecnicoRuolo tecnicoRuolo;
    ProcTecniciRecord tecnico;
     
    public String init() {
        proc = null;
        procIter = null; 
        procCat = null;
        
        LTipoProcRecord tipoProc = ps.getTipoProcedimentoByNome("VLO");
        /*if(!ss.isAuthorized(getUtenteOmniapp(), tipoProc, TipoOperazioneEnum.M)) {
            return redirect("/access");
        }*/
        
        centroidi = cs.getCentroidi();
        proc = new ProcRecord();
        proc.setIdTipoProc(tipoProc.getIdTipoProc());
        procIter = new ProcIterRecord();
        procCat = new ArrayList<>();
        procTecnici = new ArrayList<>();
        ruoliTecnici = ps.getRuoliTecniciMap();
        tecnici = ps.getProcTecnici();
        tecnico = new ProcTecniciRecord();
        tecnicoRuolo = new TecnicoRuolo();
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

    public Collection<LRuoloTecnicoRecord> getRuoliTecnici() {
        return ruoliTecnici.values();
    }

    public List<TecnicoRuolo> getProcTecnici() {
        return procTecnici;
    }

    public ProcIterRecord getProcIter() {
        return procIter;
    }
         
    public TecnicoRuolo getTecnicoRuolo() {
        return tecnicoRuolo;
    }

    public void setTecnicoRuolo(TecnicoRuolo tecnicoRuolo) {
        this.tecnicoRuolo = tecnicoRuolo;
    }

    public List<ProcTecniciRecord> getTecnici() { 
        return tecnici;
    }

    public ProcTecniciRecord getTecnico() {
        return tecnico;
    }
            
    public void nuovoTecnicoRuolo() { 
        tecnicoRuolo = new TecnicoRuolo();        
    } 
    
    public void nuovoTecnico() {
        tecnico = new ProcTecniciRecord();
    }
    
    public void aggiungiTecnicoRuolo() {
        if(!procTecnici.contains(tecnicoRuolo)) {
            procTecnici.add(tecnicoRuolo);
            tecnicoRuolo = new TecnicoRuolo();
            PrimeFaces.current().executeScript("PF('wvIncaricoDialog').hide();");
        }
        else {
            addMessage(Message.warn("Attenzione! La coppia tecnico-ruolo è già presente in elenco."));            
        }
    }
    
    public void annullaTecnicoRuolo() {
        tecnicoRuolo = new TecnicoRuolo(); 
    }
    
    public void rimuoviTecnicoRuolo(TecnicoRuolo tr) {
        procTecnici.remove(tr);
        tecnicoRuolo = new TecnicoRuolo();
    }
            
    public void salva() { 
        if(procCat.isEmpty()) {
            addMessage(Message.warn("Inserire almeno un dato catastale."));
            return;
        }
        else {
            procCat.forEach(pc -> {
                pc.setIstatComune(String.valueOf(proc.getCodiceCom()));
            });
        }
        
        if(procTecnici.isEmpty()) {
            addMessage(Message.warn("Inserire almeno tecnico incaricato."));
            return;        
        }
        
        try {                        
            procIter.setIdUtente(getUtenteOmniapp().getUtente().getIdUtente());                                    
            int idVlo = ps.inserisciProcedimento(proc, procCat, procTecnici, procIter);
            logger.info("Procedura VLO inserita con ID=[{}]", idVlo);
            addMessage(Message.info("VLO inserita correttamente."));
        }
        catch(DatabaseException dbe) {
            logger.error("Inserimento procedura VLO non andata a buon fine a causa di: {}", dbe);            
            logInnerField(logger, this);
            addMessage(Message.error("Inserimento VLO non andata a buon fine a causa di: "+dbe.getMessage()));
        }
    }
    
    public void salvaTecnico() {
        try {
            int id = ps.inserisciTecnico(tecnico);
            logger.info("Tecnico inserito con ID=[{}]", id);
            addMessage(Message.info("Tecnico inserito correttamente."));
            tecnici = ps.getProcTecnici();
            
            PrimeFaces.current().executeScript("PF('wvTecnicoDialog').hide();");
        }
        catch(DuplicationException de) {
            logger.warn("Il tecnico con i dati {} esiste già in archvio.", tecnico);
            addMessage(Message.warn("Un tecnico con la i dati indicati esiste già in archivio."));
        }
        catch(DatabaseException dbe) {
            logger.error("Inserimento tecnico non andato a buon fine a causa di: {}", dbe);            
            logInnerField(logger, this);
            addMessage(Message.error("Inserimento tecnico non andato a buon fine a causa di: "+dbe.getMessage()));
        }
    }
    
    public void aggiungiProcCat() { 
        ProcCatRecord pcr = new ProcCatRecord();
        pcr.setAttivo(1);
        //pcr.setIstatComune(proc.getCodiceCom()!=null ? String.valueOf(proc.getCodiceCom()) : "ISTAT");
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
