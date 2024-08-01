package Controladores;

import Entidades.Direccion;
import Entidades.Receptor_nr;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_Receptor_NR_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Receptor_NR_V3() {
    }

    public Receptor_nr obtener_receptor_nr_v3(Long id_dte, String ambiente, Connection conn) {
        Receptor_nr resultado = new Receptor_nr();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            resultado.setTipoDocumento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_022 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_022 FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNumDocumento(ctrl_base_datos.ObtenerString("SELECT F.NUM_DOCUMENTO FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNrc(ctrl_base_datos.ObtenerString("SELECT F.NRC FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCodActividad(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setDescActividad(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNombreComercial(ctrl_base_datos.ObtenerString("SELECT F.NOMBRECOMERCIAL FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            
            Direccion direccion_nr = new Direccion();
            direccion_nr.setDepartamento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_012 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_012 FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            direccion_nr.setMunicipio(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_013 FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            direccion_nr.setComplemento(ctrl_base_datos.ObtenerString("SELECT F.DIRECCION_COMPLEMENTO FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDireccion(direccion_nr);
            
            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            if (ambiente.equals("PY")) {
                resultado.setCorreo("pruebasecsasv@servicioscompartidos.com");
            } else {
                resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            }

            resultado.setBienTitulo(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_025 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_025 FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_receptor_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    public String extraer_receptor_jde_nr_v3(Long id_dte, String ambiente, String AN8_JDE, String SHAN_JDE, Connection conn) {
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
            
            Long ID_CAT_022 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_022 C WHERE C.VALOR_JDE IN (SELECT NVL(TRIM(F.WWREM1),'36') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            
            String NUM_DOCUMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTX2),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + AN8_JDE, conn);
            NUM_DOCUMENTO = NUM_DOCUMENTO.replaceAll(" ", "");
            
            String NRC;
            if (ID_CAT_022 == Long.valueOf("1")) {
                NRC = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTAX),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + AN8_JDE, conn);
                NRC = NRC.replaceAll(" ", "");
                if (NRC.length() > 8) {
                    NRC = NRC.substring(0, 8);
                }
            } else {
                NRC = "NULL";
            }
            
            String NOMBRE = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.WWMLNM),'-') || (SELECT NVL(TRIM(F.ALADD1),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + AN8_JDE + ")) NOMBRE_FISCAL FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + AN8_JDE, conn);
            NOMBRE = NOMBRE.replaceAll("'", "");
            
            Long ID_CAT_019 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC12) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + AN8_JDE + ")", conn);
            if (ID_CAT_019 == null) {
                ID_CAT_019 = Long.valueOf("772");
            }
            String NOMBRECOMERCIAL = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWALPH),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + AN8_JDE, conn);
            NOMBRECOMERCIAL = NOMBRECOMERCIAL.replaceAll("'", "");
            
            Long ID_CAT_012 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALADDS) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + AN8_JDE + ")", conn);
            Long ID_CAT_013 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALCOUN) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + AN8_JDE + ")", conn);
            String CODIGO_CAT_013 = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013 + " AND C.ID_CAT_012=" + ID_CAT_012, conn);
            if(CODIGO_CAT_013 == null) {
                ID_CAT_012 = Long.valueOf("6");
                ID_CAT_013 = Long.valueOf("111");
            }
            String DIRECCION_COMPLEMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.ALADD2),' ') || ' ' || NVL(TRIM(F.ALADD3),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + AN8_JDE, conn);
            if (DIRECCION_COMPLEMENTO == null) {
                DIRECCION_COMPLEMENTO = "Sin dirección registrada en el código del cliente";
            }
            String TELEFONO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WPAR1) || TRIM(F.WPPH1),'-') PHONE FROM " + esquema + ".F0115@" + dblink + " F WHERE (F.WPAN8, F.WPIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if (TELEFONO == null) {
                TELEFONO = "25288000";
            }
            String CORREO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.EAEMAL),'-') EMAIL FROM " + esquema + ".F01151@" + dblink + " F WHERE TRIM(F.EAETP)='E' AND (F.EAAN8, F.EAIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + AN8_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if (CORREO == null) {
                CORREO = "sinregistro@terra-uno.com";
            }
            
            Long ID_CAT_025 = Long.valueOf("1");
            
            String cadenasql = "INSERT INTO RECEPTOR_NR_V3 ("
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
                    + "CORREO, "
                    + "ID_CAT_025) VALUES ("
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
                    + CORREO + "',"
                    + ID_CAT_025 + ")";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            Long ID_SHIPTO = Long.valueOf("1");
            
            Long ID_CAT_022_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_022 C WHERE C.VALOR_JDE IN (SELECT NVL(TRIM(F.WWREM1),'36') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + SHAN_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            
            String NUM_DOCUMENTO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTX2),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + SHAN_JDE, conn);
            NUM_DOCUMENTO_SHAN = NUM_DOCUMENTO_SHAN.replaceAll(" ", "");
            
            String NRC_SHAN;
            if (ID_CAT_022_SHAN == Long.valueOf("1")) {
                NRC_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTAX),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + SHAN_JDE, conn);
                NRC_SHAN = NRC_SHAN.replaceAll(" ", "");
                if (NRC_SHAN.length() > 8) {
                    NRC_SHAN = NRC_SHAN.substring(0, 8);
                }
            } else {
                NRC_SHAN = "NULL";
            }
            
            String NOMBRE_SHAN = ctrl_base_datos.ObtenerString("SELECT UPPER(NVL(TRIM(F.WWMLNM),'-') || (SELECT NVL(TRIM(F.ALADD1),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + SHAN_JDE + ")) NOMBRE_FISCAL FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + SHAN_JDE, conn);
            NOMBRE_SHAN = NOMBRE_SHAN.replaceAll("'", "");
            
            Long ID_CAT_019_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC12) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + SHAN_JDE + ")", conn);
            if (ID_CAT_019_SHAN == null) {
                ID_CAT_019_SHAN = Long.valueOf("772");
            }
            String NOMBRECOMERCIAL_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWALPH),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + SHAN_JDE, conn);
            NOMBRECOMERCIAL_SHAN = NOMBRECOMERCIAL_SHAN.replaceAll("'", "");
            
            Long ID_CAT_012_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALADDS) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + SHAN_JDE + ")", conn);
            Long ID_CAT_013_SHAN = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALCTY1) FROM " + esquema + ".F0116@" + dblink + " G WHERE ROWNUM=1 AND G.ALAN8=" + SHAN_JDE + ")", conn);
            String CODIGO_CAT_013_SHAN = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013_SHAN + " AND C.ID_CAT_012=" + ID_CAT_012_SHAN, conn);
            if(CODIGO_CAT_013_SHAN == null) {
                ID_CAT_012_SHAN = Long.valueOf("6");
                ID_CAT_013_SHAN = Long.valueOf("111");
            }
            String DIRECCION_COMPLEMENTO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.ALADD2),' ') || ' ' || NVL(TRIM(F.ALADD3),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE ROWNUM=1 AND F.ALAN8=" + SHAN_JDE, conn);
            if (DIRECCION_COMPLEMENTO_SHAN == null) {
                DIRECCION_COMPLEMENTO_SHAN = "Sin dirección registrada en el código del cliente";
            }
            String TELEFONO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WPAR1) || TRIM(F.WPPH1),'-') PHONE FROM " + esquema + ".F0115@" + dblink + " F WHERE (F.WPAN8, F.WPIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + SHAN_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if (TELEFONO_SHAN == null) {
                TELEFONO_SHAN = "25288000";
            }
            String CORREO_SHAN = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.EAEMAL),'-') EMAIL FROM " + esquema + ".F01151@" + dblink + " F WHERE TRIM(F.EAETP)='E' AND (F.EAAN8, F.EAIDLN) IN (SELECT F.WWAN8, F.WWIDLN FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + SHAN_JDE + " AND TRIM(F.WWTYC)='S')", conn);
            if (CORREO_SHAN == null) {
                CORREO_SHAN = "sinregistro@terra-uno.com";
            }
            
            Long ID_CAT_025_SHAN = Long.valueOf("1");
            
            cadenasql = "INSERT INTO SHIPTO_NR_V3 ("
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
                    + "CORREO, "
                    + "ID_CAT_025) VALUES ("
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
                    + CORREO_SHAN + "',"
                    + ID_CAT_025_SHAN + ")";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_receptor_jde_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
