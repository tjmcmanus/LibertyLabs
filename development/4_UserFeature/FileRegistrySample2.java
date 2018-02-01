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

//The user and group information required by this sample is contained in the users.props file and groups.props file files.

//Attention: The samples that are provided are intended to familiarize you with this feature. Do not use these samples in an actual production environment.

package labbundle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import labapi.MyRegistry;

import com.ibm.websphere.security.CertificateMapFailedException;
import com.ibm.websphere.security.CertificateMapNotSupportedException;
import com.ibm.websphere.security.CustomRegistryException;
import com.ibm.websphere.security.EntryNotFoundException;
import com.ibm.websphere.security.NotImplementedException;
import com.ibm.websphere.security.PasswordCheckFailedException;
import com.ibm.websphere.security.Result;
import com.ibm.websphere.security.UserRegistry;
import com.ibm.websphere.security.cred.WSCredential;


/**
 *  The main purpose of this sample is to demonstrate the use of the
 *  custom user registry feature available in WebSphere Application Server. This 
 *  sample is a file-based registry sample where the users and the groups  
 *  information is listed in files (users.props and groups.props). As such 
 *  simplicity and not the performance was a major factor. This 
 *  sample should be used only to get familiarized with this feature. An 
 *  actual implementation of a realistic registry should consider various 
 *  factors like performance, scalability, thread safety, and so on.
 **/
public class FileRegistrySample implements UserRegistry, MyRegistry {

   private static String USERFILENAME = null;        
   private static String GROUPFILENAME = null;

   /** Default Constructor **/
   public FileRegistrySample() throws java.rmi.RemoteException {
   }

  /**
   * Initializes the registry. This method is called when creating the
   * registry.
   *
   * @param     props - The registry-specific properties with which to
   *                    initialize the custom registry
   * @exception CustomRegistryException
   *                    if there is any registry-specific problem
   **/
  public void initialize(java.util.Properties props)
         throws CustomRegistryException {
      try {
    
         /* try getting the USERFILENAME and the GROUPFILENAME from 
          * properties that are passed in (For example, from the 
          * administrative console). Set these values in the administrative 
          * console. Go to the special custom settings in the custom 
          * user registry section of the Authentication panel.
          * For example: 
          * usersFile   c:/temp/users.props
          * groupsFile  c:/temp/groups.props
          */
          if (props != null) {
             USERFILENAME = props.getProperty("usersFile");
             GROUPFILENAME = props.getProperty("groupsFile");
          }

      } catch(Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      }

      if (USERFILENAME == null || GROUPFILENAME == null) {
         throw new CustomRegistryException("users/groups information missing");
      }

   }

  /**
   * Checks the password of the user. This method is called to authenticate
   * a user when the user's name and password are given.
   *
   * @param  userSecurityName the name of user
   * @param  password the password of the user
   * @return a valid userSecurityName. Normally this is
   *         the name of same user whose password was checked 
   *         but if the implementation wants to return any other
   *         valid userSecurityName in the registry it can do so
   * @exception CheckPasswordFailedException if userSecurityName/
   *            password combination does not exist 
   *            in the registry
   * @exception CustomRegistryException if there is any registry-
   *            specific problem
   **/
   public String checkPassword(String userSecurityName, String passwd) 
      throws PasswordCheckFailedException,
             CustomRegistryException {
      String s,userName = null;
      BufferedReader in = null;
    
      try {
         in = fileOpen(USERFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               int index1 = s.indexOf(":",index+1);
               // check if the userSecurityName:passwd combination exists
               if ((s.substring(0,index)).equals(userSecurityName) && 
                       s.substring(index+1,index1).equals(passwd)) {
                  // Authentication successful, return the userID.
                  userName = userSecurityName;
                  break;
               }
            }
         }
      } catch(Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }
       

      if (userName == null) {
         throw new PasswordCheckFailedException("Password check failed for user:"  
         + userSecurityName);
      } 

