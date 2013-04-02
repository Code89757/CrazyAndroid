package com.crazyapk.bean;

import java.util.ArrayList;

public class FetchResult<T> {
	public int Code;
	public String ErrorDesc;
	public ResultBean<T> Result;
	public int Index;

	public static class ResultBean<T> {
		public boolean atLastPage = false;
		public ArrayList<T> items = new ArrayList<T>();
	}
}
