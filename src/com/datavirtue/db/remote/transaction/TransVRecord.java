/*
 * VRecord.java
 *
 * Created on March 9, 2007, 7:18 AM
 *
 *This class keeps Object data in a Virtual Record that knows how to handle itself
 *Need to create and throw custom Exceptions instead of all the boolean return values.
 *
 *To keep MVC architecture DO NOT address this object from your UI.  
 *Establish another controller object that uses this object.
 *Have the UI talk with the more generic controller object.
 *
 *
 *It is wise to reference fields by name instead of index 
 *
 */

package com.datavirtue.db.remote.transaction;

import com.datavirtue.tools.DV;
import java.util.ArrayList;


import java.rmi.RemoteException;




/**
 *
 * @author Sean K Anderson - Copyright Data Virtue 2007
 *
 */

/**
 *this VRecord performs tasks that need an access key, they are pass through granted
 *
 */
public class TransVRecord implements java.io.Serializable  {
    
    /** Constructor for new data */
        
    /** Constructor for new data */
    public TransVRecord(RemoteTransactionServerInterface dbe, String dbname, String tempAccessKey) throws RemoteException {
    
        super();
       debugInfo = dbname;
       db = dbe;
       this.dbname = dbname;  //do a copy
       fields = getFieldNames(dbname, tempAccessKey);
       valid = false;
       key = 0;
       data = new Object [fields.length];
       data [0] = new Integer (key);
              
           
    }
    
    
     /** Constructor for existing data */
    public TransVRecord(RemoteTransactionServerInterface dbe, String dbname, Object [] datain, String tempAccessKey) throws RemoteException {
    
       super();
        debugInfo = dbname;
       db = dbe;
       this.dbname = dbname;
       data = existingData(datain);  //do a copy?
       
       key = (Integer)datain[0];
       
       valid  = isRecordValid(dbname, data, tempAccessKey);
       
       fields = getFieldNames(dbname, tempAccessKey);
       
       //System.out.println("Field Length "+fields.length);
       
    }
    
    private Object [] existingData (Object [] ed){
        
        int e = ed.length;
        
        Object [] d = new Object [e];
        
        int w = 0;
        
        for (int i = 0; i < e; i++){
            
             w = DV.whatIsIt(ed[i]);
             
             //System.out.println("What is it: "+w);
            // DV.expose(ed);
            
            if (w == 1){
                
                d[i] = new String ((String)ed[i]);
                
            }
            
            if (w == 2){
                
                d[i] = new Integer ((Integer)ed[i]);
                
            }
            if (w == 3){
                
                d[i] = new Float ((Float)ed[i]);
                
            }
            if (w == 4){
                
                d[i] = new Boolean ((Boolean)ed[i]);
                
            }
            
        }
        return d;
        
        
    }
    
    private String [] getFieldNames (String dbname, String ak){
        
        Object [] ret = null;
        String stat ="";
        
       
            try {
                
                ret = db.getFieldNames(dbname, ak);
                
            } catch (RemoteException ex) {
                ex.printStackTrace();
                return null;
            }
            
        stat = (String) ret[0];
        
    
        System.out.println("TransVRecord getFieldNames stat = "+stat);
        
            if (stat.equals("O")) return (String []) ret[1];
           
            return null;
            
              
    }
    
    /** Abandons data effectivly clearing the object of its usefulness, meant to release resources.  */
    public boolean clear() throws RemoteException {
        
        key = 0;
        data = new Object [fields.length];
        data [0] = key;
        
        return true;
        
    }
    
    
    public boolean save (String ak) throws RemoteException{
        
        
        valid  = isRecordValid(dbname, data, ak);
        
        if (!save) return true;
        if (!this.valid) return false;
        
        boolean ok = true;
        boolean tmp;
        int savedKey;
        
        relation.trimToSize();
        TransVRecord vr;
        
        savedKey = db.saveRecord(dbname, data, false, ak);
        
        if (savedKey == -1){
            /*If an error occurs this tells me which VR had the problem */
            System.out.println("VRecord Save: "+debugInfo);
            return false;
        }
         
        /* Cycle through and save each referenced VRecord */
        if (relation != null && relation.size() > 0){
        
            for (int i = 0; i < relation.size(); i++){
                
                vr = (TransVRecord)relation.get(i);
                tmp = vr.save(ak);
                if (!tmp) return false;  //error occurred or invalid
                   
            }
                        
        }
        
        return true;
        
    }
    
    public boolean delete (String ak) throws RemoteException {
        
        /* Cycles through and deletes all VRecords, "dependent" VRecords are deleted first, if any
         fail then the "main" record is not deleted.*/
        
        boolean tmp;
        relation.trimToSize();
        TransVRecord vr;
                        
        if (relation != null && relation.size() > 0){
        
            for (int i = 0; i < relation.size(); i++){
                
                vr = (TransVRecord)relation.get(i);
                tmp = vr.delete(ak);
                if (!tmp) {
                    
                    System.out.println("VRecord Delete: "+debugInfo);
                    return false;  //error occurred
                } 
                   
            }
                        
        }
        /* Delete the "main" record after all others have been deleted. */
        tmp = removeRecord(this.dbname, this.key, ak);
        if (!tmp) return false;
        
        return true;  //finnished!
    }
    
