package Controladores;

import ClienteServicio.Cliente_Rest_Firma;
import Entidades.Json_Firmado;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;

public class Ctrl_Firmar_Documento_JWT implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Firmar_Documento_JWT() {
    }

    public Json_Firmado firmardocumento(String ambiente, String emisor_nit, String json_sin_firmar) {
        Json_Firmado resultado = null;

        try {
            Cliente_Rest_Firma cliente_rest = new Cliente_Rest_Firma();
            String json_firmado;
            if (ambiente.equals("PY")) {
                json_firmado = cliente_rest.firmardocumento(json_sin_firmar);
            } else {
                json_firmado = cliente_rest.firmardocumento_prod(json_sin_firmar);
            }
            
            Type listType = new TypeToken<Json_Firmado>() {
            }.getType();
            
            resultado = new Gson().fromJson(json_firmado, listType);
        } catch (Exception ex) {
            resultado = null;
            System.out.println("Proyecto: api-grupoterra-test-felsv | Clase: " + this.getClass().getName() + " | Metodo: firmardocumento | Error: " + ex.toString());
        }

        return resultado;
    }
    
}
