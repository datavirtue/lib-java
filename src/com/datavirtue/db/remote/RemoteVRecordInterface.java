/*
 * RemoteVRecordInterface.java
 *
 * Created on March 11, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.db.remote;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.*;

/**
 *
 * @author Data Virtue
 */
public interface RemoteVRecordInterface extends Remote {
    
      
        public boolean clear() throws RemoteException; 
    
    public boolean save () throws RemoteException;
    
    public boolean delete () throws RemoteException ;
    
   public Object getData (int idx) throws RemoteException;
    
   public Object getData (String field)  throws RemoteException;
   
   /** Generic candidate, this is public but it should be avoided in favor of setData(String, Object))
    * Adds data to the storage component (Object [] data), cannot modify key with this method. */
   public boolean setData (int idx, Object obj) throws RemoteException;
   
   /** If you have not constructed with data but you already have 
    * it you can use this method to populate data */
   public boolean setData (Object [] obj) throws RemoteException;
   
   /** Adds data to the storage component (Object [] data), cannot modify key with this method. */
   public boolean setData (String field, Object obj) throws RemoteException;
   
   /**Lets the programmer look at the available fields. */
   public String [] getFields () throws RemoteException;
   
   /* Register other VRecord objects so that cascaded save/delete are possible */
   public void register (RemoteVRecord vr) throws RemoteException; 
   
   public int getKey() throws RemoteException;
   
   /** Trigger status that allows this record to be deleted. */
   public boolean setDeleteStatus (boolean tf) throws RemoteException;
   
   /**Trigger status that allows this record to be saved. */
   public boolean setSaveStatus (boolean tf) throws RemoteException;
   
   /**Verify that this record is valid against the expected data structure in the db schema  */
   public boolean isValid() throws RemoteException; 
   
   /** Verify that you can save this record. */
   public boolean canSave () throws RemoteException ;
  
   /** Releaases the numerous data recources used by this object.*/
   public void dissolve() throws RemoteException;
  
   
   
   public Object [] getData() throws RemoteException;
  
    
}
