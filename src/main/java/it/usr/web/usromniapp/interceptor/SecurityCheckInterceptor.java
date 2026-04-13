/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.interceptor;

import it.usr.web.domain.ActiveUser;
import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.model.Utente;
import it.usr.web.usromniapp.service.SecurityService;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;
import jakarta.annotation.Priority;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.io.Serializable;
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
        //String function = context.getMethod().getClass().getName()+"."+context.getMethod().getName();
        String function = context.getMethod().getDeclaringClass().getSimpleName();
        int authCalc = calcolaAutorizzazioni(ra);
        logger.info("Verifico l'accesso alla risorsa [{}] per l'utente [{}] con le autorizzazioni [{}/{}].", function, u.getUtente().getUtente(), ra.value(), authCalc);
        if(!ss.checkFunction(function, u, authCalc)) {
           logger.warn("L'utente [{}] non ha accesso alla risorsa [{}]/pagina [{}] con autorizzazioni [{}]. Invio pagina di notifica.", u.getUtente().getUtente(), function, FacesContext.getCurrentInstance().getViewRoot().getViewId(), ra.value());
           return "/access";
        } 
        else {
            logger.info("L'utente [{}] ha accesso alla risorsa [{}] con le autorizzazioni [{}] consentita.", u.getUtente().getUtente(), function,  ra.value());
            return context.proceed();
        } 
    }       
 
    private int calcolaAutorizzazioni(RequiredAuthorization a) {
        int res = 0;
        for(TipoOperazioneEnum op : a.value()) {
            res = res | op.getOperazione();
        }
        return res;
    }
} 
