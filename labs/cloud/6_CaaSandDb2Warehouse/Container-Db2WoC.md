### 1. Containers and Db2 Warehouse on IBM Cloud

The lab will use IBM Cloud for executing the application in containers and hosting the database.

With IBM Cloud Container Service, the user’s CaaS experience begins with deploying Docker containers in a Kubernetes orchestrated managed environment with additional qualities of services, e.g., vulnerability scanning of the images in the registry, very simple mechanism to bind to almost any of the available services in IBM Cloud, integrated monitoring and logging, attaching storage volumes for persistent data needs.

The lab is divided into four main parts:

1.  Create a IBM Container Cluster on IBM Cloud.

1.  Create an instance of Db2 Warehouse on Cloud (entry version)

1.  Containerize the Liberty server along with the application in a Docker container and access the database host on IBM Cloud. The application along with its middleware is containerized in a Docker container for portability.

1.  Next push the Docker container to the secure IBM Bluemix container registry. Once the Docker container is pushed to the IBM secure registry it starts its life cycle as an IBM container. Deploy the container as a Kubernetes deployment resource and expose it as a Kubernetes service. You will access the execute the application by accessing the Kubernetes service. The on-prem application of 
has now been transparently **lifted and shifted** to IBM Bluemix cloud using containers. The application will access the database on the same network as the IBM Container service

1.  Finally, you will scale up and down the container (actually pod) instances using the underlying Kubernetes orchestration engine. Along with scalability, you will also try out the auto recovery features of Kubernetes pods.

The following diagram provides an overview of the four parts of the lab.

![](./media/image1.png)

The lab requires the following software installed:

-   Docker (latest)

-   IBM Bluemix Command Line Interface (CLI) (setup lab)

-   IBM Bluemix Container Service Plug-in

-   IBM Bluemix Container Registry Plug-in

-   WebSphere Liberty

-   kubectl CLI version (latest)

In addition to the above software, the machine also contain the artifacts needed to complete the lab. The following list points out the most important artifacts.

Please refer to the following table for file and resource location references on different operating systems.

Location Ref. |   OS    |     Absolute Path
 --------------| ------- | --------------------------
 *{LAB_HOME}*  | Windows |  `C:\\WLP_<VERSION>` or your choice
 *{LAB_HOME}*  | Linux   |  `~/WLP_<VERSION>` or your choice
 *{LAB_HOME}*  | Mac OSX |  `~/WLP_<VERSION>` or your choice  

The EmployeeApp is a Servlet for performing CRUD operations on a DB2 database. The application will be deployed to WebSphere Liberty as the middleware. The Servlet’s user interface is intuitive enough to try it out in the next sections. Throughout this lab you will run the application locally, in a container, in a container server then scaled in a container service.

**Notes**: IBM Cloud is a fast-evolving platform it is almost guaranteed that there will be some updates to the GUI which may not match the lab workbook. However, using the relevant screen captures and descriptions of the lab book as initial guidelines, with little effort, you should be able find out the right controls in the IBM Cloud console to perform the desired tasks.

## Create a Kubernetes Cluster in Bluemix

You can create a Kubernetes (aka K8s) cluster from IBM Cloud console or from the command line interface (CLI). In the following we provide the details of creating a cluster from the UI.

1.  Browse to http://www.bluemix.net and click on the **Log in** link.

![](./media/image2.png)

1.  Provide your **userid** and **password** in the login panel to logon to IBM Cloud. After logging in, explore the regions and pick which is best for you. These instructions will stay with US South. As you explore, Tokyo is available for Container Services, but not available yet for Db2 Warehouse. Pardon the guess work, but we are expanding availability zones quickly.

![](./media/image3.png)

1.  Click on the menu icon ( ![](./media/image4.png) ) on the upper left-hand side of the Dashboard page.

1.  Select **Containers.**

    ![](./media/image5.png)

1.  Pick the region from the drop down box. The instruction will be US South.

![](./media/image6.png)

1.  Click on **Create Cluster**.

![](./media/image7.png)

1.  Select the defaults and add the name of the cluster. We will use **EmployeeApp-inst**, choose your cluster name. Suggestion `EmployeeApp-<initials>`. If you have a non-lite account, you can see all the different locations and options you can select. We will stay with the free account for the lab. Click **Create Cluster**.

    ![](./media/image8.png)

