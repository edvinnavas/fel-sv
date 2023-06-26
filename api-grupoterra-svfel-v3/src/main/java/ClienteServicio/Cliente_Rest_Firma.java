package ClienteServicio;

import java.io.Serializable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

public class Cliente_Rest_Firma implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String BASE_URI = "http://FirmaJWT-FELSV:8013/firmardocumento/";
    private static final String BASE_URI_PROD = "http://FirmaJWT-FELSV-PROD:8013/firmardocumento/";
    
    private ClientConfig clientConfig;
    private Client client;

    public Cliente_Rest_Firma() {
        try {
            this.clientConfig = new ClientConfig();
            this.clientConfig.register(String.class);
            this.client = ClientBuilder.newClient(this.clientConfig);
        } catch (Exception ex) {
            System.out.println("CLASE: " + this.getClass().getName() + " METODO: Cliente_Rest ERROR: " + ex.toString());
        }
    }

    public String firmardocumento(String data) {
        String resultado = "";

        try {
            WebTarget webTarget = client.target(BASE_URI);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.post(Entity.json(data));
            if (response.getStatus() == 200) {
                resultado = response.readEntity(String.class);
            } else {
                resultado = response.getStatus() + ": " + response.getStatusInfo();
            }
        } catch (Exception ex) {
            resultado = "ERROR: " + ex.toString();
        }

        return resultado;
    }
    
    public String firmardocumento_prod(String data) {
        String resultado = "";

        try {
            WebTarget webTarget = client.target(BASE_URI_PROD);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.post(Entity.json(data));
            if (response.getStatus() == 200) {
                resultado = response.readEntity(String.class);
            } else {
                resultado = response.getStatus() + ": " + response.getStatusInfo();
            }
        } catch (Exception ex) {
            resultado = "ERROR: " + ex.toString();
        }

        return resultado;
    }

}
