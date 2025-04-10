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
public class ElencoVisibili implements Serializable {
    private final List<Integer> idTipiProc = new ArrayList<>();
    private final List<Integer> idProcVisibili = new ArrayList<>();
    private final List<Integer> idProcEsclusi = new ArrayList<>();

    public List<Integer> getIdTipiProc() {
        return idTipiProc;
    }

    public List<Integer> getIdProcVisibili() {
        return idProcVisibili;
    }

    public List<Integer> getIdProcEsclusi() {
        return idProcEsclusi;
    }   
}
