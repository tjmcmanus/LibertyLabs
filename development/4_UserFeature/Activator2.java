/*
 * COPYRIGHT LICENSE: This information contains sample code provided in source
 * code form. You may copy, modify, and distribute these sample programs in any
 * form without payment to IBM for the purposes of developing, using, marketing
 * or distributing application programs conforming to the application programming
 * interface for the operating platform for which the sample code is written.
 * Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE
 * ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED,
 * INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF
 * MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE,
 * AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE. IBM HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE
 * SAMPLE SOURCE CODE.
 *
 * (C) Copyright IBM Corp. 2013.
 * All Rights Reserved. Licensed Materials - Property of IBM.
*/

package labbundle;

import java.rmi.RemoteException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import com.ibm.websphere.security.CustomRegistryException;
import com.ibm.websphere.security.UserRegistry;

import labapi.MyRegistry;

public class Activator extends FileRegistrySample implements BundleActivator, ManagedService {
	public Activator() throws RemoteException {
		super();
	}

	private String usersFile = null;
	private String groupsFile = null;
	private ServiceRegistration<UserRegistry> curRef = null;
	private ServiceRegistration<UserRegistry> curRefMyRegistry = null;
	private String CFG_PID = "customUserRegistry";
	private ServiceRegistration<ManagedService> mgdSvc = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		// register as a UserRegistry service type in start()
		Hashtable registryProperties = new Hashtable();
		FileRegistrySample registry = new FileRegistrySample();
		
		
		curRef = context.registerService(
						UserRegistry.class,
						registry,
						registryProperties);
		
		registryProperties.put("osgi.jndi.service.name", "myRegistry");
		curRefMyRegistry = context.registerService(
			    MyRegistry.class,
				registry,
				registryProperties);
		
		Hashtable serviceProperties = new Hashtable();
		serviceProperties.put(org.osgi.framework.Constants.SERVICE_PID, CFG_PID);
		mgdSvc = context.registerService(ManagedService.class,this,
		serviceProperties);	
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		curRef.unregister();
		curRefMyRegistry.unregister();
		mgdSvc.unregister();
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		// keys for user configuration
		String usersFileAttr = "usersFile";
		String groupsFileAttr = "groupsFile";

		System.out.println("Updating configuration properties");
		if (properties != null)
		{
			usersFile = (String) properties.get(usersFileAttr);
			System.out.println("UsersFile: " + usersFile);
			groupsFile = (String) properties.get(groupsFileAttr);
			System.out.println("GroupsFile: "+groupsFile);
		}

		Properties initprops = new Properties();
		initprops.put("usersFile", usersFile);
		initprops.put("groupsFile", groupsFile);
		try {
			this.initialize(initprops);
		} catch (CustomRegistryException e) {
			e.printStackTrace();
		}
	}

}

