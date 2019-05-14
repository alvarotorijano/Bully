function valid_ip()
{
    local  ip=$1
    local  stat=1

    if [[ $ip =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        OIFS=$IFS
        IFS='.'
        ip=($ip)
        IFS=$OIFS
        [[ ${ip[0]} -le 255 && ${ip[1]} -le 255 \
            && ${ip[2]} -le 255 && ${ip[3]} -le 255 ]]
        stat=$?
    fi
    return $stat
}

direcciones=$@

notify-send 'Lanzando Servidores' $direcciones

echo "Script de lanzamiento de servidores"

# $# te devuelve el numero de argumentos

nArgumentos=$@
echo "estos son mis argumentos"
echo $nArgumentos

#linea de argumentos de prueba: 1 192.168.1.1 2 192.168.1.1 3 192.168.1.2

direcciones="$(grep -oE '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' <<< "$@")"  
maquinas=$(echo "$direcciones"|tr " " "\n"|sort -u|tr "\n" " ")
echo $maquinas

#for ip in maquinas
#do
#	indice++
#	echo "parametro: $ip"
#
#	if valid_ip $ip; 
#	then 
#		ssh root@$ip "
#			rm -rf Bully;
#			apt update;
#			apt install git wget -y;
#			git clone https://github.com/alvarotorijano/Bully.git;
#			./Bully/Bully/deploy/Launch.sh
#		";
#	else 
#		echo "Parametro invalido $ip"; 
#	fi
#
#	java -jar 
#
#done

