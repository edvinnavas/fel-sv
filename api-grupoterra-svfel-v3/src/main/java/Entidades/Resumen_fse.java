package Entidades;

import java.io.Serializable;

public class Resumen_fse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Number totalCompra;
    private Number descu;
    private Number totalDescu;
    private Number subTotal;
    private Number ivaRete1;
    private Number reteRenta;
    private Number totalPagar;
    private String totalLetras;
    private Number condicionOperacion;
    private Pagos pagos;
    private String observaciones;

    public Resumen_fse(Number totalCompra, Number descu, Number totalDescu, Number subTotal, Number ivaRete1, Number reteRenta, Number totalPagar, String totalLetras, Number condicionOperacion, Pagos pagos, String observaciones) {
        this.totalCompra = totalCompra;
        this.descu = descu;
        this.totalDescu = totalDescu;
        this.subTotal = subTotal;
        this.ivaRete1 = ivaRete1;
        this.reteRenta = reteRenta;
        this.totalPagar = totalPagar;
        this.totalLetras = totalLetras;
        this.condicionOperacion = condicionOperacion;
        this.pagos = pagos;
        this.observaciones = observaciones;
    }

    public Resumen_fse() {
    }

    public Number getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(Number totalCompra) {
        this.totalCompra = totalCompra;
    }

    public Number getDescu() {
        return descu;
    }

    public void setDescu(Number descu) {
        this.descu = descu;
    }

    public Number getTotalDescu() {
        return totalDescu;
    }

    public void setTotalDescu(Number totalDescu) {
        this.totalDescu = totalDescu;
    }

    public Number getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Number subTotal) {
        this.subTotal = subTotal;
    }

    public Number getIvaRete1() {
        return ivaRete1;
    }

    public void setIvaRete1(Number ivaRete1) {
        this.ivaRete1 = ivaRete1;
    }

    public Number getReteRenta() {
        return reteRenta;
    }

    public void setReteRenta(Number reteRenta) {
        this.reteRenta = reteRenta;
    }

    public Number getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(Number totalPagar) {
        this.totalPagar = totalPagar;
    }

    public String getTotalLetras() {
        return totalLetras;
    }

    public void setTotalLetras(String totalLetras) {
        this.totalLetras = totalLetras;
    }

    public Number getCondicionOperacion() {
        return condicionOperacion;
    }

    public void setCondicionOperacion(Number condicionOperacion) {
        this.condicionOperacion = condicionOperacion;
    }

    public Pagos getPagos() {
        return pagos;
    }

    public void setPagos(Pagos pagos) {
        this.pagos = pagos;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "Resumen_fse{" + "totalCompra=" + totalCompra + ", descu=" + descu + ", totalDescu=" + totalDescu + ", subTotal=" + subTotal + ", ivaRete1=" + ivaRete1 + ", reteRenta=" + reteRenta + ", totalPagar=" + totalPagar + ", totalLetras=" + totalLetras + ", condicionOperacion=" + condicionOperacion + ", pagos=" + pagos + ", observaciones=" + observaciones + '}';
    }
    
}
