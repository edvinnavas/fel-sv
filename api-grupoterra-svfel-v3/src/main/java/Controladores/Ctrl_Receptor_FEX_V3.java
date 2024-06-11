package Controladores;

import Entidades.Receptor_fex;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_Receptor_FEX_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Receptor_FEX_V3() {
    }

    public Receptor_fex obtener_receptor_fex_v3(Long id_dte, Connection conn) {
        Receptor_fex resultado = new Receptor_fex();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTipoDocumento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_022 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_022 FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNumDocumento(ctrl_base_datos.ObtenerString("SELECT F.NUM_DOCUMENTO FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNombreComercial(ctrl_base_datos.ObtenerString("SELECT F.NOMBRECOMERCIAL FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCodPais(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_020 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_020 FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNombrePais(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_020 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_020 FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setComplemento(ctrl_base_datos.ObtenerString("SELECT F.COMPLEMENTO FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setTipoPersona(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_029 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_029 FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setDescActividad(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCorreo("pruebasecsasv@servicioscompartidos.com");
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_receptor_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_receptor_jde_fex_v3(Long id_dte, String ambiente, String AN8_JDE, String SHAN_JDE, Connection conn) {
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
            Long ID_RECEPTOR = Long.valueOf("1");

            String NOMBRE = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.WWMLNM),'-') || (SELECT NVL(TRIM(F.ALADD1),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + AN8_JDE + ")) NOMBRE_FISCAL FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + AN8_JDE, conn);
            NOMBRE = NOMBRE.replaceAll("'", "");

            Long ID_CAT_022 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_022 C WHERE C.VALOR_JDE IN (SELECT NVL(TRIM(F.WWREM1),'36') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if(ID_CAT_022 == null) {
                ID_CAT_022 = Long.valueOf("1");
            }
            String NUM_DOCUMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTX2),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + AN8_JDE, conn);
            NUM_DOCUMENTO = NUM_DOCUMENTO.replaceAll(" ", "");

            String NOMBRECOMERCIAL = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWALPH),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + AN8_JDE, conn);
            NOMBRECOMERCIAL = NOMBRECOMERCIAL.replaceAll("'", "");

            Long ID_CAT_020 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_020 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALCTR) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + AN8_JDE + ")", conn);

            String COMPLEMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.ALADD2),' ') || ' ' || NVL(TRIM(F.ALADD3),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + AN8_JDE, conn);
            if (COMPLEMENTO == null) {
                COMPLEMENTO = "Sin dirección registrada en el código del cliente";
            }

            Long ID_CAT_029 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_029 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABTAXC) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + AN8_JDE + ")", conn);
            if (ID_CAT_029 == null) {
                ID_CAT_029 = Long.valueOf("1");
            }

            Long ID_CAT_019 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC12) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + AN8_JDE + ")", conn);
            if (ID_CAT_019 == null) {
                ID_CAT_019 = Long.valueOf("772");
            }

            String TELEFONO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WPAR1) || TRIM(F.WPPH1),'-') PHONE FROM " + esquema + ".F0115@" + dblink + " F WHERE (F.WPAN8, F.WPIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if (TELEFONO == null) {
                TELEFONO = "25288000";
            }
            String CORREO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.EAEMAL),'-') EMAIL FROM " + esquema + ".F01151@" + dblink + " F WHERE TRIM(F.EAETP)='E' AND (F.EAAN8, F.EAIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if (CORREO == null) {
                CORREO = "sinregistro@terra-uno.com";
            }

            String cadenasql = "INSERT INTO RECEPTOR_FEX_V3 ("
                    + "ID_DTE, "
                    + "ID_RECEPTOR, "
                    + "NOMBRE, "
                    + "ID_CAT_022, "
                    + "NUM_DOCUMENTO, "
                    + "NOMBRECOMERCIAL, "
                    + "ID_CAT_020, "
                    + "COMPLEMENTO, "
                    + "ID_CAT_029, "
                    + "ID_CAT_019, "
                    + "TELEFONO, "
                    + "CORREO) VALUES ("
                    + ID_DTE + ","
                    + ID_RECEPTOR + ",'"
                    + NOMBRE + "',"
                    + ID_CAT_022 + ",'"
                    + NUM_DOCUMENTO + "','"
                    + NOMBRECOMERCIAL + "',"
                    + ID_CAT_020 + ",'"
                    + COMPLEMENTO + "',"
                    + ID_CAT_029 + ","
                    + ID_CAT_019 + ",'"
                    + TELEFONO + "','"
                    + CORREO + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            Long ID_SHIPTO = Long.valueOf("1");

            String NOMBRE_SHAN = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.WWMLNM),'-') || (SELECT NVL(TRIM(F.ALADD1),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + SHAN_JDE + ")) NOMBRE_FISCAL FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + SHAN_JDE, conn);
            NOMBRE_SHAN = NOMBRE_SHAN.replaceAll("'", "");

            Long ID_CAT_022_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_022 C WHERE C.VALOR_JDE IN (SELECT NVL(TRIM(F.WWREM1),'36') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + SHAN_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if(ID_CAT_022_SHAN == null) {
                ID_CAT_022_SHAN = Long.valueOf("1");
            }
            String NUM_DOCUMENTO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTX2),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + SHAN_JDE, conn);
            NUM_DOCUMENTO_SHAN = NUM_DOCUMENTO_SHAN.replaceAll(" ", "");

            String NOMBRECOMERCIAL_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWALPH),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + SHAN_JDE, conn);
            NOMBRECOMERCIAL_SHAN = NOMBRECOMERCIAL_SHAN.replaceAll("'", "");

            Long ID_CAT_020_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_020 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALCTR) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + SHAN_JDE + ")", conn);

            String COMPLEMENTO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.ALADD2),' ') || ' ' || NVL(TRIM(F.ALADD3),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + SHAN_JDE, conn);
            if (COMPLEMENTO_SHAN == null) {
                COMPLEMENTO_SHAN = "Sin dirección registrada en el código del cliente";
            }

            Long ID_CAT_029_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_029 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABTAXC) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + SHAN_JDE + ")", conn);

            Long ID_CAT_019_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC12) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + SHAN_JDE + ")", conn);
            if (ID_CAT_019_SHAN == null) {
                ID_CAT_019_SHAN = Long.valueOf("772");
            }

            String TELEFONO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WPAR1) || TRIM(F.WPPH1),'-') PHONE FROM " + esquema + ".F0115@" + dblink + " F WHERE (F.WPAN8, F.WPIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + SHAN_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if (TELEFONO_SHAN == null) {
                TELEFONO_SHAN = "25288000";
            }
            String CORREO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.EAEMAL),'-') EMAIL FROM " + esquema + ".F01151@" + dblink + " F WHERE TRIM(F.EAETP)='E' AND (F.EAAN8, F.EAIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + SHAN_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if (CORREO_SHAN == null) {
                CORREO_SHAN = "sinregistro@terra-uno.com";
            }

            cadenasql = "INSERT INTO SHIPTO_FEX_V3 ("
                    + "ID_DTE, "
                    + "ID_SHIPTO, "
                    + "NOMBRE, "
                    + "ID_CAT_022, "
                    + "NUM_DOCUMENTO, "
                    + "NOMBRECOMERCIAL, "
                    + "ID_CAT_020, "
                    + "COMPLEMENTO, "
                    + "ID_CAT_029, "
                    + "ID_CAT_019, "
                    + "TELEFONO, "
                    + "CORREO) VALUES ("
                    + ID_DTE + ","
                    + ID_SHIPTO + ",'"
                    + NOMBRE_SHAN + "',"
                    + ID_CAT_022_SHAN + ",'"
                    + NUM_DOCUMENTO_SHAN + "','"
                    + NOMBRECOMERCIAL_SHAN + "',"
                    + ID_CAT_020_SHAN + ",'"
                    + COMPLEMENTO_SHAN + "',"
                    + ID_CAT_029_SHAN + ","
                    + ID_CAT_019_SHAN + ",'"
                    + TELEFONO_SHAN + "','"
                    + CORREO_SHAN + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_receptor_jde_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
