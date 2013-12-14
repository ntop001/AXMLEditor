package com.umeng.editor.decode;

import java.io.IOException;

public class BNSNode extends BXMLNode {
	private final int TAG_START = 0x00100100;
	private final int TAG_END   = 0x00100101;
	
	private int mPrefix;
	private int mUri;
	
	public void checkStartTag(int tag) throws IOException{
		checkTag(TAG_START, tag);
	}
	
	public void checkEndTag(int tag) throws IOException{
		checkTag(TAG_END, tag);
	}
	
	@SuppressWarnings("unused")
	public void readStart(IntReader reader) throws IOException{
		super.readStart(reader);
		
		int ffffx0 = reader.readInt(); //unused int value(0xFFFF)
		mPrefix = reader.readInt();
		mUri = reader.readInt();
	}
	
	@SuppressWarnings("unused")
	public void readEnd(IntReader reader) throws IOException{
		super.readEnd(reader);
		
		int ffffx0 =  reader.readInt();//skip unused value
		int prefix = reader.readInt();
		int uri = reader.readInt();
		
		if((prefix != mPrefix) || (uri != mUri) ){
			throw new IOException("Invalid end element");
		}
	}
	
	public void writeStart(IntWriter writer){
		
	}
	
	public void writeEnd(IntWriter writer){
		
	}
	
	public int getPrefix(){
		return mPrefix;
	}
	
	public int getUri(){
		return mUri;
	}

	@Override
	public void accept(IVisitor v) {
		v.visit(this);
	}
}
