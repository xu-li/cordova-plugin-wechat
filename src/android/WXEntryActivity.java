package com.wizky.housediary.wxapi;

import xu.li.cordova.wechat.Wechat;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;


/*
    Cordova WeChat Plugin
    https://github.com/vilic/cordova-plugin-wechat

    by VILIC VANE
    https://github.com/vilic

    MIT License
*/

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Wechat.wxAPI.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Wechat.wxAPI.handleIntent(intent, this);
    }


    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                Wechat.currentCallbackContext.success();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Wechat.currentCallbackContext.error(Wechat.ERR_USER_CANCEL);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                Wechat.currentCallbackContext.error(Wechat.ERR_AUTH_DENIED);
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                Wechat.currentCallbackContext.error(Wechat.ERR_SENT_FAILED);
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                Wechat.currentCallbackContext.error(Wechat.ERR_UNSUPPORT);
                break;
            case BaseResp.ErrCode.ERR_COMM:
                Wechat.currentCallbackContext.error(Wechat.ERR_COMM);
                break;
            default:
                Wechat.currentCallbackContext.error(Wechat.ERR_UNKNOWN);
                break;
        }
        finish();
    }

    @Override
    public void onReq(com.tencent.mm.sdk.modelbase.BaseReq req) {
        finish();
    }
}