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

if [ -d "$WLPHOME/usr/servers/adminCenterController" ]; then
  $WLPHOME/bin/server stop adminCenterController ;
  rm -r $WLPHOME/usr/servers/adminCenterController ;
fi

if [ -d "$WLPHOME/etc" ]; then
     rm -r  $WLPHOME/etc ;
fi
if [ ! -d "$WLPHOME/etc" ]; then
     mkdir $WLPHOME/etc ;
     echo "COMPUTERNAME="$HOST > $WLPHOME/etc/server.env ;
fi


$WLPHOME/bin/server create adminCenterController ;
$WLPHOME/bin/collective create adminCenterController --keystorePassword=labPassword --createConfigFile --hostName=$HOST  ;
cp $LAB_HOME/labs/management/0_AdminCenter_*/*.xml $WLPHOME/usr/servers/adminCenterController ;
cp $LAB_HOME/labs/management/0_AdminCenter_*/bootstrap.properties $WLPHOME/usr/servers/adminCenterController ;

$WLPHOME/bin/server start adminCenterController ;
if [ -d "$LAB_HOME/packagedServers" ]; then
     rm -r $LAB_HOME/packagedServers ;
fi
if [ ! -d "$LAB_HOME/packagedServers" ]; then
     mkdir $LAB_HOME/packagedServers ;
fi

sleep 3 ;

$WLPHOME/bin/collective updateHost $HOST --host=$HOST --port=9443 --user=admin --password=adminpwd --rpcUser=$RPC_USER --rpcUserPassword=$RPC_PASSWORD --hostReadPath=$LAB_HOME/packagedServers --hostWritePath=$LAB_HOME/packagedServers  --autoAcceptCertificates --hostJavaHome=$WLPHOME/java
