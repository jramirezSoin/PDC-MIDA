#!/bin/ksh
#
#Reporte de GLIDs en PDC
#Autor: Adrian Rocha

WORK_DIR=$PDC_HOME/ice_custom/apps/proveedores/sql

sqlplus -s $PIN_DB_USER/$PIN_DB_PASS@$PIN_DB_NAME @$WORK_DIR/consulta_glid.sql "$1" |grep -v ^old |grep -v ^new
if [ $? != 0 ]; then
   echo "Error al ejecutar la consulta consulta_glid.sql" | tee -a control_t_doc.log
   exit 1
fi

echo "------------------------------------------------------------"

