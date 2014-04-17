package com.umeng.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.umeng.editor.decode.AXMLDoc;

public class Main {
	/**
	 * In:AndroidManifest.xml channel1,channel2,channel3...
	 * Out:channel1.xml,channel2.xml,channel3.xml...
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			if(args == null || args.length <2){
				System.err.println("Usage:");
				System.err.println("AndroidManifest.xml xxx [xxx] [xxx]");
				System.exit(0);
			}
			
			if(args.length == 2){
				cloneAXML(new File(args[0]), args[1]);
			}else{
				
				String[] chans = new String[args.length -1];
				for(int i=0; i< args.length; i++){
					if(i > 0){
						chans[i-1] = args[i];
					}
				}	
				cloneAXML(new File(args[0]), chans);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void cloneAXML(File axml, String channel) throws Exception{
		AXMLDoc doc = new AXMLDoc();
		doc.parse(new FileInputStream(axml));
		
		ChannelEditor editor = new ChannelEditor(doc);

		editor.setChannel(channel);
		editor.commit();
		
		doc.build(new FileOutputStream(String.format("axml_%s.xml", channel)));	
	}
	
	private static void cloneAXML(File axml, String[] channels) throws Exception{
		AXMLDoc doc = new AXMLDoc();
		doc.parse(new FileInputStream(axml));
		
		ChannelEditor editor = new ChannelEditor(doc);
		
		for(String chan : channels){
			editor.setChannel(chan);
			editor.commit();
			doc.build(new FileOutputStream(String.format("axml_%s.xml", chan)));
		}	
	}
}
