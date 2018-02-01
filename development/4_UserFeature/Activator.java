package labbundle;

import java.rmi.RemoteException;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.ibm.websphere.security.CustomRegistryException;
import com.ibm.websphere.security.UserRegistry;



public class Activator extends FileRegistrySample implements BundleActivator {

	private String usersFile = "{LAB_HOME}\\labs\\development\\4_Extension_<timestamp>\\users.props";
	private String groupsFile = "{LAB_HOME}\\labs\\development\\4_Extension_<timestamp>\\groups.props";
	private ServiceRegistration<UserRegistry> curRef = null;


	public Activator() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Properties initprops = new Properties();
		initprops.put("usersFile", usersFile);
		initprops.put("groupsFile", groupsFile);
		try {
					this.initialize(initprops);
		} catch (CustomRegistryException e) {
					e.printStackTrace();
		}


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

	}

}
