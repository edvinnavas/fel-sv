package Entidades;

import java.io.Serializable;

public class SujetoExcluido_fse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String tipoDocumento;
    private String numDocumento;
    private String nombre;
    private String codActividad;
    private String descActividad;
    private Direccion direccion;
    private String telefono;
    private String correo;

    public SujetoExcluido_fse(String tipoDocumento, String numDocumento, String nombre, String codActividad, String descActividad, Direccion direccion, String telefono, String correo) {
        this.tipoDocumento = tipoDocumento;
        this.numDocumento = numDocumento;
        this.nombre = nombre;
        this.codActividad = codActividad;
        this.descActividad = descActividad;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
    }

    public SujetoExcluido_fse() {
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumDocumento() {
        return numDocumento;
    }

    public void setNumDocumento(String numDocumento) {
        this.numDocumento = numDocumento;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public String toString() {
        return "SujetoExcluido_fse{" + "tipoDocumento=" + tipoDocumento + ", numDocumento=" + numDocumento + ", nombre=" + nombre + ", codActividad=" + codActividad + ", descActividad=" + descActividad + ", direccion=" + direccion + ", telefono=" + telefono + ", correo=" + correo + '}';
    }
    
}
