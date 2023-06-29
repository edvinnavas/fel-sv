package Controladores;

import Entidades.Identificacion_invalidacion;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

public class Ctrl_Identificacion_INVALIDACION_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Identificacion_INVALIDACION_V3() {
    }

    public Identificacion_invalidacion obtener_identificacion_invalidacion_v3(Long id_dte, Connection conn) {
        Identificacion_invalidacion resultado = new Identificacion_invalidacion();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setVersion(ctrl_base_datos.ObtenerEntero("SELECT F.DTE_VERSION FROM IDENTIFICACION_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setAmbiente(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_001 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_001 FROM IDENTIFICACION_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setCodigoGeneracion(ctrl_base_datos.ObtenerString("SELECT F.CODIGOGENERACION FROM IDENTIFICACION_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setFecAnula(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_ANULACION,'YYYY-MM-DD') FROM IDENTIFICACION_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setHorAnula(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_ANULACION,'HH24:MI:SS') FROM IDENTIFICACION_INVALIDACION_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_identificacion_invalidacion_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_identificacion_jde_invalidacion_v3(Long id_dte, String ambiente, Connection conn) {
        String resultado = "";

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            Long ID_DTE = id_dte;
            Long ID_IDENTIFICACION = Long.valueOf("1");
            Long DTE_VERSION = Long.valueOf("2");
            Long ID_CAT_001;
            if (ambiente.equals("PY")) {
                ID_CAT_001 = Long.valueOf("1");
            } else {
                ID_CAT_001 = Long.valueOf("2");
            }
            String CODIGOGENERACION = UUID.randomUUID().toString().toUpperCase();
            String FECHA_HORA_ANULACION = ctrl_base_datos.ObtenerString("SELECT TO_CHAR(CURRENT_DATE,'YYYY/MM/DD') || ' ' || TO_CHAR(CURRENT_TIMESTAMP,'HH24:MI:SS') FECHA_HORA_EMISION FROM DUAL", conn);

            String cadenasql = "INSERT INTO IDENTIFICACION_INVALIDACION_V3 ("
                    + "ID_DTE,"
                    + "ID_IDENTIFICACION, "
                    + "DTE_VERSION, "
                    + "ID_CAT_001, "
                    + "CODIGOGENERACION, "
                    + "FECHA_HORA_ANULACION, "
                    + "TIPOMONEDA) VALUES ("
                    + ID_DTE + ","
                    + ID_IDENTIFICACION + ","
                    + DTE_VERSION + ","
                    + ID_CAT_001 + ",'"
                    + CODIGOGENERACION + "',"
                    + "TO_DATE('" + FECHA_HORA_ANULACION + "','YYYY/MM/DD HH24:MI:SS')" + ")";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_identificacion_jde_invalidacion_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
