/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package it.usr.web.usromniapp.record;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author riccardo.iovenitti
 */
public class RProcIter {
    Integer idProcIter;
    Integer idProc;
    LocalDateTime dataora;
    Integer attivo;
    Integer modificabile;
    Integer idUtente;
    Integer codicePasso;
    Integer prot;
    LocalDate dataProt;
    String nMude;
    String annotazioni;
    Integer idEsito;
    Integer idProcIterLink;
    Integer giorniSospensione;

    public RProcIter(Integer idProcIter, Integer idProc, LocalDateTime dataora, Integer attivo, Integer modificabile, Integer idUtente, Integer codicePasso, Integer prot, LocalDate dataProt, String nMude, String annotazioni, Integer idEsito, Integer idProcIterLink, Integer giorniSospensione) {
        this.idProcIter = idProcIter;
        this.idProc = idProc;
        this.dataora = dataora;
        this.attivo = attivo;
        this.modificabile = modificabile;
        this.idUtente = idUtente;
        this.codicePasso = codicePasso;
        this.prot = prot;
        this.dataProt = dataProt;
        this.nMude = nMude;
        this.annotazioni = annotazioni;
        this.idEsito = idEsito;
        this.idProcIterLink = idProcIterLink;
        this.giorniSospensione = giorniSospensione;
    }

    public Integer getIdProcIter() {
        return idProcIter;
    }

    public void setIdProcIter(Integer idProcIter) {
        this.idProcIter = idProcIter;
    }

    public Integer getIdProc() {
        return idProc;
    }

    public void setIdProc(Integer idProc) {
        this.idProc = idProc;
    }

    public LocalDateTime getDataora() {
        return dataora;
    }

    public void setDataora(LocalDateTime dataora) {
        this.dataora = dataora;
    }

    public Integer getAttivo() {
        return attivo;
    }

    public void setAttivo(Integer attivo) {
        this.attivo = attivo;
    }

    public Integer getModificabile() {
        return modificabile;
    }

    public void setModificabile(Integer modificabile) {
        this.modificabile = modificabile;
    }

    public Integer getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(Integer idUtente) {
        this.idUtente = idUtente;
    }
  
    public Integer getCodicePasso() {
        return codicePasso;
    }

    public void setCodicePasso(Integer codicePasso) {
        this.codicePasso = codicePasso;
    }

    public Integer getProt() {
        return prot;
    }

    public void setProt(Integer prot) {
        this.prot = prot;
    }

    public LocalDate getDataProt() {
        return dataProt;
    }

    public void setDataProt(LocalDate dataProt) {
        this.dataProt = dataProt;
    }

    public String getnMude() {
        return nMude;
    }

    public void setnMude(String nMude) {
        this.nMude = nMude;
    }

    public String getAnnotazioni() {
        return annotazioni;
    }

    public void setAnnotazioni(String annotazioni) {
        this.annotazioni = annotazioni;
    }

    public Integer getIdEsito() {
        return idEsito;
    }

    public void setIdEsito(Integer idEsito) {
        this.idEsito = idEsito;
    }

    public Integer getIdProcIterLink() {
        return idProcIterLink;
    }

    public void setIdProcIterLink(Integer idProcIterLink) {
        this.idProcIterLink = idProcIterLink;
    }

    public Integer getGiorniSospensione() {
        return giorniSospensione;
    }

    public void setGiorniSospensione(Integer giorniSospensione) {
        this.giorniSospensione = giorniSospensione;
    }    
}
