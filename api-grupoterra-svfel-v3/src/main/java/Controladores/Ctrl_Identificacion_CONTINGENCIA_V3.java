package Controladores;

import Entidades.Identificacion_contingencia;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

public class Ctrl_Identificacion_CONTINGENCIA_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Identificacion_CONTINGENCIA_V3() {
    }

    public Identificacion_contingencia obtener_identificacion_contingencia_v3(Long id_contingencia, Connection conn) {
        Identificacion_contingencia resultado = new Identificacion_contingencia();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            resultado.setVersion(ctrl_base_datos.ObtenerEntero("SELECT F.DTE_VERSION FROM IDENTIFICACION_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
            resultado.setAmbiente(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_001 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_001 FROM IDENTIFICACION_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia + ")", conn));
            resultado.setCodigoGeneracion(ctrl_base_datos.ObtenerString("SELECT F.CODIGOGENERACION FROM IDENTIFICACION_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
            resultado.setfTransmision(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_TRANSMISION,'YYYY-MM-DD') FROM IDENTIFICACION_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
            resultado.sethTransmision(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_TRANSMISION,'HH24:MI:SS') FROM IDENTIFICACION_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_identificacion_contingencia_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_identificacion_jde_contingencia_v3(Long id_contingencia, String ambiente, Connection conn) {
        String resultado = "";

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            Long ID_CONTINGENCIA = id_contingencia;
            Long ID_IDENTIFICACION = Long.valueOf("1");
            
            Long DTE_VERSION = Long.valueOf("3");
            Long ID_CAT_001;
            if (ambiente.equals("PY")) {
                ID_CAT_001 = Long.valueOf("1");
            } else {
                ID_CAT_001 = Long.valueOf("2");
            }
            String CODIGOGENERACION = UUID.randomUUID().toString().toUpperCase();
            String FECHA_HORA_TRANSMISION = ctrl_base_datos.ObtenerString("SELECT TO_CHAR(CURRENT_DATE,'YYYY/MM/DD') || ' ' || TO_CHAR(CURRENT_TIMESTAMP,'HH24:MI:SS') FECHA_HORA_TRANSMISION FROM DUAL", conn);

            String cadenasql = "INSERT INTO IDENTIFICACION_CONTINGENCIA_V3 ("
                    + "ID_CONTINGENCIA, "
                    + "ID_IDENTIFICACION, "
                    + "DTE_VERSION, "
                    + "ID_CAT_001, "
                    + "CODIGOGENERACION, "
                    + "FECHA_HORA_TRANSMISION) VALUES ("
                    + ID_CONTINGENCIA + ","
                    + ID_IDENTIFICACION + ","
                    + DTE_VERSION + ","
                    + ID_CAT_001 + ",'"
                    + CODIGOGENERACION + "',"
                    + "TO_DATE('" + FECHA_HORA_TRANSMISION + "','YYYY/MM/DD HH24:MI:SS')" + ")";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_identificacion_jde_contingencia_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
