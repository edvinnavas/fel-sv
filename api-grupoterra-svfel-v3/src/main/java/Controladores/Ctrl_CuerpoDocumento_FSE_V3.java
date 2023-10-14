package Controladores;

import Entidades.CuerpoDocumento_fse;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_CuerpoDocumento_FSE_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_CuerpoDocumento_FSE_V3() {
    }

    public List<CuerpoDocumento_fse> obtener_cuerpo_documento_fse_v3(Long id_dte, Connection conn) {
        List<CuerpoDocumento_fse> resultado = new ArrayList<>();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            String cadenasql = "SELECT F.ID_CUERPO_DOCUMENTO FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " ORDER BY F.ID_CUERPO_DOCUMENTO";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                Long id_cuerpo_documento = rs.getLong(1);
                CuerpoDocumento_fse cuerpo_documento_fse = new CuerpoDocumento_fse();
                cuerpo_documento_fse.setNumItem(id_cuerpo_documento);
                cuerpo_documento_fse.setTipoItem(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_011 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_011 FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + ")", conn));
                cuerpo_documento_fse.setCantidad(ctrl_base_datos.ObtenerDouble("SELECT F.CANTIDAD FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_fse.setCodigo(ctrl_base_datos.ObtenerString("SELECT F.CODIGO FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_fse.setUniMedida(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_014 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_014 FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + ")", conn));
                cuerpo_documento_fse.setDescripcion(ctrl_base_datos.ObtenerString("SELECT F.DESCRIPCION FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_fse.setPrecioUni(ctrl_base_datos.ObtenerDouble("SELECT F.PRCIOUNI FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_fse.setMontoDescu(ctrl_base_datos.ObtenerDouble("SELECT F.MONTODESCU FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_fse.setCompra(ctrl_base_datos.ObtenerDouble("SELECT F.COMPRA FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                resultado.add(cuerpo_documento_fse);
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_cuerpo_documento_fse_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_cuerpo_documento_jde_fse_v3(Long id_dte, String ambiente, String KCOO_JDE, String DOCO_JDE, String DCTO_JDE, String tabla_sales_orders, Connection conn) {
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
                    + "TRIM(F.NRLNTY) tipoItem, "
                    + "F.NRUORG cantidad, "
                    + "NVL(TRIM(F.NRLITM),'XF-001') codigo, "
                    + "NVL(TRIM(F.NRUOM),'EA') uniMedida, "
                    + "TRIM(F.NRBDS4) descripcion, "
                    + "F.NRUPRC/10000 precioUni "
                    + "FROM " + esquema + ".F554211N@" + dblink + " F "
                    + "WHERE F.NRKCOO='" + KCOO_JDE + "' AND F.NRDOCO=" + DOCO_JDE + " AND F.NRDCTO='" + DCTO_JDE + "' AND F.NRLNTY IN ('M','S','SX')";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            Integer contador = 0;
            while (rs.next()) {
                contador++;
                Long ID_DTE = id_dte;
                Long ID_CUERPO_DOCUMENTO = Long.valueOf(contador.toString());
                Long ID_CAT_011 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_011 C WHERE C.VALOR_JDE LIKE '%[" + rs.getString(1) + "]%'", conn);
                Long CANTIDAD = rs.getLong(2);
                if (CANTIDAD < 0.00) {
                    CANTIDAD = CANTIDAD * -1;
                }
                String CODIGO = rs.getString(3);
                Long ID_CAT_014 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_014 C WHERE C.VALOR_JDE LIKE '%[" + rs.getString(4) + "]%'", conn);
                String DESCRIPCION = rs.getString(5);

                // EXTRAE EL PRECIO UNITARIO.
                Number PRECIOUNI = rs.getDouble(6);
                if (PRECIOUNI.doubleValue() < 0.00) {
                    PRECIOUNI = PRECIOUNI.doubleValue() * -1;
                }

                Number MONTODESCU = 0.00;
                Number COMPRA = 0.00;

                cadenasql = "INSERT INTO CUERPO_DOCU_FSE_V3 ( "
                        + "ID_DTE, "
                        + "ID_CUERPO_DOCUMENTO, "
                        + "ID_CAT_011, "
                        + "CANTIDAD, "
                        + "CODIGO, "
                        + "ID_CAT_014, "
                        + "DESCRIPCION, "
                        + "PRCIOUNI, "
                        + "MONTODESCU, "
                        + "COMPRA) VALUES ("
                        + ID_DTE + ","
                        + ID_CUERPO_DOCUMENTO + ","
                        + ID_CAT_011 + ","
                        + CANTIDAD + ",'"
                        + CODIGO + "',"
                        + ID_CAT_014 + ",'"
                        + DESCRIPCION + "',"
                        + PRECIOUNI + ","
                        + MONTODESCU + ","
                        + COMPRA + ")";
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
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_cuerpo_documento_jde_fse_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
