/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.service.ProcedimentoService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class NuovoProcedimentoController extends OmniappBaseController {
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ProcedimentoService ps;        
    List<LTipoProcRecord> tipiProcedimento;
    LTipoProcRecord tipoProcedimento;
    
    public void init() {
        tipiProcedimento = ps.getTipiProcedimento(getUtenteOmniapp());
        tipoProcedimento = null;
    }

    public List<LTipoProcRecord> getTipiProcedimento() {
        return tipiProcedimento;
    }        

    public LTipoProcRecord getTipoProcedimento() {
        return tipoProcedimento;
    }

    public void setTipoProcedimento(LTipoProcRecord tipoProcedimento) {
        this.tipoProcedimento = tipoProcedimento;
    }        
    
    public String apri() {
        return redirect(tipoProcedimento.getFunzione());
    }
}
