/**
 *  Copyright 2011 Ryszard Wiśniewski <brut.alll@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.umeng.editor.decode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Dmitry Skiba
 * 
 * Block of strings, used in binary xml and arsc.
 * 
 * TODO:
 * - implement get()
 *
 */
public class StringBlock implements IAXMLSerialize{
		private static final int TAG = 0x001C0001;
		private static final int INT_SIZE = 4;
		
		private int mChunkSize;
		private int mStringsCount;
		private int mStylesCount;
		private int mEncoder;
		
		private int mStrBlockOffset;
		private int mStyBlockOffset;
		
		private int[] mPerStrOffset;
		private int[] mPerStyOffset;
		
		/**
		 * raw String
		 */
		private List<String> mStrings;
		/**
		 * android can identify HTML tags in a string，all the styles are kept here 
		 */
		private List<Style> mStyles;
		
		public int getStringMapping(String str){
			int size = mStrings.size();
			for(int i=0; i< size ; i++){
				if(mStrings.get(i).equals(str)){
					return i;
				}
			}
			
			return -1;
		}
		
		public int putString(String str){
			if(containsString(str)){
				return getStringMapping(str);
			}
			
			mStrings.add(str);
			
			return ( mStrings.size() - 1);
		}
		
		public boolean containsString(String str){
			return mStrings.contains(str.trim());
		}
		
        /**
         * Reads whole (including chunk type) string block from stream.
         * Stream must be at the chunk type.
         */
        public void read(IntReader reader) throws IOException {
			mChunkSize = reader.readInt();
			mStringsCount = reader.readInt();
			mStylesCount = reader.readInt();
			
			mEncoder = reader.readInt();//utf-8 or uft16
			
			mStrBlockOffset =reader.readInt();
			mStyBlockOffset =reader.readInt();
			
			if(mStringsCount > 0){
				mPerStrOffset = reader.readIntArray(mStringsCount);
				mStrings = new ArrayList<String>(mStringsCount);
			}
			
			if(mStylesCount > 0){
				mPerStyOffset = reader.readIntArray(mStylesCount);
				mStyles = new ArrayList<Style>();
			}
			
			//read string
			if(mStringsCount >0){
				int size = ((mStyBlockOffset == 0)?mChunkSize:mStyBlockOffset) - mStrBlockOffset;
				byte[] rawStrings = reader.readByteArray(size);
				
				for(int i =0; i < mStringsCount ; i++){
					int offset = mPerStrOffset[i];
		        	short len = toShort(rawStrings[offset], rawStrings[offset+1]);
					mStrings.add(i,new String(rawStrings,offset+2, len*2, Charset.forName("UTF-16LE")));
				}
			}
			
			//read styles
			if(mStylesCount > 0){
				int size = mChunkSize - mStyBlockOffset;
				int[] styles = reader.readIntArray(size/4);
				

				for(int i = 0; i< mStylesCount; i++){
					int offset = mPerStyOffset[i];
					int j = offset;
					for(; j< styles.length; j++){
						if(styles[j] == -1) break;
					}
					
					int[] array = new int[j-offset];
					System.arraycopy(styles, offset, array, 0, array.length);
					Style d = Style.parse(array);
					
					mStyles.add(d);
				}
			}
        }
        
        @Override
		public void write(IntWriter writer) throws IOException {
			writer.writeInt(TAG);
			writer.writeInt(getSize());
			writer.writeInt(mStrings == null ?0: mStrings.size());
			//TODO style count ,writer.writeInt(i)
			writer.writeInt(mEncoder);
			
			int strBlockOffset = 
					+ writer.getPosition() 	//current position
					+ INT_SIZE 				//string base offset
					+ INT_SIZE				//style base offset
					+ (mStrings == null ?0: mStrings.size()*INT_SIZE) //int array for string relative offset
					+ 0 //TODO about style(mSty)
					;
			writer.writeInt(strBlockOffset);
			
			int strSize = 0;
			int []perStrSize = null;
			
			if(mStrings != null){
				int size = 0;
				perStrSize = new int[mStrings.size()];
				for(int i =0; i< mStrings.size(); i++){
					perStrSize[i] = size;
					size = 2 + mStrings.get(i).getBytes("UTF-16LE").length;
					strSize += size;
				}
			}
			
			int stySize = 0;
			int[] perStySize = null;
			
			//TODO style again
			
			int styleBlockOffset = strBlockOffset + stySize;
			writer.writeInt(styleBlockOffset);
			
			if(perStrSize != null){
				for(int i : perStrSize){
					writer.writeInt(i);
				}
			}
			
			if(perStySize != null){
				for(int i : perStySize){
					writer.writeInt(i);
				}
			}
			
			if(mStrings != null){
				for(String s : mStrings){
					byte[] raw = s.getBytes("UTF-16LE");
					writer.writeShort((short)raw.length);
					writer.writeByteArray(raw);
				}
			}
			
			//TODO write style...
			
		}
        
        public int getSize(){
        	int size = 0;
        	size += INT_SIZE;	//for type
        	size += INT_SIZE;  	//for size
        	size += INT_SIZE;  	//string count
        	size += INT_SIZE;  	//style count
        	size += INT_SIZE;	//encode
        	size += INT_SIZE;	//string block offset
        	size += INT_SIZE;	//style block offset
        	
        	if(mStrings.size() > 0){
        		size += mStrings.size()*INT_SIZE;
        		for(String s : mStrings){
        			try {
        				size += 2;
						size += s.getBytes("UTF-16LE").length;
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
        		}
        	}
        	
        	//TODO if(mStyles.)
        	return size;
        }
        
        public String getStringFor(int index){
        	return mStrings.get(index);
        }
        
        private short toShort(short byte1, short byte2)
        {
            return (short)((byte2 << 8) + byte1);
        }
        
        public Style getStyle(int index){
        	return mStyles.get(index);
        }

        ///////////////////////////////////////////// implementation

        public StringBlock() {
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
		
		public static class Style {
			List<Decorator> mDct;
			
			public Style(){
			}
			
			public void addStyle(Decorator style){
				mDct.add(style);
			}
			
			public static Style parse(int[] muti_triplet) throws IOException{
				if(muti_triplet == null || (muti_triplet.length%3 != 0)){
					throw new IOException("Fail to parse style");
				}
				
				Style d = new Style();
				
				Decorator style = null;
				for(int i = 0; i < muti_triplet.length; i++){
					if(i%3 == 0){
						new Decorator();
					}
					
					switch(i%3){
					case 0:
					{
						style = new Decorator();
						style.mTag = muti_triplet[i];
					}break;
					case 1:
					{
						style.mDoctBegin = muti_triplet[i];
					}break;
					case 2:
					{
						style.mDoctEnd = muti_triplet[i];
						d.mDct.add(style);
					}break;
					}
				}
				
				return d;
			}
		}
		
		public static class Decorator{
			public int mTag;
			public int mDoctBegin;
			public int mDoctEnd;
			
			public Decorator(int[] triplet){
				mTag = triplet[0];
				mDoctBegin = triplet[1];
				mDoctEnd = triplet[2];
			}
			
			public Decorator(){}
			
		}
}