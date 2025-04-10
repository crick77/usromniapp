/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.usromniapp.model.Utente;

/**
 *
 * @author riccardo.iovenitti
 */
public abstract class OmniappBaseController extends BaseController {
    public Utente getUtenteOmniapp() {
        return (Utente)getUtente().getAttributes();
    }
}
