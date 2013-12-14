package com.umeng.editor;

import java.util.List;

import com.umeng.editor.decode.AXMLDoc;
import com.umeng.editor.decode.BTagNode;
import com.umeng.editor.decode.BTagNode.Attribute;
import com.umeng.editor.decode.BXMLNode;
import com.umeng.editor.decode.StringBlock;
import com.umeng.editor.utils.TypedValue;

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
		//First add resource and get mapping ids
		StringBlock sb = doc.getStringBlock();
		
		int meta_data = sb.putString(META_DATA);
		int attr_name = sb.putString(NAME);
		int attr_value = sb.putString(VALUE);
		int channel_name = sb.putString(mChannelName);
		int channel_value = sb.putString(mChannelValue);
		int namespace = sb.getStringMapping("android");
		
		BXMLNode application = doc.getApplicationNode(); //manifest node
		List<BXMLNode> children = application.getChildren();
		
		BTagNode umeng_meta = null;
		
		end:for(BXMLNode node : children){
			BTagNode m = (BTagNode)node;
			//it's a risk that the value for "android:name" may be not String
			if((meta_data == m.getName()) && (m.getAttrStringForKey(attr_name) == channel_name)){
					umeng_meta = m;
					break end;
			}
		}
		
		if(umeng_meta != null){
			umeng_meta.setAttrStringForKey(attr_value, channel_value);
		}else{
			Attribute name_attr = new Attribute(namespace, attr_name, TypedValue.TYPE_STRING);
			name_attr.setString( channel_name );
			Attribute value_attr = new Attribute(namespace, attr_value, TypedValue.TYPE_STRING);
			value_attr.setString( channel_value );
			
			umeng_meta = new BTagNode(-1, meta_data);
			umeng_meta.setAttribute(name_attr);
			umeng_meta.setAttribute(value_attr);
			
			children.add(umeng_meta);
		}
		
		//doc.build();
	}
	
}
