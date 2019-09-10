[logging]
infranet.log.level=3
infranet.log.path=$PDC_HOME/ice_custom/apps/proveedores/proveedores.log
infranet.PcmTimeoutInMsecs=500000
infranet.custom.field.package=customfields

#DATABASE CONFIGURATION
db.pin.classforname=oracle.jdbc.driver.OracleDriver
db.pin.userName=$PIN_DB_USER
db.pin.userPasswd=$PIN_DB_PASS
db.pin.url=jdbc:oracle:thin:@$PIN_DB_HOSTNAME:$PIN_DB_PORT:$PIN_DB_NAME

db.ifw.userName=$IFW_DB_USER
db.ifw.userPasswd=$IFW_DB_PASS

[pdc_loader]
pdc.loader.password=$PDCUSER_PASS
pdc.loader.command=./ImportExportPricing
pdc.loader.path=$PDC_HOME/apps/bin
pdc.loader.done=$PDC_HOME/apps/bin/proveedores

[glid_loader]
glid.loader.command=load_pin_glid
glid.loader.path=$PIN_HOME/sys/test
glid.loader.done=$PIN_HOME/sys/test/proveedores

default.price.minute.pre=40.78
default.price.sms.pre=3.05

[pin_deploy]
pin_deploy.loader.command=pin_deploy
pin_deploy.loader.path=$PIN_HOME/sys/test
pin_deploy.loader.done=$PIN_HOME/sys/test/proveedores

[base_pdc_xml]
xml.dbdir=$PDC_HOME/ice_custom/apps/proveedores/xml
xml.item_selectors_sms=item_selectors_sms_config.xml
xml.charge_rate_plan_sms_usd=charges_sms_usd_pricing.xml
xml.charge_rate_plan_gprs_usd=charges_gprs_usd_pricing.xml
xml.item_selectors_tel=item_selectors_tel_config.xml
xml.zonning=zonning_config.xml
xml.zoneresult=zone_result_configuration.xml
xml.charge_rate_plan_sms=charge_rate_plan_sms_pricing.xml
xml.charge_rate_plan_tel=charge_rate_plan_tel_pricing.xml
xml.charge_rate_plan_tel_usd=charges_tel_usd_pricing.xml
xml.history=history.xml
xml.usc_selector=usc_selector.xml

#PDC Object Names
pdc.config.charges.sms=COMMON_SMS
pdc.config.charges.smsusd=COMMON_SMS_USD
pdc.config.charges.gprsusd=COMMON_GPRS_USD
pdc.config.charges.tel=COMMON
pdc.config.charges.telusd=COMMON_USD
pdc.config.items.sms=ItemTypeSelector_Para_Eventos_SMS
pdc.config.items.tel=ItemTypeSelector_Para_Eventos_TEL
pdc.config.zonning=ZM_COMMON
pdc.config.uscselector=ALL_RATE

#Config PRE
pdc.config.zonning.sms.pre=PRE_ZM_SMS_SERVICIOS_ESPECIALES_VA
pdc.config.zonning.tel.pre=PRE_ZM_VOZ_SERVICIOS_ESPECIALES_VA
pdc.config.charges.sms.pre=PRE_CRP_SMS_PLAN_BASICO_PREPAGO_NUMEROS_ESPECIALES
pdc.config.charges.tel.pre=PRE_CRP_VOZ_PLAN_BASICO_PREPAGO_NUMEROS_ESPECIALES
xml.config.zonning.sms.pre=zoning_sms_pre_config.xml
xml.config.zonning.tel.pre=zoning_tel_pre_config.xml
xml.config.charges.sms.pre=charge_rate_plan_sms_pre_pricing.xml
xml.config.charges.tel.pre=charge_rate_plan_tel_pre_pricing.xml
xml.config.selector.sms.pre=ItemTypeSelector_Para_Eventos_SMS_PREPAGO_config.xml
xml.config.selector.tel.pre=ItemTypeSelector_Para_Eventos_TEL_PREPAGO_config.xml
xml.config.selector.sms.pre=ItemTypeSelector_Para_Eventos_SMS_PREPAGO_config.xml
xml.config.selector.tel.pre=ItemTypeSelector_Para_Eventos_TEL_PREPAGO_config.xml

