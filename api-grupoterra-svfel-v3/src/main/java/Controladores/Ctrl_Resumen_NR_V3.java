package Controladores;

import Entidades.Resumen_nr;
import Entidades.Tributo;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_Resumen_NR_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Resumen_NR_V3() {
    }

    public Resumen_nr obtener_resumen_nr_v3(Long id_dte, Connection conn) {
        Resumen_nr resultado = new Resumen_nr();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setTotalNoSuj(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALNOSUJ FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalExenta(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALEXENTA FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalGravada(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALGRAVADA FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setSubTotalVentas(ctrl_base_datos.ObtenerDouble("SELECT F.SUBTOTALVENTAS FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDescuNoSuj(ctrl_base_datos.ObtenerDouble("SELECT F.DESCUNOSUJ FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDescuExenta(ctrl_base_datos.ObtenerDouble("SELECT F.DESCUEXENTA FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDescuGravada(ctrl_base_datos.ObtenerDouble("SELECT F.DESCUGRAVADA FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setPorcentajeDescuento(ctrl_base_datos.ObtenerDouble("SELECT F.PORCENTAJEDESCUENTO FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalDescu(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALDESCU FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));

            List<Tributo> tributos = new ArrayList<>();
            Long id_resumen = ctrl_base_datos.ObtenerLong("SELECT F.ID_RESUMEN FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            String cadenasql = "SELECT F.NUM_TRIBUTO FROM RESUMEN_TRIBUTO_NR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_RESUMEN=" + id_resumen + " ORDER BY F.NUM_TRIBUTO";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                Long num_tributo = rs.getLong(1);
                Tributo tributo = new Tributo();
                tributo.setCodigo(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_015 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_015 FROM RESUMEN_TRIBUTO_NR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_RESUMEN=" + id_resumen + " AND F.NUM_TRIBUTO=" + num_tributo + ")", conn));
                tributo.setDescripcion(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_015 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_015 FROM RESUMEN_TRIBUTO_NR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_RESUMEN=" + id_resumen + " AND F.NUM_TRIBUTO=" + num_tributo + ")", conn));
                tributo.setValor(ctrl_base_datos.ObtenerDouble("SELECT F.VALOR FROM RESUMEN_TRIBUTO_NR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_RESUMEN=" + id_resumen + " AND F.NUM_TRIBUTO=" + num_tributo, conn));
                tributos.add(tributo);
            }
            rs.close();
            stmt.close();

            if (tributos.isEmpty()) {
                tributos = null;
            }
            resultado.setTributos(tributos);

            resultado.setSubTotal(ctrl_base_datos.ObtenerDouble("SELECT F.SUBTOTAL FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setMontoTotalOperacion(ctrl_base_datos.ObtenerDouble("SELECT F.MONTOTOTALOPERACION FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalLetras(ctrl_base_datos.ObtenerString("SELECT F.TOTALLETRAS FROM RESUMEN_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_resumen_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_resumen_jde_nr_v3(Long id_dte, String ambiente, Connection conn) {
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
            Number TOTALNOSUJ = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.VENTANOSUJ) FROM CUERPO_DOCU_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number TOTALEXENTA = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.VENTAEXENTA) FROM CUERPO_DOCU_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number TOTALGRAVADA = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.VENTAGRAVADA) FROM CUERPO_DOCU_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number SUBTOTALVENTAS = TOTALNOSUJ.doubleValue() + TOTALEXENTA.doubleValue() + TOTALGRAVADA.doubleValue();
            Number DESCUNOSUJ = 0.00;
            Number DESCUEXENTA = 0.00;
            Number DESCUGRAVADA = 0.00;
            Number PORCENTAJEDESCUENTO = 0.00;
            Number TOTALDESCU = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.MONTODESCU) FROM CUERPO_DOCU_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number SUBTOTAL = SUBTOTALVENTAS.doubleValue();
            Number MONTOTOTALOPERACION = SUBTOTAL.doubleValue() + ctrl_base_datos.ObtenerDouble("SELECT ROUND(SUM(F.VALOR),2) FROM CUERPO_TRIBUTO_NR_V3 F WHERE F.ID_CAT_015 NOT IN (18) AND F.ID_DTE=" + ID_DTE, conn);
            Number TOTALPAGAR = MONTOTOTALOPERACION.doubleValue();
            Long TOTALPAGAR_LONG = TOTALPAGAR.longValue();
            Double TOTALPAGAR_DOUBLE = TOTALPAGAR.doubleValue();
            String[] NUMERO_PARTES = TOTALPAGAR_DOUBLE.toString().split("\\.");
            if (NUMERO_PARTES[1] != null) {
                if (NUMERO_PARTES[1].length() > 2) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    NUMERO_PARTES[1] = decimalFormat.format(TOTALPAGAR_DOUBLE - TOTALPAGAR_LONG);
                    NUMERO_PARTES[1] = NUMERO_PARTES[1].substring(1, NUMERO_PARTES[1].length());
                } else {
                    if (NUMERO_PARTES[1].length() == 1) {
                        NUMERO_PARTES[1] = NUMERO_PARTES[1] + "0";
                    }
                }
            } else {
                NUMERO_PARTES[1] = "00";
            }
            String TOTALLETRAS = driver.cantidadConLetra(TOTALPAGAR_LONG.toString()).toUpperCase() + " DOLARES CON " + NUMERO_PARTES[1] + "/100";

            String cadenasql = "INSERT INTO RESUMEN_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_RESUMEN, "
                    + "TOTALNOSUJ, "
                    + "TOTALEXENTA, "
                    + "TOTALGRAVADA, "
                    + "SUBTOTALVENTAS, "
                    + "DESCUNOSUJ, "
                    + "DESCUEXENTA, "
                    + "DESCUGRAVADA, "
                    + "PORCENTAJEDESCUENTO, "
                    + "TOTALDESCU, "
                    + "SUBTOTAL, "
                    + "MONTOTOTALOPERACION, "
                    + "TOTALLETRAS) VALUES ("
                    + ID_DTE + ","
                    + ID_RESUMEN + ","
                    + TOTALNOSUJ + ","
                    + TOTALEXENTA + ","
                    + TOTALGRAVADA + ","
                    + SUBTOTALVENTAS + ","
                    + DESCUNOSUJ + ","
                    + DESCUEXENTA + ","
                    + DESCUGRAVADA + ","
                    + PORCENTAJEDESCUENTO + ","
                    + TOTALDESCU + ","
                    + SUBTOTAL + ","
                    + MONTOTOTALOPERACION + ",'"
                    + TOTALLETRAS + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            cadenasql = "SELECT F.ID_CAT_015, SUM(F.VALOR) VALOR FROM CUERPO_TRIBUTO_NR_V3 F WHERE F.ID_CAT_015 NOT IN (18) AND F.ID_DTE=" + ID_DTE + " GROUP BY F.ID_CAT_015";
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            Long NUM_TRIBUTO = Long.valueOf("0");
            while (rs.next()) {
                NUM_TRIBUTO++;
                Long ID_CAT_015 = rs.getLong(1);
                Number VALOR = rs.getDouble(2);

                cadenasql = "INSERT INTO RESUMEN_TRIBUTO_NR_V3 ("
                        + "ID_DTE, "
                        + "ID_RESUMEN, "
                        + "NUM_TRIBUTO, "
                        + "ID_CAT_015, "
                        + "VALOR) VALUES ("
                        + ID_DTE + ","
                        + ID_RESUMEN + ","
                        + NUM_TRIBUTO + ","
                        + ID_CAT_015 + ","
                        + VALOR + ")";
                Statement stmt1 = conn.createStatement();
                // System.out.println(cadenasql);
                stmt1.executeUpdate(cadenasql);
                stmt1.close();
            }
            rs.close();
            stmt.close();

            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_resumen_jde_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
