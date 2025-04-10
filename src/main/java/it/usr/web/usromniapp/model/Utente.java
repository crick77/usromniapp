/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import it.usr.web.usromniapp.domain.tables.records.UfficiRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;

/**
 *
 * @author riccardo.iovenitti
 */
public class Utente {
    private final UtentiRecord utente;
    private final UfficiRecord ufficio;

    public Utente(UtentiRecord utente, UfficiRecord ufficio) {
        this.utente = utente;
        this.ufficio = ufficio;
    }

    public UtentiRecord getUtente() {
        return utente;
    }

    public UfficiRecord getUfficio() {
        return ufficio;
    }
    
    public String getGender() {
        return "M";
    }
}
