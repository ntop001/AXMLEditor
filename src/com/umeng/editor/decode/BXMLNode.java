package com.umeng.editor.decode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BXMLNode implements IVisitable{
	public Pair<Integer,Integer> mChunkSize = new Pair<Integer,Integer>();
	public Pair<Integer,Integer> mLineNumber= new Pair<Integer,Integer>();
	
	private List<BXMLNode> mChild;
	
	public void checkTag(int expect, int value) throws IOException{
		if(value != expect){
			throw new IOException("Can't read current node");
		}
	}
	
	public void readStart(IntReader reader) throws IOException{
		mChunkSize.first = reader.readInt();
		mLineNumber.first = reader.readInt();
	}

	public void readEnd(IntReader reader) throws IOException{
		mChunkSize.second = reader.readInt();
		mLineNumber.second = reader.readInt();
	}
	
	public void writeStart(IntWriter writer){
		
	}
	
	public void writeEnd(IntWriter writer){
		
	}
	
	public boolean hasChild(){
		return (mChild != null && !mChild.isEmpty());
	}
	
	public List<BXMLNode> getChildren(){
		return mChild;
	}
	
	public void addChild(BXMLNode node){
		if(mChild == null) mChild = new ArrayList<BXMLNode>();
		if(node != null){
			mChild.add(node);
		}
	}
}
