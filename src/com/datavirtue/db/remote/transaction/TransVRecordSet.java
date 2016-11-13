/*
 * VRecordSet.java
 *
 * Created on March 10, 2007, 7:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.db.remote.transaction;
import java.util.*;
import javax.swing.table.*;
import com.datavirtue.tools.DV;

import java.rmi.RemoteException;
import java.rmi.*;

/**
 *
 * @author Data Virtue
 */
public class TransVRecordSet implements java.io.Serializable {
    
    /** Creates a new instance of VRecordSet */
    public TransVRecordSet(RemoteTransactionServerInterface dbe, String dbname, TableModel tm, String tempAccessKey) throws RemoteException {
        
        super();
        db = dbe;
        this.dbname = dbname;
        
        VRSource = (DefaultTableModel)tm;
        if (tm.getRowCount() > 0 ) createVRecords(tm, tempAccessKey);
        
    }
    
    /* Work horse */
    public void save (Object [] data, String ak) throws RemoteException{
        
        if (0 == (Integer)data[0]) {
            
            VRList.add(new TransVRecord(db, dbname ,data, ak));
            return;
            
        }
        
        TransVRecord temp;
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (TransVRecord)VRList.get(i);
            
            if (temp.getRecordKey() == data[0]){
                
                temp = new TransVRecord (db, dbname, data, ak);
                temp.save(ak);
                
                VRList.add(i, temp );  //overwrite old data
                return;                
            }
            
        }
        
        temp = new TransVRecord(db, dbname ,data, ak);
        temp.save(ak);
        
        this.add(temp);
        
    }
    
    /**Convenience method to overwrite existing records based on key */
    public void save (int key, String ak) throws RemoteException{
        
        TransVRecord temp;
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (TransVRecord)VRList.get(i);
            
            if (temp.getRecordKey() == key){
                
                temp.save(ak);
                return;   
                
            }
            
        }
        
    }
    
        
    public void delete (int key, String ak) throws RemoteException{
        
        TransVRecord temp;
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (TransVRecord)VRList.get(i);
            
            if (temp.getRecordKey() == key){
                
                temp.delete(ak);
                                               
            }
            
        }
        
        for (int i = 0; i < VRSource.getRowCount(); i++){
            
            int k = (Integer)VRSource.getValueAt(i, 0);  //get key
            
            
            if (k == key){
                
                VRSource.removeRow(i);
                return;
                               
            }
            
        }
        
      
        
    }
    
    public void deleteAll(String ak) throws RemoteException{
        
        TransVRecord temp;
        VRList.trimToSize();
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (TransVRecord)VRList.get(i);
            temp.delete(ak);
                       
        }
        
        this.dissolve();
        
        
    }
    
    
    public void dissolve() throws RemoteException{
        
        db = null;
        VRSource = null;
        VRList = null;
        dbname = null;
                
    }
    
    public TransVRecord get (int key) throws RemoteException{
        
        TransVRecord temp;
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (TransVRecord)VRList.get(i);
            
            if (temp.getRecordKey() == key){
                
                return temp;
                               
            }
            
        }
        
        return null;
        
    }
    
    
    
    /** Convenience method */
    public void add (TransVRecord vr) throws RemoteException{
        
        VRSource.addRow(vr.getData());
        VRList.add(vr);
                
    }
    
    private void createVRecords (TableModel tm, String ak) throws RemoteException{
        
       /* Cycle through and grab each row from the table 
          converting each into a VRecord stored in VRList */ 
        
        int rows = tm.getRowCount();
        
        
        for (int r = 0; r < rows; r++){
            
            /* DV.getRow() copies the data instead of refrencing */
            VRList.add(new TransVRecord(db, dbname,DV.getRow(tm,r), ak));
            
        }
        
        
        VRList.trimToSize();
        
    }
    
    
    
    /*  */
    protected ArrayList VRList = new ArrayList();
    protected DefaultTableModel VRSource;
    protected boolean debug = true;
    
    protected RemoteTransactionServerInterface db;
    protected String dbname;
    
    
    
}
