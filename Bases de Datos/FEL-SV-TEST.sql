-- ***************************************************************************************************************************
-- *          CONSULTA DE TABLAS CATÁLOGO FEL-SV.                                                                            *
-- ***************************************************************************************************************************
SELECT C.* FROM CAT_000 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_001 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_002 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_003 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_004 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_005 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_006 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_007 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_008 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_009 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_010 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_011 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_012 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_013 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_014 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_015 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_016 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_017 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_018 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_019 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_020 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_021 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_022 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_023 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_024 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_025 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_026 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_027 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_028 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_029 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_030 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_031 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_032 C ORDER BY C.ID_CAT;
SELECT C.* FROM CAT_033 C ORDER BY C.ID_CAT;
-- ***************************************************************************************************************************
-- *          CONSULTA DE TABLAS EMISOR, SHAN, COMPAÑIA Y ESTABLECIMIENTO FEL-SV.                                            *
-- ***************************************************************************************************************************
SELECT F.* FROM EMISOR_V3 F;
SELECT F.* FROM EMISOR_KCOO_V3 F;
SELECT F.* FROM EMISOR_ESTABLECIMIENTO_V3 F;

SELECT F.* FROM NOTIFIACION_CORREO_V3 F;
SELECT F.* FROM IMPRESORAS F;
SELECT F.* FROM NOTIFICACIONES F;
SELECT F.* FROM NOTIFICACIONES_CONTIN F;
-- ***************************************************************************************************************************
-- *          COMPROBANTE DE CRÉDITO FISCAL.                                                                                 *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_CCF_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM IDENTIFICACION_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM DOCU_RELA_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM RECEPTOR_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM SHIPTO_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM OTROS_DOCU_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM VENTA_TERCERO_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM CUERPO_DOCU_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM CUERPO_TRIBUTO_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM RESUMEN_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM RESUMEN_TRIBUTO_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM EXTENSION_CCF_V3 F WHERE F.ID_DTE IN (417,418);
SELECT F.* FROM APENDICE_CCF_V3 F WHERE F.ID_DTE IN (417,418);
-- ***************************************************************************************************************************
-- *          NOTA DE CRÉDITO.                                                                                               *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_NC_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM IDENTIFICACION_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM DOCU_RELA_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM RECEPTOR_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM SHIPTO_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM VENTA_TERCERO_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM CUERPO_DOCU_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM CUERPO_TRIBUTO_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM RESUMEN_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM RESUMEN_TRIBUTO_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM EXTENSION_NC_V3 F WHERE F.ID_DTE IN (153);
SELECT F.* FROM APENDICE_NC_V3 F WHERE F.ID_DTE IN (153);
-- ***************************************************************************************************************************
-- *          NOTA DE DÉBITO.                                                                                                *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_ND_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM IDENTIFICACION_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM DOCU_RELA_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM RECEPTOR_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM SHIPTO_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM VENTA_TERCERO_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM CUERPO_DOCU_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM CUERPO_TRIBUTO_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM RESUMEN_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM RESUMEN_TRIBUTO_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM EXTENSION_ND_V3 F WHERE F.ID_DTE IN (100);
SELECT F.* FROM APENDICE_ND_V3 F WHERE F.ID_DTE IN (100);
-- ***************************************************************************************************************************
-- *          FACTURA ELECTRÓNICA.                                                                                           *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_F_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM IDENTIFICACION_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM DOCU_RELA_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM RECEPTOR_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM SHIPTO_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM OTROS_DOCU_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM VENTA_TERCERO_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM CUERPO_DOCU_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM CUERPO_TRIBUTO_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM RESUMEN_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM RESUMEN_TRIBUTO_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM EXTENSION_F_V3 F WHERE F.ID_DTE IN (230);
SELECT F.* FROM APENDICE_F_V3 F WHERE F.ID_DTE IN (230);
-- ***************************************************************************************************************************
-- *          FACTURA DE EXPORTACIÓN.                                                                                        *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_FEX_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM IDENTIFICACION_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM RECEPTOR_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM SHIPTO_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM OTROS_DOCU_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM VENTA_TERCERO_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM CUERPO_DOCU_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM CUERPO_TRIBUTO_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM RESUMEN_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM RESUMEN_TRIBUTO_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM EXTENSION_FEX_V3 F WHERE F.ID_DTE IN (173);
SELECT F.* FROM APENDICE_FEX_V3 F WHERE F.ID_DTE IN (173);
-- ***************************************************************************************************************************
-- *          NOTA DE REMISION.                                                                                              *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_NR_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM IDENTIFICACION_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM DOCU_RELA_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM RECEPTOR_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM SHIPTO_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM OTROS_DOCU_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM VENTA_TERCERO_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM CUERPO_DOCU_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM CUERPO_TRIBUTO_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM RESUMEN_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM RESUMEN_TRIBUTO_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM EXTENSION_NR_V3 F WHERE F.ID_DTE IN (150);
SELECT F.* FROM APENDICE_NR_V3 F WHERE F.ID_DTE IN (150);
-- ***************************************************************************************************************************
-- *          COMPROBANTE DE RETENCIÓN.                                                                                      *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_CR_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM IDENTIFICACION_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM DOCU_RELA_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM RECEPTOR_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM SHIPTO_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM OTROS_DOCU_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM VENTA_TERCERO_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM CUERPO_DOCU_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM CUERPO_TRIBUTO_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM RESUMEN_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM RESUMEN_TRIBUTO_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM EXTENSION_CR_V3 F WHERE F.ID_DTE IN (122);
SELECT F.* FROM APENDICE_CR_V3 F WHERE F.ID_DTE IN (122);
-- ***************************************************************************************************************************
-- *          FACTURA SUJETO EXCLUIDO.                                                                                       *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_FSE_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM IDENTIFICACION_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM DOCU_RELA_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM SUJETOEXCLUIDO_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM OTROS_DOCU_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM VENTA_TERCERO_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM CUERPO_DOCU_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM CUERPO_TRIBUTO_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM RESUMEN_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM RESUMEN_TRIBUTO_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM EXTENSION_FSE_V3 F WHERE F.ID_DTE IN (1);
SELECT F.* FROM APENDICE_FSE_V3 F WHERE F.ID_DTE IN (1);
-- ***************************************************************************************************************************
-- *          EVENTO DE INVALIDACIÓN.                                                                                        *
-- ***************************************************************************************************************************
SELECT F.* FROM DTE_INVALIDACION_V3 F ORDER BY F.ID_DTE DESC;

