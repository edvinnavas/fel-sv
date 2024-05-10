package Entidades;

import java.io.Serializable;

public class Respuesta_INFILE implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String codigoGeneracion;
    private String status;
    private String numeroControl;
    private String identificador;
    private String fechaEmision;
    private String selloRecepcion;

    public Respuesta_INFILE(String codigoGeneracion, String status, String numeroControl, String identificador, String fechaEmision, String selloRecepcion) {
        this.codigoGeneracion = codigoGeneracion;
        this.status = status;
        this.numeroControl = numeroControl;
        this.identificador = identificador;
        this.fechaEmision = fechaEmision;
        this.selloRecepcion = selloRecepcion;
    }

    public Respuesta_INFILE() {
    }

    public String getCodigoGeneracion() {
        return codigoGeneracion;
    }

    public void setCodigoGeneracion(String codigoGeneracion) {
        this.codigoGeneracion = codigoGeneracion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNumeroControl() {
        return numeroControl;
    }

    public void setNumeroControl(String numeroControl) {
        this.numeroControl = numeroControl;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getSelloRecepcion() {
        return selloRecepcion;
    }

    public void setSelloRecepcion(String selloRecepcion) {
        this.selloRecepcion = selloRecepcion;
    }

    @Override
    public String toString() {
        return "Respuesta_INFILE [codigoGeneracion=" + codigoGeneracion + ", status=" + status + ", numeroControl=" + numeroControl + ", identificador=" + identificador + ", fechaEmision=" + fechaEmision + ", selloRecepcion=" + selloRecepcion + "]";
    }
    
}