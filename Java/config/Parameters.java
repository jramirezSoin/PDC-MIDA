package config;

import java.util.ResourceBundle;

/**
 *
 * @author Adrian Rocha
 */
public final class Parameters {
    //public static String connectionString;
    //public static String loginType;
    public static String LOG_PATH;
    public static int LOG_LEVEL;
    //public static String pcmTimeout;
    
    private static boolean parametersLoaded=false;

    public static String START_DATE_FROM = "20091201";
    public static String START_DATE_FROM_PRE = "0";
    //DB
    public static String DB_PIN_CLASSFORNAME="";
    public static String DB_PIN_USERNAME="";
    public static String DB_PIN_USERPASSWD="";
    public static String DB_PIN_URL="";
    
    public static String GLID_LOADER_PATH="";
    public static String GLID_LOADER_DONE_DIR="";
    public static String GLID_LOADER_COMMAND="";
    
    public static String PIN_DEPLOY_LOADER_PATH="";
    public static String PIN_DEPLOY_LOADER_DONE_DIR="";
    public static String PIN_DEPLOY_LOADER_COMMAND="";
    
    public static String DB_IFW_USERNAME="";
    public static String DB_IFW_USERPASSWD="";
    
    public static String PDC_LOADER_PATH="";
    public static String PDC_LOADER_DONE_DIR="";
    public static String PDC_LOADER_COMMAND="";
    public static String PDC_LOADER_PASSWORD="";
    
    public static String PDC_CONFIG_ZONNING="";
    public static String PDC_CONFIG_USC_SELECTOR="";
    public static String PDC_CONFIG_ITEM_SMS="";
    public static String PDC_CONFIG_ITEM_TEL="";
    public static String PDC_CONFIG_CHARGES_SMS="";
    public static String PDC_CONFIG_CHARGES_SMS_USD="";
    public static String PDC_CONFIG_CHARGES_GPRS_USD="";
    public static String PDC_CONFIG_CHARGES_TEL="";
    public static String PDC_CONFIG_CHARGES_TEL_USD="";
    
    //CONFIG PRE
    public static String PDC_CONFIG_ZONNING_SMS_PRE="";
    public static String PDC_CONFIG_ZONNING_TEL_PRE="";
    public static String PDC_CONFIG_CHARGES_TEL_PRE="";
    public static String PDC_CONFIG_CHARGES_SMS_PRE="";
    public static String XML_CONFIG_ZONNING_SMS_PRE="";
    public static String XML_CONFIG_ZONNING_TEL_PRE="";
    public static String XML_CONFIG_CHARGES_TEL_PRE="";
    public static String XML_CONFIG_CHARGES_SMS_PRE="";
    public static String XML_CONFIG_SELECTOR_SMS_PRE="";
    public static String XML_CONFIG_SELECTOR_TEL_PRE="";
    
    //CONFIG HIB
    public static String PDC_CONFIG_ZONNING_SMS_HYB="";
    public static String PDC_CONFIG_ZONNING_TEL_HYB="";
    public static String PDC_CONFIG_ZONNING_TEL_HYB_HORARIO="";
    public static String PDC_CONFIG_CHARGES_SMS_HYB="";
    public static String PDC_CONFIG_CHARGES_TEL_HYB="";
    public static String PDC_CONFIG_CHARGES_TEL_HYB_HORARIO="";
    public static String XML_CONFIG_ZONNING_SMS_HYB="";
    public static String XML_CONFIG_ZONNING_TEL_HYB="";
    public static String XML_CONFIG_ZONNING_TEL_HYB_HORARIO="";
    public static String XML_CONFIG_CHARGES_SMS_HYB="";
    public static String XML_CONFIG_CHARGES_TEL_HYB="";
    public static String XML_CONFIG_CHARGES_TEL_HYB_HORARIO="";
    public static String XML_CONFIG_SELECTOR_SMS_HYB="";
    public static String XML_CONFIG_SELECTOR_TEL_HYB="";

