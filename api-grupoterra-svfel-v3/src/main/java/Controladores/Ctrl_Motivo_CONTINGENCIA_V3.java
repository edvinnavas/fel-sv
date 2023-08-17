package Controladores;

import Entidades.Motivo_contingencia;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_Motivo_CONTINGENCIA_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Motivo_CONTINGENCIA_V3() {
    }

    public Motivo_contingencia obtener_motivo_contingencia_v3(Long id_contingencia, Connection conn) {
        Motivo_contingencia resultado = new Motivo_contingencia();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setfInicio(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_INICIO,'YYYY-MM-DD') FROM MOTIVO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
            resultado.setfFin(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_FIN,'YYYY-MM-DD') FROM MOTIVO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
            resultado.sethInicio(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_INICIO,'HH24:MI:SS') FROM MOTIVO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
            resultado.sethFin(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_FIN,'HH24:MI:SS') FROM MOTIVO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
            resultado.setTipoContingencia(ctrl_base_datos.ObtenerLong("SELECT F.ID_CAT_005 FROM MOTIVO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
            resultado.setMotivoContingencia(ctrl_base_datos.ObtenerString("SELECT F.MOTIVO_CONTINGENCIA FROM MOTIVO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_motivo_contingencia_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_motivo_jde_contingencia_v3(String ambiente, Long id_contingencia, String FECHA_HORA_INICIO, String FECHA_HORA_FIN, Connection conn) {
        String resultado = "";

        try {
            Long ID_CONTINGENCIA = id_contingencia;
            Long ID_MOTIVO = Long.valueOf("1");
            FECHA_HORA_INICIO = FECHA_HORA_INICIO + "00:00:00";
            FECHA_HORA_FIN = FECHA_HORA_FIN + "23:59:59";
            Long ID_CAT_005 = Long.valueOf("2");
            String MOTIVO_CONTINGENCIA = "No disponibilidad de sistema del emisor.";

            String cadenasql = "INSERT INTO MOTIVO_CONTINGENCIA_V3 ("
                    + "ID_CONTINGENCIA, "
                    + "ID_MOTIVO, "
                    + "FECHA_HORA_INICIO, "
                    + "FECHA_HORA_FIN, "
                    + "ID_CAT_005, "
                    + "MOTIVO_CONTINGENCIA) VALUES ("
                    + ID_CONTINGENCIA + ","
                    + ID_MOTIVO + ","
                    + "TO_DATE('" + FECHA_HORA_INICIO + "','YYYY/MM/DD HH24:MI:SS')" + ","
                    + "TO_DATE('" + FECHA_HORA_FIN + "','YYYY/MM/DD HH24:MI:SS')" + ","
                    + ID_CAT_005 + ",'"
                    + MOTIVO_CONTINGENCIA + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_motivo_jde_contingencia_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
