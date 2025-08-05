/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author riccardo.iovenitti
 */
public class TipoProcAssModel implements Serializable {
    int idTipoProcAss;
    Integer idUtente;
    Integer idRuolo;
    Integer idUfficio;
    Integer idUtenteAssegnante;
    String nomeUtente;
    String ruolo;
    String ufficio;
    String autorizzazioni;
    String assegnante;
    LocalDateTime dataAssegnazione;
    String note;
           
    public Integer getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(Integer idUtente) {
        this.idUtente = idUtente;
    }

    public Integer getIdRuolo() {
        return idRuolo;
    }

    public void setIdRuolo(Integer idRuolo) {
        this.idRuolo = idRuolo;
    }

    public Integer getIdUfficio() {
        return idUfficio;
    }

    public void setIdUfficio(Integer idUfficio) {
        this.idUfficio = idUfficio;
    }

    public Integer getIdUtenteAssegnante() {
        return idUtenteAssegnante;
    }

    public void setIdUtenteAssegnante(Integer idUtenteAssegnante) {
        this.idUtenteAssegnante = idUtenteAssegnante;
    }
        
    public int getIdTipoProcAss() {
        return idTipoProcAss;
    }

    public void setIdTipoProcAss(int idTipoProcAss) {
        this.idTipoProcAss = idTipoProcAss;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    public void setNomeUtente(String nomeUtente) {
        this.nomeUtente = nomeUtente;
    }

    public String getUfficio() {
        return ufficio;
    }

    public void setUfficio(String ufficio) {
        this.ufficio = ufficio;
    }

    public String getAutorizzazioni() {
        return autorizzazioni;
    }

    public void setAutorizzazioni(String autorizzazioni) {
        this.autorizzazioni = autorizzazioni;
    }

    public LocalDateTime getDataAssegnazione() {
        return dataAssegnazione;
    }

    public void setDataAssegnazione(LocalDateTime dataAssegnazione) {
        this.dataAssegnazione = dataAssegnazione;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }        

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public String getAssegnante() {
        return assegnante;
    }

    public void setAssegnante(String assegnante) {
        this.assegnante = assegnante;
    }        
}
