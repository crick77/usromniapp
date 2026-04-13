/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usromniapp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author riccardo.iovenitti
 */
public class StatoDiFatto extends ProcAssegnata {
    String codicePasso;
    LocalDate dataProt;
    String stato;

    public StatoDiFatto() {
        super();
    }
            
    public StatoDiFatto(Integer idProc, Integer idTipoProc, LocalDateTime dataora, BigDecimal lat, BigDecimal lon, String codice, String richiedente, String indirizzo, String codiceCom, String descrizione, String note, Integer idProcIterUltimo, String assegnante, String utenti, String comune, String codicePasso, LocalDate dataProt, String stato) {
        super(idProc, idTipoProc, dataora, lat, lon, codice, richiedente, indirizzo, codiceCom, descrizione, note, idProcIterUltimo, assegnante, utenti, comune);
        this.codicePasso = codicePasso;
        this.dataProt = dataProt;
        this.stato = stato;                
    }

    public String getCodicePasso() {
        return codicePasso;
    }

    public void setCodicePasso(String codicePasso) {
        this.codicePasso = codicePasso;
    }

    public LocalDate getDataProt() {
        return dataProt;
    }

    public void setDataProt(LocalDate dataProt) {
        this.dataProt = dataProt;
    }  

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }        
}
