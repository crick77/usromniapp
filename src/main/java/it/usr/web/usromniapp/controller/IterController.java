/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.LTipoPassoRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcEsitiRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcIterRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.interceptor.RequiredAuthorization;
import it.usr.web.usromniapp.interceptor.SecurityCheck;
import it.usr.web.usromniapp.service.DatabaseException;
import it.usr.web.usromniapp.service.IterService;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.SecurityService;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class IterController extends OmniappBaseController {
    @Inject
    IterService is;
    @Inject
    ProcedimentoService ps;
    @Inject
    SecurityService ss;
    @Inject
    UtenteService us;
    @Inject
    @AppLogger
    Logger logger;
    Integer idProc;
    ProcRecord procedimento;
    List<ProcIterRecord> iterPratica;
    List<ProcIterRecord> iterPraticaLink;
    ProcIterRecord iterPraticaSelezionato;
    ProcIterRecord iter;
    Map<Integer, LTipoPassoRecord> tipiPasso;
    List<SelectItem> tipiPassoIter;
    List<ProcEsitiRecord> esiti;
    Map<Integer, UtentiRecord> utenti;
    LTipoPassoRecord passoSelezionato;
    String azione;
    
    @SecurityCheck
    @RequiredAuthorization(TipoOperazioneEnum.M)
    public String init() {    
        if(!ss.isAssigned(getUtenteOmniapp(), idProc, LocalDateTime.now())) {
            return "/access";
        }
        aggiornaIter(); 
        
        return SAME_VIEW;
    }

    public Integer getIdProc() {
        return idProc;
    }

    public void setIdProc(Integer idProc) {
        this.idProc = idProc;
    }

    public List<ProcIterRecord> getIterPratica() {
        return iterPratica;
    }

    public ProcRecord getProcedimento() {
        return procedimento;
    } 

    public LTipoPassoRecord decodeTipoPasso(int codicePasso) {
        return tipiPasso.get(codicePasso);
    }

    public ProcEsitiRecord decodeEsito(int idEsito) {
        return esiti.stream().filter(e -> e.getIdEsito()==idEsito).findFirst().orElse(null);
    }
    
    public List<SelectItem> getTipiPassoIter() {
        return tipiPassoIter;
    }

    public ProcIterRecord getIter() {
        return iter;
    }

    public LTipoPassoRecord getPassoSelezionato() {
        return passoSelezionato; 
    }

    public List<ProcEsitiRecord> getEsiti() {
        return esiti;
    }
             
    public UtentiRecord decodificaUtente(int idUtente) {
        return utenti.get(idUtente);
    }

    public List<ProcIterRecord> getIterPraticaLink() {
        return iterPraticaLink;
    }

    public ProcIterRecord getIterPraticaSelezionato() {
        return iterPraticaSelezionato;
    }

    public void setIterPraticaSelezionato(ProcIterRecord iterPraticaSelezionato) {
        this.iterPraticaSelezionato = iterPraticaSelezionato;
    }

    public String getAzione() {
        return azione;
    }
                            
    public void aggiornaIter() {
        procedimento = ps.getProcedimentoById(idProc);
        tipiPasso = is.getTipiPasso(procedimento.getIdTipoProc());
        esiti = is.getEsiti(procedimento.getIdTipoProc()); 
        
        iterPratica = is.getIterAttiviByIdProc(idProc);                         
        List<Integer> utentiIter = iterPratica.stream().map(ProcIterRecord::getIdUtente).distinct().collect(Collectors.toList());
        List<UtentiRecord> ur = us.getUtenti(utentiIter);
        utenti = new HashMap<>();
        ur.forEach(u -> utenti.put(u.getIdUtente(), u));        
        
        iter = new ProcIterRecord();
        tipiPassoIter = null;
        passoSelezionato = null;
        iterPraticaLink = null;
        iterPraticaSelezionato = null; 
        azione = null;
    }        
    
    public void nuovo() { 
        iter = new ProcIterRecord();
        iter.setIdProc(idProc);
        iter.setIdUtente(getUtenteOmniapp().getUtente().getIdUtente());
        
        aggiornaDatiDialog();
        
        passoSelezionato = null;
        azione = "Nuovo";
    } 
    
    public void aggiornaDatiDialog() {
        Integer ultimoPasso = null;
        if(!iterPratica.isEmpty()) {
            ultimoPasso = iterPratica.getFirst().getCodicePasso();
        }
        List<LTipoPassoRecord> tpAmmessi = is.getTipiPasso(procedimento.getIdTipoProc(), ultimoPasso);
        List<LTipoPassoRecord> tpTotali = is.getTipiPasso(procedimento.getIdTipoProc(), null);        
        tipiPassoIter = new ArrayList<>();
        
        SelectItemGroup sgAmmessi = new SelectItemGroup("ITER SUCCESSIVI AMMESSI");
        final List<SelectItem> sItemAmmessi = new ArrayList<>();
        tpAmmessi.forEach(e -> {
            sItemAmmessi.add(new SelectItem(e.getCodicePasso(), e.getTestoPasso()));
        });
        if(sItemAmmessi.isEmpty()) sItemAmmessi.add(new SelectItem(null, "-- nessun passo disponibile --", null, true));        
        sgAmmessi.setSelectItems(sItemAmmessi);
        
        SelectItemGroup sgTotali = new SelectItemGroup("TUTTI GLI ITER");
        final List<SelectItem> sItemTotali = new ArrayList<>();
        tpTotali.forEach(e -> {
            sItemTotali.add(new SelectItem(e.getCodicePasso(), e.getTestoPasso()));
        });
        sgTotali.setSelectItems(sItemTotali);
         
        tipiPassoIter.add(sgAmmessi); 
        tipiPassoIter.add(sgTotali);                 
    }
    
    public void modifica(ProcIterRecord i) {
        iter = i;
        iter.setIdUtente(getUtenteOmniapp().getUtente().getIdUtente());
        
        aggiornaDatiDialog();
        
        azione = "Modifica";
    } 
    
    public void mostraDettaglio(ProcIterRecord i) {
        iter = i;
        aggiornaDatiDialog();
    }
    
    public void aggiornaPasso() {
        passoSelezionato = is.getPassoByCodice(iter.getCodicePasso());
    }
    
    public boolean isEsitoObbligatorio() {
        if(passoSelezionato==null || passoSelezionato.getEsitoObbligatorio()==null) return false;
        return passoSelezionato.getEsitoObbligatorio();
    }
    
    public void onRowToggle(ToggleEvent event) {
        if(event.getVisibility()==Visibility.VISIBLE) {
            ProcIterRecord sel = (ProcIterRecord)event.getData(); 
            iterPraticaLink = is.getSequenzaIter(sel.getIdProcIter());
        }        
    }
    
    public void salva() {  
        try {
            if(iter.getIdProcIter()==null) {
                is.inserisci(iter);
            }
            else {
                is.modifica(iter);
            }
            
            aggiornaIter();
            PrimeFaces.current().executeScript("PF('iterDialogWV').hide();");
        }
        catch(DatabaseException dbe) {
            addMessage(Message.error(dbe.toString()));
        }         
    }   
         
    public String tornaElencoPratiche() {
        return "incarico";
    }
}
