package com.umeng.editor.decode;

import java.io.IOException;
import java.util.Stack;

public class BXMLTree implements IAXMLSerialize{
	private final int NS_START  = 0x00100100;
	private final int NS_END  	= 0x00100101;
	private final int NODE_START= 0x00100102;
	private final int NODE_END  = 0x00100103;
	private final int TEXT		= 0x00100104;
	
	private Stack<BXMLNode> mVisitor;
	private BNSNode mRoot;

	public BXMLTree(){
		mRoot = new BNSNode();
		mVisitor = new Stack<BXMLNode>();
	}
	
	public void print(IVisitor visitor){
		mRoot.accept(visitor);
	}

	public int getSize(){
		return 0;
	}
	
	public BXMLNode getRoot(){
		return mRoot;
	}
	
	public void read(IntReader reader) throws IOException{
		mRoot.checkStartTag(NS_START);
		mVisitor.push(mRoot);
		mRoot.readStart(reader);
		
		int chunkType;
		
		end:while(true){
			chunkType = reader.readInt();
			
			switch(chunkType){
			case NODE_START:
			{
				BTagNode node = new BTagNode();
				node.checkStartTag(NODE_START);
				BXMLNode parent = mVisitor.peek();
				parent.addChild(node);
				mVisitor.push(node);
				
				node.readStart(reader);
			}
			break;
			case NODE_END:
			{
				BTagNode node = (BTagNode)mVisitor.pop();
				node.checkEndTag(NODE_END);
				node.readEnd(reader);
			}
			break;
			case TEXT:
			{
				System.out.println("Hello Text");
				
			}
			break;
			case NS_END:
				break end;
			}
		}
		
		if( !mRoot.equals(mVisitor.pop())){
			throw new IOException("doc has invalid end");
		}
		
		mRoot.checkEndTag(chunkType);
		mRoot.readEnd(reader);
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
