package Controladores;

import Entidades.Extension_fex;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_Extension_FEX_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Extension_FEX_V3() {
    }

    public Extension_fex obtener_extension_fex_v3(Long id_dte, Connection conn) {
        Extension_fex resultado = new Extension_fex();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            resultado.setNombEntrega(ctrl_base_datos.ObtenerString("SELECT F.NOMBENTREGA FROM EXTENSION_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDocuEntrega(ctrl_base_datos.ObtenerString("SELECT F.DOCUENTREGA FROM EXTENSION_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNombRecibe(ctrl_base_datos.ObtenerString("SELECT F.NOMBRECIBE FROM EXTENSION_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDocuRecibe(ctrl_base_datos.ObtenerString("SELECT F.DOCURECIBE FROM EXTENSION_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setObservaciones(ctrl_base_datos.ObtenerString("SELECT F.OBSERVACIONES FROM EXTENSION_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setPlacaVehiculo(ctrl_base_datos.ObtenerString("SELECT F.PLACAVEHICULO FROM EXTENSION_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_extension_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    public String extraer_extension_jde_fex_v3(Long id_dte, String ambiente, String AN8_JDE, Connection conn) {
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
            String NOMBENTREGA = ctrl_base_datos.ObtenerString("SELECT F.NOMBENTREGA FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA IN (SELECT F.MCU_JDE FROM DTE_FEX_V3 F WHERE F.ID_DTE=" + ID_DTE + ")", conn);
            String DOCUENTREGA = ctrl_base_datos.ObtenerString("SELECT F.DOCUENTREGA FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA IN (SELECT F.MCU_JDE FROM DTE_FEX_V3 F WHERE F.ID_DTE=" + ID_DTE + ")", conn);
            
            String NOMBRECIBE = ctrl_base_datos.ObtenerString("SELECT TRIM(F.WWMLNM) NOMBRECIBE FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S'", conn);
            if (NOMBRECIBE == null) {
                NOMBRECIBE = "Sin registro";
            }
            String DOCURECIBE = ctrl_base_datos.ObtenerString("SELECT TRIM(F.WWNICK) DOCURECIBE FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S'", conn);
            if (DOCURECIBE == null) {
                DOCURECIBE = "Sin registro";
            }
            
            String OBSERVACIONES = "Sin registro.";
            String PLACAVEHICULO = null;
            
            String cadenasql = "INSERT INTO EXTENSION_FEX_V3 ("
                    + "ID_DTE, "
                    + "ID_EXTENSION, "
                    + "NOMBENTREGA, "
                    + "DOCUENTREGA, "
                    + "NOMBRECIBE, "
                    + "DOCURECIBE, "
                    + "OBSERVACIONES, "
                    + "PLACAVEHICULO) VALUES ("
                    + ID_DTE + ","
                    + ID_EXTENSION + ",'"
                    + NOMBENTREGA + "','"
                    + DOCUENTREGA + "','"
                    + NOMBRECIBE + "','"
                    + DOCURECIBE + "','"
                    + OBSERVACIONES + "',"
                    + PLACAVEHICULO + ")";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_extension_jde_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
