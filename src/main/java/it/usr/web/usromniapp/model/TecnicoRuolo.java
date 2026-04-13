/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import it.usr.web.usromniapp.domain.tables.records.LRuoloTecnicoRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcTecniciRecord;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author riccardo.iovenitti
 */
public class TecnicoRuolo implements Serializable {
    ProcTecniciRecord tecnico;
    LRuoloTecnicoRecord ruolo;

    public TecnicoRuolo() {
        tecnico = new ProcTecniciRecord();
        ruolo = new LRuoloTecnicoRecord();
    }
        
    public ProcTecniciRecord getTecnico() {
        return tecnico;
    }

    public void setTecnico(ProcTecniciRecord tecnico) {
        this.tecnico = tecnico;
    }

    public LRuoloTecnicoRecord getRuolo() {
        return ruolo;
    }

    public void setRuolo(LRuoloTecnicoRecord ruolo) {
        this.ruolo = ruolo;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.tecnico);
        hash = 83 * hash + Objects.hashCode(this.ruolo);
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
        final TecnicoRuolo other = (TecnicoRuolo) obj;
        if (!Objects.equals(this.tecnico, other.tecnico)) {
            return false;
        }
        return Objects.equals(this.ruolo, other.ruolo);
    }
        
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TecnicoRuolo{");
        sb.append("tecnico=").append(tecnico);
        sb.append(", ruolo=").append(ruolo);
        sb.append('}');
        return sb.toString();
    }        
}