1.  For a few seconds, you will encounter a spinning cursor and after that you should see your intended cluster is in the **Deploying** state. The resulting panel provides valuable information about downloading and configuring various Bluemix plug-ins required for command line work. You have set up the IBM Cloud CLI in the `{LAB_HOME}/labs/gettingStarted/0_setup` lab. If you have not, follow the instruction on the panel.

    1.  Download the Kubernette’s client libs https://kubernetes.io/docs/tasks/tools/install-kubectl/

    1.  Execute ‘**bx plugin install container-service -r Bluemix**’ to add container commands to the CLI


1.  Alternatively, you can use the `bx cs cluster-create --name <cluster name>` command after logging in to IBM Cloud from the command shell using IBM Cloud (bx) CLI.

It will take some time (approximately 15 to 30 minutes) to get the cluster created. While you complete the other parts of the lab, the cluster will get created by that time and it will be ready for the deployment of your container.

## Creating an instance of Db2 Warehouse on IBM Cloud

1.  Click on the **Catalog** button

    ![](./media/image9.png)

1.  On the left hand side, Click on **Data & Analytics**

    ![](./media/image10.png)

1.  In the search bar, enter `db2 wa`

![](./media/image11.png)

1.  Click on the **Db2 Warehouse** icon. Go ahead and explore the page for the different plans. We will stay with **Entry** and our DB is only 50 rows, so we will be within the 1GB limit. Change the **Service name** if you desire, or leave is as the default unique name. we hanged it to **Db2Warehouse-Empl**. Click **Create**.

    ***Note***: The entry version is a fully managed data base which doesn’t have full self-serve DBA function. If you need this, move to a Flex or other plan.

    ![](./media/image12.png)

1.  Once created, there is a list to the left. **Manage, Service credentials** and **Connections**. To the right is an **Open** button which leads to the **Administration** **console**.

    ![](./media/image13.png)

1.  Click on **Service credentials** then the **New credentials** button then **Add**, so that password can be generated for you. Take note of how to get this information, so we can use it in the next step.

1.  Click on **View credentials** and see the vital connection information, which will be used in the next section.

    1.  Host name
    1.  Port number
    1.  Data base name
    1.  User name
    1.  Password

        ![](./media/image14.png)

1.  Click on the **Manage** link to the left. Then click **Open** to view the administration console

1.  Once the console is open. you can see across the top: Storage used, Load data, Explore data, run SQL queries.

    ![](./media/image15.png)

## Part 1: Execute the Application Locally in WebSphere Liberty

In this section, you’ll create a WebSphere Liberty application server on the local machine and execute your application, which will remotely access a DB2 database running in the Db2 Warehouse on IBM Cloud.

1.  Open terminal, cd to the `{LAB_HOME}/wlp/bin` directory and create a Liberty server using the command

    `server create labServer`

2.  Copy the supplied server.xml file from the `{LAB_HOME}/labs/cloud/6_CaaSandDb2Warehouse` directory to the newly created server’s directory.

    `cp {LAB_HOME}/labs/cloud/6_CaaSandDb2Warehouse/server.xml {LAB_HOME)/wlp/usr/servers/labServer`

3.  Edit the server.xml file, look for the `properties.db2.jcc` xml tag. Change the elements. then **Save** the server.xml file.

    a.  **serverName** with **hostname**

    b.  **user** with **user name**

    c.  **password** with **password**

    d.  **port** should be the same

    e.  **database** name should be the same

4.  Copy the EmployeeApp.war file again from the {LAB\_HOME}/labs/cloud/6\_CaaSandDb2Warehouse to the apps directory of the app server just created to complete the deployment of the EmployeeApp application in Liberty.

    `cp {LAB_HOME}/labs/cloud/6_CaaSandDb2Warehouse/EmployeeApp.war {LAB_HOME}/wlp/usr/servers/labServer/apps`

5.  Start the liberty server using the command

    `server run labServer`

6.  Your Liberty server should be up and running with the EmployeeApp application.

7.  Launch the application using the URL <http://localhost:9080/EmployeeApp>

  ![pic7](./media/image16.png)

