/*
 * RemoteVRecordSetInterface.java
 *
 * Created on March 11, 2007, 5:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.db.remote.transaction;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.*;

/**
 *
 * @author Data Virtue
 */
public interface TransVRecordSetInterface extends Remote {
    
        /* Work horse */
    public void save (Object [] data, String ak) throws RemoteException;
    
    /**Convenience method to overwrite existing records based on key */
    public void save (int key, String ak) throws RemoteException;
    
        
    public void delete (int key, String ak) throws RemoteException;
    
    public void deleteAll(String ak) throws RemoteException;
    
    
    public void dissolve() throws RemoteException;
    
    public TransVRecord get (int key) throws RemoteException;
    
    /** Convenience method */
    public void add (TransVRecord vr) throws RemoteException;
    
      
    
}
