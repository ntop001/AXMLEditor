package com.umeng.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

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
				System.err.println("x(ml) AndroidManifest.xml xxx");
				System.err.println("a(pk) [file].apk xxx,xxx,xxx...");
				System.exit(0);
			}
			
			String origin = args[0];
			
			if(origin.equals("x") || origin.equals("xml")){
				cloneAXML(new File(args[1]), args[2]);
			}
			
			if(origin.equals("a") || origin.equals("apk")){
				cloneApk(new File(args[1]), Arrays.asList(args[2].split(",")));
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void cloneAXML(File axml, String channel) throws Exception{
		MetaEditor editor = new MetaEditor();
		AXMLDoc doc = new AXMLDoc();
		doc.parse(new FileInputStream(axml));
		doc.addEditor(editor);
		
		editor.setChannel(channel);
		doc.build(new FileOutputStream(String.format("test/%s.xml", channel)));	
	}
	
	private static void cloneApk(File apk, List<String> channels) throws Exception{
		Kagebunsin k = new Kagebunsin(null);
		k.setApk(apk);
		k.setChannels(channels);
		k.start();
	}
}