SELECT F.* FROM DTE_INVALIDACION_V3 F WHERE F.ID_DTE IN (12);
SELECT F.* FROM IDENTIFICACION_INVALIDACION_V3 F WHERE F.ID_DTE IN (12);
SELECT F.* FROM DOCUMENTO_INVALIDACION_V3 F WHERE F.ID_DTE IN (12);
SELECT F.* FROM MOTIVO_INVALIDACION_V3 F WHERE F.ID_DTE IN (12);
-- ***************************************************************************************************************************
-- *          EVENTO DE CONTINGENCIA.                                                                                        *
-- ***************************************************************************************************************************
SELECT F.* FROM EVENTO_CONTINGENCIA_V3 F ORDER BY F.ID_CONTINGENCIA DESC;

SELECT F.* FROM EVENTO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA IN (19);
SELECT F.* FROM IDENTIFICACION_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA IN (19);
SELECT F.* FROM DETALLE_DTE_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA IN (19);
SELECT F.* FROM MOTIVO_CONTINGENCIA_V3 F WHERE F.ID_CONTINGENCIA IN (19);
-- ***************************************************************************************************************************
-- *          TABLAS FEL-GUATEMALA.                                                                                          *
-- ***************************************************************************************************************************
SELECT F.* FROM CRPDTA.F59421CA@JDEPY F;
SELECT F.* FROM CRPDTA.F59421DE@JDEPY F; -- ESTA TABLA NO SE PUEDE CONSULTAR A TRAVES DE DBLINK.
SELECT F.* FROM CRPDTA.F5942PAR@JDEPY F;
-- ***************************************************************************************************************************
-- *          QUERY PARA FECHAS JULIANAS Y GREGORIANAS.                                                                      *
-- ***************************************************************************************************************************
SELECT TO_NUMBER(SUBSTR(TO_CHAR(TO_DATE('12/09/2023','dd/MM/yyyy'),'ccYYddd'),2,6)) FECHA_JULIANA FROM DUAL;
SELECT TO_CHAR(TO_DATE(TO_CHAR(A.SDIVD + 1900000,'9999999'),'YYYYDDD'),'dd/MM/yyyy') FECHA_GREGORIANA FROM DUAL;

