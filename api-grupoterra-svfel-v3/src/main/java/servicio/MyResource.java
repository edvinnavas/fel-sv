package servicio;

import ClienteServicio.Cliente_Rest_INFILE;
import ClienteServicio.Cliente_Rest_MH;
import Controladores.Ctrl_Archivos;
import Controladores.Ctrl_ConsultarDteLote_V3;
import Controladores.Ctrl_DTE_CCF_V3;
import Controladores.Ctrl_DTE_CONTINGENCIA_V3;
import Controladores.Ctrl_DTE_CR_V3;
import Controladores.Ctrl_DTE_FEX_V3;
import Controladores.Ctrl_DTE_FSE_V3;
import Controladores.Ctrl_DTE_NC_V3;
import Controladores.Ctrl_DTE_ND_V3;
import Controladores.Ctrl_DTE_F_V3;
import Controladores.Ctrl_DTE_INVALIDACION_V3;
import Controladores.Ctrl_DTE_NR_V3;
import Controladores.Ctrl_DTE_V3;
import Controladores.Ctrl_Firmar_Documento_JWT;
import Controladores.Driver;
import Entidades.DTE_CCF_V3;
import Entidades.DTE_CONTIGENCIA_V3;
import Entidades.DTE_CR_V3;
import Entidades.DTE_FEX_V3;
import Entidades.DTE_FSE_V3;
import Entidades.DTE_NC_V3;
import Entidades.DTE_ND_V3;
import Entidades.DTE_F_V3;
import Entidades.DTE_INVALIDACION_V3;
import Entidades.DTE_NR_V3;
import Entidades.Errores_INFILE;
import Entidades.JsonCONTIN;
import Entidades.JsonDTE;
import Entidades.JsonLoteDTE;
import Entidades.Json_Firmado;
import Entidades.RESPUESTA_CONTINGENCIA_MH;
import Entidades.RESPUESTA_LOTE_DTE_MH;
import Entidades.RESPUESTA_RECEPCIONDTELOTE_MH;
import Entidades.RESPUESTA_RECEPCIONDTE_MH;
import Entidades.Respuesta_INFILE_DGI;
import Entidades.RESPUESTA_RECEPCIONDTE_INFILE;
import Entidades.TokenMH;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("felsv")
public class MyResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it! (CAMBIO).";
    }
    
    @Path("recepciondte-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            /****************************************************************************************************
             * SELECCIONAR DOCUMENTO EN JDE Y REGISTRAR EN EL ESQUEMA FELSV.                                    *  
             ****************************************************************************************************/
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS CCF DESDE JDE HACIA FELSV.                                                    *
             ****************************************************************************************************/
            Ctrl_DTE_CCF_V3 ctrl_dte_ccf_v3 = new Ctrl_DTE_CCF_V3();
            List<Long> no_dtes_ccf = ctrl_dte_ccf_v3.extraer_documento_jde_ccf_v3(ambiente);

            for (Integer d = 0; d < no_dtes_ccf.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON SIN FIRMAR CCF.                                                                     *
                 ****************************************************************************************************/
                DTE_CCF_V3 dte_ccf_v3 = ctrl_dte_ccf_v3.generar_json_dte_ccf_v3(ambiente, no_dtes_ccf.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_ccf_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_ccf_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes_ccf.get(d), "ccf", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes_ccf.get(d), "ccf", gson.toJson(dte_ccf_v3));
                
                Number opcion_contigencia = 1;
                if (dte_ccf_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT CCF.                                                                         *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_ccf_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes_ccf.get(d), "ccf", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA CCF.                                                  *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_ccf_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_ccf_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_ccf_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes_ccf.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes_ccf.get(d), "ccf", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_ccf_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes_ccf.get(d), "ccf", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_ccf_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_ccf.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_ccf.get(d), "ccf", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_ccf_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_ccf_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_ccf_v3.getIdentificacion().getFecEmi() + " " + dte_ccf_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_ccf_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_ccf.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_ccf.get(d), "ccf", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS NC DESDE JDE HACIA FELSV.                                                     *
             ****************************************************************************************************/
            Ctrl_DTE_NC_V3 ctrl_dte_nc_v3 = new Ctrl_DTE_NC_V3();
            List<Long> no_dtes_nc = ctrl_dte_nc_v3.extraer_documento_jde_nc_v3(ambiente);

            for (Integer d = 0; d < no_dtes_nc.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON SIN FIRMAR NC.                                                                      *
                 ****************************************************************************************************/
                DTE_NC_V3 dte_nc_v3 = ctrl_dte_nc_v3.generar_json_dte_nc_v3(ambiente, no_dtes_nc.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nc_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nc_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes_nc.get(d), "nc", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes_nc.get(d), "nc", gson.toJson(dte_nc_v3));
                
                Number opcion_contigencia = 1;
                if (dte_nc_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT NC.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nc_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes_nc.get(d), "nc", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA NC.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nc_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nc_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nc_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes_nc.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes_nc.get(d), "nc", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nc_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes_nc.get(d), "nc", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nc_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_nc.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_nc.get(d), "nc", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nc_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nc_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nc_v3.getIdentificacion().getFecEmi() + " " + dte_nc_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nc_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_nc.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_nc.get(d), "nc", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS ND DESDE JDE HACIA FELSV.                                                     *
             ****************************************************************************************************/
            Ctrl_DTE_ND_V3 ctrl_dte_nd_v3 = new Ctrl_DTE_ND_V3();
            List<Long> no_dtes_nd = ctrl_dte_nd_v3.extraer_documento_jde_nd_v3(ambiente);

            for (Integer d = 0; d < no_dtes_nd.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON SIN FIRMAR ND.                                                                      *
                 ****************************************************************************************************/
                DTE_ND_V3 dte_nd_v3 = ctrl_dte_nd_v3.generar_json_dte_nd_v3(ambiente, no_dtes_nd.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nd_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nd_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes_nd.get(d), "nd", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes_nd.get(d), "nd", gson.toJson(dte_nd_v3));
                
                Number opcion_contigencia = 1;
                if (dte_nd_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT ND.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nd_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes_nd.get(d), "nd", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA ND.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nd_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nd_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nd_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes_nd.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes_nd.get(d), "nd", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nd_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes_nd.get(d), "nd", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nd_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_nd.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_nd.get(d), "nd", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nd_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nd_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nd_v3.getIdentificacion().getFecEmi() + " " + dte_nd_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nd_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_nd.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_nd.get(d), "nd", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS F DESDE JDE HACIA FELSV.                                                      *
             ****************************************************************************************************/
            Ctrl_DTE_F_V3 ctrl_dte_f_v3 = new Ctrl_DTE_F_V3();
            List<Long> no_dtes_f = ctrl_dte_f_v3.extraer_documento_jde_f_v3(ambiente);

            for (Integer d = 0; d < no_dtes_f.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON SIN FIRMAR F.                                                                       *
                 ****************************************************************************************************/
                DTE_F_V3 dte_f_v3 = ctrl_dte_f_v3.generar_json_dte_f_v3(ambiente, no_dtes_f.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_f_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_f_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes_f.get(d), "f", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes_f.get(d), "f", gson.toJson(dte_f_v3));
                
                Number opcion_contigencia = 1;
                if (dte_f_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT F.                                                                           *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_f_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes_f.get(d), "f", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA F.                                                    *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_f_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_f_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_f_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes_f.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes_f.get(d), "f", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_f_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes_f.get(d), "f", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_f_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_f.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_f.get(d), "f", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_f_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_f_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_f_v3.getIdentificacion().getFecEmi() + " " + dte_f_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_f_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_f.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_f.get(d), "f", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS FEX DESDE JDE HACIA FELSV.                                                    *
             ****************************************************************************************************/
            Ctrl_DTE_FEX_V3 ctrl_dte_fex_v3 = new Ctrl_DTE_FEX_V3();
            List<Long> no_dtes_fex = ctrl_dte_fex_v3.extraer_documento_jde_fex_v3(ambiente);

            for (Integer d = 0; d < no_dtes_fex.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON SIN FIRMAR FEX.                                                                     *
                 ****************************************************************************************************/
                DTE_FEX_V3 dte_fex_v3 = ctrl_dte_fex_v3.generar_json_dte_fex_v3(ambiente, no_dtes_fex.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_fex_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_fex_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente,no_dtes_fex.get(d), "fex", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes_fex.get(d), "fex", gson.toJson(dte_fex_v3));
                
                Number opcion_contigencia = 1;
                if (dte_fex_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT FEX.                                                                         *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_fex_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes_fex.get(d), "fex", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA FEX.                                                  *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_fex_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_fex_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_fex_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes_fex.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes_fex.get(d), "fex", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA FEX.                                                        *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_fex_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes_fex.get(d), "fex", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                     /****************************************************************************************************
                      * RESPUESTA DEL MINISTERIO DE HACIENDA FEX.                                                        *
                      ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_fex_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_fex.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_fex.get(d), "fex", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                      * RESPUESTA DEL MINISTERIO DE HACIENDA FEX.                                                        *
                      ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_fex_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_fex_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_fex_v3.getIdentificacion().getFecEmi() + " " + dte_fex_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_fex_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_fex.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_fex.get(d), "fex", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS NR DESDE JDE HACIA FELSV.                                                     *
             ****************************************************************************************************/
            Ctrl_DTE_NR_V3 ctrl_dte_nr_v3 = new Ctrl_DTE_NR_V3();
            List<Long> no_dtes_nr = ctrl_dte_nr_v3.extraer_documento_jde_nr_v3(ambiente);

            for (Integer d = 0; d < no_dtes_nr.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON SIN FIRMAR NR.                                                                      *
                 ****************************************************************************************************/
                DTE_NR_V3 dte_nr_v3 = ctrl_dte_nr_v3.generar_json_dte_nr_v3(ambiente, no_dtes_nr.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nr_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nr_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes_nr.get(d), "nr", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes_nr.get(d), "nr", gson.toJson(dte_nr_v3));
                
                Number opcion_contigencia = 1;
                if (dte_nr_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT NR.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nr_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes_nr.get(d), "nr", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA NR.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nr_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nr_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nr_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes_nr.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes_nr.get(d), "nr", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nr_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes_nr.get(d), "nr", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_nr.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_nr.get(d), "nr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nr_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nr_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nr_v3.getIdentificacion().getFecEmi() + " " + dte_nr_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_nr.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_nr.get(d), "nr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS CR DESDE JDE HACIA FELSV.                                                     *
             ****************************************************************************************************/
            Ctrl_DTE_CR_V3 ctrl_dte_cr_v3 = new Ctrl_DTE_CR_V3();
            List<Long> no_dtes_cr = ctrl_dte_cr_v3.extraer_documento_jde_cr_v3(ambiente);

            for (Integer d = 0; d < no_dtes_cr.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON SIN FIRMAR CR.                                                                      *
                 ****************************************************************************************************/
                DTE_CR_V3 dte_cr_v3 = ctrl_dte_cr_v3.generar_json_dte_cr_v3(ambiente, no_dtes_cr.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_cr_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_cr_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes_cr.get(d), "cr", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes_cr.get(d), "cr", gson.toJson(dte_cr_v3));
                
                Number opcion_contigencia = 1;
                if (dte_cr_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT CR.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_cr_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes_cr.get(d), "cr", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA CR.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_cr_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_cr_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_cr_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes_cr.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes_cr.get(d), "cr", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_cr_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes_cr.get(d), "cr", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_cr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_cr.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_cr.get(d), "cr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_cr_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_cr_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_cr_v3.getIdentificacion().getFecEmi() + " " + dte_cr_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_cr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_cr.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_cr.get(d), "cr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS FSE DESDE JDE HACIA FELSV.                                                     *
             ****************************************************************************************************/
            Ctrl_DTE_FSE_V3 ctrl_dte_fse_v3 = new Ctrl_DTE_FSE_V3();
            List<Long> no_dtes_fse = ctrl_dte_fse_v3.extraer_documento_jde_fse_v3(ambiente);

            for (Integer d = 0; d < no_dtes_fse.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON SIN FIRMAR FSE.                                                                      *
                 ****************************************************************************************************/
                DTE_FSE_V3 dte_fse_v3 = ctrl_dte_fse_v3.generar_json_dte_fse_v3(ambiente, no_dtes_fse.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_fse_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_fse_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes_fse.get(d), "fse", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes_fse.get(d), "fse", gson.toJson(dte_fse_v3));
                
                Number opcion_contigencia = 1;
                if (dte_fse_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT FSE.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_fse_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes_fse.get(d), "fse", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA FSE.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_fse_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_fse_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_fse_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes_fse.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes_fse.get(d), "fse", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA FSE.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_fse_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes_fse.get(d), "fse", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA FSE.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_fse_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_fse.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_fse.get(d), "fse", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA FSE.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_fse_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_fse_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_fse_v3.getIdentificacion().getFecEmi() + " " + dte_fse_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_fse_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes_fse.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes_fse.get(d), "fse", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }
            
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("selecionar-documentos-v3/{ambiente}/{fecha}/{modo}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Integer selecionar_documentos_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {
        
        Integer resultado = 0;

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            resultado = ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:selecionar_documentos_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("extraer-documento-jde-ccf-v3/{ambiente}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String extraer_documento_jde_ccf_v3(@PathParam("ambiente") String ambiente) {
        String resultado = "";

        try {
            Ctrl_DTE_CCF_V3 ctrl_dte_ccf_v3 = new Ctrl_DTE_CCF_V3();
            List<Long> no_dtes = ctrl_dte_ccf_v3.extraer_documento_jde_ccf_v3(ambiente);
            resultado = "No. de Documentos procesados: " + no_dtes.size();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_jde_ccf_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("certificar-ccf-v3/{ambiente}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String certificar_ccf_v3(
            @PathParam("ambiente") String ambiente) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_CCF_V3 ctrl_dte_ccf_v3 = new Ctrl_DTE_CCF_V3();
            List<Long> no_dtes = ctrl_dte_ccf_v3.extraer_documento_jde_ccf_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_CCF_V3 dte_ccf_v3 = ctrl_dte_ccf_v3.generar_json_dte_ccf_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_ccf_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_ccf_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "ccf", gson.toJson(dte_ccf_v3));

                Number opcion_contigencia = 1;
                if (dte_ccf_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT CCF.                                                                         *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_ccf_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA CCF.                                                  *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_ccf_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_ccf_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_ccf_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_ccf_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_ccf_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_ccf_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_ccf_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_ccf_v3.getIdentificacion().getFecEmi() + " " + dte_ccf_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_ccf_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_ccf_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-ccf-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_ccf_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_CCF_V3 ctrl_dte_ccf_v3 = new Ctrl_DTE_CCF_V3();
            List<Long> no_dtes = ctrl_dte_ccf_v3.extraer_documento_jde_ccf_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_CCF_V3 dte_ccf_v3 = ctrl_dte_ccf_v3.generar_json_dte_ccf_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_ccf_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_ccf_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "ccf", gson.toJson(dte_ccf_v3));

                Number opcion_contigencia = 1;
                if (dte_ccf_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT CCF.                                                                         *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_ccf_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA CCF.                                                  *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_ccf_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_ccf_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_ccf_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_ccf_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_ccf_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CCF.                                                        *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_ccf_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_ccf_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_ccf_v3.getIdentificacion().getFecEmi() + " " + dte_ccf_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_ccf_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_ccf_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("extraer-documento-jde-nc-v3/{ambiente}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String extraer_documento_jde_nc_v3(@PathParam("ambiente") String ambiente) {
        String resultado = "";

        try {
            Ctrl_DTE_NC_V3 ctrl_dte_nc_v3 = new Ctrl_DTE_NC_V3();
            List<Long> no_dtes = ctrl_dte_nc_v3.extraer_documento_jde_nc_v3(ambiente);
            resultado = "No. de Documentos procesados: " + no_dtes.size();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_jde_nc_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("certificar-nc-v3/{ambiente}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String certificar_nc_v3(
            @PathParam("ambiente") String ambiente) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_NC_V3 ctrl_dte_nc_v3 = new Ctrl_DTE_NC_V3();
            List<Long> no_dtes = ctrl_dte_nc_v3.extraer_documento_jde_nc_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_NC_V3 dte_nc_v3 = ctrl_dte_nc_v3.generar_json_dte_nc_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nc_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nc_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nc", gson.toJson(dte_nc_v3));

                Number opcion_contigencia = 1;
                if (dte_nc_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT NC.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nc_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA NC.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nc_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nc_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nc_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nc_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nc_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nc_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nc_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nc_v3.getIdentificacion().getFecEmi() + " " + dte_nc_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nc_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nc_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-nc-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_nc_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_NC_V3 ctrl_dte_nc_v3 = new Ctrl_DTE_NC_V3();
            List<Long> no_dtes = ctrl_dte_nc_v3.extraer_documento_jde_nc_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_NC_V3 dte_nc_v3 = ctrl_dte_nc_v3.generar_json_dte_nc_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nc_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nc_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nc", gson.toJson(dte_nc_v3));

                Number opcion_contigencia = 1;
                if (dte_nc_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT NC.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nc_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA NC.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nc_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nc_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nc_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nc_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nc_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NC.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nc_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nc_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nc_v3.getIdentificacion().getFecEmi() + " " + dte_nc_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nc_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nc_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("extraer-documento-jde-nd-v3/{ambiente}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String extraer_documento_jde_nd_v3(@PathParam("ambiente") String ambiente) {
        String resultado = "";

        try {
            Ctrl_DTE_ND_V3 ctrl_dte_nd_v3 = new Ctrl_DTE_ND_V3();
            List<Long> no_dtes = ctrl_dte_nd_v3.extraer_documento_jde_nd_v3(ambiente);
            resultado = "No. de Documentos procesados: " + no_dtes.size();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_jde_nd_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("certificar-nd-v3/{ambiente}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String certificar_nd_v3(
            @PathParam("ambiente") String ambiente) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_ND_V3 ctrl_dte_nd_v3 = new Ctrl_DTE_ND_V3();
            List<Long> no_dtes = ctrl_dte_nd_v3.extraer_documento_jde_nd_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_ND_V3 dte_nd_v3 = ctrl_dte_nd_v3.generar_json_dte_nd_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nd_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nd_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nd", gson.toJson(dte_nd_v3));

                Number opcion_contigencia = 1;
                if (dte_nd_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT ND.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nd_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA ND.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nd_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nd_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nd_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nd_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nd_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nd_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nd_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nd_v3.getIdentificacion().getFecEmi() + " " + dte_nd_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nd_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nd_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-nd-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_nd_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_ND_V3 ctrl_dte_nd_v3 = new Ctrl_DTE_ND_V3();
            List<Long> no_dtes = ctrl_dte_nd_v3.extraer_documento_jde_nd_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_ND_V3 dte_nd_v3 = ctrl_dte_nd_v3.generar_json_dte_nd_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nd_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nd_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nd", gson.toJson(dte_nd_v3));

                Number opcion_contigencia = 1;
                if (dte_nd_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT ND.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nd_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA ND.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nd_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nd_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nd_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nd_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nd_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA ND.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nd_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nd_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nd_v3.getIdentificacion().getFecEmi() + " " + dte_nd_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nd_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nd_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("extraer-documento-jde-f-v3/{ambiente}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String extraer_documento_jde_f_v3(@PathParam("ambiente") String ambiente) {
        String resultado = "";

        try {
            Ctrl_DTE_F_V3 ctrl_dte_f_v3 = new Ctrl_DTE_F_V3();
            List<Long> no_dtes = ctrl_dte_f_v3.extraer_documento_jde_f_v3(ambiente);
            resultado = "No. de Documentos procesados: " + no_dtes.size();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_jde_f_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("certificar-f-v3/{ambiente}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String certificar_f_v3(
            @PathParam("ambiente") String ambiente) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_F_V3 ctrl_dte_f_v3 = new Ctrl_DTE_F_V3();
            List<Long> no_dtes = ctrl_dte_f_v3.extraer_documento_jde_f_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_F_V3 dte_f_v3 = ctrl_dte_f_v3.generar_json_dte_f_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_f_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_f_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "f", gson.toJson(dte_f_v3));

                Number opcion_contigencia = 1;
                if (dte_f_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT F.                                                                           *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_f_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA F.                                                    *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_f_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_f_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_f_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_f_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_f_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_f_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_f_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_f_v3.getIdentificacion().getFecEmi() + " " + dte_f_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_f_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_f_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-f-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_f_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_F_V3 ctrl_dte_f_v3 = new Ctrl_DTE_F_V3();
            List<Long> no_dtes = ctrl_dte_f_v3.extraer_documento_jde_f_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_F_V3 dte_f_v3 = ctrl_dte_f_v3.generar_json_dte_f_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_f_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_f_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "f", gson.toJson(dte_f_v3));

                Number opcion_contigencia = 1;
                if (dte_f_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT F.                                                                           *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_f_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA F.                                                    *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_f_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_f_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_f_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_f_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_f_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA F.                                                          *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_f_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_f_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_f_v3.getIdentificacion().getFecEmi() + " " + dte_f_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_f_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_f_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("extraer-documento-jde-fex-v3/{ambiente}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String extraer_documento_jde_fex_v3(@PathParam("ambiente") String ambiente) {
        String resultado = "";

        try {
            Ctrl_DTE_FEX_V3 ctrl_dte_fex_v3 = new Ctrl_DTE_FEX_V3();
            List<Long> no_dtes = ctrl_dte_fex_v3.extraer_documento_jde_fex_v3(ambiente);
            resultado = "No. de Documentos procesados: " + no_dtes.size();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_jde_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("certificar-fex-v3/{ambiente}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String certificar_fex_v3(
            @PathParam("ambiente") String ambiente) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_FEX_V3 ctrl_dte_fex_v3 = new Ctrl_DTE_FEX_V3();
            List<Long> no_dtes = ctrl_dte_fex_v3.extraer_documento_jde_fex_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_FEX_V3 dte_fex_v3 = ctrl_dte_fex_v3.generar_json_dte_fex_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_fex_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_fex_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "fex", gson.toJson(dte_fex_v3));

                Number opcion_contigencia = 1;
                if (dte_fex_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT FEX.                                                                         *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_fex_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA FEX.                                                  *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_fex_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_fex_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_fex_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA FEX.                                                        *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_fex_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                     /****************************************************************************************************
                      * RESPUESTA DEL MINISTERIO DE HACIENDA FEX.                                                        *
                      ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_fex_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                      * RESPUESTA DEL MINISTERIO DE HACIENDA FEX.                                                        *
                      ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_fex_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_fex_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_fex_v3.getIdentificacion().getFecEmi() + " " + dte_fex_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_fex_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-fex-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_fex_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_FEX_V3 ctrl_dte_fex_v3 = new Ctrl_DTE_FEX_V3();
            List<Long> no_dtes = ctrl_dte_fex_v3.extraer_documento_jde_fex_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_FEX_V3 dte_fex_v3 = ctrl_dte_fex_v3.generar_json_dte_fex_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_fex_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_fex_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "fex", gson.toJson(dte_fex_v3));

                Number opcion_contigencia = 1;
                if (dte_fex_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT FEX.                                                                         *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_fex_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA FEX.                                                  *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_fex_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_fex_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_fex_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA FEX.                                                        *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_fex_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                     /****************************************************************************************************
                      * RESPUESTA DEL MINISTERIO DE HACIENDA FEX.                                                        *
                      ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_fex_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                      * RESPUESTA DEL MINISTERIO DE HACIENDA FEX.                                                        *
                      ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_fex_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_fex_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_fex_v3.getIdentificacion().getFecEmi() + " " + dte_fex_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_fex_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_fex_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("extraer-documento-jde-nr-v3/{ambiente}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String extraer_documento_jde_nr_v3(@PathParam("ambiente") String ambiente) {
        String resultado = "";

        try {
            Ctrl_DTE_NR_V3 ctrl_dte_nr_v3 = new Ctrl_DTE_NR_V3();
            List<Long> no_dtes = ctrl_dte_nr_v3.extraer_documento_jde_nr_v3(ambiente);
            resultado = "No. de Documentos procesados: " + no_dtes.size();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_jde_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("certificar-nr-v3/{ambiente}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String certificar_nr_v3(
            @PathParam("ambiente") String ambiente) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_NR_V3 ctrl_dte_nr_v3 = new Ctrl_DTE_NR_V3();
            List<Long> no_dtes = ctrl_dte_nr_v3.extraer_documento_jde_nr_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_NR_V3 dte_nr_v3 = ctrl_dte_nr_v3.generar_json_dte_nr_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nr_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nr_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nr", gson.toJson(dte_nr_v3));

                Number opcion_contigencia = 1;
                if (dte_nr_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT NR.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nr_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA NR.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nr_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nr_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nr_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nr_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nr_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nr_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nr_v3.getIdentificacion().getFecEmi() + " " + dte_nr_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-nr-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_nr_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_NR_V3 ctrl_dte_nr_v3 = new Ctrl_DTE_NR_V3();
            List<Long> no_dtes = ctrl_dte_nr_v3.extraer_documento_jde_nr_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_NR_V3 dte_nr_v3 = ctrl_dte_nr_v3.generar_json_dte_nr_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_nr_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_nr_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nr", gson.toJson(dte_nr_v3));

                Number opcion_contigencia = 1;
                if (dte_nr_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT NR.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nr_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA NR.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_nr_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_nr_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_nr_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_nr_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_nr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA NR.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_nr_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_nr_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_nr_v3.getIdentificacion().getFecEmi() + " " + dte_nr_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_nr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("extraer-documento-jde-cr-v3/{ambiente}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String extraer_documento_jde_cr_v3(@PathParam("ambiente") String ambiente) {
        String resultado = "";

        try {
            Ctrl_DTE_CR_V3 ctrl_dte_cr_v3 = new Ctrl_DTE_CR_V3();
            List<Long> no_dtes = ctrl_dte_cr_v3.extraer_documento_jde_cr_v3(ambiente);
            resultado = "No. de Documentos procesados: " + no_dtes.size();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:extraer_documento_jde_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("certificar-cr-v3/{ambiente}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String certificar_cr_v3(
            @PathParam("ambiente") String ambiente) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_CR_V3 ctrl_dte_cr_v3 = new Ctrl_DTE_CR_V3();
            List<Long> no_dtes = ctrl_dte_cr_v3.extraer_documento_jde_cr_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_CR_V3 dte_cr_v3 = ctrl_dte_cr_v3.generar_json_dte_cr_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_cr_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_cr_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "cr", gson.toJson(dte_cr_v3));

                Number opcion_contigencia = 1;
                if (dte_cr_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT CR.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_cr_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA CR.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_cr_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_cr_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_cr_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_cr_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_cr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_cr_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_cr_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_cr_v3.getIdentificacion().getFecEmi() + " " + dte_cr_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_cr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-cr-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_cr_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_CR_V3 ctrl_dte_cr_v3 = new Ctrl_DTE_CR_V3();
            List<Long> no_dtes = ctrl_dte_cr_v3.extraer_documento_jde_cr_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_CR_V3 dte_cr_v3 = ctrl_dte_cr_v3.generar_json_dte_cr_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_cr_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_cr_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "cr", gson.toJson(dte_cr_v3));

                Number opcion_contigencia = 1;
                if (dte_cr_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT CR.                                                                          *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_cr_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA CR.                                                   *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_cr_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_cr_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_cr_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_cr_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_cr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA CR.                                                         *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_cr_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_cr_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_cr_v3.getIdentificacion().getFecEmi() + " " + dte_cr_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_cr_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_cr_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-fse-v3/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_fse_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_FSE_V3 ctrl_dte_fse_v3 = new Ctrl_DTE_FSE_V3();
            List<Long> no_dtes = ctrl_dte_fse_v3.extraer_documento_jde_fse_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_FSE_V3 dte_fse_v3 = ctrl_dte_fse_v3.generar_json_dte_fse_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_fse_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_fse_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fse", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "fse", gson.toJson(dte_fse_v3));

                Number opcion_contigencia = 1;
                if (dte_fse_v3.getIdentificacion().getTipoOperacion().equals(opcion_contigencia)) {
                    /****************************************************************************************************
                     * FIRMAR JSON CON JWT FSE.                                                                         *
                     ****************************************************************************************************/
                    Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                    Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_fse_v3.getEmisor().getNit(), dte_sin_firmar);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fse", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));
                    /****************************************************************************************************
                     * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA FSE.                                                  *
                     ****************************************************************************************************/
                    JsonDTE json_dte = new JsonDTE();
                    json_dte.setVersion(dte_fse_v3.getIdentificacion().getVersion().intValue());
                    json_dte.setAmbiente(dte_fse_v3.getIdentificacion().getAmbiente());
                    json_dte.setTipoDte(dte_fse_v3.getIdentificacion().getTipoDte());
                    json_dte.setIdEnvio(no_dtes.get(d));
                    json_dte.setDocumento(dte_firmado.getBody());
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fse", "JSON-DTE:: " + new Gson().toJson(json_dte));
                    /****************************************************************************************************
                     * GENERAR TOKEN MINISTERIO DE HACIENDA FSE.                                                         *
                     ****************************************************************************************************/
                    Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                    String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_fse_v3.getEmisor().getNit(), "UNOSV2021*");
                    Type listType1 = new TypeToken<TokenMH>() {
                    }.getType();
                    TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fse", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA FSE.                                                        *
                     ****************************************************************************************************/
                    String respuesta_mh = cliente_rest_mh.recepciondte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                    }.getType();
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                    ctrl_dte_fse_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fse", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                } else {
                    /****************************************************************************************************
                     * RESPUESTA DEL MINISTERIO DE HACIENDA FSE.                                                        *
                     ****************************************************************************************************/
                    RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new RESPUESTA_RECEPCIONDTE_MH();
                    respuesta_recepciondte_mh.setVersion(dte_fse_v3.getIdentificacion().getVersion());
                    respuesta_recepciondte_mh.setAmbiente(dte_fse_v3.getIdentificacion().getAmbiente());
                    respuesta_recepciondte_mh.setVersionApp(2);
                    respuesta_recepciondte_mh.setEstado("EMITIDO-CONTIGENCIA");
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_recepciondte_mh.setSelloRecibido("-");
                    respuesta_recepciondte_mh.setFhProcesamiento(dte_fse_v3.getIdentificacion().getFecEmi() + " " + dte_fse_v3.getIdentificacion().getHorEmi());
                    respuesta_recepciondte_mh.setClasificaMsg("00");
                    respuesta_recepciondte_mh.setCodigoMsg("CCC");
                    respuesta_recepciondte_mh.setDescripcionMsg("Documeto emitido en contigencia.");
                    List<String> observaciones = new ArrayList<>();
                    observaciones.add("Documeto emitido en contigencia.");
                    respuesta_recepciondte_mh.setObservaciones(observaciones);
                    ctrl_dte_fse_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                    driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fse", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
                }
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_fse_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("anulardte-v3/{ambiente}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String anulardte_v3(
            @PathParam("ambiente") String ambiente) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_INVALIDACION_V3 ctrl_dte_invalidacion_v3 = new Ctrl_DTE_INVALIDACION_V3();
            List<Long> no_dtes = ctrl_dte_invalidacion_v3.extraer_documento_jde_invalidacion_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_INVALIDACION_V3 dte_invalidacion_v3 = ctrl_dte_invalidacion_v3.generar_json_dte_invalidacion_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String dte_sin_firmar = "{"
                        + "\"nit\":\"" + dte_invalidacion_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_invalidacion_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "anulardte", "JSON-NO-FIRMADO:: " + dte_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "anulardte", gson.toJson(dte_invalidacion_v3));

                // FIRMAR JSON CON JWT.
                Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_invalidacion_v3.getEmisor().getNit(), dte_sin_firmar);
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "anulardte", "JSON-FIRMADO:: " + new Gson().toJson(dte_firmado));

                // ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA.
                JsonDTE json_dte = new JsonDTE();
                json_dte.setVersion(dte_invalidacion_v3.getIdentificacion().getVersion().intValue());
                json_dte.setAmbiente(dte_invalidacion_v3.getIdentificacion().getAmbiente());
                json_dte.setIdEnvio(no_dtes.get(d));
                json_dte.setDocumento(dte_firmado.getBody());
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "anulardte", "JSON-DTE:: " + new Gson().toJson(json_dte));

                // GENERAR TOKEN MINISTERIO DE HACIENDA.
                Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_invalidacion_v3.getEmisor().getNit(), "UNOSV2021*");
                Type listType1 = new TypeToken<TokenMH>() {
                }.getType();
                TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "anulardte", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));

                // RESPUESTA DEL MINISTERIO DE HACIENDA.
                String respuesta_mh = cliente_rest_mh.anulardte(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_dte));
                Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_MH>() {
                }.getType();
                RESPUESTA_RECEPCIONDTE_MH respuesta_recepciondte_mh = new Gson().fromJson(respuesta_mh, listType2);
                ctrl_dte_invalidacion_v3.registro_db_respuesta_mh(ambiente, respuesta_recepciondte_mh, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "anulardte", "RESPUESTA-DTE-MH:: " + new Gson().toJson(respuesta_recepciondte_mh));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:anulardte_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("contingencia-v3/{ambiente}/{KCOO_JDE}/{MCU_JDE}/{id_emisor}/{fecha_hora_inicio}/{fecha_hora_fin}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String contingencia_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("KCOO_JDE") String KCOO_JDE, 
            @PathParam("MCU_JDE") String MCU_JDE,
            @PathParam("id_emisor") Long id_emisor, 
            @PathParam("fecha_hora_inicio") String fecha_hora_inicio, 
            @PathParam("fecha_hora_fin") String fecha_hora_fin) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_CONTINGENCIA_V3 ctrl_dte_contingencia_v3 = new Ctrl_DTE_CONTINGENCIA_V3();
            List<Long> no_contin = ctrl_dte_contingencia_v3.extraer_evento_contingencia_v3(ambiente, KCOO_JDE, MCU_JDE, id_emisor, fecha_hora_inicio, fecha_hora_fin);
            
            for (Integer d = 0; d < no_contin.size(); d++) {
                // GENERAR JSON SIN FIRMAR.
                DTE_CONTIGENCIA_V3 dte_contingencia_v3 = ctrl_dte_contingencia_v3.generar_json_contingencia_v3(ambiente, no_contin.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                String json_conting_sin_firmar = "{"
                        + "\"nit\":\"" + dte_contingencia_v3.getEmisor().getNit() + "\","
                        + "\"activo\":true,"
                        + "\"passwordPri\":\"UNOSV2021*\","
                        + "\"dteJson\":" + gson.toJson(dte_contingencia_v3)
                        + "}";
                driver.guardar_en_archivo(ambiente, no_contin.get(d), "contin", "JSON-EVENTO-CONTIN-NO-FIRMADO:: " + json_conting_sin_firmar);
                driver.guardar_en_archivo_json(ambiente, no_contin.get(d), "contin", gson.toJson(dte_contingencia_v3));
                /****************************************************************************************************
                 * FIRMAR JSON CON JWT EVENTO CONTINGENCIA.                                                         *
                 ****************************************************************************************************/
                Ctrl_Firmar_Documento_JWT ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                Json_Firmado json_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_contingencia_v3.getEmisor().getNit(), json_conting_sin_firmar);
                driver.guardar_en_archivo(ambiente, no_contin.get(d), "contin", "JSON-EVENTO-CONTIN-FIRMADO:: " + new Gson().toJson(json_firmado));
                /****************************************************************************************************
                 * ENVIAR DOCUMENTO AL MINISTERIO DE HACIENDA EVENTO CONTINGENCIA.                                  *
                 ****************************************************************************************************/
                JsonCONTIN json_contin = new JsonCONTIN();
                json_contin.setNit(dte_contingencia_v3.getEmisor().getNit());
                json_contin.setDocumento(json_firmado.getBody());
                driver.guardar_en_archivo(ambiente, no_contin.get(d), "contin", "JSON-EVENTO-CONTIN-MH:: " + new Gson().toJson(json_contin));
                /****************************************************************************************************
                 * GENERAR TOKEN MINISTERIO DE HACIENDA EVENTO CONTINGENCIA.                                        *
                 ****************************************************************************************************/
                Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
                String token_autenticacion = cliente_rest_mh.autenticar(ambiente, dte_contingencia_v3.getEmisor().getNit(), "UNOSV2021*");
                Type listType1 = new TypeToken<TokenMH>() {
                }.getType();
                TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
                driver.guardar_en_archivo(ambiente, no_contin.get(d), "contin", "AUTH-TOKEN-MH:: " + new Gson().toJson(token_mh));
                /****************************************************************************************************
                 * RESPUESTA DEL MINISTERIO DE HACIENDA EVENTO CONTINGENCIA.                                        *
                 ****************************************************************************************************/
                String respuesta_mh = cliente_rest_mh.contingencia(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_contin));
                Type listType2 = new TypeToken<RESPUESTA_CONTINGENCIA_MH>() {
                }.getType();
                RESPUESTA_CONTINGENCIA_MH respuesta_contingencia_mh = new Gson().fromJson(respuesta_mh, listType2);
                ctrl_dte_contingencia_v3.registro_db_respuesta_mh(ambiente, respuesta_contingencia_mh, no_contin.get(d));
                driver.guardar_en_archivo(ambiente, no_contin.get(d), "contin", "RESPUESTA-EVENTO-CONTIN-MH:: " + new Gson().toJson(respuesta_contingencia_mh));
                /****************************************************************************************************
                 * GENERAR JSON-LOTE SIN FIRMAR.                                                                    *
                 ****************************************************************************************************/
                List<String> lista_dtes_firmados = new ArrayList<>();
                if (respuesta_contingencia_mh.getEstado().equals("RECIBIDO")) {
                    for (Integer i = 0; i < dte_contingencia_v3.getDetalleDTE().size(); i++) {
                        switch (dte_contingencia_v3.getDetalleDTE().get(i).getTipoDoc()) {
                            case "01": {
                                Ctrl_DTE_F_V3 ctrl_dte_f_v3 = new Ctrl_DTE_F_V3();
                                Long id_dte_f = ctrl_dte_f_v3.obtener_id_dte_codigo_generacion(ambiente, dte_contingencia_v3.getDetalleDTE().get(i).getCodigoGeneracion());
                                DTE_F_V3 dte_f_v3 = ctrl_dte_f_v3.generar_json_dte_f_v3(ambiente, id_dte_f);
                                String dte_sin_firmar = "{"
                                        + "\"nit\":\"" + dte_f_v3.getEmisor().getNit() + "\","
                                        + "\"activo\":true,"
                                        + "\"passwordPri\":\"UNOSV2021*\","
                                        + "\"dteJson\":" + gson.toJson(dte_f_v3)
                                        + "}";
                                ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                                Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_f_v3.getEmisor().getNit(), dte_sin_firmar);
                                lista_dtes_firmados.add(dte_firmado.getBody());
                                break;
                            }
                            case "03": {
                                Ctrl_DTE_CCF_V3 ctrl_dte_ccf_v3 = new Ctrl_DTE_CCF_V3();
                                Long id_dte_ccf = ctrl_dte_ccf_v3.obtener_id_dte_codigo_generacion(ambiente, dte_contingencia_v3.getDetalleDTE().get(i).getCodigoGeneracion());
                                DTE_CCF_V3 dte_ccf_v3 = ctrl_dte_ccf_v3.generar_json_dte_ccf_v3(ambiente, id_dte_ccf);
                                String dte_sin_firmar = "{"
                                        + "\"nit\":\"" + dte_ccf_v3.getEmisor().getNit() + "\","
                                        + "\"activo\":true,"
                                        + "\"passwordPri\":\"UNOSV2021*\","
                                        + "\"dteJson\":" + gson.toJson(dte_ccf_v3)
                                        + "}";
                                ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                                Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_ccf_v3.getEmisor().getNit(), dte_sin_firmar);
                                lista_dtes_firmados.add(dte_firmado.getBody());
                                break;
                            }
                            case "04": {
                                Ctrl_DTE_NR_V3 ctrl_dte_nr_v3 = new Ctrl_DTE_NR_V3();
                                Long id_dte_nr = ctrl_dte_nr_v3.obtener_id_dte_codigo_generacion(ambiente, dte_contingencia_v3.getDetalleDTE().get(i).getCodigoGeneracion());
                                DTE_NR_V3 dte_nr_v3 = ctrl_dte_nr_v3.generar_json_dte_nr_v3(ambiente, id_dte_nr);
                                String dte_sin_firmar = "{"
                                        + "\"nit\":\"" + dte_nr_v3.getEmisor().getNit() + "\","
                                        + "\"activo\":true,"
                                        + "\"passwordPri\":\"UNOSV2021*\","
                                        + "\"dteJson\":" + gson.toJson(dte_nr_v3)
                                        + "}";
                                ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                                Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nr_v3.getEmisor().getNit(), dte_sin_firmar);
                                lista_dtes_firmados.add(dte_firmado.getBody());
                                break;
                            }
                            case "05": {
                                Ctrl_DTE_NC_V3 ctrl_dte_nc_v3 = new Ctrl_DTE_NC_V3();
                                Long id_dte_nc = ctrl_dte_nc_v3.obtener_id_dte_codigo_generacion(ambiente, dte_contingencia_v3.getDetalleDTE().get(i).getCodigoGeneracion());
                                DTE_NC_V3 dte_nc_v3 = ctrl_dte_nc_v3.generar_json_dte_nc_v3(ambiente, id_dte_nc);
                                String dte_sin_firmar = "{"
                                        + "\"nit\":\"" + dte_nc_v3.getEmisor().getNit() + "\","
                                        + "\"activo\":true,"
                                        + "\"passwordPri\":\"UNOSV2021*\","
                                        + "\"dteJson\":" + gson.toJson(dte_nc_v3)
                                        + "}";
                                ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                                Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nc_v3.getEmisor().getNit(), dte_sin_firmar);
                                lista_dtes_firmados.add(dte_firmado.getBody());
                                break;
                            }
                            case "06": {
                                Ctrl_DTE_ND_V3 ctrl_dte_nd_v3 = new Ctrl_DTE_ND_V3();
                                Long id_dte_nd = ctrl_dte_nd_v3.obtener_id_dte_codigo_generacion(ambiente, dte_contingencia_v3.getDetalleDTE().get(i).getCodigoGeneracion());
                                DTE_ND_V3 dte_nd_v3 = ctrl_dte_nd_v3.generar_json_dte_nd_v3(ambiente, id_dte_nd);
                                String dte_sin_firmar = "{"
                                        + "\"nit\":\"" + dte_nd_v3.getEmisor().getNit() + "\","
                                        + "\"activo\":true,"
                                        + "\"passwordPri\":\"UNOSV2021*\","
                                        + "\"dteJson\":" + gson.toJson(dte_nd_v3)
                                        + "}";
                                ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                                Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_nd_v3.getEmisor().getNit(), dte_sin_firmar);
                                lista_dtes_firmados.add(dte_firmado.getBody());
                                break;
                            }
                            case "07": {
                                Ctrl_DTE_CR_V3 ctrl_dte_cr_v3 = new Ctrl_DTE_CR_V3();
                                Long id_dte_cr = ctrl_dte_cr_v3.obtener_id_dte_codigo_generacion(ambiente, dte_contingencia_v3.getDetalleDTE().get(i).getCodigoGeneracion());
                                DTE_CR_V3 dte_cr_v3 = ctrl_dte_cr_v3.generar_json_dte_cr_v3(ambiente, id_dte_cr);
                                String dte_sin_firmar = "{"
                                        + "\"nit\":\"" + dte_cr_v3.getEmisor().getNit() + "\","
                                        + "\"activo\":true,"
                                        + "\"passwordPri\":\"UNOSV2021*\","
                                        + "\"dteJson\":" + gson.toJson(dte_cr_v3)
                                        + "}";
                                ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                                Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_cr_v3.getEmisor().getNit(), dte_sin_firmar);
                                lista_dtes_firmados.add(dte_firmado.getBody());
                                break;
                            }
                            case "11": {
                                Ctrl_DTE_FEX_V3 ctrl_dte_fex_v3 = new Ctrl_DTE_FEX_V3();
                                Long id_dte_fex = ctrl_dte_fex_v3.obtener_id_dte_codigo_generacion(ambiente, dte_contingencia_v3.getDetalleDTE().get(i).getCodigoGeneracion());
                                DTE_FEX_V3 dte_fex_v3 = ctrl_dte_fex_v3.generar_json_dte_fex_v3(ambiente, id_dte_fex);
                                String dte_sin_firmar = "{"
                                        + "\"nit\":\"" + dte_fex_v3.getEmisor().getNit() + "\","
                                        + "\"activo\":true,"
                                        + "\"passwordPri\":\"UNOSV2021*\","
                                        + "\"dteJson\":" + gson.toJson(dte_fex_v3)
                                        + "}";
                                ctrl_firmar_documento_jwt = new Ctrl_Firmar_Documento_JWT();
                                Json_Firmado dte_firmado = ctrl_firmar_documento_jwt.firmardocumento(ambiente, dte_fex_v3.getEmisor().getNit(), dte_sin_firmar);
                                lista_dtes_firmados.add(dte_firmado.getBody());
                                break;
                            }
                        }
                    }
                }
                /****************************************************************************************************
                 * GENERAICION JSON-LOTE-DTE CONTINGENCIA.                                                          *
                 ****************************************************************************************************/
                JsonLoteDTE json_lote_dte = new JsonLoteDTE();
                json_lote_dte.setAmbiente(dte_contingencia_v3.getIdentificacion().getAmbiente());
                json_lote_dte.setIdEnvio(UUID.randomUUID().toString().toUpperCase());
                json_lote_dte.setVersion(dte_contingencia_v3.getIdentificacion().getVersion().intValue());
                json_lote_dte.setNitEmisor(dte_contingencia_v3.getEmisor().getNit());
                json_lote_dte.setDocumentos(lista_dtes_firmados);
                driver.guardar_en_archivo(ambiente, no_contin.get(d), "contin", "JSON-LOTE-DTE-MH:: " + new Gson().toJson(json_lote_dte));
                /****************************************************************************************************
                 * RESPUESTA DEL MINISTERIO DE HACIENDA EVENTO CONTINGENCIA.                                        *
                 ****************************************************************************************************/
                respuesta_mh = cliente_rest_mh.recepcionlote(ambiente, token_mh.getBody().getToken(), new Gson().toJson(json_lote_dte));
                Type listType3 = new TypeToken<RESPUESTA_LOTE_DTE_MH>() {
                }.getType();
                RESPUESTA_LOTE_DTE_MH respuesta_lote_dte_mh = new Gson().fromJson(respuesta_mh, listType3);
                ctrl_dte_contingencia_v3.registro_db_respuesta_lote_mh(ambiente, respuesta_lote_dte_mh, no_contin.get(d));
                driver.guardar_en_archivo(ambiente, no_contin.get(d), "contin", "RESPUESTA-LOTE-DTE-MH:: " + new Gson().toJson(respuesta_lote_dte_mh));
                
                resultado = gson.toJson(respuesta_lote_dte_mh);
            }
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:contingencia_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("consultadtelote-v3/{ambiente}/{nit}/{codigo_lote}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String consultadtelote_v3(
            @PathParam("ambiente") String ambiente,
            @PathParam("nit") String nit,
            @PathParam("codigo_lote") String codigo_lote) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            /****************************************************************************************************
             * GENERAR TOKEN MINISTERIO DE HACIENDA EVENTO CONTINGENCIA.                                        *
             ****************************************************************************************************/
            Cliente_Rest_MH cliente_rest_mh = new Cliente_Rest_MH();
            String token_autenticacion = cliente_rest_mh.autenticar(ambiente, nit, "UNOSV2021*");
            Type listType1 = new TypeToken<TokenMH>() {
            }.getType();
            TokenMH token_mh = new Gson().fromJson(token_autenticacion, listType1);
            /****************************************************************************************************
             * RESPUESTA DEL MINISTERIO DE HACIENDA EVENTO CONTINGENCIA.                                        *
             ****************************************************************************************************/
            Gson gson = new GsonBuilder().serializeNulls().create();
            String respuesta_mh = cliente_rest_mh.consultadtelote(ambiente, token_mh.getBody().getToken(), codigo_lote);
            Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTELOTE_MH>() {
            }.getType();
            RESPUESTA_RECEPCIONDTELOTE_MH respuesta_recepciondtelote_mh = new Gson().fromJson(respuesta_mh, listType2);
            
            Ctrl_ConsultarDteLote_V3 ctrl_consultar_dte_lote_v3 = new Ctrl_ConsultarDteLote_V3();
            ctrl_consultar_dte_lote_v3.grabar_respuesta_dte_lote(ambiente, respuesta_recepciondtelote_mh);
            
            resultado = gson.toJson(respuesta_recepciondtelote_mh);
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:contingencia_v3()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("certificar-json-infile/{ambiente}/{usuario}/{llave}/{identificador}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String certificar_json_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("usuario") String usuario,
            @PathParam("llave") String llave,
            @PathParam("identificador") String identificador,
            String documento) {

        String resultado = "";

        try {
            System.out.println("JSON: " + documento);
            Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
            resultado = cliente_rest_infile.certificar_json(ambiente, usuario, llave, identificador, documento);
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:certificar_json_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-ccf-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_ccf_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_CCF_V3 ctrl_dte_ccf_v3 = new Ctrl_DTE_CCF_V3();
            List<Long> no_dtes = ctrl_dte_ccf_v3.extraer_documento_jde_ccf_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON CCF.                                                                                *
                 ****************************************************************************************************/
                DTE_CCF_V3 dte_ccf_v3 = ctrl_dte_ccf_v3.generar_json_dte_ccf_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "JSON:: " + gson.toJson(dte_ccf_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "ccf", gson.toJson(dte_ccf_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE CCF.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";

                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_ccf_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-CCF-" + no_dtes.get(d), gson.toJson(dte_ccf_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_ccf_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "ccf", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_ccf_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-f-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_f_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_F_V3 ctrl_dte_f_v3 = new Ctrl_DTE_F_V3();
            List<Long> no_dtes = ctrl_dte_f_v3.extraer_documento_jde_f_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON F.                                                                                *
                 ****************************************************************************************************/
                DTE_F_V3 dte_f_v3 = ctrl_dte_f_v3.generar_json_dte_f_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "JSON:: " + gson.toJson(dte_f_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "f", gson.toJson(dte_f_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE F.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_f_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-F-" + no_dtes.get(d), gson.toJson(dte_f_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_f_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "f", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_f_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }
    
    @Path("recepciondte-nc-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_nc_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_NC_V3 ctrl_dte_nc_v3 = new Ctrl_DTE_NC_V3();
            List<Long> no_dtes = ctrl_dte_nc_v3.extraer_documento_jde_nc_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON NC.                                                                                *
                 ****************************************************************************************************/
                DTE_NC_V3 dte_nc_v3 = ctrl_dte_nc_v3.generar_json_dte_nc_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "JSON:: " + gson.toJson(dte_nc_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nc", gson.toJson(dte_nc_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE NC.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nc_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-NC-" + no_dtes.get(d), gson.toJson(dte_nc_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nc_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nc", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nc_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-nd-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_nd_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_ND_V3 ctrl_dte_nd_v3 = new Ctrl_DTE_ND_V3();
            List<Long> no_dtes = ctrl_dte_nd_v3.extraer_documento_jde_nd_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON ND.                                                                                *
                 ****************************************************************************************************/
                DTE_ND_V3 dte_nd_v3 = ctrl_dte_nd_v3.generar_json_dte_nd_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "JSON:: " + gson.toJson(dte_nd_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nd", gson.toJson(dte_nd_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE ND.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nd_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-ND-" + no_dtes.get(d), gson.toJson(dte_nd_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nd_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nd", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nd_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-fex-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_fex_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_FEX_V3 ctrl_dte_fex_v3 = new Ctrl_DTE_FEX_V3();
            List<Long> no_dtes = ctrl_dte_fex_v3.extraer_documento_jde_fex_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON FEX.                                                                                *
                 ****************************************************************************************************/
                DTE_FEX_V3 dte_fex_v3 = ctrl_dte_fex_v3.generar_json_dte_fex_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "JSON:: " + gson.toJson(dte_fex_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "fex", gson.toJson(dte_fex_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE FEX.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_fex_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-FEX-" + no_dtes.get(d), gson.toJson(dte_fex_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_fex_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fex", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_fex_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-nr-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_nr_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_NR_V3 ctrl_dte_nr_v3 = new Ctrl_DTE_NR_V3();
            List<Long> no_dtes = ctrl_dte_nr_v3.extraer_documento_jde_nr_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON NR.                                                                                *
                 ****************************************************************************************************/
                DTE_NR_V3 dte_nr_v3 = ctrl_dte_nr_v3.generar_json_dte_nr_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "JSON:: " + gson.toJson(dte_nr_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "nr", gson.toJson(dte_nr_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE NR.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nr_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-NR-" + no_dtes.get(d), gson.toJson(dte_nr_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nr_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "nr", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_nr_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-cr-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_cr_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_CR_V3 ctrl_dte_cr_v3 = new Ctrl_DTE_CR_V3();
            List<Long> no_dtes = ctrl_dte_cr_v3.extraer_documento_jde_cr_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON CR.                                                                                *
                 ****************************************************************************************************/
                DTE_CR_V3 dte_cr_v3 = ctrl_dte_cr_v3.generar_json_dte_cr_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "JSON:: " + gson.toJson(dte_cr_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "cr", gson.toJson(dte_cr_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE CR.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_cr_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-CR-" + no_dtes.get(d), gson.toJson(dte_cr_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_cr_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "cr", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_cr_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-fse-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_fse_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            // EXTRAER DOCUMENTOS DESDE JDE HACIA FEL_TEST.
            Ctrl_DTE_FSE_V3 ctrl_dte_fse_v3 = new Ctrl_DTE_FSE_V3();
            List<Long> no_dtes = ctrl_dte_fse_v3.extraer_documento_jde_fse_v3(ambiente);

            for (Integer d = 0; d < no_dtes.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON FSE.                                                                                *
                 ****************************************************************************************************/
                DTE_FSE_V3 dte_fse_v3 = ctrl_dte_fse_v3.generar_json_dte_fse_v3(ambiente, no_dtes.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fse", "JSON:: " + gson.toJson(dte_fse_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes.get(d), "fse", gson.toJson(dte_fse_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE FSE.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_fse_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-FSE-" + no_dtes.get(d), gson.toJson(dte_fse_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_fse_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes.get(d), "fse", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_fse_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-infile/{ambiente}/{fecha}/{modo}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("fecha") String fecha,
            @PathParam("modo") Integer modo) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            Ctrl_DTE_V3 ctrl_dte_v3 = new Ctrl_DTE_V3();
            ctrl_dte_v3.selecionar_documentos_v3(ambiente, fecha, modo);
            
            /****************************************************************************************************
             * EXTRAER DOCUMENTOS CCF DESDE JDE HACIA FEL_TEST.                                                 *
             ****************************************************************************************************/
            Ctrl_DTE_CCF_V3 ctrl_dte_ccf_v3 = new Ctrl_DTE_CCF_V3();
            List<Long> no_dtes_ccf = ctrl_dte_ccf_v3.extraer_documento_jde_ccf_v3(ambiente);

            for (Integer d = 0; d < no_dtes_ccf.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON CCF.                                                                                *
                 ****************************************************************************************************/
                DTE_CCF_V3 dte_ccf_v3 = ctrl_dte_ccf_v3.generar_json_dte_ccf_v3(ambiente, no_dtes_ccf.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes_ccf.get(d), "ccf", "JSON:: " + gson.toJson(dte_ccf_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes_ccf.get(d), "ccf", gson.toJson(dte_ccf_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE CCF.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_ccf_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-CCF-" + no_dtes_ccf.get(d), gson.toJson(dte_ccf_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_ccf_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes_ccf.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes_ccf.get(d), "ccf", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            /****************************************************************************************************
             * EXTRAER DOCUMENTOS F DESDE JDE HACIA FEL_TEST.                                                   *
             ****************************************************************************************************/
            Ctrl_DTE_F_V3 ctrl_dte_f_v3 = new Ctrl_DTE_F_V3();
            List<Long> no_dtes_f = ctrl_dte_f_v3.extraer_documento_jde_f_v3(ambiente);

            for (Integer d = 0; d < no_dtes_f.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON F.                                                                                *
                 ****************************************************************************************************/
                DTE_F_V3 dte_f_v3 = ctrl_dte_f_v3.generar_json_dte_f_v3(ambiente, no_dtes_f.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes_f.get(d), "f", "JSON:: " + gson.toJson(dte_f_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes_f.get(d), "f", gson.toJson(dte_f_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE F.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_f_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-F-" + no_dtes_f.get(d), gson.toJson(dte_f_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_f_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes_f.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes_f.get(d), "f", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            /****************************************************************************************************
             * EXTRAER DOCUMENTOS NC DESDE JDE HACIA FEL_TEST.                                                  *
             ****************************************************************************************************/
            Ctrl_DTE_NC_V3 ctrl_dte_nc_v3 = new Ctrl_DTE_NC_V3();
            List<Long> no_dtes_nc = ctrl_dte_nc_v3.extraer_documento_jde_nc_v3(ambiente);

            for (Integer d = 0; d < no_dtes_nc.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON NC.                                                                                *
                 ****************************************************************************************************/
                DTE_NC_V3 dte_nc_v3 = ctrl_dte_nc_v3.generar_json_dte_nc_v3(ambiente, no_dtes_nc.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes_nc.get(d), "nc", "JSON:: " + gson.toJson(dte_nc_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes_nc.get(d), "nc", gson.toJson(dte_nc_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE NC.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nc_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-NC-" + no_dtes_nc.get(d), gson.toJson(dte_nc_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nc_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes_nc.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes_nc.get(d), "nc", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            /****************************************************************************************************
             * EXTRAER DOCUMENTOS ND DESDE JDE HACIA FEL_TEST.                                                  *
             ****************************************************************************************************/
            Ctrl_DTE_ND_V3 ctrl_dte_nd_v3 = new Ctrl_DTE_ND_V3();
            List<Long> no_dtes_nd = ctrl_dte_nd_v3.extraer_documento_jde_nd_v3(ambiente);

            for (Integer d = 0; d < no_dtes_nd.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON ND.                                                                                *
                 ****************************************************************************************************/
                DTE_ND_V3 dte_nd_v3 = ctrl_dte_nd_v3.generar_json_dte_nd_v3(ambiente, no_dtes_nd.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes_nd.get(d), "nd", "JSON:: " + gson.toJson(dte_nd_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes_nd.get(d), "nd", gson.toJson(dte_nd_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE ND.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nd_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-ND-" + no_dtes_nd.get(d), gson.toJson(dte_nd_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nd_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes_nd.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes_nd.get(d), "nd", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            /****************************************************************************************************
             * EXTRAER DOCUMENTOS FEX DESDE JDE HACIA FEL_TEST.                                                 *
             ****************************************************************************************************/
            Ctrl_DTE_FEX_V3 ctrl_dte_fex_v3 = new Ctrl_DTE_FEX_V3();
            List<Long> no_dtes_fex = ctrl_dte_fex_v3.extraer_documento_jde_fex_v3(ambiente);

            for (Integer d = 0; d < no_dtes_fex.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON FEX.                                                                                *
                 ****************************************************************************************************/
                DTE_FEX_V3 dte_fex_v3 = ctrl_dte_fex_v3.generar_json_dte_fex_v3(ambiente, no_dtes_fex.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes_fex.get(d), "fex", "JSON:: " + gson.toJson(dte_fex_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes_fex.get(d), "fex", gson.toJson(dte_fex_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE FEX.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_fex_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-FEX-" + no_dtes_fex.get(d), gson.toJson(dte_fex_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_fex_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes_fex.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes_fex.get(d), "fex", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            /****************************************************************************************************
             * EXTRAER DOCUMENTOS NR DESDE JDE HACIA FEL_TEST.                                                  *
             ****************************************************************************************************/
            Ctrl_DTE_NR_V3 ctrl_dte_nr_v3 = new Ctrl_DTE_NR_V3();
            List<Long> no_dtes_nr = ctrl_dte_nr_v3.extraer_documento_jde_nr_v3(ambiente);

            for (Integer d = 0; d < no_dtes_nr.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON NR.                                                                                *
                 ****************************************************************************************************/
                DTE_NR_V3 dte_nr_v3 = ctrl_dte_nr_v3.generar_json_dte_nr_v3(ambiente, no_dtes_nr.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes_nr.get(d), "nr", "JSON:: " + gson.toJson(dte_nr_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes_nr.get(d), "nr", gson.toJson(dte_nr_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE NR.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nr_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-NR-" + no_dtes_nr.get(d), gson.toJson(dte_nr_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nr_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes_nr.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes_nr.get(d), "nr", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            /****************************************************************************************************
             * EXTRAER DOCUMENTOS CR DESDE JDE HACIA FEL_TEST.                                                  *
             ****************************************************************************************************/
            Ctrl_DTE_CR_V3 ctrl_dte_cr_v3 = new Ctrl_DTE_CR_V3();
            List<Long> no_dtes_cr = ctrl_dte_cr_v3.extraer_documento_jde_cr_v3(ambiente);

            for (Integer d = 0; d < no_dtes_cr.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON CR.                                                                                *
                 ****************************************************************************************************/
                DTE_CR_V3 dte_cr_v3 = ctrl_dte_cr_v3.generar_json_dte_cr_v3(ambiente, no_dtes_cr.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes_cr.get(d), "cr", "JSON:: " + gson.toJson(dte_cr_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes_cr.get(d), "cr", gson.toJson(dte_cr_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE CR.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_cr_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-CR-" + no_dtes_cr.get(d), gson.toJson(dte_cr_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_cr_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes_cr.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes_cr.get(d), "cr", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            /****************************************************************************************************
             * EXTRAER DOCUMENTOS FSE DESDE JDE HACIA FEL_TEST.                                                 *
             ****************************************************************************************************/
            Ctrl_DTE_FSE_V3 ctrl_dte_fse_v3 = new Ctrl_DTE_FSE_V3();
            List<Long> no_dtes_fse = ctrl_dte_fse_v3.extraer_documento_jde_fse_v3(ambiente);

            for (Integer d = 0; d < no_dtes_fse.size(); d++) {
                /****************************************************************************************************
                 * GENERAR JSON FSE.                                                                                *
                 ****************************************************************************************************/
                DTE_FSE_V3 dte_fse_v3 = ctrl_dte_fse_v3.generar_json_dte_fse_v3(ambiente, no_dtes_fse.get(d));
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, no_dtes_fse.get(d), "fse", "JSON:: " + gson.toJson(dte_fse_v3));
                driver.guardar_en_archivo_json(ambiente, no_dtes_fse.get(d), "fse", gson.toJson(dte_fse_v3));
                /****************************************************************************************************
                 * ENVIAR DODUMENTO Y RESPUESTA DE INFILE FSE.                                                      *
                 ****************************************************************************************************/
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                        // System.out.println("mysql-db-host:" + mysql_db_host);
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_fse_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-FSE-" + no_dtes_fse.get(d), gson.toJson(dte_fse_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_fse_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, no_dtes_fse.get(d));
                driver.guardar_en_archivo(ambiente, no_dtes_fse.get(d), "fse", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + no_dtes_ccf.toString();
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

    @Path("recepciondte-json-infile/{ambiente}/{tipo_dte}/{id_dte}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String recepciondte_json_infile(
            @PathParam("ambiente") String ambiente,
            @PathParam("tipo_dte") String tipo_dte,
            @PathParam("id_dte") Long id_dte) {

        Driver driver = new Driver();
        String resultado = "";

        try {
            if(tipo_dte.equals("ccf")) {
                Ctrl_DTE_CCF_V3 ctrl_dte_ccf_v3 = new Ctrl_DTE_CCF_V3();
                DTE_CCF_V3 dte_ccf_v3 = ctrl_dte_ccf_v3.generar_json_dte_ccf_v3(ambiente, id_dte);
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, id_dte, "ccf", "JSON:: " + gson.toJson(dte_ccf_v3));
                driver.guardar_en_archivo_json(ambiente, id_dte, "ccf", gson.toJson(dte_ccf_v3));
                
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_ccf_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-CCF-" + id_dte, gson.toJson(dte_ccf_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_ccf_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_ccf_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, id_dte);
                driver.guardar_en_archivo(ambiente, id_dte, "ccf", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            if(tipo_dte.equals("f")) {
                Ctrl_DTE_F_V3 ctrl_dte_f_v3 = new Ctrl_DTE_F_V3();
                DTE_F_V3 dte_f_v3 = ctrl_dte_f_v3.generar_json_dte_f_v3(ambiente, id_dte);
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, id_dte, "f", "JSON:: " + gson.toJson(dte_f_v3));
                driver.guardar_en_archivo_json(ambiente, id_dte, "f", gson.toJson(dte_f_v3));
                
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_f_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-F-" + id_dte, gson.toJson(dte_f_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_f_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_f_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, id_dte);
                driver.guardar_en_archivo(ambiente, id_dte, "f", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            if(tipo_dte.equals("nc")) {
                Ctrl_DTE_NC_V3 ctrl_dte_nc_v3 = new Ctrl_DTE_NC_V3();
                DTE_NC_V3 dte_nc_v3 = ctrl_dte_nc_v3.generar_json_dte_nc_v3(ambiente, id_dte);
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, id_dte, "nc", "JSON:: " + gson.toJson(dte_nc_v3));
                driver.guardar_en_archivo_json(ambiente, id_dte, "nc", gson.toJson(dte_nc_v3));
                
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nc_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-NC-" + id_dte, gson.toJson(dte_nc_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nc_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nc_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, id_dte);
                driver.guardar_en_archivo(ambiente, id_dte, "nc", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            if(tipo_dte.equals("nd")) {
                Ctrl_DTE_ND_V3 ctrl_dte_nd_v3 = new Ctrl_DTE_ND_V3();
                DTE_ND_V3 dte_nd_v3 = ctrl_dte_nd_v3.generar_json_dte_nd_v3(ambiente, id_dte);
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, id_dte, "nd", "JSON:: " + gson.toJson(dte_nd_v3));
                driver.guardar_en_archivo_json(ambiente, id_dte, "nd", gson.toJson(dte_nd_v3));
                
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nd_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-ND-" + id_dte, gson.toJson(dte_nd_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nd_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nd_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, id_dte);
                driver.guardar_en_archivo(ambiente, id_dte, "nd", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            if(tipo_dte.equals("fex")) {
                Ctrl_DTE_FEX_V3 ctrl_dte_fex_v3 = new Ctrl_DTE_FEX_V3();
                DTE_FEX_V3 dte_fex_v3 = ctrl_dte_fex_v3.generar_json_dte_fex_v3(ambiente, id_dte);
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, id_dte, "fex", "JSON:: " + gson.toJson(dte_fex_v3));
                driver.guardar_en_archivo_json(ambiente, id_dte, "fex", gson.toJson(dte_fex_v3));
                
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_fex_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-FEX-" + id_dte, gson.toJson(dte_fex_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_fex_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_fex_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, id_dte);
                driver.guardar_en_archivo(ambiente, id_dte, "fex", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            if(tipo_dte.equals("nr")) {
                Ctrl_DTE_NR_V3 ctrl_dte_nr_v3 = new Ctrl_DTE_NR_V3();
                DTE_NR_V3 dte_nr_v3 = ctrl_dte_nr_v3.generar_json_dte_nr_v3(ambiente, id_dte);
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, id_dte, "nr", "JSON:: " + gson.toJson(dte_nr_v3));
                driver.guardar_en_archivo_json(ambiente, id_dte, "nr", gson.toJson(dte_nr_v3));
                
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_nr_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-NR-" + id_dte, gson.toJson(dte_nr_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_nr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_nr_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, id_dte);
                driver.guardar_en_archivo(ambiente, id_dte, "nr", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            if(tipo_dte.equals("cr")) {
                Ctrl_DTE_CR_V3 ctrl_dte_cr_v3 = new Ctrl_DTE_CR_V3();
                DTE_CR_V3 dte_cr_v3 = ctrl_dte_cr_v3.generar_json_dte_cr_v3(ambiente, id_dte);
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, id_dte, "cr", "JSON:: " + gson.toJson(dte_cr_v3));
                driver.guardar_en_archivo_json(ambiente, id_dte, "cr", gson.toJson(dte_cr_v3));
                
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_cr_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-CR-" + id_dte, gson.toJson(dte_cr_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_cr_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_cr_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, id_dte);
                driver.guardar_en_archivo(ambiente, id_dte, "cr", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            if(tipo_dte.equals("fse")) {
                Ctrl_DTE_FSE_V3 ctrl_dte_fse_v3 = new Ctrl_DTE_FSE_V3();
                DTE_FSE_V3 dte_fse_v3 = ctrl_dte_fse_v3.generar_json_dte_fse_v3(ambiente, id_dte);
                Gson gson = new GsonBuilder().serializeNulls().create();
                driver.guardar_en_archivo(ambiente, id_dte, "fse", "JSON:: " + gson.toJson(dte_fse_v3));
                driver.guardar_en_archivo_json(ambiente, id_dte, "fse", gson.toJson(dte_fse_v3));
                
                String infile_rest_auth_py = "";
                String infile_rest_auth_pd = "";
                
                Ctrl_Archivos ctrl_archivos = new Ctrl_Archivos();
                List<String> lineas_archivos = ctrl_archivos.lineas_archivo("/FELSV3/config/properties.conf");
                for (Integer i = 0; i < lineas_archivos.size(); i++) {
                    String[] param_db = lineas_archivos.get(i).trim().split(":");

                    if (param_db[0].trim().equals("infile_rest_auth_py")) {
                        infile_rest_auth_py = param_db[1];
                    }
                    if (param_db[0].trim().equals("infile_rest_auth_pd")) {
                        infile_rest_auth_pd = param_db[1];
                    }
                }
                
                String llave_infile;
                if (ambiente.equals("PY")) {
                    llave_infile = infile_rest_auth_py;
                } else {
                    llave_infile = infile_rest_auth_pd;
                }

                Cliente_Rest_INFILE cliente_rest_infile = new Cliente_Rest_INFILE();
                String respuesta_infile = cliente_rest_infile.certificar_json(ambiente, dte_fse_v3.getEmisor().getNit(), llave_infile, "DTE-" + ambiente + "-FSE-" + id_dte, gson.toJson(dte_fse_v3));
                RESPUESTA_RECEPCIONDTE_INFILE respuesta_recepciondte_infile;
                try {
                    Type listType2 = new TypeToken<RESPUESTA_RECEPCIONDTE_INFILE>() {
                    }.getType();
                    respuesta_recepciondte_infile = new Gson().fromJson(respuesta_infile, listType2);
                    respuesta_infile = respuesta_infile.replaceAll("\'","");

                    if(!respuesta_recepciondte_infile.getOk()) {
                        SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        List<String> observaciones = new ArrayList<>();
                        observaciones.add(respuesta_infile);

                        Errores_INFILE errores_infile = new Errores_INFILE();
                        errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setVersion(2);
                        errores_infile.setAmbiente("00");
                        errores_infile.setVersionApp(2);
                        errores_infile.setEstado("RECHAZADO");
                        errores_infile.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                        errores_infile.setSelloRecibido(null);
                        errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        errores_infile.setClasificaMsg("99");
                        errores_infile.setCodigoMsg("999");
                        errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        errores_infile.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setErrores(errores_infile);

                        respuesta_recepciondte_infile.setRespuesta(null);

                        Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                        respuesta_infile_dgi.setVersion(2);
                        respuesta_infile_dgi.setAmbiente("00");
                        respuesta_infile_dgi.setVersionApp(2);
                        respuesta_infile_dgi.setEstado("RECHAZADO");
                        respuesta_infile_dgi.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                        respuesta_infile_dgi.setSelloRecibido(null);
                        respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                        respuesta_infile_dgi.setClasificaMsg("99");
                        respuesta_infile_dgi.setCodigoMsg("999");
                        respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                        respuesta_infile_dgi.setObservaciones(observaciones);
                        respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                        respuesta_recepciondte_infile.setPdf_path(null);
                        respuesta_recepciondte_infile.setAdendas(null);
                    }
                } catch(Exception ex) {
                    SimpleDateFormat dateFormat_Infile = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    List<String> observaciones = new ArrayList<>();
                    observaciones.add(respuesta_infile);

                    respuesta_recepciondte_infile = new RESPUESTA_RECEPCIONDTE_INFILE();
                    respuesta_recepciondte_infile.setOk(false);
                    respuesta_recepciondte_infile.setMensaje("Erros en el proceso para deserializar la respuesta INFILE.");

                    Errores_INFILE errores_infile = new Errores_INFILE();
                    errores_infile.setError_infile("Erros en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setVersion(2);
                    errores_infile.setAmbiente("00");
                    errores_infile.setVersionApp(2);
                    errores_infile.setEstado("RECHAZADO");
                    errores_infile.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    errores_infile.setSelloRecibido(null);
                    errores_infile.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    errores_infile.setClasificaMsg("99");
                    errores_infile.setCodigoMsg("999");
                    errores_infile.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    errores_infile.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setErrores(errores_infile);

                    respuesta_recepciondte_infile.setRespuesta(null);

                    Respuesta_INFILE_DGI respuesta_infile_dgi = new Respuesta_INFILE_DGI();
                    respuesta_infile_dgi.setVersion(2);
                    respuesta_infile_dgi.setAmbiente("00");
                    respuesta_infile_dgi.setVersionApp(2);
                    respuesta_infile_dgi.setEstado("RECHAZADO");
                    respuesta_infile_dgi.setCodigoGeneracion(dte_fse_v3.getIdentificacion().getCodigoGeneracion());
                    respuesta_infile_dgi.setSelloRecibido(null);
                    respuesta_infile_dgi.setFhProcesamiento(dateFormat_Infile.format(new Date()));
                    respuesta_infile_dgi.setClasificaMsg("99");
                    respuesta_infile_dgi.setCodigoMsg("999");
                    respuesta_infile_dgi.setDescripcionMsg("Error en el proceso para deserializar la respuesta INFILE.");
                    respuesta_infile_dgi.setObservaciones(observaciones);
                    respuesta_recepciondte_infile.setRespuesta_dgi(respuesta_infile_dgi);

                    respuesta_recepciondte_infile.setPdf_path(null);
                    respuesta_recepciondte_infile.setAdendas(null);
                }
                ctrl_dte_fse_v3.registro_db_respuesta_infile(ambiente, respuesta_recepciondte_infile, id_dte);
                driver.guardar_en_archivo(ambiente, id_dte, "fse", "RESPUESTA-DTE-INFILE:: " + new Gson().toJson(respuesta_recepciondte_infile));
            }

            resultado = "ID-DTE PROCESADOS: " + id_dte;
        } catch (Exception ex) {
            System.out.println("PROYECTO:api-grupoterra-svfel-v3|CLASE:" + this.getClass().getName() + "|METODO:recepciondte_infile()|ERROR:" + ex.toString());
        }

        return resultado;
    }

}
