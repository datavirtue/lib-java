
/****************************************************************************
     DbEngine 1.2 CORE+
     Created by Sean Kristen Anderson - Data Virtue 2005 - 2007
     CopyRight Data Virtue 2005, 2006, 2007  All Rights Reserved.
 *  
 * Started May 2005

	TO DO:  ??
  --------------------------------------------------------------------------
  Create report object?
 * desgin a method to create a data control-layer Java Bean?
 * Create index system for searching large files?
 * Output DTD and XML files for EDI?
 * Based on searches you can export a set of keys to XML and then generate a DTD from that
 * make utility to tranzfer to SQL Engine
 * support transactions??
 * buld method to return Object[] filled with data from a specified column
 * make method to search a list of keys passed in as an ArrayList
 * create browser dialog object for return of specified column selected row value
 * handle combo boxes somehow
 * build logging system?
 * build method to find AND return certain columns of data from a record
 * make a public method to set data location  <<>>
 * --------------------------------------------------------------------------

     System Features:

 * Can load as many databases as you wish (Data System)  [.dsf file]
 * Databases are easily definable through text files (Schema Files) [.sch file]
 * Supports relational data structuring (you can do whatever you want programatically)
 * Import/Export of common .csv files /
 * Ready-made methods to generate sorted TableModels for displaying data & search results
 * Supports 50000+ records per file
 * Easy backup and restoration of data
 * Contains methods for general and limited advanced searching
 * Instant record deletes with no data loss on failure
 * Easily integrates into any project with super small learning curve
 * 
 *
CHANGES IN 1.2 CORE+
 *Changed recording scheme for stings, version 1.0 was UTF encoded strings which took up too much space
 *Converted from UTF to bytes with top half dropped  - RandomAccessFile.writeBytes(String s) method
 *Used to use RandomAccess File writeChar(char c) which sucks down 2 bytes for each one
 *
 *Changed file mode scheme to only be in the mode needed.  For example when getting records the db is in Read Only mode
 *When saving records the file is in Read/Write mode. this is done to increase speed and reliability for data queries (searches)
 *
*****************************************************************************/
package com.datavirtue.db;

import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.BufferedReader;  //Only import the needed classes
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileReader;
//import java.io.FileWriter;
import java.util.ArrayList;
import javax.crypto.interfaces.PBEKey;
import javax.swing.table.*;
import javax.swing.JTable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import com.datavirtue.tools.DV;
import com.datavirtue.gui.util.TableSorter;
import com.datavirtue.gui.util.ProgressDialogInterface;

//import de.schlichtherle.io.FileWriter;




public class DbEngine	  {

 public DbEngine () {

    //better use loadSchema after calling with this constructor

}/* END CONSTRUCTOR */


 public DbEngine (String DSF) {
	// make another constructor to handle a single .sch
    /* Process Database System File */
    loadDSF(DSF);

}/* END CONSTRUCTOR */

/*-------------- PUBLIC  API Data Processing Methods ------------*/

/* These methods work on the "currentDb" global */

    /** Cycles through each record and keeps all but the one record
         that has been specified in 'int key'*/

    public boolean removeRecords (String dbname, int col, int key) {  //not in use
   /*  //this override is used to delete records that contain a reference to another .db/record
     //look through db to find actual key of the record which spec'd col contains key
        use_db(dbname);

        ArrayList keys = scanColumn ( col, Integer.toString(key), false, true);
        if (keys.size() == 0) return true;
        boolean b;
        for (int i=0; i < keys.size(); i++) {

           //b = removeRecord (dbname, Long.valueOf( (Long) keys.get(i) ) );
            if (b == false) return false;

            //this is SLOWWWWWWW!!!!!
        }
*/

            return true;
    }

  
    /**
     *In use
     */
    public boolean removeRecord (String dbname, int key) {
        
        /* Launch this in a new thread ? */
        /* move this crap to packdb(String dbname, String username, String Password)  */
        try {
            use_db(dbname, "r"); //we are only going to read
            
            RAF.seek(0);  

            File newFile = new File(currentSchema.getDbPath()+"x");

            RandomAccessFile newRAF = new RandomAccessFile (newFile, "rw");

            long h = getNumOfRecs();

            Object [] xRecord = new Object [currentSchema.getNumFields()];

            int xKey = 0;

            newRAF.writeInt(RAF.readInt()); //Copy the key meta first

            for (int x=0; x < h; x++ ) { //cycle through all records in .db

                xRecord = readRecord(); //read record from .db
                Integer H = (Integer) xRecord[0];
                xKey = H.intValue();


                if (xKey != key) { //check key then write
                               // write record to .dbx
                    int rl = xRecord.length;

                        for (int i = 0; i < rl ; i++) {
                            int ftype = currentSchema.getFieldType(i);

                            /* STRING */
                            if (ftype == 1) {

                                String s = (String) xRecord[i];
                                
				int fsize = currentSchema.getFieldSize(i) ;  /*FIXME*/
				newRAF.writeBytes(padString(s, fsize));
                                
                            }
                                /* Integer - KEY */
                            if (ftype == 3 || ftype == 0) {
                                Integer I = (Integer) xRecord[i];
                                newRAF.writeInt(I.intValue());
                            }

                                /* MONEY $$ */
                            if (ftype == 2) { /* NUM */
                                Float f = (Float) xRecord[i];
                                newRAF.writeFloat(f.floatValue());
                            }
                            
                                  /* LONG */
                            if (ftype == 5) { /* LONG */
                                Long l = (Long) xRecord[i];
                                newRAF.writeLong(l.longValue());
                            }

                                    /* YES/NO */
                            if (ftype == 4) {
                                Boolean b = (Boolean) xRecord[i];
                                newRAF.writeBoolean(b.booleanValue());
                            }
                       }
                }
       }

    newRAF.close();
    newRAF = null;
    closeDb();
    dbFile.delete();
    File goodDb = new File (currentSchema.getPath()+currentSchema.getDbName()+".db");
    newFile.renameTo(goodDb);

    } catch (Exception e) {

        status = "error:21:The application had a probem removing a record" + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " The record was not removed.";

        DV.writeFile("db.err", "removeRecord() error :" +currentSchema.getDbName()+".db" + nl , true ); return false;


    }

    return true;
}