    //CONFIG MIDA
    public static String PDC_CONFIG_ZONNING_TEL_PRE_MIDA="";
    public static String PDC_CONFIG_ZONNING_TEL_CCF_MIDA="";
    public static String PDC_CONFIG_ZONNING_TEL_CCM_MIDA="";
    public static String PDC_CONFIG_ZONNING_TEL_HIB_MIDA="";
    public static String PDC_CONFIG_CHARGES_TEL_PRE_MIDA="";
    public static String PDC_CONFIG_CHARGES_TEL_CCF_MIDA="";
    public static String PDC_CONFIG_CHARGES_TEL_CCM_MIDA="";
    public static String PDC_CONFIG_CHARGES_TEL_HIB_MIDA="";
    public static String XML_CONFIG_ZONNING_TEL_PRE_MIDA="";
    public static String XML_CONFIG_ZONNING_TEL_CCF_MIDA="";
    public static String XML_CONFIG_ZONNING_TEL_CCM_MIDA="";
    public static String XML_CONFIG_ZONNING_TEL_HIB_MIDA="";
    public static String XML_CONFIG_CHARGES_TEL_PRE_MIDA="";
    public static String XML_CONFIG_CHARGES_TEL_CCF_MIDA="";
    public static String XML_CONFIG_CHARGES_TEL_CCM_MIDA="";
    public static String XML_CONFIG_CHARGES_TEL_HIB_MIDA="";
    
    
    public static final int ERR_INVALID_VALUE=1; 
    public static final int ERR_DATABASE=2;
    public static final int ERR_FILESYSTEM=3;
    public static final int ERR_IOERROR=4;
    public static final int ERR_INTEGRATE=5;
    public static final int ERR_USER_CONFIRMATION=8;
    public static final int ERR_UNKNOWN=9;

//    public static String ITEM_CONFIG_PATH="";
//    public static String ITEM_CONFIG_DONE_PATH="";

    public static String XML_DB_DIR="";
    public static String XML_ZONNING="";
    public static String XML_ZONE_RESULT_CONFIGURATION="";
    public static String XML_USC_SELECTOR="";
    public static String XML_ITEM_SELECTORS_SMS="";
    public static String XML_ITEM_SELECTORS_TEL="";
    public static String XML_CHARGES_SMS="";
    public static String XML_CHARGES_SMS_USD="";
    public static String XML_CHARGES_GPRS_USD="";
    public static String XML_CHARGES_TEL="";
    public static String XML_CHARGES_TEL_USD="";
    public static String XML_HISTORY="";
    
    public static Double DEFAULT_PRICE_MINUTE = 0.0;
    public static Double DEFAULT_PRICE_SMS = 0.0;
    
    public static String XML_TAG_FLAG="iceUpdater";

    public static void loadParameters(){
        ResourceBundle brmProp;
        try {
            brmProp = ResourceBundle.getBundle("Infranet");
            addDatabaseConfig(brmProp);
            addLogConfig(brmProp);
            addGlidConfig(brmProp);
            addHandlerConfig(brmProp);
            addXmlConfig(brmProp);
        } catch (ExceptionInInitializerError e) {
            System.out.println("No se pudo leer Infranet.properties!!!");
            System.out.println("Saliendo");
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("Error al cargar par\u00E1metros de Infranet.properties!!!");
            System.out.println("Saliendo");
            System.exit(1);
        }

        parametersLoaded=true;
    }

    private static void addDatabaseConfig(ResourceBundle confFile){
        DB_PIN_CLASSFORNAME=confFile.getString("db.pin.classforname").trim();
        DB_PIN_USERNAME=confFile.getString("db.pin.userName").trim();
        DB_PIN_USERPASSWD=confFile.getString("db.pin.userPasswd").trim();
        DB_PIN_URL=confFile.getString("db.pin.url").trim();
        
        DB_IFW_USERNAME=confFile.getString("db.ifw.userName").trim();
        DB_IFW_USERPASSWD=confFile.getString("db.ifw.userPasswd").trim();
    }

    private static void addLogConfig(ResourceBundle confFile){
        LOG_LEVEL=Integer.parseInt(confFile.getString("infranet.log.level").trim());
        LOG_PATH=confFile.getString("infranet.log.path").trim();
    }

    private static void addGlidConfig(ResourceBundle confFile){
        GLID_LOADER_PATH=confFile.getString("glid.loader.path").trim();
        if (!GLID_LOADER_PATH.endsWith("/")) {
            GLID_LOADER_PATH = GLID_LOADER_PATH + "/";
        }
        GLID_LOADER_DONE_DIR=confFile.getString("glid.loader.done").trim();
        if (!GLID_LOADER_DONE_DIR.endsWith("/")) {
            GLID_LOADER_DONE_DIR = GLID_LOADER_DONE_DIR + "/";
        }
        GLID_LOADER_COMMAND=confFile.getString("glid.loader.command").trim();
    }

