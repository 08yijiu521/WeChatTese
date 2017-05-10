/**
 * Copyright (C) 2014-2015 Imtoy Technologies. All rights reserved.
 * @charset UTF-8
 * @author xiongxunxiang
 */
package com.test.wechat.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.test.wechat.util.JsonUtils;
import com.test.wechat.wxapi.WXEntryActivity;

import org.json.JSONObject;

/**
 * @description 更多移动开发内容请关注： http://blog.csdn.net/xiong_it
 * @charset UTF-8
 * @author xiong_it
 * @date 2015-7-16下午5:38:38
 * @version 
 */
public class AchieveTokenTask extends AsyncTask<Void, Integer, Integer> {
	private static final String TAG = "AchieveTokenTask";
	private Context context;
	private String code ; 
	private String openid ; 
	private String access_token ; 
	private String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+WXEntryActivity.APP_ID+"&secret="+WXEntryActivity.APP_SECRET+"&code="+code+"&grant_type=authorization_code";
	AchieveTokenTask(Context context , String code) {
		this.context = context;
		this.code = code ;
	}
	@Override
	protected void onPreExecute() {
	}
	@Override
	protected Integer doInBackground(Void... params) {//appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
		try {
//			JSONObject jsonObject = JsonUtils.initSSLWithHttpClinet(Constant.LoginPathWXAchieveToken+"appid="+UtilConstant.WXAppID+"&secret="+UtilConstant.WXappSecret+"&code="+code+"&grant_type=authorization_code");
			JSONObject jsonObject = JsonUtils.initSSLWithHttpClinet(path);
			if (null != jsonObject) {
					openid = jsonObject.getString("openid").toString().trim();
					access_token = jsonObject.getString("access_token").toString().trim();
					Log.i(TAG, openid+"==openid+access_token=="+access_token);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null; 
	}
	@Override
	protected void onPostExecute(Integer integer) {	
		/**** 把处理完的数据发送给后台服务器*****/
//		(new SendTokenToback(WXEntryActivity.this, openid , access_token )).execute();
	}
	@Override
	protected void onProgressUpdate(Integer... values) {
	}
}
