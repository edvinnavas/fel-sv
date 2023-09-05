package Controladores;

import ClienteServicio.Cliente_Rest_SendMail;
import Entidades.Adjunto;
import Entidades.DTE_CONTIGENCIA_V3;
import Entidades.Mensaje_Correo;
import Entidades.RESPUESTA_CONTINGENCIA_MH;
import Entidades.RESPUESTA_LOTE_DTE_MH;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class Ctrl_DTE_CONTINGENCIA_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_DTE_CONTINGENCIA_V3() {
    }

    public List<Long> extraer_evento_contingencia_v3(String ambiente, String KCOO_JDE, String MCU_JDE, Long ID_EMISOR, String FECHA_HORA_INICIO, String FECHA_HORA_FIN) {
        List<Long> resultado = new ArrayList<>();
        Connection conn = null;

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            conn = ctrl_base_datos.obtener_conexion(ambiente);

            conn.setAutoCommit(false);

            Long ID_CONTINGENCIA = ctrl_base_datos.ObtenerLong("SELECT NVL(MAX(F.ID_CONTINGENCIA), 0) + 1 MAXIMO FROM EVENTO_CONTINGENCIA_V3 F", conn);
            Number NO_DTES = 0;
            Number NO_DTES_PROCESADOS = 0;
            Number NO_DTES_RECHAZADOS = 0;

            String cadenasql = "INSERT INTO EVENTO_CONTINGENCIA_V3 ("
                    + "ID_CONTINGENCIA, "
                    + "NO_DTES, "
                    + "NO_DTES_PROCESADOS, "
                    + "NO_DTES_RECHAZADOS, "
                    + "KCOO_JDE, "
                    + "MCU_JDE, "
                    + "ID_EMISOR, "
                    + "RESPONSE_ESTADO, "
                    + "RESPONSE_FECHA_HORA, "
                    + "RESPONSE_MENSAJE, "
                    + "RESPONSE_SELLO_RECIBIDO, "
                    + "RESPONSE_OBSERVACIONES) VALUES ("
                    + ID_CONTINGENCIA + ","
                    + NO_DTES + ","
                    + NO_DTES_PROCESADOS + ","
                    + NO_DTES_RECHAZADOS + ",'"
                    + KCOO_JDE + "','"
                    + MCU_JDE + "',"
                    + ID_EMISOR + ","
                    + "null, null, null, null, null)";
            Statement stmt1 = conn.createStatement();
            // System.out.println(cadenasql);
            stmt1.executeUpdate(cadenasql);
            stmt1.close();

            Ctrl_Identificacion_CONTINGENCIA_V3 ctrl_identificacion_contingencia_v3 = new Ctrl_Identificacion_CONTINGENCIA_V3();
            ctrl_identificacion_contingencia_v3.extraer_identificacion_jde_contingencia_v3(ID_CONTINGENCIA, ambiente, conn);

            Ctrl_Detalle_DTE_CONTINGENCIA_V3 ctrl_detalle_dte_contingencia_v3 = new Ctrl_Detalle_DTE_CONTINGENCIA_V3();
            String result_detalle_dte = ctrl_detalle_dte_contingencia_v3.extraer_detalle_dte_contingencia_v3(ambiente, KCOO_JDE, MCU_JDE, ID_EMISOR, ID_CONTINGENCIA, conn);

            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
            Ctrl_Motivo_CONTINGENCIA_V3 ctrl_motivo_contingencia_v3 = new Ctrl_Motivo_CONTINGENCIA_V3();
            ctrl_motivo_contingencia_v3.extraer_motivo_jde_contingencia_v3(ambiente, ID_CONTINGENCIA, dateFormat2.format(dateFormat1.parse(FECHA_HORA_INICIO)), dateFormat2.format(dateFormat1.parse(FECHA_HORA_FIN)), conn);

            cadenasql = "UPDATE EVENTO_CONTINGENCIA_V3 SET NO_DTES=" + result_detalle_dte + " WHERE ID_CONTINGENCIA=" + ID_CONTINGENCIA;
            stmt1 = conn.createStatement();
            // System.out.println(cadenasql);
            stmt1.executeUpdate(cadenasql);
            stmt1.close();

            resultado.add(ID_CONTINGENCIA);

            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception ex) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);

                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_evento_contingencia_v3()|ERROR:" + ex.toString());
                resultado.clear();
            } catch (Exception ex1) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_evento_contingencia_v3()-rollback|ERROR:" + ex1.toString());
                resultado.clear();
            }

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_evento_contingencia_v3()-finally|ERROR:" + ex.toString());
                resultado.clear();
            }
        }

        return resultado;
    }

    public DTE_CONTIGENCIA_V3 generar_json_contingencia_v3(String ambiente, Long id_contingencia) {
        DTE_CONTIGENCIA_V3 resultado = new DTE_CONTIGENCIA_V3();
        Connection conn = null;

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            conn = ctrl_base_datos.obtener_conexion(ambiente);

            conn.setAutoCommit(false);

            Ctrl_Identificacion_CONTINGENCIA_V3 ctrl_identificacion_contingencia_V3 = new Ctrl_Identificacion_CONTINGENCIA_V3();
            resultado.setIdentificacion(ctrl_identificacion_contingencia_V3.obtener_identificacion_contingencia_v3(id_contingencia, conn));
            
            Ctrl_Emisor_Contigencia_V3 ctrl_emisor_contigencia_V3 = new Ctrl_Emisor_Contigencia_V3();
            resultado.setEmisor(ctrl_emisor_contigencia_V3.obtener_emisor_contingencia_v3(id_contingencia, conn));

            Ctrl_Detalle_DTE_CONTINGENCIA_V3 ctrl_detalle_dte_contingencia_V3 = new Ctrl_Detalle_DTE_CONTINGENCIA_V3();
            resultado.setDetalleDTE(ctrl_detalle_dte_contingencia_V3.obtener_detalle_dte_contingencia_v3(id_contingencia, conn));

            Ctrl_Motivo_CONTINGENCIA_V3 ctrl_motivo_contingencia_V3 = new Ctrl_Motivo_CONTINGENCIA_V3();
            resultado.setMotivo(ctrl_motivo_contingencia_V3.obtener_motivo_contingencia_v3(id_contingencia, conn));

            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception ex) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);

                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:generar_json_contingencia_v3()|ERROR:" + ex.toString());
            } catch (Exception ex1) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:generar_json_contingencia_v3()-rollback|ERROR:" + ex.toString());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:generar_json_contingencia_v3()-finally|ERROR:" + ex.toString());
            }
        }

        return resultado;
    }

    public void registro_db_respuesta_mh(String ambiente, RESPUESTA_CONTINGENCIA_MH respuesta_contingencia_mh, Long id_contigencia) {
        Connection conn = null;

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            conn = ctrl_base_datos.obtener_conexion(ambiente);

            String esquema;
            String dblink;
            if (ambiente.equals("PY")) {
                esquema = "CRPDTA";
                dblink = "JDEPY";
            } else {
                esquema = "PRODDTA";
                dblink = "JDEPD";
            }

            conn.setAutoCommit(false);

            String cadenasql = "UPDATE EVENTO_CONTINGENCIA_V3 SET "
                    + "RESPONSE_ESTADO='" + respuesta_contingencia_mh.getEstado() + "', "
                    + "RESPONSE_FECHA_HORA='" + respuesta_contingencia_mh.getFechaHora() + "', "
                    + "RESPONSE_MENSAJE='" + respuesta_contingencia_mh.getMensaje() + "', "
                    + "RESPONSE_SELLO_RECIBIDO='" + respuesta_contingencia_mh.getSelloRecibido() + "', "
                    + "RESPONSE_OBSERVACIONES='" + respuesta_contingencia_mh.getObservaciones().toString() + "' "
                    + "WHERE "
                    + "ID_CONTINGENCIA=" + id_contigencia;
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            String CODIGOGENERACION = ctrl_base_datos.ObtenerString("SELECT F.CODIGOGENERACION FROM IDENTIFICACION_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contigencia, conn);
            
            conn.commit();
            conn.setAutoCommit(true);

            String cuerpo_html_correo = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<style>"
                    + "table {"
                    + "font-family: arial, sans-serif;"
                    + "border-collapse: collapse;"
                    + "width: 100%;"
                    + "}"
                    + "td,"
                    + "th {"
                    + "border: 1px solid #dddddd;"
                    + "text-align: left;"
                    + "padding: 8px;"
                    + "}"
                    + "tr:nth-child(even) {"
                    + "background-color: #dddddd;"
                    + "}"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<h2>CONTINGENCIA: " + CODIGOGENERACION + "</h2>"
                    + "<table>"
                    + "<tr>"
                    + "<th>Respuesta</th>"
                    + "<th>Valor</th>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Estado</td>"
                    + "<td>" + respuesta_contingencia_mh.getEstado() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Fecha-Hora</td>"
                    + "<td>" + respuesta_contingencia_mh.getFechaHora() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Mensaje</td>"
                    + "<td>" + respuesta_contingencia_mh.getMensaje() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Sello Recibido</td>"
                    + "<td>" + respuesta_contingencia_mh.getSelloRecibido() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Observaciones</td>"
                    + "<td>" + respuesta_contingencia_mh.getObservaciones().toString() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Evento</td>"
                    + "<td>Contigencia</td>"
                    + "</tr>"
                    + "</table>"
                    + "</body>"
                    + "</html>";
            List<Adjunto> files = new ArrayList<>();
            
            File TargetFileJson;
            if (ambiente.equals("PY")) {
                TargetFileJson = new File("/FELSV3/json/jsondte_contin_" + id_contigencia + ".json");                
            } else {
                TargetFileJson = new File("/FELSV3/json_pd/jsondte_contin_" + id_contigencia + ".json");                
            }

            Adjunto adjunto_json = new Adjunto();
            adjunto_json.setName(CODIGOGENERACION + ".json");
            adjunto_json.setType("application/json");
            InputStream inputstream_mail_json = new FileInputStream(TargetFileJson);
            byte[] bytes_json = IOUtils.toByteArray(inputstream_mail_json);
            adjunto_json.setData(Base64.getEncoder().encodeToString(bytes_json));
            adjunto_json.setExt("json");
            adjunto_json.setPath(null);
            files.add(adjunto_json);

            Mensaje_Correo mensaje_correo = new Mensaje_Correo();
            String send_to = ctrl_base_datos.ObtenerString("SELECT LISTAGG(TO_CHAR(TRIM(F.CORREO_ELECTRONICO)),', ') WITHIN GROUP (ORDER BY TO_CHAR(TRIM(F.CORREO_ELECTRONICO))) CUENTAS_CORREO FROM NOTIFICACIONES_CONTIN F WHERE F.ACTIVO=1", conn);
            mensaje_correo.setRecipients(send_to);
            String send_to_cc = ctrl_base_datos.ObtenerString("SELECT LISTAGG(TO_CHAR(TRIM(F.CORREO_ELECTRONICO)),', ') WITHIN GROUP (ORDER BY TO_CHAR(TRIM(F.CORREO_ELECTRONICO))) CUENTAS_CORREO FROM NOTIFICACIONES_CONTIN F WHERE F.ACTIVO=2", conn);
            mensaje_correo.setCc(send_to_cc);
            mensaje_correo.setSubject("Contingencia FELSV.");
            mensaje_correo.setBody(null);
            mensaje_correo.setFrom("replegal-unosv@uno-terra.com");
            mensaje_correo.setBodyHtml(cuerpo_html_correo);
            mensaje_correo.setFiles(files);

            Cliente_Rest_SendMail cliente_rest_sendmail = new Cliente_Rest_SendMail();
            String resul_envio_correo = cliente_rest_sendmail.sendmail(new Gson().toJson(mensaje_correo));
            // System.out.println("Notificación Correo: " + resul_envio_correo);
        } catch (Exception ex) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);

                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:registro_db_respuesta_mh()|ERROR:" + ex.toString());
            } catch (Exception ex1) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:registro_db_respuesta_mh()-rollback|ERROR:" + ex.toString());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:registro_db_respuesta_mh()-finally|ERROR:" + ex.toString());
            }
        }
    }

    public void registro_db_respuesta_lote_mh(String ambiente, RESPUESTA_LOTE_DTE_MH respuesta_lote_dte_mh, Long id_contigencia) {
        Connection conn = null;

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            conn = ctrl_base_datos.obtener_conexion(ambiente);

            String esquema;
            String dblink;
            if (ambiente.equals("PY")) {
                esquema = "CRPDTA";
                dblink = "JDEPY";
            } else {
                esquema = "PRODDTA";
                dblink = "JDEPD";
            }

            conn.setAutoCommit(false);

            String cadenasql = "UPDATE EVENTO_CONTINGENCIA_V3 SET "
                    + "LOTE_VERSION='" + respuesta_lote_dte_mh.getVersion() + "', "
                    + "LOTE_AMBIENTE='" + respuesta_lote_dte_mh.getAmbiente() + "', "
                    + "LOTE_VERSIONAPP='" + respuesta_lote_dte_mh.getVersionApp() + "', "
                    + "LOTE_ESTADO='" + respuesta_lote_dte_mh.getEstado() + "', "
                    + "LOTE_IDENVIO='" + respuesta_lote_dte_mh.getIdEnvio() + "', "
                    + "LOTE_FNPROCESAMIENTO='" + respuesta_lote_dte_mh.getFhProcesamiento() + "', "
                    + "LOTE_CODIGOLOTE='" + respuesta_lote_dte_mh.getCodigoLote() + "', "
                    + "LOTE_CODIGOMSG='" + respuesta_lote_dte_mh.getCodigoMsg() + "', "
                    + "LOTE_DESCRIPCIONMSG='" + respuesta_lote_dte_mh.getDescripcionMsg() + "', "
                    + "LOTE_CLASIFICAMSG='" + respuesta_lote_dte_mh.getClasificaMsg() + "' "
                    + "WHERE "
                    + "ID_CONTINGENCIA=" + id_contigencia;
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            conn.commit();
            conn.setAutoCommit(true);

            String cuerpo_html_correo = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<style>"
                    + "table {"
                    + "font-family: arial, sans-serif;"
                    + "border-collapse: collapse;"
                    + "width: 100%;"
                    + "}"
                    + "td,"
                    + "th {"
                    + "border: 1px solid #dddddd;"
                    + "text-align: left;"
                    + "padding: 8px;"
                    + "}"
                    + "tr:nth-child(even) {"
                    + "background-color: #dddddd;"
                    + "}"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<h2>LOTE-DTE: " + respuesta_lote_dte_mh.getCodigoLote() + "</h2>"
                    + "<table>"
                    + "<tr>"
                    + "<th>Respuesta</th>"
                    + "<th>Valor</th>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>verson</td>"
                    + "<td>" + respuesta_lote_dte_mh.getVersion() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>ambiente</td>"
                    + "<td>" + respuesta_lote_dte_mh.getAmbiente() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>versionApp</td>"
                    + "<td>" + respuesta_lote_dte_mh.getVersionApp() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>estado</td>"
                    + "<td>" + respuesta_lote_dte_mh.getEstado() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>IdEnvio</td>"
                    + "<td>" + respuesta_lote_dte_mh.getIdEnvio() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<tr>"
                    + "<td>fhProcesamiento</td>"
                    + "<td>" + respuesta_lote_dte_mh.getFhProcesamiento() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<tr>"
                    + "<td>codigoLote</td>"
                    + "<td>" + respuesta_lote_dte_mh.getCodigoLote() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<tr>"
                    + "<td>codigoMsg</td>"
                    + "<td>" + respuesta_lote_dte_mh.getCodigoMsg() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<tr>"
                    + "<td>descripcionMsg</td>"
                    + "<td>" + respuesta_lote_dte_mh.getDescripcionMsg() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Evento</td>"
                    + "<td>Contigencia</td>"
                    + "</tr>"
                    + "</table>"
                    + "</body>"
                    + "</html>";
            List<Adjunto> files = new ArrayList<>();
            
            File TargetFileJson;
            if (ambiente.equals("PY")) {
                TargetFileJson = new File("/FELSV3/json/json_lote_dte_" + id_contigencia + ".json");
            } else {
                TargetFileJson = new File("/FELSV3/json_pd/json_lote_dte_" + id_contigencia + ".json");
            }
            
            Adjunto adjunto_json = new Adjunto();
            adjunto_json.setName(respuesta_lote_dte_mh.getCodigoLote() + ".json");
            adjunto_json.setType("application/json");
            InputStream inputstream_mail_json = new FileInputStream(TargetFileJson);
            byte[] bytes_json = IOUtils.toByteArray(inputstream_mail_json);
            adjunto_json.setData(Base64.getEncoder().encodeToString(bytes_json));
            adjunto_json.setExt("json");
            adjunto_json.setPath(null);
            files.add(adjunto_json);

            Mensaje_Correo mensaje_correo = new Mensaje_Correo();
            String send_to = ctrl_base_datos.ObtenerString("SELECT LISTAGG(TO_CHAR(TRIM(F.CORREO_ELECTRONICO)),', ') WITHIN GROUP (ORDER BY TO_CHAR(TRIM(F.CORREO_ELECTRONICO))) CUENTAS_CORREO FROM NOTIFICACIONES_CONTIN F WHERE F.ACTIVO=1", conn);
            mensaje_correo.setRecipients(send_to);
            String send_to_cc = ctrl_base_datos.ObtenerString("SELECT LISTAGG(TO_CHAR(TRIM(F.CORREO_ELECTRONICO)),', ') WITHIN GROUP (ORDER BY TO_CHAR(TRIM(F.CORREO_ELECTRONICO))) CUENTAS_CORREO FROM NOTIFICACIONES_CONTIN F WHERE F.ACTIVO=2", conn);
            mensaje_correo.setCc(send_to_cc);
            mensaje_correo.setSubject("Contingencia FELSV.");
            mensaje_correo.setBody(null);
            mensaje_correo.setFrom("replegal-unosv@uno-terra.com");
            mensaje_correo.setBodyHtml(cuerpo_html_correo);
            mensaje_correo.setFiles(files);

            Cliente_Rest_SendMail cliente_rest_sendmail = new Cliente_Rest_SendMail();
            String resul_envio_correo = cliente_rest_sendmail.sendmail(new Gson().toJson(mensaje_correo));
            // System.out.println("Notificación Correo: " + resul_envio_correo);
        } catch (Exception ex) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);

                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:registro_db_respuesta_mh()|ERROR:" + ex.toString());
            } catch (Exception ex1) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:registro_db_respuesta_mh()-rollback|ERROR:" + ex.toString());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:registro_db_respuesta_mh()-finally|ERROR:" + ex.toString());
            }
        }
    }
    
}
