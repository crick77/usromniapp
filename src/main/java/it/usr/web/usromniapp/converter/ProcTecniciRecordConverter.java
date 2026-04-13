/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.converter;

import it.usr.web.usromniapp.domain.tables.records.ProcTecniciRecord;
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
@FacesConverter("procTecniciRecordConverter")
public class ProcTecniciRecordConverter implements Converter<ProcTecniciRecord> {
    @Inject
    ProcedimentoService ps; 
    
    @Override
    public ProcTecniciRecord getAsObject(FacesContext context, UIComponent component, String value) {
        return (value!=null) ? ps.getProcTecnicoById(Integer.parseInt(value)) : null; 
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProcTecniciRecord t) {
        return (t!=null) ? String.valueOf(t.getIdTecnico()) : null;
    }    
}
