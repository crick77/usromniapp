/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.converter;

import it.usr.web.usromniapp.domain.tables.records.GisCentroidiRecord;
import it.usr.web.usromniapp.service.CodiceService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author riccardo.iovenitti
 */
@FacesConverter("gisCentroidiConverter")
public class GisCentroidiConverter implements Converter<GisCentroidiRecord> {  
    @Inject
    CodiceService cs;
    
    @Override
    public GisCentroidiRecord getAsObject(FacesContext fc, UIComponent uic, String string) {
        return (string!=null) ? cs.getCentroide(Integer.parseInt(string)) : null;        
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, GisCentroidiRecord g) {
        return (g!=null) ? String.valueOf(g.getCodiceCom()) : null;
    }    
}