    /** Reads and returns a record from disk of the specified dbname - key */
    public Object [] getRecord (String dbname, int key) {
        
        /*
         * enclose this method in a loop based on use_db
         * use_db checks to see if it is locked if not the action is performed
         * if it is locked use_db returns a certain val that can be checked 
         * and logic branched accordingly
         */
        
        use_db(dbname, "r");
        if (seekToKey(key))	return readRecord();
	return null;

     }
     
    /* Added for performance reasons */
    public Object [] getRecord (String dbname, long pos){  
        
        
        use_db(dbname, "r");
        
        if (seekTo(pos)){
                    
            return readRecord();
            
        }else return null;
        
        
    }
    
    
    /** Saves (insert - update) the record you provide in 'data'.  The first
     * Object [0] must be Integer ZERO for new record or an exsisting key within the
     * database.  This method returns the key used */
    public int saveRecord (String dbname, Object [] data, boolean unique) {

        use_db (dbname);
	if (!dbVerify()) { System.out.println (".db is corrupt!"); return -1; }

        if (isRecordValid(data)) {
            if (unique) {
                if (isRecordUnique(data)) {
                    return writeRecord(data);
		}else return 0;  //this means record was specified unique but was not
            }
	return writeRecord(data);

	} else return -1;  //bad data

    }


    /* ---------- ADD-ON PUBLIC METHODS ----------- */
    public ArrayList search (String dbname, int col, String searchText, Boolean substring) {

        use_db(dbname,"r");
	return scanColumn (col, searchText, substring, false);

    }

    public ArrayList searchFast (String dbname, int col, String searchText, Boolean substring) {

        use_db(dbname, "r");
        
	return scanColumn (col, searchText, substring, true);

    }
    
    
    public TableModel createTableModel (String db, ArrayList list, JTable jt) {

        use_db (db, "r");
        int recs = list.size();
        int numFields = currentSchema.getNumFields();

        Object [] [] data = new Object [recs] [numFields];
        String [] headers = new String [numFields];

        Object [] record = new Object [numFields];
         int s = list.size();


        int [] l = new int [s];


        for (int row = 0; row < s; row++) {
            l[row] = Integer.valueOf((Integer)list.get(row));

        }

        list.clear();
         list = null;

        for (int h = 0; h < headers.length; h++) {
            headers [h] = currentSchema.getFieldName(h);
        }

        //try { RAF.seek(4); } catch (Exception e) {e.printStackTrace();};
        skipDbHeader();
        long time;



        for (int row = 0; row < s; row++) {


            seekToKey(l[row], false);


            record = readRecord ();


            for (int col = 0; col < numFields; col++){
                data [row] [col] = record [col];
            }

        }

        if (jt != null){
        sorter = new TableSorter(new DefaultTableModel(data,headers));
        sorter.setTableHeader(jt.getTableHeader());
        data = null;
        return sorter;

        }else {

            return new DefaultTableModel(data, headers);


        }
    }

    
    
    public TableModel createTableModel (String db, ArrayList list, boolean sort) {

        if (list == null) return null;  //do not pass a null value in

        use_db (db, "r");

        int recs = list.size();
        int numFields = currentSchema.getNumFields();

        Object [] [] data = new Object [recs] [numFields];
        String [] headers = new String [numFields];

        Object [] record = new Object [numFields];
         int s = list.size();


        int [] l = new int [s];


        for (int row = 0; row < s; row++) {  //put all the keys into an 
            l[row] = Integer.valueOf((Integer)list.get(row));

        }

        list.clear();
         list = null;

        for (int h = 0; h < headers.length; h++) {
            headers [h] = currentSchema.getFieldName(h);
        }


        skipDbHeader();
        long time;



        for (int row = 0; row < s; row++) {


            seekToKey(l[row], false);


            record = readRecord ();


            for (int col = 0; col < numFields; col++){
                data [row] [col] = record [col];
            }

        }
        if (sort){

            sorter = new TableSorter(new DefaultTableModel(data, headers));
            //data = null;
            return sorter;


        }else {

            return new DefaultTableModel(data,headers){
             public Class getColumnClass(int column) {
                return DV.idObject(this.getValueAt(0,column));
            }
         };
        }

    }

        /** Do not pass in a null ArrayList */
        public TableModel createTableModelFast (String db, ArrayList list, boolean sort) {

        if (list == null) return null;  //do not pass a null value in!!!!

        use_db (db, "r");

        int recs = list.size();
        int numFields = currentSchema.getNumFields();

        Object [] [] data = new Object [recs] [numFields];
        String [] headers = new String [numFields];

        Object [] record = new Object [numFields];
         int s = list.size();


        long [] l = new long [s];


        for (int row = 0; row < s; row++) {  //put all the keys into an 
            l[row] = Long.valueOf((Long)list.get(row));

        }

        list.clear();
         list = null;

        for (int h = 0; h < headers.length; h++) {
            headers [h] = currentSchema.getFieldName(h);
        }


        skipDbHeader();
        long time;



        for (int row = 0; row < s; row++) {


           // seekToKey(l[row], false);
           seekTo(l[row]);

            record = readRecord ();


            for (int col = 0; col < numFields; col++){
                data [row] [col] = record [col];
            }

        }
        if (sort){

            sorter = new TableSorter(new DefaultTableModel(data, headers));
            //data = null;
            return sorter;


        }else {

            return new DefaultTableModel(data,headers){
             public Class getColumnClass(int column) {  //this little area makes sure the proper renderer is used
                return DV.idObject(this.getValueAt(0,column));
            }
         };
        }

    }

    
    
    
    public TableModel createTableModel (String db, JTable jt) {

        use_db (db, "r");
        int recs = (int) getNumOfRecs();
        int numFields = currentSchema.getNumFields();
          //System.out.println("Num of records: "+recs+" number of Firlds "+numFields);
        // build TableModel with data and headers

        Object [] [] data = new Object [recs] [numFields];
        String [] headers = new String [numFields];

        Object [] record = new Object [numFields];

        for (int h = 0; h < headers.length; h++) {
            headers [h] = currentSchema.getFieldName(h);
        }

        skipDbHeader();

        for (int row = 0; row < recs; row++) {
                record = readRecord ();

                for (int col = 0; col < numFields; col++){
                    data [row] [col] = record [col];
                }

            }

        sorter = new TableSorter(new DefaultTableModel(data,headers));
        
        if (jt == null)  sorter.setTableHeader(null);
            else sorter.setTableHeader(jt.getTableHeader());
        
        data = null;
        return sorter;
    }


