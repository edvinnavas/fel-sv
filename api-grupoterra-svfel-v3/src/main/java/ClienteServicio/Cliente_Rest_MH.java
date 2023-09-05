package ClienteServicio;

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
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;

public class Cliente_Rest_MH implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final String BASE_URI = "https://apitest.dtes.mh.gob.sv";
    private static final String BASE_URI_PROD = "https://api.dtes.mh.gob.sv";
    
    private ClientConfig clientConfig;
    private Client client;

    public Cliente_Rest_MH() {
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

    public String autenticar(String ambiente, String user, String pwd) {
        String resultado = "";

        try {
            WebTarget webTarget;
            if (ambiente.equals("PY")) {
                webTarget = client.target(BASE_URI).path("seguridad/auth");
            } else {
                webTarget = client.target(BASE_URI_PROD).path("seguridad/auth");
            }
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.add("user", user);
            formData.add("pwd", pwd);
            Response response = invocationBuilder.post(Entity.form(formData));
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

    public String recepciondte(String ambiente, String token, String documento) {
        String resultado = "";

        try {
            WebTarget webTarget;
            if (ambiente.equals("PY")) {
                webTarget = client.target(BASE_URI).path("fesv/recepciondte");
            } else {
                webTarget = client.target(BASE_URI_PROD).path("fesv/recepciondte");
            }
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            invocationBuilder.header("Authorization", token);
            Response response = invocationBuilder.post(Entity.json(documento));

            if (response.getStatus() == 200 || response.getStatus() == 400) {
                resultado = response.readEntity(String.class);
            } else {
                resultado = response.getStatus() + ": " + response.getStatusInfo();
            }
        } catch (Exception ex) {
            resultado = "ERROR: " + ex.toString();
        }

        return resultado;
    }

    public String anulardte(String ambiente, String token, String documento) {
        String resultado = "";

        try {
            WebTarget webTarget;
            if (ambiente.equals("PY")) {
                webTarget = client.target(BASE_URI).path("fesv/anulardte");
            } else {
                webTarget = client.target(BASE_URI_PROD).path("fesv/anulardte");
            }
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            invocationBuilder.header("Authorization", token);
            Response response = invocationBuilder.post(Entity.json(documento));

            if (response.getStatus() == 200 || response.getStatus() == 400) {
                resultado = response.readEntity(String.class);
            } else {
                resultado = response.getStatus() + ": " + response.getStatusInfo();
            }
        } catch (Exception ex) {
            resultado = "ERROR: " + ex.toString();
        }

        return resultado;
    }
    
    public String contingencia(String ambiente, String token, String documento) {
        String resultado = "";

        try {
            WebTarget webTarget;
            if (ambiente.equals("PY")) {
                webTarget = client.target(BASE_URI).path("fesv/contingencia");
            } else {
                webTarget = client.target(BASE_URI_PROD).path("fesv/contingencia");
            }
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            invocationBuilder.header("Authorization", token);
            Response response = invocationBuilder.post(Entity.json(documento));

            if (response.getStatus() == 200 || response.getStatus() == 400) {
                resultado = response.readEntity(String.class);
            } else {
                resultado = response.getStatus() + ": " + response.getStatusInfo();
            }
        } catch (Exception ex) {
            resultado = "ERROR: " + ex.toString();
        }

        return resultado;
    }
    
    public String recepcionlote(String ambiente, String token, String documento) {
        String resultado = "";

        try {
            WebTarget webTarget;
            if (ambiente.equals("PY")) {
                webTarget = client.target(BASE_URI).path("fesv/recepcionlote/");
            } else {
                webTarget = client.target(BASE_URI_PROD).path("fesv/recepcionlote/");
            }
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            invocationBuilder.header("Authorization", token);
            Response response = invocationBuilder.post(Entity.json(documento));

            if (response.getStatus() == 200 || response.getStatus() == 400) {
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
