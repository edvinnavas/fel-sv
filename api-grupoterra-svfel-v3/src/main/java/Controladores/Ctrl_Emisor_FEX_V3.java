package Controladores;

import Entidades.Direccion;
import Entidades.Emisor_fex;
import java.io.Serializable;
import java.sql.Connection;

public class Ctrl_Emisor_FEX_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Emisor_FEX_V3() {
    }

    public Emisor_fex obtener_emisor_fex_v3(Long id_dte, String ambiente, Connection conn) {
        Emisor_fex resultado = new Emisor_fex();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            Long id_emisor = ctrl_base_datos.ObtenerLong("SELECT F.ID_EMISOR FROM DTE_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            String mcu_jde = ctrl_base_datos.ObtenerString("SELECT TRIM(F.MCU_JDE) FROM DTE_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            
            resultado.setNit(ctrl_base_datos.ObtenerString("SELECT F.NIT FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setNrc(ctrl_base_datos.ObtenerString("SELECT F.NRC FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setCodActividad(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor + ")", conn));
            resultado.setDescActividad(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor + ")", conn));
            resultado.setNombreComercial(ctrl_base_datos.ObtenerString("SELECT F.NOMBRECOMERCIAL FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setTipoEstablecimiento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_009 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_009 FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "')", conn));
            
            Direccion direccion_fex = new Direccion();
            direccion_fex.setDepartamento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_012 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_012 FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "')", conn));
            direccion_fex.setMunicipio(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_013 FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "')", conn));
            direccion_fex.setComplemento(ctrl_base_datos.ObtenerString("SELECT F.DIRECCION_COMPLEMENTO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setDireccion(direccion_fex);
            
            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCodEstableMH(ctrl_base_datos.ObtenerString("SELECT F.CODESTABLEMH FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCodEstable(ctrl_base_datos.ObtenerString("SELECT LPAD(F.CODESTABLE,4,'0') FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCodPuntoVentaMH(ctrl_base_datos.ObtenerString("SELECT F.CODPUNTOVENTAMH FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCodPuntoVenta(ctrl_base_datos.ObtenerString("SELECT DECODE(F.CODPUNTOVENTA_INT, '0000', NULL, F.CODPUNTOVENTA_INT) FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            
            Integer num_tipo_Item_Expor = ctrl_base_datos.ObtenerEntero("SELECT COUNT(*) NUM FROM CAT_011 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_011 FROM CUERPO_DOCU_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn);
            Integer tipo_Item_Expor = 0;
            if(num_tipo_Item_Expor > 1) {
                tipo_Item_Expor = 3;
            } else {
                tipo_Item_Expor = ctrl_base_datos.ObtenerEntero("SELECT C.CODIGO FROM CAT_011 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_011 FROM CUERPO_DOCU_FEX_V3 F WHERE F.ID_DTE=" + id_dte + ")", conn);
            }
            resultado.setTipoItemExpor(tipo_Item_Expor);
            
            /* *****************************************************************
             *  AGERGADO ESPECIAL PARA EXTRAER EL RECINTO FISCAL Y EL REGIMEN 
             *  EN LA SECCION EMISOR PARA LA FACTURA DE EXPORACIÓN.
             ******************************************************************* */
            String esquema;
            String dblink;
            if (ambiente.equals("PY")) {
                esquema = "CRPDTA";
                dblink = "JDEPY";
            } else {
                esquema = "PRODDTA";
                dblink = "JDEPD";
            }
            
            String AN8_JDE = ctrl_base_datos.ObtenerString("SELECT F.AN8_JDE FROM DTE_FEX_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            
            String RecintoFiscal = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_027 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC23) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + AN8_JDE + ")", conn);
            if(RecintoFiscal == null) {
                RecintoFiscal = "03";
            }
            resultado.setRecintoFiscal(RecintoFiscal);
            
            String Regimen = ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_028 C WHERE C.VALOR_JDE IN (SELECT TRIM(G.ABAC27) FROM " + esquema + ".F0101@" + dblink + " G WHERE G.ABAN8=" + AN8_JDE + ")", conn);
            if(Regimen == null) {
                Regimen = "EX 1.1000.000";
            }
            resultado.setRegimen(Regimen);
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_emisor_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
