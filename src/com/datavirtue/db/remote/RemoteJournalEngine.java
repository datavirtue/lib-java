/*
 * RemoteJournalEngine.java
 *
 * Created on April 9, 2007, 10:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.db.remote;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import com.datavirtue.tools.DV;

/**
 *
 * @author Sean K Anderson - Data Virtue 2007
 */
public class RemoteJournalEngine extends UnicastRemoteObject implements RemoteJournalEngineInterface {
    
    /** Creates a new instance of RemoteJournalEngine */
 public RemoteJournalEngine() throws RemoteException {
    }
    
 public RemoteJournalEngine (String folder) throws RemoteException {
     
     dataFolder = folder;
     
 }
 
 private boolean isLocked (){
     
     return locked;
     
 } 
 
 private void lock() {
     
     locked = true;
     
 }
 
 private void unlock() {  
     
     locked = false;
     
 }
 
volatile private boolean locked = false;
    
    
    
     /* returns a list of journal entries in the specified folder (integer key named folder) */   
    public Object [] listEntries(int folder) throws RemoteException{
        
        synchronized (this){
          if (isLocked()) return new Object [] {new String("B"), null};
        lock();}
        
        javax.swing.DefaultListModel lm = new javax.swing.DefaultListModel();
        
        String path = dataFolder + folder + "/";
        
        java.io.File dir = new java.io.File (path);
        
        if (!dir.exists()) dir.mkdirs();
        
        String [] files = dir.list();
        
        if (files == null || files.length < 1) {
            Object [] obj = new Object [] {new String ("O"), null};
            unlock();
            return obj;
        }
        
        for (int i = files.length-1; i > -1; i--){
        
            lm.addElement(files[i]);
            
        }
        Object [] obj = new Object [] {new String ("O"), lm};
        unlock();
        return obj;
       
        
    }
    
    private javax.swing.DefaultListModel grabEntries (int folder) {
        
                
        javax.swing.DefaultListModel lm = new javax.swing.DefaultListModel();
        
        String path = dataFolder + folder + "/";
        
        java.io.File dir = new java.io.File (path);
        
        if (!dir.exists()) dir.mkdirs();
        
        String [] files = dir.list();
        
        if (files == null || files.length < 1) return null;
        
        for (int i = files.length-1; i > -1; i--){
        
            lm.addElement(files[i]);
            
        }
        return lm;
        
        
    }
    
    /* returns the text inside the specified entry */
    public Object [] getEntry(int folder, String file) throws RemoteException {
        
        synchronized (this){
          if (isLocked()) return new Object [] {new String("B"), null};
        lock();}
        
        String t = DV.readFile(dataFolder + folder + "/" + file );
        Object [] obj = new Object [] {new String ("O"), new String (t)};
        unlock();
        return obj;
        
    }
    
    /* saves the edit of the specified entry */
    public Object [] saveEntry (int folder, String file, String t) throws RemoteException{
        
        synchronized (this){
          if (isLocked()) return new Object [] {new String("B"), null};
        lock();}
        
        DV.writeFile(dataFolder + folder + "/" + file, t, false);
        Object [] obj = new Object [] {new String ("O"), new Boolean(true)};
        unlock();
        return obj;
        
    }
    
    /* creates a new entry for today, returns the name of the created file */
    public Object [] createEntry (int folder) throws RemoteException{
        
        synchronized (this){
          if (isLocked()) return new Object [] {new String("B"), null};
        lock();}
        
        
        String date = DV.getShortDate().replace('/', '-');
        String tmp="";
        
        javax.swing.DefaultListModel lm = this.grabEntries(folder);
        int elements = 0;
        if (lm != null) {
            
            elements = lm.size();
            
        }
        
        boolean match=false;
        
        for (int e = 0; e < elements; e++){
            
            tmp = (String)lm.getElementAt(e);
            
            if (tmp.equals(date) ) match = true;
            
            
        }
        
        if (!match) {
            
            java.io.File jFile = new java.io.File(dataFolder + folder +"/");
            
            if (!jFile.exists()) jFile.mkdirs();
            
            boolean t = DV.writeFile(jFile.toString() + '/'+ date, DV.getFullDate(), false);
            Object [] obj = new Object [] {new String ("O"), new String (date)};
            unlock();
            return obj;
            
            
           
        }
        Object [] obj = new Object [] {new String ("O"), new String ("")};
        unlock();
        return obj;
        
    }
    
    private String dataFolder = "data/jrnls/";
    
    
}
