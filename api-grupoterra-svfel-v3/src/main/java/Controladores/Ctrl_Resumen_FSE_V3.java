package Controladores;

import Entidades.Pagos;
import Entidades.Resumen_fse;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DecimalFormat;

public class Ctrl_Resumen_FSE_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Resumen_FSE_V3() {
    }

    public Resumen_fse obtener_resumen_fse_v3(Long id_dte, Connection conn) {
        Resumen_fse resultado = new Resumen_fse();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setTotalCompra(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALCOMPRA FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDescu(ctrl_base_datos.ObtenerDouble("SELECT F.DESCU FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalDescu(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALDESCU FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setSubTotal(ctrl_base_datos.ObtenerDouble("SELECT F.SUBTOTAL FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setIvaRete1(ctrl_base_datos.ObtenerDouble("SELECT F.IVARETEL FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setReteRenta(ctrl_base_datos.ObtenerDouble("SELECT F.RETERENTA FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalPagar(ctrl_base_datos.ObtenerDouble("SELECT F.TOTALPAGAR FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTotalLetras(ctrl_base_datos.ObtenerString("SELECT F.TOTALLETRAS FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCondicionOperacion(ctrl_base_datos.ObtenerLong("SELECT C.CODIGO FROM CAT_016 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_016 FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));

            Pagos pagos = new Pagos();
            pagos.setCodigo(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_017 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_017 FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            pagos.setMontoPago(ctrl_base_datos.ObtenerDouble("SELECT F.PAGOS_MONTOPAGO FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            pagos.setReferencia(ctrl_base_datos.ObtenerString("SELECT F.PAGOS_REFERENCIA FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            pagos.setPlazo(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_018 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_018 FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            pagos.setPeriodo(ctrl_base_datos.ObtenerDouble("SELECT F.PAGOS_PERIODO FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            pagos = null;
            resultado.setPagos(pagos);
            
            resultado.setObservaciones(ctrl_base_datos.ObtenerString("SELECT F.OBSERVACIONES FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_resumen_fse_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_resumen_jde_fse_v3(Long id_dte, String ambiente, Connection conn) {
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
            Number TOTALCOMPRA = ctrl_base_datos.ObtenerDouble("SELECT SUM(F.COMPRA) FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            Number DESCU = 0.00;
            Number TOTALDESCU = 0.00;
            Number SUBTOTAL = TOTALCOMPRA.doubleValue();
            Number IVARETEL = 0.00;
            Number RETERENTA = SUBTOTAL.doubleValue() * 0.10;
            Number TOTALPAGAR = SUBTOTAL.doubleValue() - IVARETEL.doubleValue() - RETERENTA.doubleValue();
            
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
            Number PAGOS_PERIODO = 1;
            String OBSERVACIONES = "-";

            String cadenasql = "INSERT INTO RESUMEN_FSE_V3 ("
                    + "ID_DTE, "
                    + "ID_RESUMEN, "
                    + "TOTALCOMPRA, "
                    + "DESCU, "
                    + "TOTALDESCU, "
                    + "SUBTOTAL, "
                    + "IVARETEL, "
                    + "RETERENTA, "
                    + "TOTALPAGAR, "
                    + "TOTALLETRAS, "
                    + "ID_CAT_016, "
                    + "ID_CAT_017, "
                    + "PAGOS_MONTOPAGO, "
                    + "PAGOS_REFERENCIA, "
                    + "ID_CAT_018, "
                    + "PAGOS_PERIODO, "
                    + "OBSERVACIONES) VALUES ("
                    + ID_DTE + ","
                    + ID_RESUMEN + ","
                    + TOTALCOMPRA + ","
                    + DESCU + ","
                    + TOTALDESCU + ","
                    + SUBTOTAL + ","
                    + IVARETEL + ","
                    + RETERENTA + ","
                    + TOTALPAGAR + ",'"
                    + TOTALLETRAS + "',"
                    + ID_CAT_016 + ","
                    + ID_CAT_017 + ","
                    + PAGOS_MONTOPAGO + ","
                    + PAGOS_REFERENCIA + ","
                    + ID_CAT_018 + ","
                    + PAGOS_PERIODO + ",'"
                    + OBSERVACIONES + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_resumen_jde_fse_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
