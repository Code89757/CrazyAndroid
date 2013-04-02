package com.crazyapk.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.crazyapk.bean.AppBean;
import com.crazyapk.bean.FetchResult;
import com.crazyapk.list.AppAdapter;
import com.crazyapk.list.FetchJsonHandler;
import com.crazyapk.net.NetFetcher;

public class MainActivity extends Activity
{
	private ListView	mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		String url = getIntent().getExtras().getString("URL");

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main_activity);
		setProgressBarIndeterminateVisibility(true);

		mListView = (ListView) findViewById(R.id.listview);
		AppAdapter adapter = new AppAdapter(getApplicationContext());
		mListView.setAdapter(adapter);

		NetFetcher fetcher = new NetFetcher();

		JsonFetchHandler handler = new JsonFetchHandler();
		handler.setDataListener(adapter);
		fetcher.request(url, handler);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				AppBean value = (AppBean) mListView.getItemAtPosition(position);
				Intent intent = new Intent(getApplicationContext(), DownloadPage.class);
				intent.putExtra("DATA", (Parcelable) value);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onPause(){
		super.onPause();
	}

	class JsonFetchHandler extends FetchJsonHandler<AppBean>
	{
		IDataListener	mListener;

		public void setDataListener(IDataListener listener)
		{
			mListener = listener;
		}

		@Override
		public void onError(int code, String error)
		{
			mListener.onError(code, error);
			setProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onSuccessed(FetchResult<AppBean> result)
		{
			mListener.onSuccessed(result);
			setProgressBarIndeterminateVisibility(false);
		}

		@Override
		public AppBean parseItem(JSONObject jsonObject) throws JSONException
		{
			return AppBean.getObject(jsonObject);
		}
	}
	
	public interface IDataListener
	{
		void onError(int code, String error);

		void onSuccessed(FetchResult result);
	}
}
