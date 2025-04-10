/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.converter;

import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author riccardo.iovenitti
 */
@FacesConverter("utentiRecordConverter")
public class UtentiRecordConverter implements Converter<UtentiRecord> {  
    @Inject
    UtenteService us;
    
    @Override
    public UtentiRecord getAsObject(FacesContext fc, UIComponent uic, String string) {
        return (string!=null) ? us.getUtente(Integer.parseInt(string)) : null;        
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, UtentiRecord u) {
        return (u!=null) ? String.valueOf(u.getIdUtente()) : null;
    }    
}
