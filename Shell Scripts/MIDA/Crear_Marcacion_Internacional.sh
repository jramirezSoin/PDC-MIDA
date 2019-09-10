#!/bin/ksh
cd $PDC_HOME"/ice_custom/apps/mida"



function Creacion_xml #PARAM_DESTINO PARAM_NOMRE PARAM_DESCRIPCION PARAM_ZONA
{

#verificar que la marcacion no exista.
Verifica="select 'Marcacion: '||count(1) from IFW_STANDARD_ZONE where DESTIN_AREACODE ='00$1' and SERVICECODE='TEL' and ZONE_RT like '%MI%';"
echo "$Verifica" >slct.sql
sqlplus $IFW_DB_USER/$IFW_DB_PASS@$IFW_DB_NAME < slct.sql > VerificaMarcacion.act
Resultado=$( cat VerificaMarcacion.act | grep -v '||' | grep 'Marcacion:' | awk -F":" '{print $2}' | tr -d " ")
rm slct.sql
rm VerificaMarcacion.act


if [ $Resultado -eq 1 ]
then
 echo La Marcacion destino $1 que intenta configurar ya existe para el servicio TEL, proceso cancelado
 exit 1
fi

echo '<?xml version="1.0" encoding="UTF-8"?>
<IFW xsi:noNamespaceSchemaLocation="LoadIfwConfig.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<RecordSet>' > XML_Marcacion_$1.xml
echo ' <IFW_DESTINDESC AREACODE ="00'$1'" NAME ="'$2'" TYPE ="I" />' >> XML_Marcacion_$1.xml
echo '</RecordSet>
<RecordSet>' >> XML_Marcacion_$1.xml
echo ' <IFW_STANDARD_ZONE DESCRIPTION ="'$3'" DESTIN_AREACODE ="00'$1'" NAME ="'$2'" ORIGIN_AREACODE ="00" SERVICECODE ="TEL" VALID_FROM ="1980-01-01T00:00:00" ZONE_RT ="'$4'" ZONE_WS ="'$4'" ZONEMODEL ="ZM_COMMON" />' >> XML_Marcacion_$1.xml
echo ' <IFW_STANDARD_ZONE DESCRIPTION ="'$3'" DESTIN_AREACODE ="00'$1'" NAME ="'$2'" ORIGIN_AREACODE ="00" SERVICECODE ="TELDE" VALID_FROM ="1980-01-01T00:00:00" ZONE_RT ="'$4'" ZONE_WS ="'$4'" ZONEMODEL ="ZM_COMMON" />' >> XML_Marcacion_$1.xml

echo '</RecordSet>' >> XML_Marcacion_$1.xml
echo '</IFW>' >> XML_Marcacion_$1.xml

cp XML_Marcacion_$1.xml $IFW_HOME/tools/XmlLoader
cd $IFW_HOME/tools/XmlLoader
echo Realizando respaldo del IFW
LoadIfwConfig -rall -o BACKUP_IFW_BRM_Antes_Marcacion_$1.xml

#echo aca se ejecuta el load
#ls -lrt | grep XML_Marcacion

pwd
LoadIfwConfig -c -u -i XML_Marcacion_$1.xml
if [[ $? -ne 0 ]]
then
  echo "Error al ejecutar IFW Loader, abortando..."
  exit 1
fi

rm XML_Marcacion_$1.xml

}


#Chequea parametros de entrada
if [[ $# -ne 4 ]]
then
 echo "El numero de parametros es incorrecto"
 exit 1
fi

echo "Inicio Consulta_Destino_Internacional.sh: "`date +%Y/%m/%d_%H:%M:%S` >> bitacora.txt
echo "Parmetros: $*" >> bitacora.txt

#Parameros: PARAM_DESTINO, PARAM_NOMRE, PARAM_DESCRIPCION, PARAM_ZONA
PARAM_DESTINO=$1
PARAM_NOMRE=$2
PARAM_DESCRIPCION=$3
PARAM_ZONA=$4

case $PARAM_ZONA in
	"Grupo_A") PARAM_ZONA='MIDAGRPA';;
	"Grupo_B") PARAM_ZONA='MIDAGRPB';;
	"Grupo_C1") PARAM_ZONA='MIDAGRC1';;
	"Grupo_C2") PARAM_ZONA='MIDAGRC2';;
	"Grupo_D") PARAM_ZONA='MIDAGRPD';;
	"Grupo_E") PARAM_ZONA='MIDAGRPE';;
	"Grupo_F") PARAM_ZONA='MIDAGRPF';;
    "Grupo_G") PARAM_ZONA='MIDAGRPG';;
	"$PARAM_ZONA")  exit 1;;
esac
echo La zona es: $PARAM_ZONA

Creacion_xml "$PARAM_DESTINO" "$PARAM_NOMRE" "$PARAM_DESCRIPCION" "$PARAM_ZONA"

dirBase=$PDC_HOME"/ice_custom/apps/mida"
cmdClassPath="$PIN_HOME/jars/pcm.jar:$PIN_HOME/jars/pcmext.jar:$PDC_HOME/ice_custom/jars/commons-io-2.4.jar:$PDC_HOME/oui/ext/ojdbc6.jar:$PDC_HOME/ice_custom/jars/jdom-2.0.6.jar:$PDC_HOME/ice_custom/apps/proveedores/markmanager.jar:$PDC_HOME/ice_custom/apps/mida/GruposInternacionales.jar:$PDC_HOME/ice_custom/apps/mida"
cmdExec="groupUpdater.AddDestin"
cmd="$JAVA_8_HOME/bin/java -cp "$cmdClassPath" "$cmdExec

cd $dirBase

$cmd "$1" "$2" "$3" "$PARAM_ZONA"

if [[ $? -ne 0 ]]
then
        echo "Error al creacion la marcacion"
fi

echo 'Creacion de Marcacion Destino Internacional finalizada'
