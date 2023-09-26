package Entidades;

import java.io.Serializable;

public class Identificacion_fse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Number version;
    private String ambiente;
    private String tipoDte;
    private String numeroControl;
    private String codigoGeneracion;
    private Number tipoModelo;
    private Number tipoOperacion;
    private Number tipoContingencia;
    private String motivoContin;
    private String fecEmi;
    private String horEmi;
    private String tipoMoneda;

    public Identificacion_fse(Number version, String ambiente, String tipoDte, String numeroControl, String codigoGeneracion, Number tipoModelo, Number tipoOperacion, Number tipoContingencia, String motivoContin, String fecEmi, String horEmi, String tipoMoneda) {
        this.version = version;
        this.ambiente = ambiente;
        this.tipoDte = tipoDte;
        this.numeroControl = numeroControl;
        this.codigoGeneracion = codigoGeneracion;
        this.tipoModelo = tipoModelo;
        this.tipoOperacion = tipoOperacion;
        this.tipoContingencia = tipoContingencia;
        this.motivoContin = motivoContin;
        this.fecEmi = fecEmi;
        this.horEmi = horEmi;
        this.tipoMoneda = tipoMoneda;
    }

    public Identificacion_fse() {
    }

    public Number getVersion() {
        return version;
    }

    public void setVersion(Number version) {
        this.version = version;
    }

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public String getTipoDte() {
        return tipoDte;
    }

    public void setTipoDte(String tipoDte) {
        this.tipoDte = tipoDte;
    }

    public String getNumeroControl() {
        return numeroControl;
    }

    public void setNumeroControl(String numeroControl) {
        this.numeroControl = numeroControl;
    }

    public String getCodigoGeneracion() {
        return codigoGeneracion;
    }

    public void setCodigoGeneracion(String codigoGeneracion) {
        this.codigoGeneracion = codigoGeneracion;
    }

    public Number getTipoModelo() {
        return tipoModelo;
    }

    public void setTipoModelo(Number tipoModelo) {
        this.tipoModelo = tipoModelo;
    }

    public Number getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(Number tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public Number getTipoContingencia() {
        return tipoContingencia;
    }

    public void setTipoContingencia(Number tipoContingencia) {
        this.tipoContingencia = tipoContingencia;
    }

    public String getMotivoContin() {
        return motivoContin;
    }

    public void setMotivoContin(String motivoContin) {
        this.motivoContin = motivoContin;
    }

    public String getFecEmi() {
        return fecEmi;
    }

    public void setFecEmi(String fecEmi) {
        this.fecEmi = fecEmi;
    }

    public String getHorEmi() {
        return horEmi;
    }

    public void setHorEmi(String horEmi) {
        this.horEmi = horEmi;
    }

    public String getTipoMoneda() {
        return tipoMoneda;
    }

    public void setTipoMoneda(String tipoMoneda) {
        this.tipoMoneda = tipoMoneda;
    }

    @Override
    public String toString() {
        return "Identificacion_ccf{" + "version=" + version + ", ambiente=" + ambiente + ", tipoDte=" + tipoDte + ", numeroControl=" + numeroControl + ", codigoGeneracion=" + codigoGeneracion + ", tipoModelo=" + tipoModelo + ", tipoOperacion=" + tipoOperacion + ", tipoContingencia=" + tipoContingencia + ", motivoContin=" + motivoContin + ", fecEmi=" + fecEmi + ", horEmi=" + horEmi + ", tipoMoneda=" + tipoMoneda + '}';
    }
    
}
