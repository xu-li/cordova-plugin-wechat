package __PACKAGE_NAME__;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import org.json.JSONException;
import org.json.JSONObject;

import xu.li.cordova.wechat.Wechat;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

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
        Log.d(Wechat.TAG, resp.toString());

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                switch (resp.getType()) {
                    case ConstantsAPI.COMMAND_SENDAUTH:
                        auth(resp);
                        break;
                    default:
                        Wechat.currentCallbackContext.success();
                        break;
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Wechat.currentCallbackContext.error(Wechat.ERROR_USER_CANCEL);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                Wechat.currentCallbackContext.error(Wechat.ERROR_AUTH_DENIED);
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                Wechat.currentCallbackContext.error(Wechat.ERROR_SENT_FAILED);
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                Wechat.currentCallbackContext.error(Wechat.ERROR_UNSUPPORT);
                break;
            case BaseResp.ErrCode.ERR_COMM:
                Wechat.currentCallbackContext.error(Wechat.ERROR_COMMON);
                break;
            default:
                Wechat.currentCallbackContext.error(Wechat.ERROR_UNKNOWN);
                break;
        }

        finish();
    }

    private void auth(BaseResp resp) {
        SendAuth.Resp res = ((SendAuth.Resp) resp);

        Log.d(Wechat.TAG, res.toString());

        JSONObject response = new JSONObject();
        try {
            response.put("code", res.code);
            response.put("state", res.state);
            response.put("country", res.country);
            response.put("lang", res.lang);
        } catch (JSONException e) {
            Log.e(Wechat.TAG, e.getMessage());
        }

        Wechat.currentCallbackContext.success(response);
    }

    @Override
    public void onReq(com.tencent.mm.sdk.modelbase.BaseReq req) {
        finish();
    }
}
