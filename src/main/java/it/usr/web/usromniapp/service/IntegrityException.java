/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usromniapp.service;

/**
 *
 * @author riccardo.iovenitti
 */
public class IntegrityException extends DatabaseException {
    /**
     * Constructs an instance of <code>IntegrityException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IntegrityException(String msg) {
        super(msg);
    }

    public IntegrityException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegrityException(Throwable cause) {
        super(cause);
    }        
}
