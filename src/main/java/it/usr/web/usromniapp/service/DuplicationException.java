/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;

/**
 *
 * @author riccardo.iovenitti
 */
public class DuplicationException extends DatabaseException {
    public DuplicationException(String msg) {
        super(msg);
    }

    public DuplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicationException(Throwable cause) {
        super(cause);
    }    
}