1.  Explore and experiment with the EmployeeApp application demonstrating CRUD operations on a Db2 Warehouse from a servlet.

  1.  Click on the **PopulateEmpoyeeTable** button first to populate the database.

  1. Click on the **GetEmployeeList**  button to list all the employees in the database.
    ![](./media/image17.png)
    ![](./media/image18.png)

  1.  Add an employee by clicking on the **AddEmployee** button then click **Add**
    ![](./media/image19.png)
    ![](./media/image20.png)

  1.  Check the added employee information by clicking on the GetEmployee button.
    ![](./media/image21.png)
    ![](./media/image22.png)

  1.  Update the employee information by clicking on the **UpdateEmployee** button then the **Update** button
    ![](./media/image23.png)
    ![](./media/image24.png)
  1.  You may check the employee again by clicking on the **GetEmployee** button then the **Get** button
     ![](./media/image25.png)
     ![](./media/image26.png)
  1.  Delete the added employee by clicking on the **DeleteEmployee** button then the **Delete** button
     ![](./media/image27.png)
     ![](./media/image28.png)
  1.  You can confirm the deletion by trying to access the same employee in the data base. Click the **Get** button
     ![](./media/image29.png)
     ![](./media/image30.png)
     ![](./media/image31.png)

1.  There should not be any unexpected error message in the liberty server’s log file. Take a look at the `{LAB_HOME}/wlp/usr/servers/labServer/logs/console.log` file. All the messages logged there will also appear in the console window where labServer is running.

1.  Stop the liberty server using Ctrl+C from the same Ubuntu terminal from where you started the Liberty application server.

You have successfully deployed, started and explored the EmployeeApp application running on the Liberty Server running in a local machine.

## Part 2:  Execute the Application Locally in Dockerized Liberty

Now you’ll run your Liberty server as a Docker container packaging it with the WebSphere Liberty image that is available from the online Docker Hub repository of images.
1.  Move to the `{LAB\_HOME}/labs/cloud/6\_CaaSandDb2Warehouse` folder.

  `cd {LAB\_HOME}/labs/cloud/6\_CaaSandDb2Warehouse`

1.  Build a new Docker image containing the EmployeeApp application.

  `docker build -t emp .`

  ***Note:*** Image building in the machine can be very fast, if the required components have already been downloaded and are cached in the machine.
    ~~~~
    docker build -t emp .

    Sending build context to Docker daemon  24.03MB
    Step 1/4 : FROM websphere-liberty
     ---> f55609fb2492
    Step 2/4 : COPY EmployeeApp.war /opt/ibm/wlp/usr/servers/defaultServer/apps/
     ---> a47b597a34fa
    Step 3/4 : COPY server.xml /opt/ibm/wlp/usr/servers/defaultServer/
     ---> 1a2514ef97fd
    Step 4/4 : COPY db2*jar /opt/ibm/wlp/usr/shared/resources/DB2Libs/
     ---> 7278aa886b34
    Successfully built 7278aa886b34
    Successfully tagged emp:latest
    ~~~~
1.  Run the Dockerized Liberty using the image that you created in step 3.

    `docker run -d -p 9080:9080 --name emp --hostname emp emp`

1.  Check the Docker container log which should clearly show that the EmployeeApp application is up and running. In general, a Docker container log can provide valuable troubleshooting information.

    `docker logs --tail=all -f emp`

3.  Now you are ready to launch the EmployeeApp application. <http://localhost:9080/EmployeeApp/>

 ![](./media/image32.png)

 ![](./media/image33.png)

1.  Explore the EmployeeApp application as you did earlier in Section 2.

2.  During the process of trying out the EmployeeApp application, no unexpected error messages should appear in the container logs – refer to step 5.

3.  Finally, to get ready for the next section of the lab, from another Ubuntu terminal, run the following commands to stop and remove the Docker container.
~~~~
$ docker stop emp
$ docker rm emp
~~~~

You have successfully built and tested a Liberty Docker image with the EmployeeApp which accessed a Db2 Warehouse on the IBM Cloud.

## Part 3: Execute the Application in a Container in IBM Bluemix

In this section, you will deploy the same **EmployeeApp** application in Docker containers on IBM Cloud, demonstrating the portability of your application from your machine to the IBM Cloud Container Service. When you place a Docker image into the **IBM Cloud Container Registry** and deploy it in IBM Cloud, it starts its life as a key component of **IBM Cloud Container Service** having many extra qualities over vanilla Docker containers as discussed in Section 1. ***There is no change in to run this on bare metal, local Docker environment or IBM Container Service using Db2 Warehouse on IBM Cloud.***

In this section, you will mainly use CLIs to interact with IBM Cloud and IBM Cloud Container Service. For working with command line, you need to download and install a few Cloud plugins. Once these plugins are available, you can work from any. If you have run the setup lab to completion, you should have most of this already installed, apikey already created and ready to go.