    public TableModel createTableModel (String db) {

        use_db (db, "r");
        int recs = (int) getNumOfRecs();
        int numFields = currentSchema.getNumFields();
          //System.out.println("Num of records: "+recs+" number of Firlds "+numFields);
        // build TableModel with data and headers

        Object [] [] data = new Object [recs] [numFields];
        String [] headers = new String [numFields];

        Object [] record = new Object [numFields];

        for (int h = 0; h < headers.length; h++) {
            headers [h] = currentSchema.getFieldName(h);
        }

        //try { RAF.seek(4); } catch (Exception e) {e.printStackTrace();};

        skipDbHeader();

        for (int row = 0; row < recs; row++) {
                record = readRecord ();

                for (int col = 0; col < numFields; col++){
                    data [row] [col] = record [col];
                }

            }


        return new DefaultTableModel(data,headers);
    }




    /** Checks to see if the desired db is "loaded" */
    public boolean isDbAvailable(String dbname) {

        for (int i = 0; i < schList.size(); i++) {
            DbSchema s = (DbSchema) schList.get(i);
            if (s.getDbName().equalsIgnoreCase(dbname)) return true;
	}

	return false;
    }
/*
 *
 *Direct Disk Access Section \
 *These methods work with the file system or disk operations and take a path
 *
 *
 */
public int csvExport (String dbname, File f, int [] fields) {
    use_db (dbname, "r");

    de.schlichtherle.io.File csv = new de.schlichtherle.io.File(f);
    
    int recs = (int) getNumOfRecs();
    
    
    int numFields = currentSchema.getNumFields();
    int numProcessed = 0;

    StringBuilder sb = new StringBuilder();
    
    for (int i = 0; i < numFields; i++ ){
        
        if (i < numFields-1){
            
            sb.append(currentSchema.getFieldName(i)+',');
            
        }else sb.append(currentSchema.getFieldName(i));
        
        
    }
    
    Object [] record = new Object [numFields];

    //File csv = new File (destPathFile);

    PrintWriter out= null;
    
    try {

            skipDbHeader();
            out = new PrintWriter (new BufferedWriter(new de.schlichtherle.io.FileWriter (csv)));

            out.println(sb.toString());  //header
            
                for (int row = 0; row < recs; row++) {

                    record = readRecord ();

                    out.println(toComma(record, fields));
                    numProcessed++;
                    //pd.progress(numProcessed);
            }
            
    }catch (Exception e) {  DV.writeFile("db.err", "cvsExport() error :" + System.getProperty("line.separator") , true ) ; }
     finally {
         
         out.close();
     
         //pd.close();
     }

    return numProcessed;

}


public int csvExport (String dbname, File f, int [] fields, ProgressDialogInterface pd) {
    
    use_db (dbname, "r");

    de.schlichtherle.io.File csv = new de.schlichtherle.io.File(f);
    
    int recs = (int) getNumOfRecs();
    
    pd.setBarMax(recs);
    pd.updateBar(0);
    
    int numFields = currentSchema.getNumFields();
    int numProcessed = 0;

    StringBuilder sb = new StringBuilder();
   
    /* Build export header */ 
    
    for (int i = 0; i < numFields; i++ ){
        
        if (i < numFields-1){
            
            sb.append(currentSchema.getFieldName(i)+',');
            
        }else sb.append(currentSchema.getFieldName(i));
        
    }
    
    Object [] record = new Object [numFields];
    
    PrintWriter out= null;
    
    try {

            skipDbHeader();
            out = new PrintWriter (new BufferedWriter(new de.schlichtherle.io.FileWriter (csv)));

            out.println(sb.toString());  //header
            
                for (int row = 0; row < recs; row++) {

                    record = readRecord ();

                    out.println(toComma(record, fields));
                    numProcessed++;
                    //System.out.println("ARE WE GTTING HERE?!!");
                    
                    pd.updateBar(row);
                    //pd.progress(numProcessed);
            }
            
    }catch (Exception e) {  DV.writeFile("db.err", "cvsExport() error :" + System.getProperty("line.separator") , true ) ; }
     finally {
         
         out.close();
     
         
         
     }

    
     pd.close();
     
     return numProcessed;
    
    
}


/** If the return value is over 0 it means an error (mismatch) has occured & denotes the line number of error*/
public int csvImport (String dbname, File f, boolean skipHeader, int [] toFields, boolean overwrite){

    String line = new String();
    use_db(dbname);
    int count = 0;
   
    de.schlichtherle.io.File inputFile = new de.schlichtherle.io.File(f);
    
    BufferedReader in=null;
    try {
                in = new BufferedReader(new de.schlichtherle.io.FileReader(inputFile));
                
                if (skipHeader) in.readLine();
                
                line = in.readLine();
                                                              
                Object [] tmp;

                while (line != null)    {

                    count++;
                   
                    tmp = fromComma (line, toFields) ;  //power method

                    if (tmp == null){  //we tried to put data in the wrong "hole"
                        
                        in.close();
                        in = null;    //clean up
                        line = null;
                        return count;  //didn't match import fields properly (tried to put a Float into Integer fields, etc..)
                        
                    }
                    
                    int t = 0;
                                        
                    /* Cycle through tmp to make sure every field 
                     is initialized to at least blank values */
                    for (int i = 1; i < tmp.length; i++){
                        
                        t = currentSchema.getFieldType(i);
                        
                        if (t == 1) if (!(tmp[i] instanceof String)){
                            tmp[i] = new String ("");                            
                        }
                        if (t == 2) if (!(tmp[i] instanceof Float)){
                            tmp[i] = new Float(0.00f);                            
                        }
                        if (t == 3) if (!(tmp[i] instanceof Integer)){
                            tmp[i] = new Integer(0);
                        }
                        if (t == 4) if (!(tmp[i] instanceof Boolean)){
                            tmp[i] = new Boolean(false);
                        }
                        
                        if (t == 5) if (!(tmp[i] instanceof Long)){
                            tmp[i] = new Long(0);
                        }
                        
                    }
                        
                    if (!overwrite) tmp[0] = new Integer(0);
                        saveRecord (dbname, tmp, false);

                                      
                   line = in.readLine();
                   //processed =+ line.length();
                //pd.setProgress(processed);
                
                }


    }catch (Exception e) {
             
        e.printStackTrace();
        
    }finally {try {
                     
                if (in != null) in.close();
                in = null;
                line = null;
                //pd.close();
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }}
        
    return 0;
}


public int csvImport (String dbname, File f, boolean skipHeader, int [] toFields, ProgressDialogInterface id, boolean overwrite){

    
    String line = new String();
    use_db(dbname);
    int count = 0;

    int size = 0;
        
    de.schlichtherle.io.File inputFile = new de.schlichtherle.io.File(f);
    
   
    id.setBarMax((int)f.length());
    
    id.updateBar(0);
    
    BufferedReader in=null;
    try {
                in = new BufferedReader(new de.schlichtherle.io.FileReader(inputFile));
                
                if (skipHeader){
                    
                    line = in.readLine();
                    size += line.length();
                }
                
                line = in.readLine();
                    size += line.length();
                    
                    id.updateBar(size);
                    
                Object [] tmp;

                while (line != null)    {

                    count++;
                   
                    //System.out.println(line);
                    
                    tmp = fromComma (line, toFields) ;  //power method

                    if (tmp == null){  //we tried to put data in the wrong "hole"
                        
                        in.close();
                        in = null;    //clean up
                        line = null;
                        return count;  //didn't match import fields properly (tried to put a Float into Integer fields, etc..)
                        
                    }
                    
                    int t = 0;
                                        
                    /* Cycle through tmp to make sure every field 
                     is initialized to at least blank values */
                    for (int i = 1; i < tmp.length; i++){
                        
                        t = currentSchema.getFieldType(i);
                        
                        if (t == 1) if (!(tmp[i] instanceof String)){
                            tmp[i] = new String ("");                            
                        }
                        if (t == 2) if (!(tmp[i] instanceof Float)){
                            tmp[i] = new Float(0.00f);                            
                        }
                        if (t == 3) if (!(tmp[i] instanceof Integer)){
                            tmp[i] = new Integer(0);
                        }
                        if (t == 4) if (!(tmp[i] instanceof Boolean)){
                            tmp[i] = new Boolean(false);
                        }
                        
                        if (t == 5) if (!(tmp[i] instanceof Long)){
                            tmp[i] = new Long(0);
                        }
                    }
                        
                    if (!overwrite) tmp[0] = new Integer(0);
                    
                    saveRecord (dbname, tmp, false);

                                      
                   line = in.readLine();
                   if (line != null) size += line.length();
                   id.updateBar(size);
                   
                }
                
                


    }catch (Exception e) {
             
        e.printStackTrace();
        
    }finally {try {
                     
                if (in != null) in.close();
                in = null;
                line = null;
                //pd.close();
                id.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }}
        
    return 0;
}




/* ----------------------- PRIVATE INTERNAL ENGINE METHODS --------------------*/
/* These methods all work on the currentDb and currentSchema  */

