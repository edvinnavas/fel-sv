package Entidades;

import java.io.Serializable;
import java.util.List;

public class JsonLoteDTE implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ambiente;
    private String idEnvio;
    private Integer version;
    private String nitEmisor;
    private List<String> documentos;

    public JsonLoteDTE(String ambiente, String idEnvio, Integer version, String nitEmisor, List<String> documentos) {
        this.ambiente = ambiente;
        this.idEnvio = idEnvio;
        this.version = version;
        this.nitEmisor = nitEmisor;
        this.documentos = documentos;
    }

    public JsonLoteDTE() {
    }

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public String getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(String idEnvio) {
        this.idEnvio = idEnvio;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getNitEmisor() {
        return nitEmisor;
    }

    public void setNitEmisor(String nitEmisor) {
        this.nitEmisor = nitEmisor;
    }

    public List<String> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<String> documentos) {
        this.documentos = documentos;
    }

    @Override
    public String toString() {
        return "JsonLoteDTE{" + "ambiente=" + ambiente + ", idEnvio=" + idEnvio + ", version=" + version + ", nitEmisor=" + nitEmisor + ", documentos=" + documentos + '}';
    }
    
}
