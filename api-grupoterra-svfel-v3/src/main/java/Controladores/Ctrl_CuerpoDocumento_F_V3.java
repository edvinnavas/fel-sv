package Controladores;

import Entidades.CuerpoDocumento_f;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_CuerpoDocumento_F_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_CuerpoDocumento_F_V3() {
    }

    public List<CuerpoDocumento_f> obtener_cuerpo_documento_f_v3(Long id_dte, Connection conn) {
        List<CuerpoDocumento_f> resultado = new ArrayList<>();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            String cadenasql = "SELECT F.ID_CUERPO_DOCUMENTO FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " ORDER BY F.ID_CUERPO_DOCUMENTO";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                Long id_cuerpo_documento = rs.getLong(1);
                CuerpoDocumento_f cuerpo_documento_f = new CuerpoDocumento_f();
                cuerpo_documento_f.setNumItem(id_cuerpo_documento);
                cuerpo_documento_f.setTipoItem(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_011 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_011 FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + ")", conn));
                cuerpo_documento_f.setNumeroDocumento(ctrl_base_datos.ObtenerString("SELECT F.NUMERODOCUMENTO FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setCantidad(ctrl_base_datos.ObtenerDouble("SELECT F.CANTIDAD FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setCodigo(ctrl_base_datos.ObtenerString("SELECT F.CODIGO FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setCodTributo(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_015 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_015 FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + ")", conn));
                cuerpo_documento_f.setUniMedida(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_014 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_014 FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + ")", conn));
                cuerpo_documento_f.setDescripcion(ctrl_base_datos.ObtenerString("SELECT F.DESCRIPCION FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setPrecioUni(ctrl_base_datos.ObtenerDouble("SELECT A.PRECIOUNI FROM CUERPO_DOCU_F_V3 A WHERE A.ID_DTE=" + id_dte + " AND A.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setMontoDescu(ctrl_base_datos.ObtenerDouble("SELECT A.MONTODESCU FROM CUERPO_DOCU_F_V3 A WHERE A.ID_DTE=" + id_dte + " AND A.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setVentaNoSuj(ctrl_base_datos.ObtenerDouble("SELECT F.VENTANOSUJ FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setVentaExenta(ctrl_base_datos.ObtenerDouble("SELECT F.VENTAEXENTA FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setVentaGravada(ctrl_base_datos.ObtenerDouble("SELECT A.VENTAGRAVADA FROM CUERPO_DOCU_F_V3 A WHERE A.ID_DTE=" + id_dte + " AND A.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));

                List<String> tributos = new ArrayList();
                String cadenasql_1 = "SELECT F.NUM_TRIBUTO FROM CUERPO_TRIBUTO_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CAT_015 NOT IN (18,1) AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento;
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery(cadenasql_1);
                while (rs1.next()) {
                    Long num_tributo = rs1.getLong(1);
                    String tributo = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_015 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_015 FROM CUERPO_TRIBUTO_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + " AND F.NUM_TRIBUTO=" + num_tributo + ")", conn);
                    tributos.add(tributo);
                }
                rs1.close();
                stmt1.close();

                if (tributos.isEmpty()) {
                    cuerpo_documento_f.setTributos(null);
                } else {
                    cuerpo_documento_f.setTributos(tributos);
                }

                cuerpo_documento_f.setPsv(ctrl_base_datos.ObtenerDouble("SELECT F.PSV FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setNoGravado(ctrl_base_datos.ObtenerDouble("SELECT F.NOGRAVADO FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_f.setIvaItem(ctrl_base_datos.ObtenerDouble("SELECT F.IVAITEM FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                resultado.add(cuerpo_documento_f);
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_cuerpo_documento_f_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_cuerpo_documento_jde_f_v3(Long id_dte, String ambiente, String KCOO_JDE, String DOCO_JDE, String DCTO_JDE, String tabla_sales_orders, Connection conn) {
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
                    + "TRIM(F.SDLNTY) tipoItem, "
                    + "F.SDUORG cantidad, "
                    + "TRIM(F.SDLITM) codigo, "
                    + "TRIM(F.SDUOM) uniMedida, "
                    + "TRIM(F.SDDSC1) || TRIM(F.SDDSC2) descripcion, "
                    + "DECODE((SELECT CCCRCD FROM " + esquema + ".F0010@" + dblink + " G WHERE G.CCCO=F.SDKCOO), F.SDCRCD, F.SDUPRC, F.SDFUP)/10000 precioUni, "
                    + "F.SDLNID lineaId, "
                    + "TRIM(F.SDTAX1) aplicaImpuesto, "
                    + "TRIM(F.SDTXA1) impuesto "
                    + "FROM " + esquema + "." + tabla_sales_orders + "@" + dblink + " F "
                    + "WHERE F.SDKCOO='" + KCOO_JDE + "' AND F.SDDOCO=" + DOCO_JDE + " AND F.SDDCTO='" + DCTO_JDE + "' AND F.SDLNTY IN ('M','S','SX')";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            Integer contador = 0;
            while (rs.next()) {
                contador++;
                Long ID_DTE = id_dte;
                Long ID_CUERPO_DOCUMENTO = Long.valueOf(contador.toString());
                Long ID_CAT_011 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_011 C WHERE C.VALOR_JDE LIKE '%[" + rs.getString(1) + "]%'", conn);
                String NUMERODOCUMENTO = "null";
                Long CANTIDAD = rs.getLong(2);
                if (CANTIDAD < 0.00) {
                    CANTIDAD = CANTIDAD * -1;
                }
                String CODIGO = rs.getString(3);
                Long ID_CAT_015 = null;
                Long ID_CAT_014 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_014 C WHERE C.VALOR_JDE LIKE '%[" + rs.getString(4) + "]%'", conn);
                String DESCRIPCION = rs.getString(5);

                // EXTRAE EL PRECIO UNITARIO.
                Number PRECIOUNI = rs.getDouble(6);
                if (PRECIOUNI.doubleValue() < 0.00) {
                    PRECIOUNI = PRECIOUNI.doubleValue() * -1;
                }

                // EXTRAE EL AJUSTE COMPETITIVO (DESCUENTO)
                Number MONTODESCU = ctrl_base_datos.ObtenerDouble("SELECT F.ALUPRC/10000 FROM " + esquema + ".F4074@" + dblink + " F WHERE F.ALKCOO='" + KCOO_JDE + "' AND F.ALDOCO=" + DOCO_JDE + " AND F.ALDCTO='" + DCTO_JDE + "' AND F.ALLNID=" + rs.getString(7) + " AND TRIM(F.ALAST) IN ('SVPSCG')", conn);
                if (MONTODESCU == null) {
                    MONTODESCU = 0.00;
                }
                if (MONTODESCU.doubleValue() < 0.00) {
                    MONTODESCU = MONTODESCU.doubleValue() * -1;
                }
                PRECIOUNI = PRECIOUNI.doubleValue() + MONTODESCU.doubleValue();
                MONTODESCU = CANTIDAD * MONTODESCU.doubleValue();

                // EXTRAE EL FLETE SI APLICA MAYOR A 0.00
                Number PRECIOUNIFLETE = ctrl_base_datos.ObtenerDouble("SELECT F.ALUPRC/10000 FROM " + esquema + ".F4074@" + dblink + " F WHERE F.ALKCOO='" + KCOO_JDE + "' AND F.ALDOCO=" + DOCO_JDE + " AND F.ALDCTO='" + DCTO_JDE + "' AND F.ALLNID=" + rs.getString(7) + " AND TRIM(F.ALAST) IN ('SVBITFRG', 'SVBITFR', 'SVECSAFR', 'SVCOMBFR', 'SVFCG2', 'SVMARBFG')", conn);
                if (PRECIOUNIFLETE == null) {
                    PRECIOUNIFLETE = 0.00;
                }
                if (PRECIOUNIFLETE.doubleValue() < 0.00) {
                    PRECIOUNIFLETE = PRECIOUNIFLETE.doubleValue() * -1;
                }
                PRECIOUNI = PRECIOUNI.doubleValue() - PRECIOUNIFLETE.doubleValue();
                PRECIOUNIFLETE = CANTIDAD * PRECIOUNIFLETE.doubleValue();

                // EXTRAE EL IMPUESTO ESPECIAL IEC SI APLICA MAYOR A 0.00, ESTE MONTO NO SE RESTA DEL PRECIO UNITARIO BASE
                Number PRECIOUNIIEC = ctrl_base_datos.ObtenerDouble("SELECT F.ALUPRC/10000 FROM " + esquema + ".F4074@" + dblink + " F WHERE F.ALKCOO='" + KCOO_JDE + "' AND F.ALDOCO=" + DOCO_JDE + " AND F.ALDCTO='" + DCTO_JDE + "' AND F.ALLNID=" + rs.getString(7) + " AND TRIM(F.ALAST) IN ('SVECSAT4', 'SVT4')", conn);
                if (PRECIOUNIIEC == null) {
                    PRECIOUNIIEC = 0.00;
                }
                if (PRECIOUNIIEC.doubleValue() < 0.00) {
                    PRECIOUNIIEC = PRECIOUNIIEC.doubleValue() * -1;
                }
                PRECIOUNIIEC = CANTIDAD * PRECIOUNIIEC.doubleValue();

                // EXTRAE LA PROMOCION SI APLICA MAYOR A 0.00, ESTE MONTO NO SE RESTA DEL PRECIO UNITARIO BASE
                Number PRECIOUNIPROMO = ctrl_base_datos.ObtenerDouble("SELECT F.ALUPRC/10000 FROM " + esquema + ".F4074@" + dblink + " F WHERE F.ALKCOO='" + KCOO_JDE + "' AND F.ALDOCO=" + DOCO_JDE + " AND F.ALDCTO='" + DCTO_JDE + "' AND F.ALLNID=" + rs.getString(7) + " AND TRIM(F.ALAST) IN ('SVP')", conn);
                if (PRECIOUNIPROMO == null) {
                    PRECIOUNIPROMO = 0.00;
                }
                if (PRECIOUNIPROMO.doubleValue() < 0.00) {
                    PRECIOUNIPROMO = PRECIOUNIPROMO.doubleValue() * -1;
                }
                PRECIOUNIPROMO = CANTIDAD * PRECIOUNIPROMO.doubleValue();

                Number VENTANOSUJ = 0.00;
                Number VENTAEXENTA = 0.00;
                Number VENTAGRAVADA = (CANTIDAD * PRECIOUNI.doubleValue()) - MONTODESCU.doubleValue();;
                Number PSV = 0.00;
                Number NOGRAVADO = 0.00;
                Number IVAITEM = 0.00;

                cadenasql = "INSERT INTO CUERPO_DOCU_F_V3 ( "
                        + "ID_DTE, "
                        + "ID_CUERPO_DOCUMENTO,"
                        + "ID_CAT_011, "
                        + "NUMERODOCUMENTO, "
                        + "CANTIDAD, "
                        + "CODIGO, "
                        + "ID_CAT_015, "
                        + "ID_CAT_014, "
                        + "DESCRIPCION, "
                        + "PRECIOUNI, "
                        + "MONTODESCU, "
                        + "VENTANOSUJ, "
                        + "VENTAEXENTA, "
                        + "VENTAGRAVADA, "
                        + "PSV, "
                        + "NOGRAVADO, "
                        + "IVAITEM) VALUES ("
                        + ID_DTE + ","
                        + ID_CUERPO_DOCUMENTO + ","
                        + ID_CAT_011 + ","
                        + NUMERODOCUMENTO + ","
                        + CANTIDAD + ",'"
                        + CODIGO + "',"
                        + ID_CAT_015 + ","
                        + ID_CAT_014 + ",'"
                        + DESCRIPCION + "',"
                        + PRECIOUNI + ","
                        + MONTODESCU + ","
                        + VENTANOSUJ + ","
                        + VENTAEXENTA + ","
                        + VENTAGRAVADA + ","
                        + PSV + ","
                        + NOGRAVADO + ","
                        + IVAITEM + ")";
                Statement stmt1 = conn.createStatement();
                // System.out.println(cadenasql);
                stmt1.executeUpdate(cadenasql);
                stmt1.close();

                // EXTRAE EL IMPUESTO APLICADO A LA LINEA DEL PRODCUTO.
                Integer NUM_TRIBUTO = 0;
                if (rs.getString(8).equals("Y")) {
                    NUM_TRIBUTO++;
                    Long ID_CAT_015_TRIBUTO = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_015 C WHERE C.VALOR_JDE LIKE '%[" + rs.getString(9) + "]%'", conn);
                    Number TRIBUTO_VALOR = ctrl_base_datos.ObtenerDouble("SELECT F.TATXR1/100000 FROM " + esquema + ".F4008@" + dblink + " F WHERE TRIM(F.TATXA1)='" + rs.getString(9) + "' AND F.TAITM=0", conn);
                    TRIBUTO_VALOR = VENTAGRAVADA.doubleValue() * TRIBUTO_VALOR.doubleValue();
                    if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                        cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                + "ID_DTE, "
                                + "ID_CUERPO_DOCUMENTO, "
                                + "NUM_TRIBUTO, "
                                + "ID_CAT_015, "
                                + "VALOR) VALUES ("
                                + ID_DTE + ","
                                + ID_CUERPO_DOCUMENTO + ","
                                + NUM_TRIBUTO + ","
                                + ID_CAT_015_TRIBUTO + ","
                                + TRIBUTO_VALOR + ")";
                        stmt1 = conn.createStatement();
                        // System.out.println(cadenasql);
                        stmt1.executeUpdate(cadenasql);
                        stmt1.close();
                    }

                    if (ID_CAT_015_TRIBUTO != null) {
                        if (ID_CAT_015_TRIBUTO.equals(Long.valueOf("1"))) {
                            cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET IVAITEM=" + TRIBUTO_VALOR + " WHERE ID_DTE=" + ID_DTE + " AND ID_CUERPO_DOCUMENTO=" + ID_CUERPO_DOCUMENTO;
                            stmt1 = conn.createStatement();
                            // System.out.println(cadenasql);
                            stmt1.executeUpdate(cadenasql);
                            stmt1.close();

                            Number PRECIOUNI_TEMP;
                            if (PRECIOUNI.doubleValue() > 0.00) {
                                PRECIOUNI_TEMP = PRECIOUNI.doubleValue() + (TRIBUTO_VALOR.doubleValue() / CANTIDAD);
                            } else {
                                PRECIOUNI_TEMP = 0.00;
                            }

                            Number MONTODESCU_TEMP;
                            if (MONTODESCU.doubleValue() > 0.00) {
                                MONTODESCU_TEMP = MONTODESCU.doubleValue() + TRIBUTO_VALOR.doubleValue();
                            } else {
                                MONTODESCU_TEMP = 0.00;
                            }

                            Number VENTAGRAVADA_TEMP;
                            if (VENTAGRAVADA.doubleValue() > 0.00) {
                                VENTAGRAVADA_TEMP = VENTAGRAVADA.doubleValue() + TRIBUTO_VALOR.doubleValue();
                            } else {
                                VENTAGRAVADA_TEMP = 0.00;
                            }

                            cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET PRECIOUNI=" + PRECIOUNI_TEMP + ", MONTODESCU=" + MONTODESCU_TEMP + ", VENTAGRAVADA=" + VENTAGRAVADA_TEMP + " WHERE ID_DTE=" + ID_DTE + " AND ID_CUERPO_DOCUMENTO=" + ID_CUERPO_DOCUMENTO;
                            stmt1 = conn.createStatement();
                            // System.out.println(cadenasql);
                            stmt1.executeUpdate(cadenasql);
                            stmt1.close();
                        }
                    }

                    if (rs.getString(9).trim().equals("EIVAC")) {
                        NUM_TRIBUTO++;
                        Long ID_CAT_015_TRIBUTO_EIVAC = Long.valueOf("18");
                        Number TRIBUTO_VALOR_EIVAC = ctrl_base_datos.ObtenerDouble("SELECT F.TATXR2/100000 FROM " + esquema + ".F4008@" + dblink + " F WHERE TRIM(F.TATXA1)='" + rs.getString(9) + "' AND F.TAITM=0", conn);
                        TRIBUTO_VALOR = VENTAGRAVADA.doubleValue() * TRIBUTO_VALOR_EIVAC.doubleValue();
                        if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                            cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                    + "ID_DTE, "
                                    + "ID_CUERPO_DOCUMENTO, "
                                    + "NUM_TRIBUTO, "
                                    + "ID_CAT_015, "
                                    + "VALOR) VALUES ("
                                    + ID_DTE + ","
                                    + ID_CUERPO_DOCUMENTO + ","
                                    + NUM_TRIBUTO + ","
                                    + ID_CAT_015_TRIBUTO_EIVAC + ","
                                    + TRIBUTO_VALOR + ")";
                            stmt1 = conn.createStatement();
                            // System.out.println(cadenasql);
                            stmt1.executeUpdate(cadenasql);
                            stmt1.close();
                        }
                    }
                }

                // EXTRAE EL FOVIAL Y CONTRAN SI APLICA EN LA LINEA DEL PRODUCTO.
                cadenasql = "SELECT "
                        + "F.SDUORG cantidad, "
                        + "TRIM(F.SDLITM) codigo, "
                        + "DECODE((SELECT CCCRCD FROM " + esquema + ".F0010@" + dblink + " G WHERE G.CCCO=F.SDKCOO), F.SDCRCD, F.SDUPRC, F.SDFUP)/10000 precioUni "
                        + "FROM " + esquema + "." + tabla_sales_orders + "@" + dblink + " F "
                        + "WHERE "
                        + "F.SDKCOO='" + KCOO_JDE + "' AND F.SDDOCO=" + DOCO_JDE + " AND F.SDDCTO='" + DCTO_JDE + "' AND "
                        + "F.SDOKCO='" + KCOO_JDE + "' AND F.SDOORN=" + DOCO_JDE + " AND F.SDOCTO='" + DCTO_JDE + "' AND F.SDOGNO=" + rs.getString(7) + " AND "
                        + "F.SDLNTY IN ('PE','DT')";
                stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery(cadenasql);
                while (rs1.next()) {
                    NUM_TRIBUTO++;
                    Long ID_CAT_015_TRIBUTO = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_015 C WHERE C.VALOR_JDE LIKE '%[" + rs1.getString(2) + "]%'", conn);
                    Number TRIBUTO_VALOR = rs1.getLong(1) * rs1.getDouble(3);
                    if (TRIBUTO_VALOR.doubleValue() < 0.00) {
                        TRIBUTO_VALOR = TRIBUTO_VALOR.doubleValue() * -1;
                    }
                    if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                        cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                + "ID_DTE, "
                                + "ID_CUERPO_DOCUMENTO, "
                                + "NUM_TRIBUTO, "
                                + "ID_CAT_015, "
                                + "VALOR) VALUES ("
                                + ID_DTE + ","
                                + ID_CUERPO_DOCUMENTO + ","
                                + NUM_TRIBUTO + ","
                                + ID_CAT_015_TRIBUTO + ","
                                + TRIBUTO_VALOR + ")";
                        Statement stmt2 = conn.createStatement();
                        // System.out.println(cadenasql);
                        stmt2.executeUpdate(cadenasql);
                        stmt2.close();
                    }
                }
                rs1.close();
                stmt1.close();

                // EXTRAE EL FLETE DEL PRODUCTO SI APLICA.
                if (PRECIOUNIFLETE.doubleValue() > 0.00) {
                    contador++;
                    ID_CUERPO_DOCUMENTO = Long.valueOf(contador.toString());
                    ID_CAT_011 = Long.valueOf("2");

                    Number PRECIOUNIFLETE_TEMP = PRECIOUNIFLETE.doubleValue() / CANTIDAD;
                    VENTAEXENTA = 0.00;
                    VENTAGRAVADA = PRECIOUNIFLETE;
                    NOGRAVADO = 0.00;

                    cadenasql = "INSERT INTO CUERPO_DOCU_F_V3 ( "
                            + "ID_DTE, "
                            + "ID_CUERPO_DOCUMENTO,"
                            + "ID_CAT_011, "
                            + "NUMERODOCUMENTO, "
                            + "CANTIDAD, "
                            + "CODIGO, "
                            + "ID_CAT_015, "
                            + "ID_CAT_014, "
                            + "DESCRIPCION, "
                            + "PRECIOUNI, "
                            + "MONTODESCU, "
                            + "VENTANOSUJ, "
                            + "VENTAEXENTA, "
                            + "VENTAGRAVADA, "
                            + "PSV, "
                            + "NOGRAVADO, "
                            + "IVAITEM) VALUES ("
                            + ID_DTE + ","
                            + ID_CUERPO_DOCUMENTO + ","
                            + ID_CAT_011 + ","
                            + NUMERODOCUMENTO + ","
                            + CANTIDAD + ",'"
                            + "FLETE" + "',"
                            + ID_CAT_015 + ","
                            + ID_CAT_014 + ",'"
                            + "FLETE " + DESCRIPCION + "',"
                            + PRECIOUNIFLETE_TEMP + ","
                            + "0" + ","
                            + VENTANOSUJ + ","
                            + VENTAEXENTA + ","
                            + VENTAGRAVADA + ","
                            + PSV + ","
                            + NOGRAVADO + ","
                            + IVAITEM + ")";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();

                    // EXTRAE EL IMPUESTO APLICADO A LA LINEA DEL FLETE.
                    NUM_TRIBUTO = 0;
                    if (rs.getString(8).equals("Y")) {
                        NUM_TRIBUTO++;
                        Long ID_CAT_015_TRIBUTO = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_015 C WHERE C.VALOR_JDE LIKE '%[" + rs.getString(9) + "]%'", conn);
                        Number TRIBUTO_VALOR = ctrl_base_datos.ObtenerDouble("SELECT F.TATXR1/100000 FROM " + esquema + ".F4008@" + dblink + " F WHERE TRIM(F.TATXA1)='" + rs.getString(9) + "' AND F.TAITM=0", conn);
                        TRIBUTO_VALOR = VENTAGRAVADA.doubleValue() * TRIBUTO_VALOR.doubleValue();
                        if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                            cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                    + "ID_DTE, "
                                    + "ID_CUERPO_DOCUMENTO, "
                                    + "NUM_TRIBUTO, "
                                    + "ID_CAT_015, "
                                    + "VALOR) VALUES ("
                                    + ID_DTE + ","
                                    + ID_CUERPO_DOCUMENTO + ","
                                    + NUM_TRIBUTO + ","
                                    + ID_CAT_015_TRIBUTO + ","
                                    + TRIBUTO_VALOR + ")";
                            stmt1 = conn.createStatement();
                            // System.out.println(cadenasql);
                            stmt1.executeUpdate(cadenasql);
                            stmt1.close();
                        }

                        if (ID_CAT_015_TRIBUTO != null) {
                            if (ID_CAT_015_TRIBUTO.equals(Long.valueOf("1"))) {
                                cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET IVAITEM=" + TRIBUTO_VALOR + " WHERE ID_DTE=" + ID_DTE + " AND ID_CUERPO_DOCUMENTO=" + ID_CUERPO_DOCUMENTO;
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();

                                Number PRECIOUNIFLETE_TEMP_TEMP;
                                if (PRECIOUNIFLETE_TEMP.doubleValue() > 0.00) {
                                    PRECIOUNIFLETE_TEMP_TEMP = PRECIOUNIFLETE_TEMP.doubleValue() + (TRIBUTO_VALOR.doubleValue() / CANTIDAD);
                                } else {
                                    PRECIOUNIFLETE_TEMP_TEMP = 0.00;
                                }

                                Number VENTAGRAVADA_TEMP;
                                if (VENTAGRAVADA.doubleValue() > 0.00) {
                                    VENTAGRAVADA_TEMP = VENTAGRAVADA.doubleValue() + TRIBUTO_VALOR.doubleValue();
                                } else {
                                    VENTAGRAVADA_TEMP = 0.00;
                                }

                                cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET PRECIOUNI=" + PRECIOUNIFLETE_TEMP_TEMP + ", VENTAGRAVADA=" + VENTAGRAVADA_TEMP + " WHERE ID_DTE=" + ID_DTE + " AND ID_CUERPO_DOCUMENTO=" + ID_CUERPO_DOCUMENTO;
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();
                            }
                        }

                        if (rs.getString(9).trim().equals("EIVAC")) {
                            NUM_TRIBUTO++;
                            Long ID_CAT_015_TRIBUTO_EIVAC = Long.valueOf("18");
                            Number TRIBUTO_VALOR_EIVAC = ctrl_base_datos.ObtenerDouble("SELECT F.TATXR2/100000 FROM " + esquema + ".F4008@" + dblink + " F WHERE TRIM(F.TATXA1)='" + rs.getString(9) + "' AND F.TAITM=0", conn);
                            TRIBUTO_VALOR = VENTAGRAVADA.doubleValue() * TRIBUTO_VALOR_EIVAC.doubleValue();
                            if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                                cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                        + "ID_DTE, "
                                        + "ID_CUERPO_DOCUMENTO, "
                                        + "NUM_TRIBUTO, "
                                        + "ID_CAT_015, "
                                        + "VALOR) VALUES ("
                                        + ID_DTE + ","
                                        + ID_CUERPO_DOCUMENTO + ","
                                        + NUM_TRIBUTO + ","
                                        + ID_CAT_015_TRIBUTO_EIVAC + ","
                                        + TRIBUTO_VALOR + ")";
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();
                            }
                        }
                    }
                }

                // EXTRAE EL IEC DEL PRODUCTO SI APLICA.
                if (PRECIOUNIIEC.doubleValue() > 0.00) {
                    contador++;
                    ID_CUERPO_DOCUMENTO = Long.valueOf(contador.toString());
                    ID_CAT_011 = Long.valueOf("4");
                    Long ID_CAT_015_IEC = Long.valueOf("9");
                    Long ID_CAT_014_IEC = Long.valueOf("56");

                    // Number PRECIOUNIIEC_TEMP = PRECIOUNIIEC.doubleValue() / CANTIDAD;
                    VENTAEXENTA = 0.00;
                    VENTAGRAVADA = PRECIOUNIIEC;
                    NOGRAVADO = 0.00;

                    cadenasql = "INSERT INTO CUERPO_DOCU_F_V3 ( "
                            + "ID_DTE, "
                            + "ID_CUERPO_DOCUMENTO,"
                            + "ID_CAT_011, "
                            + "NUMERODOCUMENTO, "
                            + "CANTIDAD, "
                            + "CODIGO, "
                            + "ID_CAT_015, "
                            + "ID_CAT_014, "
                            + "DESCRIPCION, "
                            + "PRECIOUNI, "
                            + "MONTODESCU, "
                            + "VENTANOSUJ, "
                            + "VENTAEXENTA, "
                            + "VENTAGRAVADA, "
                            + "PSV, "
                            + "NOGRAVADO, "
                            + "IVAITEM) VALUES ("
                            + ID_DTE + ","
                            + ID_CUERPO_DOCUMENTO + ","
                            + ID_CAT_011 + ","
                            + NUMERODOCUMENTO + ","
                            + "1" + ",'"
                            + "IEC" + "',"
                            + ID_CAT_015_IEC + ","
                            + ID_CAT_014_IEC + ",'"
                            + "IEC " + DESCRIPCION + "',"
                            + VENTAGRAVADA + ","
                            + "0" + ","
                            + VENTANOSUJ + ","
                            + VENTAEXENTA + ","
                            + VENTAGRAVADA + ","
                            + PSV + ","
                            + NOGRAVADO + ","
                            + IVAITEM + ")";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();

                    // EXTRAE EL IMPUESTO APLICADO A LA LINEA DEL IEC.
                    NUM_TRIBUTO = 0;
                    if (rs.getString(8).equals("Y")) {
                        NUM_TRIBUTO++;
                        Long ID_CAT_015_TRIBUTO = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_015 C WHERE C.VALOR_JDE LIKE '%[" + rs.getString(9) + "]%'", conn);
                        Number TRIBUTO_VALOR = ctrl_base_datos.ObtenerDouble("SELECT F.TATXR1/100000 FROM " + esquema + ".F4008@" + dblink + " F WHERE TRIM(F.TATXA1)='" + rs.getString(9) + "' AND F.TAITM=0", conn);
                        TRIBUTO_VALOR = VENTAGRAVADA.doubleValue() * TRIBUTO_VALOR.doubleValue();
                        if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                            cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                    + "ID_DTE, "
                                    + "ID_CUERPO_DOCUMENTO, "
                                    + "NUM_TRIBUTO, "
                                    + "ID_CAT_015, "
                                    + "VALOR) VALUES ("
                                    + ID_DTE + ","
                                    + ID_CUERPO_DOCUMENTO + ","
                                    + NUM_TRIBUTO + ","
                                    + ID_CAT_015_TRIBUTO + ","
                                    + TRIBUTO_VALOR + ")";
                            stmt1 = conn.createStatement();
                            // System.out.println(cadenasql);
                            stmt1.executeUpdate(cadenasql);
                            stmt1.close();
                        }

                        if (ID_CAT_015_TRIBUTO != null) {
                            if (ID_CAT_015_TRIBUTO.equals(Long.valueOf("1"))) {
                                cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET IVAITEM=" + TRIBUTO_VALOR + " WHERE ID_DTE=" + ID_DTE + " AND ID_CUERPO_DOCUMENTO=" + ID_CUERPO_DOCUMENTO;
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();

                                if (VENTAGRAVADA.doubleValue() > 0.00) {
                                    VENTAGRAVADA = VENTAGRAVADA.doubleValue() + (TRIBUTO_VALOR.doubleValue() / CANTIDAD);
                                } else {
                                    VENTAGRAVADA = 0.00;
                                }

                                cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET PRECIOUNI=" + VENTAGRAVADA + ", VENTAGRAVADA=" + VENTAGRAVADA + " WHERE ID_DTE=" + ID_DTE + " AND ID_CUERPO_DOCUMENTO=" + ID_CUERPO_DOCUMENTO;
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();
                            }
                        }

                        if (rs.getString(9).trim().equals("EIVAC")) {
                            NUM_TRIBUTO++;
                            Long ID_CAT_015_TRIBUTO_EIVAC = Long.valueOf("18");
                            Number TRIBUTO_VALOR_EIVAC = ctrl_base_datos.ObtenerDouble("SELECT F.TATXR2/100000 FROM " + esquema + ".F4008@" + dblink + " F WHERE TRIM(F.TATXA1)='" + rs.getString(9) + "' AND F.TAITM=0", conn);
                            TRIBUTO_VALOR = VENTAGRAVADA.doubleValue() * TRIBUTO_VALOR_EIVAC.doubleValue();
                            if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                                cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                        + "ID_DTE, "
                                        + "ID_CUERPO_DOCUMENTO, "
                                        + "NUM_TRIBUTO, "
                                        + "ID_CAT_015, "
                                        + "VALOR) VALUES ("
                                        + ID_DTE + ","
                                        + ID_CUERPO_DOCUMENTO + ","
                                        + NUM_TRIBUTO + ","
                                        + ID_CAT_015_TRIBUTO_EIVAC + ","
                                        + TRIBUTO_VALOR + ")";
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();
                            }
                        }
                    }
                }

                // EXTRAE LA PROMOCIÓN DEL PRODUCTO SI APLICA.
                if (PRECIOUNIPROMO.doubleValue() > 0.00) {
                    contador++;
                    ID_CUERPO_DOCUMENTO = Long.valueOf(contador.toString());
                    ID_CAT_011 = Long.valueOf("2");

                    Number PRECIOUNIPROMO_TEMP = PRECIOUNIPROMO.doubleValue() / CANTIDAD;
                    VENTAEXENTA = 0.00;
                    VENTAGRAVADA = PRECIOUNIPROMO;
                    NOGRAVADO = 0.00;

                    cadenasql = "INSERT INTO CUERPO_DOCU_F_V3 ( "
                            + "ID_DTE, "
                            + "ID_CUERPO_DOCUMENTO,"
                            + "ID_CAT_011, "
                            + "NUMERODOCUMENTO, "
                            + "CANTIDAD, "
                            + "CODIGO, "
                            + "ID_CAT_015, "
                            + "ID_CAT_014, "
                            + "DESCRIPCION, "
                            + "PRECIOUNI, "
                            + "MONTODESCU, "
                            + "VENTANOSUJ, "
                            + "VENTAEXENTA, "
                            + "VENTAGRAVADA, "
                            + "PSV, "
                            + "NOGRAVADO, "
                            + "IVAITEM) VALUES ("
                            + ID_DTE + ","
                            + ID_CUERPO_DOCUMENTO + ","
                            + ID_CAT_011 + ","
                            + NUMERODOCUMENTO + ","
                            + CANTIDAD + ",'"
                            + "PROMOCIÓN" + "',"
                            + ID_CAT_015 + ","
                            + ID_CAT_014 + ",'"
                            + "PROMOCIÓN " + DESCRIPCION + "',"
                            + PRECIOUNIPROMO_TEMP + ","
                            + "0" + ","
                            + VENTANOSUJ + ","
                            + VENTAEXENTA + ","
                            + VENTAGRAVADA + ","
                            + PSV + ","
                            + NOGRAVADO + ","
                            + IVAITEM + ")";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();

                    // EXTRAE EL IMPUESTO APLICADO A LA LINEA DE LA PROMOCIÓN.
                    NUM_TRIBUTO = 0;
                    if (rs.getString(8).equals("Y")) {
                        NUM_TRIBUTO++;
                        Long ID_CAT_015_TRIBUTO = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_015 C WHERE C.VALOR_JDE LIKE '%[" + rs.getString(9) + "]%'", conn);
                        Number TRIBUTO_VALOR = ctrl_base_datos.ObtenerDouble("SELECT F.TATXR1/100000 FROM " + esquema + ".F4008@" + dblink + " F WHERE TRIM(F.TATXA1)='" + rs.getString(9) + "' AND F.TAITM=0", conn);
                        TRIBUTO_VALOR = VENTAGRAVADA.doubleValue() * TRIBUTO_VALOR.doubleValue();
                        if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                            cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                    + "ID_DTE, "
                                    + "ID_CUERPO_DOCUMENTO, "
                                    + "NUM_TRIBUTO, "
                                    + "ID_CAT_015, "
                                    + "VALOR) VALUES ("
                                    + ID_DTE + ","
                                    + ID_CUERPO_DOCUMENTO + ","
                                    + NUM_TRIBUTO + ","
                                    + ID_CAT_015_TRIBUTO + ","
                                    + TRIBUTO_VALOR + ")";
                            stmt1 = conn.createStatement();
                            // System.out.println(cadenasql);
                            stmt1.executeUpdate(cadenasql);
                            stmt1.close();
                        }

                        if (ID_CAT_015_TRIBUTO != null) {
                            if (ID_CAT_015_TRIBUTO.equals(Long.valueOf("1"))) {
                                cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET IVAITEM=" + TRIBUTO_VALOR + " WHERE ID_DTE=" + ID_DTE + " AND ID_CUERPO_DOCUMENTO=" + ID_CUERPO_DOCUMENTO;
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();

                                Number PRECIOUNIPROMO_TEMP_TEMP;
                                if (PRECIOUNIPROMO_TEMP.doubleValue() > 0.00) {
                                    PRECIOUNIPROMO_TEMP_TEMP = PRECIOUNIPROMO_TEMP.doubleValue() + (TRIBUTO_VALOR.doubleValue() / CANTIDAD);
                                } else {
                                    PRECIOUNIPROMO_TEMP_TEMP = 0.00;
                                }

                                Number VENTAGRAVADA_TEMP;
                                if (VENTAGRAVADA.doubleValue() > 0.00) {
                                    VENTAGRAVADA_TEMP = VENTAGRAVADA.doubleValue() + TRIBUTO_VALOR.doubleValue();
                                } else {
                                    VENTAGRAVADA_TEMP = 0.00;
                                }

                                cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET PRECIOUNI=" + PRECIOUNIPROMO_TEMP_TEMP + ", VENTAGRAVADA=" + VENTAGRAVADA_TEMP + " WHERE ID_DTE=" + ID_DTE + " AND ID_CUERPO_DOCUMENTO=" + ID_CUERPO_DOCUMENTO;
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();
                            }
                        }

                        if (rs.getString(9).trim().equals("EIVAC")) {
                            NUM_TRIBUTO++;
                            Long ID_CAT_015_TRIBUTO_EIVAC = Long.valueOf("18");
                            Number TRIBUTO_VALOR_EIVAC = ctrl_base_datos.ObtenerDouble("SELECT F.TATXR2/100000 FROM " + esquema + ".F4008@" + dblink + " F WHERE TRIM(F.TATXA1)='" + rs.getString(9) + "' AND F.TAITM=0", conn);
                            TRIBUTO_VALOR = VENTAGRAVADA.doubleValue() * TRIBUTO_VALOR_EIVAC.doubleValue();
                            if (TRIBUTO_VALOR.doubleValue() > 0.00) {
                                cadenasql = "INSERT INTO CUERPO_TRIBUTO_F_V3 ( "
                                        + "ID_DTE, "
                                        + "ID_CUERPO_DOCUMENTO, "
                                        + "NUM_TRIBUTO, "
                                        + "ID_CAT_015, "
                                        + "VALOR) VALUES ("
                                        + ID_DTE + ","
                                        + ID_CUERPO_DOCUMENTO + ","
                                        + NUM_TRIBUTO + ","
                                        + ID_CAT_015_TRIBUTO_EIVAC + ","
                                        + TRIBUTO_VALOR + ")";
                                stmt1 = conn.createStatement();
                                // System.out.println(cadenasql);
                                stmt1.executeUpdate(cadenasql);
                                stmt1.close();
                            }
                        }
                    }
                }
            }
            rs.close();
            stmt.close();

            cadenasql = "SELECT F.ID_DTE, F.ID_CUERPO_DOCUMENTO, F.VENTAGRAVADA, F.IVAITEM FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE=" + id_dte + " ORDER BY F.ID_DTE, F.ID_CUERPO_DOCUMENTO";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                Integer no_impuestos = ctrl_base_datos.ObtenerEntero("SELECT COUNT(*) NUMERO FROM CUERPO_TRIBUTO_F_V3 F WHERE F.ID_DTE=" + rs.getString(1) + " AND F.ID_CUERPO_DOCUMENTO=" + rs.getString(2), conn);
                if (no_impuestos == 0) {
                    cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET VENTAEXENTA=" + rs.getDouble(3) + ", VENTAGRAVADA=0.00, IVAITEM=0.00 WHERE ID_DTE=" + rs.getString(1) + " AND ID_CUERPO_DOCUMENTO=" + rs.getString(2);
                    Statement stmt1 = conn.createStatement();
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();
                } else {
                    if (rs.getDouble(3) > 0.00 && rs.getDouble(4) == 0.00) {
                        Double IVAITEM = rs.getDouble(3) - (rs.getDouble(3) / 1.13);
                        DecimalFormat decimalFormat = new DecimalFormat("############0.00");
                        cadenasql = "UPDATE CUERPO_DOCU_F_V3 SET IVAITEM=" + decimalFormat.format(IVAITEM) + " WHERE ID_DTE=" + rs.getString(1) + " AND ID_CUERPO_DOCUMENTO=" + rs.getString(2);
                        Statement stmt1 = conn.createStatement();
                        stmt1.executeUpdate(cadenasql);
                        stmt1.close();
                    }
                }
            }
            rs.close();
            stmt.close();

            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_cuerpo_documento_jde_f_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
