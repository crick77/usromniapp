/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author riccardo.iovenitti
 */
public class UtenteRuolo implements Serializable {
    private int idRuoliUtente;
    private int idUtente;
    private int idRuolo;
    private int idUfficio;
    private String nomeUtente;
    private String ruolo;
    private String ufficio;

    public UtenteRuolo() {
    }

    public int getIdRuoliUtente() {
        return idRuoliUtente;
    }

    public void setIdRuoliUtente(int idRuoliUtente) {
        this.idRuoliUtente = idRuoliUtente;
    }
        
    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public int getIdRuolo() {
        return idRuolo;
    }

    public void setIdRuolo(int idRuolo) {
        this.idRuolo = idRuolo;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    public void setNomeUtente(String nomeUtente) {
        this.nomeUtente = nomeUtente;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public int getIdUfficio() {
        return idUfficio;
    }

    public void setIdUfficio(int idUfficio) {
        this.idUfficio = idUfficio;
    }

    public String getUfficio() {
        return ufficio;
    }

    public void setUfficio(String ufficio) {
        this.ufficio = ufficio;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.idRuoliUtente;
        hash = 43 * hash + this.idUtente;
        hash = 43 * hash + this.idRuolo;
        hash = 43 * hash + this.idUfficio;
        hash = 43 * hash + Objects.hashCode(this.nomeUtente);
        hash = 43 * hash + Objects.hashCode(this.ruolo);
        hash = 43 * hash + Objects.hashCode(this.ufficio);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UtenteRuolo other = (UtenteRuolo) obj;
        if (this.idRuoliUtente != other.idRuoliUtente) {
            return false;
        }
        if (this.idUtente != other.idUtente) {
            return false;
        }
        if (this.idRuolo != other.idRuolo) {
            return false;
        }
        if (this.idUfficio != other.idUfficio) {
            return false;
        }
        if (!Objects.equals(this.nomeUtente, other.nomeUtente)) {
            return false;
        }
        if (!Objects.equals(this.ruolo, other.ruolo)) {
            return false;
        }
        return Objects.equals(this.ufficio, other.ufficio);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UtenteRuolo{");
        sb.append("idRuoliUtente=").append(idRuoliUtente);
        sb.append(", idUtente=").append(idUtente);
        sb.append(", idRuolo=").append(idRuolo);
        sb.append(", idUfficio=").append(idUfficio);
        sb.append(", nomeUtente=").append(nomeUtente);
        sb.append(", ruolo=").append(ruolo);
        sb.append(", ufficio=").append(ufficio);
        sb.append('}');
        return sb.toString();
    }   
}
