/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.usromniapp.model.Utente;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;

/**
 *
 * @author riccardo.iovenitti
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Creates a new instance of <code>AccessDeniedException</code> without
     * detail message.
     */
    public AccessDeniedException() {
    }

    /**
     * Constructs an instance of <code>AccessDeniedException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public AccessDeniedException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>AccessDeniedException</code> with the
     * specified detail message and details for placeholders
     * 
     * @param msg
     * @param args 
     */
    public AccessDeniedException(String msg, Object... args) {        
        super(String.format(msg, fixArgs(args)));
    }
    
    /**
     * 
     * @param args
     * @return 
     */
    public static Object[] fixArgs(Object[] args) {
        for(int i=0;i<args.length;i++) {
            if(args[i] instanceof Utente u) {
                args[i] = u.getUtente().getNomeUtente();
            }                     
        }
        return args;
    }
}
