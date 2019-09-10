#!/bin/ksh

echo "****** Inicio modificar_precio.sh: "`date +%Y/%m/%d_%H:%M:%S`" ******"
echo "Inicio modificar_precio.sh: "`date +%Y/%m/%d_%H:%M:%S` >> bitacora.txt
echo "Parmetros: $*" >> bitacora.txt
dirBase=$PDC_HOME"/ice_custom/apps/proveedores"
cmdClassPath="$PIN_HOME/jars/pcm.jar:$PIN_HOME/jars/pcmext.jar:$PDC_HOME/ice_custom/jars/commons-io-2.4.jar:$PDC_HOME/oui/ext/ojdbc6.jar:$PDC_HOME/ice_custom/jars/jdom-2.0.6.jar:$PDC_HOME/ice_custom/apps/proveedores/markmanager.jar:$PDC_HOME/ice_custom/apps/proveedores"
cmdExec="markmanager.AddPriceModelStep"
cmd="$JAVA_8_HOME/bin/java -cp "$cmdClassPath" "$cmdExec

cd $dirBase
if [[ $# -lt 7 ]]; then
	echo "Parametros insuficientes"
	echo "Modo de uso: ./modificar_precio.sh <Tipo de campana> <Tipo de tasacion> <ID campana> <Precio> <Fecha desde> <Tax Code> <GLID>"
	echo "Donde:"
	echo "	Tipo de campana: SMS/900"
	echo "	Tipo de tasacion: MO/MT (para SMS) o bien DUR/EVT (para 900)"
	echo "	Las fechas deben indicarse en formato: yyyyMMdd"
	echo "  Tax Code: IV/CR/NU/IV-NU/IV-CR/NU-CR/IV-CR-NU"
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
