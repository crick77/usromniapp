/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import java.io.Serializable;

/**
 *
 * @author riccardo.iovenitti
 */
public class Carico implements Serializable {
    int totaleDaIstruire;
    int totaleIstruite;
    int totaleIntegrate;
    int totaleAllaFirma;
    int totaleConcluse;
    int totaleInRevisione;

    public Carico() {
        totaleAllaFirma = totaleConcluse = totaleDaIstruire = totaleIntegrate = totaleIstruite = 0;
    }

    public int getTotaleDaIstruire() {
        return totaleDaIstruire;
    }
             
    public int getTotaleIstruite() {
        return totaleIstruite;
    }

    public int getTotaleIntegrate() {
        return totaleIntegrate;
    }

    public int getTotaleAllaFirma() {
        return totaleAllaFirma;
    }

    public int getTotaleConcluse() {
        return totaleConcluse;
    }

    public int getTotaleInRevisione() {
        return totaleInRevisione;
    }

    public void incrementaTotaleDaIstruire() {
        this.totaleDaIstruire++;
    }

    public void incrementaTotaleIstruite() {
        this.totaleIstruite++;
    }

    public void incrementaTotaleIntegrate() {
        this.totaleIntegrate++;
    }

    public void incrementaTotaleAllaFirma() {
        this.totaleAllaFirma++;
    }

    public void incrementaTotaleConcluse() {
        this.totaleConcluse++;
    }

    public void incrementaTotaleInRevisione() {
        this.totaleInRevisione++;
    }
        
    public int getComplessivo() {
        return totaleDaIstruire+totaleIstruite+totaleIntegrate+totaleConcluse+totaleInRevisione;                
    }        
}
