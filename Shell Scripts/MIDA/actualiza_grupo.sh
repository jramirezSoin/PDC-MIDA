#!/bin/ksh

echo "****** Inicio actualiza_grupo.sh: "`date +%Y/%m/%d_%H:%M:%S`" ******"
echo "Inicio actualiza_grupo.sh: "`date +%Y/%m/%d_%H:%M:%S` >> bitacora.txt
echo "Parmetros: $*" >> bitacora.txt
dirBase=$PDC_HOME"/ice_custom/apps/mida"
cmdClassPath="$PIN_HOME/jars/pcm.jar:$PIN_HOME/jars/pcmext.jar:$PDC_HOME/ice_custom/jars/commons-io-2.4.jar:$PDC_HOME/oui/ext/ojdbc6.jar:$PDC_HOME/ice_custom/jars/jdom-2.0.6.jar:$PDC_HOME/ice_custom/apps/proveedores/markmanager.jar:$PDC_HOME/ice_custom/apps/mida/GruposInternacionales.jar:$PDC_HOME/ice_custom/apps/mida"
cmdExec="groupUpdater.UpdateGroup"
cmd="$JAVA_8_HOME/bin/java -cp "$cmdClassPath" "$cmdExec

cd $dirBase
if [[ $# -lt 1 ]]; then
	echo "Parametros insuficientes"
	echo "Modo de uso: ./actualiza_grupo.sh <ID Grupo Internacional> <Fecha Tarifa YYYYMMDD> <Tarifa 1> <Tarifa 2> <Tarifa 3>"
	exit 1
fi

$cmd $1 $2 $3 $4 $5 $6
	if [[ $? -ne 0 ]]; then
		echo "Codigo de retorno: ERROR" >> bitacora.txt
		echo "Codigo de retorno: ERROR"
	else
		echo "Codigo de retorno: OK" >> bitacora.txt
		echo "Codigo de retorno: OK"
	fi
