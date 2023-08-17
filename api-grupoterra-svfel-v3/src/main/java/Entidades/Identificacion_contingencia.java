package Entidades;

import java.io.Serializable;

public class Identificacion_contingencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private Number version;
    private String ambiente;
    private String codigoGeneracion;
    private String fTransmision;
    private String hTransmision;

    public Identificacion_contingencia(Number version, String ambiente, String codigoGeneracion, String fTransmision, String hTransmision) {
        this.version = version;
        this.ambiente = ambiente;
        this.codigoGeneracion = codigoGeneracion;
        this.fTransmision = fTransmision;
        this.hTransmision = hTransmision;
    }

    public Identificacion_contingencia() {
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

    public String getCodigoGeneracion() {
        return codigoGeneracion;
    }

    public void setCodigoGeneracion(String codigoGeneracion) {
        this.codigoGeneracion = codigoGeneracion;
    }

    public String getfTransmision() {
        return fTransmision;
    }

    public void setfTransmision(String fTransmision) {
        this.fTransmision = fTransmision;
    }

    public String gethTransmision() {
        return hTransmision;
    }

    public void sethTransmision(String hTransmision) {
        this.hTransmision = hTransmision;
    }

    @Override
    public String toString() {
        return "Identificacion_contigencia{" + "version=" + version + ", ambiente=" + ambiente + ", codigoGeneracion=" + codigoGeneracion + ", fTransmision=" + fTransmision + ", hTransmision=" + hTransmision + '}';
    }
    
}
