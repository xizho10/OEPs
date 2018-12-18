/*
 * **************************************************************************************
 *  Copyright © 2014-2018 Ontology Foundation Ltd.
 *  All rights reserved.
 *
 *  This software is supplied only under the terms of a license agreement,
 *  nondisclosure agreement or other written agreement with Ontology Foundation Ltd.
 *  Use, redistribution or other disclosure of any parts of this
 *  software is prohibited except in accordance with the terms of such written
 *  agreement with Ontology Foundation Ltd. This software is confidential
 *  and proprietary information of Ontology Foundation Ltd.
 *
 * **************************************************************************************
 */

package com.github.ontio.onto.asset;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.ontio.onto.BaseActivity;
import com.github.ontio.onto.R;
import com.github.ontio.onto.common.Constant;
import com.github.ontio.onto.singleton.SettingSingleton;
import com.github.ontio.onto.util.LogUtils;
import com.github.ontio.onto.util.SDKCallback;
import com.github.ontio.onto.util.SDKWrapper;
import com.github.ontio.onto.util.ToastUtil;
import com.github.ontio.onto.view.GetOngDialog;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/9/17.
 */
public class GameWebActivity extends BaseActivity {

    private final String TAG = "SocialMediaWebview";
    protected String mUrl = "";
    @BindView(R.id.progress_loading)
    ProgressBar pg;
    @BindView(R.id.frame)
    FrameLayout frameLayout;
    private WebView mWebView = null;
    private boolean mActivityDestroyed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_web);
        ButterKnife.bind(this);

        initData();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView = new WebView(getApplicationContext());
        mWebView.setLayoutParams(params);
        frameLayout.addView(mWebView);
        initWebView();

        mWebView.loadUrl("http://192.168.50.130:8080/");

    }

    private void initData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
//            mUrl = extras.getString(Constant.KEY);
        }
    }

    private void initWebView() {
        final WebSettings webSetting = mWebView.getSettings();
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);

        webSetting.setJavaScriptEnabled(true);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setAllowFileAccess(false);
        webSetting.setAllowFileAccessFromFileURLs(false);
        webSetting.setAllowContentAccess(false);
        webSetting.setDomStorageEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                String[] split = message.split("=");
                handleAction(split[split.length - 1]);
                if (result != null) {
                    result.confirm("");
                }
                if (result != null) {
                    result.cancel();
                }
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    pg.setVisibility(View.GONE);
                } else {
                    pg.setVisibility(View.VISIBLE);
                    pg.setProgress(newProgress);
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                LogUtils.LogI(TAG, "initData: " + request.getUrl().toString());
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Android8.0以下的需要返回true 并且需要loadUrl；8.0之后效果相反
                if (Build.VERSION.SDK_INT < 26) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mActivityDestroyed) {
                    return;
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//                error.getErrorCode()
//                加载本地失败的界面
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //https
                handler.proceed();
                super.onReceivedSslError(view, handler, error);
            }
        });

    }
    //1. Base64.decode   2. Uri.decode
    private void handleAction(String message) {

        byte[] decode = Base64.decode(message, Base64.NO_WRAP);
        String result = Uri.decode(new String(decode));
//        {"action":"login","params":{"type":"account","dappName":"My dapp","message":"test message","expired":"201812181000","callback":""}}
        LogUtils.LogI(TAG, "deal: " + result);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            String action = jsonObject.getString("action");
            switch (action) {
                case "login":
                case "invoke":
                    showDialog(result);
                    break;
                case "getAccount":
                    getAccount(jsonObject);
                    break;
                default:
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getAccount(JSONObject reqJson) {
//        {
//            action    string   // action
//            version   string   // version
//            error     int      // error code
//            desc      string   // desc of error code
//            result    string   // result
//        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action",reqJson.getString("action"));
            jsonObject.put("version",reqJson.getString("version"));
            jsonObject.put("error",0);
            jsonObject.put("desc","Success");
            jsonObject.put("result",SettingSingleton.getInstance().getDefaultAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String s = Base64.encodeToString(Uri.encode(jsonObject.toString()).getBytes(), Base64.NO_WRAP);
        mWebView.loadUrl("javascript:emitMessage(\"" + s + "\")");
    }

    private GetOngDialog getOngDialog;

    //显示付款
    private void showDialog(final String message) {
        if (getOngDialog != null && getOngDialog.isShowing()) {
            getOngDialog.dismiss();
        }
        getOngDialog = new GetOngDialog(this);
        getOngDialog.setConfirmListener(new GetOngDialog.ConfirmListener() {
            @Override
            public void passwordConfirm(String password) {
                getOngDialog.dismiss();
                showLoading();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(message);
                    String action = jsonObject.getString("action");
                    switch (action) {
                        case "login":
                            reqLogin(jsonObject.getJSONObject("params").getString("message"), password);
                            break;
                        case "invoke":
                            reqTran(message, password);
                            break;
                        default:
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        getOngDialog.show();
    }

    private void reqTran(String data, String password) {

        SDKWrapper.getSendAddress(new SDKCallback() {
            @Override
            public void onSDKSuccess(String tag, Object message) {
                dismissLoading();
                showAttention((String) message);
            }

            @Override
            public void onSDKFail(String tag, String message) {
                dismissLoading();
                showAttention((String) message);
            }
        }, TAG, data, password, SettingSingleton.getInstance().getDefaultAddress());
    }

    private void reqLogin(String data, String password) {
        SDKWrapper.getGameLogin(new SDKCallback() {
            @Override
            public void onSDKSuccess(String tag, Object message) {
                String result = (String) message;
                String s = Base64.encodeToString(Uri.encode(result).getBytes(), Base64.NO_WRAP);
                mWebView.loadUrl("javascript:emitMessage(\"" + s + "\")");
                dismissLoading();
            }

            @Override
            public void onSDKFail(String tag, String message) {
                dismissLoading();
                ToastUtil.showToast(GameWebActivity.this, message);
            }
        }, TAG, password, data);
    }

    /**
     * 停止webview的加载
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.i("ansen","是否有上一个页面:"+webView.canGoBack());
        if (mWebView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK) {//点击返回按钮的时候判断有没有上一页
            mWebView.goBack(); // goBack()表示返回webView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        mActivityDestroyed = true;
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @OnClick({R.id.line_back, R.id.line_finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.line_back:
                if (mWebView.canGoBack()) {//点击返回按钮的时候判断有没有上一页
                    mWebView.goBack(); // goBack()表示返回webView的上一页面
                } else {
                    finish();
                }
                break;
            case R.id.line_finish:
                finish();
                break;
            default:
        }
    }
}
