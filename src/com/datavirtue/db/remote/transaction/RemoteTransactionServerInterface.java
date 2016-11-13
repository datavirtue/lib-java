/*
 * RemoteDbEngine.java
 *
 * Created on March 11, 2007, 9:19 AM
 *
 * 
 *
 *All public methods return an Object []
 *This Object [] contains a "header" which you can access '(String)Object [0] '
 *to find out what happened during the call
 *if the status is "O" you can get the data from (Whatever)Object[1]
 *if the status is "B" you can try again
 *if the status is "U" it means the service is unavailable (not implemented)
 *if the status is "X" it denotes a 'crash' of some sort
 *
 *each method call locks the thread if not already locked and procededs with the task
 *if the thread is locked it responds as busy
 *its then up to the client to try again
 *as many tmes as needed until serviced
 *
 */

package com.datavirtue.db.remote.transaction;
import java.rmi.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;


/**
 *
 * @author Sean K Anderson - Data Virtue 2007
 *
 */
public interface RemoteTransactionServerInterface extends Remote {
   
    public Object [] removeRecord(String dbname, int key, String ak) throws RemoteException ;
 
    /** Reads and returns a record from disk of the specified dbname - key */
    public Object [] getRecord (String dbname, int key, String ak) throws RemoteException ;
     
    /* Added for performance reasons */
    public Object [] getRecord (String dbname, long pos, String ak) throws RemoteException ;
    
        
    /** Saves (insert - update) the record you provide in 'data'.  The first
     * Object [0] must be Integer ZERO for new record or an exsisting key within the
     * database.  This method returns the key used */
    public int saveRecord (String dbname, Object [] data, boolean unique, String ak) throws RemoteException ;

    /* ---------- ADD-ON PUBLIC METHODS ----------- */
    public Object [] search (String dbname, int col, String searchText, Boolean substring, String ak) throws RemoteException ;

    public Object [] searchFast (String dbname, int col, String searchText, Boolean substring, String ak) throws RemoteException ;
               
    public Object [] createTableModel (String db, ArrayList list, String ak) throws RemoteException ;

    /** Do not pass in a null ArrayList */
    public Object [] createTableModelFast (String db, ArrayList list, String ak) throws RemoteException ;
    
    public Object [] createTableModel (String db, String ak) throws RemoteException ;
    
   
    public Object [] getFieldNames (String dbname, String ak) throws RemoteException ;

    public Object [] isValidRecord (String dbname, Object [] data, String ak) throws RemoteException;
    
    public String getKey () throws RemoteException;
    
    public void releaseKey (String k) throws RemoteException;
    
}/*END DbEngine 1.0 CLASS*/
