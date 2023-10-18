package Controladores;

import Entidades.Direccion;
import Entidades.Receptor_cr;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_Receptor_CR_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Receptor_CR_V3() {
    }

    public Receptor_cr obtener_receptor_cr_v3(Long id_dte, Connection conn) {
        Receptor_cr resultado = new Receptor_cr();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setTipoDocumento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_022 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_022 FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNumDocumento(ctrl_base_datos.ObtenerString("SELECT F.NUM_DOCUMENTO FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNrc(ctrl_base_datos.ObtenerString("SELECT F.NRC FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCodActividad(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setDescActividad(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNombreComercial(ctrl_base_datos.ObtenerString("SELECT F.NOMBRECOMERCIAL FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));

            Direccion direccion_cr = new Direccion();
            direccion_cr.setDepartamento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_012 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_012 FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            direccion_cr.setMunicipio(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_013 FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            direccion_cr.setComplemento(ctrl_base_datos.ObtenerString("SELECT F.DIRECCION_COMPLEMENTO FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDireccion(direccion_cr);

            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_receptor_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_receptor_jde_cr_v3(Long id_dte, String ambiente, String AN8_JDE, String SHAN_JDE, String KCOO_JDE, String DOCO_JDE, String DCTO_JDE, Connection conn) {
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

            Long ID_CAT_022;
            String NUM_DOCUMENTO;
            String NRC;
            String NOMBRE;
            Long ID_CAT_019;
            String NOMBRECOMERCIAL;
            Long ID_CAT_012;
            Long ID_CAT_013;
            String CODIGO_CAT_013;
            String DIRECCION_COMPLEMENTO;
            String TELEFONO;
            String CORREO;

            if (!AN8_JDE.trim().equals("0")) {
                ID_CAT_022 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_022 C WHERE C.VALOR_JDE IN (SELECT NVL(TRIM(F.WWREM1),'36') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
                if (ID_CAT_022 == null) {
                    ID_CAT_022 = Long.valueOf("1");
                }

                NUM_DOCUMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTX2),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + AN8_JDE, conn);
                NUM_DOCUMENTO = NUM_DOCUMENTO.replaceAll(" ", "");

                if (ID_CAT_022 == Long.valueOf("1")) {
                    NRC = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTAX),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + AN8_JDE, conn);
                    NRC = NRC.replaceAll(" ", "");
                    if (NRC.length() > 8) {
                        NRC = NRC.substring(0, 8);
                    }
                } else {
                    NRC = "NULL";
                }

                NOMBRE = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.WWMLNM),'-') || (SELECT NVL(TRIM(F.ALADD1),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + AN8_JDE + ")) NOMBRE_FISCAL FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + AN8_JDE, conn);
                NOMBRE = NOMBRE.replaceAll("'", "");

                ID_CAT_019 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC12) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + AN8_JDE + ")", conn);
                if (ID_CAT_019 == null) {
                    ID_CAT_019 = Long.valueOf("772");
                }

                NOMBRECOMERCIAL = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWALPH),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + AN8_JDE, conn);
                NOMBRECOMERCIAL = NOMBRECOMERCIAL.replaceAll("'", "");

                ID_CAT_012 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALADDS) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + AN8_JDE + ")", conn);

                ID_CAT_013 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALCOUN) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + AN8_JDE + ")", conn);

                CODIGO_CAT_013 = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013 + " AND C.ID_CAT_012=" + ID_CAT_012, conn);
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
            } else {
                String CRTAX = ctrl_base_datos.ObtenerString("SELECT TRIM(F.CRTAX) FROM " + esquema + ".F5504001@" + dblink + " F WHERE F.CRKCO='" + KCOO_JDE + "' AND F.CRN001=" + DOCO_JDE + " AND F.CRURCD='" + DCTO_JDE + "'", conn);

                ID_CAT_022 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_022 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.CRVR04) FROM " + esquema + ".F5504001@" + dblink + " F WHERE F.CRKCO='" + KCOO_JDE + "' AND F.CRN001=" + DOCO_JDE + " AND F.CRURCD='" + DCTO_JDE + "')", conn);
                if (ID_CAT_022 == null) {
                    ID_CAT_022 = Long.valueOf("1");
                }

                NUM_DOCUMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.CRTX2),'-',''),'-') FROM " + esquema + ".F5504001@" + dblink + " F WHERE F.CRKCO='" + KCOO_JDE + "' AND F.CRN001=" + DOCO_JDE + " AND F.CRURCD='" + DCTO_JDE + "'", conn);
                NUM_DOCUMENTO = NUM_DOCUMENTO.replaceAll(" ", "");

                if (ID_CAT_022 == Long.valueOf("1")) {
                    NRC = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.CRTAX),'-',''),'-') FROM " + esquema + ".F5504001@" + dblink + " F WHERE F.CRKCO='" + KCOO_JDE + "' AND F.CRN001=" + DOCO_JDE + " AND F.CRURCD='" + DCTO_JDE + "'", conn);
                    NRC = NRC.replaceAll(" ", "");
                    if (NRC.length() > 8) {
                        NRC = NRC.substring(0, 8);
                    }
                } else {
                    NRC = "NULL";
                }

                NOMBRE = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.A9MLNM),'-')) FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + CRTAX + "'", conn);
                NOMBRE = NOMBRE.replaceAll("'", "");

                ID_CAT_019 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT NVL(TRIM(F.A9VR01),'-') FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + CRTAX + "')", conn);
                if (ID_CAT_019 == null) {
                    ID_CAT_019 = Long.valueOf("772");
                }

                NOMBRECOMERCIAL = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.A9ALPH),'-')) FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + CRTAX + "'", conn);
                NOMBRECOMERCIAL = NOMBRECOMERCIAL.replaceAll("'", "");

                ID_CAT_012 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.A9ADDS) FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + CRTAX + "')", conn);

                ID_CAT_013 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.A9COUN) FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + CRTAX + "')", conn);

                CODIGO_CAT_013 = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013 + " AND C.ID_CAT_012=" + ID_CAT_012, conn);
                if (CODIGO_CAT_013 == null) {
                    ID_CAT_012 = Long.valueOf("6");
                    ID_CAT_013 = Long.valueOf("111");
                }

                DIRECCION_COMPLEMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.A9ADD2),' ') || ' ' || NVL(TRIM(F.A9ADD3),' ') FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + CRTAX + "'", conn);
                if (DIRECCION_COMPLEMENTO == null) {
                    DIRECCION_COMPLEMENTO = "Sin dirección registrada en el código del cliente";
                }

                TELEFONO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.A9VR04),'-') PHONE FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + CRTAX + "'", conn);
                if (TELEFONO == null) {
                    TELEFONO = "25288000";
                }

                CORREO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.A9EMAIL),'sinregistro@terra-uno.com') EMAIL FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + CRTAX + "'", conn);
                if (CORREO == null) {
                    CORREO = "sinregistro@terra-uno.com";
                }
            }

            String cadenasql = "INSERT INTO RECEPTOR_CR_V3 ("
                    + "ID_DTE, "
                    + "ID_RECEPTOR, "
                    + "ID_CAT_022, "
                    + "NUM_DOCUMENTO, "
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
                    + ID_RECEPTOR + ","
                    + ID_CAT_022 + ",'"
                    + NUM_DOCUMENTO + "',"
                    + NRC + ",'"
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
            Long ID_CAT_022_SHAN = ID_CAT_022;
            String NUM_DOCUMENTO_SHAN = NUM_DOCUMENTO;
            String NRC_SHAN = NRC;
            String NOMBRE_SHAN = NOMBRE;
            Long ID_CAT_019_SHAN = ID_CAT_019;
            String NOMBRECOMERCIAL_SHAN = NOMBRECOMERCIAL;
            Long ID_CAT_012_SHAN = ID_CAT_012;
            Long ID_CAT_013_SHAN = ID_CAT_013;
            String DIRECCION_COMPLEMENTO_SHAN = DIRECCION_COMPLEMENTO;
            String TELEFONO_SHAN = TELEFONO;
            String CORREO_SHAN = CORREO;

            cadenasql = "INSERT INTO SHIPTO_CR_V3 ("
                    + "ID_DTE, "
                    + "ID_SHIPTO, "
                    + "ID_CAT_022, "
                    + "NUM_DOCUMENTO, "
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
                    + ID_SHIPTO + ","
                    + ID_CAT_022_SHAN + ",'"
                    + NUM_DOCUMENTO_SHAN + "',"
                    + NRC_SHAN + ",'"
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
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_receptor_jde_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
