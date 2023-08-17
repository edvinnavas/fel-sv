package Entidades;

import java.io.Serializable;

public class Emisor_contingencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nit;
    private String nombre;
    private String nombreResponsable;
    private String tipoDocResponsable;
    private String numeroDocResponsable;
    private String tipoEstablecimiento;
    private String codEstableMH;
    private String codPuntoVenta;
    private String telefono;
    private String correo;

    public Emisor_contingencia(String nit, String nombre, String nombreResponsable, String tipoDocResponsable, String numeroDocResponsable, String tipoEstablecimiento, String codEstableMH, String codPuntoVenta, String telefono, String correo) {
        this.nit = nit;
        this.nombre = nombre;
        this.nombreResponsable = nombreResponsable;
        this.tipoDocResponsable = tipoDocResponsable;
        this.numeroDocResponsable = numeroDocResponsable;
        this.tipoEstablecimiento = tipoEstablecimiento;
        this.codEstableMH = codEstableMH;
        this.codPuntoVenta = codPuntoVenta;
        this.telefono = telefono;
        this.correo = correo;
    }

    public Emisor_contingencia() {
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public String getTipoDocResponsable() {
        return tipoDocResponsable;
    }

    public void setTipoDocResponsable(String tipoDocResponsable) {
        this.tipoDocResponsable = tipoDocResponsable;
    }

    public String getNumeroDocResponsable() {
        return numeroDocResponsable;
    }

    public void setNumeroDocResponsable(String numeroDocResponsable) {
        this.numeroDocResponsable = numeroDocResponsable;
    }

    public String getTipoEstablecimiento() {
        return tipoEstablecimiento;
    }

    public void setTipoEstablecimiento(String tipoEstablecimiento) {
        this.tipoEstablecimiento = tipoEstablecimiento;
    }

    public String getCodEstableMH() {
        return codEstableMH;
    }

    public void setCodEstableMH(String codEstableMH) {
        this.codEstableMH = codEstableMH;
    }

    public String getCodPuntoVenta() {
        return codPuntoVenta;
    }

    public void setCodPuntoVenta(String codPuntoVenta) {
        this.codPuntoVenta = codPuntoVenta;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public String toString() {
        return "Emisor_contigencia{" + "nit=" + nit + ", nombre=" + nombre + ", nombreResponsable=" + nombreResponsable + ", tipoDocResponsable=" + tipoDocResponsable + ", numeroDocResponsable=" + numeroDocResponsable + ", tipoEstablecimiento=" + tipoEstablecimiento + ", codEstableMH=" + codEstableMH + ", codPuntoVenta=" + codPuntoVenta + ", telefono=" + telefono + ", correo=" + correo + '}';
    }
    
}