      return userName;
   }

  /**
   * Maps an X.509 format certificate to a valid user in the registry.
   * This is used to map the name in the certificate supplied by a browser
   * to a valid userSecurityName in the registry
   *
   * @param     cert the X509 certificate chain
   * @return    The mapped name of the user userSecurityName
   * @exception CertificateMapNotSupportedException if the 
   *            particular certificate is not supported.
   * @exception CertificateMapFailedException if the mapping of 
   *            the certificate fails.
   * @exception CustomRegistryException if there is any registry
   *            -specific problem
   **/
   public String mapCertificate(X509Certificate[] cert)
      throws CertificateMapNotSupportedException,
             CertificateMapFailedException,
             CustomRegistryException {
      String name=null;
      X509Certificate cert1 = cert[0];
      try {
         // map the SubjectDN in the certificate to a userID.
         name = cert1.getSubjectDN().getName();
      } catch(Exception ex) {
         throw new CertificateMapNotSupportedException(ex.getMessage(),ex);
      }

      if(!isValidUser(name)) {
         throw new CertificateMapFailedException("user:"  + name 
         +  "is not valid");
      }
      return name;
   }

  /**
   * Returns the realm of the registry.
   *
   * @return the realm. The realm is a registry-specific string 
   * indicating the realm or domain for which this registry 
   * applies. For example, for OS/400 or AIX this would be 
   * the host name of the system whose user registry this 
   * object represents. If null is returned by this method,
   * realm defaults to the value of "customRealm". It is 
   * recommended that you use your own value for realm.
   * 
   * @exception CustomRegistryException if there is any registry-
   * specific problem
   **/
   public String getRealm()
      throws CustomRegistryException {
      String name = "customRealm";
      return name;
   }

  /**
   * Gets a list of users that match a pattern in the registry.
   * The maximum number of users returned is defined by the limit
   * argument.
   * This method is called by the administrative console and scripting 
   * (command line) to make the users in the registry available for 
   * adding them (users) to roles.
   *
   * @param      pattern the pattern to match. (For example, a* will 
   *             match  all userSecurityNames starting with a)
   * @param      limit the maximum number of users that should be
   *             returned. This is very useful in situations where 
   *             there are thousands of users in the registry and 
   *             getting all of them at once is not practical. The 
   *             default is 100. A value of 0 implies get all the
   *             users and hence must be used with care.
   * @return     a Result object that contains the list of users 
   *             requested and a flag to indicate if  more users
   *             exist.
   * @exception  CustomRegistryException if there is any registry-
   *             specific problem
   **/
   public Result getUsers(String pattern, int limit)
      throws CustomRegistryException {
      String s;
      BufferedReader in = null;
      List allUsers = new ArrayList();
      Result result = new Result();
      int count = 0;
      int newLimit = limit+1;
      try {
         in = fileOpen(USERFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               String user = s.substring(0,index);
               if (match(user,pattern)) {
                  allUsers.add(user);
                  if (limit !=0 && ++count == newLimit) {
                     allUsers.remove(user);
                     result.setHasMore();
                     break;
                  }
               }
            }
         }
      } catch (Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }

      result.setList(allUsers);
      return result;
   }

  /**
   * Returns the display name for the user specified by 
   *  userSecurityName.
   *
   * This method may be called only when the user information
   * is displayed (information purposes only, for example, in 
   * the administrative console) and hence not used in the actual 
   * authentication or authorization purposes. If there are no 
   * display names in the registry return null or empty string.
   *
   * In WebSphere Application Server 4.x custom registry, if you 
   * had a display name for the user and if it was different from the 
   * security name, the display name was returned for the EJB 
   * methods getCallerPrincipal() and the servlet methods
   * getUserPrincipal() and  getRemoteUser().
   * In  Version 5.x and later, for the 
   * same methods, the security name will be returned by default. 
   * This is the recommended way as the display name is not unique 
   * and might create security holes. However, for backward 
   * compatibility if you need the display name to be returned 
   * set the property WAS_UseDisplayName to true.
   *
   *See the Information Center documentation for more information.
   *
   * @param     userSecurityName the name of the user.
   * @return    the display name for the user. The display 
   *            name is a registry-specific string that 
   *            represents a descriptive, not necessarily 
   *            unique, name for a user. If a display name 
   *            does not exist return null or empty string.
   * @exception EntryNotFoundException if userSecurityName 
   *            does not exist.
   * @exception CustomRegistryException if there is any registry- 
    *           specific problem
   **/
   public String getUserDisplayName(String userSecurityName)
      throws CustomRegistryException,
             EntryNotFoundException {

      String s,displayName = null;
      BufferedReader in = null;

      if(!isValidUser(userSecurityName)) {
         EntryNotFoundException nsee = new EntryNotFoundException("user:"  
         + userSecurityName +  "is not valid");
         throw nsee;
      }

      try {
         in = fileOpen(USERFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               int index1 = s.lastIndexOf(":");
               if ((s.substring(0,index)).equals(userSecurityName)) {
                  displayName = s.substring(index1+1);
                  break;
               }
            }
         }
      } catch(Exception ex) {
         throw new CustomRegistryException(ex.getMessage(), ex);
      } finally {
         fileClose(in);
      }

      return displayName;
   }

  /**
   * Returns the unique ID for a userSecurityName. This method is called 
   * when creating a credential for a user.
   *
   * @param    userSecurityName - The name of the user.
   * @return   The unique ID of the user. The unique ID for a user 
   *           is the stringified form of some unique, registry-specific, 
   *           data that serves to represent the user. For example, for 
   *           the UNIX user registry, the unique ID for a user can be 
   *           the UID.
   * @exception EntryNotFoundException if userSecurityName does not 
   *            exist.
   * @exception CustomRegistryException if there is any registry-
   *            specific problem
   **/
   public String getUniqueUserId(String userSecurityName)
      throws CustomRegistryException, 
             EntryNotFoundException {

      String s,uniqueUsrId = null;
      BufferedReader in = null;
      try {
         in = fileOpen(USERFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               int index1 = s.indexOf(":", index+1);
               if ((s.substring(0,index)).equals(userSecurityName)) {
                  int index2 = s.indexOf(":", index1+1);
                  uniqueUsrId = s.substring(index1+1,index2);
                  break;
               }
            }
         }
      } catch(Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }

      if (uniqueUsrId == null) {
         EntryNotFoundException nsee = 
         new EntryNotFoundException("Cannot obtain uniqueId for user:"  
         + userSecurityName);
         throw nsee;
      }

      return uniqueUsrId;
   }

  /**
   * Returns the name for a user given its unique ID.
   *
   * @param      uniqueUserId  - The unique ID of the user.
   * @return     The userSecurityName of the user.
   * @exception  EntryNotFoundException if the unique user ID does not exist.
   * @exception  CustomRegistryException if there is any registry-specific
   *             problem
   **/
   public String getUserSecurityName(String uniqueUserId)
      throws CustomRegistryException,
             EntryNotFoundException {
      String s,usrSecName = null;
      BufferedReader in = null;
      try {
         in = fileOpen(USERFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               int index1 = s.indexOf(":", index+1);
               int index2 = s.indexOf(":", index1+1);
               if ((s.substring(index1+1,index2)).equals(uniqueUserId)) {
                  usrSecName = s.substring(0,index);
                  break;
               }
            }
         }
      } catch (Exception ex) {
         throw new CustomRegistryException(ex.getMessage(), ex);
      } finally {
         fileClose(in);
      }

      if (usrSecName == null) {
         EntryNotFoundException ex =
            new EntryNotFoundException("Cannot obtain the user securityName for"  + uniqueUserId);
         throw ex;
      }

      return usrSecName;

   }

  /**
   * Determines if the userSecurityName exists in the registry
   *
   * @param     userSecurityName - The name of the user
   * @return    True if the user is valid; otherwise false
   * @exception CustomRegistryException if there is any registry-
   *            specific problem
   * @exception RemoteException as this extends java.rmi.Remote 
   *            interface 
   **/
   public boolean isValidUser(String userSecurityName)
      throws CustomRegistryException {
      String s;
      boolean isValid = false;
      BufferedReader in = null;
      try {
         in = fileOpen(USERFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               if ((s.substring(0,index)).equals(userSecurityName)) {
                  isValid=true;
                  break;
               }
            }
         }
      } catch (Exception ex) {
         throw new CustomRegistryException(ex.getMessage(), ex);
      } finally {
         fileClose(in);
      }

      return isValid;
   }


  /**
   * Gets a list of groups that match a pattern in the registry
   * The maximum number of groups returned is defined by the 
   * limit argument. This method is called by administrative console
   * and scripting (command line) to make available the groups in  
   * the registry for adding them (groups) to roles.
   *
   * @param       pattern the pattern to match. (For example, a* matches 
   *              all groupSecurityNames starting with a)
   * @param       Limits the maximum number of groups to return 
   *              This is very useful in situations where there 
   *              are thousands of groups in the registry and getting all 
   *              of them at once is not practical. The default is 100. 
   *              A value of 0 implies get all the groups and hence must 
   *              be used with care.
   * @return      A Result object that contains the list of groups 
   *              requested and a flag to indicate if more groups exist.
   * @exception CustomRegistryException if there is any registry-specific
   *              problem
   **/
   public Result getGroups(String pattern, int limit)
      throws CustomRegistryException {
      String s;
      BufferedReader in = null;
      List allGroups = new ArrayList();
      Result result = new Result();
      int count = 0;
      int newLimit = limit+1;
      try {
         in = fileOpen(GROUPFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               String group = s.substring(0,index);
               if (match(group,pattern)) {
                  allGroups.add(group);
                  if (limit !=0 && ++count == newLimit) {
                     allGroups.remove(group);
                     result.setHasMore();
                     break;
                  }
               }
            }
         }
      } catch (Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }
  
      result.setList(allGroups);
      return result;
   }

  /**
   * Returns the display name for the group specified by groupSecurityName.
   * For this version of WebSphere Application Server, the only usage of  
   * this method is by the clients (administrative console and scripting)   
   * to present a descriptive name of the user if it exists.
   *
   * @param groupSecurityName the name of the group.
   * @return  the display name for the group. The display name
   *          is a registry-specific string that represents a  
   *          descriptive, not necessarily unique, name for a group.  
   *          If a display name does not exist return null or empty 
   *          string.
   * @exception EntryNotFoundException if groupSecurityName does 
   *          not exist.
   * @exception CustomRegistryException if there is any registry-
   *          specific problem
   **/
   public String getGroupDisplayName(String groupSecurityName)
      throws CustomRegistryException,
             EntryNotFoundException {
      String s,displayName = null;
      BufferedReader in = null;

      if(!isValidGroup(groupSecurityName)) {
         EntryNotFoundException nsee = new EntryNotFoundException("group:"  
         + groupSecurityName +  "is not valid");
         throw nsee;
      }
      
      try {
         in = fileOpen(GROUPFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               int index1 = s.lastIndexOf(":");
               if ((s.substring(0,index)).equals(groupSecurityName)) {
                  displayName = s.substring(index1+1);
                  break;
               }
            }
         }
      } catch(Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }
   
      return displayName;
   }

  /**
   * Returns the Unique ID for a group.

   * @param     groupSecurityName the name of the group.
   * @return    The unique ID of the group. The unique ID for
   *            a group is the stringified form of some unique,
   *            registry-specific, data that serves to represent
   *            the group. For example, for the UNIX user registry,
   *            the unique ID might be the GID.
   * @exception EntryNotFoundException if groupSecurityName does 
   *            not exist.
   * @exception CustomRegistryException if there is any registry-
   *            specific problem
   * @exception RemoteException as this extends java.rmi.Remote
   **/
   public String getUniqueGroupId(String groupSecurityName)
      throws CustomRegistryException,
             EntryNotFoundException {
      String s,uniqueGrpId = null;
      BufferedReader in = null;
      try {
         in = fileOpen(GROUPFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               int index1 = s.indexOf(":", index+1);
               if ((s.substring(0,index)).equals(groupSecurityName)) {
                  uniqueGrpId = s.substring(index+1,index1);
                  break;
               }
            }
         }
      } catch(Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }

      if (uniqueGrpId == null) {
         EntryNotFoundException nsee = 
         new EntryNotFoundException("Cannot obtain the uniqueId for group:"  
         + groupSecurityName);
         throw nsee;
      }
      
      return uniqueGrpId;
   }

  /**
   * Returns the Unique IDs for all the groups that contain the unique ID 
   * of a user. Called during creation of a user's credential.
   *
   * @param     uniqueUserId the unique ID of the user.
   * @return    A list of all the group unique IDs that the unique user 
   *            ID belongs to. The unique ID for an entry is the 
   *            stringified form of some unique, registry-specific, data 
   *            that serves to represent the entry.  For example, for the
   *            UNIX user registry, the unique ID for a group might be 
   *            the GID and the Unique ID for the user might be the UID.
   * @exception EntryNotFoundException if uniqueUserId does not exist.
   * @exception CustomRegistryException if there is any registry-specific
   *            problem
   **/
   public List getUniqueGroupIds(String uniqueUserId)
      throws CustomRegistryException,
             EntryNotFoundException {
      String s,uniqueGrpId = null;
      BufferedReader in = null;
      List uniqueGrpIds=new ArrayList();
      try {
         in = fileOpen(USERFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               int index1 = s.indexOf(":", index+1);
               int index2 = s.indexOf(":", index1+1);
               if ((s.substring(index1+1,index2)).equals(uniqueUserId)) {
                  int lastIndex = s.lastIndexOf(":");
                  String subs = s.substring(index2+1,lastIndex);
                  StringTokenizer st1 = new StringTokenizer(subs, ",");
                  while (st1.hasMoreTokens()) 
                     uniqueGrpIds.add(st1.nextToken());
                  break;
               }
            }
         }
      } catch(Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }

      return uniqueGrpIds;
   }

  /**
   * Returns the name for a group given its unique ID.
   *
   * @param     uniqueGroupId the unique ID of the group.
   * @return    The name of the group.
   * @exception EntryNotFoundException if the uniqueGroupId does 
   *            not exist.
   * @exception CustomRegistryException if there is any registry-
   *            specific problem
   **/
   public String getGroupSecurityName(String uniqueGroupId)
      throws CustomRegistryException,
             EntryNotFoundException {
      String s,grpSecName = null;
      BufferedReader in = null;
      try {
         in = fileOpen(GROUPFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               int index1 = s.indexOf(":", index+1);
               if ((s.substring(index+1,index1)).equals(uniqueGroupId)) {
                  grpSecName = s.substring(0,index);
                  break;
               }
            }
         }
      } catch (Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }

      if (grpSecName == null) {
         EntryNotFoundException ex = 
            new EntryNotFoundException("Cannot obtain the group security name for:"  + uniqueGroupId);
         throw ex;
      }

      return grpSecName;

   }

  /**
   * Determines if the groupSecurityName exists in the registry
   *
   * @param     groupSecurityName the name of the group
   * @return    True if the groups exists; otherwise false
   * @exception CustomRegistryException if there is any registry-
   *            specific problem
   **/
   public boolean isValidGroup(String groupSecurityName)
      throws CustomRegistryException {
      String s;
      boolean isValid = false;
      BufferedReader in = null;
      try {
         in = fileOpen(GROUPFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               if ((s.substring(0,index)).equals(groupSecurityName)) {
                  isValid=true;
                  break;
               }
            }
         }
      } catch (Exception ex) {
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }

      return isValid;
   }

  /**
   * Returns the securityNames of all the groups that contain the user
   *
   * This method is called by the administrative console and scripting 
   * (command line) to verify that the user entered for RunAsRole mapping  
   * belongs to that role in the roles to user mapping. Initially, the 
   * check is done to see  if the role contains the user. If the role does 
   * not contain the user explicitly, this method is called to get the groups 
   * that this user belongs to so that a check can be made on the groups that 
   * the role contains.
   *
   * @param     userSecurityName the name of the user
   * @return    A list of all the group securityNames that the user
   *            belongs to.
   * @exception EntryNotFoundException if user does not exist.
   * @exception CustomRegistryException if there is any registry-
   *            specific problem
   * @exception RemoteException as this extends the java.rmi.Remote
   *            interface 
   **/
   public List getGroupsForUser(String userName)
      throws CustomRegistryException,
             EntryNotFoundException {
      String s;
      List grpsForUser = new ArrayList();
      BufferedReader in = null;
      try {
         in = fileOpen(GROUPFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               StringTokenizer st = new StringTokenizer(s, ":");
               for (int i=0; i<2; i++)
                  st.nextToken();
               String subs = st.nextToken();
               StringTokenizer st1 = new StringTokenizer(subs, ",");
               while (st1.hasMoreTokens()) {
                  if((st1.nextToken()).equals(userName)) {
                     int index = s.indexOf(":");
                     grpsForUser.add(s.substring(0,index));
                  }
               }
            }
         }
      } catch (Exception ex) {
         if (!isValidUser(userName)) {
            throw new EntryNotFoundException("user:"  + userName  
            +  "is not valid");
         }
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }

      return grpsForUser;
   }

  /**
   * Gets a list of users in a group.  
   *
   * The maximum number of users returned is defined by the 
   * limit argument.
   *
   * This method is being used by the WebSphere Application Server 
   * Enterprise process choreographer (Enterprise) when 
   * staff assignments are modeled using groups.
   *
   * In rare situations, if you are working with a registry where  
   * getting all the users from any of your groups is not practical   
   * (for example if there are a large number of users) you can create
   * the NotImplementedException for that particular group. Make sure   
   * that if the process choreographer is installed (or if installed later) 
   * the staff assignments are not modeled using these particular groups.
   * If there is no concern about returning the users from groups 
   * in the registry it is recommended that this method be implemented
   * without creating the NotImplemented exception.
   * @param         groupSecurityName the name of the group
   * @param         Limits the maximum number of users that should be 
   *                returned. This is very useful in situations where there 
   *                are lots of users in the registry and getting all of  
   *                them at once is not practical. A value of 0 implies   
   *                get all the users and hence must be used with care. 
   * @return        A Result object that contains the list of users
   *                requested and a flag to indicate if more users exist.
   * @deprecated    This method will be deprecated in future.
   * @exception     NotImplementedException create this exception in rare 
   *                situations if it is not practical to get this information   
   *                for any of the group or groups from the registry.
   * @exception     EntryNotFoundException if the group does not exist in 
   *                the registry
   * @exception     CustomRegistryException if there is any registry-specific 
   *                problem
   **/
   public Result getUsersForGroup(String groupSecurityName, int limit)
      throws NotImplementedException,
             EntryNotFoundException,
             CustomRegistryException {
      String s, user;
      BufferedReader in = null;
      List usrsForGroup = new ArrayList();
      int count = 0;
      int newLimit = limit+1;
      Result result = new Result();

      try {
         in = fileOpen(GROUPFILENAME);
         while ((s=in.readLine())!=null)
         {
            if (!(s.startsWith("#") || s.trim().length() <=0 )) {
               int index = s.indexOf(":");
               if ((s.substring(0,index)).equals(groupSecurityName))
               {
                  StringTokenizer st = new StringTokenizer(s, ":");
                  for (int i=0; i<2; i++)
                     st.nextToken();
                  String subs = st.nextToken();
                  StringTokenizer st1 = new StringTokenizer(subs, ",");
                  while (st1.hasMoreTokens()) {
                     user = st1.nextToken();
                     usrsForGroup.add(user);
                     if (limit !=0 && ++count == newLimit) {
                        usrsForGroup.remove(user);
                        result.setHasMore();
                        break;
                     }
                  }
               }
            }
         }
      } catch (Exception ex) {
         if (!isValidGroup(groupSecurityName)) {
            throw new EntryNotFoundException("group:"  
            + groupSecurityName 
            +  "is not valid");
         }
         throw new CustomRegistryException(ex.getMessage(),ex);
      } finally {
         fileClose(in);
      }

      result.setList(usrsForGroup);
      return result;
   }

  /**
   * This method is implemented internally by the WebSphere Application Server 
   * code in this release. This method is not called for the custom 
   * registry implementations for this release. Return null in the 
   * implementation.
   *
   **/
   public WSCredential createCredential(String userSecurityName)
         throws CustomRegistryException,
                NotImplementedException,
                EntryNotFoundException {

      // This method is not called.
      return null;
   }

   // private methods
   private BufferedReader fileOpen(String fileName)
      throws FileNotFoundException {
      try {
         return new BufferedReader(new FileReader(fileName));
      } catch(FileNotFoundException e) {
         throw e;
      }
   }

   private void fileClose(BufferedReader in) {
      try {
         if (in != null) in.close();
      } catch(Exception e) {
         System.out.println("Error closing file" + e);
      }
   }

   private boolean match(String name, String pattern) {
      RegExpSample regexp = new RegExpSample(pattern);
      boolean matches = false;
      if(regexp.match(name))
          matches = true;
      return matches;
   }

	@Override
	public void changePassword(String user, String password) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(USERFILENAME));
			List<String> users = new ArrayList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(user + ":")) {
					String[] cols = line.split(":");
					StringBuilder newLine = new StringBuilder();
					for (int i = 0; i < cols.length; i++) {
						newLine.append(':');
						if (i == 1) {
							newLine.append(password);
						} else {
							newLine.append(cols[i]);
						}
					}
					newLine.deleteCharAt(0);
					line = newLine.toString();
				}
				
				users.add(line);
			}
			reader.close();
			
			FileWriter writer = new FileWriter(USERFILENAME);
			for (String toWrite : users) {
				writer.write(toWrite);
				writer.write("\r\n");
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


//----------------------------------------------------------------------
// The program provides the Regular Expression implementation 
// used in the sample for the custom user registry (FileRegistrySample). 
// The pattern matching in the sample uses this program to search for the 
// pattern (for users and groups).
//----------------------------------------------------------------------

class RegExpSample
{

    private boolean match(String s, int i, int j, int k)
    {
        for(; k < expr.length; k++)
label0:
            {
                Object obj = expr[k];
                if(obj == STAR)
                {
                    if(++k >= expr.length)
                        return true;
                    if(expr[k] instanceof String)
                    {
                        String s1 = (String)expr[k++];
                        int l = s1.length();
                        for(; (i = s.indexOf(s1, i)) >= 0; i++)
                            if(match(s, i + l, j, k))
                                return true;

                        return false;
                    }
                    for(; i < j; i++)
                        if(match(s, i, j, k))
                            return true;

                    return false;
                }
                if(obj == ANY)
                {
                    if(++i > j)
                        return false;
                    break label0;
                }
                if(obj instanceof char[][])
                {
                    if(i >= j)
                        return false;
                    char c = s.charAt(i++);
                    char ac[][] = (char[][])obj;
                    if(ac[0] == NOT)
                    {
                        for(int j1 = 1; j1 < ac.length; j1++)
                            if(ac[j1][0] <= c && c <= ac[j1][1])
                                return false;

                        break label0;
                    }
                    for(int k1 = 0; k1 < ac.length; k1++)
                        if(ac[k1][0] <= c && c <= ac[k1][1])
                            break label0;

                    return false;
                }
                if(obj instanceof String)
                {
                    String s2 = (String)obj;
                    int i1 = s2.length();
                    if(!s.regionMatches(i, s2, 0, i1))
                        return false;
                    i += i1;
                }
            }

        return i == j;
    }

    public boolean match(String s)
    {
        return match(s, 0, s.length(), 0);
    }

    public boolean match(String s, int i, int j)
    {
        return match(s, i, j, 0);
    }

    public RegExpSample(String s)
    {
        Vector vector = new Vector();
        int i = s.length();
        StringBuffer stringbuffer = null;
        Object obj = null;
        for(int j = 0; j < i; j++)
        {
            char c = s.charAt(j);
            switch(c)
            {
            case 63: /* '?' */
                obj = ANY;
                break;

            case 42: /* '*' */
                obj = STAR;
                break;

            case 91: /* '[' */
                int k = ++j;
                Vector vector1 = new Vector();
                for(; j < i; j++)
                {
                    c = s.charAt(j);
                    if(j == k && c == '^')
                    {
                        vector1.addElement(NOT);
                        continue;
                    }
                    if(c == '\\')
                    {
                        if(j + 1 < i)
                            c = s.charAt(++j);
                    }
                    else
                    if(c == ']')
                        break;
                    char c1 = c;
                    if(j + 2 < i && s.charAt(j + 1) == '-')
                        c1 = s.charAt(j += 2);
                    char ac1[] = {
                        c, c1
                    };
                    vector1.addElement(ac1);
                }

                char ac[][] = new char[vector1.size()][];
                vector1.copyInto(ac);
                obj = ac;
                break;

            case 92: /* '\\' */
                if(j + 1 < i)
                    c = s.charAt(++j);
                break;

            }
            if(obj != null)
            {
                if(stringbuffer != null)
                {
                    vector.addElement(stringbuffer.toString());
                    stringbuffer = null;
                }
                vector.addElement(obj);
                obj = null;
            }
            else
            {
                if(stringbuffer == null)
                    stringbuffer = new StringBuffer();
                stringbuffer.append(c);
            }
        }

        if(stringbuffer != null)
            vector.addElement(stringbuffer.toString());
        expr = new Object[vector.size()];
        vector.copyInto(expr);
    }

    static final char NOT[] = new char[2];
    static final Integer ANY = new Integer(0);
    static final Integer STAR = new Integer(1);
    Object expr[];

}