    /* Data and Environment Control Methods */

    private void closeDb()	{

        try {

            if (RAF != null) { RAF.close(); RAF = null;}
            nowdb = "_";

        }catch (Exception e) {

            status = "error:13:The application had a probem closing " + currentDb + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";

            DV.writeFile("db.err", "closeDb() error :" + nl, true);  return;

        }

        return;

    }

    private void use_db (String db_name){
       
        use_db(db_name, "rw");  //default read/write mode
        
    }
    private void use_db (String db_name, String mode)	 {


		/* selects db to perform operations on */
		/* initializes currentDb and currentSchema */
		/* Setup of RAF to open currentDb  */

        if (db_name.equals(nowdb) && mode.equals(current_mode)) return;	// <---- Get out of here if trying to use same db

        if (isDbAvailable(db_name)) {

            try {

                closeDb();
                currentSchema = getSchemaObject(db_name);

                currentDb = currentSchema.getDbPath();

                dbFile = new File(currentDb);
                RAF = new RandomAccessFile (dbFile, mode);
                
                current_mode = mode;  // set current file access mode
                
                nowdb = currentSchema.getDbName();

            } catch (Exception e) {

                status = "error:08:The application had a probem accessing " + currentDb + nl +
                " this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";

                DV.writeFile("db.err", "use_db() error :"+ currentSchema.getDbPath() + nl, true);

            }

        }else { DV.writeFile("db.err",db_name + " is not available!" + nl, true); return; }

	return;
       
        
        
        
     }

    /** Moves the file pointer to the first data after the meta */
    private void skipDbHeader (){

        try {
            RAF.seek(4);
        }catch (Exception e) {

            status = "error:08:The application had a probem accessing " + currentDb + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";

            DV.writeFile("db.err", "problem skipping header :" +currentDb+ System.getProperty("line.separator"), true);   }

    }


    private String toComma (Object [] obj){

        StringBuilder temp = new StringBuilder();

        for (int i=0; i < obj.length; i++ ) {  //Start at one (1) to skip key

             if (DV.whatIsIt(obj [i]) == 1) {  // string

                temp.append((String) obj [i]);
            }
             
             if (DV.whatIsIt(obj [i]) == 4) {  // bool

                temp.append(new Boolean((Boolean) obj [i]).toString());
                                
            }
             
             if (DV.whatIsIt(obj [i]) == 2) {  // integer

                temp.append(new Integer((Integer) obj [i]).toString());
                                
            }
             
             if (DV.whatIsIt(obj [i]) == 3) {  // float
                temp.append(new Float((Float) obj [i]).toString());
                                
            }
             
             if (DV.whatIsIt(obj [i]) == 5) {  // long
                temp.append(new Long((Long) obj [i]).toString());
                                
            }

            if (obj.length > 2  && i != obj.length-1) temp.append(",");
        }


        return temp.toString();
    }