1.  Set the endpoint you desire to point to.  For the example syntax, **US South** or **ng** will be used.

    `bx api <https://api.ng.bluemix.net/>`

    For other regions of the world:
    ~~~~
    bx api <https://api.eu-gb.bluemix.net/>
    bx api https://api.us-east.bluemix.net/
    bx api <https://api.eu-de.bluemix.net/>
    bx api <https://api.au-syd.bluemix.net/>
    ~~~~
1. Logon to your IBM Cloud region (US South in this case) using the ***IBM Cloud (bx) plugin***  in a new terminal, use the following command:

  `bx login –u <Your IBM Cloud username> --apikey @{LAB_HOME}/Liberty-APIKey.json`
  
    ~~~~
    API endpoint: https://api.ng.bluemix.net
    Authenticating...
    OK
    Targeted account <your account> Account (eeee7d19b6701916e21bf02f116813a9)
    Targeted org <Your org (email if non subscribed account)>
    Targeted space LAB_SPACE
    API endpoint:   https://api.ng.IBM Cloud.net (API version: 2.75.0)   
    Region:         us-south   
    User:           <your user email>
    Account:        <your account> Account (eeee7d19b6701916e21bf02f116813a9)   
    Org:            <Your org (email if non subscribed account)>
    Space:          LAB_SPACE   
    ~~~~ 
   
1.  Just as a pro-active work, try to update all the installed IBM Cloud (bx) CLI plug-ins. If any plug-in has been updated, the new images will be pulled in and the relevant plug-ins will get updated.

    `bx plugin update -all -r Bluemix`

2.  Initialize the IBM Cloud container service plug-in and logon to the IBM Cloud container registry service. Execute the following commands

    `bx cs init`
    
     ~~~~
     bx cs init
     Using default API endpoint: https://containers.bluemix.net
     OK
     ~~~~

1.  If you do not have a namespace in IBM Cloud Container registry, you need to set one by using the following command

    `bx cr namespace-add <your namespace name>`

    ***Note:*** You can use any namespace name between 4 and 30 characters. Only lowercase and underscore characters are permitted in forming namespace names.***

    We have used **libertypot** as the name space in all the relevant commands for this specific run of the lab.

     ~~~~
     bx cr namespace-add libertypot
     Adding namespace 'libertypot'...

     Successfully added namespace 'libertypot'

     OK
     ~~~~

1.  You have to build and push a Docker image for the application.

    1. Change to the build directory
      `cd  {LAB_HOME}/labs/cloud/6_CaaSandDb2Warehouse directory`
      
    1. Build the container
      `docker build –t emp .`

      ~~~~
      docker build -t emp .
      Sending build context to Docker daemon  24.03MB
      Step 1/4 : FROM websphere-liberty
      ---> f55609fb2492
      Step 2/4 : COPY EmployeeApp.war /opt/ibm/wlp/usr/servers/defaultServer/apps/
       ---> Using cache
       ---> a47b597a34fa
      Step 3/4 : COPY server.xml /opt/ibm/wlp/usr/servers/defaultServer/
       ---> Using cache
       ---> 1a2514ef97fd
      Step 4/4 : COPY db2*jar /opt/ibm/wlp/usr/shared/resources/DB2Libs/
       ---> Using cache
       ---> 7278aa886b34
      Successfully built 7278aa886b34
      Successfully tagged emp:latest
      ~~~~

1.  Tag the created image with the namespace and registry information . Registry URL can be found in Step 3

    `docker tag emp registry.ng.bluemix.net/<your name space>/emp`

    Optionally, you can also check the creation of the tagged image by issuing
    `docker images | grep emp` command.
    
      ~~~~
      $ docker tag emp regis-try.ng.bluemix.net/libertypot/emp
      $ docker images | grep emp
      emp                                        latest            7278aa886b34    19 hours ago        435MB
      registry.ng.bluemix.net/libertypot/emp     latest            7278aa886b34    19 hours ago        435MB
      ~~~~
