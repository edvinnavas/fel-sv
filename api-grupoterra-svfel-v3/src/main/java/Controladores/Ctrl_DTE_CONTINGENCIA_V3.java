package Controladores;

import ClienteServicio.Cliente_Rest_SendMail;
import Entidades.Adjunto;
import Entidades.DTE_CONTIGENCIA_V3;
import Entidades.Mensaje_Correo;
import Entidades.RESPUESTA_RECEPCIONDTE_MH;
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

    public void registro_db_respuesta_mh(String ambiente, RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh, Long id_dte) {
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

            String cadenasql = "UPDATE DTE_INVALIDACION_V3 SET "
                    + "RESPONSE_VERSION=" + respuesta_recepciondte_mh.getVersion() + ", "
                    + "RESPONSE_AMBIENTE='" + respuesta_recepciondte_mh.getAmbiente() + "', "
                    + "RESPONSE_VERSIONAPP=" + respuesta_recepciondte_mh.getVersionApp() + ", "
                    + "RESPONSE_ESTADO='" + respuesta_recepciondte_mh.getEstado() + "', "
                    + "RESPONSE_CODIGOGENERACION='" + respuesta_recepciondte_mh.getCodigoGeneracion() + "', "
                    + "RESPONSE_NUMVALIDACION='" + respuesta_recepciondte_mh.getSelloRecibido() + "', "
                    + "RESPONSE_FHPROCESAMIENTO='" + respuesta_recepciondte_mh.getFhProcesamiento() + "', "
                    + "RESPONSE_CODIGOMSG='" + respuesta_recepciondte_mh.getCodigoMsg() + "', "
                    + "RESPONSE_DESCRIPCIONMSG='" + respuesta_recepciondte_mh.getDescripcionMsg() + "', "
                    + "RESPONSE_OBSERVACIONES='" + respuesta_recepciondte_mh.getObservaciones().toString() + "', "
                    + "RESPONSE_CLASIFICAMSG='" + respuesta_recepciondte_mh.getClasificaMsg() + "' "
                    + "WHERE "
                    + "ID_DTE=" + id_dte;
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            String KCOO_JDE = ctrl_base_datos.ObtenerString("SELECT F.KCOO_JDE FROM DTE_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            String DOCO_JDE = ctrl_base_datos.ObtenerString("SELECT F.DOCO_JDE FROM DTE_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            String DCTO_JDE = ctrl_base_datos.ObtenerString("SELECT F.DCTO_JDE FROM DTE_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            String DOC_JDE = ctrl_base_datos.ObtenerString("SELECT F.DOC_JDE FROM DTE_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            String DCT_JDE = ctrl_base_datos.ObtenerString("SELECT F.DCT_JDE FROM DTE_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn);

            cadenasql = "UPDATE " + esquema + ".F5542FEL@" + dblink + " SET "
                    + "FERCD='" + respuesta_recepciondte_mh.getCodigoMsg().trim() + "' "
                    + "WHERE FEKCOO='" + KCOO_JDE + "' AND FEDOCO=" + DOCO_JDE + " AND FEDCTO='" + DCTO_JDE + "' AND FEDOC=" + DOC_JDE + " AND FEDCT='" + DCT_JDE + "'";
            stmt = conn.createStatement();
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
                    + "<h2>Anulación DTE: " + respuesta_recepciondte_mh.getCodigoGeneracion() + "</h2>"
                    + "<table>"
                    + "<tr>"
                    + "<th>Respuesta</th>"
                    + "<th>Valor</th>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Versión</td>"
                    + "<td>" + respuesta_recepciondte_mh.getVersion() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Ambiente</td>"
                    + "<td>" + respuesta_recepciondte_mh.getAmbiente() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Versión-APP</td>"
                    + "<td>" + respuesta_recepciondte_mh.getVersionApp() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Estado</td>"
                    + "<td>" + respuesta_recepciondte_mh.getEstado() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Código Generación</td>"
                    + "<td>" + respuesta_recepciondte_mh.getCodigoGeneracion() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Sello Recibido</td>"
                    + "<td>" + respuesta_recepciondte_mh.getSelloRecibido() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Fecha Procesamiento</td>"
                    + "<td>" + respuesta_recepciondte_mh.getFhProcesamiento() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Código Mensaje</td>"
                    + "<td>" + respuesta_recepciondte_mh.getCodigoMsg() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Descripción Mensaje</td>"
                    + "<td>" + respuesta_recepciondte_mh.getDescripcionMsg() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Observaciones</td>"
                    + "<td>" + respuesta_recepciondte_mh.getObservaciones().toString() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Clasificación Mensaje</td>"
                    + "<td>" + respuesta_recepciondte_mh.getClasificaMsg() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Orden-Venta</td>"
                    + "<td>" + DCTO_JDE + "-" + DOCO_JDE + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Documento-Interno</td>"
                    + "<td>" + DCT_JDE + "-" + DOC_JDE + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Evento</td>"
                    + "<td>Invalidación DTE</td>"
                    + "</tr>"
                    + "</table>"
                    + "</body>"
                    + "</html>";

            if (respuesta_recepciondte_mh.getCodigoMsg().trim().equals("001") || respuesta_recepciondte_mh.getCodigoMsg().trim().equals("002")) {
                List<Adjunto> files = new ArrayList<>();

                File TargetFileJson = new File("/FELSV3/json/jsondte_invalidacion_" + id_dte + ".json");

                Adjunto adjunto_json = new Adjunto();
                adjunto_json.setName(respuesta_recepciondte_mh.getCodigoGeneracion() + ".json");
                adjunto_json.setType("application/json");
                InputStream inputstream_mail_json = new FileInputStream(TargetFileJson);
                byte[] bytes_json = IOUtils.toByteArray(inputstream_mail_json);
                adjunto_json.setData(Base64.getEncoder().encodeToString(bytes_json));
                adjunto_json.setExt("json");
                adjunto_json.setPath(null);
                files.add(adjunto_json);

                String MCU_JDE_I = ctrl_base_datos.ObtenerString("SELECT TRIM(F.FEMCU) FROM " + esquema + ".F5542FEL@" + dblink + " F WHERE TRIM(F.FEKCOO)='" + KCOO_JDE + "' AND F.FEDOCO=" + DOCO_JDE + " AND F.FEDCT='" + DCT_JDE + "'", conn);
                String DCTO_JDE_I = ctrl_base_datos.ObtenerString("SELECT F.FEDCTO FROM " + esquema + ".F5542FEL@" + dblink + " F WHERE TRIM(F.FEKCOO)='" + KCOO_JDE + "' AND F.FEDOCO=" + DOCO_JDE + " AND F.FEDCT='" + DCT_JDE + "'", conn);
                Mensaje_Correo mensaje_correo = new Mensaje_Correo();
                String send_to = ctrl_base_datos.ObtenerString("SELECT LISTAGG(TO_CHAR(TRIM(F.CORREO_ELECTRONICO)),', ') WITHIN GROUP (ORDER BY TO_CHAR(TRIM(F.CORREO_ELECTRONICO))) CUENTAS_CORREO FROM NOTIFICACIONES F WHERE F.ACTIVO=1 AND F.MCU_JDE='" + MCU_JDE_I + "' AND F.DCTO_JDE='" + DCTO_JDE_I + "'", conn);
                mensaje_correo.setRecipients(send_to);
                String send_to_cc = ctrl_base_datos.ObtenerString("SELECT LISTAGG(TO_CHAR(TRIM(F.CORREO_ELECTRONICO)),', ') WITHIN GROUP (ORDER BY TO_CHAR(TRIM(F.CORREO_ELECTRONICO))) CUENTAS_CORREO FROM NOTIFICACIONES F WHERE F.ACTIVO=2 AND F.MCU_JDE='" + MCU_JDE_I + "' AND F.DCTO_JDE='" + DCTO_JDE_I + "'", conn);
                mensaje_correo.setCc(send_to_cc);
                mensaje_correo.setSubject("Anulación DTE.");
                mensaje_correo.setBody(null);
                mensaje_correo.setFrom("replegal-unosv@uno-terra.com");
                mensaje_correo.setBodyHtml(cuerpo_html_correo);
                mensaje_correo.setFiles(files);

                Cliente_Rest_SendMail cliente_rest_sendmail = new Cliente_Rest_SendMail();
                String resul_envio_correo = cliente_rest_sendmail.sendmail(new Gson().toJson(mensaje_correo));
                // System.out.println("Notificación Correo: " + resul_envio_correo);
            } else {
                List<Adjunto> files = new ArrayList<>();
                File TargetFileJson = new File("/FELSV3/json/jsondte_invalidacion_" + id_dte + ".json");
                Adjunto adjunto_json = new Adjunto();
                adjunto_json.setName("jsondte_invalidacion_" + id_dte + ".json");
                adjunto_json.setType("application/json");
                InputStream inputstream_mail_json = new FileInputStream(TargetFileJson);
                byte[] bytes_json = IOUtils.toByteArray(inputstream_mail_json);
                adjunto_json.setData(Base64.getEncoder().encodeToString(bytes_json));
                adjunto_json.setExt("json");
                adjunto_json.setPath(null);
                files.add(adjunto_json);

                String MCU_JDE_I = ctrl_base_datos.ObtenerString("SELECT TRIM(F.FEMCU) FROM " + esquema + ".F5542FEL@" + dblink + " F WHERE TRIM(F.FEKCOO)='" + KCOO_JDE + "' AND F.FEDOCO=" + DOCO_JDE + " AND F.FEDCT='" + DCT_JDE + "'", conn);
                String DCTO_JDE_I = ctrl_base_datos.ObtenerString("SELECT F.FEDCTO FROM " + esquema + ".F5542FEL@" + dblink + " F WHERE TRIM(F.FEKCOO)='" + KCOO_JDE + "' AND F.FEDOCO=" + DOCO_JDE + " AND F.FEDCT='" + DCT_JDE + "'", conn);
                Mensaje_Correo mensaje_correo = new Mensaje_Correo();
                String send_to = ctrl_base_datos.ObtenerString("SELECT LISTAGG(TO_CHAR(TRIM(F.CORREO_ELECTRONICO)),', ') WITHIN GROUP (ORDER BY TO_CHAR(TRIM(F.CORREO_ELECTRONICO))) CUENTAS_CORREO FROM NOTIFICACIONES F WHERE F.ACTIVO=1 AND F.MCU_JDE='" + MCU_JDE_I + "' AND F.DCTO_JDE='" + DCTO_JDE_I + "'", conn);
                mensaje_correo.setRecipients(send_to);
                String send_to_cc = ctrl_base_datos.ObtenerString("SELECT LISTAGG(TO_CHAR(TRIM(F.CORREO_ELECTRONICO)),', ') WITHIN GROUP (ORDER BY TO_CHAR(TRIM(F.CORREO_ELECTRONICO))) CUENTAS_CORREO FROM NOTIFICACIONES F WHERE F.ACTIVO=2 AND F.MCU_JDE='" + MCU_JDE_I + "' AND F.DCTO_JDE='" + DCTO_JDE_I + "'", conn);
                mensaje_correo.setCc(send_to_cc);
                mensaje_correo.setSubject("Error Anulación DTE.");
                mensaje_correo.setBody(null);
                mensaje_correo.setFrom("replegal-unosv@uno-terra.com");
                mensaje_correo.setBodyHtml(cuerpo_html_correo);
                mensaje_correo.setFiles(files);

                Cliente_Rest_SendMail cliente_rest_sendmail = new Cliente_Rest_SendMail();
                String resul_envio_correo = cliente_rest_sendmail.sendmail(new Gson().toJson(mensaje_correo));
                // System.out.println("Notificación Correo: " + resul_envio_correo);
            }
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
