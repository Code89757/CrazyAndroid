package com.crazyapk.bean;

import java.io.Serializable;

import org.json.JSONObject;

import com.crazyapk.download.DownloadTask;

import android.os.Parcel;
import android.os.Parcelable;

public class AppBean implements Parcelable, Serializable {
	private static final long serialVersionUID = -2503927834462721670L;

	public String resId;
	public String identifier;
	public String detailUrl;
	public int star = 0;
	public String name;
	public String icon;
	public String versionName;
	public String downloadUrl;
	public String downnum;
	public int actionType;
	public String price;
	public String size;
	public int versionCode;
	public String author;
	public boolean warn;
	public int cb;
	public long markType;
	public int score;
	public int speciesType;
	
	public DownloadTask downloadTask = null;

	public static AppBean getObject(JSONObject jsonObject) {
		AppBean bean = new AppBean();
		bean.name = jsonObject.optString("name");
		bean.downloadUrl = jsonObject.optString("downloadUrl");
		bean.size = jsonObject.optString("size");
		bean.icon = jsonObject.optString("icon");
		bean.identifier = jsonObject.optString("identifier");
		bean.versionCode = jsonObject.optInt("versionCode");
		return bean;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(downloadUrl);
		dest.writeString(size);
		dest.writeString(identifier);
		dest.writeString(icon);
		dest.writeInt(versionCode);
	}

	public static final Parcelable.Creator<AppBean> CREATOR = new Parcelable.Creator<AppBean>() {
		@Override
		public AppBean createFromParcel(Parcel source) {
			AppBean bean = new AppBean();
			bean.name = source.readString();
			bean.downloadUrl = source.readString();
			bean.size = source.readString();
			bean.identifier = source.readString();
			bean.icon = source.readString();
			bean.versionCode = source.readInt();
			return bean;
		}

		@Override
		public AppBean[] newArray(int size) {
			return new AppBean[size];
		}
	};
}