    private static void addHandlerConfig(ResourceBundle confFile){
        PDC_LOADER_PATH=confFile.getString("pdc.loader.path").trim();
        if (!PDC_LOADER_PATH.endsWith("/")) {
            PDC_LOADER_PATH = PDC_LOADER_PATH + "/";
        }
        PDC_LOADER_DONE_DIR=confFile.getString("pdc.loader.done").trim();
        if (!PDC_LOADER_DONE_DIR.endsWith("/")) {
            PDC_LOADER_DONE_DIR = PDC_LOADER_DONE_DIR + "/";
        }
        PIN_DEPLOY_LOADER_PATH=confFile.getString("pin_deploy.loader.path").trim();
        if (!PIN_DEPLOY_LOADER_PATH.endsWith("/")) {
        	PIN_DEPLOY_LOADER_PATH = PIN_DEPLOY_LOADER_PATH + "/";
        }
        PIN_DEPLOY_LOADER_DONE_DIR=confFile.getString("pin_deploy.loader.done").trim();
        if (!PIN_DEPLOY_LOADER_DONE_DIR.endsWith("/")) {
        	PIN_DEPLOY_LOADER_DONE_DIR = PIN_DEPLOY_LOADER_DONE_DIR + "/";
        }
        PIN_DEPLOY_LOADER_COMMAND=confFile.getString("pin_deploy.loader.command").trim();
        
        PDC_LOADER_COMMAND=confFile.getString("pdc.loader.command").trim();
        PDC_LOADER_PASSWORD=confFile.getString("pdc.loader.password").trim();
        
        PDC_CONFIG_CHARGES_SMS=confFile.getString("pdc.config.charges.sms").trim();
        PDC_CONFIG_CHARGES_SMS_USD=confFile.getString("pdc.config.charges.smsusd").trim();
        PDC_CONFIG_CHARGES_GPRS_USD=confFile.getString("pdc.config.charges.gprsusd").trim();
        PDC_CONFIG_CHARGES_TEL=confFile.getString("pdc.config.charges.tel").trim();
        PDC_CONFIG_CHARGES_TEL_USD=confFile.getString("pdc.config.charges.telusd").trim();
        PDC_CONFIG_ITEM_SMS=confFile.getString("pdc.config.items.sms").trim();
        PDC_CONFIG_ITEM_TEL=confFile.getString("pdc.config.items.tel").trim();
        PDC_CONFIG_ZONNING=confFile.getString("pdc.config.zonning").trim();
        PDC_CONFIG_USC_SELECTOR=confFile.getString("pdc.config.uscselector").trim();
        
        PDC_CONFIG_ZONNING_SMS_PRE=confFile.getString("pdc.config.zonning.sms.pre").trim();
        PDC_CONFIG_ZONNING_TEL_PRE=confFile.getString("pdc.config.zonning.tel.pre").trim();
        PDC_CONFIG_CHARGES_TEL_PRE=confFile.getString("pdc.config.charges.tel.pre").trim();
        PDC_CONFIG_CHARGES_SMS_PRE=confFile.getString("pdc.config.charges.sms.pre").trim();
        
        PDC_CONFIG_ZONNING_SMS_HYB=confFile.getString("pdc.config.zonning.sms.hib").trim();
        PDC_CONFIG_ZONNING_TEL_HYB=confFile.getString("pdc.config.zonning.tel.hib").trim();
        PDC_CONFIG_ZONNING_TEL_HYB_HORARIO=confFile.getString("pdc.config.zonning.tel.hib.horario").trim();
        PDC_CONFIG_CHARGES_SMS_HYB=confFile.getString("pdc.config.charges.sms.hib").trim();
        PDC_CONFIG_CHARGES_TEL_HYB=confFile.getString("pdc.config.charges.tel.hib").trim();
        PDC_CONFIG_CHARGES_TEL_HYB_HORARIO=confFile.getString("pdc.config.charges.tel.hib.horario").trim();
        
        PDC_CONFIG_ZONNING_TEL_PRE_MIDA=confFile.getString("pdc.config.zonning.tel.pre.mida").trim();
        PDC_CONFIG_ZONNING_TEL_CCF_MIDA=confFile.getString("pdc.config.zonning.tel.ccf.mida").trim();
        PDC_CONFIG_ZONNING_TEL_CCM_MIDA=confFile.getString("pdc.config.zonning.tel.ccm.mida").trim();
        PDC_CONFIG_ZONNING_TEL_HIB_MIDA=confFile.getString("pdc.config.zonning.tel.hib.mida").trim();
        PDC_CONFIG_CHARGES_TEL_PRE_MIDA=confFile.getString("pdc.config.charges.tel.pre.mida").trim();
        PDC_CONFIG_CHARGES_TEL_CCF_MIDA=confFile.getString("pdc.config.charges.tel.ccf.mida").trim();
        PDC_CONFIG_CHARGES_TEL_CCM_MIDA=confFile.getString("pdc.config.charges.tel.ccm.mida").trim();
        PDC_CONFIG_CHARGES_TEL_HIB_MIDA=confFile.getString("pdc.config.charges.tel.hib.mida").trim();

        DEFAULT_PRICE_MINUTE = Double.parseDouble(confFile.getString("default.price.minute.pre").trim());
        DEFAULT_PRICE_SMS = Double.parseDouble(confFile.getString("default.price.sms.pre").trim());
    }