1.  Push the just created and tagged image to your namespace in the Bluemix container registry by issuing the following command.
    `docker push registry.eu-de.bluemix.net//<your name space>/emp`

     ~~~~
     docker push registry.ng.bluemix.net/libertypot/emp
     The push refers to repository [regis-try.ng.bluemix.net/libertypot/emp]
     2441614a498a: Pushed
     ee7ea7cbd58f: Pushed
     7ed02c9d49c6: Pushed
     781354fd50a4: Pushed
     7e1504ddbdfa: Pushed
     a97f5ee8fd60: Pushing [=================================================> ]  74.79MB/75.09MB
     a97f5ee8fd60: Pushed
     66a59718a073: Pushed
     9879d2f7f51e: Pushed
     ae273540fc0d: Pushed
     6e466f97b365: Pushed
     1850bb064017: Pushed
     1b83e56c61e4: Pushing [===================>                               ]  72.44MB/185.4MB
     1b83e56c61e4: Pushing [===========================>                       ]    103MB/185.4MB
     f17fc24fb8d0: Pushed
     6458f770d435: Pushed
     latest: digest: sha256:e9fdd8cc1bf35e1dd1d49fab57be428c99cd867d5163dd6a2b25f1c80c180c9a size: 4289
     ~~~~

It may take a few minutes for the push operation to complete. **Make sure** that the image indeed got pushed to IBM Cloud Container Registry. As shown here, at the completion of the push, you should see the ‘digest’ information of the image stored in IBM IBM Cloud Container Registry.

Note: If because of networking issues the push fails to complete (in isolated instances you may receive “unauthorized” or some other security errors), try the push again. The layers which have been already pushed to the Bluemix container registry will not be pushed again.

1.  Check the pushed image in the Bluemix container registry by issuing the following command.

    `bx cr images`

     ~~~~
     bx cr images
     Listing images...

     REPOSITORY                               NAMESPACE    TAG      DIGEST         CREATED         SIZE     VULNERABILITY STA-TUS   
     registry.ng.bluemix.net/libertypot/emp   libertypot   lat-est   ccc3d25ba41c   2 minutes ago   279 MB   Pending Scan   
     ~~~~

1.  After about 3-5 minutes, the IBM Container Service will run a vulnerability scan on the container. Good luck If you see that the image is vulnerable, run the following command to find out what is wrong.

     ~~~~
     bx cr va registry.ng.bluemix.net/libertypot/emp:latest
     Checking vulnerabilities for 'regis-try.ng.bluemix.net/libertypot/emp:latest'...

     Image 'registry.ng.bluemix.net/libertypot/emp:latest' was last scanned on Tue Feb 13 20:36:01 UTC 2018.
     The scan results show the image should be deployed with CAUTION.

     VULNERABLE PACKAGES FOUND
     =========================

     PACKAGE   VULNERABILITIES   CORRECTIVE ACTION   
     systemd   1                 Upgrade to systemd 229-4ubuntu21.1   

     To see the details about the fixes for these packages, run the command again with the '--extended' flag.

     CONFIGURATION ISSUES FOUND
     ==========================

     SECURITY PRACTICE                                                     CORRECTIVE ACTION   
     PASS_MAX_DAYS must be set to 90 days                                  Maximum password age must be set to 90 days.   
     Minimum password length not specified in /etc/pam.d/common-password   Minimum password length must be 8.   
     PASS_MIN_DAYS must be set to 1                                        Minimum days that must elapse between user-initiated pass-word changes should be 1.   
     File /var/log/faillog does not exist                                  Permission check of /var/log/faillog   
     file /var/log/faillog must exist if pam_tally2.so is not being used   faillog file checking   

     OK
     ~~~~

1.  Based on this report, we can work to fix the vulnerabilities, by adding lines to the Dockerfile or we can change the underlying Liberty/OS layer to use the Public version in IBM Cloud Public Registry.

1.  Edit the Dockerfile in the current directory. Switch the # symbol so the `FROM registry.ng.bluemix.net/ibmliberty:latest` is being pulled as the Liberty layer.
    ~~~~
    FROM websphere-liberty:latest
    #FROM registry.ng.bluemix.net/ibmliberty:latest
    ~~~~
1.  Remove the current instance, the build then new instance, tag, push then verify that instance to the IBM Cloud Container Registry.

    1. `docker rmi registry.ng.bluemix.net/libertypot/emp`
    1. `docker build -t emp .`
    1. `docker tag emp registry.ng.bluemix.net/libertypot/emp`
    1. `docker push registry.ng.bluemix.net/libertypot/emp`
    1. `bx cr images`
    1. `bx cr va registry.ng.bluemix.net/libertypot/emp:latest`

The IBM teams go to great lengths to verify that their images are properly secured.
~~~~
bx cr images
Listing images...

