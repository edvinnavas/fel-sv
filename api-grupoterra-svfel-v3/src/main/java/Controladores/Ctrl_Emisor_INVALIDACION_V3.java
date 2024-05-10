package Controladores;

import Entidades.Emisor_invalidacion;
import java.io.Serializable;
import java.sql.Connection;

public class Ctrl_Emisor_INVALIDACION_V3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Emisor_INVALIDACION_V3() {
    }

    public Emisor_invalidacion obtener_emisor_invalidacion_v3(Long id_contigencia, Connection conn) {
        Emisor_invalidacion resultado = new Emisor_invalidacion();

        try {
            Ctrl_Base_Datos ctrl_base_datos = new Ctrl_Base_Datos();
            
            Long id_emisor = ctrl_base_datos.ObtenerLong("SELECT F.ID_EMISOR FROM EVENTO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contigencia, conn);
            String mcu_jde = ctrl_base_datos.ObtenerString("SELECT F.MCU_JDE FROM EVENTO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA=" + id_contigencia, conn);
            
            resultado.setNit(ctrl_base_datos.ObtenerString("SELECT F.NIT FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setNombre(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setTipoEstablecimiento(ctrl_base_datos.ObtenerString("SELECT C.CODIGO FROM CAT_009 C WHERE C.ID_CAT IN (SELECT F.ID_CAT_009 FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA=" + mcu_jde + " AND F.ID_EMISOR=" + id_emisor + ")", conn));
            resultado.setNomEstablecimiento(ctrl_base_datos.ObtenerString("SELECT F.NOMBRE FROM EMISOR_V3 F WHERE F.ID_EMISOR=" + id_emisor, conn));
            resultado.setCodEstableMH(ctrl_base_datos.ObtenerString("SELECT F.CODESTABLEMH FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA=" + mcu_jde + " AND F.ID_EMISOR=" + id_emisor, conn));
            resultado.setCodEstable(ctrl_base_datos.ObtenerString("SELECT F.CODESTABLE FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA=" + mcu_jde + " AND F.ID_EMISOR=" + id_emisor, conn));
            resultado.setCodPuntoVentaMH(ctrl_base_datos.ObtenerString("SELECT F.CODPUNTOVENTAMH FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA=" + mcu_jde + " AND F.ID_EMISOR=" + id_emisor, conn));
            resultado.setCodPuntoVenta(ctrl_base_datos.ObtenerString("SELECT DECODE(F.CODPUNTOVENTA_INT, '0000', NULL) FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA=" + mcu_jde + " AND F.ID_EMISOR=" + id_emisor, conn));
            resultado.setTelefono(ctrl_base_datos.ObtenerString("SELECT F.TELEFONO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA=" + mcu_jde + " AND F.ID_EMISOR=" + id_emisor, conn));
            resultado.setCorreo(ctrl_base_datos.ObtenerString("SELECT F.CORREO FROM EMISOR_ESTABLECIMIENTO_V3 F WHERE F.CODPUNTOVENTA=" + mcu_jde + " AND F.ID_EMISOR=" + id_emisor, conn));
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:obtener_emisor_invalidacion_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
