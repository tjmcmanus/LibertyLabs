
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

## Notices

This information was developed for products and services offered in the U.S.A.

IBM may not offer the products, services, or features discussed in this document in other countries. Consult your local IBM representative for information on the products and services currently available in your area. Any reference to an IBM product, program, or service is not intended to state or imply that only that IBM product, program, or service may be used. Any functionally equivalent product, program, or service that does not infringe any IBM intellectual property right may be used instead. However, it is the user's responsibility to evaluate and verify the operation of any non-IBM product, program, or service.

IBM may have patents or pending patent applications covering subject matter described in this document. The furnishing of this document does not grant you any license to these patents. You can send license inquiries, in writing, to:

IBM Director of Licensing\
IBM Corporation\
North Castle Drive\
Armonk, NY 10504-1785\
U.S.A.

For license inquiries regarding double-byte (DBCS) information, contact the IBM Intellectual Property Department in your country or send inquiries, in writing, to:

IBM World Trade Asia Corporation\
Licensing\
2-31 Roppongi 3-chome, Minato-ku\
Tokyo 106-0032, Japan

**The following paragraph does not apply to the United Kingdom or any other country where such provisions are inconsistent with local law:** INTERNATIONAL BUSINESS MACHINES CORPORATION PROVIDES THIS PUBLICATION "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Some states do not allow disclaimer of express or implied warranties in certain transactions, therefore, this statement may not apply to you.

This information could include technical inaccuracies or typographical errors. Changes are periodically made to the information herein; these changes will be incorporated in new editions of the publication. IBM may make improvements and/or changes in the product(s) and/or the program(s) described in this publication at any time without notice.

Any references in this information to non-IBM Web sites are provided for convenience only and do not in any manner serve as an endorsement of those Web sites. The materials at those Web sites are not part of the materials for this IBM product and use of those Web sites is at your own risk.

IBM may use or distribute any of the information you supply in any way it believes appropriate without incurring any obligation to you.

Any performance data contained herein was determined in a controlled environment. Therefore, the results obtained in other operating environments may vary significantly. Some measurements may have been made on development-level systems and there is no guarantee that these measurements will be the same on generally available systems. Furthermore, some measurements may have been estimated through extrapolation. Actual results may vary. Users of this document should verify the applicable data for their specific environment.

Information concerning non-IBM products was obtained from the suppliers of those products, their published announcements or other publicly available sources. IBM has not tested those products and cannot confirm the accuracy of performance, compatibility or any other claims related to non-IBM products. Questions on the capabilities of non-IBM products should be addressed to the suppliers of those products.

All statements regarding IBM's future direction and intent are subject to change or withdrawal without notice, and represent goals and objectives only.

This information contains examples of data and reports used in daily business operations. To illustrate them as completely as possible, the examples include the names of individuals, companies, brands, and products. All of these names are fictitious and any similarity to the names and addresses used by an actual business enterprise is entirely coincidental. All references to fictitious companies or individuals are used for illustration purposes only.

COPYRIGHT LICENSE:

This information contains sample application programs in source language, which illustrate programming techniques on various operating platforms. You may copy, modify, and distribute these sample programs in any form without payment to IBM, for the purposes of developing, using, marketing or distributing application programs conforming to the application programming interface for the operating platform for which the sample programs are written. These examples have not been thoroughly tested under all conditions. IBM, therefore, cannot guarantee or imply reliability, serviceability, or function of these programs.

## Trademarks and copyrights

The following terms are trademarks of International Business Machines Corporation in the United States, other countries, or both:

  IBM          AIX        CICS             ClearCase      ClearQuest   Cloudscape   
  ------------ ---------- ---------------- -------------- ------------ ------------ --
  Cube Views   DB2        developerWorks   DRDA           IMS          IMS/ESA      
  Informix     Lotus      Lotus Workflow   MQSeries       OmniFind                  
  Rational     Redbooks   Red Brick        RequisitePro   System i                  
  *System z*   *Tivoli*   *WebSphere*      *Workplace*    *System p*                

Adobe, Acrobat, Portable Document Format (PDF), and PostScript are either registered trademarks or trademarks of Adobe Systems Incorporated in the United States, other countries, or both.

Cell Broadband Engine is a trademark of Sony Computer Entertainment, Inc. in the United States, other countries, or both and is used under license therefrom.

Java and all Java-based trademarks and logos are trademarks of Sun Microsystems, Inc. in the United States, other countries, or both. See Java Guidelines

Microsoft, Windows, Windows NT, and the Windows logo are registered trademarks of Microsoft Corporation in the United States, other countries, or both.

Intel, Intel logo, Intel Inside, Intel Inside logo, Intel Centrino, Intel Centrino logo, Celeron, Intel Xeon, Intel SpeedStep, Itanium, and Pentium are trademarks or registered trademarks of Intel Corporation or its subsidiaries in the United States and other countries.

UNIX is a registered trademark of The Open Group in the United States and other countries.

Linux is a registered trademark of Linus Torvalds in the United States, other countries, or both.

ITIL is a registered trademark and a registered community trademark of the Office of Government Commerce, and is registered in the U.S. Patent and Trademark Office.

IT Infrastructure Library is a registered trademark of the Central Computer and Telecommunications Agency which is now part of the Office of Government Commerce.

Other company, product and service names may be trademarks or service marks of others.

![IBM-1](./media/image2.png)

© Copyright IBM Corporation 2017.

The information contained in these materials is provided for informational purposes only, and is provided AS IS without warranty of any kind, express or implied. IBM shall not be responsible for any damages arising out of the use of, or otherwise related to, these materials. Nothing contained in these materials is intended to, nor shall have the effect of, creating any warranties or representations from IBM or its suppliers or licensors, or altering the terms and conditions of the applicable license agreement governing the use of IBM software. References in these materials to IBM products, programs, or services do not imply that they will be available in all countries in which IBM operates. This information is based on current IBM product plans and strategy, which are subject to change by IBM without notice. Product release dates and/or capabilities referenced in these materials may change at any time at IBM’s sole discretion based on market opportunities or other factors, and are not intended to be a commitment to future product or feature availability in any way.

IBM, the IBM logo and ibm.com are trademarks or registered trademarks of International Business Machines Corporation in the United States, other countries, or both. If these and other IBM trademarked terms are marked on their first occurrence in this information with a trademark symbol (® or ™), these symbols indicate U.S. registered or common law trademarks owned by IBM at the time this information was published. Such trademarks may also be registered or common law trademarks in other countries. A current list of IBM trademarks is available on the Web at “Copyright and trademark information” at ibm.com/legal/copytrade.shtml

Other company, product and service names may be trademarks or service marks of others.

![Please Recycle](./media/image3.png)
