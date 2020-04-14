package Netty.coder.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Message {
	private int magicNumber;
	private int mainVersion;
	private int subVersion;
	private int modifyVersion;
	
	private MessageTypeEnum messageType;  //消息类型
	private int sessionId;   //用户身份证明
	private Map<String,String> attachments = new HashMap<>();
	
	private String body;
	
	public int getMagicNumber() {
		return magicNumber;
	}
	public void setMagicNumber(int magicNumber) {
		this.magicNumber=magicNumber;
	}
	//主版本号
	public int getMainVersion() {
		return mainVersion;
	}
	public void setMainVersion(int mainVersion) {
		this.mainVersion=mainVersion;
	}
	//次版本号
	public int getSubVersion() {
		return subVersion;
	}
	public void setSubVersion(int subVersion) {
		this.subVersion=subVersion;
	}
	//三版本号
	public int getModifyVersion() {
		return modifyVersion;
	}
	public void setModifyVersion(int modifyVersion) {
		this.modifyVersion=modifyVersion;
	}
	//消息类型
	public MessageTypeEnum getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageTypeEnum messageType) {
		this.messageType=messageType;
	}
	//sessionID
	public int getSessionId() {
		return sessionId;
	}
	public void setSessionId(int sessionId) {
		this.sessionId=sessionId;
	}
	
	//attachments
	public Map<String, String> getAttachments() {
	    return Collections.unmodifiableMap(attachments);  //产生一个只读的Map
    }
	public String getAttachments(String key) {
		return attachments.get(key);
	}
	public void setAttachments(Map<String, String> attachments) {
	    this.attachments.clear();
	    if (null != attachments) {
	      this.attachments.putAll(attachments);
	    }
	  }

	public void addAttachment(String key, String value) {
	    attachments.put(key, value);
	}
	//body
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body=body;
	}

}
