package Controladores;

import ClienteServicio.Cliente_Rest_JDE;
import Entidades.Apendice_nr;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_Apendice_NR_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Apendice_NR_V3() {
    }

    public List<Apendice_nr> obtener_apendice_nr_v3(Long id_dte, Connection conn) {
        List<Apendice_nr> resultado = new ArrayList<>();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            String cadenasql = "SELECT F.ID_APENDICE FROM APENDICE_NR_V3 F WHERE F.ID_DTE=" + id_dte + " ORDER BY F.ID_APENDICE"; 
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(cadenasql);
            while(rs.next()) {
                Long id_apendice = rs.getLong(1);
                Apendice_nr apendice_nr = new Apendice_nr();
                apendice_nr.setCampo(ctrl_base_datos.ObtenerString("SELECT F.CAMPO FROM APENDICE_NR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_APENDICE=" + id_apendice, conn));
                apendice_nr.setEtiqueta(ctrl_base_datos.ObtenerString("SELECT F.ETIQUETA FROM APENDICE_NR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_APENDICE=" + id_apendice, conn));
                String valor = ctrl_base_datos.ObtenerString("SELECT F.VALOR FROM APENDICE_NR_V3 F WHERE F.ID_DTE=" + id_dte + " AND F.ID_APENDICE=" + id_apendice, conn);
                if(valor.length() > 150) {
                    valor = valor.substring(0, 149);
                }
                apendice_nr.setValor(valor);
                resultado.add(apendice_nr);
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_apendice_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    public String extraer_apendice_jde_nr_v3(Long id_dte, String ambiente, String DOCO_JDE, String DCTO_JDE, String MCU_JDE, Connection conn) {
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

            String CODIGO_CLIENTE = ctrl_base_datos.ObtenerString("SELECT F.AN8_JDE FROM DTE_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            String NUMERO_ORDEN = ctrl_base_datos.ObtenerString("SELECT F.DCTO_JDE || '-' || F.DOCO_JDE FROM DTE_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            String NUMERO_DOCUMENTO = ctrl_base_datos.ObtenerString("SELECT F.DCT_JDE || '-' || F.DOC_JDE FROM DTE_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            String FECHA_VENCIMIENTO = ctrl_base_datos.ObtenerString("SELECT C16.VALOR || ' - ' || RES.PAGOS_PERIODO || ' días' || ' - ' || TO_CHAR(IDE.FECHA_HORA_EMISION + RES.PAGOS_PERIODO, 'DD-MM-YYYY') infoCondicionOperacion FROM RESUMEN_NR_V3 RES LEFT JOIN CAT_016 C16 ON (RES.ID_CAT_016=C16.ID_CAT) LEFT JOIN IDENTIFICACION_NR_V3 IDE ON (IDE.ID_DTE=RES.ID_DTE) WHERE RES.ID_DTE=" + ID_DTE, conn);
            String CODIGO_DESTINO = ctrl_base_datos.ObtenerString("SELECT F.SHAN_JDE FROM DTE_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            String ENVIAR_A = ctrl_base_datos.ObtenerString("SELECT SHI.NOMBRE || ' ' || SHI.DIRECCION_COMPLEMENTO infoEnviarANombre FROM SHIPTO_NR_V3 SHI WHERE SHI.ID_DTE=" + ID_DTE, conn);
            String NUMERO_VIAJE = ctrl_base_datos.ObtenerString("SELECT DISTINCT F.TDLDNM FROM " + esquema + ".F49621@" + dblink + " F WHERE F.TDDCTO='" + DCTO_JDE + "' AND F.TDDOCO=" + DOCO_JDE + " AND TRIM(F.TDVMCU)='" + MCU_JDE + "'", conn);
            if(NUMERO_VIAJE == null) {
                NUMERO_VIAJE = "0";
            }
            String SELLOS_SEGURIDAD = ctrl_base_datos.ObtenerString("SELECT DISTINCT T.SELLOS FROM (SELECT (LISTAGG(F.SUSLN, ', ') WITHIN GROUP (ORDER BY F.SUSLN) OVER (PARTITION BY F.SULDNM, F.SUVMCU)) SELLOS FROM " + esquema + ".F49380@" + dblink + " F  WHERE F.SULDNM='" + NUMERO_VIAJE + "' AND TRIM(F.SUVMCU)='" + MCU_JDE + "') T", conn);
            if(SELLOS_SEGURIDAD == null) {
                SELLOS_SEGURIDAD = "-";
            }
            String NUMERO_CONTRATO = ctrl_base_datos.ObtenerString("SELECT TRIM(F.WWATTL) FROM " + esquema + ".F0111@" + dblink + " F WHERE F.WWAN8=" + CODIGO_CLIENTE + " AND TRIM(F.WWTYC)='S'", conn);
            if(NUMERO_CONTRATO == null) {
                NUMERO_CONTRATO = "-";
            }
            String KCOO_JDE = ctrl_base_datos.ObtenerString("SELECT F.KCOO_JDE FROM DTE_NR_V3 F WHERE F.ID_DTE=" + ID_DTE, conn);
            Cliente_Rest_JDE cliente_rest_jde = new Cliente_Rest_JDE();
            String PERIODO_FACTURACION = cliente_rest_jde.obetener_texto_encabezado_orden_ventas("PET", ambiente, DOCO_JDE, DCTO_JDE, KCOO_JDE);
            if(PERIODO_FACTURACION == null) {
                PERIODO_FACTURACION = "SIN REGISTRO.";
            }
            PERIODO_FACTURACION = PERIODO_FACTURACION.replaceAll("\"", "");
            if(PERIODO_FACTURACION == null) {
                PERIODO_FACTURACION = "SIN REGISTRO.";
            }
            if(PERIODO_FACTURACION.trim().equals("")) {
                PERIODO_FACTURACION = "SIN REGISTRO.";
            }

            Long ID_APENDICE = Long.valueOf("1");
            String CAMPO = "Apendice-1";
            String ETIQUETA = "Código cliente";
            String VALOR = CODIGO_CLIENTE;

            String cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            Statement stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            ID_APENDICE = Long.valueOf("2");
            CAMPO = "Apendice-2";
            ETIQUETA = "No. orden";
            VALOR = NUMERO_ORDEN;

            cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            ID_APENDICE = Long.valueOf("3");
            CAMPO = "Apendice-3";
            ETIQUETA = "No. documento";
            VALOR = NUMERO_DOCUMENTO;

            cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            /* ID_APENDICE = Long.valueOf("4");
            CAMPO = "Apendice-4";
            ETIQUETA = "Fecha de vencimiento";
            VALOR = FECHA_VENCIMIENTO;

            cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close(); */
            
            ID_APENDICE = Long.valueOf("4");
            CAMPO = "Apendice-4";
            ETIQUETA = "Código destino";
            VALOR = CODIGO_DESTINO;

            cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            ID_APENDICE = Long.valueOf("5");
            CAMPO = "Apendice-5";
            ETIQUETA = "Enviar a";
            VALOR = ENVIAR_A;

            cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            ID_APENDICE = Long.valueOf("6");
            CAMPO = "Apendice-6";
            ETIQUETA = "Sellos de seguridad";
            VALOR = SELLOS_SEGURIDAD;

            cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();

            /* ID_APENDICE = Long.valueOf("8");
            CAMPO = "Apendice-8";
            ETIQUETA = "No. contrato";
            VALOR = NUMERO_CONTRATO;

            cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close(); */

            ID_APENDICE = Long.valueOf("7");
            CAMPO = "Apendice-7";
            ETIQUETA = "Período de facturación";
            VALOR = PERIODO_FACTURACION;

            cadenasql = "INSERT INTO APENDICE_NR_V3 ("
                    + "ID_DTE, "
                    + "ID_APENDICE, "
                    + "CAMPO, "
                    + "ETIQUETA, "
                    + "VALOR) VALUES ("
                    + ID_DTE + ","
                    + ID_APENDICE + ",'"
                    + CAMPO + "','"
                    + ETIQUETA + "','"
                    + VALOR + "')";
            stmt = conn.createStatement();
            // System.out.println(cadenasql);
            stmt.executeUpdate(cadenasql);
            stmt.close();
            
            resultado = "0,TRANSACCIONES PROCESADAS.";
        } catch (Exception ex) {
            resultado = "1," + ex.toString();
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_apendice_jde_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
