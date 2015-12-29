package __PACKAGE_NAME__;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import org.json.JSONException;
import org.json.JSONObject;

import xu.li.cordova.wechat.Wechat;

/**
 * Created by xu.li<AthenaLightenedMyPath@gmail.com> on 9/1/15.
 */
public class EntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Wechat.instance.getWxAPI() == null) {
            startMainActivity();
        } else {
            Wechat.instance.getWxAPI().handleIntent(getIntent(), this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        if (Wechat.instance.getWxAPI() == null) {
            startMainActivity();
        } else {
            Wechat.instance.getWxAPI().handleIntent(intent, this);
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(Wechat.TAG, resp.toString());

        if (Wechat.instance.getCurrentCallbackContext() == null) {
            startMainActivity();
            return ;
        }

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                switch (resp.getType()) {
                    case ConstantsAPI.COMMAND_SENDAUTH:
                        auth(resp);
                        break;

                    case ConstantsAPI.COMMAND_PAY_BY_WX:
                    default:
                        Wechat.instance.getCurrentCallbackContext().success();
                        break;
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Wechat.instance.getCurrentCallbackContext().error(Wechat.ERROR_WECHAT_RESPONSE_USER_CANCEL);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                Wechat.instance.getCurrentCallbackContext().error(Wechat.ERROR_WECHAT_RESPONSE_AUTH_DENIED);
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                Wechat.instance.getCurrentCallbackContext().error(Wechat.ERROR_WECHAT_RESPONSE_SENT_FAILED);
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                Wechat.instance.getCurrentCallbackContext().error(Wechat.ERROR_WECHAT_RESPONSE_UNSUPPORT);
                break;
            case BaseResp.ErrCode.ERR_COMM:
                Wechat.instance.getCurrentCallbackContext().error(Wechat.ERROR_WECHAT_RESPONSE_COMMON);
                break;
            default:
                Wechat.instance.getCurrentCallbackContext().error(Wechat.ERROR_WECHAT_RESPONSE_UNKNOWN);
                break;
        }

        finish();
    }

    @Override
    public void onReq(BaseReq req) {
        finish();
    }

    protected void startMainActivity() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getApplicationContext().getPackageName());
        getApplicationContext().startActivity(intent);
    }

    protected void auth(BaseResp resp) {
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

        Wechat.instance.getCurrentCallbackContext().success(response);
    }
}
