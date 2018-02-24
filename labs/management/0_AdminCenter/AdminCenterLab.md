# Managing Liberty collective with Admin Center

This lab will show you how to use the Admin Center graphical user interface to manage Liberty servers from a collective controller. We will deploy a cluster of packaged servers, view the deployed environment, perform start and stop operations, and view basic performance metrics.

Please refer to the following table for file and resource location references on different operating systems.

Location Ref. |   OS    |     Absolute Path
 --------------| ------- | --------------------------
 *{LAB_HOME}*  | Windows |  `C:\WLP_<VERSION>` or your choice
 *{LAB_HOME}*  | Linux   |  `~/WLP_<VERSION>` or your choice
 *{LAB_HOME}*  | Mac OSX |  `~/WLP_<VERSION>` or your choice  

## Prerequisites

The following preparation must be completed prior to beginning this lab:

1.  Complete the Getting Started lab to set up the lab environment, including JRE, and Liberty runtime.

1.  Ensure your web browser is up to date. For example, if you are using the Mozilla browser and you see 0 servers in the Admin Center, and pressing the Refresh Key `(Ctrl+F5)` does not help, you need to update your browser.

## Setting up your Windows environment for remote access (RXA)

Liberty collective administration is agent-less. It uses either SSH or user id and password to connect remotely to a host to perform file transfer and remote command execution. This is needed to deploy, start, and stop Liberty profile servers.

Even though it is possible to use SSH, for this lab we will use user id and password to keep the setup simple. For Windows, we need to configure the Windows environment to enable remote file transfer and command execution:

1.  Ensure you are using a user ID that is part of the Administrator group.

1.  Disable **User Account Control** (UAC).

    1.  For Windows 7

        1.  Search for “change user account control settings” from the Start menu

            ![](./media/image2.png)

            ![](./media/image3.png)

        1. Move the slider to **Never notify**

            ![](./media/image4.png)

1.  Ensure the **Remote Registry** service is running. Set the startup type to automatic so you don’t have to remember to enable it after reboot.

    1.  For **Windows 7**

        1.  Search for ***services*** from the Start menu

            ![](./media/image5.png)

            ![](./media/image6.png)

        1. Find **Remote Registry**

            ![](./media/image7.png)

1.  Ensure you enable remote file sharing for your network adapter

1.  Ensure that you share your ***C drive***

    ![](./media/image8.png)

1.  If running **Windows XP**, disable simple file sharing.

1.  For **Windows 10**, a registry key has to be edited to access the **TEMP** variable.

    1.  From a command prompt, run following command

        `regedit.exe /S {LAB_HOME}\labs\management\0_AdminCenter\LocalAccountTokenFilterPolicy.reg`

        The following key will be added to the Windows registry

        `[HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Policies\System\]` to include the **LocalAccountTokenFilterPolicy** **DWORD** set to **1**

## Setup up other environments

1.  Ensure you are using a user ID that is part of the Administrator group.

1.  Make sure you have **sshd** running in your Linux or Mac environment  ***Note:*** For Mac grep use `ps -ax | grep ssh`

    1.  `ps -aux | grep sshd`
        ~~~~
        root 1389 0.0 0.0 65520 6192 ? Ss 09:17 0:00 **/usr/sbin/sshd -D**

        liberty 5989 0.0 0.0 21292 940 pts/2 S+ 13:15 0:00 grep --color=auto sshd
        ~~~~

## Encoding Password

The Liberty profile supports password encoding, password encryption, and password hashing. Password encoding uses a simple algorithm to obfuscate the password stored in your configuration files.

Password encryption allows you to provide a key to encrypt the password. Password hashing provides a one way hash for passwords, useful for storing passwords of users in the built-in user registry.

To keep it simple, we’ll use password encoding for this lab. You may generate encoded passwords via the **securityUtility** command. Try:

`{LAB_HOME}\wlp\bin\securityUtility encode labPassword`

