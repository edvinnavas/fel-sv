package Controladores;

import ClienteServicio.Cliente_Rest_IATA;
import Entidades.NR_ZQ_IATA;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_DTE_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_DTE_V3() {
    }

    public Integer selecionar_documentos_v3(String ambiente, String fecha, Integer modo) {
        Integer resultado = 0;
        String tabla_sales_orders;
        Connection conn = null;

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            conn = ctrl_base_datos.obtener_conexion(ambiente);

            String esquema;
            String dblink;
            if (ambiente.equals("PY")) {
                esquema = "CRPDTA";
                dblink = "JDEPY";
            } else {
                esquema = "PRODDTA";
                dblink = "JDEPD";
            }

            conn.setAutoCommit(false);

            Long ivd = ctrl_base_datos.ObtenerLong("SELECT TO_NUMBER(SUBSTR(TO_CHAR(TO_DATE('" + fecha + "','YYYYMMDD')-10,'ccYYddd'),2,6)) IVD FROM DUAL", conn);

            String STCD;
            if (modo == 1) {
                tabla_sales_orders = "F4211";
                STCD = "000";
            } else {
                tabla_sales_orders = "F42119";
                STCD = "999";
            }

            // EXTRAE COMPROBANTES DE CRÉDITO FISCAL, FACTURAS CONSUMIDOR FINAL, FACTURAS DE EXPORACIÓN, NOTAS DE CRÉDITO Y NOTAS DE DÉBITO DESDE JDE.
            String cadenasql = "SELECT DISTINCT F.SDKCOO, F.SDDOCO, F.SDDCTO, F.SDDOC, F.SDDCT, F.SDMCU, F.SDAN8, F.SDSHAN, F.SDCRCD, F.SDIVD, '" + STCD + "' STCD, '-' CRSREF01, '-' CRSREF02, '-' CRSREF03, '-' CRSREF04, '-' CRSREF05, '" + tabla_sales_orders + "' TABLA, TRIM(F.SDTXA1) SDTXA1, NVL(TRIM(G.ABAC30),'-') ABAC30 "
                    + "FROM " + esquema + "." + tabla_sales_orders + "@" + dblink + " F LEFT JOIN " + esquema + ".F0101@" + dblink + " G ON (F.SDSHAN=G.ABAN8) "
                    + "WHERE (TRIM(F.SDKCOO) IN (SELECT C.KCOO_JDE FROM EMISOR_KCOO_V3 C)) AND (F.SDDOC > 0) AND (F.SDDCT NOT IN ('LF','LA','LD')) AND (TRIM(F.SDLTTR) NOT IN ('904','902','900','980')) AND (TRIM(F.SDDCTO) IN ('S3','C3','SD')) AND (TRIM(F.SDCRMD) IS NULL) AND (F.SDIVD >= " + ivd + ")";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                String DCTO = rs.getString(3);
                String AC30 = rs.getString(19);

                if (DCTO.equals("S3")) {
                    switch (AC30) {
                        case "EXP":
                            DCTO = "EX";
                            break;
                        case "FCF":
                            DCTO = "FE";
                            break;
                        case "TMP":
                            cadenasql = "SELECT TRIM(F.CUCSTP) FROM " + esquema + ".F40071@" + dblink + " F WHERE F.CUAN8=" + rs.getString(7) + " AND CUPRFR=30";
                            Statement stmt1 = conn.createStatement();
                            ResultSet rs1 = stmt1.executeQuery(cadenasql);
                            while (rs1.next()) {
                                if (rs1.getString(1).equals("SVCLFACT")) {
                                    DCTO = "FE";
                                } else {
                                    DCTO = "S3";
                                }
                            }
                            rs1.close();
                            stmt1.close();
                            break;
                    }
                }

                Boolean FEDOCO = false;
                cadenasql = "SELECT F.FEDOCO "
                        + "FROM " + esquema + ".F5542FEL@" + dblink + " F "
                        + "WHERE "
                        + "F.FEKCOO='" + rs.getString(1) + "' AND "
                        + "F.FEDOCO=" + rs.getString(2) + " AND "
                        + "F.FEDCTO='" + DCTO + "' AND "
                        + "F.FEDOC=" + rs.getString(4) + " AND "
                        + "F.FEDCT='" + rs.getString(5) + "'";
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery(cadenasql);
                while (rs1.next()) {
                    FEDOCO = true;
                }
                rs1.close();
                stmt1.close();

                if (!FEDOCO) {
                    cadenasql = "INSERT INTO " + esquema + ".F5542FEL@" + dblink + " ("
                            + "FEKCOO, "
                            + "FEDOCO, "
                            + "FEDCTO, "
                            + "FEDOC, "
                            + "FEDCT, "
                            + "FEMCU, "
                            + "FEAN8, "
                            + "FESHAN, "
                            + "FECRCD,"
                            + "FEIVD, "
                            + "FESTCD, "
                            + "FECRSREF01, "
                            + "FECRSREF02, "
                            + "FECRSREF03, "
                            + "FECRSREF04, "
                            + "FECRSREF05, "
                            + "FEJEVER, "
                            + "FERCD, "
                            + "FEAEXP, "
                            + "FETXA1) VALUES ('"
                            + rs.getString(1) + "','"
                            + rs.getString(2) + "','"
                            + DCTO + "','"
                            + rs.getString(4) + "','"
                            + rs.getString(5) + "','"
                            + rs.getString(6) + "','"
                            + rs.getString(7) + "','"
                            + rs.getString(8) + "','"
                            + rs.getString(9) + "','"
                            + rs.getString(10) + "','"
                            + rs.getString(11) + "','"
                            + rs.getString(12) + "','"
                            + rs.getString(13) + "','"
                            + rs.getString(14) + "','"
                            + rs.getString(15) + "','"
                            + rs.getString(16) + "','"
                            + rs.getString(17) + "',"
                            + "null" + ","
                            + "0" + ",'"
                            + rs.getString(18) + "')";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();

                    cadenasql = "UPDATE " + esquema + "." + rs.getString(17) + "@" + dblink + " "
                            + "SET SDCRMD='4' "
                            + "WHERE SDKCOO='" + rs.getString(1) + "' AND SDDOCO=" + rs.getString(2) + " AND SDDCTO='" + rs.getString(3) + "' AND SDDOC=" + rs.getString(4) + " AND SDDCT='" + rs.getString(5) + "'";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();
                    resultado++;
                }
            }
            rs.close();
            stmt.close();

            // EXTRAE NOTAS DE REMISIÓN DESDE JDE.
            cadenasql = "SELECT DISTINCT F.NRKCOO, F.NRDOCO, F.NRDCTO, F.NRN001, F.NRURCD, F.NRMCU, F.NRAN8, F.NRSHAN, F.NRCRCD, F.NRURDT, '" + STCD + "' STCD, '-' CRSREF01, '-' CRSREF02, '-' CRSREF03, '-' CRSREF04, '-' CRSREF05, 'F554211N' TABLA, TRIM(F.NRTXA1) NRTXA1, NVL(TRIM(G.ABAC30),'-') ABAC30 "
                    + "FROM " + esquema + ".F554211N@" + dblink + " F LEFT JOIN " + esquema + ".F0101@" + dblink + " G ON (F.NRAN8=G.ABAN8) "
                    + "WHERE (TRIM(F.NRKCOO) IN (SELECT C.KCOO_JDE FROM EMISOR_KCOO_V3 C)) AND (F.NRN001 > 0) AND (TRIM(F.NRLTTR) NOT IN ('904','902','900','980')) AND (TRIM(F.NRDCTO) IN ('S3','C3','SD')) AND (TRIM(F.NREV01) IN ('N')) AND (F.NRURDT >= " + ivd + ")";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                String DCTO = "NR";

                Boolean FEDOCO = false;
                cadenasql = "SELECT F.FEDOCO "
                        + "FROM " + esquema + ".F5542FEL@" + dblink + " F "
                        + "WHERE "
                        + "F.FEKCOO='" + rs.getString(1) + "' AND "
                        + "F.FEDOCO=" + rs.getString(2) + " AND "
                        + "F.FEDCTO='" + DCTO + "' AND "
                        + "F.FEDOC=" + rs.getString(4) + " AND "
                        + "F.FEDCT='" + rs.getString(5) + "'";
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery(cadenasql);
                while (rs1.next()) {
                    FEDOCO = true;
                }
                rs1.close();
                stmt1.close();

                if (!FEDOCO) {
                    cadenasql = "INSERT INTO " + esquema + ".F5542FEL@" + dblink + " ("
                            + "FEKCOO, "
                            + "FEDOCO, "
                            + "FEDCTO, "
                            + "FEDOC, "
                            + "FEDCT, "
                            + "FEMCU, "
                            + "FEAN8, "
                            + "FESHAN, "
                            + "FECRCD,"
                            + "FEIVD, "
                            + "FESTCD, "
                            + "FECRSREF01, "
                            + "FECRSREF02, "
                            + "FECRSREF03, "
                            + "FECRSREF04, "
                            + "FECRSREF05, "
                            + "FEJEVER, "
                            + "FERCD, "
                            + "FEAEXP, "
                            + "FETXA1) VALUES ('"
                            + rs.getString(1) + "','"
                            + rs.getString(2) + "','"
                            + DCTO + "','"
                            + rs.getString(4) + "','"
                            + rs.getString(5) + "','"
                            + rs.getString(6) + "','"
                            + rs.getString(7) + "','"
                            + rs.getString(8) + "','"
                            + rs.getString(9) + "','"
                            + rs.getString(10) + "','"
                            + rs.getString(11) + "','"
                            + rs.getString(12) + "','"
                            + rs.getString(13) + "','"
                            + rs.getString(14) + "','"
                            + rs.getString(15) + "','"
                            + rs.getString(16) + "','"
                            + rs.getString(17) + "',"
                            + "null" + ","
                            + "0" + ",'"
                            + rs.getString(18) + "')";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();

                    cadenasql = "UPDATE " + esquema + ".F554211N@" + dblink + " "
                            + "SET NREV01='P' "
                            + "WHERE NRKCOO='" + rs.getString(1) + "' AND NRDOCO=" + rs.getString(2) + " AND NRDCTO='" + rs.getString(3) + "' AND NRN001=" + rs.getString(4) + " AND NRURCD='" + rs.getString(5) + "'";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();
                    resultado++;
                }
            }
            rs.close();
            stmt.close();

            // EXTRAE COMPROBANTES DE RETENCION DESDE JDE.
            cadenasql = "SELECT DISTINCT F.CRKCO, F.CRN001, F.CRURCD, F.CRN001, F.CRURCD, F.CRMCU, F.CRAN8, F.CRSHAN, 'USD' CRCRCD, F.CRURDT, '" + STCD + "' STCD, '-' CRSREF01, '-' CRSREF02, '-' CRSREF03, '-' CRSREF04, '-' CRSREF05, 'F5504001' TABLA, TRIM(F.CRGL01) NRTXA1, NVL(TRIM(G.ABAC30),'-') ABAC30 "
                    + "FROM " + esquema + ".F5504001@" + dblink + " F LEFT JOIN " + esquema + ".F0101@" + dblink + " G ON (F.CRAN8=G.ABAN8) "
                    + "WHERE (TRIM(F.CRKCO) IN (SELECT C.KCOO_JDE FROM EMISOR_KCOO_V3 C)) AND (F.CRN001 > 0) AND (TRIM(F.CREV01) IN ('N')) AND (F.CRURDT >= " + ivd + ")";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                String DCTO = "CR";

                Boolean FEDOCO = false;
                cadenasql = "SELECT F.FEDOCO "
                        + "FROM " + esquema + ".F5542FEL@" + dblink + " F "
                        + "WHERE "
                        + "F.FEKCOO='" + rs.getString(1) + "' AND "
                        + "F.FEDOCO=" + rs.getString(2) + " AND "
                        + "F.FEDCTO='" + DCTO + "' AND "
                        + "F.FEDOC=" + rs.getString(4) + " AND "
                        + "F.FEDCT='" + rs.getString(5) + "'";
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery(cadenasql);
                while (rs1.next()) {
                    FEDOCO = true;
                }
                rs1.close();
                stmt1.close();

                if (!FEDOCO) {
                    cadenasql = "INSERT INTO " + esquema + ".F5542FEL@" + dblink + " ("
                            + "FEKCOO, "
                            + "FEDOCO, "
                            + "FEDCTO, "
                            + "FEDOC, "
                            + "FEDCT, "
                            + "FEMCU, "
                            + "FEAN8, "
                            + "FESHAN, "
                            + "FECRCD,"
                            + "FEIVD, "
                            + "FESTCD, "
                            + "FECRSREF01, "
                            + "FECRSREF02, "
                            + "FECRSREF03, "
                            + "FECRSREF04, "
                            + "FECRSREF05, "
                            + "FEJEVER, "
                            + "FERCD, "
                            + "FEAEXP, "
                            + "FETXA1) VALUES ('"
                            + rs.getString(1) + "','"
                            + rs.getString(2) + "','"
                            + DCTO + "','"
                            + rs.getString(4) + "','"
                            + rs.getString(5) + "','"
                            + rs.getString(6) + "','"
                            + rs.getString(7) + "','"
                            + rs.getString(8) + "','"
                            + rs.getString(9) + "','"
                            + rs.getString(10) + "','"
                            + rs.getString(11) + "','"
                            + rs.getString(12) + "','"
                            + rs.getString(13) + "','"
                            + rs.getString(14) + "','"
                            + rs.getString(15) + "','"
                            + rs.getString(16) + "','"
                            + rs.getString(17) + "',"
                            + "null" + ","
                            + "0" + ",'"
                            + rs.getString(18) + "')";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();

                    cadenasql = "UPDATE " + esquema + ".F5504001@" + dblink + " "
                            + "SET CREV01='P' "
                            + "WHERE CRKCO='" + rs.getString(1) + "' AND CRN001=" + rs.getString(2) + " AND CRURCD='" + rs.getString(3) + "'";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();
                    resultado++;
                }
            }
            rs.close();
            stmt.close();

            // EXTRAE NOTAS DE REMISIÓN - IATA DESDE JDE.
            Cliente_Rest_IATA cliente_rest_iata = new Cliente_Rest_IATA();
            String json_iata;
            if (ambiente.equals("PY")) {
                json_iata = cliente_rest_iata.genticketszq();
            } else {
                json_iata = cliente_rest_iata.genticketszq_prod();
            }

            List<NR_ZQ_IATA> lista_nr_zq_iata = new ArrayList<>();
            try {
                Type respuesta_nr_zq_iata_type = new TypeToken<List<NR_ZQ_IATA>>() {
                }.getType();
                lista_nr_zq_iata = new Gson().fromJson(json_iata, respuesta_nr_zq_iata_type);
            } catch (Exception ex_iata) {
                System.out.println("WS-IATA-ZQ: " + ex_iata.toString());
            }

            String ordenes_zq_iata = "";
            for (Integer i = 0; i < lista_nr_zq_iata.size(); i++) {
                ordenes_zq_iata = ordenes_zq_iata + lista_nr_zq_iata.get(i).getOrdernumber() + ",";
            }

            if (lista_nr_zq_iata.isEmpty()) {
                ordenes_zq_iata = "-111";
            } else {
                ordenes_zq_iata = ordenes_zq_iata.substring(0, (ordenes_zq_iata.length() - 1));
            }

            cadenasql = "SELECT DISTINCT F.SDKCOO, F.SDDOCO, F.SDDCTO, F.SDDOCO SDDOC, 'IA' SDDCT, F.SDMCU, F.SDAN8, F.SDSHAN, F.SDCRCD, F.SDTRDJ, '" + STCD + "' STCD, '-' CRSREF01, '-' CRSREF02, '-' CRSREF03, '-' CRSREF04, '-' CRSREF05, '" + tabla_sales_orders + "' TABLA, TRIM(F.SDTXA1) SDTXA1, NVL(TRIM(G.ABAC30),'-') ABAC30 "
                    + "FROM " + esquema + ".F4211@" + dblink + " F LEFT JOIN " + esquema + ".F0101@" + dblink + " G ON (F.SDSHAN=G.ABAN8) "
                    + "WHERE (TRIM(F.SDKCOO) IN (SELECT C.KCOO_JDE FROM EMISOR_KCOO_V3 C)) AND (TRIM(F.SDLTTR) NOT IN ('904','902','900','980')) AND (TRIM(F.SDDCTO) IN ('ZQ')) AND (F.SDDOCO IN (" + ordenes_zq_iata + ")) AND (TRIM(F.SDCRMD) IS NULL) AND (F.SDTRDJ >= " + ivd + ")";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                String DCTO = "NI";

                Boolean FEDOCO = false;
                cadenasql = "SELECT F.FEDOCO "
                        + "FROM " + esquema + ".F5542FEL@" + dblink + " F "
                        + "WHERE "
                        + "F.FEKCOO='" + rs.getString(1) + "' AND "
                        + "F.FEDOCO=" + rs.getString(2) + " AND "
                        + "F.FEDCTO='" + DCTO + "' AND "
                        + "F.FEDOC=" + rs.getString(4) + " AND "
                        + "F.FEDCT='" + rs.getString(5) + "'";
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery(cadenasql);
                while (rs1.next()) {
                    FEDOCO = true;
                }
                rs1.close();
                stmt1.close();

                if (!FEDOCO) {
                    cadenasql = "INSERT INTO " + esquema + ".F5542FEL@" + dblink + " ("
                            + "FEKCOO, "
                            + "FEDOCO, "
                            + "FEDCTO, "
                            + "FEDOC, "
                            + "FEDCT, "
                            + "FEMCU, "
                            + "FEAN8, "
                            + "FESHAN, "
                            + "FECRCD,"
                            + "FEIVD, "
                            + "FESTCD, "
                            + "FECRSREF01, "
                            + "FECRSREF02, "
                            + "FECRSREF03, "
                            + "FECRSREF04, "
                            + "FECRSREF05, "
                            + "FEJEVER, "
                            + "FERCD, "
                            + "FEAEXP, "
                            + "FETXA1) VALUES ('"
                            + rs.getString(1) + "','"
                            + rs.getString(2) + "','"
                            + DCTO + "','"
                            + rs.getString(4) + "','"
                            + rs.getString(5) + "','"
                            + rs.getString(6) + "','"
                            + rs.getString(7) + "','"
                            + rs.getString(8) + "','"
                            + rs.getString(9) + "','"
                            + rs.getString(10) + "','"
                            + rs.getString(11) + "','"
                            + rs.getString(12) + "','"
                            + rs.getString(13) + "','"
                            + rs.getString(14) + "','"
                            + rs.getString(15) + "','"
                            + rs.getString(16) + "','"
                            + rs.getString(17) + "',"
                            + "null" + ","
                            + "0" + ",'"
                            + rs.getString(18) + "')";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();

                    cadenasql = "UPDATE " + esquema + ".F4211@" + dblink + " "
                            + "SET SDCRMD='4' "
                            + "WHERE SDKCOO='" + rs.getString(1) + "' AND SDDOCO=" + rs.getString(2) + " AND SDDCTO='" + rs.getString(3) + "' AND SDDOC=" + rs.getString(4) + " AND SDDCT='" + rs.getString(5) + "'";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();
                    resultado++;
                }
            }
            rs.close();
            stmt.close();
            
            // EXTRAE FACTURAS DE SUJETO EXCLUIDO DESDE JDE.
            cadenasql = "SELECT DISTINCT F.NRKCOO, F.NRDOCO, F.NRDCTO, F.NRN001, F.NRURCD, F.NRMCU, F.NRAN8, F.NRSHAN, F.NRCRCD, F.NRURDT, '" + STCD + "' STCD, '-' CRSREF01, '-' CRSREF02, '-' CRSREF03, '-' CRSREF04, '-' CRSREF05, 'F554211N' TABLA, TRIM(F.NRTXA1) NRTXA1, NVL(TRIM(G.ABAC30),'-') ABAC30 "
                    + "FROM " + esquema + ".F554211N@" + dblink + " F LEFT JOIN " + esquema + ".F0101@" + dblink + " G ON (F.NRAN8=G.ABAN8) "
                    + "WHERE (TRIM(F.NRKCOO) IN (SELECT C.KCOO_JDE FROM EMISOR_KCOO_V3 C)) AND (F.NRN001 > 0) AND (TRIM(F.NRDCTO) IN ('XF')) AND (TRIM(F.NREV01) IN ('N')) AND (F.NRURDT >= " + ivd + ")";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                String DCTO = "FS";

                Boolean FEDOCO = false;
                cadenasql = "SELECT F.FEDOCO "
                        + "FROM " + esquema + ".F5542FEL@" + dblink + " F "
                        + "WHERE "
                        + "F.FEKCOO='" + rs.getString(1) + "' AND "
                        + "F.FEDOCO=" + rs.getString(2) + " AND "
                        + "F.FEDCTO='" + DCTO + "' AND "
                        + "F.FEDOC=" + rs.getString(4) + " AND "
                        + "F.FEDCT='" + rs.getString(5) + "'";
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery(cadenasql);
                while (rs1.next()) {
                    FEDOCO = true;
                }
                rs1.close();
                stmt1.close();

                if (!FEDOCO) {
                    cadenasql = "INSERT INTO " + esquema + ".F5542FEL@" + dblink + " ("
                            + "FEKCOO, "
                            + "FEDOCO, "
                            + "FEDCTO, "
                            + "FEDOC, "
                            + "FEDCT, "
                            + "FEMCU, "
                            + "FEAN8, "
                            + "FESHAN, "
                            + "FECRCD,"
                            + "FEIVD, "
                            + "FESTCD, "
                            + "FECRSREF01, "
                            + "FECRSREF02, "
                            + "FECRSREF03, "
                            + "FECRSREF04, "
                            + "FECRSREF05, "
                            + "FEJEVER, "
                            + "FERCD, "
                            + "FEAEXP, "
                            + "FETXA1) VALUES ('"
                            + rs.getString(1) + "','"
                            + rs.getString(2) + "','"
                            + DCTO + "','"
                            + rs.getString(4) + "','"
                            + rs.getString(5) + "','"
                            + rs.getString(6) + "','"
                            + rs.getString(7) + "','"
                            + rs.getString(8) + "','"
                            + rs.getString(9) + "','"
                            + rs.getString(10) + "','"
                            + rs.getString(11) + "','"
                            + rs.getString(12) + "','"
                            + rs.getString(13) + "','"
                            + rs.getString(14) + "','"
                            + rs.getString(15) + "','"
                            + rs.getString(16) + "','"
                            + rs.getString(17) + "',"
                            + "null" + ","
                            + "0" + ",'"
                            + rs.getString(18) + "')";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();

                    cadenasql = "UPDATE " + esquema + ".F554211N@" + dblink + " "
                            + "SET NREV01='P' "
                            + "WHERE NRKCOO='" + rs.getString(1) + "' AND NRDOCO=" + rs.getString(2) + " AND NRDCTO='" + rs.getString(3) + "' AND NRN001=" + rs.getString(4) + " AND NRURCD='" + rs.getString(5) + "'";
                    stmt1 = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt1.executeUpdate(cadenasql);
                    stmt1.close();
                    resultado++;
                }
            }
            rs.close();
            stmt.close();

            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception ex) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);

                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:selecionar_documentos_v3()|ERROR:" + ex.toString());
                resultado = 0;
            } catch (Exception ex1) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:selecionar_documentos_v3()-rollback|ERROR:" + ex1.toString());
                resultado = 0;
            }

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:selecionar_documentos_v3()-finally|ERROR:" + ex.toString());
            }
        }

        return resultado;
    }

}
