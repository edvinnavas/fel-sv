package Entidades;

import java.io.Serializable;

public class Motivo_contingencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fInicio;
    private String fFin;
    private String hInicio;
    private String hFin;
    private Number tipoContingencia;
    private String motivoContingencia;

    public Motivo_contingencia(String fInicio, String fFin, String hInicio, String hFin, Number tipoContingencia, String motivoContingencia) {
        this.fInicio = fInicio;
        this.fFin = fFin;
        this.hInicio = hInicio;
        this.hFin = hFin;
        this.tipoContingencia = tipoContingencia;
        this.motivoContingencia = motivoContingencia;
    }

    public Motivo_contingencia() {
    }

    public String getfInicio() {
        return fInicio;
    }

    public void setfInicio(String fInicio) {
        this.fInicio = fInicio;
    }

    public String getfFin() {
        return fFin;
    }

    public void setfFin(String fFin) {
        this.fFin = fFin;
    }

    public String gethInicio() {
        return hInicio;
    }

    public void sethInicio(String hInicio) {
        this.hInicio = hInicio;
    }

    public String gethFin() {
        return hFin;
    }

    public void sethFin(String hFin) {
        this.hFin = hFin;
    }

    public Number getTipoContingencia() {
        return tipoContingencia;
    }

    public void setTipoContingencia(Number tipoContingencia) {
        this.tipoContingencia = tipoContingencia;
    }

    public String getMotivoContingencia() {
        return motivoContingencia;
    }

    public void setMotivoContingencia(String motivoContingencia) {
        this.motivoContingencia = motivoContingencia;
    }

    @Override
    public String toString() {
        return "Motivo_contingencia{" + "fInicio=" + fInicio + ", fFin=" + fFin + ", hInicio=" + hInicio + ", hFin=" + hFin + ", tipoContingencia=" + tipoContingencia + ", motivoContingencia=" + motivoContingencia + '}';
    }
    
}
