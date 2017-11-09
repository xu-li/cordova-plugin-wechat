package __PACKAGE_NAME__;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
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

        IWXAPI api = Wechat.getWxAPI(this);

        if (api == null) {
            startMainActivity();
        } else {
            api.handleIntent(getIntent(), this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        IWXAPI api = Wechat.getWxAPI(this);
        if (api == null) {
            startMainActivity();
        } else {
            api.handleIntent(intent, this);
        }

    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(Wechat.TAG, resp.toString());

        CallbackContext ctx = Wechat.getCurrentCallbackContext();

        if (ctx == null) {
            startMainActivity();
            return ;
        }

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                switch (resp.getType()) {
                    case ConstantsAPI.COMMAND_SENDAUTH:
                        auth(resp);
                        break;
                    case ConstantsAPI.COMMAND_CHOOSE_CARD_FROM_EX_CARD_PACKAGE:
                        plunckInvoiceData(resp);
                        break;
                    case ConstantsAPI.COMMAND_PAY_BY_WX:
                    default:
                        ctx.success();
                        break;
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                ctx.error(Wechat.ERROR_WECHAT_RESPONSE_USER_CANCEL);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                ctx.error(Wechat.ERROR_WECHAT_RESPONSE_AUTH_DENIED);
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                ctx.error(Wechat.ERROR_WECHAT_RESPONSE_SENT_FAILED);
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                ctx.error(Wechat.ERROR_WECHAT_RESPONSE_UNSUPPORT);
                break;
            case BaseResp.ErrCode.ERR_COMM:
                ctx.error(Wechat.ERROR_WECHAT_RESPONSE_COMMON);
                break;
            default:
                ctx.error(Wechat.ERROR_WECHAT_RESPONSE_UNKNOWN);
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

        // get current callback context
        CallbackContext ctx = Wechat.getCurrentCallbackContext();

        if (ctx == null) {
            return ;
        }

        JSONObject response = new JSONObject();
        try {
            response.put("code", res.code);
            response.put("state", res.state);
            response.put("country", res.country);
            response.put("lang", res.lang);
        } catch (JSONException e) {
            Log.e(Wechat.TAG, e.getMessage());
        }

        ctx.success(response);
    }

    protected void plunckInvoiceData(BaseResp resp) {

            CallbackContext ctx = Wechat.getCurrentCallbackContext();
            ChooseCardFromWXCardPackage.Resp resp1 = (ChooseCardFromWXCardPackage.Resp) resp;
            JSONObject response = new JSONObject();

            try {
                JSONArray resp2 = new JSONArray(resp1.cardItemList);
                response.put("data", resp2);
            } catch (JSONException e) {
                Log.e(Wechat.TAG, e.getMessage());
            }

            ctx.success(response);
        }
}
