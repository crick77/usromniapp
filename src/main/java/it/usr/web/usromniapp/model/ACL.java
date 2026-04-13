/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author riccardo.iovenitti
 */
public class ACL implements Serializable {
    private final List<Integer> tipiProcedureAmmesse = new ArrayList<>();
    private final List<Integer> tipiProcedureEscluse = new ArrayList<>();
    private final List<Integer> procedureAmmesse = new ArrayList<>();
    private final List<Integer> procedureEscluse = new ArrayList<>();

    public List<Integer> getTipiProcedureAmmesse() {
        return tipiProcedureAmmesse;
    }

    public List<Integer> getProcedureAmmesse() {
        return procedureAmmesse;
    }

    public List<Integer> getProcedureEscluse() {
        return procedureEscluse;
    }   

    public List<Integer> getTipiProcedureEscluse() {
        return tipiProcedureEscluse;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ACL{");
        sb.append("tipiProcedureAmmesse=").append(tipiProcedureAmmesse);
        sb.append(", tipiProcedureEscluse=").append(tipiProcedureEscluse);
        sb.append(", procedureAmmesse=").append(procedureAmmesse);
        sb.append(", procedureEscluse=").append(procedureEscluse);
        sb.append('}');
        return sb.toString();
    }        
}
