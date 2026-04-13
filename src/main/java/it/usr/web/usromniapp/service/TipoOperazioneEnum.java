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
    X(8), // diniego
    L(4), // leggi
    M(2), // modifica
    A(1), // assegna    
    ANY(15), // tutto    
    ADMIN(16); 
    private final int operazione;

    private TipoOperazioneEnum(int operazione) {
        this.operazione = operazione;
    }

    public int getOperazione() {
        return this.operazione;
    }
 
    @Override
    public String toString() {
        return this.name()+"("+String.valueOf(this.operazione)+")";
    }
    
    /*public static int combina(TipoOperazioneEnum[] ops) {
        int out = 0;
        for(TipoOperazioneEnum op : ops) {
            out = out | op.operazione; 
        }
        return out;
    }*/
    
    public static int combina(TipoOperazioneEnum... ops) {
        int out = 0;
        for(TipoOperazioneEnum op : ops) {
            out = out | op.operazione; 
        }
        return out;
    }
}
