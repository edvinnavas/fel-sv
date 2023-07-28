package Controladores;

import Entidades.Identificacion_nr;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

public class Ctrl_Identificacion_NR_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Identificacion_NR_V3() {
    }

    public Identificacion_nr obtener_identificacion_nr_v3(Long id_dte, Connection conn) {
        Identificacion_nr resultado = new Identificacion_nr();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            resultado.setVersion(ctrl_base_datos.ObtenerEntero("SELECT F.DTE_VERSION FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setAmbiente(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_001 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_001 FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setTipoDte(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_002 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_002 FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNumeroControl(ctrl_base_datos.ObtenerString("SELECT F.NUMEROCONTROL FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCodigoGeneracion(ctrl_base_datos.ObtenerString("SELECT F.CODIGOGENERACION FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTipoModelo(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_003 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_003 FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setTipoOperacion(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_004 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_004 FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setTipoContingencia(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_005 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_005 FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setMotivoContin(ctrl_base_datos.ObtenerString("SELECT F.MOTIVOCONTIN FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setFecEmi(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_EMISION,'YYYY-MM-DD') FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setHorEmi(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_EMISION,'HH24:MI:SS') FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTipoMoneda(ctrl_base_datos.ObtenerString("SELECT F.TIPOMONEDA FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_identificacion_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    public String extraer_identificacion_jde_nr_v3(Long id_dte, String ambiente, String DCTO_JDE, String MCU_JDE, String CRCD_JDE, String IVD_JDE, Connection conn) {
        String resultado = "";

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            Long ID_DTE = id_dte;
            Long ID_IDENTIFICACION = Long.valueOf("1");
            Long DTE_VERSION = Long.valueOf("3");            
            Long ID_CAT_001;
            if(ambiente.equals("PY")) {
                ID_CAT_001 = Long.valueOf("1");
            } else {
                ID_CAT_001 = Long.valueOf("2");
            }
            Long ID_CAT_002 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_002 C WHERE C.VALOR_JDE='NR'", conn);
            String NUMEROCONTROL = "DTE-" 
                    + ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_002 C WHERE C.VALOR_JDE='NR'", conn) + "-"
                    + ctrl_base_datos.ObtenerString("SELECT F.CODESTABLE FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA='" + MCU_JDE + "'", conn)
                    + MCU_JDE + "-"
                    + ctrl_base_datos.ObtenerString("SELECT LPAD(F.CORRELATIVO_NR + 1, 15, '0') FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA='" + MCU_JDE + "'", conn);
            String cadenasql = "UPDATE EMISOR_ESTABLECIMIENTO_V3 SET CORRELATIVO_NR=CORRELATIVO_NR+1 WHERE CODPUNTOVENTA='" + MCU_JDE + "'";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            String CODIGOGENERACION = UUID.randomUUID().toString().toUpperCase();
            Long ID_CAT_003 = Long.valueOf("1");
            Long ID_CAT_004 = ctrl_base_datos.ObtenerLong("SELECT F.CONTINGENCIA FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA='" + MCU_JDE + "'", conn);
            Long ID_CAT_005;
            String MOTIVOCONTIN;
            if(ID_CAT_004.equals(Long.valueOf("2"))) {
                ID_CAT_005 = Long.valueOf("2");
                MOTIVOCONTIN = "'No disponibilidad de sistema del emisor.'";
            } else {
                ID_CAT_005 = null;
                MOTIVOCONTIN = null;
            }
            // String FECHA_HORA_EMISION = ctrl_base_datos.ObtenerString("SELECT TO_CHAR(CURRENT_DATE,'YYYY/MM/DD') || ' ' || TO_CHAR(CURRENT_TIMESTAMP,'HH24:MI:SS') FECHA_HORA_EMISION FROM DUAL", conn);
            String FECHA_HORA_EMISION = ctrl_base_datos.ObtenerString("SELECT TO_CHAR(TO_DATE(TO_CHAR(" + IVD_JDE + " + 1900000,'9999999'),'YYYYDDD'),'YYYY/MM/DD') || ' ' || TO_CHAR(CURRENT_TIMESTAMP,'HH24:MI:SS') FECHA_HORA_EMISION FROM DUAL", conn);
            String TIPOMONEDA = CRCD_JDE;
            
            cadenasql = "INSERT INTO IDENTIFICACION_NR_V3 ("
                    + "ID_DTE,"
                    + "ID_IDENTIFICACION, "
                    + "DTE_VERSION, "
                    + "ID_CAT_001, "
                    + "ID_CAT_002, "
                    + "NUMEROCONTROL, "
                    + "CODIGOGENERACION, "
                    + "ID_CAT_003, "
                    + "ID_CAT_004, "
                    + "ID_CAT_005, "
                    + "MOTIVOCONTIN, "
                    + "FECHA_HORA_EMISION, "
                    + "TIPOMONEDA) VALUES ("
                    + ID_DTE + ","
                    + ID_IDENTIFICACION + ","
                    + DTE_VERSION + ","
                    + ID_CAT_001 + ","
                    + ID_CAT_002 + ",'"
                    + NUMEROCONTROL + "','"
                    + CODIGOGENERACION + "',"
                    + ID_CAT_003 + ","
                    + ID_CAT_004 + ","
                    + ID_CAT_005 + ","
                    + MOTIVOCONTIN + ","
                    + "TO_DATE('" + FECHA_HORA_EMISION + "','YYYY/MM/DD HH24:MI:SS')" + ",'"
                    + TIPOMONEDA + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_identificacion_jde_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