You’ll get:

***{xor}Mz49Dz4sLCgwLTs=***

![](./media/image9.png)

## Configuring hostname

You can define your server configuration files so that they are host independent. This allows the same configuration files to be deployed across multiple hosts without changes. Variables whose values can stay the same for each host, such as port numbers, can go into the file bootstrap.properties. Variables that vary for each host, such as hostname, can be resolved via operating system environment variables.

Review `{LAB_HOME}\labs\management\0_AdminCenter\collective-create-include.xml` **Note:** The following lines:
~~~~
<variable name="defaultHostName" value="${env.COMPUTERNAME}" />
<quickStartSecurity userName="${adminUser}" userPassword="${adminPassword}" />
~~~~
**Note:** The variables adminUser and adminPassword are resolved in the file bootstrap.properties by the values **admin** and **adminpwd**
~~~~
adminUser=admin
adminPassword=adminpwd
~~~~
For the variable **defaultHostName**, it is currently resolved to `${env.COMPUTERNAME}`, which is the operating system environment variable **COMPUTERNAME**:

1.  For the Windows platform, this resolves to the pre-defined environment variable **COMPUTERNAME**.

    1.  Open a command prompt window and issue the command **set**. Verify that it resolves to your hostname. For example:

      `set COMPUTERNAME=<your hostname>`

       **Note:** that it may be in all upper case.

1.  Verify that you can ping your hostname. It is okay to use lower case.

1.  You may use this hostname for the rest of this lab. It is okay to use lower case. If you have multiple hostnames on your host, and you want to use a different hostname, you may change `${env.COMPUTERNAME}` to a different hostname, or to another environment variable that resolves to a different hostname. Ensure you are able to ping the hostname you plan to use for this lab.

1.  For non-Windows platform, you need to

    1.  Change `${env.COMPUTERNAME}` to your hostname, which is the simplest for the purpose of this lab, or

    1.  Define an environment variable **COMPUTERNAME** that maps to your hostname,

     `export COMPUTERNAME=<yourhostname>`

    1.  Change `${env.COMPUTERNAME}` to a different environment variable that maps to your hostname

    1.  Ensure you can ping your hostname

## Creating the Collective Controller

The collective controller is a Liberty server that you configure with features that allow it to serve as an administrative server.

There is a `buildCollective.sh` script that will execute this section (***for use on Mac and Linux only***).

1. `cd {LAB_HOME}\labs\management\0_AdminCenter`

1. `./buildCollective.sh –d {LAB_HOME} –h <host name> -u <rpcUser> -p <rpcUserPassword>`

    a.  If running on MAC, edit the script to remove `–-hostJavaHome= `from the updateHost command, which is the last line in the script.

1. Skip now to section **Logging into Admin Center**

To create the controller manually:

1.  Make sure you first stop all other servers from other labs.

1.  From the last section, you should have determined the hostname you will use for this lab, and modified the configuration file collective-create-include.xml if you are unable to use the default environment variable `${env.COMPUTERNAME}` to resolve your hostname. From here on, where you see `<hostname>`, substitute with your hostname.

2.  Change directory to `{LAB_HOME}\wlp\bin`

3.  Create a new server called **adminCenterController** by issuing the following command. This is the server that we will use as the collective controller:

`server create adminCenterController`

1.  Create the additional configurations required to run the ***adminCenterController*** as a collective controller:

`collective create adminCenterController --keystorePassword=labPassword --createConfigFile --hostName=<hostname>`

![](./media/image10.png)

1.  Copy the following files from `{LAB_HOME}\labs\management\0_AdminCenter` directory to `{LAB_HOME}\wlp\usr\servers\adminCenterController` directory:

    1. `server.xml`
    1. `collective-create-include.xml`
    1. `remotefileaccess.xml`
    1. `bootstrap.properties`