    private static void addXmlConfig(ResourceBundle confFile){
        XML_DB_DIR=confFile.getString("xml.dbdir").trim();
        if (!XML_DB_DIR.endsWith("/")) {
        	XML_DB_DIR = XML_DB_DIR + "/";
        }
        XML_ZONNING=confFile.getString("xml.zonning").trim();
        XML_ZONE_RESULT_CONFIGURATION=confFile.getString("xml.zoneresult").trim();
        XML_USC_SELECTOR=confFile.getString("xml.usc_selector").trim();
        XML_CHARGES_SMS=confFile.getString("xml.charge_rate_plan_sms").trim();
        XML_CHARGES_SMS_USD=confFile.getString("xml.charge_rate_plan_sms_usd").trim();
        XML_CHARGES_GPRS_USD=confFile.getString("xml.charge_rate_plan_gprs_usd").trim();
        XML_CHARGES_TEL=confFile.getString("xml.charge_rate_plan_tel").trim();
        XML_CHARGES_TEL_USD=confFile.getString("xml.charge_rate_plan_tel_usd").trim();
        XML_ITEM_SELECTORS_SMS=confFile.getString("xml.item_selectors_sms").trim();
        XML_ITEM_SELECTORS_TEL=confFile.getString("xml.item_selectors_tel").trim();
        XML_HISTORY=confFile.getString("xml.history").trim();
        
        XML_CONFIG_ZONNING_SMS_PRE=confFile.getString("xml.config.zonning.sms.pre").trim();
        XML_CONFIG_ZONNING_TEL_PRE=confFile.getString("xml.config.zonning.tel.pre").trim();
        XML_CONFIG_CHARGES_SMS_PRE=confFile.getString("xml.config.charges.sms.pre").trim();
        XML_CONFIG_CHARGES_TEL_PRE=confFile.getString("xml.config.charges.tel.pre").trim();
        XML_CONFIG_SELECTOR_SMS_PRE=confFile.getString("xml.config.selector.sms.pre").trim();
        XML_CONFIG_SELECTOR_TEL_PRE=confFile.getString("xml.config.selector.tel.pre").trim();

        XML_CONFIG_ZONNING_SMS_HYB=confFile.getString("xml.config.zonning.sms.hib").trim();
        XML_CONFIG_ZONNING_TEL_HYB=confFile.getString("xml.config.zonning.tel.hib").trim();
        XML_CONFIG_ZONNING_TEL_HYB_HORARIO=confFile.getString("xml.config.zonning.tel.hib.horario").trim();
        XML_CONFIG_CHARGES_SMS_HYB=confFile.getString("xml.config.charges.sms.hib").trim();
        XML_CONFIG_CHARGES_TEL_HYB=confFile.getString("xml.config.charges.tel.hib").trim();
        XML_CONFIG_CHARGES_TEL_HYB_HORARIO=confFile.getString("xml.config.charges.tel.hib.horario").trim();
        XML_CONFIG_SELECTOR_SMS_HYB=confFile.getString("xml.config.selector.sms.hib").trim();
        XML_CONFIG_SELECTOR_TEL_HYB=confFile.getString("xml.config.selector.tel.hib").trim();

        XML_CONFIG_ZONNING_TEL_PRE_MIDA=confFile.getString("xml.config.zonning.tel.pre.mida").trim();
        XML_CONFIG_ZONNING_TEL_CCF_MIDA=confFile.getString("xml.config.zonning.tel.ccf.mida").trim();
        XML_CONFIG_ZONNING_TEL_CCM_MIDA=confFile.getString("xml.config.zonning.tel.ccm.mida").trim();
        XML_CONFIG_ZONNING_TEL_HIB_MIDA=confFile.getString("xml.config.zonning.tel.hib.mida").trim();
        XML_CONFIG_CHARGES_TEL_PRE_MIDA=confFile.getString("xml.config.charges.tel.pre.mida").trim();
        XML_CONFIG_CHARGES_TEL_CCF_MIDA=confFile.getString("xml.config.charges.tel.ccf.mida").trim();
        XML_CONFIG_CHARGES_TEL_CCM_MIDA=confFile.getString("xml.config.charges.tel.ccm.mida").trim();
        XML_CONFIG_CHARGES_TEL_HIB_MIDA=confFile.getString("xml.config.charges.tel.hib.mida").trim();
    }
    
    public static boolean isParametersLoaded() {
        return parametersLoaded;
    }
    
}
