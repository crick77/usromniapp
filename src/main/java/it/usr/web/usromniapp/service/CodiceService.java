/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;

import static it.usr.web.usromniapp.domain.Tables.*;
import it.usr.web.usromniapp.domain.tables.records.GisCentroidiRecord;
import it.usr.web.usromniapp.domain.tables.records.TipoProcProgressivoRecord;
import it.usr.web.usromniapp.producer.DSLCtx;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import org.jooq.Configuration;
import org.jooq.DSLContext;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
public class CodiceService {    
    @Inject
    @DSLCtx
    DSLContext ctx;
    
    public String generaCodiceVLO(Configuration trx, int idTipoProc) throws DatabaseException {            
        TipoProcProgressivoRecord prog = trx.dsl().selectFrom(TIPO_PROC_PROGRESSIVO).where(TIPO_PROC_PROGRESSIVO.ID_TIPO_PROC.eq(idTipoProc)).fetchOne();
        if(prog==null) {
            throw new DatabaseException("Non riesco a trovare il progessivo per la TIPO_PROC = "+idTipoProc);
        }
        else {
            int current = prog.getProgressivo();
            prog.setProgressivo(current+1);
            trx.dsl().update(TIPO_PROC_PROGRESSIVO).set(prog).execute();
            return String.format(prog.getFormato(), current);
        }                
    }
    
    public List<GisCentroidiRecord> getCentroidi() {
        return ctx.selectFrom(GIS_CENTROIDI).orderBy(GIS_CENTROIDI.COMUNE).fetch();
    }
    
    public GisCentroidiRecord getCentroide(int codiceCom) {
        return ctx.selectFrom(GIS_CENTROIDI).where(GIS_CENTROIDI.CODICE_COM.eq(codiceCom)).fetchSingle();
    }
}
