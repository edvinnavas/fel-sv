package Controladores;

import Entidades.DetalleDTE_contingencia;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_Detalle_DTE_CONTINGENCIA_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Detalle_DTE_CONTINGENCIA_V3() {
    }

    public List<DetalleDTE_contingencia> obtener_detalle_dte_contingencia_v3(Long id_contingencia, Connection conn) {
        List<DetalleDTE_contingencia> resultado = new ArrayList<>();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            String cadenasql = "SELECT F.ID_DETALLE_DTE FROM DETALLE_DTE_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia + " ORDER BY F.ID_DETALLE_DTE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                Long id_detalle_dte = rs.getLong(1);
                DetalleDTE_contingencia detalle_dte_contingencia = new DetalleDTE_contingencia();
                detalle_dte_contingencia.setNoItem(id_detalle_dte);
                detalle_dte_contingencia.setCodigoGeneracion(ctrl_base_datos.ObtenerString("SELECT F.CODIGOGENERACION FROM DETALLE_DTE_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia + " AND F.ID_DETALLE_DTE=" + id_detalle_dte, conn));
                detalle_dte_contingencia.setTipoDoc(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_002 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_002 FROM DETALLE_DTE_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contingencia + " AND F.ID_DETALLE_DTE=" + id_detalle_dte + ")", conn));
                resultado.add(detalle_dte_contingencia);
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_detalle_dte_contingencia_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_detalle_dte_contingencia_v3(String ambiente, String KCOO_JDE_1, String MCU_JDE, Long ID_EMISOR, Long id_contingencia, Connection conn) {
        String resultado;

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

            String cadenasql = "SELECT "
                    + "F.FEKCOO, "
                    + "F.FEDOCO, "
                    + "F.FEDCTO, "
                    + "F.FEDOC, "
                    + "F.FEDCT, "
                    + "F.FECRSREF02 "
                    + "FROM "
                    + esquema + ".F5542FEL@" + dblink + " F "
                    + "LEFT JOIN EMISOR_KCOO_V3 G ON (TRIM(F.FEKCOO)=TRIM(G.KCOO_JDE)) "
                    + "WHERE "
                    + "TRIM(F.FEKCOO)='" + KCOO_JDE_1 + "' AND "
                    + "TRIM(F.FEMCU)='" + MCU_JDE + "' AND "
                    + "TRIM(FESTCD)='CCC' AND "
                    + "F.FEDCTO IN ('NR','S3','SD','FE','S3','C3','EX') AND "
                    + "(TRIM(F.FECRSREF03)='-' OR F.FECRSREF03 IS NULL) AND "
                    + "G.ID_EMISOR=" + ID_EMISOR;
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            ResultSet rs = stmt.executeQuery(cadenasql);
            Long ID_DETALLE_DTE = Long.valueOf("0");
            while (rs.next()) {
                String KCOO_JDE = rs.getString(1);
                String DOCO_JDE = rs.getString(2);
                String DCTO_JDE = rs.getString(3);
                String DOC_JDE = rs.getString(4);
                String DCT_JDE = rs.getString(5);
                String CODIGOGENERACION = rs.getString(6);
                        
                cadenasql = "UPDATE " + esquema + ".F5542FEL@" + dblink + " "
                        + "SET FESTCD='99C' "
                        + "WHERE FEKCOO='" + KCOO_JDE + "' AND FEDOCO=" + DOCO_JDE + " AND FEDCTO='" + DCTO_JDE + "' AND FEDOC=" + DOC_JDE + " AND FEDCT='" + DCT_JDE + "'";
                Statement stmt1 = conn.createStatement();
                // System.out.println(cadenasql);
                stmt1.executeUpdate(cadenasql);
                stmt1.close();
                
                Long ID_CONTINGENCIA = id_contingencia;
                Long ID_CAT_002 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_002 C WHERE C.VALOR_JDE='" + DCTO_JDE + "'", conn);
                
                ID_DETALLE_DTE++;
                cadenasql = "INSERT INTO DETALLE_DTE_CONTINGENCIA_V3 ("
                        + "ID_CONTINGENCIA, "
                        + "ID_DETALLE_DTE, "
                        + "CODIGOGENERACION, "
                        + "ID_CAT_002) VALUES ("
                        + ID_CONTINGENCIA + ","
                        + ID_DETALLE_DTE + ",'"
                        + CODIGOGENERACION + "',"
                        + ID_CAT_002 + ")";
                stmt1 = conn.createStatement();
                // System.out.println(cadenasql);
                stmt1.executeUpdate(cadenasql);
                stmt1.close();
            }
            rs.close();
            stmt.close();
            
            resultado = ID_DETALLE_DTE.toString();
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_detalle_dte_contingencia_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
