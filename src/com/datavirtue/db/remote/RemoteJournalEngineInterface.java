/*
 * RemoteJournalEngineInterface.java
 *
 * Created on April 9, 2007, 9:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.db.remote;
import java.rmi.*;



/**
 *
 * @author Sean K Anderson
 */

public interface RemoteJournalEngineInterface extends Remote {
    
    /* returns a list of journal entries in the specified folder (integer key named folder) */   
    public Object [] listEntries(int folder) throws RemoteException;
    
    /* returns the text inside the specified entry */
    public Object [] getEntry(int folder, String file) throws RemoteException;
    
    /* saves the edit of the specified entry */
    public Object [] saveEntry (int folder, String file, String t) throws RemoteException;
    
    /* creates a new entry for today, returns the name of the created file */
    public Object [] createEntry (int folder) throws RemoteException;
    
    
    
}
