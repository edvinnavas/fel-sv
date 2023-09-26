package Entidades;

import java.io.Serializable;

public class Emisor_fse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String nit;
    private String nrc;
    private String nombre;
    private String codActividad;
    private String descActividad;
    private Direccion direccion;
    private String telefono;
    private String codEstableMH;
    private String codEstable;
    private String codPuntoVentaMH;
    private String codPuntoVenta;
    private String correo;

    public Emisor_fse(String nit, String nrc, String nombre, String codActividad, String descActividad, Direccion direccion, String telefono, String codEstableMH, String codEstable, String codPuntoVentaMH, String codPuntoVenta, String correo) {
        this.nit = nit;
        this.nrc = nrc;
        this.nombre = nombre;
        this.codActividad = codActividad;
        this.descActividad = descActividad;
        this.direccion = direccion;
        this.telefono = telefono;
        this.codEstableMH = codEstableMH;
        this.codEstable = codEstable;
        this.codPuntoVentaMH = codPuntoVentaMH;
        this.codPuntoVenta = codPuntoVenta;
        this.correo = correo;
    }

    public Emisor_fse() {
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodActividad() {
        return codActividad;
    }

    public void setCodActividad(String codActividad) {
        this.codActividad = codActividad;
    }

    public String getDescActividad() {
        return descActividad;
    }

    public void setDescActividad(String descActividad) {
        this.descActividad = descActividad;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCodEstableMH() {
        return codEstableMH;
    }

    public void setCodEstableMH(String codEstableMH) {
        this.codEstableMH = codEstableMH;
    }

    public String getCodEstable() {
        return codEstable;
    }

    public void setCodEstable(String codEstable) {
        this.codEstable = codEstable;
    }

    public String getCodPuntoVentaMH() {
        return codPuntoVentaMH;
    }

    public void setCodPuntoVentaMH(String codPuntoVentaMH) {
        this.codPuntoVentaMH = codPuntoVentaMH;
    }

    public String getCodPuntoVenta() {
        return codPuntoVenta;
    }

    public void setCodPuntoVenta(String codPuntoVenta) {
        this.codPuntoVenta = codPuntoVenta;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public String toString() {
        return "Emisor_fse{" + "nit=" + nit + ", nrc=" + nrc + ", nombre=" + nombre + ", codActividad=" + codActividad + ", descActividad=" + descActividad + ", direccion=" + direccion + ", telefono=" + telefono + ", codEstableMH=" + codEstableMH + ", codEstable=" + codEstable + ", codPuntoVentaMH=" + codPuntoVentaMH + ", codPuntoVenta=" + codPuntoVenta + ", correo=" + correo + '}';
    }
    
}