#Config HIB
pdc.config.zonning.sms.hib=HIB_ZM_SMS_SERVICIOS_ESPECIALES_VA
pdc.config.zonning.tel.hib=HIB_ZM_VOZ_SERVICIOS_ESPECIALES_VA
pdc.config.charges.sms.hib=HIB_CRP_SMS_PLAN_BASICO_NUMEROS_ESPECIALES
pdc.config.charges.tel.hib=HIB_CRP_VOZ_NUMEROS_ESPECIALES_TELEFONIA
xml.config.zonning.sms.hib=zoning_sms_hib_config.xml
xml.config.zonning.tel.hib=zoning_tel_hib_config.xml
xml.config.charges.sms.hib=charge_rate_plan_sms_hib_pricing.xml
xml.config.charges.tel.hib=charge_rate_plan_tel_hib_pricing.xml
xml.config.charges.tel.hib.horario=charge_rate_plan_tel_hib_horario_pricing.xml
xml.config.selector.sms.hib=ItemTypeSelector_Para_Eventos_SMS_PREPAGO_config.xml
xml.config.selector.tel.hib=ItemTypeSelector_Para_Eventos_TEL_PREPAGO_config.xml

#Config Horario
pdc.config.zonning.tel.hib.horario=HIB_ZM_VOZ_SERVICIOS_ESPECIALES_VA
pdc.config.charges.tel.hib.horario=HIB_CRP_VOZ_NUMEROS_ESPECIALES_TELEFONIA_HIBRIDO_HORARIO
xml.config.zonning.tel.hib.horario=zoning_tel_hib_config.xml
xml.config.charges.tel.hib.horario=charge_rate_plan_tel_hib_horario_pricing.xml

#Config PRE MIDA
pdc.config.zonning.tel.pre.mida=PRE_ZM_VOZ_MIDA
pdc.config.charges.tel.pre.mida=PRE_CRP_VOZ_PLAN_BASICO_PREPAGO_ROAMING_MIDA_USD
xml.config.zonning.tel.pre.mida=zoning_tel_pre_mida_config.xml
xml.config.charges.tel.pre.mida=charge_rate_plan_tel_pre_mida_pricing.xml

#Config CCF
pdc.config.zonning.tel.ccf.mida=PRE_ZM_VOZ_CALLING_CARD_FIJO
pdc.config.charges.tel.ccf.mida=PRE_CRP_VOZ_PLAN_BASICO_PREPAGO_CC_FIJO_MIDA_USD
xml.config.zonning.tel.ccf.mida=zoning_tel_ccf_mida_config.xml
xml.config.charges.tel.ccf.mida=charge_rate_plan_tel_ccf_mida_pricing.xml

#Config CCM 
pdc.config.zonning.tel.ccm.mida=PRE_ZM_VOZ_CALLING_CARD_MOVIL
pdc.config.charges.tel.ccm.mida=PRE_CRP_VOZ_PLAN_BASICO_PREPAGO_CC_MOVIL_MIDA_USD
xml.config.zonning.tel.ccm.mida=zoning_tel_ccm_mida_config.xml
xml.config.charges.tel.ccm.mida=charge_rate_plan_tel_ccm_mida_pricing.xml

#Config HIB MIDA
pdc.config.zonning.tel.hib.mida=HIB_ZM_VOZ_MIDA
pdc.config.charges.tel.hib.mida=HIB_CRP_VOZ_PLAN_BASICO_PREPAGO_USD_VOZ
xml.config.zonning.tel.hib.mida=zoning_tel_hib_mida_config.xml
xml.config.charges.tel.hib.mida=charge_rate_plan_tel_hib_mida_pricing.xml





