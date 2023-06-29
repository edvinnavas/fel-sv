package Controladores;

import ClienteServicio.Cliente_Rest_JDE;
import Entidades.Apendice_cr;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_Apendice_CR_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Apendice_CR_V3() {
    }

    public List<Apendice_cr> obtener_apendice_cr_v3(Long id_dte, Connection conn) {
        List<Apendice_cr> resultado = new ArrayList<>();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            String cadenasql = "SELECT F.ID_APENDICE FROM APENDICE_CR_V3 F WHERE F.ID_DTE=" + id_dte + " ORDER BY F.ID_APENDICE"; 
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while(rs.next()) {
                Long id_apendice = rs.getLong(1);
                Apendice_cr apendice_cr = new Apendice_cr();
                apendice_cr.setCampo(ctrl_base_datos.ObtenerString("SELECT F.CAMPO FROM APENDICE_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_APENDICE=" + id_apendice, conn));
                apendice_cr.setEtiqueta(ctrl_base_datos.ObtenerString("SELECT F.ETIQUETA FROM APENDICE_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_APENDICE=" + id_apendice, conn));
                String valor = ctrl_base_datos.ObtenerString("SELECT F.VALOR FROM APENDICE_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_APENDICE=" + id_apendice, conn);
                if(valor.length() > 150) {
                    valor = valor.substring(0, 149);
                }
                apendice_cr.setValor(valor);
                resultado.add(apendice_cr);
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_apendice_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    public String extraer_apendice_jde_cr_v3(Long id_dte, String ambiente, String DOCO_JDE, String DCTO_JDE, String MCU_JDE, Connection conn) {
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
            
            Long ID_APENDICE = Long.valueOf("1");
            String CAMPO = "SELLOS";
            String ETIQUETA = "SELLOS DE SEGURIDAD";
            String TDLDNM = ctrl_base_datos.ObtenerString("SELECT DISTINCT F.TDLDNM FROM " + esquema + ".F49621@" + dblink + " F WHERE F.TDDCTO='" + DCTO_JDE + "' AND F.TDDOCO=" + DOCO_JDE + " AND TRIM(F.TDVMCU)='" + MCU_JDE + "'", conn);
            if(TDLDNM == null) {
                TDLDNM = "0";
            }
            String VALOR = ctrl_base_datos.ObtenerString("SELECT DISTINCT T.SELLOS FROM (SELECT (LISTAGG(F.SUSLN, ', ') WITHIN GROUP (ORDER BY F.SUSLN) OVER (PARTITION BY F.SULDNM, F.SUVMCU)) SELLOS FROM " + esquema + ".F49380@" + dblink + " F  WHERE F.SULDNM='" + TDLDNM + "' AND TRIM(F.SUVMCU)='" + MCU_JDE + "') T", conn);
            if(VALOR == null) {
                VALOR = "-";
            }
            
            String cadenasql = "INSERT INTO APENDICE_CR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            ID_APENDICE = Long.valueOf("2");
            CAMPO = "NO_CONTRATO";
            ETIQUETA = "NO CONTRATO";
            String AN8_JDE = ctrl_base_datos.ObtenerString("SELECT F.AN8_JDE FROM DTE_CR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            VALOR = ctrl_base_datos.ObtenerString("SELECT TRIM(F.WWATTL) FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S'", conn);
            if(VALOR == null) {
                VALOR = "-";
            }
            
            cadenasql = "INSERT INTO APENDICE_CR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            ID_APENDICE = Long.valueOf("3");
            CAMPO = "BOLETAS";
            ETIQUETA = "BOLETAS";
            String KCOO_JDE = ctrl_base_datos.ObtenerString("SELECT F.KCOO_JDE FROM DTE_CR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Cliente_Rest_JDE cliente_rest_jde = new Cliente_Rest_JDE();
            VALOR = cliente_rest_jde.obetener_texto_encabezado_orden_ventas("PET", ambiente, DOCO_JDE, DCTO_JDE, KCOO_JDE);
            if(VALOR == null) {
                VALOR = "SIN REGISTRO.";
            }
            VALOR = VALOR.replaceAll("\"", "");
            if(VALOR == null) {
                VALOR = "SIN REGISTRO.";
            }
            if(VALOR.trim().equals("")) {
                VALOR = "SIN REGISTRO.";
            }
            cadenasql = "INSERT INTO APENDICE_CR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_apendice_jde_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
}
