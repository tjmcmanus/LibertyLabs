#FROM websphere-liberty:latest
FROM registry.ng.bluemix.net/ibmliberty:latest
COPY EmployeeApp.war /opt/ibm/wlp/usr/servers/defaultServer/apps/
COPY server.xml /opt/ibm/wlp/usr/servers/defaultServer/
COPY db2*jar /opt/ibm/wlp/usr/shared/resources/DB2Libs/
