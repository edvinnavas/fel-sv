package Entidades;

import java.io.Serializable;

public class CuerpoDocumento_fse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Number numItem;
    private Number tipoItem;
    private Number cantidad;
    private String codigo;
    private Number uniMedida;
    private String descripcion;
    private Number precioUni;
    private Number montoDescu;
    private Number compra;

    public CuerpoDocumento_fse(Number numItem, Number tipoItem, Number cantidad, String codigo, Number uniMedida, String descripcion, Number precioUni, Number montoDescu, Number compra) {
        this.numItem = numItem;
        this.tipoItem = tipoItem;
        this.cantidad = cantidad;
        this.codigo = codigo;
        this.uniMedida = uniMedida;
        this.descripcion = descripcion;
        this.precioUni = precioUni;
        this.montoDescu = montoDescu;
        this.compra = compra;
    }

    public CuerpoDocumento_fse() {
    }

    public Number getNumItem() {
        return numItem;
    }

    public void setNumItem(Number numItem) {
        this.numItem = numItem;
    }

    public Number getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(Number tipoItem) {
        this.tipoItem = tipoItem;
    }

    public Number getCantidad() {
        return cantidad;
    }

    public void setCantidad(Number cantidad) {
        this.cantidad = cantidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Number getUniMedida() {
        return uniMedida;
    }

    public void setUniMedida(Number uniMedida) {
        this.uniMedida = uniMedida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Number getPrecioUni() {
        return precioUni;
    }

    public void setPrecioUni(Number precioUni) {
        this.precioUni = precioUni;
    }

    public Number getMontoDescu() {
        return montoDescu;
    }

    public void setMontoDescu(Number montoDescu) {
        this.montoDescu = montoDescu;
    }

    public Number getCompra() {
        return compra;
    }

    public void setCompra(Number compra) {
        this.compra = compra;
    }

    @Override
    public String toString() {
        return "CuerpoDocumento_fse{" + "numItem=" + numItem + ", tipoItem=" + tipoItem + ", cantidad=" + cantidad + ", codigo=" + codigo + ", uniMedida=" + uniMedida + ", descripcion=" + descripcion + ", precioUni=" + precioUni + ", montoDescu=" + montoDescu + ", compra=" + compra + '}';
    }
    
}
