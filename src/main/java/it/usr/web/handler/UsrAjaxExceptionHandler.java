/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.handler;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.FacesContext;
import org.omnifaces.exceptionhandler.FullAjaxExceptionHandler;
import static org.omnifaces.util.FacesLocal.getRequestAttribute;
import static org.omnifaces.util.FacesLocal.getRequest;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
public class UsrAjaxExceptionHandler extends FullAjaxExceptionHandler {
    Logger logger;
    
    public UsrAjaxExceptionHandler(ExceptionHandler wrapped, Logger logger) {
        super(wrapped);
        this.logger = logger;
    }

    @Override
    protected void logException(FacesContext context, Throwable exception, String location, String message, Object... parameters) {
        String uri = getRequest(context).getRequestURI();
        String uuid = getRequestAttribute(context, "org.omnifaces.exception_uuid");
        logger.error("UUID [{}]  -- INIZIO --", uuid);
        logger.error("Pagina [{}]", uri);            
        logger.error("Destinazione [{}]", location);            
        logger.error("Messaggio [{}]", message);
        logger.error("Parametri: [{}]", parameters);
        logger.error("Eccezione di base [{}]", exception!=null ? exception.toString() : null);
        logger.error("StackTrace:\n{}", stackTraceToString(exception!=null ? exception.getCause() : exception));
        logger.error("UUID [{}] -- FINE --", uuid);
    }
    
    private String stackTraceToString(Throwable t) {                
        if(t!=null) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for(StackTraceElement el : t.getStackTrace()) {
                if(i>0) sb = sb.append("\t");                
                sb = sb.append(el.toString()).append("\n");
                i++;
            }
            return sb.toString();
        }
        else {
            return "No Exception Trace.";
        }        
    }
}
