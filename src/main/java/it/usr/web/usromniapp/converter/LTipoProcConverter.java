/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.converter;

import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.service.ProcedimentoService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author riccardo.iovenitti
 */
@FacesConverter("lTipoProcConverter")
public class LTipoProcConverter implements Converter<LTipoProcRecord> {  
    @Inject
    ProcedimentoService ps;
    
    @Override
    public LTipoProcRecord getAsObject(FacesContext fc, UIComponent uic, String string) {
        return (string!=null) ? ps.getTipoProcedimentoById(Integer.parseInt(string)) : null;        
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, LTipoProcRecord t) {
        return (t!=null) ? String.valueOf(t.getIdTipoProc()) : null;
    }    
}
