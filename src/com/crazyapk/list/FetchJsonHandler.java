package com.crazyapk.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crazyapk.bean.FetchResult;
import com.crazyapk.bean.FetchResult.ResultBean;
import com.crazyapk.net.BaseJsonFetchHandler;
import com.crazyapk.net.NetConst;
import com.crazyapk.net.ServerSideThrowException;

public abstract class FetchJsonHandler<T> extends BaseJsonFetchHandler {

	boolean isLastPage;
	boolean mHasIndex = false;
	int mIndex;
	
	List<T> mList = new ArrayList<T>();	
	protected FetchResult<T> result = new FetchResult<T>();
	
	protected Object parseData(JSONObject object) throws JSONException {
		JSONArray array = object.getJSONArray("items");
		isLastPage = object.getBoolean("atLastPage");

		// 判断是否有index参数，如果有证明服务端采用了新的分页方法。
		if (object.isNull("index")) {
			mHasIndex = false;
		} else {
			mHasIndex = true;
			mIndex = object.getInt("index");
		}

		int count = array.length();
		for (int index = 0; index < count; index++) {

			T bean = parseItem(array.getJSONObject(index));
			mList.add(bean);
		}
		return mList;
	}
	
	@Override
	protected void onReceivedData(String data){
		try {
			JSONObject object = new JSONObject(data);
			int code = object.getInt("Code");
			if (code == 0) {
				object = object.getJSONObject("Result");
				parseData(object);
				sendSuccessMessage(data);
			} else {
				String errorDes = object.optString("ErrorDesc");
				sendErrorMessage(NetConst.RESULT_CODE_SERVER_SIDE_FALIED,
						new ServerSideThrowException(code, errorDes));
			}
		} catch (Exception e) {
			sendErrorMessage(e);
		}
		return;
	}
	
	@Override
	protected void onSuccess(Object data) {
		result.Result = new ResultBean<T>();
		result.Result.atLastPage = isLastPage;
		result.Result.items.addAll(mList);
		result.Index = mIndex;
		onSuccessed(result);
	}
	
	@Override
	protected void onFailure(Throwable error) {		
		error.printStackTrace();
		if(error instanceof ServerSideThrowException){
			ServerSideThrowException ex = (ServerSideThrowException)error;
			onError(ex.getMessageCode(),ex.getMessage());
		}
		else{
			onError(NetConst.RESULT_CODE_ERROR, error.getMessage());
		}
	}

	public abstract void onError(int code, String error);

	public abstract void onSuccessed(FetchResult<T> result);

	public abstract T parseItem(JSONObject jsonObject) throws JSONException;
}
