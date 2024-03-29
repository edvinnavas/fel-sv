package Entidades;

import java.io.Serializable;

public class RESPUESTA_LOTE_DTE_MH implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Number version;
    private String ambiente;
    private Number versionApp;
    private String estado;
    private String idEnvio;
    private String codigoLote;
    private String fhProcesamiento;
    private String clasificaMsg;
    private String codigoMsg;
    private String descripcionMsg;

    public RESPUESTA_LOTE_DTE_MH(Number version, String ambiente, Number versionApp, String estado, String idEnvio, String codigoLote, String fhProcesamiento, String clasificaMsg, String codigoMsg, String descripcionMsg) {
        this.version = version;
        this.ambiente = ambiente;
        this.versionApp = versionApp;
        this.estado = estado;
        this.idEnvio = idEnvio;
        this.codigoLote = codigoLote;
        this.fhProcesamiento = fhProcesamiento;
        this.clasificaMsg = clasificaMsg;
        this.codigoMsg = codigoMsg;
        this.descripcionMsg = descripcionMsg;
    }

    public RESPUESTA_LOTE_DTE_MH() {
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

    public Number getVersionApp() {
        return versionApp;
    }

    public void setVersionApp(Number versionApp) {
        this.versionApp = versionApp;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(String idEnvio) {
        this.idEnvio = idEnvio;
    }

    public String getCodigoLote() {
        return codigoLote;
    }

    public void setCodigoLote(String codigoLote) {
        this.codigoLote = codigoLote;
    }

    public String getFhProcesamiento() {
        return fhProcesamiento;
    }

    public void setFhProcesamiento(String fhProcesamiento) {
        this.fhProcesamiento = fhProcesamiento;
    }

    public String getClasificaMsg() {
        return clasificaMsg;
    }

    public void setClasificaMsg(String clasificaMsg) {
        this.clasificaMsg = clasificaMsg;
    }

    public String getCodigoMsg() {
        return codigoMsg;
    }

    public void setCodigoMsg(String codigoMsg) {
        this.codigoMsg = codigoMsg;
    }

    public String getDescripcionMsg() {
        return descripcionMsg;
    }

    public void setDescripcionMsg(String descripcionMsg) {
        this.descripcionMsg = descripcionMsg;
    }

    @Override
    public String toString() {
        return "RESPUESTA_LOTE_DTE_MH{" + "version=" + version + ", ambiente=" + ambiente + ", versionApp=" + versionApp + ", estado=" + estado + ", idEnvio=" + idEnvio + ", codigoLote=" + codigoLote + ", fhProcesamiento=" + fhProcesamiento + ", clasificaMsg=" + clasificaMsg + ", codigoMsg=" + codigoMsg + ", descripcionMsg=" + descripcionMsg + '}';
    }
    
}