    /**
     With this method you can leave out, repeat, reorder any "field" you want from an Object [] ("record")
     */
    private String toComma (Object [] obj, int [] fields){

        StringBuilder temp = new StringBuilder();

        boolean hit = false;
        String tmp;
        
        for (int i=0; i < fields.length; i++ ) {  //Start at one (1) to skip key

                      
            if (fields[i] == 0 ) {
                
                temp.append(new Integer((Integer) obj[0]).toString());
                hit = true;
            } 
            
            if (!hit && DV.whatIsIt(obj [fields[i]]) == 1) {  // string

                tmp = (String) obj [fields[i]];
                                
                temp.append( tmp.replace(',', '.').replace(nl,"_~_") );
                hit = true;
            }
             
             if (!hit && DV.whatIsIt(obj [fields[i]]) == 4) {  // bool

                temp.append(new Boolean((Boolean) obj [fields[i]]).toString());
                hit = true;

                
            }
             
              if (!hit && DV.whatIsIt(obj [fields[i]]) == 2) {  // integer

                temp.append(new Integer((Integer) obj[fields[i]]).toString());
                hit = true;                
            }
             
             if (!hit && DV.whatIsIt(obj [fields[i]]) == 3) {  // float
                temp.append(new Float((Float) obj[fields[i]]).toString());
                hit = true;
                                
            }
            
            if (!hit && DV.whatIsIt(obj [fields[i]]) == 5) {  // long
                temp.append(new Long((Long) obj[fields[i]]).toString());
                hit = true;
                                
            }
            
            hit = false;
            
            if (fields.length > 1  && i != fields.length-1) temp.append(",");
        }


        return temp.toString();
    }

    
    

    private Object [] fromComma (String csv)  {

        int numfields = currentSchema.getNumFields();

        Object [] record = new Object [numfields];

        String [] stringRecord = new String [numfields-1];  //minu one for jey, not any more
        
        char [] line = csv.toCharArray();
        //System.out.println(line.length);

        StringBuilder temp = new StringBuilder (line.length);

        /* This loop grabs each comma separated value */
            int index = 0;
        for (int i = 0; i < stringRecord.length; i++){

                do {
                        if (line[index] == ',' ) break;
                      if (line[index] != '"' ) temp.append(line[index]);  //this took a while to straighten out
                      index++;


          }
                while (  index < line.length && line[index] != ',' );


          temp.trimToSize();
          stringRecord [i] = temp.toString();

            temp.delete(0, temp.length());
            index++;

    }

        //convert strRecord to Object []
        int type=0;
        record [0] = new Integer(0);

        for (int j = 1; j <= stringRecord.length; j++){

            type = currentSchema.getFieldType(j);

            if (type == 0 || type == 3) record[j] = new Integer (Integer.parseInt((String)stringRecord[j-1]));
            if (type == 4) {
                
               record[j] = new Boolean (Boolean.parseBoolean((String) stringRecord[j-1]));
            }
            if (type == 2) record[j] = new Float (Float.parseFloat((String) stringRecord[j-1]));
            if (type == 5) record[j] = new Long (Long.parseLong((String) stringRecord[j-1]));
            if (type == 1) record[j] = new String (stringRecord[j-1]);
        }


        //Free Memory
        temp = null;
        stringRecord = null;
        line = null;

        return record;


    }
    
        private synchronized Object [] fromComma (String csv, int [] toFields)  {

        int numfields = currentSchema.getNumFields();

        Object [] record = new Object [numfields];

        String [] stringRecord = new String [numfields];
        
        char [] line = csv.toCharArray();
        //System.out.println(line.length);

        StringBuilder temp = new StringBuilder (line.length);

        /* This loop grabs each comma separated value */
            int index = 0;
        for (int i = 0; i < stringRecord.length; i++){

                do {
                        if (line[index] == ',' ) break;
                      if (line[index] != '"' ) temp.append(line[index]);  //this took a while to straighten out
                      index++;


          }
                while (  index < line.length && line[index] != ',' );


          temp.trimToSize();
          stringRecord [i] = temp.toString().replace("_~_", System.getProperty("line.separator"));

            temp.delete(0, temp.length());
            index++;

    }

            
            //System.out.println("string record length "+stringRecord.length);

            //System.out.println("to Fields length"+toFields.length);
        //convert strRecord to Object []
        int type=0;
        

        for (int j = 0; j < toFields.length; j++){

            type = currentSchema.getFieldType(j);
            if (type == 1) record[j] = new String (stringRecord[toFields[j]]);
            else { 
                
                try {

                if (type == 0 || type == 3) record[j] = new Integer (Integer.parseInt((String)stringRecord[toFields[j]]));
                if (type == 4) {
                    
                   record[j] = new Boolean (Boolean.parseBoolean((String) stringRecord[toFields[j]]));
                }
                if (type == 2) record[j] = new Float (Float.parseFloat((String) stringRecord[toFields[j]]));
                if (type == 5) record[j] = new Long (Long.parseLong((String) stringRecord[toFields[j]]));
            } catch (NumberFormatException ex) {
                
                temp = null;
                stringRecord = null;
                line = null;
                return null;
                
            }
            
            }
            
        }


        //Free Memory
        temp = null;
        stringRecord = null;
        line = null;

        if (record[0] != null && record[0] instanceof Integer ) return record;
        else {
            
            record [0] = new Integer(0);
            return record;
            
        }


    }


    public String [] getFieldNames (String dbname) {

        use_db(dbname);
        
        int a = currentSchema.getNumFields();
        String [] names = new String [a];

        for (int j = 0; j < names.length;  j++) {

            names [j] = currentSchema.getFieldName (j);

        }

        return names;
    }

    public int getFieldSize (String dbname, int col) {

        use_db(dbname);

     return currentSchema.getFieldSize(col); /*FIXME*/

    }


