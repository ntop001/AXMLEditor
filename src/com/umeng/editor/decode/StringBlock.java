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
import java.util.Arrays;
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
		
		private List<String> mStrings;
		private byte[] mRawStyles;
		
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
				mRawStyles = reader.readByteArray(size);
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

        ///////////////////////////////////////////// implementation

        public StringBlock() {
        }
        
        /**
         * Returns style information - array of int triplets,
         * where in each triplet:
         *      * first int is index of tag name ('b','i', etc.)
         *      * second int is tag start index in string
         *      * third int is tag end index in string
         */
        public int[] getStyle(int index) {
                if (m_styleOffsets==null || m_styles==null ||
                        index>=m_styleOffsets.length)
                {
                        return null;
                }
                int offset=m_styleOffsets[index]/4;
                int style[];
                {
                        int count=0;
                        for (int i=offset;i<m_styles.length;++i) {
                                if (m_styles[i]==-1) {
                                        break;
                                }
                                count+=1;
                        }
                        if (count==0 || (count%3)!=0) {
                                return null;
                        }
                        style=new int[count];
                }
                for (int i=offset,j=0;i<m_styles.length;) {
                        if (m_styles[i]==-1) {
                                break;
                        }
                        style[j++]=m_styles[i++];
                }
                return style;
        }
       
        private int[] m_styleOffsets;
        private int[] m_styles;
        
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
}