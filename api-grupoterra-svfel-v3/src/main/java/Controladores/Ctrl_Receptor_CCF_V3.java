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
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_receptor_ccf_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    public String extraer_receptor_jde_ccf_v3(Long id_dte, String ambiente, String SHAN_JDE, Connection conn) {
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
            Long ID_RECEPTOR = Long.parseLong("1");
            String NIT = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTX2),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + SHAN_JDE, conn);
            String NRC = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.ABTAX),'-',''),'-') FROM " + esquema + ".F0101@" + dblink + " F WHERE F.ABAN8=" + SHAN_JDE, conn);
            String NOMBRE = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWMLNM),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + SHAN_JDE, conn);
            Long ID_CAT_019 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC06) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + SHAN_JDE + ")", conn);
            if (ID_CAT_019 == null) {
                ID_CAT_019 = Long.parseLong("772");
            }
            String NOMBRECOMERCIAL = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.WWALPH),'-') FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWIDLN=0 AND F.WWAN8=" + SHAN_JDE, conn);
            Long ID_CAT_012 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALADDS) FROM " + esquema + ".F0116@" + dblink + " G WHERE G.ALAN8=" + SHAN_JDE + ")", conn);
            Long ID_CAT_013 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ALCTY1) FROM " + esquema + ".F0116@" + dblink + " G WHERE G.ALAN8=" + SHAN_JDE + ")", conn);
            String CODIGO_CAT_013 = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013 + " AND C.ID_CAT_012=" + ID_CAT_012, conn);
            if(CODIGO_CAT_013 == null) {
                ID_CAT_012 = Long.parseLong("6");
                ID_CAT_013 = Long.parseLong("111");
            }
            String DIRECCION_COMPLEMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.ALADD1),' ') || ' ' || NVL(TRIM(F.ALADD2),' ') || ' ' || NVL(TRIM(F.ALADD3),' ') || ' ' || NVL(TRIM(F.ALADD4),' ') FROM " + esquema + ".F0116@" + dblink + " F WHERE F.ALAN8=" + SHAN_JDE, conn);
            if (DIRECCION_COMPLEMENTO == null) {
                DIRECCION_COMPLEMENTO = "AV. ALBERT EINSTEIN Y BLVD LOS PROCERES, URB. LOMAS DE SAN FRANCISCO, # 1, TORRE CUSCATLAN, NIVEL 15, ANTGO CUSCATLAN, LA LIBERTAD.";
            }
            String TELEFONO = ctrl_base_datos.ObtenerString("SELECT NVL(REPLACE(TRIM(F.WPPH1),'-',''),'-') FROM " + esquema + ".F0115@" + dblink + " F WHERE F.WPIDLN=0 AND F.WPCNLN=0 AND F.WPAN8=" + SHAN_JDE, conn);
            if (TELEFONO == null) {
                TELEFONO = "25288000";
            }
            String CORREO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.EAEMAL),'-') FROM " + esquema + ".F01151@" + dblink + " F WHERE F.EAIDLN=0 AND TRIM(F.EAETP) = 'E' AND F.EAAN8=" + SHAN_JDE, conn);
            if (CORREO == null) {
                CORREO = "replegal-unosv@uno-terra.com";
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
            System.out.println(cadenasql);
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