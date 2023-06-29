package Controladores;

import Entidades.Documento_invalidacion;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_Documento_INVALIDACION_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Documento_INVALIDACION_V3() {
    }

    public Documento_invalidacion obtener_documento_invalidacion_v3(Long id_dte, Connection conn) {
        Documento_invalidacion resultado = new Documento_invalidacion();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setTipoDte(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_002 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_002 FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setCodigoGeneracion(ctrl_base_datos.ObtenerString("SELECT F.CODIGOGENERACION FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setSelloRecibido(ctrl_base_datos.ObtenerString("SELECT F.SELLORECIBIDO FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNumeroControl(ctrl_base_datos.ObtenerString("SELECT F.NUMEROCONTROL FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setFecEmi(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_EMISION,'YYYY-MM-DD') FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setMontoIva(ctrl_base_datos.ObtenerDouble("SELECT F.MONTOIVA FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCodigoGeneracionR(ctrl_base_datos.ObtenerString("SELECT F.CODIGOGENERACIONR FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTipoDocumento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_022 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_022 FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNumDocumento(ctrl_base_datos.ObtenerString("SELECT F.NUMDOCUMENTO FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_documento_invalidacion_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_documento_jde_invalidacion_v3(Long id_dte, String ambiente, String KCOO_JDE, String DOCO_JDE, String DCTO_JDE, String DOC_JDE, String DCT_JDE, Connection conn) {
        String resultado = "";

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            String esquema;
            String dblink;
            if (ambiente.equals("PY")) {
                esquema = "CRPDTA";
                dblink = "JDEPY";
            } else {
                esquema = "PRODDTA";
                dblink = "JDEPD";
            }
            
            String tabla_dte = "";
            String tabla_identificacion = "";
            String tabla_extension = "";
            switch (DCTO_JDE) {
                case "S3": {
                    tabla_dte = "DTE_CCF_V3";
                    tabla_identificacion = "IDENTIFICACION_CCF_V3";
                    tabla_extension = "EXTENSION_CCF_V3";
                    break;
                }
                case "C3": {
                    tabla_dte = "DTE_NC_V3";
                    tabla_identificacion = "IDENTIFICACION_NC_V3";
                    tabla_extension = "EXTENSION_NC_V3";
                    break;
                }
                case "SD": {
                    tabla_dte = "DTE_ND_V3";
                    tabla_identificacion = "IDENTIFICACION_ND_V3";
                    tabla_extension = "EXTENSION_ND_V3";
                    break;
                }
                case "FE": {
                    tabla_dte = "DTE_F_V3";
                    tabla_identificacion = "IDENTIFICACION_F_V3";
                    tabla_extension = "EXTENSION_F_V3";
                    break;
                }
                case "EX": {
                    tabla_dte = "DTE_FEX_V3";
                    tabla_identificacion = "IDENTIFICACION_FEX_V3";
                    tabla_extension = "EXTENSION_FEX_V3";
                    break;
                }
                case "NR": {
                    tabla_dte = "DTE_NR_V3";
                    tabla_identificacion = "IDENTIFICACION_NR_V3";
                    tabla_extension = "EXTENSION_NR_V3";
                    break;
                }
            }
            
            Long ID_DTE = id_dte;
            Long ID_DOCUMENTO = Long.valueOf("1");
            Long ID_DTE_ANULAR = ctrl_base_datos.ObtenerLong("SELECT F.ID_DTE FROM " + tabla_dte + " F WHERE F.KCOO_JDE='" + KCOO_JDE + "' AND F.DOCO_JDE='" + DOCO_JDE + "' AND F.DCTO_JDE='" + DCTO_JDE + "' AND F.DOC_JDE='" + DOC_JDE + "' AND F.DCT_JDE='" + DCT_JDE + "'", conn);
            String MCU_JDE = ctrl_base_datos.ObtenerString("SELECT TRIM(F.MCU_JDE) FROM " + tabla_dte + " F WHERE F.KCOO_JDE='" + KCOO_JDE + "' AND F.DOCO_JDE='" + DOCO_JDE + "' AND F.DCTO_JDE='" + DCTO_JDE + "' AND F.DOC_JDE='" + DOC_JDE + "' AND F.DCT_JDE='" + DCT_JDE + "'", conn);
            Long ID_CAT_002 = ctrl_base_datos.ObtenerLong("SELECT F.ID_CAT_002 FROM " + tabla_identificacion + " F WHERE F.ID_DTE=" + ID_DTE_ANULAR, conn);
            String CODIGOGENERACION = ctrl_base_datos.ObtenerString("SELECT F.CODIGOGENERACION FROM " + tabla_identificacion + " F WHERE F.ID_DTE=" + ID_DTE_ANULAR, conn);
            String SELLORECIBIDO = ctrl_base_datos.ObtenerString("SELECT F.RESPONSE_FHPROCESAMIENTO FROM " + tabla_dte + " F WHERE F.ID_DTE=" + ID_DTE_ANULAR, conn);
            String NUMEROCONTROL = ctrl_base_datos.ObtenerString("SELECT F.NUMEROCONTROL FROM " + tabla_identificacion + " F WHERE F.ID_DTE=" + ID_DTE_ANULAR, conn);
            String FECHA_HORA_EMISION = ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_EMISION,'YYYY/MM/DD') FROM " + tabla_identificacion + " F WHERE F.ID_DTE=" + id_dte, conn);
            Number MONTOIVA = 0.00;
            String CODIGOGENERACIONR = null;
            Long ID_CAT_022 = Long.valueOf("1");
            String NUMDOCUMENTO = ctrl_base_datos.ObtenerString("SELECT F.DOCUENTREGA FROM " + tabla_extension + " F WHERE F.ID_DTE=" + ID_DTE_ANULAR, conn);
            String NOMBRE = ctrl_base_datos.ObtenerString("SELECT F.NOMBENTREGA FROM " + tabla_extension + " F WHERE F.ID_DTE=" + ID_DTE_ANULAR, conn);
            String TELEFONO = ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA='" + MCU_JDE +"'", conn);
            String CORREO = ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA='" + MCU_JDE +"'", conn);
            
            String cadenasql = "INSERT INTO DOCUMENTO_INVALIDACION_V3 ("
                    + "ID_DTE, "
                    + "ID_DOCUMENTO, "
                    + "ID_CAT_002, "
                    + "CODIGOGENERACION, "
                    + "SELLORECIBIDO, "
                    + "NUMEROCONTROL, "
                    + "FECHA_HORA_EMISION, "
                    + "MONTOIVA, "
                    + "CODIGOGENERACIONR, "
                    + "ID_CAT_022, "
                    + "NUMDOCUMENTO, "
                    + "NOMBRE, "
                    + "TELEFONO, "
                    + "CORREO) VALUES ("
                    + ID_DTE + ","
                    + ID_DOCUMENTO + ","
                    + ID_CAT_002 + ",'"
                    + CODIGOGENERACION + "','"
                    + SELLORECIBIDO + "','"
                    + NUMEROCONTROL + "',"
                    + "TO_DATE('" + FECHA_HORA_EMISION + "','YYYY/MM/DD HH24:MI:SS')" + ","
                    + MONTOIVA + ","
                    + CODIGOGENERACIONR + ","
                    + ID_CAT_022 + ",'"
                    + NUMDOCUMENTO + "','"
                    + NOMBRE + "','"
                    + TELEFONO + "','"
                    + CORREO + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_jde_invalidacion_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
