
/*
 * Data Virtue Convienience Tools
 * Written by Sean Anderson MAY 2005 - 2006
 *
 */


package com.datavirtue.tools;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.Properties;
import java.text.*;
import java.io.*;
import javax.swing.text.html.*;
import javax.swing.JEditorPane;
import javax.swing.table.*;

public class DV	{


  
    public static String chop (String s, int max_size){
        
        if (s.length() > max_size){
                        
            return s.substring(0, max_size );
                        
        }else return s;
        
        
        
    }
    
    
    
    public static int [] whichContains (String [] ls, String clip){
        
        //searches an array of strings to find clip
        
        java.util.ArrayList al = new java.util.ArrayList();
        
        
        for (int i = 0; i < ls.length; i++){
            
            if (ls[i].contains(clip)) al.add(i);
            
            
        }
        
        
        al.trimToSize();
        int [] a = new int [al.size()];
        
        for (int i = 0; i < a.length; i++){
            
            a[i] = (Integer)al.get(i);
            
            
        }
        
        
        
        return a;
        
        
    }
    
   
  public static int scanArrayList (java.util.ArrayList al, int val){
      
      if (al == null) return -1;
      al.trimToSize();
      int ex;
      
      for (int i = 0; i < al.size(); i++ ){
          ex = (Integer) al.get(i);
          if ( ex == val ) return (Integer) al.get(i);
                   
          
          
      }
      
      return -1;  //nothing found
      
      
  }
  
  public static boolean scanArrayList (java.util.ArrayList al, String s){
      
      if (al == null) return false;
      al.trimToSize();
      String ex;
      
      for (int i = 0; i < al.size(); i++ ){
          ex = (String) al.get(i);
          if ( ex.equals(s) ) return true;
                   
          
          
      }
      
      return false;  //nothing found
      
      
  }
    
  public static boolean isValidShortDate (String dateStr, boolean show) {
            
           
      if (dateStr.length() < 6 || dateStr.length() > 8) {
          
          if (show) javax.swing.JOptionPane.showMessageDialog(null, "Please provide a date in this format:  MM/DD/YY ");
          
          return false;
          
      }
      
            
      SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy");
		
      df.setLenient(false);
	
      ParsePosition pos = new ParsePosition(0);
        Date date;
       
            date = df.parse(dateStr, pos);
       
	
      String[] result = dateStr.split("/");
      if (result.length < 3 || result[2].length() < 2 || result[2].length() > 2) {
          
          if (show) javax.swing.JOptionPane.showMessageDialog(null, "Please provide a date in this format:  MM/DD/YY ");
          
          return false;
          
      }
      
      // Check all possible things that signal a parsing error
	
      if ((date == null) || (pos.getErrorIndex() != -1) ) {
                   
         if (show)  javax.swing.JOptionPane.showMessageDialog(null, "Please provide a date in this format:  MM/DD/YY ");
          return false;
          
      }

     return true;
     
  }  
  