1.  Review the files that were copied:

    1.  Note that the `server.xml` contains the following changes:

        1.  The Admin Center feature has been added:

            `<feature>adminCenter-1.0</feature>`

        1. A hostname of *** * *** has been added to allow remote access from all interfaces

        1. It contains an include for the `collective-create-include.xml`

    1.  The `collective-create-include.xml` is required to enable the server to function as a collective controller. It is the same as the version created via the **collective create** command except for the variables introduced to resolve hostname, and administrator user and password.

    1.  The `remotefileaccess.xml` is required to enable the internal workings to read and write from the filesystem and the collective to operation properly.

    1.  The file `bootstrap.properties` was introduced to resolve the administrator user and password variables

1. Start the controller:

    `server start adminCenterController`

1. Check `{LAB_HOME}\wlp\usr\servers\adminCenterController\logs\messages.log` for the following message which indicates the controller is ready for registration from application servers

    [`5/5/15 18:54:56:669 EDT] 0000001b collective.repository.internal.CollectiveRepositoryMBeanImpl I CWWKX9000I: The CollectiveRepository MBean is available`.

1. Create a **packagedServers** directory that will be the target directory for the packaged servers. This could be named anything, but for the lab it is **packagedServers**.

    a.  `mkdir {LAB_HOME}/packagedServers`

1. Update the controller with additional parameters that will be required when we deploy a packaged server. Substitute with the correct host name. (For this lab, there is only one hostname) In addition, ***rpcUser*** is the user ID you use to log in to the operating system, and ***rpcUserPassword*** is the corresponding password. Accept the certificate if prompted to do so while running the command.  *For Mac, leave off the --hostJavaHome*

    `collective updateHost <hostname> --host=*<controller hostname>* --port=9443 --user=admin --password=adminpwd --rpcUser=<OS user> --rpcUserPassword=*<OS password>* --hostReadPath=*{LAB_HOME}/packagedServers --hostWritePath=*{LAB_HOME}/packagedServers --hostJavaHome={LAB_HOME}/wlp/java/jre`

    ![](./media/image11.png)

## Working with Admin Center

### Logging into Admin Center

1.  Point your browser to <http://localhost:9080/adminCenter>

1.  If prompted, add security exception by clicking **Add Exception**.

    ![](./media/image12.png)

1.  Confirm security exception temporarily by clicking **Confirm Security Exception**.

    ![](./media/image13.png)

1.  login to the Admin Center with user **admin**, and password **adminpwd**

    ![](./media/image14.png)

1.  Click the **Explore** icon

    ![](./media/image15.png)

1.  This shows there is one application server running, which is the *adminCenterController*. There is also one host being managed. Click on each of the icons for **server**, and **cluster** to explore. Click on the toolbox icon to go back.

    ![](./media/image16.png)

## Preparing a packaged server

A packaged server contains server.xml, application, and optionally the Liberty profile runtime. The Admin Center currently only supports packaged server deployment with packages that also contain the runtime. To create a packaged server:

1.  Copy the `snoop_1` directory included with the lab in `{LAB_HOME}\labs\management\0_AdminCenter\snoop_1` to `{LAB_HOME}\wlp\usr\servers\snoop_1` directory.

    ![](./media/image17.png)

1.  Examine the `server.xml` and `bootstrap.properties`. Notice that they include the following features:

    1.  *collectiveMember:* allows the server to be managed by the controller.

    1.  *clusterMember:* allows the server to be a member of a cluster.

1.  Examine the `admin-metadata.xml`. It provides additional information that will help you understand your environment, and will be important as the complexity of the environment increases. We will show you how they are used within the Admin Center later in this lab.

    Note: You need to have `JAVA_HOME` set on each machine you deploy a packagedServer, if you use the `–include=minify` option.

    Run the following command to package the server. This creates a `snoop_1.zip` in the server’s directory.` server package snoop_1 --include=all`

1.  Optionally examine the size and the contents of `snoop_1.zip`

## Deploying a packaged server

1. Click the **Deploy** icon.
    ![](./media/image18.png)

1. Click **Liberty** as the type of server then click **Next**.
    ![](./media/image19.png)

1. Select the deploy rule for **Server Package** then click **Next**.
    ![](./media/image20.png)

1.  Validate then click **Confirm**
    ![](./media/image21.png)

1.  On the Deployment Parameters screen, enter the following:

    1.  Package directory: `{LAB_HOME}\wlp\usr\servers\snoop_1`

    1. Server Package File: `snoop_1.zip`

    1. Target Directory: `{LAB_HOME}/packagedServers` Recall that earlier we ran a **collective updateHost** command to inform the controller what directory we are able to deploy to.

        ![](./media/image22.png)

        **Alternately**, you can click the Upload a file (above in light blue), click the browse button. This will populate the Server Package Directory and Server Package File Name for you. You will still have to populate the Target Directory.

1. Click the host to deploy the server package. This will move to the left and change 0 selected Host to 1

    ![](./media/image23.png)

1. On Security Details sections, use **labPassword** or another password of your choosing, as the key store password then click **Deploy**

![](./media/image24.png)

1.  The Server is being deployed. Another deployment can be started by clicking **Start another deployment +**

    ![](./media/image25.png)

1.  Wait for the upload to complete, then click the clipboard icon at the upper-right corner of the window to get to the Task History. Click **Task Details and History** to see the status of the deployment.

1.  Problem Determination:

    a.  If you get this message: `“CWWKX7204E: Cannot connect to host <host name> with the credentials provided”`, ensure you entered the correct user ID and password. Also ensure you have enabled File and Print Sharing for your network adapter.

    b.  If you get a `CWWKX0262E` message, ensure you specified the correct **hostReadPath** and **hostWritePath** options when running the **collective updateHost** command above. You may re-run the command again with the correct values. Also ensure you specified {`LAB_HOME}\packagedServers\snoop_1` as the destination directory.

    ![](./media/image26.png)

1.  A successful deployment looks like:

    ![](./media/image27.png)

1.  Examine what has been deployed by the Admin Center:

    a.  Look at the files in the `{LAB_HOME}\packagedServers\snoop_1\wlp\usr\servers\snoop_1` directory.

    b.  Note that the Admin Center creates the `configDropins/defaults/additionalConfig.xml` that allows the member to join with the controller.

1.  Go back to the **Explore** view. Notice that there are now two servers. However, no cluster has been detected yet.

    ![](./media/image28.png)

1.  Click the **Servers** panel

1.  Click the ![](./media/image29.png) icon on the top-right of the `snoop_1` server box, then **Start** the server `snoop_1`.

    ![](./media/image30.png)

1.  Shortly the server status will change to **Running**

    ![](./media/image31.png)

1. Click the **Speedometer** icon at top-left corner to go back to the dashboard:

    ![](./media/image32.png)

1. Note that a cluster is now detected and the application is running

    ![](./media/image33.png)

1. Explore the Admin Center on your own to start/stop clusters or applications.

### Working with Administrative Metadata

The information that you provide in the admin-metadata.xml is associated with specific servers and clusters. These include tags, contacts, and owner. The Admin Center displays the administrative metadata associated with each server and cluster. You may provide administrative metadata to give you and other administrator additional context about the server or cluster being managed.

1. Click on the **Speedometer** icon at top-left corner to go back to the dashboard

    ![](./media/image34.PNG)

1. Click on **Clusters**

    ![](./media/image35.png)

1. Click on **snoopCluster**

    ![](./media/image36.png)

1. Click on **Tags and Metadata**

    ![](./media/image37.png)

1. Note that the information exported in the admin-metadata.xml are displayed, including tags, owner, and contacts. Also note that tags are converted to lower case (e.g. `0_admincenter `instead of `0_AdminCenter`).

    ![](./media/image38.png)

1. Click on **Servers**

    ![](./media/image39.png)

1. Click on **snoop_1**

    ![](./media/image40.png)

1.  Click **Tags and Metadata**

1. Note the administrative metadata-data for the server is displayed:

    ![](./media/image41.png)

### Using the search tool

You may use the search tool in the explorer to search for resources being managed

1.  Click on the **Search** icon

    ![](./media/image42.png)

1.  The initial search criterion is to search by name. Enter “snoop” for the **Name**, then click on the **Search** button. Note that the search tool has found 1 application, 1 server, and 1 cluster with name containing the substring “snoop”.

    ![](./media/image43.png)

1.  Click on the **+** button, change the search criterion to **Type**, and select **Servers**

    ![](./media/image44.png)

1.  Click on the **Search** icon, and note that only servers whose name contain the substring **snoop** is displayed:

    ![](./media/image45.png)

1.  Click **+**, and set the criterion to **State** being **Stopped**

    ![](./media/image46.png)

1.  Click the **Search** icon, and note that no stopped server is found

    ![](./media/image47.png)

1.  Change the **State** to **Running** then click the **Search** icon, and it should find the running server

    ![](./media/image48.png)

1.  Click **X** to remove the existing search parameters

    ![](./media/image49.png)

1.  Start a new search for tag **0_AdminCenter**. Note that the search tool has found both a server and a cluster tagged with **0_AdminCenter**.

    ![](./media/image50.png)

1.  Experiment with the search tool and think about how it may help you quickly find the resources you need if you have many being managed.

### Viewing Performance Metrics

You can view basic JVM performance metrics of your application server JVMs through the Admin Center.

1.  From the **Explorer** view, click the **Servers** panel

    ![](./media/image51.png)

1.  Click the icon for **snoop_1**.

    ![](./media/image52.png)

1.  Click the **Monitor** icon

    ![](./media/image53.png)

1.  Note that the basic statics for heap, loaded classes, threads, and CPU are available.

    ![](./media/image54.png)

1.  To control which metrics to play, click on the **Edit** icon

    ![](./media/image55.png)

1.  Click the **-** sign on the graphic to remove what you don’t want to display, and click the green plus to add what you want to display. Click **SAVE** when desired selection has been made. For example, remove the **Loaded Classes** and **Active JVM Threads**, and then click **SAVE.**

    ![](./media/image56.png)

1.  This should result in only the heap and CPU graphs being displayed

## Optional Tasks

1.  Optional: deploy another cluster member

    1.  Run the following command to create another server

        `server create snoop_2`

         ![](./media/image57.png)

    1.  Copy `server.xml`, `bootstrap.properties`, and `dropins` directory from `{LAB_HOME}\wlp\usr\servers\snoop_1` to `{LAB_HOME}\wlp\usr\servers\snoop_2`

        ![](./media/image58.png)

     1.  Edit `snoop_2`’s `bootstrap.properties` to change the port numbers

          ~~~~
          httpPort=9082
          httpsPort=9445
          ~~~~

        ![](./media/image59.png)

1.  Run the following command to package the new server

    `server package snoop_2 --include=all`

1.  Deploy **snoop_2** to `{LAB_HOME}\packagedServers\snoop_2` directory



1.  ***Optional:*** Register another host and deploy an application to it. To register another host, run this command, where `<OS user>` and `<OS password>` are user ID and password required to login to the other host:

    `collective registerHost <otherhost> --host=<controller hostname> --port=9443 --user=admin --password=adminpwd --rpcUser=<OS user> --rpcUserPassword=<OS password> --hostReadPath={LAB_HOME}/packagedServers --hostWritePath={LAB_HOME}/packagedServers`

## Cleanup

Follow these steps to clean up

1.  Stop all servers from the Admin Center

1.  The Admin Center does not yet support deletion of your packaged server. You may do this from the command line. To delete **snoop_1**:

    a.  Change directory to `{LAB_HOME}\packagedServers\snoop_1\wlp\bin`

    b.  Run the following command:

        `collective remove snoop_1 --host=<controller host> --port=9443 --user=admin --password=adminpwd --hostName=<host name of member>`

        **Note:** Your collective controller and your collective member’s host name are the same for this lab.

    c.  Verify that server **snoop_1** is gone from the Admin Center

    d.  Remove the directory `{LAB_HOME}\packagedservers\snoop_1`

2.  Repeat for each server you have deployed. Remember to change to the correct directory for each deployed server.

3.  Stop the server **adminCenterController**

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

Trademarks and copyrights
=========================

The following terms are trademarks of International Business Machines Corporation in the United States, other countries, or both:

  IBM          AIX        CICS             ClearCase      ClearQuest   Cloudscape   
  ------------ ---------- ---------------- -------------- ------------ ------------ --
  Cube Views   DB2        developerWorks   DRDA           IMS          IMS/ESA      
  Informix     Lotus      Lotus Workflow   MQSeries       OmniFind                  
  Rational     Redbooks   Red Brick        RequisitePro   System i                  
  *System z*   *Tivoli*   *WebSphere*      *Workplace*    *System p*                

Adobe, the Adobe logo, PostScript, and the PostScript logo are either registered trademarks or trademarks of Adobe Systems Incorporated in the United States, and/or other countries.

IT Infrastructure Library is a registered trademark of the Central Computer and Telecommunications Agency which is now part of the Office of Government Commerce.

Intel, Intel logo, Intel Inside, Intel Inside logo, Intel Centrino, Intel Centrino logo, Celeron, Intel Xeon, Intel SpeedStep, Itanium, and Pentium are trademarks or registered trademarks of Intel Corporation or its subsidiaries in the United States and other countries.

Linux is a registered trademark of Linus Torvalds in the United States, other countries, or both.

Microsoft, Windows, Windows NT, and the Windows logo are trademarks of Microsoft Corporation in the United States, other countries, or both.

ITIL is a registered trademark, and a registered community trademark of The Minister for the Cabinet Office, and is registered in the U.S. Patent and Trademark Office.

UNIX is a registered trademark of The Open Group in the United States and other countries.

Java and all Java-based trademarks and logos are trademarks or registered trademarks of Oracle and/or its affiliates.

Cell Broadband Engine is a trademark of Sony Computer Entertainment, Inc. in the United States, other countries, or both and is used under license therefrom.

Linear Tape-Open, LTO, the LTO Logo, Ultrium, and the Ultrium logo are trademarks of HP, IBM Corp. and Quantum in the U.S. and other countries.

![IBM-600dpi-1](./media/image60.png)

© Copyright IBM Corporation 2018.

The information contained in these materials is provided for informational purposes only, and is provided AS IS without warranty of any kind, express or implied. IBM shall not be responsible for any damages arising out of the use of, or otherwise related to, these materials. Nothing contained in these materials is intended to, nor shall have the effect of, creating any warranties or representations from IBM or its suppliers or licensors, or altering the terms and conditions of the applicable license agreement governing the use of IBM software. References in these materials to IBM products, programs, or services do not imply that they will be available in all countries in which IBM operates. This information is based on current IBM product plans and strategy, which are subject to change by IBM without notice. Product release dates and/or capabilities referenced in these materials may change at any time at IBM’s sole discretion based on market opportunities or other factors, and are not intended to be a commitment to future product or feature availability in any way.

IBM, the IBM logo and ibm.com are trademarks of International Business Machines Corp., registered in many jurisdictions worldwide. Other product and service names might be trademarks of IBM or other companies. A current list of IBM trademarks is available on the Web at “Copyright and trademark information” at www.ibm.com/legal/copytrade.shtml.

![Please Recycle](./media/image61.png)
