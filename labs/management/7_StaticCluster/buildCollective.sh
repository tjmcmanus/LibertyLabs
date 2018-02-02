#!/bin/bash +x

####  This script will build you Collective environment ####
####

while [[ $# -gt 1 ]]
do
key="$1"

case $key in
    -h|--hostName)
    HOST="$2"
    shift # past argument
    ;;
    -d|--LabHomeDirectory)
    LAB_HOME="$2"
    shift # past argument
    ;;
    -u|--rpcUser)
    RPC_USER="$2"
    shift # past argument
    ;;
    -p|--rpcUserPassword)
    RPC_PASSWORD="$2"
    shift # past argument
    ;;
  esac
shift # past argument or value
done



WLPHOME=$LAB_HOME/wlp

if [ -d "$WLPHOME/usr/servers/controller" ]; then
  $WLPHOME/bin/server stop controller ;
  rm -r $WLPHOME/usr/servers/controller ;
fi

if [ ! -d "$WLPHOME/etc" ]; then
     mkdir $WLPHOME/etc ;
     echo "COMPUTERNAME="$HOST > $WLPHOME/etc/server.env ;
fi


$WLPHOME/bin/server create controller ;
$WLPHOME/bin/collective create controller --keystorePassword=labPassword --createConfigFile --hostName=$HOST  ;
cp $LAB_HOME/labs/management/0_AdminCenter_*/*.xml $WLPHOME/usr/servers/controller ;
cp $LAB_HOME/labs/management/0_AdminCenter_*/bootstrap.properties $WLPHOME/usr/servers/controller ;

$WLPHOME/bin/server start controller ;
if [ ! -d "$LAB_HOME/packagedServers" ]; then
     mkdir $LAB_HOME/packagedServers ;
fi

sleep 3 ;

$WLPHOME/bin/collective updateHost $HOST --host=$HOST --port=9443 --user=admin --password=adminpwd --rpcUser=$RPC_USER --rpcUserPassword=$RPC_PASSWORD --hostReadPath=$LAB_HOME/packagedServers --hostWritePath=$LAB_HOME/packagedServers --autoAcceptCertificates