REPOSITORY                               NAMESPACE    TAG      DIGEST         CREATED         SIZE     VUL-NERABILITY STATUS   
registry.ng.bluemix.net/libertypot/emp   libertypot   latest   daefc43c8f12   7 minutes ago   288 MB   OK   

OK
~~~~


1.  By this time the creation of the Kubernetes cluster that you initiated at Section 2 must be complete.

    1.  To look at the containers, you may click on the ![](./media/image4.png) symbol at top left corner

 ![](./media/image34.png)

1.  Click on the **Containers** link.

    ![](./media/image35.png)

1.  The resulting panel should show the created container to be in the **Ready** state.

    ![](./media/image36.png)

1.  An optional click on the created container will display the details of the cluster in a nice graphical form.

    ![](./media/image37.png)


1.  To work with the Kubernetes commands on a Kubernetes cluster, you need the kubectl CLI. This should have been installed during section 2.

    a.  From a command line, type kubectl version –short command, it will only show the client version, as shown here, since the kubectl CLI is not yet connected to the Kubernetes cluster running in IBM Bluemix.

    ~~~~
    kubectl version --short
    Client Version: v1.9.3
    The connection to the server localhost:8080
    ~~~~

    **Note**: Ignore the complaint about the connection. Kubernettes is not installed or running on your machine.

1.  For connecting to the cluster running in IBM Cloud Container Service, you need to ‘context’/’source’ the terminal.

    To source a terminal, you need to know the name of your Kubernetes cluster. You may get the name of your Kubernetes cluster from command line by issuing the `bx cs clusters` command. This should be the same as the one in the IBM Cloud UI.

    ~~~~
    bx cs clusters
    OK
    Name                   ID                                 State    Created        Workers   Location   Version   
    EmployeeCluster-inst   80eab4814e854982a990e4fb8f61ae23   normal   23 hours ago   1         hou02      1.8.6_1505   
    ~~~~

1.  With the intended cluster name get the environment variable configuration by issuing the `bx cs cluster-config <cluster name>` command.

~~~~
bx cs cluster-config EmployeeCluster-inst
OK
The configuration for EmployeeCluster-inst was downloaded successfully. Export environment variables to start using Kubernetes.

export KUBECONFIG=/Users/tjmcmanus/.bluemix/plugins/container-service/clusters/EmployeeCluster-inst/kube-config-hou02-EmployeeCluster-inst.yml
~~~~

1.  For sourcing a terminal, you need to copy that generated `export KUBECONFIG= …` command in its entirety, paste in a terminal and execute it.

   **export KUBECONFIG=/Users/tjmcmanus/.bluemix/plugins/container-service/clusters/EmployeeCluster-inst/kube-config-hou02-EmployeeCluster-inst.yml**

  `$ export KUBECONFIG=/Users/<username>/.bluemix/plugins/container-service/clusters/EmployeeCluster-inst/kube-config-hou02-EmployeeCluster-inst.yml`


***Note:*** Once a terminal is sourced, you can cd to any directory in it without affecting its sourcing.

1.  Kubernetes provides a nice GUI for deploying, administering and monitoring various Kubernetes resources. Though all the interactions can be achieved from the kubectl CLI, it is nice to have the Kubernetes GUI also for visualization and administration.

    1.  From a sourced terminal, issue `kubectl proxy`

      ~~~~
      $ kubectl proxy
      Starting to serve on 127.0.0.1:8001
      ~~~~
1.  In a browser, browse to <http://127.0.0.1:8001/ui> to launch the Kubernetes UI

    ![](./media/image38.png)

    This might ask you to sign in. If so, view the **KUBECONFG** defined `.yml` file for the **id-token**. Copy the id-token value and paste in the UI after selecting **Token**. Once you do this, it will save the value and you will not need to log in again. For example:
    ~~~~
    user:

    auth-provider:

    name: oidc

    config:

    client-id: bx

    client-secret: bx

    id-token: **eyJraWQiOiIyMDE3MTAzMC0wMDowMDowMCIsImFsZyI6IlJTMjU2In0.eyJpYW1faWQiOiJJQk1pZC0xMDAwMDBBODRHIiwiaXNzIjoiaHR0cHM6Ly9pYW0ubmcuYmx1ZW1peC5uZXQva3ViZXJuZXRlcyIsInN1YiI6Im1hY3RvbUB1cy5pYm0uY29tIiwiYXVkIjoiYngiLCJleHAiOjE1MTg2MTgzMDIsImlhdCI6MTUxODYxNDcwMn0.f7dghYXhJj0YrEwVb\_42FMTSS2RP7d97Qv6dT7YIFt6Q4-IjaXRfWiCztkYdQMwJk04OlpuvRXyQeEGW8TDj2O\_kzvI-KlsFWdZRxaSujF8TI0SkOt2w1eVn3GnqEwmQpGSTI2aeeXgMSPU5bQw3V3t9hi4gCKJrb3tFQ2DYPVv9\_TWCRG48stJTrt06\_OQuNqGOAq4qdOY115ZWjOGq94hqBSf1rs\_2d0M9AJnHMWC5k7wpu3mtGf3q86wx2G1kowChbexGDOaI9Sx3pQzDtdUHHxLLYpBXe\_PlG3gh8AwxwIq4z2ukbfGVkAEPSMzvak0nUHzbngqA4FgWlIazDg**

    idp-issuer-url: https://iam.ng.bluemix.net/kubernetes
    ~~~~
