package com.umeng.editor.decode;

import java.io.IOException;

import com.umeng.editor.utils.TypedValue;

public class BTagNode extends BXMLNode {
	private final int TAG_START = 0x00100102;
	private final int TAG_END   = 0x00100103;
	
	private int mRawNSUri;
	private int mRawName;
	
	private short mRawAttrCount;	//(id attr)<<16 + (normal attr ?)
	private short mRawClassAttr;	//
	private short mRawIdAttr;
	private short mRawStyleAttr;
	
	private int[] mRawAttrs;
	
	public BTagNode(){}
	public BTagNode(int ns, int name){
		mRawName = name;
		mRawNSUri = ns;
	}
	
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
	
	/**
	 * Eg:android:id="@+id/xxx". Equivalent to getAttributeValue(null, "id").
	 * @return Attribute(name="id").mString
	 */
	public int getIdAttr(){
		return getAttrStringForKey(mRawIdAttr);
	}
	/**
	 * Eg:android:class="com.foo.example". Equivalent to getAttributeValue(null, "class").
	 * @return Attribute(name="class").mString
	 */
	public int getClassAttr(){
		return getAttrStringForKey(mRawClassAttr);
	}
	/**
	 * Eg:style=""@style/Button". Equivalent to getAttributeValue(null, "style").
	 * @return Attribute(name="style").mString
	 */
	public int getStyleAttr(){
		return getAttrStringForKey(mRawStyleAttr);
	}
	
	public Attribute[] getAttribute(){
		Attribute[] attrs = new Attribute[mRawAttrCount];
		
		for(int i =0; i< mRawAttrCount; i++){
			attrs[i] = new Attribute(subArray(mRawAttrs,i*Attribute.SIZE,Attribute.SIZE));
		}
		
		return attrs;
	}
	
	public void setAttribute(Attribute attr){
		//TODO I can't figure out the difference about id attribute, class attribute , style attribute , so It's hard to edit attribute now!
	}
	
	/**
	 * 
	 * @param key
	 * @return String mapping id
	 */
	public int getAttrStringForKey(int key){
		Attribute[] attrs = getAttribute();
		
		for(Attribute attr : attrs){
			if(attr.mName == key){
				return attr.mString;
			}
		}
		
		return -1;
	}
	
	public boolean setAttrStringForKey(int key, int string_value){
		Attribute[] attrs = getAttribute();
		
		for(Attribute attr : attrs){
			if(attr.mName == key){
				attr.mString = string_value;
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * return scalar type for key
	 * @param key
	 * @return int[]{Type, Value}
	 */
	public int[] getAttrValueForKey(int key){
		Attribute[] attrs = getAttribute();
		
		for(Attribute attr : attrs){
			if(attr.mName == key){
				int type_value [] = new int[2];
				type_value[0] = attr.mType;
				type_value[1] = attr.mValue;
				return type_value;
			}
		}
		
		return null;
	}
	
	/**
	 * Don't support now
	 * @param key
	 * @param type
	 * @param value
	 * @return
	 */
	public boolean setAttrValueForKey(int key, int type, int value){
		return false;
	}
	
	public int getName(){
		return mRawName;
	}
	
	public void setName(int name){
		mRawName = name;
	}
	
	public int getNamesapce(){
		return mRawNSUri;
	}
	
	public void setNamespace(int ns){
		mRawNSUri = ns;
	}
	
	public static class Attribute {
		public static final int SIZE = 5;
		
		public int mNameSpace;
		public int mName;
		public int mString;
		public int mType;
		public int mValue;
		
		public Attribute(int ns, int name, int type){
			mNameSpace = ns;
			mName = name;
			mType = type;
		}
		
		public void setString(int str){
			if(mType != TypedValue.TYPE_STRING){
				throw new RuntimeException("Can't set string for none string type");
			}
			
			mString = str;
		}
		
		public void setValue(int type, int value){
			if(type == TypedValue.TYPE_STRING){
				mValue = value;
				mString = value;
			}else{
				mValue = value;
				mString = -1;
			}
		}
		
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
