package Controladores;

import Entidades.Resumen_nd;
import Entidades.Tributo;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_Resumen_ND_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Resumen_ND_V3() {
    }

    public Resumen_nd obtener_resumen_nd_v3(Long id_dte, Connection conn) {
        Resumen_nd resultado = new Resumen_nd();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setTotalNoSuj(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALNOSUJ FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalExenta(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALEXENTA FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalGravada(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALGRAVADA FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setSubTotalVentas(ctrl_base_datos.ObtenerDouble("SELECT F.SUBTOTALVENTAS FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDescuNoSuj(ctrl_base_datos.ObtenerDouble("SELECT F.DESCUNOSUJ FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDescuExenta(ctrl_base_datos.ObtenerDouble("SELECT F.DESCUEXENTA FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDescuGravada(ctrl_base_datos.ObtenerDouble("SELECT F.DESCUGRAVADA FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalDescu(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALDESCU FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));

            List<Tributo> tributos = new ArrayList<>();
            Long id_resumen = ctrl_base_datos.ObtenerLong("SELECT F.ID_RESUMEN FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            String cadenasql = "SELECT F.NUM_TRIBUTO FROM RESUMEN_TRIBUTO_ND_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_RESUMEN=" + id_resumen + " ORDER BY F.NUM_TRIBUTO";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                Long num_tributo = rs.getLong(1);
                Tributo tributo = new Tributo();
                tributo.setCodigo(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_015 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_015 FROM RESUMEN_TRIBUTO_ND_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_RESUMEN=" + id_resumen + " AND F.NUM_TRIBUTO=" + num_tributo + ")", conn));
                tributo.setDescripcion(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_015 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_015 FROM RESUMEN_TRIBUTO_ND_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_RESUMEN=" + id_resumen + " AND F.NUM_TRIBUTO=" + num_tributo + ")", conn));
                tributo.setValor(ctrl_base_datos.ObtenerDouble("SELECT F.VALOR FROM RESUMEN_TRIBUTO_ND_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_RESUMEN=" + id_resumen + " AND F.NUM_TRIBUTO=" + num_tributo, conn));
                tributos.add(tributo);
            }
            rs.close();
            stmt.close();

            if (tributos.isEmpty()) {
                tributos = null;
            }
            resultado.setTributos(tributos);

            resultado.setSubTotal(ctrl_base_datos.ObtenerDouble("SELECT F.SUBTOTAL FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setIvaPerci1(ctrl_base_datos.ObtenerDouble("SELECT F.IVAPERCI1 FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setIvaRete1(ctrl_base_datos.ObtenerDouble("SELECT F.IVARETE1 FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setReteRenta(ctrl_base_datos.ObtenerDouble("SELECT F.RETERENTA FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setMontoTotalOperacion(ctrl_base_datos.ObtenerDouble("SELECT F.MONTOTOTALOPERACION FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalLetras(ctrl_base_datos.ObtenerString("SELECT F.TOTALLETRAS FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCondicionOperacion(ctrl_base_datos.ObtenerLong("SELECT C.CODIGO FROM CAT_016 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_016 FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNumPagoElectronico(ctrl_base_datos.ObtenerString("SELECT F.NUMPAGOELECTRONICO FROM RESUMEN_ND_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_resumen_nd_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_resumen_jde_nd_v3(Long id_dte, String ambiente, Connection conn) {
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
            Number TOTALNOSUJ = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.VENTANOSUJ) FROM CUERPO_DOCU_ND_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number TOTALEXENTA = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.VENTAEXENTA) FROM CUERPO_DOCU_ND_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number TOTALGRAVADA = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.VENTAGRAVADA) FROM CUERPO_DOCU_ND_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number SUBTOTALVENTAS = TOTALNOSUJ.doubleValue() + TOTALEXENTA.doubleValue() + TOTALGRAVADA.doubleValue();
            Number DESCUNOSUJ = 0.00;
            Number DESCUEXENTA = 0.00;
            Number DESCUGRAVADA = 0.00;
            Number TOTALDESCU = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.MONTODESCU) FROM CUERPO_DOCU_ND_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number SUBTOTAL = SUBTOTALVENTAS.doubleValue();
            Number IVAPERCI1 = ctrl_base_datos.ObtenerDouble("SELECT ROUND(NVL(SUM(F.VALOR),0),2) VALOR FROM CUERPO_TRIBUTO_ND_V3 F WHERE F.ID_CAT_015=18 AND F.ID_DTE=" + ID_DTE, conn);
            Number IVARETE1 = 0.00;
            Number RETERENTA = 0.00;
            Number MONTOTOTALOPERACION = SUBTOTAL.doubleValue() + ctrl_base_datos.ObtenerDouble("SELECT ROUND(SUM(F.VALOR),2) FROM CUERPO_TRIBUTO_ND_V3 F WHERE F.ID_CAT_015 NOT IN (18) AND F.ID_DTE=" + ID_DTE, conn);
            MONTOTOTALOPERACION = MONTOTOTALOPERACION.doubleValue() + IVAPERCI1.doubleValue() + IVARETE1.doubleValue();
            Long MONTOTOTALOPERACION_LONG = MONTOTOTALOPERACION.longValue();
            Double MONTOTOTALOPERACION_DOUBLE = MONTOTOTALOPERACION.doubleValue();
            String[] NUMERO_PARTES = MONTOTOTALOPERACION_DOUBLE.toString().split("\\.");
            if (NUMERO_PARTES[1] != null) {
                if (NUMERO_PARTES[1].length() > 2) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    NUMERO_PARTES[1] = decimalFormat.format(MONTOTOTALOPERACION_DOUBLE - MONTOTOTALOPERACION_LONG);
                    NUMERO_PARTES[1] = NUMERO_PARTES[1].substring(1, NUMERO_PARTES[1].length());
                } else {
                    if (NUMERO_PARTES[1].length() == 1) {
                        NUMERO_PARTES[1] = NUMERO_PARTES[1] + "0";
                    }
                }
            } else {
                NUMERO_PARTES[1] = "00";
            }
            String TOTALLETRAS = driver.cantidadConLetra(MONTOTOTALOPERACION_LONG.toString()).toUpperCase() + " DOLARES CON " + NUMERO_PARTES[1] + "/100";
            Long ID_CAT_016 = Long.valueOf("2");
            String NUMPAGOELECTRONICO = null;

            String cadenasql = "INSERT INTO RESUMEN_ND_V3 ("
                    + "ID_DTE, "
                    + "ID_RESUMEN, "
                    + "TOTALNOSUJ, "
                    + "TOTALEXENTA, "
                    + "TOTALGRAVADA, "
                    + "SUBTOTALVENTAS, "
                    + "DESCUNOSUJ, "
                    + "DESCUEXENTA, "
                    + "DESCUGRAVADA, "
                    + "TOTALDESCU, "
                    + "SUBTOTAL, "
                    + "IVAPERCI1, "
                    + "IVARETE1, "
                    + "RETERENTA, "
                    + "MONTOTOTALOPERACION, "
                    + "TOTALLETRAS, "
                    + "ID_CAT_016, "
                    + "NUMPAGOELECTRONICO) VALUES ("
                    + ID_DTE + ","
                    + ID_RESUMEN + ","
                    + TOTALNOSUJ + ","
                    + TOTALEXENTA + ","
                    + TOTALGRAVADA + ","
                    + SUBTOTALVENTAS + ","
                    + DESCUNOSUJ + ","
                    + DESCUEXENTA + ","
                    + DESCUGRAVADA + ","
                    + TOTALDESCU + ","
                    + SUBTOTAL + ","
                    + IVAPERCI1 + ","
                    + IVARETE1 + ","
                    + RETERENTA + ","
                    + MONTOTOTALOPERACION + ",'"
                    + TOTALLETRAS + "',"
                    + ID_CAT_016 +  ","
                    + NUMPAGOELECTRONICO + ")";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            cadenasql = "SELECT F.ID_CAT_015, SUM(F.VALOR) VALOR FROM CUERPO_TRIBUTO_ND_V3 F WHERE F.ID_CAT_015 NOT IN (18) AND F.ID_DTE=" + ID_DTE + " GROUP BY F.ID_CAT_015";
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            Long NUM_TRIBUTO = Long.valueOf("0");
            while (rs.next()) {
                NUM_TRIBUTO++;
                Long ID_CAT_015 = rs.getLong(1);
                Number VALOR = rs.getDouble(2);

                cadenasql = "INSERT INTO RESUMEN_TRIBUTO_ND_V3 ("
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
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_resumen_jde_nd_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
