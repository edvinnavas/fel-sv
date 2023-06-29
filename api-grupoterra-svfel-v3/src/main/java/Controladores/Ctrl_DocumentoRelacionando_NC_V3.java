package Controladores;

import Entidades.DocumentoRelacionado_nc;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_DocumentoRelacionando_NC_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_DocumentoRelacionando_NC_V3() {
    }

    public List<DocumentoRelacionado_nc> obtener_documento_relacionado_nc_v3(Long id_dte, Connection conn) {
        List<DocumentoRelacionado_nc> resultado = new ArrayList<>();
        DocumentoRelacionado_nc documento_relacionado_nc = new DocumentoRelacionado_nc();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();

            documento_relacionado_nc.setTipoDocumento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_002 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_002 FROM DOCU_RELA_NC_V3 F WHERE F.ID_DOCUMENTOS_RELACIONADOS=1 AND F.ID_DTE=" + id_dte + ")", conn));
            documento_relacionado_nc.setTipoGeneracion(ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_007 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_007 FROM DOCU_RELA_NC_V3 F WHERE F.ID_DOCUMENTOS_RELACIONADOS=1 AND F.ID_DTE=" + id_dte + ")", conn));
            documento_relacionado_nc.setNumeroDocumento(ctrl_base_datos.ObtenerString("SELECT F.NUMERODOCUMENTO FROM DOCU_RELA_NC_V3 F WHERE F.ID_DOCUMENTOS_RELACIONADOS=1 AND F.ID_DTE=" + id_dte, conn));
            documento_relacionado_nc.setFechaEmision(ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHAEMISION,'YYYY-MM-DD') FROM DOCU_RELA_NC_V3 F WHERE F.ID_DOCUMENTOS_RELACIONADOS=1 AND F.ID_DTE=" + id_dte, conn));
            resultado.add(documento_relacionado_nc);
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_documento_relacionado_nc_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    public String extraer_documento_relacionado_jde_nc_v3(Long id_dte, String ambiente, String JEVER_JDE, String KCOO_JDE, String DOCO_JDE, String DCTO_JDE, Connection conn) {
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

            String JEVER_JDE_CA;
            if(JEVER_JDE.trim().equals("F4211")) {
                JEVER_JDE_CA = "F4201";
            } else {
                JEVER_JDE_CA = "F42019";
            }
            
            Long ID_DTE = id_dte;
            Long ID_DOCUMENTOS_RELACIONADOS = Long.valueOf("1");

            String documento_relacionado = ctrl_base_datos.ObtenerString("SELECT TRIM(F.SHTXCT) FROM " + esquema + "." + JEVER_JDE_CA + "@" + dblink + " F WHERE F.SHKCOO='" + KCOO_JDE + "' AND F.SHDOCO=" + DOCO_JDE + " AND F.SHDCTO='" + DCTO_JDE + "'", conn);
            if (documento_relacionado != null) {
                String[] partes_documento_relacionado = documento_relacionado.split("-");
                if (partes_documento_relacionado.length == 2) {
                    String DCTO_JDE_CCF = partes_documento_relacionado[0];
                    String DOCO_JDE_CCF = partes_documento_relacionado[1];
                    // System.out.println("********** DCTO_JDE_CCF: " + DCTO_JDE_CCF);
                    // System.out.println("********** DOCO_JDE_CCF: " + DOCO_JDE_CCF);
                    String codigo_generacion = ctrl_base_datos.ObtenerString("SELECT TRIM(F.FECRSREF02) FROM " + esquema + ".F5542FEL@" + dblink + " F WHERE F.FESTCD IN ('001','002') AND F.FEDOCO=" + DOCO_JDE_CCF + " AND F.FEDCTO='" + DCTO_JDE_CCF + "'", conn);
                    Long ID_CAT_002 = ctrl_base_datos.ObtenerLong("SELECT C.ID_CAT FROM CAT_002 C WHERE C.VALOR_JDE='" + DCTO_JDE_CCF + "'", conn);
                    Long ID_CAT_007;
                    String NUMERODOCUMENTO;
                    String FECHAEMISION;
                    if (codigo_generacion != null) {
                        ID_CAT_007 = Long.valueOf("2");
                        NUMERODOCUMENTO = codigo_generacion;
                        FECHAEMISION = ctrl_base_datos.ObtenerString("SELECT TO_CHAR(F.FECHA_HORA_EMISION,'YYYY/MM/DD') || ' 00:00:00' FROM IDENTIFICACION_CCF_V3 F WHERE F.CODIGOGENERACION='" + NUMERODOCUMENTO + "'", conn);
                    } else {
                        ID_CAT_007 = Long.valueOf("1");
                        String KCOO_JDE_RELA = ctrl_base_datos.ObtenerString("SELECT DISTINCT F.SDKCOO FROM " + esquema + ".F42119@" + dblink + " F WHERE F.SDDCTO='" + DCTO_JDE_CCF + "' AND F.SDDOCO=" + DOCO_JDE_CCF, conn);
                        String DCT_JDE_RELA = ctrl_base_datos.ObtenerString("SELECT DISTINCT F.SDDCT FROM " + esquema + ".F42119@" + dblink + " F WHERE F.SDDCTO='" + DCTO_JDE_CCF + "' AND F.SDDOCO=" + DOCO_JDE_CCF, conn);
                        String IVD_JDE_RELA = ctrl_base_datos.ObtenerString("SELECT DISTINCT F.SDIVD FROM " + esquema + ".F42119@" + dblink + " F WHERE F.SDDCTO='" + DCTO_JDE_CCF + "' AND F.SDDOCO=" + DOCO_JDE_CCF, conn);
                        String DOC_JDE_RELA = ctrl_base_datos.ObtenerString("SELECT DISTINCT SUBSTR(F.SDDOC,3,LENGTH(F.SDDOC)) FROM " + esquema + ".F42119@" + dblink + " F WHERE F.SDDCTO='" + DCTO_JDE_CCF + "' AND F.SDDOCO=" + DOCO_JDE_CCF, conn);
                        String DOC_JDE_RELA_TEMP = ctrl_base_datos.ObtenerString("SELECT DISTINCT F.SDDOC FROM " + esquema + ".F42119@" + dblink + " F WHERE F.SDDCTO='" + DCTO_JDE_CCF + "' AND F.SDDOCO=" + DOCO_JDE_CCF, conn);
                        NUMERODOCUMENTO = ctrl_base_datos.ObtenerString("SELECT TRIM(F.NNDL03) FROM " + esquema + ".F5500021@" + dblink + " F WHERE (F.NNCO='" + KCOO_JDE_RELA + "') AND (F.NNDCTO='" + DCTO_JDE_CCF + "') AND (F.NNDCT='" + DCT_JDE_RELA + "') AND (" + IVD_JDE_RELA + " BETWEEN F.NNEDTF AND F.NNEDTT) AND (" + DOC_JDE_RELA_TEMP +  " BETWEEN F.NNN001 AND F.NNN002)", conn);
                        NUMERODOCUMENTO = NUMERODOCUMENTO + DOC_JDE_RELA;
                        FECHAEMISION = ctrl_base_datos.ObtenerString("SELECT TO_CHAR(TO_DATE(TO_CHAR(F.SDIVD + 1900000,'9999999'),'YYYYDDD'),'YYYY/MM/DD') || ' 00:00:00' FROM " + esquema + ".F42119@" + dblink + " F WHERE F.SDDOCO=" + DOCO_JDE_CCF + " AND F.SDDCTO='" + DCTO_JDE_CCF + "'", conn);
                    }

                    String cadenasql = "INSERT INTO DOCU_RELA_NC_V3 ("
                            + "ID_DTE,"
                            + "ID_DOCUMENTOS_RELACIONADOS, "
                            + "ID_CAT_002, "
                            + "ID_CAT_007, "
                            + "NUMERODOCUMENTO, "
                            + "FECHAEMISION) VALUES ("
                            + ID_DTE + ","
                            + ID_DOCUMENTOS_RELACIONADOS + ","
                            + ID_CAT_002 + ","
                            + ID_CAT_007 + ",'"
                            + NUMERODOCUMENTO + "',"
                            + "TO_DATE('" + FECHAEMISION + "','YYYY/MM/DD HH24:MI:SS')" + ")";
                    Statement stmt = conn.createStatement();
                    // System.out.println(cadenasql);
                    stmt.executeUpdate(cadenasql);
                    stmt.close();
                    
                    resultado = NUMERODOCUMENTO;
                }
            }
        } catch (Exception ex) {
            resultado = "";
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_relacionado_jde_nc_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
