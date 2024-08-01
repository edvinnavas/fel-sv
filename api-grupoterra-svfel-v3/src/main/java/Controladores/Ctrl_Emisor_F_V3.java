package Controladores;

import Entidades.Direccion;
import Entidades.Emisor_f;
import java.io.Serializable;
import java.sql.Connection;

public class Ctrl_Emisor_F_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Emisor_F_V3() {
    }

    public Emisor_f obtener_emisor_f_v3(Long id_dte, Connection conn) {
        Emisor_f resultado = new Emisor_f();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            Long id_emisor = ctrl_base_datos.ObtenerLong("SELECT F.ID_EMISOR FROM DTE_F_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            String mcu_jde = ctrl_base_datos.ObtenerString("SELECT TRIM(F.MCU_JDE) FROM DTE_F_V3 F WHERE F.ID_DTE=" + id_dte, conn);
            
            resultado.setNit(ctrl_base_datos.ObtenerString("SELECT F.NIT FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setNrc(ctrl_base_datos.ObtenerString("SELECT F.NRC FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setCodActividad(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor + ")", conn));
            resultado.setDescActividad(ctrl_base_datos.ObtenerString("SELECT C.VALOR FROM CAT_019 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_019 FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor + ")", conn));
            resultado.setNombreComercial(ctrl_base_datos.ObtenerString("SELECT F.NOMBRECOMERCIAL FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setTipoEstablecimiento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_009 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_009 FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "')", conn));
            
            Direccion direccion_f = new Direccion();
            direccion_f.setDepartamento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_012 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_012 FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "')", conn));
            direccion_f.setMunicipio(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_013 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_013 FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "')", conn));
            direccion_f.setComplemento(ctrl_base_datos.ObtenerString("SELECT F.DIRECCION_COMPLEMENTO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setDireccion(direccion_f);
            
            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCodEstableMH(ctrl_base_datos.ObtenerString("SELECT F.CODESTABLEMH FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCodEstable(ctrl_base_datos.ObtenerString("SELECT LPAD(F.CODESTABLE,4,'0') FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCodPuntoVentaMH(ctrl_base_datos.ObtenerString("SELECT F.CODPUNTOVENTAMH FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCodPuntoVenta(ctrl_base_datos.ObtenerString("SELECT DECODE(F.CODPUNTOVENTA_INT, '0000', NULL, F.CODPUNTOVENTA_INT) FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
            resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.ID_EMISOR=" + id_emisor + " AND TRIM(F.CODPUNTOVENTA)='" + mcu_jde + "'", conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_emisor_f_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
