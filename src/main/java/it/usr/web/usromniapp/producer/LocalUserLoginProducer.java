/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.producer;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;

/**
 *
 * @author riccardo.iovenitti
 */
public class LocalUserLoginProducer {
    @Resource(lookup = "usromniapp/allowLocalLogin")
    String localUserLogin;
    
    @Produces
    @LocalUserLogin
    public boolean getDocumentFolder() {
        return Boolean.parseBoolean(localUserLogin);
    }
}
 