package Controladores;

import Entidades.Extension_nc;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_Extension_NC_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Extension_NC_V3() {
    }

    public Extension_nc obtener_extension_nc_v3(Long id_dte, Connection conn) {
        Extension_nc resultado = new Extension_nc();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            resultado.setNombEntrega(ctrl_base_datos.ObtenerString("SELECT F.NOMBENTREGA FROM EXTENSION_NC_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDocuEntrega(ctrl_base_datos.ObtenerString("SELECT F.DOCUENTREGA FROM EXTENSION_NC_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNombRecibe(ctrl_base_datos.ObtenerString("SELECT F.NOMBRECIBE FROM EXTENSION_NC_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDocuRecibe(ctrl_base_datos.ObtenerString("SELECT F.DOCURECIBE FROM EXTENSION_NC_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setObservaciones(ctrl_base_datos.ObtenerString("SELECT F.OBSERVACIONES FROM EXTENSION_NC_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_extension_nc_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    public String extraer_extension_jde_nc_v3(Long id_dte, String ambiente, String AN8_JDE, Connection conn) {
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
            
            Long ID_DTE = id_dte;
            Long ID_EXTENSION = Long.valueOf("1");
            String NOMBENTREGA = "UNO EL SALVADOR";
            String DOCUENTREGA = "06140404600015";
            
            String NOMBRECIBE = ctrl_base_datos.ObtenerString("SELECT TRIM(F.WWMLNM) NOMBRECIBE FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S'", conn);
            if (NOMBRECIBE == null) {
                NOMBRECIBE = "Sin registro";
            }
            String DOCURECIBE = ctrl_base_datos.ObtenerString("SELECT TRIM(F.WWNICK) DOCURECIBE FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S'", conn);
            if (DOCURECIBE == null) {
                DOCURECIBE = "Sin registro";
            }
            
            String OBSERVACIONES = "Fase de pruebas.";
            
            String cadenasql = "INSERT INTO EXTENSION_NC_V3 ("
                    + "ID_DTE, "
                    + "ID_EXTENSION, "
                    + "NOMBENTREGA, "
                    + "DOCUENTREGA, "
                    + "NOMBRECIBE, "
                    + "DOCURECIBE, "
                    + "OBSERVACIONES) VALUES ("
                    + ID_DTE + ","
                    + ID_EXTENSION + ",'"
                    + NOMBENTREGA + "','"
                    + DOCUENTREGA + "','"
                    + NOMBRECIBE + "','"
                    + DOCURECIBE + "','"
                    + OBSERVACIONES + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_extension_jde_nc_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
