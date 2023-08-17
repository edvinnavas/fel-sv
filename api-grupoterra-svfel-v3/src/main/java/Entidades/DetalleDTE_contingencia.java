package Entidades;

import java.io.Serializable;

public class DetalleDTE_contingencia implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Number noItem;
    private String codigoGeneracion;
    private String tipoDoc;

    public DetalleDTE_contingencia(Number noItem, String codigoGeneracion, String tipoDoc) {
        this.noItem = noItem;
        this.codigoGeneracion = codigoGeneracion;
        this.tipoDoc = tipoDoc;
    }

    public DetalleDTE_contingencia() {
    }

    public Number getNoItem() {
        return noItem;
    }

    public void setNoItem(Number noItem) {
        this.noItem = noItem;
    }

    public String getCodigoGeneracion() {
        return codigoGeneracion;
    }

    public void setCodigoGeneracion(String codigoGeneracion) {
        this.codigoGeneracion = codigoGeneracion;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    @Override
    public String toString() {
        return "DetalleDTE_contingencia{" + "noItem=" + noItem + ", codigoGeneracion=" + codigoGeneracion + ", tipoDoc=" + tipoDoc + '}';
    }
    
}
