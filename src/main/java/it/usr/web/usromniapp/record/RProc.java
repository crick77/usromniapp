/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package it.usr.web.usromniapp.record;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 *
 * @author riccardo.iovenitti
 */
public class RProc {
    Integer idProc;
    Integer idTipoProc;
    LocalDateTime dataOra;
    BigDecimal lat;
    BigDecimal lon;
    String codice;
    String richiedente;
    String indirizzo;
    Integer codiceCom;
    String descrizione;
    String note;
    Integer idProcIterUltimo;
    RProcIter iter;

    public RProc(Integer idProc, Integer idTipoProc, LocalDateTime dataOra, BigDecimal lat, BigDecimal lon, String codice, String richiedente, String indirizzo, Integer codiceCom, String descrizione, String note, Integer idProcIterUltimo, RProcIter iter) {
        this.idProc = idProc;
        this.idTipoProc = idTipoProc;
        this.dataOra = dataOra;
        this.lat = lat;
        this.lon = lon;
        this.codice = codice;
        this.richiedente = richiedente;
        this.indirizzo = indirizzo;
        this.codiceCom = codiceCom;
        this.descrizione = descrizione;
        this.note = note;
        this.idProcIterUltimo = idProcIterUltimo;
        this.iter = iter;
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

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
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

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public Integer getCodiceCom() {
        return codiceCom;
    }

    public void setCodiceCom(Integer codiceCom) {
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

    public RProcIter getIter() {
        return iter;
    }

    public void setIter(RProcIter iter) {
        this.iter = iter;
    }        
}
