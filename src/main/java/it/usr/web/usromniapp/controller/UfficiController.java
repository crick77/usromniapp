/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.UfficiRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.service.DatabaseException;
import it.usr.web.usromniapp.service.UfficiUtentiService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class UfficiController extends BaseController {
    @AppLogger
    @Inject
    Logger logger;
    @Inject
    UfficiUtentiService uus;    
    Map<Integer, UtentiRecord> utenti;
    TreeNode<UfficiRecord> uffici;
    UfficiRecord ufficio;
    List<UfficiRecord> ufficiList;
    String estremiProvv;
    
    public void init() {
        aggiorna();
        ufficio = new UfficiRecord();
        estremiProvv = null;
    }

    public void aggiorna() {
        uffici = new DefaultTreeNode<>(null, null);
        uffici.setExpanded(true);
        
        utenti = uus.getUtentiMap();
        ufficiList = uus.getUffici();
        rebuildTree(ufficiList, uffici);
    }
    
    public TreeNode<UfficiRecord> getUffici() {
        return uffici;
    } 
           
    public Map<Integer, UtentiRecord> getUtenti() {
        return utenti;
    }
    
    public Collection<UtentiRecord> getUtentiList() {
        return utenti.values();
    }
    
    public UtentiRecord decodeUtente(int idUtente) {
        return utenti.get(idUtente); 
    }            

    public UfficiRecord getUfficio() {
        return ufficio;
    }

    public void setUfficio(UfficiRecord ufficio) {
        this.ufficio = ufficio;
    }

    public String getEstremiProvv() {
        return estremiProvv;
    }

    public void setEstremiProvv(String estremiProvv) {
        this.estremiProvv = estremiProvv;
    }
        
    public List<UfficiRecord> getUfficiPossibili() {
        return (ufficio==null) ? 
                ufficiList : ufficiList.stream().filter(u -> !u.equals(ufficio)).collect(Collectors.toList());
    }
    
    private void rebuildTree(List<UfficiRecord> lUffici, TreeNode<UfficiRecord> parent) {
        if(parent.getData()==null) {
            for(UfficiRecord u : lUffici) {
                if(u.getIdUfficioSovraordinato()==null) {
                    TreeNode<UfficiRecord> child = new DefaultTreeNode<>(u, parent);
                    child.setExpanded(true);
                    rebuildTree(lUffici, child);                    
                }
            }
        } 
        else {
            for(UfficiRecord u : lUffici) {
                if(Objects.equals(u.getIdUfficioSovraordinato(), parent.getData().getIdUfficio())) {
                    TreeNode<UfficiRecord> child = new DefaultTreeNode<>(u, parent);
                    child.setExpanded(true);
                    rebuildTree(lUffici, child);
                }
            }
        }
    } 
    
    public void nuovo() {
        ufficio = new UfficiRecord();
        estremiProvv = null;
    }
    
    public void salva() {
        try {
            if(ufficio.getIdUfficio()!=null) {
                uus.modifica(ufficio, estremiProvv);
            }               
            else {
                uus.inserisci(ufficio);
            }
            
            aggiorna();
            addMessage(Message.info("Ufficio inserieto correttamente."));           
            PrimeFaces.current().executeScript("PF('vwUfficioDialog').hide();");
        }
        catch(DatabaseException de) {
            addMessage(Message.error(de.getMessage()));            
        }                
    }
    
    public void annulla() {
        ufficio = new UfficiRecord();
        PrimeFaces.current().executeScript("PF('vwUfficioDialog').hide();");
    }
    
    public void disattiva(UfficiRecord ufficio) {
        try {
            uus.disattiva(ufficio, estremiProvv);
                        
            aggiorna();
            addMessage(Message.info("Ufficio disabilitato correttamente."));           
            PrimeFaces.current().executeScript("PF('vwUfficioDialog').hide();");
        }
        catch(DatabaseException de) {
            addMessage(Message.error(de.getMessage()));            
        } 
    }
}
