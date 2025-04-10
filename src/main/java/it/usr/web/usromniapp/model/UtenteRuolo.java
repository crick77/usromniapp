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
    private String nomeUtente;
    private String ruolo;

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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.idRuoliUtente;
        hash = 89 * hash + this.idUtente;
        hash = 89 * hash + this.idRuolo;
        hash = 89 * hash + Objects.hashCode(this.nomeUtente);
        hash = 89 * hash + Objects.hashCode(this.ruolo);
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
        if (!Objects.equals(this.nomeUtente, other.nomeUtente)) {
            return false;
        }
        return Objects.equals(this.ruolo, other.ruolo);
    }   

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UtenteRuolo{");
        sb.append("idRuoliUtente=").append(idRuoliUtente);
        sb.append(", idUtente=").append(idUtente);
        sb.append(", idRuolo=").append(idRuolo);
        sb.append(", nomeUtente=").append(nomeUtente);
        sb.append(", ruolo=").append(ruolo);
        sb.append('}');
        return sb.toString();
    }        
}
