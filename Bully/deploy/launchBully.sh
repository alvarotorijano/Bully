
#linea de argumentos de prueba: 1 192.168.1.1 2 192.168.1.1 3 192.168.1.2

direcciones="$(grep -oE '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' <<< "$@")"  
maquinas=$(echo "$direcciones"|tr " " "\n"|sort -u|tr "\n" " ")
echo $maquinas

./downloadAndLaunch.sh $maquinas

java -jar Bully.jar $@