1.  You may want to iconize the terminal window of step 8a now and keep the Kubernetes UI running.

1.  You will deploy the Docker image that you created and placed in the IBM CLoud Container Registry in Step` 5 from the `kubectl` CLI using an `yml` file. The machine, has the `emp-app.yml file in the `{LAB_HOME}/labs/cloud/6_CaaSandDb2Warehouse` directory.
     ~~~~
     $ kubectl create -f emp-app.yml
     deployment "emp-deployment" created
     service "emp-service" created
     ~~~~

1.  A click to the **Deployments** link of the K8s UI

    ![](./media/image39.png)

    and a refresh of the screen should show the new `emp-deployment` deployment.

    ![](./media/image40.png)

1.  Similarly, a click on the **Services** link

    ![](./media/image41.png)

    followed by a screen refresh should show the just created `emp-service` service.

    ![](./media/image42.png)



1.  Now it is time to try out the newly deployed application in IBM Bluemix K8s cluster.

    1.  Get the public ip address of the lone worker node of the cluster by issuing the following command from any terminal in your machine

        `bx cs workers <cluster name>`
        ~~~~
        bx cs workers EmployeeCluster-inst
        OK
        ID                                                 Public IP        Private IP     Machine Type   State    Status   Zone    Version   
        kube-hou02-pa80eab4814e854982a990e4fb8f61ae23-w1   184.172.214.41   10.47.84.195   free           normal   Ready    hou02   1.8.6_1506   
        ~~~~

Note down the Public IP address ______________ of the worker node. Above would be ***184.172.214.41***

1.  In a browser from your machine or from anywhere else including your laptop, access the application using the `http://<worker node pubic-ip-address>:30191/EmployeeApp`.

    ![](./media/image43.png)

1. Try out the application by getting the list of employees, doing some other CRUD operations on the Db2 Warehouse on IBM Cloud data base.
    ![](./media/image44.png)

1. The application should behave identically illustrating the success of the **lift-and-shift** exercise that you just completed using a container.

1.  In the K8s UI, a click on the **Pods** link, will show the ***CPU*** and ***Memory*** usage of the Pod hosting the lone container.

    ![](./media/image45.png)

1.  A click on the ![](./media/image46.png) icon of the lone container of the pod will show the container log.

    ![](./media/image47.png)

1.  You can explore other resources in the K8s UI.

### Part 4: Scale the Containerized Application in IBM Cloud

Kubernetes by default provide auto recoverability of the pods and hence containers. It is also very easy to scale deployments in a K8s environment. In this section, you will try out some scalability and recoverability aspects of K8s deployments.

1.  You can scale a deployment is many ways: using commands from the kubectl CLI, from K8s UI or by editing the K8s yml file dynamically from the kubectl CLI. For simplicity and convenience, we will use kubectl command for scaling.

    1.  From a sourced terminal in your machine, scale the number of pods of the emp-deployment deployment up to 3 by issuing the command

        `kubectl scale --replicas=3 deployment/emp-deployment`
      ~~~~
      $ kubectl scale --replicas=3 deployment/emp-deployment

       deployment "emp-deployment" scaled
      ~~~~

1.  On a navigation to the **Pods** link of the K8s GUI with a possible screen refresh, should display the three pods for the `emp-deployment` deployment.

    ![](./media/image48.png)

    ***Note:*** The top and bottom pods in the UI got added because of the scale up operations.

