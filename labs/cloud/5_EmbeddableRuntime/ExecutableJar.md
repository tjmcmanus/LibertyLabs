
# Embeddable Runtime

This lab shows how to run a REST application with an embedded liberty runtime (runnable jar).

To run this lab, your workstation must meet the following requirements:

    - You need Java 7 or 8 to run this application; set your JAVA\_HOME environment
    - Variable to the Java install location.
    - Connectivity to the internet is *NOT* required
    - Please refer to the following table for file and resource location references on different operating systems.

Location Ref. |   OS    |     Absolute Path
 --------------| ------- | --------------------------
 *{LAB_HOME}*  | Windows |  `C:\WLP_<VERSION>` or your choice
 *{LAB_HOME}*  | Linux   |  `~/WLP_<VERSION>` or your choice
 *{LAB_HOME}*  | Mac OSX |  `~/WLP_<VERSION>` or your choice


## Packaging and running the runnable jar file.

### Preparing a packaged server

A packaged server contains server.xml, application, and optionally the Liberty profile runtime. The Admin Center currently only supports packaged server deployment with packages that also contain the runtime. To create a packaged server:

1.  Copy the apiDiscovery directory included with the lab in `{LAB_HOME}/labs/cloud/5_EmbeddableRuntime/apiDiscovery` to `{LAB_HOME}/wlp/usr/servers/apiDiscovery` directory.

    Note: if you already did the API Discover Lab, just rename that server to something different.

1.  Package the application by running:

    `server package apiDiscovery --include=minify,runnable --archive=apiDiscovery.ja`r

    Note: You need to have `JAVA_HOME` set on each machine you deploy a packagedServer, if you use the `--include=minify` option. Also run the server command from `{LAB_HOME}/wlp/bin`

1.  This jar file can be moved anywhere and executed with the steps below. For this lab, we will run it from the servers working directory, since that is where the command left the file.

### Start the application with this command:

1.  To run this application:

    1.  `cd {LAB_HOME}/wlp/usr/servers/apiDiscovery`

    1.  `java -jar apiDiscovery.jar`

        **NOTE:** Before starting the app, you need to make sure you don't already have a Liberty app started using port 9080. 


1.  You should see the application output similar to this:
  ~~~~
  Extracting files to /Users/liberty/wlpExtract/apiDiscovery_47273256098806/wlp

   Successfully extracted all product files.

   Launching apiDiscovery (WebSphere Application Server 17.0.0.4/wlp-1.0.12.cl50920160227-1523) on Java HotSpot(TM) 64-Bit Server VM, version 1.8.0_73-b02 (en_US)

  [AUDIT ] CWWKE0001I: The server apiDiscovery has been launched.

  [AUDIT] CWWKZ0058I: Monitoring dropins for applications.

  [AUDIT] CWWKT0016I: Web application available (default_host): http://localhost:9080/ibm/api/

  [AUDIT] CWWKT0016I: Web application available (default_host): http://localhost:9080/ibm/api/explorer/

  [AUDIT] CWWKT0016I: Web application available (default_host): http://localhost:9080/airlines/

  [AUDIT] CWWKZ0001I: Application airlines started in 0.291 seconds.

  [AUDIT] CWWKF0012I: The server installed the following features: [servlet-3.1, ssl-1.0, jndi-1.0, apiDiscovery-1.0, json-1.0, distributedMap-1.0, appSecurity-2.0, jaxrs-2.0, jaxrsClient-2.0].

  [AUDIT ] CWWKF0011I: The server apiDiscovery is ready to run a smarter planet.
  ~~~~
1.  Access the **airlines** REST application in your browser at the URL specified in the output message above: http://localhost:9080/airlines/

1.  Explore the airlines API in the API Explorer GUI <http://localhost:9080/ibm/api/explorer/>

    1.  User id: **admin**
    1.  Password: **admin**

1. Invoke the APIs using the 'try it out' buttons in the explorer

1. The extract location of the application is shown in the message output; in this example it is

`Users/liberty/wlpExtract/apiDiscovery_47273256098806/wlp`

The Liberty runtime also writes output to that location.

1. Look at the swagger annotations in the application source files that provide the documentation for the APIs. In this example the application files are in this location:

 `Users/liberty/wlpExtract/apiDiscovery_47273256098806/wlp/usr/servers/apiDiscovery/apps/expanded`

1.  Examine the liberty server log files written under the application extract location. In this example it is

 `Users/liberty/wlpExtract/apiDiscovery_47273256098806/wlp/usr/servers/apiDiscovery/logs`

1.  Stop the application: `Ctl-C` in the application command window or run the command

 `Users/liberty/wlpExtract/apiDiscovery_47273256098806/wlp/bin/server stop apiDiscovery`

Notice that the extracted application and server output files have been cleaned up. To retain the server output files after the server stops, set this property to a different location from the extract directory before starting the server: `WLP_OUTPUT_DIR`
