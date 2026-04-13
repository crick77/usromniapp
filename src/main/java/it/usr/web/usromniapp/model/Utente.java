/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import it.usr.web.usromniapp.domain.tables.records.UfficiRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import java.util.List;
import java.util.Map;

/**
 *
 * @author riccardo.iovenitti
 */
public class Utente {
    private final UtentiRecord utente;
    private final List<UfficiRecord> uffici;
    private final Map<Integer, Integer> deleghe;
    private final Map<Integer, List<Integer>> delegheGerarchiche;

    public Utente(UtentiRecord utente, List<UfficiRecord> uffici, Map<Integer, Integer> deleghe, Map<Integer, List<Integer>> delegheGerarchiche) {
        this.utente = utente;
        this.uffici = uffici;
        this.deleghe = deleghe;
        this.delegheGerarchiche = delegheGerarchiche;
    }

    public UtentiRecord getUtente() {
        return utente;
    }

    public List<UfficiRecord> getUffici() {
        return uffici;
    }

    public Map<Integer, Integer> getDeleghe() {
        return deleghe;
    }

    public Map<Integer, List<Integer>> getDelegheGerarchiche() {
        return delegheGerarchiche;
    }

    public String getGender() {
        return "M";
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Utente{");
        sb.append("utente=").append(utente);
        sb.append(", uffici=").append(uffici);
        sb.append(", deleghe=").append(deleghe);
        sb.append(", delegheGerarchiche=").append(delegheGerarchiche);
        sb.append('}');
        return sb.toString();
    }   
}
