package Entidades;

import java.io.Serializable;
import java.util.List;

public class RESPUESTA_RECEPCIONDTELOTE_MH implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private List<RESPUESTA_RECEPCIONDTE_MH> procesados;
    private List<RESPUESTA_RECEPCIONDTE_MH> rechazados;

    public RESPUESTA_RECEPCIONDTELOTE_MH(List<RESPUESTA_RECEPCIONDTE_MH> procesados, List<RESPUESTA_RECEPCIONDTE_MH> rechazados) {
        this.procesados = procesados;
        this.rechazados = rechazados;
    }

    public RESPUESTA_RECEPCIONDTELOTE_MH() {
    }

    public List<RESPUESTA_RECEPCIONDTE_MH> getProcesados() {
        return procesados;
    }

    public void setProcesados(List<RESPUESTA_RECEPCIONDTE_MH> procesados) {
        this.procesados = procesados;
    }

    public List<RESPUESTA_RECEPCIONDTE_MH> getRechazados() {
        return rechazados;
    }

    public void setRechazados(List<RESPUESTA_RECEPCIONDTE_MH> rechazados) {
        this.rechazados = rechazados;
    }

    @Override
    public String toString() {
        return "RESPUESTA_RECEPCIONDTELOTE_MH{" + "procesados=" + procesados + ", rechazados=" + rechazados + '}';
    }
    
}
