package com.crazyapk.list;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crazyapk.android.MainActivity.IDataListener;
import com.crazyapk.android.R;
import com.crazyapk.bean.AppBean;
import com.crazyapk.bean.FetchResult;
import com.crazyapk.util.image.ImageViewLoader;
import com.crazyapk.util.image.RemoteImage;

public class AppAdapter extends AbsListViewAdapter<AppBean, AppViewItem> implements IDataListener {
	ArrayList<AppBean> mArray = new ArrayList<AppBean>();
	private Context context;
	public AppAdapter(Context applicationContext) {
		context = applicationContext;
	}

	@Override
	public int getCount() {
		return mArray.size();
	}

	@Override
	public AppBean getItem(int position) {
		return mArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		AppViewItem holder = null;
		if (view == null) {
			view = createItem();
			holder = initHolder(view);
			view.setTag(holder);
		} else {
			holder = (AppViewItem) view.getTag();
		}
		
		setViewContent(holder, getItem(position), position);

		return view;
	}

	private void setViewContent(AppViewItem holder, AppBean item, int position) {
		holder.NAME.setText(item.name);
		holder.SIZE.setText(item.size);
//		ImageViewLoader.load(holder.ICON, new RemoteImage(item.icon), R.drawable.ic_launcher);
	}

	private AppViewItem initHolder(View view) {
		AppViewItem item = new AppViewItem();
		item.NAME = (TextView)view.findViewById(R.id.textView1);
		item.SIZE = (TextView)view.findViewById(R.id.textView2);
		item.ICON = (ImageView)view.findViewById(R.id.imageView1);
		return item;
	}

	private View createItem() {
		return View.inflate(context, R.layout.app_item, null);
	}

	@Override
	public void onError(int code, String error) {
		
	}

	@Override
	public void onSuccessed(FetchResult result) {
		mArray.addAll(result.Result.items);
		notifyDataSetChanged();
	}

}
