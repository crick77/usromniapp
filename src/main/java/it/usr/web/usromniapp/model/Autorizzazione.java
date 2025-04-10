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
public class Autorizzazione implements Serializable {
    private Integer idTipoProc;
    private Integer idProc;
    private Integer idTipoProcRif;
    private Integer idUtente;    
    private Integer IdUfficio;
    private String autorizzazioni;

    public Autorizzazione() {
    }

    public Autorizzazione(Integer idTipoProc, Integer idProc, Integer idTipoProcRif, Integer idUtente, Integer IdUfficio, String autorizzazioni) {
        this.idTipoProc = idTipoProc;
        this.idProc = idProc;
        this.idTipoProcRif = idTipoProcRif;
        this.idUtente = idUtente;
        this.IdUfficio = IdUfficio;
        this.autorizzazioni = autorizzazioni;
    }

    public Integer getIdTipoProc() {
        return idTipoProc;
    }

    public void setIdTipoProc(Integer idTipoProc) {
        this.idTipoProc = idTipoProc;
    }

    public Integer getIdProc() {
        return idProc;
    }

    public void setIdProc(Integer idProc) {
        this.idProc = idProc;
    }

    public Integer getIdTipoProcRif() {
        return idTipoProcRif;
    }

    public void setIdTipoProcRif(Integer idTipoProcRif) {
        this.idTipoProcRif = idTipoProcRif;
    }

    public Integer getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(Integer idUtente) {
        this.idUtente = idUtente;
    }

    public Integer getIdUfficio() {
        return IdUfficio;
    }

    public void setIdUfficio(Integer IdUfficio) {
        this.IdUfficio = IdUfficio;
    }

    public String getAutorizzazioni() {
        return autorizzazioni;
    }

    public void setAutorizzazioni(String autorizzazioni) {
        this.autorizzazioni = autorizzazioni;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Autorizzazione{");
        sb.append("idTipoProc=").append(idTipoProc);
        sb.append(", idProc=").append(idProc);
        sb.append(", idTipoProcRif=").append(idTipoProcRif);
        sb.append(", idUtente=").append(idUtente);
        sb.append(", IdUfficio=").append(IdUfficio);
        sb.append(", autorizzazioni=").append(autorizzazioni);
        sb.append('}');
        return sb.toString();
    }        
}
