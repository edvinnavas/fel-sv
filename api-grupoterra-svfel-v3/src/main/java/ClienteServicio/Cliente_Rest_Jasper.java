package ClienteServicio;

import java.io.InputStream;
import java.io.Serializable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

public class Cliente_Rest_Jasper implements Serializable {

    private static final long serialVersionUID = 1L;

    private String j_username = "jasperadmin";
    private String j_password = "jasperadmin";

    private static final String BASE_URI = "http://10.254.7.203:9292/jasperserver/rest_v2";
    private ClientConfig clientConfig;
    private Client client;

    public Cliente_Rest_Jasper() {
        try {
            this.clientConfig = new ClientConfig();
            this.clientConfig.register(String.class);
            this.client = ClientBuilder.newClient(this.clientConfig);
        } catch (Exception ex) {
            System.out.println("CLASE: " + this.getClass().getName() + " METODO: Cliente_Rest_Jasper ERROR: " + ex.toString());
        }
    }

    public InputStream reporte_ccf_pdf(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs/Formato_CCF.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
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
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_ccf_pdf():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_nc_pdf(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs/Formato_NC.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-NC: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_nc_pdf():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_nd_pdf(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs/Formato_ND.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-ND: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_nd_pdf():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_f_pdf(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs/Formato_F.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-F: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_f_pdf():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_fex_pdf(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs/Formato_FEX.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-FEX: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_fex_pdf():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_nr_pdf(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs/Formato_NR.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-NR: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_nr_pdf():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_cr_pdf(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs/Formato_CR.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-CR: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_cr_pdf():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_fse_pdf(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs/Formato_FSE.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
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
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_fse_pdf():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_ccf_pdf_prod(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs_PROD/Formato_CCF.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-CCF-PROD: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_ccf_pdf_prod():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_nc_pdf_prod(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs_PROD/Formato_NC.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-NC-PROD: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_nc_pdf_prod():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_nd_pdf_prod(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs_PROD/Formato_ND.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-ND-PROD: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_nd_pdf_prod():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_f_pdf_prod(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs_PROD/Formato_F.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-F-PROD: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_f_pdf_prod():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_fex_pdf_prod(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs_PROD/Formato_FEX.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-FEX-PROD: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_fex_pdf_prod():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_nr_pdf_prod(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs_PROD/Formato_NR.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-NR-PROD: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_nr_pdf_prod():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_cr_pdf_prod(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs_PROD/Formato_CR.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-CR-PROD: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_cr_pdf_prod():" + ex.toString());
        }

        return resultado;
    }
    
    public InputStream reporte_fse_pdf_prod(String id_dte) {
        InputStream resultado;

        try {
            WebTarget webTarget = this.client.target(BASE_URI)
                    .path("reports/FELSV/DTEs_PROD/Formato_FSE.pdf")
                    .queryParam("id_dte", id_dte)
                    .queryParam("j_username", j_username)
                    .queryParam("j_password", j_password);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get();
            // System.out.println("CONEXION JASPER-REST-API-CCF-PROD: " + response.getStatus());
            if (response.getStatus() == 200) {
                resultado = response.readEntity(InputStream.class);
            } else {
                resultado = null;
            }
        } catch (Exception ex) {
            resultado = null;
            System.out.println("1,ERROR (" + this.getClass().getName() + " - reporte_fse_pdf_prod():" + ex.toString());
        }

        return resultado;
    }
    
}
