package Controladores;

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

            String CODIGO_CLIENTE = ctrl_base_datos.ObtenerString("SELECT F.AN8_JDE FROM DTE_CR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            String NUMERO_ORDEN = ctrl_base_datos.ObtenerString("SELECT F.DCTO_JDE || '-' || F.DOCO_JDE FROM DTE_CR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            String NUMERO_DOCUMENTO = ctrl_base_datos.ObtenerString("SELECT F.DCT_JDE || '-' || F.DOC_JDE FROM DTE_CR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);

            Long ID_APENDICE = Long.valueOf("1");
            String CAMPO = "Apendice-1";
            String ETIQUETA = "Código cliente";
            String VALOR = CODIGO_CLIENTE;

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
            CAMPO = "Apendice-2";
            ETIQUETA = "No. orden";
            VALOR = NUMERO_ORDEN;

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
            CAMPO = "Apendice-3";
            ETIQUETA = "No. documento";
            VALOR = NUMERO_DOCUMENTO;

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
