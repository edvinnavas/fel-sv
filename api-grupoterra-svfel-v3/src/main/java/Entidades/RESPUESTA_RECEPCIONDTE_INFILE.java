package Entidades;

import java.io.Serializable;

public class RESPUESTA_RECEPCIONDTE_INFILE implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Boolean ok;
    private String mensaje;
    private Errores_INFILE errores;
    private Respuesta_INFILE respuesta;
    private Respuesta_INFILE_DGI respuesta_dgi;
    private String pdf_path;
    private String adendas;

    public RESPUESTA_RECEPCIONDTE_INFILE(Boolean ok, String mensaje, Errores_INFILE errores, Respuesta_INFILE respuesta, Respuesta_INFILE_DGI respuesta_dgi, String pdf_path, String adendas) {
        this.ok = ok;
        this.mensaje = mensaje;
        this.errores = errores;
        this.respuesta = respuesta;
        this.respuesta_dgi = respuesta_dgi;
        this.pdf_path = pdf_path;
        this.adendas = adendas;
    }

    public RESPUESTA_RECEPCIONDTE_INFILE() {
    }

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Errores_INFILE getErrores() {
        return errores;
    }

    public void setErrores(Errores_INFILE errores) {
        this.errores = errores;
    }

    public Respuesta_INFILE getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(Respuesta_INFILE respuesta) {
        this.respuesta = respuesta;
    }

    public Respuesta_INFILE_DGI getRespuesta_dgi() {
        return respuesta_dgi;
    }

    public void setRespuesta_dgi(Respuesta_INFILE_DGI respuesta_dgi) {
        this.respuesta_dgi = respuesta_dgi;
    }

    public String getPdf_path() {
        return pdf_path;
    }

    public void setPdf_path(String pdf_path) {
        this.pdf_path = pdf_path;
    }

    public String getAdendas() {
        return adendas;
    }

    public void setAdendas(String adendas) {
        this.adendas = adendas;
    }

    @Override
    public String toString() {
        return "RESPUESTA_RECEPCIONDTE_INFILE [ok=" + ok + ", mensaje=" + mensaje + ", errores=" + errores + ", respuesta=" + respuesta + ", respuesta_dgi=" + respuesta_dgi + ", pdf_path=" + pdf_path + ", adendas=" + adendas + "]";
    }
    
}