1.  Continue to access and use the application in an identical way as you did in Section 4: browsing to `http://<worker node ip-address>:30191/EmployeeApp` in a browser. K8s by default will distribute the input requests to all the three pods in a round-robin fashion.
    ![](./media/image49.png)

    1. Add an employee
    ![](./media/image50.png)
    ![](./media/image51.png)

    1. Verify the addition
    ![](./media/image52.png)
    ![](./media/image53.png)

    1. All the three pods should show roughly similar CPU utilizations in the K8s GUI. ***Note:*** You may need to refresh your browser.
    ![](./media/image54.png)

1.  You can scale down a deployment in the same way as you scaled up.

    a.  From a sourced terminal in your machine, scale down the number of pods of the emp-deployment deployment to 2 by issuing the command

        `kubectl scale --replicas=2 deployment/emp-deployment`

    ~~~~
    $ kubectl scale --replicas=2
    deployment/emp-deployment
    deployment "emp-deployment" scaled
    ~~~~

1.  On a navigation to the **Pods** link of the K8s GUI with a possible refresh, should display two pods for the emp-deployment deployment.

    ![](./media/image55.png)

    Or from a sourced terminal

    ~~~~
    kubectl get pods
    NAME                              READY     STATUS    RESTARTS   AGE
    emp-deployment-5457759b9d-8482k   1/1       Running   0          17m
    emp-deployment-5457759b9d-z57tk   1/1       Running   0          6m
    ~~~~

1.  You may perform CRUD operations on the database using the EmployeeApp application during and after the scaling down process. To the users of the application, all the scaling operations should be functionally transparent[^6].

    ![](./media/image56.png)

    ![](./media/image57.png)

    ![](./media/image58.png)

    ![](./media/image59.png)

    ![](./media/image60.png)

1.  You can try out the K8s high availability by bringing down a pod from the K8s GUI.

    1.  In the K8s UI, after navigating to the **Pods** link, click on the extreme right icon of a pod as shown.

        ![](./media/image61.png)

    1.  Click on **Delete**.

        ![](./media/image62.png)

        and confirm the **Delete a Pod** dialogue

        ![](./media/image63.png)

    1.  K8s should start bringing down the requested pod and you will see it is immediately starting a new one to maintain the desired number of replicas.

        ![](./media/image64.png)

        After a few seconds, you should see only two pods – the last two of the above picture.

        ![](./media/image65.png)

    1.  The state of the pods can also be monitored from a sourced terminal using `kubectl`.

    ~~~~
    $ kubectl get pods
    NAME                              READY     STATUS    RE-STARTS   AGE
    emp-deployment-5457759b9d-8482k   1/1       Running   0          18m
    emp-deployment-5457759b9d-z57tk   1/1       Running   0          8m     <-

    $ kubectl get pods
    NAME                              READY     STATUS    RE-STARTS   AGE
    emp-deployment-5457759b9d-8482k   1/1       Running   0          19m
    emp-deployment-5457759b9d-mqqn7   1/1       Running   0          24s    <-
    ~~~~

1.  You can try out the EmployeeApp application from a browser during this pod recoverability exercise. There should not be any effect on the application.

## Cleanup

In this section, you will perform some cleanup operations.

1.  In a sourced Ubuntu terminal, cd to the `{LAB_HOME}/labs/cloud/6_CaaSandDb2Warehouse` (or provide the full path name of the file) and execute

`kubectl delete –f emp-app.yml`

to delete all the K8s resources created in the lab.
~~~~
$ kubectl delete -f emp-app.yml
deployment "emp-deployment" deleted
service "emp-service" deleted
~~~~

Soon the K8s UI will return to its pristine condition with no resources.

![](./media/image66.png)

1.  To conserve resources, you may want to delete the emp image from your namespace in the Bluemix repository by issuing the following command from a terminal in your Skytap machine.

    `bx cr image-rm registry.ng.bluemix.net/<your namespace>/emp`

    ~~~~
    $ bx cr image-rm registry.ng.bluemix.net/libertypot/emp
    Deleting image 'registry.ng.bluemix.net/libertypot/emp'...

    Successfully deleted image 'sha256:daefc43c8f120079b90b37aa49ca942ffa617dc16f797116c242d951ce7a6c43'

    OK
    ~~~~

1.  Again, to conserve resources, you may also want to delete the created cluster from the Bluemix console

    ![](./media/image67.png)

    or from any terminal in your machine by issuing

  `  bx cs cluster-rm <your cluster name>`
