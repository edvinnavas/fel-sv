package Controladores;

import Entidades.CuerpoDocumento_cr;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_CuerpoDocumento_CR_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_CuerpoDocumento_CR_V3() {
    }

    public List<CuerpoDocumento_cr> obtener_cuerpo_documento_cr_v3(Long id_dte, Connection conn) {
        List<CuerpoDocumento_cr> resultado = new ArrayList<>();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            String cadenasql = "SELECT F.ID_CUERPO_DOCUMENTO FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " ORDER BY F.ID_CUERPO_DOCUMENTO";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while (rs.next()) {
                Long id_cuerpo_documento = rs.getLong(1);
                CuerpoDocumento_cr cuerpo_documento_cr = new CuerpoDocumento_cr();
                cuerpo_documento_cr.setNumItem(id_cuerpo_documento);
                cuerpo_documento_cr.setTipoDte(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_002 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_002 FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + ")", conn));
                cuerpo_documento_cr.setTipoDoc(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_007 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_007 FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + ")", conn));
                cuerpo_documento_cr.setNumDocumento(ctrl_base_datos.ObtenerString("SELECT F.NUMDOCUMENTO FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_cr.setFechaEmision(ctrl_base_datos.ObtenerString("SELECT F.FECHAEMISION FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_cr.setMontoSujetoGrav(ctrl_base_datos.ObtenerDouble("SELECT F.MONTOSUJETOGRAV FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_cr.setCodigoRetencionMH(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_006 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_006 FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento + ")", conn));
                cuerpo_documento_cr.setIvaRetenido(ctrl_base_datos.ObtenerDouble("SELECT F.IVARETENIDO FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                cuerpo_documento_cr.setDescripcion(ctrl_base_datos.ObtenerString("SELECT F.DESCRIPCION FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_CUERPO_DOCUMENTO=" + id_cuerpo_documento, conn));
                resultado.add(cuerpo_documento_cr);
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_cuerpo_documento_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_cuerpo_documento_jde_cr_v3(Long id_dte, String ambiente, String KCOO_JDE, String DOCO_JDE, String DCTO_JDE, Connection conn) {
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
                    + "TRIM(F.CRVR02) ID_CAT_002, "
                    + "TRIM(F.CREV02) ID_CAT_007, "
                    + "CASE WHEN F.CREV02='1' THEN (TRIM(G.CDURRF) || TRIM(G.CDVINV)) WHEN F.CREV02='2' THEN TRIM(G.CDGENKEY) END NUMDOCUMENTO, " 
                    + "TO_CHAR(TO_DATE(TO_CHAR(G.CDDIVJ + 1900000,'9999999'),'YYYYDDD'),'YYYY-MM-DD') FECHAEMISION, " 
                    + "G.CDAG/100 MONTOSUJETOGRAV, " 
                    + "TRIM(F.CRVR01) ID_CAT_006, "
                    + "G.CDMATH01/100 IVARETENIDO, " 
                    + "'FELSV' DESCRIPCION " 
                    + "FROM " 
                    + esquema + ".F5504001@" + dblink + " F "
                    + "LEFT JOIN " + esquema + ".F550401D@" + dblink + " G ON (F.CRKCO=G.CDKCO AND F.CRURCD=G.CDURCD AND F.CRN001=G.CDN001) " 
                    + "WHERE " 
                    + "F.CRKCO='" + KCOO_JDE + "' AND F.CRN001=" + DOCO_JDE + " AND F.CRURCD='" + DCTO_JDE + "'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            Integer contador = 0;
            while (rs.next()) {
                contador++;
                Long ID_DTE = id_dte;
                Long ID_CUERPO_DOCUMENTO = Long.valueOf(contador.toString());
                Long ID_CAT_002 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_002 C WHERE C.VALOR_JDE='" + rs.getString(1) + "'", conn);
                Long ID_CAT_007 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_007 C WHERE C.CODIGO='" + rs.getString(2) + "'", conn);
                String NUMDOCUMENTO = rs.getString(3);
                if(NUMDOCUMENTO == null) {
                    NUMDOCUMENTO = "SIN REGISTRO";
                }
                if(NUMDOCUMENTO.length() > 36) {
                    NUMDOCUMENTO = NUMDOCUMENTO.substring(0, 36);
                }
                String FECHAEMISION = rs.getString(4);
                Number MONTOSUJETOGRAV = rs.getDouble(5);
                Long ID_CAT_006 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_006 C WHERE C.CODIGO='" + rs.getString(6) + "'", conn);
                Number IVARETENIDO = rs.getDouble(7);
                String DESCRIPCION = rs.getString(8);

                cadenasql = "INSERT INTO CUERPO_DOCU_CR_V3 ( "
                        + "ID_DTE, "
                        + "ID_CUERPO_DOCUMENTO, "
                        + "ID_CAT_002, "
                        + "ID_CAT_007, "
                        + "NUMDOCUMENTO, "
                        + "FECHAEMISION, "
                        + "MONTOSUJETOGRAV, "
                        + "ID_CAT_006, "
                        + "IVARETENIDO, "
                        + "DESCRIPCION) VALUES ("
                        + ID_DTE + ","
                        + ID_CUERPO_DOCUMENTO + ","
                        + ID_CAT_002 + ","
                        + ID_CAT_007 + ",'"
                        + NUMDOCUMENTO + "','"
                        + FECHAEMISION + "',"
                        + MONTOSUJETOGRAV + ","
                        + ID_CAT_006 + ","
                        + IVARETENIDO + ",'"
                        + DESCRIPCION + "')";
                Statement stmt1 = conn.createStatement();
                // System.out.println(cadenasql);
                stmt1.executeUpdate(cadenasql);
                stmt1.close();
            }

            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_cuerpo_documento_jde_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
