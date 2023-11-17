package Controladores;

import Entidades.Direccion;
import Entidades.SujetoExcluido_fse;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;

public class Ctrl_SujetoExcluido_FSE_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_SujetoExcluido_FSE_V3() {
    }

    public SujetoExcluido_fse obtener_sujeto_excluido_fse_v3(Long id_dte, Connection conn) {
        SujetoExcluido_fse resultado = new SujetoExcluido_fse();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            resultado.setTipoDocumento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_022 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_022 FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setNumDocumento(ctrl_base_datos.ObtenerString("SELECT F.NUM_DOCUMENTO FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCodActividad(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            resultado.setDescActividad(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));

            Direccion direccion_fse = new Direccion();
            direccion_fse.setDepartamento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_012 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_012 FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            direccion_fse.setMunicipio(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_013 FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn));
            direccion_fse.setComplemento(ctrl_base_datos.ObtenerString("SELECT F.DIRECCION_COMPLEMENTO FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setDireccion(direccion_fse);

            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
            resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE=" + id_dte, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_sujeto_excluido_fse_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_sujeto_excluido_fse_v3(Long id_dte, String ambiente, String KCOO_JDE, String DCTO_JDE, String DOCO_JDE, Connection conn) {
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
            Long ID_SUJETOEXCLUIDO = Long.valueOf("1");

            Long ID_CAT_022 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_022 C WHERE C.VALOR_JDE IN (SELECT NVL(TRIM(F.NRRMK),'36') FROM " + esquema + ".F554211N@" + dblink + " F WHERE F.NRKCOO='" + KCOO_JDE + "' AND F.NRDCTO='" + DCTO_JDE + "' AND F.NRDOCO=" + DOCO_JDE + ")", conn);
            String NUM_DOCUMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.NRTAX),'-') FROM " + esquema + ".F554211N@" + dblink + " F WHERE F.NRKCOO='" + KCOO_JDE + "' AND F.NRDCTO='" + DCTO_JDE + "' AND F.NRDOCO=" + DOCO_JDE, conn);

            String NOMBRE = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.A9ALPH),'-') FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + NUM_DOCUMENTO + "'", conn);

            Long ID_CAT_019 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_019 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.A9VR01) FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + NUM_DOCUMENTO + "')", conn);
            if (ID_CAT_019 == null) {
                ID_CAT_019 = Long.valueOf("772");
            }

            Long ID_CAT_012 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_012 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.A9ADDS) FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + NUM_DOCUMENTO + "')", conn);
            
            Long ID_CAT_013 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_013 C WHERE C.VALOR_JDE IN (SELECT TRIM(F.A9COUN) FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + NUM_DOCUMENTO + "')", conn);
            
            String CODIGO_CAT_013 = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT=" + ID_CAT_013 + " AND C.ID_CAT_012=" + ID_CAT_012, conn);
            if (CODIGO_CAT_013 == null) {
                ID_CAT_012 = Long.valueOf("6");
                ID_CAT_013 = Long.valueOf("111");
            }
            
            String DIRECCION_COMPLEMENTO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.A9ADD2),' ') || ' ' || NVL(TRIM(F.A9ADD3),' ') FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + NUM_DOCUMENTO + "'", conn);
            if (DIRECCION_COMPLEMENTO == null) {
                DIRECCION_COMPLEMENTO = "Sin dirección registrada en el código del cliente";
            }
            
            String TELEFONO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.A9VR04),'-') PHONE FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + NUM_DOCUMENTO + "'", conn);
            if (TELEFONO == null || TELEFONO.equals("-")) {
                TELEFONO = "25288000";
            }
            
            String CORREO = ctrl_base_datos.ObtenerString("SELECT NVL(TRIM(F.A9EMAIL),'-') EMAIL FROM " + esquema + ".F550401A@" + dblink + " F WHERE TRIM(F.A9TAX)='" + NUM_DOCUMENTO + "'", conn);
            if (CORREO == null || CORREO.equals("-")) {
                CORREO = "felsv@uno-ca.com";
            }

            String cadenasql = "INSERT INTO SUJETOEXCLUIDO_FSE_V3 ("
                    + "ID_DTE, "
                    + "ID_SUJETOEXCLUIDO, "
                    + "ID_CAT_022, "
                    + "NUM_DOCUMENTO, "
                    + "NOMBRE, "
                    + "ID_CAT_019, "
                    + "ID_CAT_012, "
                    + "ID_CAT_013, "
                    + "DIRECCION_COMPLEMENTO, "
                    + "TELEFONO, "
                    + "CORREO) VALUES ("
                    + ID_DTE + ","
                    + ID_SUJETOEXCLUIDO + ","
                    + ID_CAT_022 + ",'"
                    + NUM_DOCUMENTO + "','"
                    + NOMBRE + "',"
                    + ID_CAT_019 + ","
                    + ID_CAT_012 + ","
                    + ID_CAT_013 + ",'"
                    + DIRECCION_COMPLEMENTO + "','"
                    + TELEFONO + "','"
                    + CORREO + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            resultado = "0,TRANSACCION PROCESADA.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_sujeto_excluido_fse_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
