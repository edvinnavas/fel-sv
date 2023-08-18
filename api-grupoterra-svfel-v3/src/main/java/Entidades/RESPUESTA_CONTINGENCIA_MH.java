package Entidades;

import java.io.Serializable;
import java.util.List;

public class RESPUESTA_CONTINGENCIA_MH implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String estado;
    private String fechaHora;
    private String mensaje;
    private String selloRecibido;
    private List<String> observaciones;

    public RESPUESTA_CONTINGENCIA_MH(String estado, String fechaHora, String mensaje, String selloRecibido, List<String> observaciones) {
        this.estado = estado;
        this.fechaHora = fechaHora;
        this.mensaje = mensaje;
        this.selloRecibido = selloRecibido;
        this.observaciones = observaciones;
    }

    public RESPUESTA_CONTINGENCIA_MH() {
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getSelloRecibido() {
        return selloRecibido;
    }

    public void setSelloRecibido(String selloRecibido) {
        this.selloRecibido = selloRecibido;
    }

    public List<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<String> observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "RESPUESTA_CONTINGENCIA_MH{" + "estado=" + estado + ", fechaHora=" + fechaHora + ", mensaje=" + mensaje + ", selloRecibido=" + selloRecibido + ", observaciones=" + observaciones + '}';
    }
    
}
