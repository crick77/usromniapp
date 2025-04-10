/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author riccardo.iovenitti
 */
public class ProcAssegnata implements Serializable {
    private Integer idProc;
    private Integer idTipoProc;
    private LocalDateTime dataora;
    private BigDecimal lat;
    private BigDecimal lon;
    private String codice;
    private String richiedente;
    private String indirizzo;
    private String codiceCom;
    private String descrizione;
    private String note;
    private Integer idProcIterUltimo;
    private String assegnante;
    private String assegnatari;
    private String comune;

    public ProcAssegnata() {
    }

    public ProcAssegnata(Integer idProc, Integer idTipoProc, LocalDateTime dataora, BigDecimal lat, BigDecimal lon, String codice, String richiedente, String indirizzo, String codiceCom, String descrizione, String note, Integer idProcIterUltimo, String assegnante, String utenti, String comune) {
        this.idProc = idProc;
        this.idTipoProc = idTipoProc;
        this.dataora = dataora;
        this.lat = lat;
        this.lon = lon;
        this.codice = codice;
        this.richiedente = richiedente;
        this.indirizzo = indirizzo;
        this.codiceCom = codiceCom;
        this.descrizione = descrizione;
        this.note = note;
        this.idProcIterUltimo = idProcIterUltimo;
        this.assegnante = assegnante;
        this.assegnatari = utenti;
        this.comune = comune;
    }
        
    public String getComune() {
        return comune;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }
       
    public Integer getIdProc() {
        return idProc;
    }

    public void setIdProc(Integer idProc) {
        this.idProc = idProc;
    }

    public Integer getIdTipoProc() {
        return idTipoProc;
    }

    public void setIdTipoProc(Integer idTipoProc) {
        this.idTipoProc = idTipoProc;
    }

    public LocalDateTime getDataora() {
        return dataora;
    }

    public void setDataora(LocalDateTime dataora) {
        this.dataora = dataora;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getRichiedente() {
        return richiedente;
    }

    public void setRichiedente(String richiedente) {
        this.richiedente = richiedente;
    }

    public String getCodiceCom() {
        return codiceCom;
    }

    public void setCodiceCom(String codiceCom) {
        this.codiceCom = codiceCom;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getIdProcIterUltimo() {
        return idProcIterUltimo;
    }

    public void setIdProcIterUltimo(Integer idProcIterUltimo) {
        this.idProcIterUltimo = idProcIterUltimo;
    }

    public String getAssegnante() {
        return assegnante;
    }

    public void setAssegnante(String assegnante) {
        this.assegnante = assegnante;
    }

    public String getAssegnatari() {
        return assegnatari;
    }

    public void setAssegnatari(String assegnatari) {
        this.assegnatari = assegnatari;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }   
}
