/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.converter;

import it.usr.web.usromniapp.model.UtenteRuolo;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@FacesConverter("utenteRuoloConverter")
public class UtenteRuoloConverter implements Converter<UtenteRuolo> {  
    private final static String ID_SEPARATOR = ",";
    @Inject
    UtenteService us;
    
    @Override
    public UtenteRuolo getAsObject(FacesContext fc, UIComponent uic, String string) {
        UtenteRuolo u = null;
        if(string!=null && string.contains(ID_SEPARATOR)) {
            String[] parts = string.split(ID_SEPARATOR);
            int idRU = Integer.parseInt(parts[0]);
            int idUff = Integer.parseInt(parts[1]);
            u = us.getUtenteRuoloUfficio(idRU, idUff);
        }
        
        return u;
    }

    @Override 
    public String getAsString(FacesContext fc, UIComponent uic, UtenteRuolo u) {
        return (u!=null) ? String.valueOf(u.getIdRuoliUtente())+ID_SEPARATOR+String.valueOf(u.getIdUfficio()) : null;
    }    
}
