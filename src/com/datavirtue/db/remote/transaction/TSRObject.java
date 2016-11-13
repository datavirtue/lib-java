/*
 * TSRObject.java
 *
 * Created on June 1, 2007, 11:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.db.remote.transaction;
import java.util.*;
import com.datavirtue.tools.DV;

/**
 *
 * @author Data Virtue
 */
public class TSRObject implements java.io.Serializable {
    
    /** Creates a new instance of TSRObject */
    public TSRObject(javax.swing.JLabel [] jLabels) {
           
          jl = jLabels;      
        
    }
    
    protected javax.swing.JLabel [] jl;
    
            
    public void giveReport(Object [] d){
        
        data = d;
        fire();
        
        
    }
    private java.util.ArrayList userList = new java.util.ArrayList();
      
    /** this is a one-off */
    public void fire(){
     
        /* update UI */
        
        //label 1 = current ip
        //label 2 = current db
        //label 3 = current resets
        //label 4 = Service Factor (grants - deny)
        
        String tmp = (String)data[0];
        if (!tmp.equals("")) {
        
            jl[0].setText(tmp);//user
            
        }
        jl[1].setText((String)data[1]); //db
        
        jl[2].setText(Integer.toString((Integer)data[2])); //resets
        
        int a = (Integer)data[3];
        
        int b = (Integer)data[4];
        
        jl[3].setText(Integer.toString(a - b));  //service factor
                
        
        if (!DV.scanArrayList(userList, tmp)){
        
            userList.add(tmp);
            //userList.trimToSize();
            jl[4].setText(Integer.toString(userList.size()-1));
            
        }
        
        
    }
    
    protected Object [] data;
    
    
}
