/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.controller;

import static it.usr.web.controller.BaseController.SAME_VIEW;
import it.usr.web.controller.Redirector;
import it.usr.web.producer.AppLogger;
import it.usr.web.usromniapp.domain.tables.records.LTipoPassoRecord;
import it.usr.web.usromniapp.domain.tables.records.LTipoProcRecord;
import it.usr.web.usromniapp.interceptor.RequiredAuthorization;
import it.usr.web.usromniapp.interceptor.SecurityCheck;
import it.usr.web.usromniapp.model.StatoDiFatto;
import it.usr.web.usromniapp.service.CodiceService;
import it.usr.web.usromniapp.service.ProcedimentoService;
import it.usr.web.usromniapp.service.SecurityService;
import it.usr.web.usromniapp.service.TipoOperazioneEnum;
import it.usr.web.usromniapp.service.UtenteService;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@ViewScoped
@Named
public class StatoDiFattoController extends OmniappBaseController {
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ProcedimentoService ps;
    @Inject
    SecurityService ss;
    @Inject
    CodiceService cs;
    @Inject
    UtenteService us;
    List<StatoDiFatto> procedimenti;
    List<LTipoProcRecord> tipiProcedimento;
    LTipoProcRecord tipoProcedimentoSelezionato;
    List<StatoDiFatto> procedimentiFiltrati;
    Map<Integer, LTipoProcRecord> mTipiProc;
    Map<Integer, LTipoPassoRecord> mTipiPasso;
    
    @SecurityCheck
    @RequiredAuthorization(TipoOperazioneEnum.M)
    public String init() {     
        mTipiProc = ps.getTipiProcedimentoMap();
        mTipiPasso = cs.getTipiPassoMap();
        tipiProcedimento = ps.getTipiProcedureAutorizzate(getUtenteOmniapp(), new TipoOperazioneEnum[] { TipoOperazioneEnum.M });
        tipoProcedimentoSelezionato = null;
        procedimenti = null;
        procedimentiFiltrati = null;
                         
        return SAME_VIEW;
    }

    public List<StatoDiFatto> getProcedimenti() {
        return procedimenti;
    }

    public List<LTipoProcRecord> getTipiProcedimento() {
        return tipiProcedimento;
    }

    public LTipoProcRecord getTipoProcedimentoSelezionato() {
        return tipoProcedimentoSelezionato;
    }

    public void setTipoProcedimentoSelezionato(LTipoProcRecord tipoProcedimentoSelezionato) {
        this.tipoProcedimentoSelezionato = tipoProcedimentoSelezionato;
    }
 
    public List<StatoDiFatto> getProcedimentiFiltrati() {
        return procedimentiFiltrati;
    }

    public void setProcedimentiFiltrati(List<StatoDiFatto> procedimentiFiltrati) {
        this.procedimentiFiltrati = procedimentiFiltrati;
    }
           
    public Collection<LTipoPassoRecord> getTipiPasso() {
        return mTipiPasso.values();
    } 
    
    public void aggiornaProcedimenti() {
        procedimenti = ps.getStatoDiFatto(getUtenteOmniapp(), tipoProcedimentoSelezionato.getIdTipoProc());
        procedimentiFiltrati = null;
    }
    
    public LTipoProcRecord decodificaProcedimento(int idTipoProc) {
        return mTipiProc.get(idTipoProc);
    }
    
    public LTipoPassoRecord decodificaPasso(int codicePasso) {
        return mTipiPasso.get(codicePasso);
    }
     
    public String[] splitAssegnatari(String ass) {
        return (ass!=null) ? ass.split("\\|") : new String[0];
    } 
    
    public String mostraIter(StatoDiFatto proc) {
        return Redirector.toPath("iter").withParam("idproc", proc.getIdProc()).withParam("back", FacesContext.getCurrentInstance().getViewRoot().getViewId()).withRedirect().go();
    }
}
