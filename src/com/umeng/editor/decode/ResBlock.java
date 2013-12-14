package com.umeng.editor.decode;

import java.io.IOException;

public class ResBlock implements IAXMLSerialize{
	private int mChunkSize;
	private byte[] mRawResIds;
	
	public void print(){
		StringBuilder sb = new StringBuilder();
		
		for(int id : getResourceIds()){
			sb.append(id);
			sb.append(" ");
		}
		
		System.out.println(sb.toString());
	}
	
	public void read(IntReader reader) throws IOException{
		mChunkSize = reader.readInt();
		
		if(mChunkSize < 8 || (mChunkSize % 4)!= 0){
			throw new IOException("Invalid resource ids size ("+mChunkSize+").");
		}
		
		mRawResIds = reader.readByteArray(mChunkSize - 8);//subtract base offset (type + size)
	}
	
	public int[] getResourceIds(){
		int len = mRawResIds.length / 4;
		int[] ids = new int[len];
		
		for(int i =0; i< len; i++){
			ids[i] = byteArrayToInt(mRawResIds, i*4);
		}
		
		return ids;
	}
	
	public int getResourceIdAt(int index){
		int[] ids = getResourceIds();
		return ids[index];
	}
	
	private int byteArrayToInt(byte[] b, int start) 
	{
		if((start + 4) > b.length ){
			throw new RuntimeException("Out of array size");
		}
		
	    int value = (b[start] & 0xff) | ((b[start+1] << 8) & 0xff00) // | 表示安位或 
	    		| ((b[start+2] << 24) >>> 8) | (b[start+3] << 24); 
	    return value;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return mChunkSize;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSize(int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setType(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(IntWriter writer) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
