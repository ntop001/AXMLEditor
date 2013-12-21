package com.umeng.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Aapt {
	private final String PATH_AAPT = "";
	
	public static String runCommand(String[] s) throws IOException{
		Process process = Runtime.getRuntime().exec(s);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()), 1024);
		
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = bufferedReader.readLine()) != null){
			sb.append(line);
		}
		
		bufferedReader.close();
		return sb.toString();
	}
	
	//aapt r[emove] [-v] file.{zip,jar,apk} file1 [file2 ...]
	public static String deleteFile(File file, String entry) throws IOException{
		List<String> list = new ArrayList<String>();
		
		list.add("aapt");
		list.add("r");
		list.add(file.getAbsolutePath());
		list.add(entry);
		
		return runCommand(list.toArray(new String[list.size()]));
	}
	
	//aapt a[dd] [-v] file.{zip,jar,apk} file1 [file2 ...]
	public static String addFile(File file, File added) throws IOException{
		List<String> list = new ArrayList<String>();
		
		list.add("aapt");
		list.add("r");
		list.add(file.getAbsolutePath());
		list.add(added.getAbsolutePath());
		
		return runCommand(list.toArray(new String[list.size()]));
	}
	
	
}
