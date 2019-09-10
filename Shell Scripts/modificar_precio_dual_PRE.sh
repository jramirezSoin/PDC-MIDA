#!/bin/ksh

echo "****** Inicio modificar_precio_dual_PRE.sh: "`date +%Y/%m/%d_%H:%M:%S`" ******"
echo "Inicio modificar_precio_dual_PRE.sh: "`date +%Y/%m/%d_%H:%M:%S` >> bitacora.txt
echo "Parmetros: $*" >> bitacora.txt
dirBase=$PDC_HOME"/ice_custom/apps/proveedores"
cmdClassPath="$PIN_HOME/jars/pcm.jar:$PIN_HOME/jars/pcmext.jar:$PDC_HOME/ice_custom/jars/commons-io-2.4.jar:$PDC_HOME/oui/ext/ojdbc6.jar:$PDC_HOME/ice_custom/jars/jdom-2.0.6.jar:$PDC_HOME/ice_custom/apps/proveedores/markmanager.jar:$PDC_HOME/ice_custom/apps/proveedores"
cmdExec="markmanager.AddPriceModelStepDual"
cmd="$JAVA_8_HOME/bin/java -cp "$cmdClassPath" "$cmdExec

cd $dirBase
if [[ $# -lt 9 ]]; then
	echo "Parametros insuficientes"
	echo "Modo de uso: ./modificar_precio_dual_PRE.sh <Modalidad> <ID campana> <Precio MO> <Precio MT> <Fecha desde> <GLID MO> <GLID MT> <Tax Code MO> <Tax Code MT>"
	echo "Donde:"
	echo "	Las fechas deben indicarse en formato: yyyyMMdd"
	exit 1
fi

$cmd $*
	if [[ $? -ne 0 ]]; then
		echo "Codigo de retorno: ERROR" >> bitacora.txt
		echo "Codigo de retorno: ERROR"
	else
		echo "Codigo de retorno: OK" >> bitacora.txt
		echo "Codigo de retorno: OK"
	fi