    private Object [] readRecord() {

        int numfields = currentSchema.getNumFields();

	int type = 0;

	int fs = 0;  //fieldsize

	Object [] data = new Object [numfields];

	try {

            for (int i = 0; i < numfields; i++)  {

                type = currentSchema.getFieldType(i);
		//System.out.println("Field " +i +":"+type);
		if (type == 0 || type == 3){
                    
                    data[i] = new Integer(RAF.readInt());
                    continue;
                }
		if (type == 4) data[i] = new Boolean (RAF.readBoolean());
                if (type == 2) data[i] = new Float (RAF.readFloat());
                if (type == 5) data[i] = new Long (RAF.readLong());
                if (type == 1) {

                    fs = currentSchema.getFieldSize(i);
                    data[i] = readStr(fs);
                    
                }

            }

	} catch (Exception e) {

            status = "error:08:The application had a probem accessing " + currentDb + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";

            DV.writeFile("db.err", "readRecord :" + currentSchema.getDbPath()+ System.getProperty("line.separator"), true);

        }


        return data;
    }

    private int writeRecord (Object [] data) {

        Integer K = (Integer) data[0];
        int key = K.intValue();

        try {
            if (key == 0) {

                data[0] = new Integer(currentSchema.nextKey(RAF,true));
		RAF.seek(dbFile.length()); /* Move to EOF */
		K = (Integer) data[0];
                key = K.intValue();

            }else { seekToKey(key);  }




            int nf = data.length;


            for (int i = 0; i < nf ; i++) {

		int ftype = currentSchema.getFieldType(i);

                /* STRING */
		if (ftype == 1) {

                    String s = (String) data[i];
                    
                    //byte[] str = s.getBytes(); /*CHANGE*/

                    //int fsize = currentSchema.getFieldSize(i) / 2 ;
                    int fsize = currentSchema.getFieldSize(i); /*CHANGE*/
                    
                    //for (int c = 0; c < fsize ; c++) {

                      //  if (c < s.length()) RAF.writeChar(s.charAt(c)); /*FIXME*/
                      //  else RAF.writeChar(0);
                        
                    if (s.length() > fsize) s = s.substring(0, fsize);
                    if (s.length() < fsize) s = padString(s, fsize);
                    
                        RAF.writeBytes(s);
                        
                        
                   // }

		}

			/* Integer - KEY */

		if (ftype == 0 || ftype == 3) {

                    Integer I = (Integer) data[i];
                    RAF.writeInt(I.intValue());
                    

		}

                        /* MONEY $$ */
		if (ftype == 2) { /* NUM */

                    Float f = (Float) data[i];
                    RAF.writeFloat(f.floatValue());
                    
                }

                         /* LONG */
		if (ftype == 5) { /* LONG */

                    Long l = (Long) data[i];
                    RAF.writeLong(l.longValue());
                    
		}

                
                    /* YES/NO */
		if (ftype == 4) { /* YESNO */

                    Boolean b = (Boolean) data[i];
                    RAF.writeBoolean(b.booleanValue());
                    

		}

            }

        closeDb();

        } catch (Exception e) {

            status = "error:08:The application had a probem accessing " + currentDb + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";

            DV.writeFile("db.err", "writeRecord :" + currentDb+ nl, true); return -1;

        }

	return key;

    }

    private String readStr(int size)  {
/*FIXME*/
	//StringBuilder temp = new StringBuilder (size);
	byte [] str = new byte[size];
                        
	try {
        
            /*for (int c = 0; c < size; c++) { 

                ch =  RAF.readByte();
		if (ch != 0) temp.append(ch);

            }*/
                
                RAF.read(str);

	} catch (Exception e) {

                status = "error:08:The application had a problem accessing " + currentDb + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";

            }

        //System.out.println(temp.toString());
        
	return new String (str).trim();

    }

    private boolean seekTo (long pos){
        try {
        
        
            RAF.seek(pos);
        } catch (IOException ex) {
            
            status = "error:08:The application had a probem accessing " + currentDb + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";
            
            return false;
            
        }
                
        return true;
        
    }
    
    /** Simple method moves the file pointer to the proper record.  It starts from the begining each time. */
    private boolean seekToKey (int key) {

        try {
            
            skipDbHeader();
            long numrecs = getNumOfRecs();
            int recsize = currentSchema.getRecordSize();
            int jump = recsize - 4;
            int k = 0;

            for (int i = 0; i < numrecs; i++)  {

                k = RAF.readInt();
		if (k == key) { RAF.seek(RAF.getFilePointer() - 4); return true;  }
                    else RAF.seek(RAF.getFilePointer() + jump);

            }

	} catch (Exception e) {

            status = "error:08:The application had a probem accessing " + currentDb + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";

            DV.writeFile("db.err", "seekToKey :" + currentDb+ System.getProperty("line.separator"), true);

            }

	return false;
    }



    /** Use this to prevent a file pointer reset every time you call seekToKey() in a loop.  */
    private boolean seekToKey (int key, boolean reset) {

        try {
            if (reset) {
            
            skipDbHeader();
            }

            long numrecs = getNumOfRecs();
            int recsize = currentSchema.getRecordSize();
            int jump = recsize - 4;
            int k = 0;

            for (int i = 0; i < numrecs; i++)  {
                
                try {
                    
                k = RAF.readInt();
                
                } catch (Exception e) { RAF.seek(4); }
                
		if (k == key) { RAF.seek(RAF.getFilePointer() - 4); return true;  }
                    else RAF.seek(RAF.getFilePointer() + jump);

            }

	} catch (Exception e) {

            status = "error:08:The application had a probem accessing " + currentDb + nl +
                ", this could be a problem with the disk/OpSys or file permissions." +
                nl + " Verify the permissions and hardware integrity.";


            DV.writeFile("db.err", "seekToKey(r) :" + currentDb+ nl, true); return false;

        }

	return false;
    }

    
    
    
    
    private boolean isRecordUnique(Object [] data) {

        /* NOT Working */
	/* Search db for equal record */

        return false;

    }

    /** Takes a record and checks it aginst the schema to make sure field types match  */
    private boolean isRecordValid (Object [] data) {

        return currentSchema.isDataCorrect(data);

    }

