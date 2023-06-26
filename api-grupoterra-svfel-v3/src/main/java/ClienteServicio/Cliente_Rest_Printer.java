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
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public class Cliente_Rest_Printer implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String BASE_URI = "http://10.253.7.252:8086/ApiRestUtils/Print/printDocumentBase64";
    private static final String BASE_URI_PROD = "http://10.253.7.223:8086/ApiRestUtils/Print/printDocumentBase64";
    
    private HttpAuthenticationFeature feature;
    private ClientConfig clientConfig;
    private Client client;

    public Cliente_Rest_Printer(String user, String pass) {
        try {
            this.feature = HttpAuthenticationFeature.basic(user, pass);
            this.clientConfig = new ClientConfig();
            this.clientConfig.register(this.feature);
            this.clientConfig.register(String.class);
            this.client = ClientBuilder.newClient(this.clientConfig);
        } catch (Exception ex) {
            System.out.println("CLASE: " + this.getClass().getName() + " METODO: Cliente_Rest ERROR: " + ex.toString());
        }
    }

    public String printDocumentBase64(String data) {
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
    
    public String printDocumentBase64_prod(String data) {
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
