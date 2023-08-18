package Entidades;

import java.io.Serializable;

public class JsonCONTIN implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nit;
    private String documento;

    public JsonCONTIN(String nit, String documento) {
        this.nit = nit;
        this.documento = documento;
    }

    public JsonCONTIN() {
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    @Override
    public String toString() {
        return "JsonCONTIN{" + "nit=" + nit + ", documento=" + documento + '}';
    }
    
}
