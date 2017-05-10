package com.test.wechat.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXMusicObject;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXVideoObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.platformtools.Util;
import com.test.wechat.R;
import com.test.wechat.util.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Administrator on 2017/5/10 0010.
 */

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "WXEntryActivity";

    public static final String APP_ID = "wxbc9df1769fb5902e";// 微信开放平台申请到的app_id
    public static final String APP_SECRET = "0eb8d63342bfa52daef600366dce70a9";// 微信开放平台申请到的app_id对应的app_secret
    private static final String WEIXIN_SCOPE = "snsapi_userinfo";// 用于请求用户信息的作用域
    private static final String WEIXIN_STATE = "login_state"; // 自定义

    protected static final int RETURN_OPENID_ACCESSTOKEN = 0;// 返回openid，accessToken消息码
    protected static final int RETURN_NICKNAME_UID = 1; // 返回昵称，uid消息码

    protected static final int THUMB_SIZE = 150;// 分享的图片大小

    private TextView textView;// 显示token值，uid值
    private Button loginBtn;// 请求登录按钮
    private Button shareFriBtn;// 分享给好友按钮
    private Button shareCirBtn;// 分享到朋友圈按钮
    private ImageView imageView;// 显示分享图片

    private IWXAPI api;
    private SendAuth.Req req;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RETURN_OPENID_ACCESSTOKEN:
                    Bundle bundle1 = (Bundle) msg.obj;
                    String accessToken = bundle1.getString("access_token");
                    String openId = bundle1.getString("open_id");

                    getUID(openId, accessToken);
                    break;

                case RETURN_NICKNAME_UID:
                    Bundle bundle2 = (Bundle) msg.obj;
                    String nickname = bundle2.getString("nickname");
                    String uid = bundle2.getString("unionid");
                    textView.setText("uid:" + uid);
                    loginBtn.setText("昵称：" + nickname);
                    break;

                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        textView = (TextView) findViewById(R.id.token);
        loginBtn = (Button) findViewById(R.id.login_btn);
        shareFriBtn = (Button) findViewById(R.id.share_to_friend);
        shareCirBtn = (Button) findViewById(R.id.share_to_circle);
        imageView = (ImageView) findViewById(R.id.image);

        api = WXAPIFactory.createWXAPI(this, APP_ID, false);
        api.registerApp(APP_ID);// 注册到微信列表，没什么用，笔者不知道干嘛用的，有知道的请告诉我，该文件顶部有我博客链接。或加Q1692475028,谢谢！

        try {
            api.handleIntent(getIntent(), this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // 请求授权登录
        loginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "登录微信",
                        Toast.LENGTH_LONG).show();
                Log.i(TAG, "登录微信");
                sendAuth();
            }
        });

        // 分享到朋友圈
        shareCirBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG, "分享到朋友圈");
                String text = "微信分享纯文本"; // 用于分享的文字
                String url = "http://blog.csdn.net/xiong_it";// 用于分享的链接
                String picPath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+"test.jpg";// 用于分享的本地图片
                final String imgUrl = "http://segmentfault.com/img/bVkIvr";// 用于分享的在线图片
                String musicUrl = "http://staff2.ustc.edu.cn/~wdw/softdown/index.asp/0042515_05.ANDY.mp3";// 用于分享在线音乐
                String videoUrl = "http://v.youku.com/v_show/id_XMjExOTcxNg==.html?f=1245977";// 用于分享的在线视频

                // 分享文字：分享成功
                shareText2Circle(text);

                // 分享链接:分享成功
                //shareUrl2Circle(url);

                //分享sd图片:分享sdcard图片成功
                //shareLocalPic2Cir(picPath);

                // 分享在线图片:无法点击右上角发送按钮，如图：http://segmentfault.com/img/bVkIvr";
                // new DownloadPicTask().execute(imgUrl);//下载图片到本地后再分享，也只能这样了
                //分享本地图片：程序本地图片分享成功
				/*Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
				WXImageObject imgObj = new WXImageObject(bmp);

				WXMediaMessage msg = new WXMediaMessage();
				msg.mediaObject = imgObj;

				Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
				Log.i(TAG, "height = " + thumbBmp.getHeight());
				Log.i(TAG, "width = " + thumbBmp.getWidth());
				bmp.recycle();
				msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

				SendMessageToWX.Req req = new SendMessageToWX.Req();
				req.transaction = buildTransaction("img");
				req.message = msg;
				req.scene = SendMessageToWX.Req.WXSceneTimeline;
				api.sendReq(req);*/

                // 分享在线音乐：分享成功
                //shareMusic2Circle(musicUrl);

                // 分享低宽带音乐

                // 分享在线视频:分享成功
                //shareVideo2Circle(videoUrl);

                // 分享低宽带视频
            }

            /**
             * @param videoUrl 需要分享的视频链接
             */
            private void shareVideo2Circle(String videoUrl) {
                WXVideoObject videoObject = new WXVideoObject();
                videoObject.videoUrl = videoUrl;

                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = videoObject;
                msg.title = "分享视频标题";
                msg.description = "视频描述：MJ-劲爆热舞";

                Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
                msg.thumbData = Util.bmpToByteArray(thumb, true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.message = msg;
                req.transaction = buildTransaction("video");
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                api.sendReq(req);
            }

            /**
             * @param musicUrl 在线音乐链接
             */
            private void shareMusic2Circle(String musicUrl) {
                WXMusicObject musicObject = new WXMusicObject();
                musicObject.musicUrl = musicUrl;

                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = musicObject;
                msg.title = "分享音乐标题";
                msg.description = "音乐描述：这是一首非常流行的音乐";

                Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
                byte[] byteArray = Util.bmpToByteArray(thumb, true);
                msg.thumbData = byteArray;

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.message = msg;
                req.transaction = buildTransaction("music");
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                api.sendReq(req);
            }

            /**
             *
             */
            private void shareLocalPic2Cir(String picPath) {
                //TODO 判断图片是否存在
                WXImageObject imageObject = new WXImageObject();
                imageObject.setImagePath(picPath);

                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imageObject;

                Bitmap bmp = BitmapFactory.decodeFile(picPath);
                Bitmap thumb = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                bmp.recycle();
                msg.thumbData = Util.bmpToByteArray(thumb, true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("img");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                api.sendReq(req);
            }

            /**
             * @param url 要分享的链接
             */
            private void shareUrl2Circle(final String url) {
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = url;
                WXMediaMessage msg = new WXMediaMessage(webpage);
                msg.title = "分享网页标题";
                msg.description = "WebPage Description";
                Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
                msg.thumbData = Util.bmpToByteArray(thumb, true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                api.sendReq(req);
            }

            /**
             * @param text 要分享的文字
             */
            private void shareText2Circle(String text) {
                WXTextObject textObj = new WXTextObject();
                textObj.text = text;

                // 用WXTextObject对象初始化一个WXMediaMessage对象
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = textObj;
                // 发送文本类型的消息时，title字段不起作用
                // msg.title = "Will be ignored";
                msg.title = "分享文字标题";
                msg.description = text;

                // 构造一个Req
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
                req.message = msg;

                req.scene = SendMessageToWX.Req.WXSceneTimeline;// 表示发送场景为朋友圈，这个代表分享到朋友圈
                // req.scene = SendMessageToWX.Req.WXSceneSession;//表示发送场景为好友对话，这个代表分享给好友
                // req.scene = SendMessageToWX.Req.WXSceneTimeline;// 表示发送场景为收藏，这个代表添加到微信收藏
                // 调用api接口发送数据到微信
                api.sendReq(req);
            }
        });

        // 分享给好友:除在线图片外都测试成功
        shareFriBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG, "分享给好友");
                String text = "微信分享纯文本"; // 用于分享的文字
                String url = "http://blog.csdn.net/xiong_it";// 用于分享的链接

                shareText2Circle(text);
                // 其他内容分享参考分享到朋友圈即可，唯一不同的地方：req.scene不同

            }

            /**
             * 分享文字给好友
             * @param text
             */
            private void shareText2Circle(String text) {
                WXTextObject textObj = new WXTextObject();
                textObj.text = text;

                // 用WXTextObject对象初始化一个WXMediaMessage对象
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = textObj;
                // 发送文本类型的消息时，title字段不起作用
                // msg.title = "Will be ignored";
                msg.title = "分享文字标题";
                msg.description = text;

                // 构造一个Req
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
                req.message = msg;

                // req.scene = SendMessageToWX.Req.WXSceneTimeline;// 表示发送场景为朋友圈，这个代表分享到朋友圈
                req.scene = SendMessageToWX.Req.WXSceneSession;//表示发送场景为好友对话，这个代表分享给好友
                // 调用api接口发送数据到微信
                api.sendReq(req);
            }
        });

    }

    /**
     * 构造一个用于请求的唯一标识
     * @param type 分享的内容类型
     * @return
     */
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }

    /**
     * 申请授权
     */
    private void sendAuth() {
        req = new SendAuth.Req();
        req.scope = WEIXIN_SCOPE;
        req.state = WEIXIN_STATE;
        api.sendReq(req);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            Log.i(TAG, "返回图片？: "+resultCode );
            switch (requestCode) {
            case RESULT_FIRST_USER:
                //TODO 返回图片？
//                final WXMediaMessage.IMediaObject appdata = new com.tencent.mm.sdk.openapi.WXAppExtendObject();
//                final String path = "/sdcard/test.jpg";
//                appdata.filePath = path;
//                appdata.extInfo = "this is ext info";
//
//                final WXMediaMessage msg = new WXMediaMessage();
//                msg.setThumbImage(Util.extractThumbNail(path, 150, 150, true));
//                msg.title = "this is title";
//                msg.description = "this is description";
//                msg.mediaObject = appdata;
//
//                SendMessageToWX.Req req = new SendMessageToWX.Req();
//                req.transaction = buildTransaction("appdata");
//                req.message = msg;
//                req.scene = SendMessageToWX.Req.WXSceneTimeline;
//                api.sendReq(req);
                break;

            default:
                break;
        }
    }

    /**
     * 请求回调接口
     */
    @Override
    public void onReq(BaseReq req) {
        Log.i(TAG, "onReq");
    }

    /**
     * 请求响应回调接口
     */
    @Override
    public void onResp(BaseResp resp) {
        Log.i(TAG, "onResp");
        SendAuth.Resp sendAuthResp = (SendAuth.Resp) resp;// 用于分享时不要有这个，不能强转
        //TODO code 没有？
//        String code = sendAuthResp.code;
        String code = sendAuthResp.token;
        textView.setText("code:" + code);

        if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
            loginBtn.setText("注销登录");
            loginBtn.setTextColor(Color.BLUE);
        }
        getResult(code);
        int errCode = resp.errCode;
        Toast.makeText(this, "errCode = " + errCode, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取openid accessToken值用于后期操作
     * @param code 请求码
     */
    private void getResult(final String code) {
        new Thread() {// 开启工作线程进行网络请求
            public void run() {
                String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                        + APP_ID
                        + "&secret="
                        + APP_SECRET
                        + "&code="
                        + code
                        + "&grant_type=authorization_code";
                try {
                    JSONObject jsonObject = JsonUtils
                            .initSSLWithHttpClinet(path);// 请求https连接并得到json结果
                    if (null != jsonObject) {
                        String openid = jsonObject.getString("openid")
                                .toString().trim();
                        String access_token = jsonObject
                                .getString("access_token").toString().trim();
                        Log.i(TAG, "openid = " + openid);
                        Log.i(TAG, "access_token = " + access_token);

                        Message msg = handler.obtainMessage();
                        msg.what = RETURN_OPENID_ACCESSTOKEN;
                        Bundle bundle = new Bundle();
                        bundle.putString("openid", openid);
                        bundle.putString("access_token", access_token);
                        msg.obj = bundle;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            };
        }.start();
    }

    /**
     * 获取用户唯一标识
     * @param openId
     * @param accessToken
     */
    private void getUID(final String openId, final String accessToken) {
        new Thread() {
            @Override
            public void run() {
                String path = "https://api.weixin.qq.com/sns/userinfo?access_token="
                        + accessToken + "&openid=" + openId;
                JSONObject jsonObject = null;
                try {
                    jsonObject = JsonUtils.initSSLWithHttpClinet(path);
                    String nickname = jsonObject.getString("nickname");
                    String unionid = jsonObject.getString("unionid");
                    Log.i(TAG, "nickname = " + nickname);
                    Log.i(TAG, "unionid = " + unionid);

                    Message msg = handler.obtainMessage();
                    msg.what = RETURN_NICKNAME_UID;
                    Bundle bundle = new Bundle();
                    bundle.putString("nickname", nickname);
                    bundle.putString("unionid", unionid);
                    msg.obj = bundle;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }
}
