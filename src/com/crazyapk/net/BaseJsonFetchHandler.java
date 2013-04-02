package com.crazyapk.net;

import com.crazyapk.net.NetFetcher.INetFetcher;

import android.os.Handler;

public abstract class BaseJsonFetchHandler implements INetFetcher {

	private final Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NetConst.RESULT_CODE_SUCCESS:
				onSuccess(msg.obj);
				break;
			case NetConst.RESULT_CODE_ERROR:
				onFailure((Throwable)msg.obj);
				break;
			case NetConst.RESULT_CODE_SERVER_SIDE_FALIED:
				onFailure((Throwable)msg.obj);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void sendCompletedMessage(String content) {
		try {
			onReceivedData(content);
		} catch (Exception e) {
			sendErrorMessage(e);
		}
	}

	@Override
	public void sendErrorMessage(Throwable error) {
		sendErrorMessage(NetConst.RESULT_CODE_ERROR, error);
	}
	
	public void sendErrorMessage(int code,Throwable error) {
		mHandler.sendMessage(mHandler.obtainMessage(code, error));
	}

	public void sendSuccessMessage(Object data) {
		mHandler.sendMessage(mHandler.obtainMessage(NetConst.RESULT_CODE_SUCCESS, data));
	}

	protected abstract void onSuccess(Object data);

	protected abstract void onReceivedData(String data) throws Exception;

	protected void onFailure(Throwable error) {	}
}
