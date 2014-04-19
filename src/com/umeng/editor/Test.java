package com.umeng.editor;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.umeng.editor.decode.AXMLDoc;

public class Test {
	public static void main(String[] args) {
		try{
			AXMLDoc doc = new AXMLDoc();
			doc.parse(new FileInputStream("test/AndroidManifest.xml"));
			doc.print();
			
			ChannelEditor editor = new ChannelEditor(doc);
			editor.setChannel("abc");
			editor.commit();
			
			doc.build(new FileOutputStream("test/abc.xml"));
			
			AXMLDoc doc2 = new AXMLDoc();
			doc2.parse(new FileInputStream("test/abc.xml"));
			doc2.print();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
