package Controladores;

import Entidades.RESPUESTA_RECEPCIONDTELOTE_MH;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_ConsultarDteLote_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_ConsultarDteLote_V3() {
    }

    public Integer grabar_respuesta_dte_lote(String ambiente, RESPUESTA_RECEPCIONDTELOTE_MH respuesta_recepciondtelote_mh) {
        Integer resultado = 0;
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

            for (Integer i = 0; i < respuesta_recepciondtelote_mh.getProcesados().size(); i++) {
                String tabla_dte_v3 = "";
                String tabla_identificacion_v3 = "";
                String tabla_resumen_v3 = "";
                String campo_resumen_v3 = "";
                String DCTO_JDE_1 = ctrl_base_datos.ObtenerString("SELECT F.FEDCTO FROM " + esquema + ".F5542FEL@" + dblink + " F WHERE TRIM(F.FECRSREF02)='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getCodigoGeneracion() + "'", conn);
                switch (DCTO_JDE_1) {
                    case "S3": {
                        tabla_dte_v3 = "DTE_CCF_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_CCF_V3";
                        tabla_resumen_v3 = "RESUMEN_CCF_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "C3": {
                        tabla_dte_v3 = "DTE_NC_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_NC_V3";
                        tabla_resumen_v3 = "RESUMEN_NC_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "SD": {
                        tabla_dte_v3 = "DTE_ND_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_ND_V3";
                        tabla_resumen_v3 = "RESUMEN_ND_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "FE": {
                        tabla_dte_v3 = "DTE_F_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_F_V3";
                        tabla_resumen_v3 = "RESUMEN_F_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "EX": {
                        tabla_dte_v3 = "DTE_FEX_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_FEX_V3";
                        tabla_resumen_v3 = "RESUMEN_FEX_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "NR": {
                        tabla_dte_v3 = "DTE_NR_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_NR_V3";
                        tabla_resumen_v3 = "RESUMEN_NR_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "CR": {
                        tabla_dte_v3 = "DTE_CR_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_CR_V3";
                        tabla_resumen_v3 = "RESUMEN_CR_V3";
                        campo_resumen_v3 = "TOTALIVARETENIDO";
                        break;
                    }
                }

                Long id_dte = ctrl_base_datos.ObtenerLong("SELECT F.ID_DTE FROM " + tabla_identificacion_v3 + " F WHERE F.CODIGOGENERACION='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getCodigoGeneracion() + "'", conn);

                String cadenasql = "UPDATE " + tabla_dte_v3 + " SET "
                        + "RESPONSE_VERSION=" + respuesta_recepciondtelote_mh.getProcesados().get(i).getVersion() + ", "
                        + "RESPONSE_AMBIENTE='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getAmbiente() + "', "
                        + "RESPONSE_VERSIONAPP=" + respuesta_recepciondtelote_mh.getProcesados().get(i).getVersionApp() + ", "
                        + "RESPONSE_ESTADO='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getEstado() + "', "
                        + "RESPONSE_NUMVALIDACION='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getSelloRecibido() + "', "
                        + "RESPONSE_FHPROCESAMIENTO='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getFhProcesamiento() + "', "
                        + "RESPONSE_CODIGOMSG='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getCodigoMsg() + "', "
                        + "RESPONSE_DESCRIPCIONMSG='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getDescripcionMsg() + "', "
                        + "RESPONSE_OBSERVACIONES='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getObservaciones().toString() + "', "
                        + "RESPONSE_CLASIFICAMSG='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getClasificaMsg() + "' "
                        + "WHERE "
                        + "ID_DTE=" + id_dte;
                Statement stmt = conn.createStatement();
                // System.out.println(cadenasql);
                stmt.executeUpdate(cadenasql);
                stmt.close();

                String NUMEROCONTROL = ctrl_base_datos.ObtenerString("SELECT F.NUMEROCONTROL FROM " + tabla_identificacion_v3 + " F WHERE F.ID_DTE=" + id_dte, conn);
                String AEXP_JDE = ctrl_base_datos.ObtenerString("SELECT REPLACE(TO_CHAR(F." + campo_resumen_v3 + ",'9999999999D99MI'),'.','') AEXP_JDE FROM " + tabla_resumen_v3 + " F WHERE F.ID_DTE=" + id_dte, conn);

                cadenasql = "UPDATE " + esquema + ".F5542FEL@" + dblink + " SET "
                        + "FESTCD='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getCodigoMsg().trim() + "', "
                        + "FECRSREF01='" + NUMEROCONTROL.trim() + "', "
                        + "FECRSREF03='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getSelloRecibido() + "', "
                        + "FEAEXP=" + AEXP_JDE + " "
                        + "WHERE TRIM(FECRSREF02)='" + respuesta_recepciondtelote_mh.getProcesados().get(i).getCodigoGeneracion() + "'";
                stmt = conn.createStatement();
                // System.out.println(cadenasql);
                stmt.executeUpdate(cadenasql);
                stmt.close();

            }
            
            for (Integer i = 0; i < respuesta_recepciondtelote_mh.getRechazados().size(); i++) {
                String tabla_dte_v3 = "";
                String tabla_identificacion_v3 = "";
                String tabla_resumen_v3 = "";
                String campo_resumen_v3 = "";
                String DCTO_JDE_1 = ctrl_base_datos.ObtenerString("SELECT F.FEDCTO FROM " + esquema + ".F5542FEL@" + dblink + " F WHERE TRIM(F.FECRSREF02)='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getCodigoGeneracion() + "'", conn);
                switch (DCTO_JDE_1) {
                    case "S3": {
                        tabla_dte_v3 = "DTE_CCF_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_CCF_V3";
                        tabla_resumen_v3 = "RESUMEN_CCF_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "C3": {
                        tabla_dte_v3 = "DTE_NC_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_NC_V3";
                        tabla_resumen_v3 = "RESUMEN_NC_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "SD": {
                        tabla_dte_v3 = "DTE_ND_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_ND_V3";
                        tabla_resumen_v3 = "RESUMEN_ND_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "FE": {
                        tabla_dte_v3 = "DTE_F_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_F_V3";
                        tabla_resumen_v3 = "RESUMEN_F_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "EX": {
                        tabla_dte_v3 = "DTE_FEX_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_FEX_V3";
                        tabla_resumen_v3 = "RESUMEN_FEX_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "NR": {
                        tabla_dte_v3 = "DTE_NR_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_NR_V3";
                        tabla_resumen_v3 = "RESUMEN_NR_V3";
                        campo_resumen_v3 = "MONTOTOTALOPERACION";
                        break;
                    }
                    case "CR": {
                        tabla_dte_v3 = "DTE_CR_V3";
                        tabla_identificacion_v3 = "IDENTIFICACION_CR_V3";
                        tabla_resumen_v3 = "RESUMEN_CR_V3";
                        campo_resumen_v3 = "TOTALIVARETENIDO";
                        break;
                    }
                }

                Long id_dte = ctrl_base_datos.ObtenerLong("SELECT F.ID_DTE FROM " + tabla_identificacion_v3 + " F WHERE F.CODIGOGENERACION='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getCodigoGeneracion() + "'", conn);

                String cadenasql = "UPDATE " + tabla_dte_v3 + " SET "
                        + "RESPONSE_VERSION=" + respuesta_recepciondtelote_mh.getRechazados().get(i).getVersion() + ", "
                        + "RESPONSE_AMBIENTE='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getAmbiente() + "', "
                        + "RESPONSE_VERSIONAPP=" + respuesta_recepciondtelote_mh.getRechazados().get(i).getVersionApp() + ", "
                        + "RESPONSE_ESTADO='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getEstado() + "', "
                        + "RESPONSE_NUMVALIDACION='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getSelloRecibido() + "', "
                        + "RESPONSE_FHPROCESAMIENTO='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getFhProcesamiento() + "', "
                        + "RESPONSE_CODIGOMSG='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getCodigoMsg() + "', "
                        + "RESPONSE_DESCRIPCIONMSG='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getDescripcionMsg() + "', "
                        + "RESPONSE_OBSERVACIONES='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getObservaciones().toString() + "', "
                        + "RESPONSE_CLASIFICAMSG='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getClasificaMsg() + "' "
                        + "WHERE "
                        + "ID_DTE=" + id_dte;
                Statement stmt = conn.createStatement();
                // System.out.println(cadenasql);
                stmt.executeUpdate(cadenasql);
                stmt.close();

                String NUMEROCONTROL = ctrl_base_datos.ObtenerString("SELECT F.NUMEROCONTROL FROM " + tabla_identificacion_v3 + " F WHERE F.ID_DTE=" + id_dte, conn);
                String AEXP_JDE = ctrl_base_datos.ObtenerString("SELECT REPLACE(TO_CHAR(F." + campo_resumen_v3 + ",'9999999999D99MI'),'.','') AEXP_JDE FROM " + tabla_resumen_v3 + " F WHERE F.ID_DTE=" + id_dte, conn);

                cadenasql = "UPDATE " + esquema + ".F5542FEL@" + dblink + " SET "
                        + "FESTCD='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getCodigoMsg().trim() + "', "
                        + "FECRSREF01='" + NUMEROCONTROL.trim() + "', "
                        + "FECRSREF03='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getSelloRecibido() + "', "
                        + "FEAEXP=" + AEXP_JDE + " "
                        + "WHERE TRIM(FECRSREF02)='" + respuesta_recepciondtelote_mh.getRechazados().get(i).getCodigoGeneracion() + "'";
                stmt = conn.createStatement();
                // System.out.println(cadenasql);
                stmt.executeUpdate(cadenasql);
                stmt.close();

            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception ex) {
            try {
                resultado = -1;
                conn.rollback();
                conn.setAutoCommit(true);

                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:grabar_respuesta_dte_lote()|ERROR:" + ex.toString());
            } catch (Exception ex1) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:grabar_respuesta_dte_lote()-rollback|ERROR:" + ex.toString());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:grabar_respuesta_dte_lote()-finally|ERROR:" + ex.toString());
            }
        }

        return resultado;
    }

}
