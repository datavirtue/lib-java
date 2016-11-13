/*
 * VRecordSet.java
 *
 * Created on March 10, 2007, 7:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.db.remote;
import java.util.*;
import javax.swing.table.*;
import com.datavirtue.tools.DV;

import java.rmi.RemoteException;
import java.rmi.*;

/**
 *
 * @author Data Virtue
 */
public class RemoteVRecordSet implements java.io.Serializable {
    
    /** Creates a new instance of VRecordSet */
    public RemoteVRecordSet(RemoteDbEngineInterface dbe, String dbname, TableModel tm) throws RemoteException {
        
        super();
        db = dbe;
        this.dbname = dbname;
        
        VRSource = (DefaultTableModel)tm;
        if (tm.getRowCount() > 0 ) createVRecords(tm);
    }
    
    /* Work horse */
    public void save (Object [] data) throws RemoteException{
        
        if (0 == (Integer)data[0]) {
            
            VRList.add(new RemoteVRecord(db, dbname ,data));
            return;
            
        }
        
        RemoteVRecord temp;
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (RemoteVRecord)VRList.get(i);
            
            if (temp.getKey() == data[0]){
                
                temp = new RemoteVRecord (db, dbname, data);
                temp.save();
                
                VRList.add(i, temp );  //overwrite old data
                return;                
            }
            
        }
        
        temp = new RemoteVRecord(db, dbname ,data);
        temp.save();
        
        this.add(temp);
        
    }
    
    /**Convenience method to overwrite existing records based on key */
    public void save (int key) throws RemoteException{
        
        RemoteVRecord temp;
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (RemoteVRecord)VRList.get(i);
            
            if (temp.getKey() == key){
                
                temp.save();
                return;   
                
            }
            
        }
        
    }
    
        
    public void delete (int key) throws RemoteException{
        
        RemoteVRecord temp;
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (RemoteVRecord)VRList.get(i);
            
            if (temp.getKey() == key){
                
                temp.delete();
                                               
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
    
    public void deleteAll() throws RemoteException{
        
        RemoteVRecord temp;
        VRList.trimToSize();
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (RemoteVRecord)VRList.get(i);
            temp.delete();
                       
        }
        
        this.dissolve();
        
        
    }
    
    
    public void dissolve() throws RemoteException{
        
        db = null;
        VRSource = null;
        VRList = null;
        dbname = null;
        
        
    }
    
    public RemoteVRecord get (int key) throws RemoteException{
        
        RemoteVRecord temp;
        
        for (int i = 0; i < VRList.size(); i++){
            
            temp = (RemoteVRecord)VRList.get(i);
            
            if (temp.getKey() == key){
                
                return temp;
                               
            }
            
        }
        
        return null;
        
    }
    
    
    
    /** Convenience method */
    public void add (RemoteVRecord vr) throws RemoteException{
        
        VRSource.addRow(vr.getData());
        VRList.add(vr);
                
    }
    
    private void createVRecords (TableModel tm) throws RemoteException{
        
       /* Cycle through and grab each row from the table 
          converting each into a VRecord stored in VRList */ 
        
        int rows = tm.getRowCount();
        
        
        for (int r = 0; r < rows; r++){
            
            /* DV.getRow() copies the data instead of refrencing */
            VRList.add(new RemoteVRecord(db, dbname,DV.getRow(tm,r)));
            
        }
        
        
        VRList.trimToSize();
        
    }
    
    
    
    /*  */
    protected ArrayList VRList = new ArrayList();
    protected DefaultTableModel VRSource;
    protected boolean debug = true;
    
    protected RemoteDbEngineInterface db;
    protected String dbname;
    
    
    
}
