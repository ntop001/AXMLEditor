package com.umeng.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.umeng.os.Aapt;

/**
 * Clone a new apk file from an old apk, the new file'll have a different channel id. 
 * @author ntooop
 *
 */
public class ApkTool {
	private static final String AXML_NAME = "AndroidManifest.xml";

	private File mOrigin;
	private File mOutput;
	private File mAXML;
	
	public static void readAXML(File apk, OutputStream os) throws IOException{
		JarFile jf = new JarFile(apk.getAbsoluteFile());
		JarEntry je = jf.getJarEntry(AXML_NAME);
		//TODO int compression = JarEntry.STORED;
		//if (je != null) {
		//	compression = je.getMethod();
		//}
		InputStream is = jf.getInputStream(je);
		
		IOUtils.copy(is, os);
		IOUtils.closeQuietly(is);
		IOUtils.closeQuietly(os);
		
		jf.close();
	}
	
	public void setOrigin(File file) throws IOException{
		if(file == null || !file.exists() || !file.isFile()){
			throw new IOException("Invalid file: " + (file == null ? "null": file.getAbsoluteFile()));
		}
		
		mOrigin = file;
	}
	
	public void setOutFile(File file) throws IOException{
		if(file == null){
			throw new IOException("Invalid output file: null");
		}
		
		if(file.exists()){
			file.delete();
		}
		
		mOutput = file;
	}
	
	public void setAXMLFile(File file) throws IOException{
		if(file == null || !file.exists() || !file.isFile()){
			throw new IOException("Invalid file: " + (file == null ? "null": file.getAbsoluteFile()));
		}
		
		mAXML = file;
	}
	
	private void copy() throws IOException{
		FileUtils.copyFile(mOrigin, mOutput);
	}
	
	private void deleteAXML() throws IOException{
		Aapt.deleteFile(mOutput, AXML_NAME);
	}
	
	private void addAXML() throws IOException{
		Aapt.addFile(mOutput, mAXML);
	}
	
	public void build() throws IOException{
		if(mOrigin == null){
			throw new IOException("No original apk file");
		}
		
		if(mOutput == null){
			throw new IOException("No output file");
		}
		
		if(mAXML == null){
			throw new IOException("No AndroidManifest.xml file");
		}
		
		copy();			//copy a new file
		deleteAXML(); 	//delete old androidmanifest.xml
		addAXML();		//add new AndroidManifest.xml
	}
}
