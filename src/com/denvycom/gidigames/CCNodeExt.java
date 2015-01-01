package com.denvycom.gidigames;

import org.cocos2d.nodes.CCNode;

public class CCNodeExt extends CCNode{
	public  String nodeText ;
	public int nodeThrowTag ;
	public int nodeNum ;
	
	public CCNodeExt(){
		super();
					
	}
	
	public int getNodeNum() {
		return nodeNum;
	}

	public void setNodeNum(int nodeNum) {
		this.nodeNum = nodeNum;
	}

	public void setNodeText(String nText){
		this.nodeText = nText; 
	}
	
	public String getNodeText(){
		return this.nodeText ;
	}
	
	public void setNodeThrowTag(int throwTag){
		this.nodeThrowTag = throwTag; 
	}

	public int getNodeThrowTag(){
		return this.nodeThrowTag ;
	}
}
