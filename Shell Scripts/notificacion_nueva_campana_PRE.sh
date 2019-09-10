#!/bin/ksh

echo "Se notifica que una nueva campana de contenido tipo $1 ha sido configurada." > mensaje.tmp
echo "Codigo: $2" >> mensaje.tmp
echo "Vigencia: $3" >> mensaje.tmp
echo "Tarifa/s: $4" >> mensaje.tmp
echo "GLID: $5" >> mensaje.tmp
echo "Nombre en la factura: $6" >> mensaje.tmp
echo "Nota: En caso de una nueva campaña de mensajería de contenido tipo MO; para su correcto funcionamiento, favor validar que el número corto esté habilitado en ECE." >> mensaje.tmp
dest=$(cat config_destinos_notificacion.txt)
if [[ $dest != "" ]] then
cat mensaje.tmp |mailx -s "Creacion de nueva campana de contenido - Autoenviado desde interfaz de proveedores" -r "$HOSTNAME <$HOSTNAME@ice.go.cr>" "$dest"
fi

