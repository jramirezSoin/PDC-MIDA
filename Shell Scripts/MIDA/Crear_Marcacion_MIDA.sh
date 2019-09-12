#!/bin/ksh
cd $PDC_HOME"/ice_custom/apps/mida"


#Chequea parametros de entrada
if [[ $# -ne 6 ]]
then
 echo "El numero de parametros es incorrecto"
 exit 1
fi

echo "Inicio Crear_Marcacion_Mida.sh: "`date +%Y/%m/%d_%H:%M:%S` >> bitacora.txt
echo "Parametros: $*" >> bitacora.txt

dirBase=$PDC_HOME"/ice_custom/apps/mida"
cmdClassPath="$PIN_HOME/jars/pcm.jar:$PIN_HOME/jars/pcmext.jar:$PDC_HOME/ice_custom/jars/commons-io-2.4.jar:$PDC_HOME/oui/ext/ojdbc6.jar:$PDC_HOME/ice_custom/jars/jdom-2.0.6.jar:$PDC_HOME/ice_custom/apps/proveedores/markmanager.jar:$PDC_HOME/ice_custom/apps/mida/GruposInternacionales.jar:$PDC_HOME/ice_custom/apps/mida"
cmdExec="groupUpdater.AddMarkMIDA"
cmd="$JAVA_8_HOME/bin/java -cp "$cmdClassPath" "$cmdExec

cd $dirBase

$cmd $1 $2 $3 $4 $5 $6

if [[ $? -ne 0 ]]
then
        echo "Error al creacion la marcacion"
fi

echo 'Creacion de Marcacion MIDA Finalizada'