    private boolean removeRecord (String dbname, int key, String ak) {
        
        Object [] ret = null;
        String stat = "";
        while (true) {
            try {
                
                ret = db.removeRecord(dbname, key, ak);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
            stat = (String)ret[0];
            if (stat.equals("O")) return (Boolean) ret[1];
            if (stat.equals("B")) continue;
            
        }
        
    }
    
    
   public Object getData (int idx) throws RemoteException{
       
       return data[idx];
       
   }
    
   public Object getData (String field)  throws RemoteException{
       
       int [] idx = DV.whichContains(fields, field);
       
       if (idx != null && idx[0] > -1){
       
           return getData(idx[0]);
       
       }else return null;
       
       
   }
   
   /** Generic candidate, this is public but it should be avoided in favor of setData(String, Object))
    * Adds data to the storage component (Object [] data), cannot modify key with this method. */
   public boolean setData (int idx, Object obj) throws RemoteException{
       
       if (idx == 0) return false;  //cant change key
       
       //fields = db.getFieldNames(dbname);
       data[idx] = obj;
       
       return false;
       
   }
   
   /** If you have not constructed with data but you already have 
    * it you can use this method to populate data */
   public boolean setData (Object [] obj) throws RemoteException{  
       
       /* init field data */
       
        try {
            
            int a = (Integer)obj[0];
        }catch(Exception e){
            
            return false;
        }
  
       if (obj != null ) {
            
            data = obj;
            key = (Integer)data[0];
            return true;
       }
       
       return false;
       
   }
   
   /** Adds data to the storage component (Object [] data), cannot modify key with this method. */
   public boolean setData (String field, Object obj) throws RemoteException{
       
       if (field.equalsIgnoreCase("key")) return false;
       
       field = field.toUpperCase();  //????
       
       int [] idx = DV.whichContains(fields, field);
       
       if (idx != null && idx[0] > -1){
           
           setData (idx[0], obj);
           return true;
       }
       
       return false;
       
   }
   
   /**Lets the programmer look at the available fields. */
   public String [] getFields () throws RemoteException {
              
       return fields;
       
   }
   
   /* Register other VRecord objects so that cascaded save/delete are possible */
   public void register (TransVRecord vr) throws RemoteException {
       
      /* Keep a list of refs to related VR objects */        
       relation.add(vr);
      
       
   }
   
   public int getRecordKey() throws RemoteException {
       
       return key;
              
   }
   
   /** Trigger status that allows this record to be deleted. */
   public boolean setDeleteStatus (boolean tf) throws RemoteException{
       
       boolean changed = false;
       if (tf == delete) changed = false;
       else changed = true;
       
       delete = tf;
       
       return changed;
       
   } 
   
   /**Trigger status that allows this record to be saved. */
   public boolean setSaveStatus (boolean tf) throws RemoteException{
       
       boolean changed = false;
       if (tf == save) changed = false;
       else changed = true;
       save = tf;
       return changed;
       
   }
   
   /**Verify that this record is valid against the expected data structure in the db schema  */
   public boolean isValid(String ak) throws RemoteException {
       
       valid = isRecordValid(dbname, data, ak);
       return valid;
       
   }
   
   private boolean isRecordValid (String dbname, Object [] data, String ak) throws RemoteException{
       
       
       Object [] ret = null;
       String stat = "";
       
       
            try {
                
                ret = db.isValidRecord(dbname, data, ak);
                
            } catch (RemoteException ex) {
                ex.printStackTrace();
                return false;
            }
       
           stat= (String)ret[0];
           if (stat.equals("O")) return (Boolean)ret[1];
           
              return false;
              
   }
   
   
   /** Verify that you can save this record. */
   public boolean canSave () throws RemoteException {
       
       return save;
       
   }
  
   /** Releaases the numerous data recources used by this object.*/
   public void dissolve() throws RemoteException{
       
       /* Clean up this object, no longer used */
       data = null;
       unmodifiedData = null;
       fields = null;
       db = null;
       dbname = null;
       debugInfo = null;
       relation = null;
            
       
   }
   
   
   public Object [] getData() throws RemoteException{
       
       return data;
       
   }
   
   /* Data */
   /**The actual record or user data */
   protected Object [] data;
   /**Keep an image of the original data supplied by the user for transactions? */
   protected Object [] unmodifiedData;
   /**Field names */
   protected String [] fields;
   
   /* DB */
   /**The persistance engine */
   protected RemoteTransactionServerInterface db;
   protected String dbname;
   /** The key for this record, zero for new. */
   protected int key;
   /** Byte location of this record, unmaintained, this could possibly change during the lifetime of the 
    object. */
   protected long byte_location;
   /** String description of this record for debug output purposes */
   protected String debugInfo = "";
   
   
   /* Relations */
   /** List of VRecords that "belong" to this VRecord, for relations. */
   protected ArrayList relation = new ArrayList();
   
   protected boolean delete = true;
   protected boolean save = true;
   protected boolean valid = true;
   
}
