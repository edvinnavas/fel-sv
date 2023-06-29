package Controladores;

import Entidades.Pagos;
import Entidades.Resumen_fex;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

public class Ctrl_Resumen_FEX_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Resumen_FEX_V3() {
    }

    public Resumen_fex obtener_resumen_fex_v3(Long id_dte, Connection conn) {
        Resumen_fex resultado = new Resumen_fex();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setTotalGravada(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALGRAVADA FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDescuento(ctrl_base_datos.ObtenerDouble("SELECT F.DESCUENTO FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setPorcentajeDescuento(ctrl_base_datos.ObtenerDouble("SELECT F.PORCENTAJEDESCUENTO FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalDescu(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALDESCU FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setSeguro(ctrl_base_datos.ObtenerDouble("SELECT F.SEGURO FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setFlete(ctrl_base_datos.ObtenerDouble("SELECT F.FLETE FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setMontoTotalOperacion(ctrl_base_datos.ObtenerDouble("SELECT F.MONTOTOTALOPERACION FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalNoGravado(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALNOGRAVADO FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalPagar(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALPAGAR FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalLetras(ctrl_base_datos.ObtenerString("SELECT F.TOTALLETRAS FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCondicionOperacion(ctrl_base_datos.ObtenerLong("SELECT C.CODIGO FROM CAT_016 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_016 FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            
            Pagos pagos = new Pagos();
            pagos.setCodigo(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_017 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_017 FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            pagos.setMontoPago(ctrl_base_datos.ObtenerDouble("SELECT F.PAGOS_MONTOPAGO FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            pagos.setReferencia(ctrl_base_datos.ObtenerString("SELECT F.PAGOS_REFERENCIA FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            pagos.setPlazo(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_018 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_018 FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            pagos.setPeriodo(ctrl_base_datos.ObtenerDouble("SELECT F.PAGOS_PERIODO FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            pagos = null;
            resultado.setPagos(pagos);
            
            resultado.setCodIncoterms(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_031 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_031 FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setDescIncoterms(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_031 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_031 FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNumPagoElectronico(ctrl_base_datos.ObtenerString("SELECT F.NUMPAGOELECTRONICO FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setObservaciones(ctrl_base_datos.ObtenerString("SELECT F.OBSERVACIONES FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_resumen_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_resumen_jde_fex_v3(Long id_dte, String ambiente, Connection conn) {
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
            Number TOTALGRAVADA = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.VENTAGRAVADA) FROM CUERPO_DOCU_FEX_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number DESCUENTO = 0.00;
            Number PORCENTAJEDESCUENTO = 0.00;
            Number TOTALDESCU = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.MONTODESCU) FROM CUERPO_DOCU_FEX_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number SEGURO = 0.00;
            Number FLETE = 0.00;
            Number MONTOTOTALOPERACION = TOTALGRAVADA.doubleValue() - TOTALDESCU.doubleValue() + SEGURO.doubleValue() + FLETE.doubleValue();
            Number TOTALNOGRAVADO = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.NOGRAVADO) FROM CUERPO_DOCU_FEX_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Number TOTALPAGAR = MONTOTOTALOPERACION.doubleValue() + TOTALNOGRAVADO.doubleValue();
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
            Long ID_CAT_016 = Long.valueOf("2");
            Long ID_CAT_017 = null;
            Number PAGOS_MONTOPAGO = null;
            String PAGOS_REFERENCIA = null;
            Long ID_CAT_018 = Long.valueOf("1");
            String AN8_JDE = ctrl_base_datos.ObtenerString("SELECT F.AN8_JDE FROM DTE_FEX_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            String dias_credito = ctrl_base_datos.ObtenerString("SELECT F.AITRAR FROM " + esquema + ".F03012@" + dblink + " F WHERE F.AIAN8=" + AN8_JDE, conn);
            Integer no_dias_credito;
            try {
                no_dias_credito = Integer.valueOf(dias_credito.trim());
            } catch(NumberFormatException ex) {
                System.out.println("DIAS CREDITO: 0");
                no_dias_credito = 0;
            }
            Number PAGOS_PERIODO = no_dias_credito;
            Long ID_CAT_031 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_031 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC16) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + AN8_JDE + ")", conn);
            if (ID_CAT_031 == null) {
                ID_CAT_031 = Long.valueOf("1");
            }
            String NUMPAGOELECTRONICO = null;
            String OBSERVACIONES = "Factura de Exportación";
            String cadenasql = "INSERT INTO RESUMEN_FEX_V3 ("
                    + "ID_DTE, "
                    + "ID_RESUMEN, "
                    + "TOTALGRAVADA, "
                    + "DESCUENTO, "
                    + "PORCENTAJEDESCUENTO, "
                    + "TOTALDESCU, "
                    + "SEGURO, "
                    + "FLETE, "
                    + "MONTOTOTALOPERACION, "
                    + "TOTALNOGRAVADO, "
                    + "TOTALPAGAR, "
                    + "TOTALLETRAS, "
                    + "ID_CAT_016, "
                    + "ID_CAT_017, "
                    + "PAGOS_MONTOPAGO, "
                    + "PAGOS_REFERENCIA, "
                    + "ID_CAT_018, "
                    + "PAGOS_PERIODO, "
                    + "ID_CAT_031, "
                    + "NUMPAGOELECTRONICO, "
                    + "OBSERVACIONES) VALUES ("
                    + ID_DTE + ","
                    + ID_RESUMEN + ","
                    + TOTALGRAVADA + ","
                    + DESCUENTO + ","
                    + PORCENTAJEDESCUENTO + ","
                    + TOTALDESCU + ","
                    + SEGURO + ","
                    + FLETE + ","
                    + MONTOTOTALOPERACION + ","
                    + TOTALNOGRAVADO + ","
                    + TOTALPAGAR + ",'"
                    + TOTALLETRAS + "',"
                    + ID_CAT_016 + ","
                    + ID_CAT_017 + ","
                    + PAGOS_MONTOPAGO + ",'"
                    + PAGOS_REFERENCIA + "',"
                    + ID_CAT_018 + ","
                    + PAGOS_PERIODO + ","
                    + ID_CAT_031 + ","
                    + NUMPAGOELECTRONICO + ",'" 
                    + OBSERVACIONES + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            cadenasql = "SELECT F.ID_CAT_015, SUM(F.VALOR) VALOR FROM CUERPO_TRIBUTO_FEX_V3 F WHERE F.ID_CAT_015 NOT IN (18) AND F.ID_DTE=" + ID_DTE + " GROUP BY F.ID_CAT_015";
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            Long NUM_TRIBUTO = Long.valueOf("0");
            while (rs.next()) {
                NUM_TRIBUTO++;
                Long ID_CAT_015 = rs.getLong(1);
                Number VALOR = rs.getDouble(2);

                cadenasql = "INSERT INTO RESUMEN_TRIBUTO_FEX_V3 ("
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
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_resumen_jde_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
