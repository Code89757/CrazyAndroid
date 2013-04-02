package com.crazyapk.net;
/**
 * 服务端抛出的异常，里面含有服务端返回的错误码和错误描述。
 * @author wen.yugang </br>
 *  2012-5-18
 */
public class ServerSideThrowException extends Exception {
	public static final long serialVersionUID = 5392421440090702151L;
	private int mCode;

	public ServerSideThrowException(int code, String message) {
		super(message);
		mCode = code;
	}
	
	public int  getMessageCode(){
		return mCode;
	}
}
