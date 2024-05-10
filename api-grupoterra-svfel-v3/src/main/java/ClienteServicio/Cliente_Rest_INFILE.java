package ClienteServicio;

import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;

public class Cliente_Rest_INFILE implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final String BASE_URI = "https://certificador.infile.com.sv/api/v1";
    private static final String BASE_URI_PROD = "https://certificador.infile.com.sv/api/v1";
    
    private ClientConfig clientConfig;
    private Client client;

    public Cliente_Rest_INFILE() {
        try {
            TrustManager[] trustAllCerts = new X509TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                        // return null;
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, null);

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String string, SSLSession ssls) {
                    // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    return true;
                }
            };

            ClientBuilder clientBuilder = new JerseyClientBuilder();
            clientBuilder.sslContext(sslContext);
            clientBuilder.hostnameVerifier(hostnameVerifier);

            this.clientConfig = new ClientConfig();
            this.clientConfig.register(String.class);

            this.client = clientBuilder.withConfig(clientConfig).build();
        } catch (Exception ex) {
            System.out.println("CLASE: " + this.getClass().getName() + " METODO: Cliente_Rest ERROR: " + ex.toString());
        }
    }

    public String certificar_json(String ambiente, String usuario, String llave, String identificador, String documento) {
        String resultado = "";

        try {
            WebTarget webTarget;
            if (ambiente.equals("PY")) {
                webTarget = client.target(BASE_URI).path("certificacion/test/documento/1/certificar_json");
            } else {
                webTarget = client.target(BASE_URI_PROD).path("certificacion/test/documento/1/certificar_json");
            }
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            invocationBuilder.header("usuario", usuario);
            invocationBuilder.header("llave", llave);
            invocationBuilder.header("identificador", identificador);
            Response response = invocationBuilder.post(Entity.json(documento));

            if (response.getStatus() == 200 || response.getStatus() == 400 || response.getStatus() == 201 || response.getStatus() == 422) {
                resultado = response.readEntity(String.class);
            } else {
                resultado = response.getStatus() + ": " + response.getStatusInfo();
            }
        } catch (Exception ex) {
            resultado = "ERROR: " + ex.toString();
        }

        return resultado;
    }

    public InputStream reporte_documento(String ambiente, String uuid, String formato) {
        InputStream resultado;

        try {
            WebTarget webTarget;
            if (ambiente.equals("PY")) {
                webTarget = this.client.target(BASE_URI).path("reporte/reporte_documento")
                    .queryParam("uuid", uuid)
                    .queryParam("formato", formato);
            } else {
                webTarget = this.client.target(BASE_URI_PROD).path("reporte/reporte_documento")
                    .queryParam("uuid", uuid)
                    .queryParam("formato", formato);
            }
            
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-CCF: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_documento():" + ex.toString());
        }

        return resultado;
    }

}