    private boolean dbVerify () {

        long totalsize = dbFile.length()-4; //-4 remove nextkey bytes from equation

	if (totalsize == 4) return true;  //empty but setup with nextkey

	int rowsize = currentSchema.getRecordSize() ;

	long remainder = totalsize % rowsize;

	if (remainder == 0) return true;



        status = "error:2: A mismatch was found with the data," + nl +
                " a 3rd party memory intrusion could be the problem if this is irregular behavior." +
                nl + " If the problem persists restore from backup.";


        DV.writeFile("db.err", "db is corrupt err2:" + currentDb+ nl, true);

        return false;  /* if the db is "off" return false */

     }

    private long getNumOfRecs () {

        if (dbVerify()) return dbFile.length() / currentSchema.getRecordSize();
	return -1;   //error

    }

	/* ---------- ADD-ON METHODS ----------- */

    /**regex and targetCols must be the same length.  Supply a regex for each column you want to search, 
     * then assign a target col for each regex by placing a column number at the same index of targetCols as the regex String.
       Supply the String type "AND" or "&" or "" (leave blank) when you require all the regexes to return true.
       Supply the String type "OR" or "|" if only one needs to be true.*/
    public ArrayList regexSearch (String dbname, String [] regex, int [] targetCols, String type){
        
        boolean and = true;
        type = type.trim();
                
        if (type.equalsIgnoreCase("and") || type.equals("") || type.equals("&&") || type.equals("&")) and = true;
            else and = false;  //OR
        
        return regexCols(dbname, regex, targetCols, and);
        
    }
    
    private ArrayList regexCols (String dbname, String [] regex, int [] targetCols, boolean and) {
        
        use_db (dbname, "r");

        ArrayList al = new ArrayList();
    
        Pattern [] p = new Pattern [targetCols.length];
        Matcher [] m = new Matcher[targetCols.length];
        
        for (int i = 0; i < p.length; i++){
          
                try {
                
                    p[i] = Pattern.compile(regex[i]);
                
                }catch (PatternSyntaxException se){
                    
                    System.out.println("Pattern "+ (i+1) + " is invalid.");
                    se.printStackTrace();
                    return null;
                }
            
        }
        
       int recs = (int) getNumOfRecs();
       Object [] record;
       String [] strRec = new String [targetCols.length];
        
       skipDbHeader();
        
       boolean miss = false;
         boolean match = false;
         
       for (int r = 0; r < recs; r++){  //big loop
                
           record = readRecord ();
           
           /*Convert the whole record to strings  */
           for (int x = 0; x < targetCols.length; x++){
               
               strRec[x] = DV.convertToString(record[targetCols[x]]);
               
           }
           
           for (int i = 0; i < m.length; i++){
            
               m[i] = p[i].matcher(strRec[i]);
            
           }
           
           miss = false;
           match = false;
           
           for (int i = 0; i < m.length; i++){
                          
               if (!m[i].matches()) miss = true;
               else match = true;
           }
           
           if (!and && match) al.add((Integer)record[0]) ;
           else if (and && !miss) al.add((Integer)record[0]);
                   
       }
            
       al.trimToSize();
       
       return al;
        
    }
    
    
    /** Returns the key(s) of the records found to contain 'text'
        in the specified column  */
    private ArrayList scanColumn (int column, String txt, boolean substring, boolean fast)  {

        //if fast return positions instead of keys
        if (txt.equals("")) return null;

        ArrayList results = new ArrayList ();
        
        String text = "";
        //new
        int x = 0;
        float flt = 0f;
        boolean boo = true;
        long lng = 0;
        
        
        
        try {

		/* This method takes a string and compares it to the value
                   obtained from the specified column.  Basically all
                   values from any column are converted to a string
                   for comparison.

                 This is a high-performance search method; it basically skips
                 through the disk touching only the data needed for comparison*/


            long numrecs = getNumOfRecs();

            int numfields = currentSchema.getNumFields();

            int fieldsize = currentSchema.getFieldSize(column);

            int recsize = currentSchema.getRecordSize();

            int type = currentSchema.getFieldType(column);

            if (type == 1) text = txt.trim().toUpperCase();  //new
            if (type == 2 ) flt = Float.parseFloat(txt);
            if (type == 3) x = Integer.parseInt(txt);
            if (type == 4) boo = Boolean.parseBoolean(txt);
            if (type == 5) lng = Long.parseLong(txt);
            
            // calculate bytes before and after search field for each record

            int bytesbefore = 0;

            for (int i = 0; i < column; i++) {
                bytesbefore += currentSchema.getFieldSize(i);
            }

            int bytesbetween = bytesbefore - 4;

            int bytesafter =0;

            if (column < numfields-1) {

                for (int i = column+1; i < numfields; i++) {
                    bytesafter += currentSchema.getFieldSize(i);
                }

            }

            int k = 0;
            Float f;
            Boolean b;
            String tmp;
            Integer z;
            Long l;

            skipDbHeader();

            for (int i = 0; i < numrecs; i++)  {

		k = RAF.readInt();  //read key - advance 4 bytes
		RAF.seek(RAF.getFilePointer() + bytesbetween); //advance to search field
		//z = new Integer (k);

                if (type == 1) {

                    tmp = readStr(fieldsize).trim().toUpperCase(); //read compare return

                    if (substring) {

                        if (tmp.contains(text)){
                            
                           if (fast){
                            
                               long rv = RAF.getFilePointer() - bytesbetween - 4 - fieldsize;
                               
                            results.add(rv);
                           
                           }else results.add(k);
                           
                        }

                    }else if (tmp.equals(text)){
                        
                        if (fast) {
                            
                            long rv = RAF.getFilePointer() - bytesbetween - 4 - fieldsize;
                            results.add(rv);
                            
                        }else results.add(k);
                        
                    }

                }

                if (type == 2) { //float

                    f = (Float) RAF.readFloat();
                    //tmp = f.toString().toUpperCase();
                    //if (tmp.equals(text)) results.add(k);
                   if (f == flt){
                        
                        if (fast){
                            
                            long rv = RAF.getFilePointer() - bytesbetween - 4 - fieldsize;
                            results.add(rv);
                            
                        }else {
                            
                            results.add(k);
                            
                        }
                        
                   }
                    
                }

                
                if (type == 5) { //long

                    l = (Long) RAF.readLong();
                    //tmp = f.toString().toUpperCase();
                    //if (tmp.equals(text)) results.add(k);
                   if (l == lng){
                        
                        if (fast){
                            
                            long rv = RAF.getFilePointer() - bytesbetween - 4 - fieldsize;
                            results.add(rv);
                            
                        }else {
                            
                            results.add(k);
                            
                        }
                        
                   }
                    
                    
                }
                
                if (type == 3) { //int

                    z = (Integer) RAF.readInt();
                    //tmp = z.toString().toUpperCase();
                    //if (tmp.equals(text)) results.add(k);
                    if (z == x) {
                        
                        if (fast){
                            
                            long rv = RAF.getFilePointer() - bytesbetween - 4 - fieldsize;
                            results.add(rv);
                            
                        }else {
                            
                            results.add(k);
                            
                        }
                     
                    }
                    
                    
                    
                }

                if (type == 4) { //boolean

                    b = (Boolean) RAF.readBoolean();
                    //tmp = b.toString().toUpperCase();
                    //if (tmp.equals(text)) results.add(k);
                    if (boo == b){
                        if (fast){
                            
                            long rv = RAF.getFilePointer() - bytesbetween - 4 - fieldsize;
                            results.add(rv);
                            
                        }else {
                        
                            results.add(k);
                            
                        }
                        
                    }
                    

                }
                RAF.seek(RAF.getFilePointer() + bytesafter); //move to beginning of first field
            }
		results.trimToSize();  //clean up
                if (results.size() < 1) return null; //results.add( new Integer(0) ); //check for junk
                return results;

        } catch (Exception e) {

            status = "error:111:The application asked for a schema that doesn't exsist," + nl +
                " a 3rd party memory intrusion could be the problem if this is irregular behavior." +
                nl + " Restart the system and/or application.";

            DV.writeFile("db.err", "scanCol :" + currentDb+ nl, true);

        }

        results.trimToSize();// clean up ArrayList before returning nothing
        //System.out.println( results.size() );
        if (results.size() < 1) return null;
        return results;

    }


/*-------------------- DbEngine System Methods --------------------*/

public void close() {

    closeDb();

}



