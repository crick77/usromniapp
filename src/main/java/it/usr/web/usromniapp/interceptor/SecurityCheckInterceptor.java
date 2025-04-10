/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.interceptor;

import it.usr.web.domain.ActiveUser;
import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.model.Utente;
import it.usr.web.usromniapp.service.SecurityService;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.annotation.Priority;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.StringJoiner;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Interceptor
@SecurityCheck
@Priority(Interceptor.Priority.APPLICATION)
public class SecurityCheckInterceptor implements Serializable {    
    @Inject
    ActiveUser user;
    @Inject
    SecurityService ss;    
    @Inject
    @AppLogger
    Logger logger;
    
    @AroundInvoke
    public Object doSecurityCheck(final InvocationContext context) throws Exception {
        if(!context.getMethod().getReturnType().equals(String.class)) {
            throw new IllegalCallerException("Il metodo ["+context.getMethod().getName()+"] deve restituire una stringa per essere controllato.");
        }
        
        RequiredAuthorization ra = (RequiredAuthorization)context.getMethod().getAnnotation(RequiredAuthorization.class);        
        if(ra==null) throw new IllegalCallerException("Il metodo ["+context.getMethod().getName()+"] non contiene la specifica di RequiredAuthorization.");
        
        Utente u = (Utente)user.getCurrentUser().getAttributes();
        logger.info("Verifico l'accesso alla risorsa [{}] per l'utente [{}] con le autorizzazioni [{}].", context.getMethod().getDeclaringClass().getSimpleName(), u.getUtente().getUtente(), ra.value());
        if(!ss.canOpen(u, ss.arrayToString(ra.value()))) {
           logger.warn("[{}] L'utente [{}] non ha alcun accesso alla pagina [{}]. Invio pagina di notifica.", this.getClass().getSimpleName(), u.getUtente().getUtente(), FacesContext.getCurrentInstance().getViewRoot().getViewId());
           return "/access";
        } 
        else {
            logger.info("Accesso alla risorsa [{}] per l'utente [{}] con le autorizzazioni [{}] consentita.", context.getMethod().getDeclaringClass().getSimpleName(), u.getUtente().getUtente(), ra.value());
            return context.proceed();
        } 
    }         
} 
