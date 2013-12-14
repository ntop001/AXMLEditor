package com.umeng.editor.decode;

import java.io.IOException;

public class BTagNode extends BXMLNode {
	private final int TAG_START = 0x00100102;
	private final int TAG_END   = 0x00100103;
	
	private int mRawNSUri;
	private int mRawName;
	
	private short mRawAttrCount;	//(id attr)<<16 + (normal attr ?)
	private short mRawClassAttr;
	private short mRawIdAttr;
	private short mRawStyleAttr;
	
	private int[] mRawAttrs;
	
	public void checkStartTag(int tag) throws IOException{
		checkTag(TAG_START, tag);
	}
	
	public void checkEndTag(int tag) throws IOException{
		checkTag(TAG_END, tag);
	}
	
	@SuppressWarnings("unused")
	public void readStart(IntReader reader) throws IOException{
		super.readStart(reader);
		
		int xffff_ffff = reader.readInt(); 	//unused int value(0xFFFF_FFFF)
		mRawNSUri = reader.readInt(); 		//TODO maybe not ns uri (0xFFFF) 
		mRawName = reader.readInt();   		//name for element 
		int x0014_0014 = reader.readInt();  //TODO unknown field
		
		mRawAttrCount = (short)reader.readShort();	//attribute count
		
		mRawIdAttr = (short)reader.readShort();		//id attribute
		mRawClassAttr = (short)reader.readShort();	//class 
		mRawStyleAttr = (short)reader.readShort();
		
		mRawAttrs = reader.readIntArray(mRawAttrCount*Attribute.SIZE); //namespace, name, value(string),value(type),value(data)
		
		//Attribute attr = getIdAttr();
	}
	
	@SuppressWarnings("unused")
	public void readEnd(IntReader reader) throws IOException{
		super.readEnd(reader);
		
		int xffff_ffff = reader.readInt(); //unused int value(0xFFFF_FFFF)
		int ns_uri = reader.readInt();
		int name = reader.readInt();
		
		if((ns_uri != mRawNSUri) || (name != mRawName) ){
			throw new IOException("Invalid end element");
		}
	}
	
	public void writeStart(IntWriter writer){
		
	}
	
	public void writeEnd(IntWriter writer){
		
	}
	
	public Attribute getIdAttr(){
		int[] idAttr = subArray(mRawAttrs, mRawIdAttr, Attribute.SIZE);
		return new Attribute(idAttr);
	}
	
	public Attribute getClassAttr(){
		int[] styleAttr = subArray(mRawAttrs, mRawClassAttr, Attribute.SIZE);
		return new Attribute(styleAttr);
	}
	
	public Attribute getStyleAttr(){
		int[] styleAttr = subArray(mRawAttrs, mRawStyleAttr, Attribute.SIZE);
		return new Attribute(styleAttr);
	}
	
	public Attribute[] getAttribute(){
		Attribute[] attrs = new Attribute[mRawAttrCount];
		
		for(int i =0; i< mRawAttrCount; i++){
			attrs[i] = new Attribute(subArray(mRawAttrs,i*Attribute.SIZE,Attribute.SIZE));
		}
		
		return attrs;
	}
	
	public int getName(){
		return mRawName;
	}
	
	public int getNamesapce(){
		return mRawNSUri;
	}
	
	public static class Attribute {
		public static final int SIZE = 5;
		
		public int mNameSpace;
		public int mName;
		public int mString;
		public int mType;
		public int mValue;
		
		public Attribute(int[] raw){
			mNameSpace = raw[0];
			mName = raw[1];
			mString = raw[2];
			mType = raw[3];
			mValue = raw[4];
		}
		
		public boolean hasNamespace(){
			return (mNameSpace != -1);
		}
	}
	
	private int[] subArray(int[] src, int start, int len){
		if((start + len) > src.length ){
			throw new RuntimeException("OutOfArrayBound");
		}
		
		int[] des = new int[len];
		System.arraycopy(src, start, des, 0, len);
		
		return des;
	}

	@Override
	public void accept(IVisitor v) {
		v.visit(this);
	}
}
