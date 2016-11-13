/*
 * RemoteBusyException.java
 *
 * Created on March 22, 2007, 3:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.db.remote;

/**
 *
 * @author Data Virtue
 */
public class RemoteBusyException extends java.lang.Exception implements java.io.Serializable {
    
    /**
     * Creates a new instance of <code>RemoteBusyException</code> without detail message.
     */
    public RemoteBusyException() {
    }
    
    
    /**
     * Constructs an instance of <code>RemoteBusyException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RemoteBusyException(String msg) {
        super(msg);
    }
       
    
}
