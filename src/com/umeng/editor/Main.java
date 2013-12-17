package com.umeng.editor;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.umeng.editor.decode.AXMLDoc;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			AXMLDoc doc = new AXMLDoc();
			doc.parse(new FileInputStream("test/AndroidManifest.xml"));
			doc.print();
			
			doc.build(new FileOutputStream("test/abc.xml"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
