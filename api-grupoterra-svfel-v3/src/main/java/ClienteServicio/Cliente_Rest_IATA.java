package ClienteServicio;

import java.io.Serializable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

public class Cliente_Rest_IATA implements Serializable {

    private static final long serialVersionUID = 1L;

    // private static final String BASE_URI = "http://192.200.107.149:8283/RestNotaRemisionIATA/services/genticketszq";
    private static final String BASE_URI = "http://10.254.7.203:8283/RestNotaRemisionIATA/services/genticketszq";
    private static final String BASE_URI_PROD = "http://10.254.7.203:8283/RestNotaRemisionIATA/services/genticketszq";
    private ClientConfig clientConfig;
    private Client client;

    public Cliente_Rest_IATA() {
        try {
            this.clientConfig = new ClientConfig();
            this.clientConfig.register(String.class);
            this.client = ClientBuilder.newClient(this.clientConfig);
        } catch (Exception ex) {
            System.out.println("CLASE: " + this.getClass().getName() + " METODO: Cliente_Rest_IATA ERROR: " + ex.toString());
        }
    }

    public String genticketszq() {
        String resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI).path("getTicketsZQ");
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION Cliente_Rest_IATA: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(String.class);
            } else {
                resultado = response.getStatus() + ": " + response.getStatusInfo();
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - genticketszq):" + ex.toString());
        }

        return resultado;
    }
    
    public String genticketszq_prod() {
        String resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI_PROD).path("getTicketsZQ");
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION Cliente_Rest_IATA: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(String.class);
            } else {
                resultado = response.getStatus() + ": " + response.getStatusInfo();
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - genticketszq):" + ex.toString());
        }

        return resultado;
    }
    
}
