package com.crazyapk.net;
/**
 * 网络定义
 * @author wen.yugang </p> 
 * 2011-12-19
 */
public class NetConst {
	/**
	 * 请求成功
	 */
	public static final int RESULT_CODE_SUCCESS = 0;
	/**
	 * 客户端本身的错误
	 */
	public static final int RESULT_CODE_ERROR = -100;
	/**
	 * 服务端返回的错误
	 */
	public static final int RESULT_CODE_SERVER_SIDE_FALIED = RESULT_CODE_ERROR + 1;
	/**
	 * 客户端解析失败，可能是服务端返回错误数据
	 */
	public static final int RESULT_CODE_PARSE_FAILED = RESULT_CODE_ERROR + 2;
	/**
	 * 软件不存在
	 */
	public static final int ERROR_CODE_SOFT_UNEXIST = 1001;
}
