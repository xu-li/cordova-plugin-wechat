package xu.li.cordova.wechat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Wechat extends CordovaPlugin {

    public static final String TAG = "Cordova.Plugin.Wechat";

    public static final String WXAPPID_PROPERTY_KEY = "wechatappid";

    public static final String ERROR_WECHAT_NOT_INSTALLED = "未安装微信";
    public static final String ERROR_INVALID_PARAMETERS = "参数错误";
    public static final String ERROR_USER_CANCEL = "用户点击取消并返回";
    public static final String ERROR_AUTH_DENIED = "授权失败";
    public static final String ERROR_SENT_FAILED = "发送失败";
    public static final String ERROR_UNSUPPORT = "微信不支持";
    public static final String ERROR_COMMON = "普通错误类型";
    public static final String ERROR_UNKNOWN = "未知错误";

    public static final String EXTERNAL_STORAGE_IMAGE_PREFIX = "external://";

    public static final String KEY_ARG_MESSAGE = "message";
    public static final String KEY_ARG_SCENE = "scene";
    public static final String KEY_ARG_TEXT = "text";
    public static final String KEY_ARG_MESSAGE_TITLE = "title";
    public static final String KEY_ARG_MESSAGE_DESCRIPTION = "description";
    public static final String KEY_ARG_MESSAGE_THUMB = "thumb";
    public static final String KEY_ARG_MESSAGE_MEDIA = "media";
    public static final String KEY_ARG_MESSAGE_MEDIA_TYPE = "type";
    public static final String KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL = "webpageUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_IMAGE = "image";
    public static final String KEY_ARG_MESSAGE_MEDIA_TEXT = "text";

    public static final int TYPE_WX_SHARING_APP = 1;
    public static final int TYPE_WX_SHARING_EMOTION = 2;
    public static final int TYPE_WX_SHARING_FILE = 3;
    public static final int TYPE_WX_SHARING_IMAGE = 4;
    public static final int TYPE_WX_SHARING_MUSIC = 5;
    public static final int TYPE_WX_SHARING_VIDEO = 6;
    public static final int TYPE_WX_SHARING_WEBPAGE = 7;
    public static final int TYPE_WX_SHARING_TEXT = 8;

    public static final int SCENE_SESSION = 0;
    public static final int SCENE_TIMELINE = 1;
    public static final int SCENE_FAVORITE = 2;

    public static IWXAPI wxAPI;
    public static CallbackContext currentCallbackContext;

    @Override
    protected void pluginInitialize() {
        // TODO Auto-generated method stub
        super.pluginInitialize();
        if (wxAPI == null) {

            String appId = webView.getPreferences().getString(WXAPPID_PROPERTY_KEY, "");
            wxAPI = WXAPIFactory.createWXAPI(webView.getContext(), appId, true);
        }
        wxAPI.registerApp(webView.getPreferences().getString(WXAPPID_PROPERTY_KEY, ""));
    }
    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, action + " is called.");

        if (action.equals("share")) {
            return share(args, callbackContext);
        } else if (action.equals("sendAuthRequest")) {
            return sendAuthRequest(args, callbackContext);
        } else if (action.equals("isWXAppInstalled")) {
            return isInstalled(callbackContext);
        }
        return super.execute(action, args, callbackContext);
    }

    protected boolean share(JSONArray args, CallbackContext callbackContext)
            throws JSONException {
        final IWXAPI api = getWXAPI(true);

        // check if installed
        if (!api.isWXAppInstalled()) {
            callbackContext.error(ERROR_WECHAT_NOT_INSTALLED);
            return true;
        }

        // check if # of arguments is correct
        if (args.length() != 1) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final JSONObject params = args.getJSONObject(0);
        final SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction();

        if (params.has(KEY_ARG_SCENE)) {
            int scene = params.getInt(KEY_ARG_SCENE);
            switch (scene) {
                case SCENE_FAVORITE:
                    req.scene = SendMessageToWX.Req.WXSceneFavorite;
                    break;
                case SCENE_TIMELINE:
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;
                    break;
                case SCENE_SESSION:
                    req.scene = SendMessageToWX.Req.WXSceneSession;
                    break;
            }
        } else {
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        }

        // run in background
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    req.message = buildSharingMessage(params);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to build message." + e);
                }

                api.sendReq(req);
                Log.d(TAG, "Message sent.");
            }
        });

        // save the current callback context
        currentCallbackContext = callbackContext;

        return true;
    }

    protected boolean sendAuthRequest(JSONArray args, CallbackContext callbackContext) {
        final IWXAPI api = getWXAPI(true);

        final SendAuth.Req req = new SendAuth.Req();
        req.state = "wechat_auth";

        // check if # of arguments is correct
        if (args.length() > 0) {
            try {
                req.scope = args.getString(0);
            } catch (Exception e) {
                Log.e(TAG, "invalid parameter in sendAuthRequest.", e);
            }
        } else {
            req.scope = "snsapi_userinfo";
        }

        api.sendReq(req);
        currentCallbackContext = callbackContext;

        return true;
    }

    protected boolean isInstalled(CallbackContext callbackContext) {
        final IWXAPI api = getWXAPI(true);

        if (!api.isWXAppInstalled()) {
            callbackContext.success(0);
        } else {
            callbackContext.success(1);
        }
        return true;
    }

    protected WXMediaMessage buildSharingMessage(JSONObject params)
            throws JSONException {
        Log.d(TAG, "Start building message.");

        // media parameters
        WXMediaMessage.IMediaObject mediaObject = null;
        WXMediaMessage wxMediaMessage = new WXMediaMessage();

        if (params.has(KEY_ARG_TEXT)) {
            WXTextObject textObject = new WXTextObject();
            textObject.text = params.getString(KEY_ARG_TEXT);
            mediaObject = textObject;
            wxMediaMessage.description = textObject.text;
        } else {
            JSONObject message = params.getJSONObject(KEY_ARG_MESSAGE);
            JSONObject media = message.getJSONObject(KEY_ARG_MESSAGE_MEDIA);

            wxMediaMessage.title = message.getString(KEY_ARG_MESSAGE_TITLE);
            wxMediaMessage.description = message
                    .getString(KEY_ARG_MESSAGE_DESCRIPTION);

            // thumbnail
            Bitmap thumbnail = getBitmap(message, KEY_ARG_MESSAGE_THUMB);
            if (thumbnail != null) {
                wxMediaMessage.thumbData = Util.bmpToByteArray(thumbnail, true);
            }

            // check types
            int type = media.has(KEY_ARG_MESSAGE_MEDIA_TYPE) ? media
                    .getInt(KEY_ARG_MESSAGE_MEDIA_TYPE) : TYPE_WX_SHARING_WEBPAGE;

            switch (type) {
                case TYPE_WX_SHARING_APP:
                    break;

                case TYPE_WX_SHARING_EMOTION:
                    break;

                case TYPE_WX_SHARING_FILE:
                    break;

                case TYPE_WX_SHARING_IMAGE:
                    mediaObject = new WXImageObject();
                    String image = getImageURL(message.getJSONObject(KEY_ARG_MESSAGE_MEDIA), KEY_ARG_MESSAGE_MEDIA_IMAGE);
                    ((WXImageObject) mediaObject).setImagePath(image);
                case TYPE_WX_SHARING_MUSIC:
                    break;

                case TYPE_WX_SHARING_VIDEO:
                    break;

                case TYPE_WX_SHARING_WEBPAGE:
                default:
                    mediaObject = new WXWebpageObject();
                    ((WXWebpageObject) mediaObject).webpageUrl = media
                            .getString(KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL);
            }
        }

        wxMediaMessage.mediaObject = mediaObject;

        return wxMediaMessage;
    }

    protected IWXAPI getWXAPI() {
        return getWXAPI(true);
    }

    protected IWXAPI getWXAPI(boolean register) {
        String appId = preferences.getString(WXAPPID_PROPERTY_KEY, "");

        if (wxAPI == null) {
            wxAPI = WXAPIFactory.createWXAPI(webView.getContext(), appId, true);
        }

        if (register) {
            wxAPI.registerApp(appId);
        }

        return wxAPI;
    }

    private String buildTransaction() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String buildTransaction(final String type) {
        return type + System.currentTimeMillis();
    }

    protected String getImageURL(JSONObject message, String key) {
        String url = null;

        try {
            url = message.getString(key);

            if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("data:")) {
                return url;
            } else if (url.startsWith(EXTERNAL_STORAGE_IMAGE_PREFIX)) { // external path
                String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                url = externalStoragePath + url.substring(EXTERNAL_STORAGE_IMAGE_PREFIX.length());
            } else if (!url.startsWith("/")) { // relative path
                Context context = cordova.getActivity().getApplicationContext();

                File f = new File(context.getCacheDir() + "/assets/" + url);
                if (!f.exists()) {

                    File parent = f.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    // save it to cache
                    try {
                        InputStream is = context.getAssets().open(url);
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();

                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(buffer);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                url = f.getAbsolutePath();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return url;
    }

    protected Bitmap getBitmap(JSONObject message, String key) {
        String url = getImageURL(message, key);

        if (url != null) {
            try {
                return BitmapFactory.decodeFile(url);
            } catch (Exception e) {
                Log.e(TAG, "Failed to decode image at " + url, e);
            }
        }

        return null;
    }
}