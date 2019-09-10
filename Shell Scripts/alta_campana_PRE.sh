#!/bin/ksh

lockfile="alta_campana.lock"
if [ -f $lockfile ]; then
	echo "Existe otro proceso en ejecucion"
	echo "Esperando a que finalice..."
	while [ -f $lockfile ]
	do
		sleep 2
	done
fi

echo "Inicio alta_campana_PRE.sh: "`date +%Y/%m/%d_%H:%M:%S` - $* >> $lockfile
echo "****** Inicio alta_campana_PRE.sh: "`date +%Y/%m/%d_%H:%M:%S`" ******"
echo "Inicio alta_campana_PRE.sh: "`date +%Y/%m/%d_%H:%M:%S` >> bitacora.txt
echo "Parmetros: $*" >> bitacora.txt
dirBase=$PDC_HOME"/ice_custom/apps/proveedores"
cmdClassPath="$PIN_HOME/jars/pcm.jar:$PIN_HOME/jars/pcmext.jar:$PDC_HOME/ice_custom/jars/commons-io-2.4.jar:$PDC_HOME/oui/ext/ojdbc6.jar:$PDC_HOME/ice_custom/jars/jdom-2.0.6.jar:$PDC_HOME/ice_custom/apps/proveedores/markmanager.jar:$PDC_HOME/ice_custom/apps/proveedores"
cmdExec="markmanager.AddMark"
cmd="$JAVA_8_HOME/bin/java -cp "$cmdClassPath" "$cmdExec

cd $dirBase
if [[ $# -lt 11 ]]; then
	echo "Parametros insuficientes"
	echo "Modo de uso: ./alta_campana_PRE.sh <Modalidad> <Tipo de campana> <Tipo de tasacion> <ID campana> <Nombre en la factura> <Marcacion real> <Fecha inicio> <Fecha final> <GLID> <Precio> <Tax Code>"
	echo "Donde:"
	echo "	Modalidad: PRE/HIB"
	echo "	Tipo de campana: SMS/900"
	echo "	Tipo de tasacion: MO/MT (para SMS) o bien DUR/EVT (para 900)"
	echo "	Las fechas deben indicarse en formato: yyyyMMdd"
	rm $lockfile
	exit 1
fi

$cmd $1 $2 $3 $4 "$5" $6 $7 $8 $9 ${10} ${11}
	if [[ $? -ne 0 ]]; then
		echo "Codigo de retorno: ERROR" >> bitacora.txt
		echo "Codigo de retorno: ERROR"
	else
		echo "Codigo de retorno: OK" >> bitacora.txt
		echo "Codigo de retorno: OK"
	fi

rm $lockfile
