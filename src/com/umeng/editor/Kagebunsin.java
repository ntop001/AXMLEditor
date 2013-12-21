package com.umeng.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.umeng.editor.decode.AXMLDoc;
import com.umeng.editor.utils.Pair;

/**
 * ==working space
 * User1
 * --App1
 * ----AndroidManifest.xml
 * ----original.apk
 * ----apks
 * ------channel1.apk
 * ------channel2.apk
 * ------channel3.apk
 * ----axml
 * ------channel1.xml
 * ------channel2.xml
 * @author ntoooooop
 */
public class Kagebunsin {
	private final String apks = "apks";
	private final String xmls = "xmls";
	
	private File mDir;//working directory
	private Map<String,Pair<File,File>> mClones = new LinkedHashMap<String,Pair<File,File>>();
	
	private File mApk;
	private File mAxml;
	
	public Kagebunsin(File workspace){
		mDir = workspace;
		
		File apks_dir = new File(workspace, apks);
		if(apks_dir.exists()){
			apks_dir.delete();
		}
		apks_dir.mkdir();
		
		File xml_dir = new File(workspace, apks);
		if(xml_dir.exists()){
			xml_dir.delete();
		}
		xml_dir.mkdir();
	}
	
	public void setWorkingDir(File dir){
		mDir = dir;
		
		if(mDir.exists()){
			mDir.delete();
		}
	}
	
	public void setApk(File apk){
		mApk = apk;
	}
	
	public void setChannels(List<String> channels){
		Set<String> set = mClones.keySet();
	
		for(String channel : channels){
			if(channel == null || channel.isEmpty()){
				continue;
			}
			
			Pair<File,File> p = new Pair<File,File>();
			p.first = new File(mDir,apks + File.separator + channel);
			p.second = new File(mDir, xmls + File.separator + channel);
			
			set.add(channel.trim());
		}
	}
	
	private void cloneAXML() throws Exception{
		MetaEditor editor = new MetaEditor();
		AXMLDoc doc = new AXMLDoc();
		doc.parse(new FileInputStream(mAxml));
		doc.addEditor(editor);
		
		for(Map.Entry<String, Pair<File,File>> entry: mClones.entrySet()){
			editor.setChannel(entry.getKey());
			doc.build(new FileOutputStream(entry.getValue().first));
		}			
	}
	
	private void cloneApks() throws Exception {
		ApkTool tool = new ApkTool();
		tool.setOrigin(mApk);
		
		for(Pair<File,File> p : mClones.values()){
			tool.setAXMLFile(p.first);
			tool.setOutFile(p.second);
			tool.build();
		}
	}
	
	public void start() throws Exception{
		//1. extract AndroidManifest.xml
		ApkTool.readAXML(mApk, new FileOutputStream(mAxml));
		//2. create AndroidManifest.xml for different channel
		cloneAXML();
		//3. cook apks
		cloneApks();
	}
}