    public boolean loadSchema (String filename)   {
    // adjust arraylist that holds schema references to hold one more
    // load new schema
    File SCH = new File (filename);
    //System.out.println(filename);
    if (SCH.exists() && SCH.canRead() && SCH.canWrite()) {

        schList.add(new DbSchema (filename));
        return true;

    }else {

        status = "error:03:The application asked for a schema that can't be accessed." + nl +
                filename  + " is missing or cannot be accessed. " +
                nl + " Restore the data from backup.";


        return false;  // error
    }


}
	/* Initializer Method */
    private void loadDSF (String filename)  {
        //System.out.println(filename);
	java.io.File DSF = new java.io.File (filename);

		/* PARSE */
	if (DSF.exists())   {
            
            BufferedReader in;
            
            try {

                in = new BufferedReader(new FileReader (DSF));

                num_files = Integer.parseInt(in.readLine());  //read the first line store the result INT

		//schema = new DbSchema[num_files];

		for (int i=0; i < num_files; i++) {

                    if (loadSchema (in.readLine())) {

                    }else {

                        status = "error:23:The application had a probem loading a schema " + nl +
                            " It could have been deleted or damaged, restore from backup." +
                            nl + " Verify the permissions and hardware integrity.";

                        DV.writeFile("db.err", "loadDSF schema Error :" + currentDb+ nl, true);
                        in.close(); /* If forgot to close this for a couple years. :)  Lesson learned. */
                            
                    }
		}
                
                in.close();  /* If forgot to close this for a couple years. :)  Lesson learned. */
                
            }catch (Exception e) {

                status = "error:23:The application had a probem loading a Data System " + nl +
                            " It could have been deleted or damaged, restore from backup." +
                            nl + " Verify the permissions and hardware integrity.";

                DV.writeFile("db.err", "DSF Error :" + currentDb+ nl, true);
                
            }
              
            
	}else {

            status = "error:00:DSF Missing! " + nl +
                            " It could have been deleted or damaged, restore from backup." +
                            nl + " Verify the permissions and hardware integrity.";

            DV.writeFile("db.err", "DSF missing :" + filename+ nl, true);
        }

	return; // RETURN ERROR!? FIX!!
    }


    private DbSchema getSchemaObject (String dbname)	{

        int sl = schList.size() - 1;  /* Assign method return to var and count back for speed */
	DbSchema s;

        for (int i=sl; i >= 0; i--)	{
            s = (DbSchema) schList.get(i);
            if (s.getDbName().equalsIgnoreCase(dbname)) return s;

        }
        status = "error:09:The application asked for a schema that doesn't exsist," + nl +
                " a 3rd party memory intrusion could be the problem if this is irregular behavior." +
                nl + " Restart the system and/or application.";

        DV.writeFile("db.err", "Problem getting Schema :" + dbname+ nl, true);
        return null;

    }

public String getStatus () {

    return status;

}

private String padString (String s, int total_length){
    
    StringBuilder sb = new StringBuilder (s);
            
        int length = (total_length ) - s.length();

       for (int i = 0; i < length; i++) {

           sb.append(' ');  //append space 

       }
                   
       sb.trimToSize();
       return sb.toString();
       
}
public boolean changePath(String db, String newPath) {
    
    this.use_db(db,"r");
    boolean changed = currentSchema.changePath(newPath);
    
    if (changed) {
        
        this.use_db(db,"rw");//change the mode address to generate a new file
        return changed;
    }
    
    return changed;
    
    
}

/* -------------  Global Objects & Variables ---------------*/

private File dbFile = null;  /* currentDb File Object */

private RandomAccessFile RAF = null; /* currentDb Random Access File Object */

private DbSchema currentSchema = null; /* Access this to get the current db name */
//private DbSchema [] schema; /* Schema-object-reference-list populated by loadDSF */
private ArrayList schList = new ArrayList (0);
private String currentDb = "_"; /* In here currentDb is the full path\filename.ext */

private int num_files = 0;   // in use?
private String nowdb = "_";

private TableSorter sorter = null;
private String nl = System.getProperty("line.separator");

private String status = "No db Errors";

private String current_mode = "rw";  

}/*END DbEngine 1.0 CLASS*/
