/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ApplicationScoped
public class VersionController {
    
    public String getCurrentVersion() {
        return "1.0-DEVELOP";
    }
    
    public String getAppName() {
        return "USROMNIAPP";
    }
}
