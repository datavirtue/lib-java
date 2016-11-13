/*
 * RemoteVRecordSetInterface.java
 *
 * Created on March 11, 2007, 5:39 PM
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
public interface RemoteVRecordSetInterface extends Remote {
    
        /* Work horse */
    public void save (Object [] data) throws RemoteException;
    
    /**Convenience method to overwrite existing records based on key */
    public void save (int key) throws RemoteException;
    
        
    public void delete (int key) throws RemoteException;
    
    public void deleteAll() throws RemoteException;
    
    
    public void dissolve() throws RemoteException;
    
    public RemoteVRecord get (int key) throws RemoteException;
    
    /** Convenience method */
    public void add (RemoteVRecord vr) throws RemoteException;
    
      
    
}
