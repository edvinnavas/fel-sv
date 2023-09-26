package Entidades;

import java.io.Serializable;
import java.util.List;

public class DTE_FSE_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    private Identificacion_fse identificacion;
    private Emisor_fse emisor;
    private SujetoExcluido_fse sujetoExcluido;
    private List<CuerpoDocumento_fse> cuerpoDocumento;
    private Resumen_fse resumen;
    private List<Apendice_fse> apendice;

    public DTE_FSE_V3(Identificacion_fse identificacion, Emisor_fse emisor, SujetoExcluido_fse sujetoExcluido, List<CuerpoDocumento_fse> cuerpoDocumento, Resumen_fse resumen, List<Apendice_fse> apendice) {
        this.identificacion = identificacion;
        this.emisor = emisor;
        this.sujetoExcluido = sujetoExcluido;
        this.cuerpoDocumento = cuerpoDocumento;
        this.resumen = resumen;
        this.apendice = apendice;
    }

    public DTE_FSE_V3() {
    }

    public Identificacion_fse getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(Identificacion_fse identificacion) {
        this.identificacion = identificacion;
    }

    public Emisor_fse getEmisor() {
        return emisor;
    }

    public void setEmisor(Emisor_fse emisor) {
        this.emisor = emisor;
    }

    public SujetoExcluido_fse getSujetoExcluido() {
        return sujetoExcluido;
    }

    public void setSujetoExcluido(SujetoExcluido_fse sujetoExcluido) {
        this.sujetoExcluido = sujetoExcluido;
    }

    public List<CuerpoDocumento_fse> getCuerpoDocumento() {
        return cuerpoDocumento;
    }

    public void setCuerpoDocumento(List<CuerpoDocumento_fse> cuerpoDocumento) {
        this.cuerpoDocumento = cuerpoDocumento;
    }

    public Resumen_fse getResumen() {
        return resumen;
    }

    public void setResumen(Resumen_fse resumen) {
        this.resumen = resumen;
    }

    public List<Apendice_fse> getApendice() {
        return apendice;
    }

    public void setApendice(List<Apendice_fse> apendice) {
        this.apendice = apendice;
    }

    @Override
    public String toString() {
        return "DTE_FSE_V3{" + "identificacion=" + identificacion + ", emisor=" + emisor + ", sujetoExcluido=" + sujetoExcluido + ", cuerpoDocumento=" + cuerpoDocumento + ", resumen=" + resumen + ", apendice=" + apendice + '}';
    }
    
}
