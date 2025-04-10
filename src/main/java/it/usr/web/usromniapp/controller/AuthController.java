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
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@RequestScoped
public class AuthController extends AbstractAuthController {
    @Inject
    UtenteService us;

    @Override
    public String doLogin() {
        if(!isEmpty(getUsername()) && getUsername().toLowerCase().endsWith("@local")) {
            String userName = getUsername().toLowerCase().replace("@local", "");
            UtentiRecord _user = us.login(userName, getPassword());
            if(_user!=null) {
                Object _attr = getUser(userName);
                AppUser u = new AppUser(userName, _attr);
                user.setCurrentUser(u);
                putIntoSession(u);
                logger.debug("L'utente [{}] ha effettuato l'accesso locale.", userName);
                return redirect("/secure/index");
            }
        
                
            logger.debug("L'utente [{}] non esiste o la password è errata o l'utente non abilitato.", userName);
            message = "Credenziali di accesso errate o utente non abilitato.";
            return SAME_VIEW;                
        }
        else { 
            return super.doLogin(); 
        }
    }
        
    @Override
    protected Object getUser(String username) {
        UtentiRecord u = us.getUtente(username);
        if(u!=null & u.getAttivo()==1 && u.getUtenteFisico()==1) {
            UfficiRecord uff = us.getUfficioUtente(u);
            return new Utente(u, uff);
        }
        return null;
    }      
} 
