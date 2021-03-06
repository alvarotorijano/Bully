#!/bin/bash
sudo apt install default-jre git -y
cd ~/
./apacheServer/bin/shutdown.sh
rm -rf apacheServer
# rm -rf Bully
# git clone https://github.com/alvarotorijano/Bully.git
wget http://ftp.cixug.es/apache/tomcat/tomcat-7/v7.0.94/bin/apache-tomcat-7.0.94.tar.gz


tar xvzf apache-tomcat-7.0.94.tar.gz
mkdir ~/apacheServer
mv apache-tomcat-7.0.94/* ~/apacheServer

rm apache-tomcat-7.0.94.tar.gz
rm -rf apache-tomcat-7.0.94

export JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")
export CATALINA_HOME=~/apacheServer/bin

cp Bully/Bully/deploy/Bully.war ~/apacheServer/webapps/;
sudo ./apacheServer/bin/startup.sh 
