#!/bin/ksh
cd $PDC_HOME"/ice_custom/apps/mida"

function Actualiza_marcacion #PARAM_NOMRE PARAM_DESTINO PARAM_ZONA
{
 #Verificar que el destino a modificar exista en BRM
 LvarVerifica="select 'Marcacion: '||count(1) from IFW_STANDARD_ZONE where NAME = '$1' AND DESTIN_AREACODE = '00$2' and SERVICECODE = 'TEL';"
 echo "$LvarVerifica" > slct_Cambio_marcacion.sql
 sqlplus $IFW_DB_USER/$IFW_DB_PASS@$IFW_DB_NAME < slct_Cambio_marcacion.sql > Verifica_CambioMarcacion.act
 Resultado=$( cat Verifica_CambioMarcacion.act | grep -v '||' | grep 'Marcacion:' | awk -F":" '{print $2}' | tr -d " ")
 rm slct_Cambio_marcacion.sql
 rm Verifica_CambioMarcacion.act

 if [ $Resultado -eq 0 ]
 then
  echo La Marcacion destino $2, Nombre $1 que intenta configurar no existe para el servicio TEL, proceso cancelado
  exit 1
 fi

 #Actualizacion
 echo "Inicio Actualizacion"
 > UpdateDestino_$2.sql
 echo "UPDATE ifw_standard_zone SET ZONE_WS = '$3', ZONE_RT = '$3' WHERE NAME = '$1' AND DESTIN_AREACODE = '00$2' and SERVICECODE in ( 'TEL', 'TELDE' ) " >> UpdateDestino_$2.sql
 echo " / " >> UpdateDestino_$2.sql
 echo "commit;" >> UpdateDestino_$2.sql
 sqlplus $IFW_DB_USER/$IFW_DB_PASS@$IFW_DB_NAME < UpdateDestino_$2.sql > UpdateDestino_$2.act
 if [[ $? -ne 0 ]]
 then
   echo "Falla en la actualizacion de la BD IFW. Saliendo"
   exit 1
 fi
}


#MAIN
#Chequea parametros de entrada
if [[ $# -ne 3 ]]
 then
  echo "El numero de parametros es incorrecto, se esperan tres parametros: nombre, codigo destino y zona"
  exit 1
fi

#Parameros: PARAM_NOMRE, PARAM_DESTINO, PARAM_ZONA
PARAM_NOMRE=$1
PARAM_DESTINO=$2
PARAM_ZONA=$3


case $PARAM_ZONA in
	"Grupo_A") PARAM_ZONA='MIDAGRPA';;
	"Grupo_B") PARAM_ZONA='MIDAGRPB';;
	"Grupo_C1") PARAM_ZONA='MIDAGRC1';;
	"Grupo_C2") PARAM_ZONA='MIDAGRC2';;
	"Grupo_D") PARAM_ZONA='MIDAGRPD';;
	"Grupo_E") PARAM_ZONA='MIDAGRPE';;
	"Grupo_F") PARAM_ZONA='MIDAGRPF';;
    "Grupo_G") PARAM_ZONA='MIDAGRPG';;
	"$PARAM_ZONA")  echo "Destino invalido"; exit 1;;
esac
echo La zona es: $PARAM_ZONA

Actualiza_marcacion "$PARAM_NOMRE" "$PARAM_DESTINO" "$PARAM_ZONA"

dirBase=$PDC_HOME"/ice_custom/apps/mida"
cmdClassPath="$PIN_HOME/jars/pcm.jar:$PIN_HOME/jars/pcmext.jar:$PDC_HOME/ice_custom/jars/commons-io-2.4.jar:$PDC_HOME/oui/ext/ojdbc6.jar:$PDC_HOME/ice_custom/jars/jdom-2.0.6.jar:$PDC_HOME/ice_custom/apps/proveedores/markmanager.jar:$PDC_HOME/ice_custom/apps/mida/GruposInternacionales.jar:$PDC_HOME/ice_custom/apps/mida"
cmdExec="groupUpdater.ModifyDestin"
cmd="$JAVA_8_HOME/bin/java -cp "$cmdClassPath" "$cmdExec

cd $dirBase

$cmd "$1" "$2" "$PARAM_ZONA"

if [[ $? -ne 0 ]]
then
	echo "Error al actualizar la marcacion"
fi

echo Fin del cambio de Grupo para el destino $2
