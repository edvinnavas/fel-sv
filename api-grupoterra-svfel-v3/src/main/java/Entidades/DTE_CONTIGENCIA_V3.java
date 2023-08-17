package Entidades;

import java.io.Serializable;
import java.util.List;

public class DTE_CONTIGENCIA_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    private Identificacion_contingencia identificacion;
    private Emisor_contingencia emisor;
    private List<DetalleDTE_contingencia>  detalleDTE;
    private Motivo_contingencia motivo;

    public DTE_CONTIGENCIA_V3(Identificacion_contingencia identificacion, Emisor_contingencia emisor, List<DetalleDTE_contingencia> detalleDTE, Motivo_contingencia motivo) {
        this.identificacion = identificacion;
        this.emisor = emisor;
        this.detalleDTE = detalleDTE;
        this.motivo = motivo;
    }

    public DTE_CONTIGENCIA_V3() {
    }

    public Identificacion_contingencia getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(Identificacion_contingencia identificacion) {
        this.identificacion = identificacion;
    }

    public Emisor_contingencia getEmisor() {
        return emisor;
    }

    public void setEmisor(Emisor_contingencia emisor) {
        this.emisor = emisor;
    }

    public List<DetalleDTE_contingencia> getDetalleDTE() {
        return detalleDTE;
    }

    public void setDetalleDTE(List<DetalleDTE_contingencia> detalleDTE) {
        this.detalleDTE = detalleDTE;
    }

    public Motivo_contingencia getMotivo() {
        return motivo;
    }

    public void setMotivo(Motivo_contingencia motivo) {
        this.motivo = motivo;
    }

    @Override
    public String toString() {
        return "DTE_CONTIGENCIA_V3{" + "identificacion=" + identificacion + ", emisor=" + emisor + ", detalleDTE=" + detalleDTE + ", motivo=" + motivo + '}';
    }
    
}
