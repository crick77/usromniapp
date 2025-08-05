/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.DelegaRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcAssRecord;
import it.usr.web.usromniapp.domain.tables.records.UtentiRecord;
import it.usr.web.usromniapp.model.TipoProcAssModel;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;
import org.slf4j.Logger;

/**
 * 
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class TipiProcAuthController extends BaseController {
    @Inject
    ProcedimentoService ps;
    @Inject
    UtenteService us;
    @Inject
    @AppLogger
    Logger logger;
    List<LTipoProcRecord> tipiProcedimento;
    LTipoProcRecord tipoProcedimentoSelezionato;
    List<TipoProcAssModel> tipiProcAss;
    List<DelegaRecord> deleghe;
    Map<Integer, UtentiRecord> mUtenti;
     
    public void init() {
        mUtenti = us.getUtenti().stream().collect(Collectors.toMap(UtentiRecord::getIdUtente, Function.identity()));
        tipiProcedimento = ps.getTipiProcedimento();
        deleghe = null;
        
        tipoProcedimentoSelezionato = null;        
    }

    public List<LTipoProcRecord> getTipiProcedimento() {
        return tipiProcedimento;
    }

    public List<TipoProcAssModel> getTipiProcAss() {
        return tipiProcAss;
    }
        
    public LTipoProcRecord getTipoProcedimentoSelezionato() {
        return tipoProcedimentoSelezionato;
    }

    public List<DelegaRecord> getDeleghe() {
        return deleghe;
    }

    public void setDeleghe(List<DelegaRecord> deleghe) {
        this.deleghe = deleghe;
    }
    
    public void setTipoProcedimentoSelezionato(LTipoProcRecord tipoProcedimentoSelezionato) {
        this.tipoProcedimentoSelezionato = tipoProcedimentoSelezionato;
    }
    
    public UtentiRecord decodeUtente(int idUtente) {
        return mUtenti.get(idUtente);
    }
    
    public void aggiornaProcAss() {
        tipiProcAss = ps.getTipiProcAssModel(tipoProcedimentoSelezionato.getIdTipoProc());
    }    

    public void caricaDeleghe(TipoProcAssModel pa) {
        if(pa.getIdUtente()!=null) {
            deleghe = ps.getDeleghe(tipoProcedimentoSelezionato.getIdTipoProc(), pa.getIdUtente());
        }
        else {
            deleghe = null;
        }
    }        
}
 