SELECT DISTINCT F.NRKCOO, F.NRDOCO, F.NRDCTO, F.NRN001, F.NRURCD, F.NRMCU, F.NRAN8, F.NRSHAN, F.NRCRCD, F.NRURDT, '000' STCD, '-' CRSREF01, '-' CRSREF02, '-' CRSREF03, '-' CRSREF04, '-' CRSREF05, 'F554211N' TABLA, TRIM(F.NRTXA1) NRTXA1, NVL(TRIM(G.ABAC30),'-') ABAC30
FROM CRPDTA.F554211N@JDEPY F LEFT JOIN CRPDTA.F0101@JDEPY G ON (F.NRAN8=G.ABAN8)
WHERE (TRIM(F.NRKCOO) IN (SELECT C.KCOO_JDE FROM EMISOR_KCOO_V3 C)) AND (F.NRN001 > 0) AND (TRIM(F.NRDCTO) IN ('XF')) AND (TRIM(F.NREV01) IN ('N')) AND (F.NRURDT >= 123224);

SELECT F.NRRMK, F.*
FROM CRPDTA.F554211N@JDEPY F LEFT JOIN CRPDTA.F0101@JDEPY G ON (F.NRAN8=G.ABAN8)
WHERE (TRIM(F.NRKCOO) IN (SELECT C.KCOO_JDE FROM EMISOR_KCOO_V3 C)) AND (F.NRN001 > 0) AND (TRIM(F.NRDCTO) IN ('XF')) AND (TRIM(F.NREV01) IN ('N')) AND (F.NRURDT >= 123224);

SELECT
F.FEKCOO,
TRIM(F.FEMCU),
F.FEDOCO,
F.FEDCTO,
F.FEDOC,
F.FEDCT,
F.FECRSREF02
FROM
CRPDTA.F5542FEL@JDEPY F
LEFT JOIN EMISOR_KCOO_V3 G ON (TRIM(F.FEKCOO)=TRIM(G.KCOO_JDE))
WHERE
TRIM(F.FEKCOO) IN ('00728','00733') AND
TRIM(F.FEMCU) IN ('728016','728012','733501') AND 
TRIM(FESTCD)='CCC' AND
F.FEDCTO IN ('NR','S3','SD','FE','S3','C3','EX') AND
(TRIM(F.FECRSREF03)='-' OR F.FECRSREF03 IS NULL) AND
G.ID_EMISOR=1
ORDER BY 
F.FEKCOO, TRIM(F.FEMCU);

-- LOTE NO. 1
SELECT F.* FROM CRPDTA.F5542FEL@JDEPY F WHERE F.FEKCOO='00728' AND TRIM(F.FEMCU)='728012' AND F.FEDOCO IN (72960262,72960268,72960266,72960237,72960268);

SELECT F.* FROM IDENTIFICACION_CCF_V3 F WHERE F.CODIGOGENERACION IN ('5A5A13CD-5080-4FE7-9925-472097E6312E') UNION ALL
SELECT F.* FROM IDENTIFICACION_F_V3 F WHERE F.CODIGOGENERACION IN ('6CB9C6E3-175B-42E2-8B34-ABFBAE57FC34') UNION ALL
SELECT F.* FROM IDENTIFICACION_FEX_V3 F WHERE F.CODIGOGENERACION IN ('2AF9499C-99E0-4605-ABEA-0AAA90DB0D5D','B51691D5-D557-45EA-A171-2E54D3AF0677') UNION ALL
SELECT F.* FROM IDENTIFICACION_NR_V3 F WHERE F.CODIGOGENERACION IN ('699FE3F1-A716-4693-A497-C336B2F9A236');