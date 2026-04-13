/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.controller.AbstractAuthController;
import static it.usr.web.controller.BaseController.SAME_VIEW;
import it.usr.web.domain.AppUser;
import it.usr.web.usromniapp.domain.tables.records.UfficiRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.model.Utente;
import it.usr.web.usromniapp.producer.LocalUserLogin;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.Map;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@RequestScoped
public class AuthController extends AbstractAuthController {
    @Inject
    UtenteService us;
    @Inject
    @LocalUserLogin
    boolean permettiLocalLogin;

    @Override
    public String doLogin() {
        logger.info("Accesso locale consentito: [{}].", permettiLocalLogin);
        if(!isEmpty(getUsername()) && getUsername().toLowerCase().endsWith("@local") && permettiLocalLogin) {
            String userName = getUsername().toLowerCase().replace("@local", "");
            UtentiRecord _user = us.login(userName, getPassword());
            if(_user!=null) {
                Object _attr = getUser(userName);
                AppUser u = new AppUser(userName, _attr);
                user.setCurrentUser(u);
                putIntoSession(u);
                logger.info("L'utente [{}] ha effettuato l'accesso locale.", userName);
                return redirect("/secure/index");
            }
        
                
            logger.info("L'utente [{}] non esiste o la password è errata o l'utente non abilitato.", userName);
            message = "Credenziali di accesso errate o utente non abilitato.";
            return SAME_VIEW;                
        }
        else { 
            return super.doLogin(); 
        }
    }
        
    @Override
    protected Object getUser(String username) {
        UtentiRecord u = us.getUtenteByUsername(username);
        if(u!=null & u.getAttivo()==1 && u.getUtenteFisico()==1) {
            List<UfficiRecord> uff = us.getUfficiUtente(u);
            Map<Integer, Integer> deleghe = us.getDelegheComplete(u);
            Map<Integer, List<Integer>> gerarchia = us.getGerarchiaRP(u.getIdUtente());
            Utente ut = new Utente(u, uff, deleghe, gerarchia);
            logger.info("Informazioni complete utente: {}", ut);
            return ut;
        } 
        return null;
    }      
}  
