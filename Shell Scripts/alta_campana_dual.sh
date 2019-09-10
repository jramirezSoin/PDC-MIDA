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

echo "Inicio alta_campana_dual.sh: "`date +%Y/%m/%d_%H:%M:%S` - $* >> $lockfile
echo "****** Inicio alta_campana_dual.sh: "`date +%Y/%m/%d_%H:%M:%S`" ******"
echo "Inicio alta_campana_dual.sh: "`date +%Y/%m/%d_%H:%M:%S` >> bitacora.txt
echo "Parmetros: $*" >> bitacora.txt
dirBase=$PDC_HOME"/ice_custom/apps/proveedores"
cmdClassPath="$PIN_HOME/jars/pcm.jar:$PIN_HOME/jars/pcmext.jar:$PDC_HOME/ice_custom/jars/commons-io-2.4.jar:$PDC_HOME/oui/ext/ojdbc6.jar:$PDC_HOME/ice_custom/jars/jdom-2.0.6.jar:$PDC_HOME/ice_custom/apps/proveedores/markmanager.jar:$PDC_HOME/ice_custom/apps/proveedores"
cmdExec="markmanager.AddMarkDual"
cmd="$JAVA_8_HOME/bin/java -cp "$cmdClassPath" "$cmdExec

cd $dirBase
if [[ $# -lt 10 ]]; then
	echo "Parametros insuficientes"
	echo "Modo de uso: ./alta_campana_dual.sh <ID campana> <Nombre en la factura> <Fecha inicio> <Fecha final> <GLID MO> <GLID MT> <Precio MO> <Precio MT> <Tax Code MO> <Tax Code MT>"
	echo "Donde:"
	echo "	Las fechas deben indicarse en formato: yyyyMMdd"
	rm $lockfile
	exit 1
fi

$cmd $1 "$2" $3 $4 $5 $6 $7 $8 $9 ${10}
	if [[ $? -ne 0 ]]; then
		echo "Codigo de retorno: ERROR" >> bitacora.txt
		echo "Codigo de retorno: ERROR"
	else
		echo "Codigo de retorno: OK" >> bitacora.txt
		echo "Codigo de retorno: OK"
	fi

rm $lockfile
