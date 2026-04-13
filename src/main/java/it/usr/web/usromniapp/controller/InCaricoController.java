/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import it.usr.web.controller.Redirector;
import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.GisCentroidiRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoPassoRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcEsitiRecord;
import it.usr.web.usromniapp.interceptor.RequiredAuthorization;
import it.usr.web.usromniapp.interceptor.SecurityCheck;
import it.usr.web.usromniapp.record.RProc;
import it.usr.web.usromniapp.service.CodiceService;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.SecurityService;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class InCaricoController extends OmniappBaseController {
    final static String[] TIPO_CARICO = {"DA ISTRUIRE", "ISTRUITI", "INTEGRATI", "ALLA FIRMA", "CONCLUSI", "IN REVISIONE"};
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ProcedimentoService ps;
    @Inject
    SecurityService ss;
    @Inject
    CodiceService cs;
    List<RProc> assegnazioni;
    List<RProc> assegnazioniFiltrate;
    RProc assegnazioneSelezionata;
    int tipo;
    List<LTipoProcRecord> tipiProcedimento;
    LTipoProcRecord tipoProcedimentoSelezionato;
    Map<Integer, LTipoProcRecord> mTipiProc;
    Map<Integer, LTipoPassoRecord> mTipiPasso;
    Map<Integer, ProcEsitiRecord> mEsiti;
    Map<Integer, GisCentroidiRecord> mCentroidi;
    
    @SecurityCheck
    @RequiredAuthorization(TipoOperazioneEnum.M)
    public String init() {
        tipiProcedimento = ps.getTipiProcedureAutorizzate(getUtenteOmniapp(), new TipoOperazioneEnum[] { TipoOperazioneEnum.M });                
        mTipiProc = ps.getTipiProcedimentoMap();        
        mEsiti = cs.getProcEsitiMap();
        mCentroidi = cs.getCentroidiMap();
        tipoProcedimentoSelezionato = null;
         
        aggiornaProcedimenti();
        
        return SAME_VIEW;
    } 

    public LTipoProcRecord getTipoProcedimentoSelezionato() {
        return tipoProcedimentoSelezionato;
    }

    public void setTipoProcedimentoSelezionato(LTipoProcRecord tipoProcedimentoSelezionato) {
        this.tipoProcedimentoSelezionato = tipoProcedimentoSelezionato;
    }
        
    public List<LTipoProcRecord> getTipiProcedimento() {
        return this.tipiProcedimento;
    }
    
    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
        
    public List<RProc> getAssegnazioniFiltrate() {
        return assegnazioniFiltrate;
    }

    public void setAssegnazioniFiltrate(List<RProc> assegnazioniFiltrate) {
        this.assegnazioniFiltrate = assegnazioniFiltrate;
    }
         
    public List<RProc> getAssegnazioni() {
        return assegnazioni;
    }
    
    public Map<Integer, LTipoProcRecord> getmTipiProc() {
        return mTipiProc;
    }
       
    public Map<Integer, LTipoPassoRecord> getmTipiPasso() { 
        return mTipiPasso;
    }

    public Collection<LTipoPassoRecord> getTipiPasso() {
        return mTipiPasso.values();
    }
    
    public Map<Integer, ProcEsitiRecord> getmEsiti() {
        return mEsiti;
    } 
       
    public Collection<ProcEsitiRecord> getEsiti() {
        return mEsiti.values();
    }
    
    public RProc getAssegnazioneSelezionata() {
        return assegnazioneSelezionata;
    }

    public void setAssegnazioneSelezionata(RProc assegnazioneSelezionata) {
        this.assegnazioneSelezionata = assegnazioneSelezionata;
    }
        
    public LTipoProcRecord decodificatProcedimento(int idTipoProc) {
        return mTipiProc.get(idTipoProc);
    }
    
    public LTipoPassoRecord decodificaPasso(int codicePasso) {
        return mTipiPasso.get(codicePasso);
    }
    
    public ProcEsitiRecord decodificaEsito(int idProcEsiti) {
        return mEsiti.get(idProcEsiti);
    }
    
    public GisCentroidiRecord decodificaCentroide(int codiceCom) {
        return mCentroidi.get(codiceCom);
    }
    
    public String mostraIter(RProc proc) {
        return Redirector.toPath("iter").withParam("idproc", proc.getIdProc()).withParam("back", FacesContext.getCurrentInstance().getViewRoot().getViewId()).withRedirect().go();
    }
    
    public String getDescrizione() {
        return TIPO_CARICO[tipo];
    }

    public void aggiornaProcedimenti() {
        //assegnazioni = ps.getAssegnazioni(getUtenteOmniapp(), true);        
        assegnazioneSelezionata = null; 
        assegnazioniFiltrate = null;
        if(tipoProcedimentoSelezionato!=null) {
            assegnazioni = ps.getPraticheInCarico(getUtenteOmniapp(), tipoProcedimentoSelezionato.getIdTipoProc(), new TipoOperazioneEnum[] { TipoOperazioneEnum.L, TipoOperazioneEnum.M }, tipo);             
            mTipiPasso = cs.getTipiPassoMap(tipoProcedimentoSelezionato);
        }
        else {
            assegnazioni = new LinkedList<>();
            mTipiPasso = new HashMap<>();
        }
    }
}
   