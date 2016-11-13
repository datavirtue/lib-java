
/****************************************************************************
     RemoteDbEngine 1.0 CORE+
     Created by Sean Kristen Anderson - Data Virtue 2005 - 2007
     CopyRight Data Virtue 2005, 2006, 2007  All Rights Reserved.
 *  
 *  
*****************************************************************************/
package com.datavirtue.db.remote;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.BufferedReader;  
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import javax.swing.table.*;
import javax.swing.JTable;
import com.datavirtue.gui.util.TableSorter;
import com.datavirtue.tools.DV;
import com.datavirtue.db.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class RemoteDbEngine extends UnicastRemoteObject 
        implements RemoteDbEngineInterface{

 public RemoteDbEngine () throws RemoteException {

    super();

    /* Better use loadSchema after calling with this constructor */

}/* END CONSTRUCTOR */


 public RemoteDbEngine (String DSF) throws RemoteException {
	// make another constructor to handle a single .sch
    /* Process Database System File */
     super();
     loadDSF(DSF);

}/* END CONSTRUCTOR */

/*-------------- PUBLIC  API Data Processing Methods ------------*/

/* These methods work on the "currentDb" global */

  
    /**
     *In use
     */
 
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
 
 public Object [] removeRecord (String dbname, int key) throws RemoteException {
        
    synchronized (this){
          if (isLocked()) return new Object [] {new String("B"), null};
        lock();}
        
        /* Launch this in a new thread ? */
        
        try {
            use_db(dbname, "r"); //we are only going to read
            
            RAF.seek(0);  

            File newFile = new File(currentSchema.getDbPath()+"x");//temp file

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

        status = "DB ERROR: removeRecord"+ " in "+nowdb;

        unlock();
        //DV.writeFile("db.err", "removeRecord() error :" +currentSchema.getDbName()+".db" + nl , true ); return false;


    }

    
        /* unlock */
        unlock();
        return new Object [] {new String ("O"), new Boolean(true)};
    
    
}

    
    
    /** Reads and returns a record from disk of the specified dbname - key */
      public Object [] getRecord (String dbname, int key) throws RemoteException {
        
          
          synchronized (this){
          if (isLocked()) return new Object [] {new String("B"), null};
        lock();}
        /*
         * enclose this method in a loop based on use_db
         * use_db checks to see if it is locked if not the action is performed
         * if it is locked use_db returns a certain val that can be checked 
         * and logic branched accordingly
         */
        
        use_db(dbname, "r");
        if (seekToKey(key)){
            Object [] a = new Object [] {new String ("O"), readRecord()};
            unlock();
            return a;
        }
	unlock();
        return new Object [] {new String ("X"), null};

     }
     
    /* Added for performance reasons */
      public Object [] getRecord (String dbname, long pos) throws RemoteException {  
        
          
          synchronized (this){
          if (isLocked()) return new Object [] {new String ("B"), null};
        lock();}
        
        use_db(dbname, "r");
        
        if (seekTo(pos)){
                   
            Object [] data = new Object [] {new String ("O"), readRecord()};
                  unlock(); 
            return data;
            
        }else {
            unlock();
            return null;
        }
        
        
    }
    
    
    /** Saves (insert - update) the record you provide in 'data'.  The first
     * Object [0] must be Integer ZERO for new record or an exsisting key within the
     * database.  This method returns the key used */
      public int saveRecord (String dbname, Object [] data, boolean unique) throws RemoteException {

          
          synchronized (this){
          if (isLocked()) return -2;
        lock();}
          /* return -2 for busy */
          
        use_db (dbname);
	if (!dbVerify()) { unlock();System.out.println ("ERROR: " + nowdb + " is corrupt!"); return -1; }

        if (isRecordValid(data)) {
            if (unique) {
                if (isRecordUnique(data)) {
                    int x = writeRecord(data);
                    unlock();
                    return x;
		}else{
                    unlock();
                    return 0;  //this means record was specified unique but was not
                }
            }
            
            int a = writeRecord(data);
            /* UNLOCK */
            unlock();
	return a;

	} else{
            
            unlock();
            return -1;  //bad data
        }

    }

    
 
    /* ---------- ADD-ON PUBLIC METHODS ----------- */
      public Object [] search (String dbname, int col, String searchText, Boolean substring) throws RemoteException {

        synchronized (this){
          if (isLocked()) return new Object [] {new String ("B"), null};
        lock();}
        
        use_db(dbname,"r");
	Object [] rv = new Object [] {new String ("O"), scanColumn (col, searchText, substring, false)};
        unlock();
        return rv;

    }

      public Object [] searchFast (String dbname, int col, String searchText, Boolean substring) throws RemoteException {

          synchronized (this){
          if (isLocked()) return new Object [] {new String ("B"), null};
        lock();}
        use_db(dbname, "r");
        Object [] rv = new Object [] {new String ("O"), scanColumn (col, searchText, substring, true)};
	unlock();
        return rv; 

    }
    
    
      public Object [] createTableModel (String db, ArrayList list) throws RemoteException {

         synchronized (this){
          if (isLocked()) return new Object [] {new String ("B"), null};
        lock();}
        
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

        
       /* sorter = new TableSorter(new DefaultTableModel(data,headers));
        sorter.setTableHeader(jt.getTableHeader());
        data = null;
        return sorter;*/
        unlock();    
        return new Object [] {new String ("O"), data};

       
       
    }

       
        /** Do not pass in a null ArrayList */
          public Object [] createTableModelFast (String db, ArrayList list) throws RemoteException {

              
              synchronized (this){
          if (isLocked()) return new Object [] {new String ("B"), null};
        lock();}
        
        if (list == null){
           
           unlock();
           return new Object [] {new String ("O"),null};  //do not pass a null value in!!!!
        }

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
      

        unlock();
        return new Object [] {new String ("O"), data};

    }

  
      public Object [] createTableModel (String db) throws RemoteException {

        synchronized (this){
          if (isLocked()) return new Object [] {new String ("B"), null};
        lock();}
          
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


        unlock();
        return new Object [] {new String ("O"), data};
        
        
    }


      private boolean isDbAvailable (String dbname, boolean xp){
        
                 
          for (int i = 0; i < schList.size(); i++) {
            DbSchema s = (DbSchema) schList.get(i);
            if (s.getDbName().equalsIgnoreCase(dbname)) return true;
	}

	return false;
        
        
    }


 
/* ----------------------- PRIVATE INTERNAL ENGINE METHODS --------------------*/
/* These methods all work on the currentDb and currentSchema  */

    /* Data and Environment Control Methods */

      private void closeDb()	{

        if (nowdb.equals("_")) return;
        
        try {

            
            
            if (RAF != null) { RAF.close(); RAF = null;}
            System.out.println(nowdb + "~ Closed: " + new java.util.Date(System.currentTimeMillis()).toString());
            nowdb = "_";

        }catch (Exception e) {

            status = "DB ERROR: closeDb"+ " in "+nowdb;
            System.out.println(status);
            //DV.writeFile("db.err", "closeDb() error :" + nl, true);  return;

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

        if (!nowdb.equals("_")){ 

            	/* Get out of here if trying to use same db*/
            if (db_name.equalsIgnoreCase(nowdb) && mode.equals(current_mode)) return;
            
        }
        
      
        if (isDbAvailable(db_name, true)) {

            try {

                if (!nowdb.equals("_"))closeDb();
                
                currentSchema = getSchemaObject(db_name);

                currentDb = currentSchema.getDbPath();

                dbFile = new File(currentDb);
                RAF = new RandomAccessFile (dbFile, mode);
                
                current_mode = mode;  // set current file access mode
                
                nowdb = currentSchema.getDbName();
                
                System.out.println(db_name + " ~ Accessed: " + new java.util.Date(System.currentTimeMillis()).toString());

            } catch (Exception e) {

                     
                status = "DB ERROR: use_db"+ " trying "+nowdb;
                System.out.println(status);
                /*DV.writeFile("db.err", "use_db() error :"+ currentSchema.getDbPath() + nl, true);*/

            }

        }else { /*DV.writeFile("db.err",db_name + " is not available!" + nl, true);*/ return; }

            
	return;
        
        
     }

    /** Moves the file pointer to the first data after the meta */
      private void skipDbHeader (){

        try {
            RAF.seek(4);
        }catch (Exception e) {

            status = "DB ERROR: skipDbHeader"+ " in "+nowdb;
            System.out.println(status);
            /*DV.writeFile("db.err", "problem skipping header :" +currentDb+ System.getProperty("line.separator"), true);*/   }

    }


      private String toComma (Object [] obj){

        StringBuilder temp = new StringBuilder();

        for (int i=1; i < obj.length; i++ ) {  //Start at one (1) to skip key

             if (DV.whatIsIt(obj [i]) == 1) {  // string

                temp.append((String) obj [i]);
            }
             
             if (DV.whatIsIt(obj [i]) == 4) {  // bool

                temp.append(new Boolean((Boolean) obj [i]).toString());
                
                
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

        for (int i=0; i < fields.length; i++ ) {  //Start at one (1) to skip key

             if (DV.whatIsIt(obj [fields[i]]) == 1) {  // string

                temp.append((String) obj [fields[i]]);
            }
             
             if (DV.whatIsIt(obj [fields[i]]) == 4) {  // bool

                temp.append(new Boolean((Boolean) obj [fields[i]]).toString());
                                
            }
             
              if (DV.whatIsIt(obj [fields[i]]) == 2) {  // integer

                temp.append(new Integer((Integer) obj[fields[i]]).toString());
                                
            }
             
             if (DV.whatIsIt(obj [fields[i]]) == 3) {  // float
                temp.append(new Float((Float) obj[fields[i]]).toString());
                                
            }
            
            if (fields.length > 2  && i != fields.length-1) temp.append(",");
        }


        return temp.toString();
    }
      
      

      private Object [] fromComma (String csv)  {

        int numfields = currentSchema.getNumFields();

        Object [] record = new Object [numfields];

        String [] stringRecord = new String [numfields-1];
        char [] line = csv.toCharArray();
        //System.out.println(line.length);

        StringBuilder temp = new StringBuilder (line.length);

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
            if (type == 1) record[j] = new String (stringRecord[j-1]);
        }


        //Free Memory
        temp = null;
        stringRecord = null;
        line = null;

        return record;


    }

      public Object [] getFieldNames (String dbname) throws RemoteException {

          synchronized (this){
          if (isLocked()) return new Object [] {new String ("B"), null};
        lock();}
        use_db(dbname);
        
        int a = currentSchema.getNumFields();
        String [] names = new String [a];

        for (int j = 0; j < names.length;  j++) {

            names [j] = currentSchema.getFieldName (j);

        }

        unlock();
        return new Object [] {new String ("O"),names};
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
		if (type == 0 || type == 3) data[i] = new Integer(RAF.readInt());
		if (type == 4) data[i] = new Boolean (RAF.readBoolean());
                if (type == 2) data[i] = new Float (RAF.readFloat());

                if (type == 1) {

                    fs = currentSchema.getFieldSize(i);
                    data[i] = readStr(fs);
                    
                }

            }

	} catch (Exception e) {

            status = "DB ERROR: readRecord"+ " in "+nowdb;
            System.out.println(status);
            //DV.writeFile("db.err", "readRecord :" + currentSchema.getDbPath()+ System.getProperty("line.separator"), true);

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
                  
                        
                    if (s.length() > fsize) s = s.substring(0, fsize);
                    if (s.length() < fsize) s = padString(s, fsize);
                    
                        RAF.writeBytes(s);
                 

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


                    /* YES/NO */
		if (ftype == 4) { /* YESNO */

                    Boolean b = (Boolean) data[i];
                    RAF.writeBoolean(b.booleanValue());

		}

            }

        closeDb();

        } catch (Exception e) {

            status = "DB ERROR: writeRecord"+ " in "+nowdb;
            System.out.println(status);
            //DV.writeFile("db.err", "writeRecord :" + currentDb+ nl, true); return -1;

        }

	return key;

    }

      private String readStr(int size)  {

	//StringBuilder temp = new StringBuilder (size);
	byte [] str = new byte[size];
                        
	try {
        
            /*for (int c = 0; c < size; c++) { 

                ch =  RAF.readByte();
		if (ch != 0) temp.append(ch);

            }*/
                
                RAF.read(str);

	} catch (Exception e) {

                status = "DB ERROR: readStr"+ " in "+nowdb;
                System.out.println(status);
            }

        //System.out.println(temp.toString());
        
	return new String (str).trim();

    }

      private boolean seekTo (long pos){
        try {
        
        
            RAF.seek(pos);
        } catch (IOException ex) {
            
            status = "DB ERROR: seekTo"+ " in "+nowdb;
            System.out.println(status);
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

            status = "DB ERROR: seekToKey"+ " in "+nowdb;
            System.out.println(status);
            e.printStackTrace();
            
            //DV.writeFile("db.err", "seekToKey :" + currentDb+ System.getProperty("line.separator"), true);

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

            status = "DB ERROR: seekToKey, reset ver"+ " in "+nowdb;
            System.out.println(status);

            //DV.writeFile("db.err", "seekToKey(r) :" + currentDb+ nl, true); return false;

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

      public Object [] isValidRecord (String dbname, Object [] data) throws RemoteException{
          
          synchronized (this){
          if (isLocked()) return new Object [] {new String ("B"), null};
        lock();}
        use_db(dbname, "r");
        boolean result = false;
        try {
            result = currentSchema.isDataCorrect(data);
        }finally {unlock();}
        
        return new Object [] {new String ("O"), new Boolean(result)};
        
          
      }
     
      private boolean dbVerify () {

        long totalsize = dbFile.length()-4; //-4 remove nextkey bytes from equation

	if (totalsize == 4) return true;  //empty but setup with nextkey

	int rowsize = currentSchema.getRecordSize() ;

	long remainder = totalsize % rowsize;

	if (remainder == 0) return true;



        status = "DB ERROR: dbVerify, DB Corrupt"+ " in "+nowdb;
        System.out.println(status);


        //DV.writeFile("db.err", "db is corrupt err2:" + currentDb+ nl, true);

        return false;  /* if the db is "off" return false */

     }

      private long getNumOfRecs () {

        if (dbVerify()) return dbFile.length() / currentSchema.getRecordSize();
	return -1;   //error

    }

	/* ---------- ADD-ON METHODS ----------- */

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
        
        
        
        
        try {

		/* This method takes a string and compares it to the value
                   obtained from the specified column.  Basically all
                   values from any column are converted to a string
                   for comparison.

                 This is a high-performance search method; it basically skips
                 through the disk touching only the data needed for comparison*/


            long numrecs = getNumOfRecs();

            if (numrecs == -1){
                
                System.out.println("DB ERROR: getNumRecs() returned -1");
                locked=false;
                return null;
                
                
            }
            
            
            int numfields = currentSchema.getNumFields();

            int fieldsize = currentSchema.getFieldSize(column);

            int recsize = currentSchema.getRecordSize();

            int type = currentSchema.getFieldType(column);

            if (type == 1) text = txt.trim().toUpperCase();  //new
            if (type ==2 ) flt = Float.parseFloat(txt);
            if (type == 3) x = Integer.parseInt(txt);
            if (type == 4) boo = Boolean.parseBoolean(txt);
            
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

            status = "DB ERROR: scanColumn"+ " in "+nowdb;
            System.out.println(status);
            e.printStackTrace();
            //DV.writeFile("db.err", "scanCol :" + currentDb+ nl, true);

        }

        results.trimToSize();// clean up ArrayList before returning nothing
        //System.out.println( results.size() );
        if (results.size() < 1) return null;
        return results;

    }


/*-------------------- DbEngine System Methods --------------------*/

  public void close() throws RemoteException{

    closeDb();

}



      private boolean loadSchema (String filename)  {
    // adjust arraylist that holds schema references to hold one more
    // load new schema
    File SCH = new File (filename);
    //System.out.println(filename);
    if (SCH.exists() && SCH.canRead() && SCH.canWrite()) {

        schList.add(new DbSchema (filename));
        System.out.println(filename + " ~ Schema loaded: "+new java.util.Date(System.currentTimeMillis()).toString());
        return true;

    }else {

        status = "DB ERROR: loadSchema"+ " in "+filename;
        System.out.println(status);


        return false;  // error
    }


}
	/* Initializer Method */
      private void loadDSF (String filename)  {
        
	File DSF = new File (filename);

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

                        status = "DB ERROR: loadDSF, loading schemas "+ " in "+filename;
                        System.out.println(status);
                        //DV.writeFile("db.err", "loadDSF schema Error :" + currentDb+ nl, true);
                        in.close(); /* If forgot to close this for a couple years. :)  Lesson learned. */
                            
                    }
		}
                
                in.close();  /* If forgot to close this for a couple years. :)  Lesson learned. */
                
            }catch (Exception e) {

                status = "DB ERROR: loadDSF"+ " in "+filename;
                System.out.println(status);
                //DV.writeFile("db.err", "DSF Error :" + currentDb+ nl, true);
                
            }
              
            
	}else {

            status = "DB ERROR: DSF Missing"+ " in "+filename;
            System.out.println(status);
            //DV.writeFile("db.err", "DSF missing :" + filename+ nl, true);
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
        status = "DB ERROR: getSchemaObject"+ " in "+nowdb;
        System.out.println(status);
        //DV.writeFile("db.err", "Problem getting Schema :" + dbname+ nl, true);
        return null;

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


private int retry = 100;  //retry db access X times

}/*END DbEngine 1.0 CLASS*/
