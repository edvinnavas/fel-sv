package Controladores;

import Entidades.Resumen_cr;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DecimalFormat;

public class Ctrl_Resumen_CR_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Resumen_CR_V3() {
    }

    public Resumen_cr obtener_resumen_cr_v3(Long id_dte, Connection conn) {
        Resumen_cr resultado = new Resumen_cr();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setTotalSujetoRetencion(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALSUJETORETENCION FROM RESUMEN_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalIVAretenido(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALIVARETENIDO FROM RESUMEN_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalIVAretenidoLetras(ctrl_base_datos.ObtenerString("SELECT F.TOTALIVARETENIDOLETRAS FROM RESUMEN_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_resumen_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_resumen_jde_cr_v3(Long id_dte, String ambiente, Connection conn) {
        String resultado = "";

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            Driver driver = new Driver();

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
            Long ID_RESUMEN = Long.valueOf("1");
            Number TOTALSUJETORETENCION = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.MONTOSUJETOGRAV) FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number TOTALIVARETENIDO = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.IVARETENIDO) FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Long TOTALIVARETENIDO_LONG = TOTALIVARETENIDO.longValue();
            Double TOTALIVARETENIDO_DOUBLE = TOTALIVARETENIDO.doubleValue();
            String[] NUMERO_PARTES = TOTALIVARETENIDO.toString().split("\\.");
            if (NUMERO_PARTES[1] != null) {
                if (NUMERO_PARTES[1].length() > 2) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    NUMERO_PARTES[1] = decimalFormat.format(TOTALIVARETENIDO_DOUBLE - TOTALIVARETENIDO_LONG);
                    NUMERO_PARTES[1] = NUMERO_PARTES[1].substring(1, NUMERO_PARTES[1].length());
                } else {
                    if (NUMERO_PARTES[1].length() == 1) {
                        NUMERO_PARTES[1] = NUMERO_PARTES[1] + "0";
                    }
                }
            } else {
                NUMERO_PARTES[1] = "00";
            }
            String TOTALIVARETENIDOLETRAS = driver.cantidadConLetra(TOTALIVARETENIDO_LONG.toString()).toUpperCase() + " DOLARES CON " + NUMERO_PARTES[1] + "/100";
                    
            String cadenasql = "INSERT INTO RESUMEN_CR_V3 ("
                    + "ID_DTE, "
                    + "ID_RESUMEN, "
                    + "TOTALSUJETORETENCION, "
                    + "TOTALIVARETENIDO, "
                    + "TOTALIVARETENIDOLETRAS) VALUES ("
                    + ID_DTE + ","
                    + ID_RESUMEN + ","
                    + TOTALSUJETORETENCION + ","
                    + TOTALIVARETENIDO + ",'"
                    + TOTALIVARETENIDOLETRAS + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_resumen_jde_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
