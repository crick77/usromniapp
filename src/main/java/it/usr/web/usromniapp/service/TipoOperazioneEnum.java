/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package it.usr.web.usromniapp.service;

/**
 *
 * @author riccardo.iovenitti
 */
public enum TipoOperazioneEnum {    
    M("M"), // modifica
    E("E"), // elimina
    A("A"), // assegna
    L("L"), // leggi
    ANY("%"); // tutto
    
    private final String operazione;

    private TipoOperazioneEnum(String operazione) {
        this.operazione = operazione;
    }

    public String getOperazione() {
        return this.operazione;
    }
 
    @Override
    public String toString() {
        return this.operazione;
    }
}
