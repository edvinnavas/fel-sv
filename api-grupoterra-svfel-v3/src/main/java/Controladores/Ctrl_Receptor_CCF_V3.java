package Controladores;

import Entidades.Direccion;
import Entidades.Receptor_ccf;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_Receptor_CCF_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Receptor_CCF_V3() {
    }

    public Receptor_ccf obtener_receptor_ccf_v3(Long id_dte, Connection conn) {
        Receptor_ccf resultado = new Receptor_ccf();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setNit(ctrl_base_datos.ObtenerString("SELECT F.NIT FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNrc(ctrl_base_datos.ObtenerString("SELECT F.NRC FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCodActividad(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setDescActividad(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNombreComercial(ctrl_base_datos.ObtenerString("SELECT F.NOMBRECOMERCIAL FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte, conn));

            Direccion direccion_ccf = new Direccion();
            direccion_ccf.setDepartamento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_012 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_012 FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            direccion_ccf.setMunicipio(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_013 FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            direccion_ccf.setComplemento(ctrl_base_datos.ObtenerString("SELECT F.DIRECCION_COMPLEMENTO FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDireccion(direccion_ccf);

            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCorreo("pruebasecsasv@servicioscompartidos.com");
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_receptor_ccf_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_receptor_jde_ccf_v3(Long id_dte, String ambiente, String AN8_JDE, String SHAN_JDE, String DOCO_JDE, String DCTO_JDE, Connection conn) {
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

            Long ID_DTE = id_dte;

            Long ID_RECEPTOR = Long.valueOf("1");
            String NIT;
            String NRC;
            String NOMBRE;
            Long ID_CAT_019 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC12) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + AN8_JDE + ")", conn);
            if (ID_CAT_019 == null) {
                ID_CAT_019 = Long.valueOf("772");
            }
            String NOMBRECOMERCIAL;
            Long ID_CAT_012;
            Long ID_CAT_013;
            String DIRECCION_COMPLEMENTO;
            String TELEFONO;
            String CORREO;

            if (AN8_JDE.equals("727353") || AN8_JDE.equals("726829") || AN8_JDE.equals("726647") || AN8_JDE.equals("721956")) {
                NIT = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.OAADD1),'-',''),'-') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=2", conn);
                NIT = NIT.replaceAll(" ", "");
                if (NIT.length() > 14) {
                    NIT = NIT.substring(0, 14);
                }
                NRC = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.OAMLNM),'-',''),'-') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=2", conn);
                NRC = NRC.replaceAll(" ", "");
                if (NRC.length() > 8) {
                    NRC = NRC.substring(0, 8);
                }
                NOMBRE = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAMLNM),'-') || NVL(TRIM(F.OAADD1),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1", conn);
                NOMBRE = NOMBRE.replaceAll("'", "");

                NOMBRECOMERCIAL = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAMLNM),'-') || NVL(TRIM(F.OAADD1),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1", conn);
                NOMBRECOMERCIAL = NOMBRECOMERCIAL.replaceAll("'", "");

                ID_CAT_012 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.OAADDS) FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1)", conn);
                ID_CAT_013 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.OACOUN) FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1)", conn);
                String CODIGO_CAT_013 = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013 + " AND C.ID_CAT_012=" + ID_CAT_012, conn);
                if (CODIGO_CAT_013 == null) {
                    ID_CAT_012 = Long.valueOf("6");
                    ID_CAT_013 = Long.valueOf("111");
                }
                DIRECCION_COMPLEMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAADD2),' ') || ' ' || NVL(TRIM(F.OAADD3),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1", conn);
                if (DIRECCION_COMPLEMENTO == null) {
                    DIRECCION_COMPLEMENTO = "Sin dirección registrada en el código del cliente";
                }
                TELEFONO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAADD4),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=2", conn);
                if (TELEFONO == null) {
                    TELEFONO = "25288000";
                }
                CORREO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAADD3),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=2", conn);
                if (CORREO == null) {
                    CORREO = "sinregistro@terra-uno.com";
                }
            } else {
                NIT = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTX2),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + AN8_JDE, conn);
                NIT = NIT.replaceAll(" ", "");
                if (NIT.length() > 14) {
                    NIT = NIT.substring(0, 14);
                }
                NRC = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTAX),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + AN8_JDE, conn);
                NRC = NRC.replaceAll(" ", "");
                if (NRC.length() > 8) {
                    NRC = NRC.substring(0, 8);
                }
                NOMBRE = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.WWMLNM),'-') || (SELECT NVL(TRIM(F.ALADD1),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + AN8_JDE + ")) NOMBRE_FISCAL FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + AN8_JDE, conn);
                NOMBRE = NOMBRE.replaceAll("'", "");

                NOMBRECOMERCIAL = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWALPH),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + AN8_JDE, conn);
                NOMBRECOMERCIAL = NOMBRECOMERCIAL.replaceAll("'", "");

                ID_CAT_012 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALADDS) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + AN8_JDE + ")", conn);
                ID_CAT_013 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALCOUN) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + AN8_JDE + ")", conn);
                String CODIGO_CAT_013 = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013 + " AND C.ID_CAT_012=" + ID_CAT_012, conn);
                if (CODIGO_CAT_013 == null) {
                    ID_CAT_012 = Long.valueOf("6");
                    ID_CAT_013 = Long.valueOf("111");
                }
                DIRECCION_COMPLEMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.ALADD2),' ') || ' ' || NVL(TRIM(F.ALADD3),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + AN8_JDE, conn);
                if (DIRECCION_COMPLEMENTO == null) {
                    DIRECCION_COMPLEMENTO = "Sin dirección registrada en el código del cliente";
                }
                TELEFONO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WPAR1) || TRIM(F.WPPH1),'-') PHONE FROM " + esquema + ".F0115@" + dblink + " F WHERE (F.WPAN8, F.WPIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
                if (TELEFONO == null) {
                    TELEFONO = "25288000";
                }
                CORREO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.EAEMAL),'-') EMAIL FROM " + esquema + ".F01151@" + dblink + " F WHERE TRIM(F.EAETP)='E' AND (F.EAAN8, F.EAIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
                if (CORREO == null) {
                    CORREO = "sinregistro@terra-uno.com";
                }
            }

            String cadenasql = "INSERT INTO RECEPTOR_CCF_V3 ("
                    + "ID_DTE, "
                    + "ID_RECEPTOR, "
                    + "NIT, "
                    + "NRC, "
                    + "NOMBRE, "
                    + "ID_CAT_019, "
                    + "NOMBRECOMERCIAL, "
                    + "ID_CAT_012, "
                    + "ID_CAT_013, "
                    + "DIRECCION_COMPLEMENTO, "
                    + "TELEFONO, "
                    + "CORREO) VALUES ("
                    + ID_DTE + ","
                    + ID_RECEPTOR + ",'"
                    + NIT + "','"
                    + NRC + "','"
                    + NOMBRE + "',"
                    + ID_CAT_019 + ",'"
                    + NOMBRECOMERCIAL + "',"
                    + ID_CAT_012 + ","
                    + ID_CAT_013 + ",'"
                    + DIRECCION_COMPLEMENTO + "','"
                    + TELEFONO + "','"
                    + CORREO + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            Long ID_SHIPTO = Long.valueOf("1");
            String NIT_SHAN;
            String NRC_SHAN;
            String NOMBRE_SHAN;
            Long ID_CAT_019_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC12) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + SHAN_JDE + ")", conn);
            if (ID_CAT_019_SHAN == null) {
                ID_CAT_019_SHAN = Long.valueOf("772");
            }
            String NOMBRECOMERCIAL_SHAN;
            Long ID_CAT_012_SHAN;
            Long ID_CAT_013_SHAN;
            String DIRECCION_COMPLEMENTO_SHAN;
            String TELEFONO_SHAN;
            String CORREO_SHAN;

            if (SHAN_JDE.equals("727353") || SHAN_JDE.equals("726829") || SHAN_JDE.equals("726647") || SHAN_JDE.equals("721956")) {
                NIT_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.OAADD1),'-',''),'-') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=2", conn);
                NIT_SHAN = NIT_SHAN.replaceAll(" ", "");
                if (NIT_SHAN.length() > 14) {
                    NIT_SHAN = NIT_SHAN.substring(0, 14);
                }
                NRC_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.OAMLNM),'-',''),'-') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=2", conn);
                NRC_SHAN = NRC_SHAN.replaceAll(" ", "");
                if (NRC_SHAN.length() > 8) {
                    NRC_SHAN = NRC_SHAN.substring(0, 8);
                }
                NOMBRE_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAMLNM),'-') || NVL(TRIM(F.OAADD1),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1", conn);
                NOMBRE_SHAN = NOMBRE_SHAN.replaceAll("'", "");

                NOMBRECOMERCIAL_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAMLNM),'-') || NVL(TRIM(F.OAADD1),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1", conn);
                NOMBRECOMERCIAL_SHAN = NOMBRECOMERCIAL_SHAN.replaceAll("'", "");

                ID_CAT_012_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.OAADDS) FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1)", conn);
                ID_CAT_013_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.OACOUN) FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1)", conn);
                String CODIGO_CAT_013_SHAN = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013_SHAN + " AND C.ID_CAT_012=" + ID_CAT_012_SHAN, conn);
                if (CODIGO_CAT_013_SHAN == null) {
                    ID_CAT_012_SHAN = Long.valueOf("6");
                    ID_CAT_013_SHAN = Long.valueOf("111");
                }
                DIRECCION_COMPLEMENTO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAADD2),' ') || ' ' || NVL(TRIM(F.OAADD3),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=1", conn);
                if (DIRECCION_COMPLEMENTO_SHAN == null) {
                    DIRECCION_COMPLEMENTO_SHAN = "Sin dirección registrada en el código del cliente";
                }
                TELEFONO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAADD4),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=2", conn);
                if (TELEFONO_SHAN == null) {
                    TELEFONO_SHAN = "25288000";
                }
                CORREO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.OAADD3),' ') FROM " + esquema + ".F4006@" + dblink + " F WHERE F.OADOCO=" + DOCO_JDE + " AND F.OADCTO='" + DCTO_JDE + "' AND F.OAANTY=2", conn);
                if (CORREO_SHAN == null) {
                    CORREO_SHAN = "sinregistro@terra-uno.com";
                }
            } else {
                NIT_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTX2),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + SHAN_JDE, conn);
                NIT_SHAN = NIT_SHAN.replaceAll(" ", "");
                if (NIT_SHAN.length() > 14) {
                    NIT_SHAN = NIT_SHAN.substring(0, 14);
                }
                NRC_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTAX),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + SHAN_JDE, conn);
                NRC_SHAN = NRC_SHAN.replaceAll(" ", "");
                if (NRC_SHAN.length() > 8) {
                    NRC_SHAN = NRC_SHAN.substring(0, 8);
                }
                NOMBRE_SHAN = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.WWMLNM),'-') || (SELECT NVL(TRIM(F.ALADD1),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + SHAN_JDE + ")) NOMBRE_FISCAL FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + SHAN_JDE, conn);
                NOMBRE_SHAN = NOMBRE_SHAN.replaceAll("'", "");

                NOMBRECOMERCIAL_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWALPH),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + SHAN_JDE, conn);
                NOMBRECOMERCIAL_SHAN = NOMBRECOMERCIAL_SHAN.replaceAll("'", "");

                ID_CAT_012_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALADDS) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + SHAN_JDE + ")", conn);
                ID_CAT_013_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALCTY1) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + SHAN_JDE + ")", conn);
                String CODIGO_CAT_013_SHAN = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013_SHAN + " AND C.ID_CAT_012=" + ID_CAT_012_SHAN, conn);
                if (CODIGO_CAT_013_SHAN == null) {
                    ID_CAT_012_SHAN = Long.valueOf("6");
                    ID_CAT_013_SHAN = Long.valueOf("111");
                }
                DIRECCION_COMPLEMENTO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.ALADD2),' ') || ' ' || NVL(TRIM(F.ALADD3),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + SHAN_JDE, conn);
                if (DIRECCION_COMPLEMENTO_SHAN == null) {
                    DIRECCION_COMPLEMENTO_SHAN = "Sin dirección registrada en el código del cliente";
                }
                TELEFONO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WPAR1) || TRIM(F.WPPH1),'-') PHONE FROM " + esquema + ".F0115@" + dblink + " F WHERE (F.WPAN8, F.WPIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + SHAN_JDE + " AND TRIM(F.WWTYC)='S')", conn);
                if (TELEFONO_SHAN == null) {
                    TELEFONO_SHAN = "25288000";
                }
                CORREO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.EAEMAL),'-') EMAIL FROM " + esquema + ".F01151@" + dblink + " F WHERE TRIM(F.EAETP)='E' AND (F.EAAN8, F.EAIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + SHAN_JDE + " AND TRIM(F.WWTYC)='S')", conn);
                if (CORREO_SHAN == null) {
                    CORREO_SHAN = "sinregistro@terra-uno.com";
                }
            }

            cadenasql = "INSERT INTO SHIPTO_CCF_V3 ("
                    + "ID_DTE, "
                    + "ID_SHIPTO, "
                    + "NIT, "
                    + "NRC, "
                    + "NOMBRE, "
                    + "ID_CAT_019, "
                    + "NOMBRECOMERCIAL, "
                    + "ID_CAT_012, "
                    + "ID_CAT_013, "
                    + "DIRECCION_COMPLEMENTO, "
                    + "TELEFONO, "
                    + "CORREO) VALUES ("
                    + ID_DTE + ","
                    + ID_SHIPTO + ",'"
                    + NIT_SHAN + "','"
                    + NRC_SHAN + "','"
                    + NOMBRE_SHAN + "',"
                    + ID_CAT_019_SHAN + ",'"
                    + NOMBRECOMERCIAL_SHAN + "',"
                    + ID_CAT_012_SHAN + ","
                    + ID_CAT_013_SHAN + ",'"
                    + DIRECCION_COMPLEMENTO_SHAN + "','"
                    + TELEFONO_SHAN + "','"
                    + CORREO_SHAN + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_receptor_jde_ccf_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
