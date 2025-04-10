/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usromniapp.service;

/**
 *
 * @author riccardo.iovenitti
 */
public class NotEmptyException extends Exception {

    /**
     * Creates a new instance of <code>ImportException</code> without detail
     * message.
     */
    public NotEmptyException() {
    }

    /**
     * Constructs an instance of <code>ImportException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public NotEmptyException(String msg) {
        super(msg);
    }

    public NotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }        
}