  public static boolean isFileAccessible (String file, String fname){
      
      
      FileOutputStream fos = null;
        try {
            
            fos = new FileOutputStream(file);
            fos.close();
            return true;
            
        } catch (Exception ex) {
            
            javax.swing.JOptionPane.showMessageDialog(null, "The " + fname + " you tried to generate was locked by another program."+ 
                    System.getProperty("line.separator") + "Close any applications that may have it open and try again.");
            
            return false;
            
        }
      
      
  }
  
  
  public static void execute (String command){
        
        String osName = System.getProperty("os.name" );
            
            try {
                
                if(osName.contains("Windows")){
                Runtime.getRuntime().exec('"' + command + '"' );
                }
                
                else {
                    
                    Runtime.getRuntime().exec('"' + command + '"');
                   //System.out.println("cmd.exe start " + '"' + "c:\\Program Files\\Adobe\\Acrobat*\\Acrobat\\acrobat " + file.replace('/','\\') + '"');
                } 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
     
        
        
    }    
    
       
    public static String getShortDate () {

        GregorianCalendar gc = new GregorianCalendar ();
        Date today = gc.getTime();
        DateFormat fullDate = DateFormat.getDateInstance(DateFormat.SHORT);

        return fullDate.format(today);

    }

    public static long stringToDate (String date) {
        
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        try {
          Date d = df.parse(date, new ParsePosition(0));
          return d.getTime();
      }
      catch(Exception e) {
         System.out.println("Unable to parse " + date);
      }
       
        return 0;
        
    }
    
    
    public static String datetoString (long d){
        
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        

        return df.format(new Date(d));
        
        
    }
    public static String getFullDate () {

        GregorianCalendar gc = new GregorianCalendar ();
        Date today = gc.getTime();
        DateFormat fullDate = DateFormat.getDateInstance(DateFormat.FULL);

        return fullDate.format(today);

    }

    public static int howManyDays (String date1, String date2) {

        long elapse = Date.parse(date2) - Date.parse(date1);
        long days = elapse / (24 * 60 * 60 * 1000);
        return (int) days;

    }

    
    public static javax.swing.JFileChooser getDirChooser (String f, java.awt.Frame parentWin){
        
        File file;
        
         
        
        try {
            
            if (f.equals("")){
            file = new File(new File(".").getCanonicalPath());
            } else file = new File (f);
            
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(file);
            fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(parentWin);
            
            return fileChooser;
            
        } catch (IOException ex) { ex.printStackTrace();  return null;}
        
        
        
    }
    
    public static javax.swing.JFileChooser getFileChooser (String f) {
        
        File file;
        
         
        
        try {
            
            
            if (f.equals("")){
            file = new File(new File(".").getCanonicalPath());
            } else file = new File (f);
            
            
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(file);
            fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.showOpenDialog(null);
            
            return fileChooser;
            
        } catch (IOException ex) { ex.printStackTrace();  return null;}
        
        
        
        
        
        
    }
    
    
    public static String verifyPath (String path) {
    
    //put this in DV
    int path_size = path.length();
    
    if (path == "" || path == null || path_size == 0) return "";
    
    char c = path.charAt(path_size -1);
    
    if ( c != '/' && c != '\\' ) path = path + '/';
            
    
    return path.replace('\\', '/');
    
    
}
    
    
    public static int searchTable (TableModel tm, int col, int val){
        
          //returns the row the value is found in
        int len = tm.getRowCount();
        int a;
        
        for (int i = 0; i < len; i++) {
            
            a = (Integer) tm.getValueAt(i, col);
            //System.out.println(a);
            if (a == val) {
             
             return i;
             
         }
         
        }
        
        return -1;
        
        
        
    }
    
    
    public static int searchTable (TableModel tm, int col, String text) {
        
        //returns the row the value is found in
        int len = tm.getRowCount();
        String a;
        
        for (int i = 0; i < len; i++) {
            
            a = (String) tm.getValueAt(i,col);
            //System.out.println(a);
            if (text.equalsIgnoreCase(a.trim())) {
             
             return i;
             
         }
         
        }
        
        return -1;
        
    }
    
     public static java.util.ArrayList searchTableMulti (TableModel tm, int col, String text) {
        
        //returns the row the value is found in
        int len = tm.getRowCount();
        String a;
        java.util.ArrayList al = new java.util.ArrayList();
        
        
        for (int i = 0; i < len; i++) {
            
            a = (String) tm.getValueAt(i,col);
            //System.out.println(a);
            if (text.equalsIgnoreCase(a.trim())) {
             
                al.add(i);
             
            }
         
        }
        
        al.trimToSize();
        
        if (al.size() > 0) return al;
        else return null;
        
    }
    
    
    
    
    public static Object [] getTableRow (TableModel dtm, int r){
        
        Object [] row = new Object [dtm.getColumnCount()];
        
        
        int cols = row.length;
        
        for (int c = 0; c < cols; c++){
            
            row [c] = dtm.getValueAt(r, c);
            
        }
        
        return row;
        
    }
    
    
    
    public static int howManyNewLines (String target) {

        //count how many times a new-line ocurrs within target
        StringTokenizer st = new StringTokenizer (target, System.getProperty("line.separator"), true);
        //System.out.println(st.countTokens());
        //String values []  = target.split(System.getProperty("line.separator"));
       //if (value == 1) value = 0;
        //System.out.println("New line tokens " + value);
        int val = 0;
        //if (val > 1) val -= 1;
        
                
        while (st.hasMoreTokens()){
            
            if (st.nextToken().equals(System.getProperty("line.separator"))) val++;
            
        }
        
        
        //System.out.println("NEWLINES: " + val);
        
        return val ;



    }

    public static String textLine (int width, char c) {

        StringBuilder s = new StringBuilder ();

        for (int i = 0; i < width; i++) {

             s.append(c);

        }
        s.trimToSize();
        return s.toString();

    }



    public static String money (float money) {

        NumberFormat formatter = new DecimalFormat("###0.00");
        //NumberFormat formatter = NumberFormat.getCurrencyInstance();
        
        return formatter.format(money);
    }

    public static String money (double money) {

        NumberFormat formatter = new DecimalFormat("###0.00");
        //NumberFormat formatter = NumberFormat.getCurrencyInstance();
        
        return formatter.format(money);
    }

    
    public static float lowerPrecision (float money) {
        
        
        NumberFormat formatter = new DecimalFormat("###0.00");
        //NumberFormat formatter = NumberFormat.getCurrencyInstance();
        
        return Float.parseFloat( formatter.format(money) );
        
        
    }
    public static float lowerPrecision (double money) {
        
        
        NumberFormat formatter = new DecimalFormat("###0.00");
        //NumberFormat formatter = NumberFormat.getCurrencyInstance();
        
        return Float.parseFloat( formatter.format(money) );
        
        
    }
    
    
    
    public static String getStringMember (int index, String text){
        
        String[] result = text.split(System.getProperty("line.separator"));
        
        if (index < result.length && index > -1  && result.length > 0) return result[index];
        else return "";
        
        
        
    }
    
    public static Object [] getRow (TableModel tm, int row){
        
        Object [] data = new Object [tm.getColumnCount()];
        
        for (int i = 0; i < tm.getColumnCount(); i++){
            
            
            if (DV.whatIsIt((Object)tm.getValueAt(row,i)) == 1){
                
                data [i] = new String ((String) tm.getValueAt(row, i));
                //System.out.println("String");
                
            } 
            
            if (DV.whatIsIt((Object)tm.getValueAt(row,i)) == 2){
                
                data [i] = new Integer ((Integer) tm.getValueAt(row, i));
                //System.out.println("Interg");
            } 
            
            if (DV.whatIsIt((Object)tm.getValueAt(row,i)) == 3){
                
                data [i] = new Float ((Float) tm.getValueAt(row, i));
                //System.out.println("Float");
            } 
            
            if (DV.whatIsIt((Object)tm.getValueAt(row,i)) == 4){
                
                data [i] = new Boolean ((Boolean) tm.getValueAt(row, i));
                //System.out.println("boolean");
            } 
            
            
        }
        
        return data;
        
        
    }
    
    
    
    public static int whatIsIt (Object obj) {

        if (obj instanceof String) return 1;
        if (obj instanceof Integer) return 2;
        if (obj instanceof Float) return 3;
        if (obj instanceof Boolean) return 4;
        if (obj instanceof Long) return 5;
        
        else return 0;
    }

    public static Class idObject (Object obj) {
        
        int q = DV.whatIsIt(obj);

            switch (q) {
                        
                        case 1: return String.class; 
                        case 2: return Integer.class; 
                        case 3: return Float.class;
                        case 4: return Boolean.class;
                        case 5: return Long.class; 
            }
            
            
        return null;
        
    }
    
    
    public static boolean boolValue (Object obj) {

        return Boolean.valueOf( (Boolean) obj );
    }
    public static float floatValue (Object obj) {

        return Float.valueOf( (Float) obj );

    }
    public static int intValue (Object obj)  {

        return Integer.valueOf( (Integer) obj );

    }


    public static boolean validIntString (String i) {
        
        int counter=0;
       if (i.equals("")) return false;
        CharacterIterator it = new StringCharacterIterator(i);

        // Iterate over the characters in the forward direction
        for (char ch=it.first(); ch != CharacterIterator.DONE; ch=it.next()) {

            if (counter ==0){
            
                
                if (ch != '-' && !Character.isDigit(ch) ) return false;
                
            }else {
                
                if (!Character.isDigit(ch)) return false;
                
            }
            
            
            counter++;
            
        }

      return true;

    }

    public static boolean validFloatString (String f) {

        if (f.equals("")) return false;
        CharacterIterator it = new StringCharacterIterator(f);
        int dot = 0;

        // Iterate over the characters in the forward direction
        for (char ch=it.first(); ch != CharacterIterator.DONE; ch=it.next()) {

            if (it.getIndex() == it.getBeginIndex() && ch == '-') { ch=it.next(); }

            if (ch == '.') { ch=it.next(); dot++; }
            if (dot > 1) return false;
            if (!Character.isDigit(ch)) return false;
        }

      return true;
    }

    public static void expose (Object [] rec) {

        int q;
        for (int i = 0; i < rec.length; i++){

            q = DV.whatIsIt(rec[i]);

            switch (q) {
                case 1: System.out.println(i+" Str: " +(String) rec[i] ); break;
                case 2: System.out.println(i+" Int: " + Integer.toString( (Integer) rec[i]) ); break;
                    case 3: System.out.println(i+" Flt: " + Float.toString( (Float) rec[i]) ); break;
                        case 4: System.out.println(i+" bool: " + Boolean.toString( (Boolean) rec[i]) ); break;
                case 0:  System.out.println(i+" ERR: " +"Unknown Object Type in DV.expose()");
            }

        }
    System.out.println("------------------------------------");
    }

    public static String convertToString (Object obj) {

     int q = DV.whatIsIt(obj);

            switch (q) {

                case 1: return new String ( (String) obj );

                case 2: return Integer.toString( (Integer) obj );

                case 3: return DV.money ( (Float) obj ) ;

                case 4: return Boolean.toString( (Boolean) obj );

                case 0:  return "Unkown object!";
            }



        return "";
    }


   
    public static boolean isInteger (Object obj) {

	if (obj instanceof Integer) return true;
	return false;
    }
    
    public static boolean isLong(Object obj) {
        
        if (obj instanceof Long) return true;
	return false;        
        
    }

    public static boolean isString (Object obj) {

	if (obj instanceof String) return true;
	return false;
    }


    public static boolean isFloat (Object obj) {

	if (obj instanceof Float) return true;
	return false;
    }


    public static boolean isBoolean (Object obj) {

	if (obj instanceof Boolean) return true;
	return false;
    }


    public static void saveObject (Object obj, String filename) {
        try {
             ObjectOutput out = new ObjectOutputStream(new FileOutputStream(filename));
             out.writeObject(obj);
             out.close();
             } catch (Exception e) { e.printStackTrace(); }
    }

    public static Object DeSerial (String filename) {

        boolean exists = (new File(filename)).exists();

        if (exists) {

            try {

                File file = new File(filename);
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                // Deserialize the object
                Object temp = in.readObject();
                in.close(); return temp;

            } catch (Exception e) { e.printStackTrace();  }

        }   else {  return null;  /*ERROR*/  }

        return null;
    }

    public static boolean writeFile (String filename, String text, boolean append){
        
        
        try {
                
            File data = new File (filename);
            
            PrintWriter out = new PrintWriter(
                    new BufferedWriter( 
                     new FileWriter (data, append ) ) );
                //write text
            
            
                out.write( text );
                 
            
            out.flush();
            out.close();
            return true;
            
        } catch (Exception e) {
            
            javax.swing.JOptionPane.showMessageDialog(null, "File or Directory doesn't exist: " + filename + System.getProperty("line.separator")+
                    "The file may be locked by another application.");
            return false;}
        
              
    }
    
    
    public static String readFile (String filename){
        
        StringBuilder sb = new StringBuilder();
        BufferedInputStream bis =null;
        
        try {
                
            File data = new File (filename);
            
            if (!data.exists()) return "";
            
            FileInputStream in = new FileInputStream(data);
              bis = new BufferedInputStream( in, 4096 /* buffsize */ );  
             BufferedReader b = new BufferedReader(new InputStreamReader(bis));
              
            String tmp = b.readLine();
            
            do {
                
                sb.append( tmp + System.getProperty("line.separator"));
                tmp = b.readLine();
                
                
                 
            }while (tmp != null);
                        
            bis.close();
            
            return sb.toString();
            
        } catch (Exception e) {e.printStackTrace(); return "READ ERROR";}
        
              
    }
    
    
    
    
    public static String getProp (String file, String key) {

        try {

            Properties props = new Properties ();
            FileInputStream is = new FileInputStream (file);
            props.load(is);

            String rval = props.getProperty(key);

            return rval;


        }catch (Exception e) {e.printStackTrace();}


     return "";
    }


    public static void setProp (String file, String key, String val, boolean append)  {

        try {


            Properties props = new Properties ();
            FileOutputStream os = new FileOutputStream (file, append);  //append
            props.put(key, val);
            props.store(os, "");

            os.flush();
            os.close();


        }catch (Exception e) {e.printStackTrace();}



    }


    public static java.awt.Dimension computeCenter (java.awt.Window win) {

        java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int w = win.getSize().width;
        int h = win.getSize().height;
        int x = (dim.width-w) / 2;
        int y = (dim.height-h) / 2;

        return new java.awt.Dimension (x, y);


    }
   
       
    public static String addSpace (String s, int total_length, char c){

       StringBuilder sb = new StringBuilder ();
            sb.append(s);

       if (total_length < 8) total_length = 8;

            int length = (total_length ) - s.length();

       for (int i = 0; i < length; i++) {

           sb.append(c);

       }
       
            
       sb.trimToSize();
       return sb.toString();
   }


}/* END CLASS */
