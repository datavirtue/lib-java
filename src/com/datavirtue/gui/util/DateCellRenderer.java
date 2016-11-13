/*
 * DateCellRenderer.java
 *
 * Created on October 4, 2007, 3:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.datavirtue.gui.util;

import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

// A holder for data and an associated icon
public class DateCellRenderer extends DefaultTableCellRenderer {
	public DateCellRenderer(String format, int align) {
		//this.ms = ms;		
		this.format = format;
		this.align = align;// alignment (LEFT, CENTER, RIGHT)
	}

	protected void setValue(Object value) {
		if (value != null && value instanceof Long) {
			setText(formatter.format(new java.util.Date((Long)value)));							
		} else {
			super.setValue(value);
		}
		setHorizontalAlignment(align);
	}

	protected long ms;
        protected String format;
	protected int align;
	protected static DateFormat formatter = DateFormat.getInstance();
}