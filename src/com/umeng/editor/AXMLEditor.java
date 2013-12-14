package com.umeng.editor;

import java.util.List;

import com.umeng.editor.decode.AXMLDoc;
import com.umeng.editor.decode.BTagNode;
import com.umeng.editor.decode.BTagNode.Attribute;
import com.umeng.editor.decode.BXMLNode;
import com.umeng.editor.decode.StringBlock;

public class AXMLEditor implements ICommand{
	private final String META_DATA = "meta-data";
	private final String NAME = "name";
	private final String VALUE = "value";
	
	private String mChannelName = "UMENG_CHANNEL";
	private String mChannelValue = "wandoujia";
	
	private AXMLDoc doc;
	
	@Override
	public void editMetaData(String name, String value) {
		// TODO Auto-generated method stu
	}

	public void editMetaData(){
		StringBlock sb = doc.getStringBlock();
		sb.addString(META_DATA, NAME, VALUE);
		sb.addString(mChannelName, mChannelValue);
		
		BXMLNode application = doc.getApplicationNode(); //manifest node
		
		List<BXMLNode> children = application.getChildren();
		
		BXMLNode umeng_meta = null;
		
		end:for(BXMLNode node : children){
			BTagNode m = (BTagNode)node;
			String name = sb.getStringFor(m.getName());
			if(META_DATA.equals(name)){
				Attribute[] attrs = m.getAttribute();
				
				for(Attribute attr: attrs){
					String a_name = sb.getStringFor( attr.mName );
					String a_value = sb.getStringFor( attr.mString );
					
					if(NAME.equals(a_name) && mChannelName.equals(a_value)){
						umeng_meta = node;
						break end;
					}
				}
			}
		}
		
		if(umeng_meta == null){
			//add a new elements
		}else{
			//edit value
		}
		
		
		
	}
	
}
