/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.service;

import it.usr.web.service.BaseService;
import static it.usr.web.usromniapp.domain.Tables.*;
import it.usr.web.usromniapp.domain.tables.records.LTipoPassoRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcEsitiRecord;
import it.usr.web.usromniapp.domain.tables.records.ProcIterRecord;
import it.usr.web.usromniapp.interceptor.LogDatabaseOperation;
import it.usr.web.usromniapp.producer.DSLCtx;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class IterService extends BaseService {
    @DSLCtx
    @Inject
    DSLContext ctx;

    /*public List<ProcIterRecord> getIterByIdProcAndUser(int idProc, Utente user) {
        return ctx.selectFrom(PROC_ITER).where(PROC_ITER.ID_PROC.eq(idProc).and(PROC_ITER.ID_UTENTE.eq(user.getUtente().getIdUtente()))).orderBy(PROC_ITER.DATA_PROT.desc()).fetch();
    }*/

    public List<ProcIterRecord> getIterAttiviByIdProc(int idProc) {
        return ctx.selectFrom(PROC_ITER).where(PROC_ITER.ID_PROC.eq(idProc).and(PROC_ITER.ATTIVO.eq(1))).orderBy(PROC_ITER.DATA_PROT.desc(), PROC_ITER.PROT.desc()).fetch();
    } 
  
    public List<ProcIterRecord> getSequenzaIter(int idProcIter) {
        String sql = """ 
                    WITH RECURSIVE cte AS (
                        SELECT * FROM proc_iter WHERE id_proc_iter = {0}
                    UNION ALL
                        SELECT proc_iter.* FROM proc_iter INNER JOIN cte ON cte.id_proc_iter_link = proc_iter.id_proc_iter
                    )
                    SELECT * FROM cte WHERE id_proc_iter<>{0} ORDER BY data_prot DESC
                     """;
        return ctx.fetch(sql, idProcIter).into(ProcIterRecord.class);                                                            
    }
    
    public Map<Integer, LTipoPassoRecord> getTipiPasso(int idTipoProc) {
        return ctx.selectFrom(L_TIPO_PASSO).where(L_TIPO_PASSO.ID_TIPO_PROC.eq(idTipoProc)).fetchMap(L_TIPO_PASSO.CODICE_PASSO);
    }

    public List<LTipoPassoRecord> getTipiPasso(int idTipoProc, Integer ultimoPasso) {
        Condition cond = DSL.noCondition();
        if (ultimoPasso == null) {
            cond = cond.and(L_TIPO_PASSO.CODICE_PASSO.in(DSL.select(PROC_DIPENDENZE.CODICE_PASSO_SUCC).from(PROC_DIPENDENZE).where(PROC_DIPENDENZE.CODICE_PASSO.isNull()).and(PROC_DIPENDENZE.ID_TIPO_PROC.eq(idTipoProc))));
        } else {
            cond = cond.and(L_TIPO_PASSO.CODICE_PASSO.in(DSL.select(PROC_DIPENDENZE.CODICE_PASSO_SUCC).from(PROC_DIPENDENZE).where(PROC_DIPENDENZE.CODICE_PASSO.eq(ultimoPasso)).and(PROC_DIPENDENZE.ID_TIPO_PROC.eq(idTipoProc))));
        }
        return ctx.selectFrom(L_TIPO_PASSO).where(L_TIPO_PASSO.ID_TIPO_PROC.eq(idTipoProc)).and(L_TIPO_PASSO.VISIBILE.eq(1)).and(cond).fetch();
    }

    public LTipoPassoRecord getPassoByCodice(int codicePasso) {
        return ctx.selectFrom(L_TIPO_PASSO).where(L_TIPO_PASSO.CODICE_PASSO.eq(codicePasso)).fetchSingle();
    } 

    public List<ProcEsitiRecord> getEsiti(int idTipoProc) {
        return ctx.selectFrom(PROC_ESITI).where(PROC_ESITI.ID_TIPO_PROC.eq(idTipoProc).and(PROC_ESITI.VISIBILE.eq(1))).fetch();
    }
    
    @LogDatabaseOperation
    public void inserisci(ProcIterRecord iter) throws DatabaseException {
        try {
            ctx.transaction(trx -> {
                LTipoPassoRecord tipoPasso = trx.dsl().selectFrom(L_TIPO_PASSO).where(L_TIPO_PASSO.CODICE_PASSO.eq(iter.getCodicePasso())).fetchSingle();
                                                
                trx.dsl().insertInto(PROC_ITER).set(iter).execute();
                int lastId = trx.dsl().lastID().intValue();
                
                // Use-Case passo 4.d
                if(tipoPasso.getInfluenzaStato()==1) {
                    trx.dsl().update(PROC).set(PROC.ID_PROC_ITER_ULTIMO, lastId).where(PROC.ID_PROC.eq(iter.getIdProc())).execute();                    
                    
                    // Use-case passo 4.d.i
                    if(tipoPasso.getPassoUfficio()) {
                        trx.dsl().update(PROC).setNull(PROC.ID_PROC_ITER_FIRMA).set(PROC.ID_PROC_ITER_ULTIMO_UFFICIO, lastId).where(PROC.ID_PROC.eq(iter.getIdProc())).execute();
                    }
                }
                
                // Use-case passo 4.e
                if(tipoPasso.getPassoFirma()) {
                    trx.dsl().update(PROC).set(PROC.ID_PROC_ITER_FIRMA, lastId).where(PROC.ID_PROC.eq(iter.getIdProc())).execute();
                }           
                
                // Use-case passo 4.f.ii
                if(tipoPasso.getEsitoObbligatorio()) {
                    trx.dsl().update(PROC).set(PROC.ID_PROC_ITER_ESITO, lastId).where(PROC.ID_PROC.eq(iter.getIdProc())).execute();
                }
            });           
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }
    }
    
    @LogDatabaseOperation
    public void modifica(ProcIterRecord iter) throws DatabaseException {
        try {
            ctx.transaction(trx -> {
                // duplica record "modificato", sarà il nuovo
                ProcIterRecord newIter = iter.copy();
                // elimina l'id per forzare l'inserimento e collega al precedente (modificato)
                newIter.setIdProcIter(null);
                newIter.setIdProcIterLink(iter.getIdProcIter());                
                // inserisci il nuovo iter e recuper l'id
                trx.dsl().insertInto(PROC_ITER).set(newIter).execute();
                int idProcIter = trx.dsl().lastID().intValue();
                // aggiorna il link di collegamento all'iter oggetto di modifica e disabilitalo
                int numUpd = trx.dsl().update(PROC_ITER).set(PROC_ITER.ATTIVO, 0).where(PROC_ITER.ID_PROC_ITER.eq(iter.getIdProcIter())).execute();
                if(numUpd!=1) throw new DatabaseException("Numero di record aggiornati non valido ("+numUpd+"), attesi 1, PROC_ITER id=["+iter.getIdProcIter()+"].");
                
                // il passo prevede l'aggiornamento dell'ultimo stato nella procedura?
                LTipoPassoRecord passo = trx.dsl().selectFrom(L_TIPO_PASSO).where(L_TIPO_PASSO.CODICE_PASSO.eq(iter.getCodicePasso())).fetchSingle();
                if(passo.getInfluenzaStato()==1) {
                    // si, effettua l'aggiornamento
                    trx.dsl().update(PROC).set(PROC.ID_PROC_ITER_ULTIMO, idProcIter).where(PROC.ID_PROC.eq(iter.getIdProc())).execute();
                }
            });            
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw new DatabaseException(dae);
        }
    }
}
 