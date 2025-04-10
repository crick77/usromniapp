/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usromniapp.service;

/**
 *
 * @author riccardo.iovenitti
 */
public class DatabaseException extends Exception {
    /**
     * Creates a new instance of <code>DatabaseException</code> without detail
     * message.
     */
        /**
     * Constructs an instance of <code>DatabaseException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DatabaseException(String msg) {
        super(msg);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }        
}
