package labbundle;

import java.rmi.RemoteException;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.ibm.websphere.security.CustomRegistryException;
import com.ibm.websphere.security.UserRegistry;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import java.util.Hashtable;


public class Activator extends FileRegistrySample implements BundleActivator, ManagedService {
	
	private String usersFile = null; // "C:\\USB_KEY\\labs\\development\\4_Extension\\users.props";
	private String groupsFile = null; // "C:\\USB_KEY\\labs\\development\\4_Extension\\groups.props"; 
	private ServiceRegistration<UserRegistry> curRef = null;
	private String CFG_PID = "customUserRegistry";
	private ServiceRegistration<ManagedService> mgdSvc = null;
	
	public Activator() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		/*Properties initprops = new Properties();
		initprops.put("usersFile", usersFile);
		initprops.put("groupsFile", groupsFile);
		try {
					this.initialize(initprops);
		} catch (CustomRegistryException e) {
					e.printStackTrace();
		}	*/
		
		// Register as a managed service
		Hashtable serviceProperties = new Hashtable();
		serviceProperties.put(org.osgi.framework.Constants.SERVICE_PID, CFG_PID);
		mgdSvc = context.registerService(ManagedService.class,this, serviceProperties);

		// register as a UserRegistry service type in start()
		curRef = context.registerService(
						UserRegistry.class,
						this,
						null);


	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		// deregister the implementation in stop()
		curRef.unregister();
		mgdSvc.unregister();

	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		String usersFileAttr = "usersFile";
		String groupsFileAttr = "groupsFile";

		System.out.println("Updating configuration properties");

		if (properties != null) {
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

