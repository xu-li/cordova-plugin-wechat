package xu.li.cordova.wechat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXEmojiObject;
import com.tencent.mm.sdk.modelmsg.WXFileObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Wechat extends CordovaPlugin {

    public static final String TAG = "Cordova.Plugin.Wechat";

    public static final String WXAPPID_PROPERTY_KEY = "wechatappid";

    public static final String ERROR_WECHAT_NOT_INSTALLED = "未安装微信";
    public static final String ERROR_INVALID_PARAMETERS = "参数格式错误";
    public static final String ERROR_SEND_REQUEST_FAILED = "发送请求失败";
    public static final String ERROR_WECHAT_RESPONSE_COMMON = "普通错误";
    public static final String ERROR_WECHAT_RESPONSE_USER_CANCEL = "用户点击取消并返回";
    public static final String ERROR_WECHAT_RESPONSE_SENT_FAILED = "发送失败";
    public static final String ERROR_WECHAT_RESPONSE_AUTH_DENIED = "授权失败";
    public static final String ERROR_WECHAT_RESPONSE_UNSUPPORT = "微信不支持";
    public static final String ERROR_WECHAT_RESPONSE_UNKNOWN = "未知错误";

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
    public static final String KEY_ARG_MESSAGE_MEDIA_MUSICURL = "musicUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_MUSICDATAURL = "musicDataUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_VIDEOURL = "videoUrl";
    public static final String KEY_ARG_MESSAGE_MEDIA_FILE = "file";
    public static final String KEY_ARG_MESSAGE_MEDIA_EMOTION = "emotion";
    public static final String KEY_ARG_MESSAGE_MEDIA_EXTINFO = "extInfo";
    public static final String KEY_ARG_MESSAGE_MEDIA_URL = "url";

    public static final int TYPE_WECHAT_SHARING_APP = 1;
    public static final int TYPE_WECHAT_SHARING_EMOTION = 2;
    public static final int TYPE_WECHAT_SHARING_FILE = 3;
    public static final int TYPE_WECHAT_SHARING_IMAGE = 4;
    public static final int TYPE_WECHAT_SHARING_MUSIC = 5;
    public static final int TYPE_WECHAT_SHARING_VIDEO = 6;
    public static final int TYPE_WECHAT_SHARING_WEBPAGE = 7;
    public static final int TYPE_WECHAT_SHARING_TEXT = 8;

    public static final int SCENE_SESSION = 0;
    public static final int SCENE_TIMELINE = 1;
    public static final int SCENE_FAVORITE = 2;

    public static final int MAX_THUMBNAIL_SIZE = 320;

    public static Wechat instance = null;

    protected CallbackContext currentCallbackContext;
    protected IWXAPI wxAPI;
    protected String appId;

    @Override
    protected void pluginInitialize() {

        super.pluginInitialize();

        instance = this;

        initWXAPI();

        Log.d(TAG, "plugin initialized.");
    }

    protected void initWXAPI() {
        if (wxAPI == null) {
            String appId = getAppId();

            wxAPI = WXAPIFactory.createWXAPI(webView.getContext(), appId, true);
            wxAPI.registerApp(appId);
        }
    }

    public IWXAPI getWxAPI() {
        return wxAPI;
    }

    public CallbackContext getCurrentCallbackContext() {
        return currentCallbackContext;
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, String.format("%s is called. Callback ID: %s.", action, callbackContext.getCallbackId()));

        if (action.equals("share")) {
            return share(args, callbackContext);
        } else if (action.equals("sendAuthRequest")) {
            return sendAuthRequest(args, callbackContext);
        } else if (action.equals("sendPaymentRequest")) {
            return sendPaymentRequest(args, callbackContext);
        } else if (action.equals("isWXAppInstalled")) {
            return isInstalled(callbackContext);
        }

        return false;
    }

    protected boolean share(CordovaArgs args, final CallbackContext callbackContext)
            throws JSONException {
        final IWXAPI api = getWXAPI();

        // check if installed
        if (!api.isWXAppInstalled()) {
            callbackContext.error(ERROR_WECHAT_NOT_INSTALLED);
            return true;
        }

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction();

        if (params.has(KEY_ARG_SCENE)) {
            switch (params.getInt(KEY_ARG_SCENE)) {
                case SCENE_FAVORITE:
                    req.scene = SendMessageToWX.Req.WXSceneFavorite;
                    break;
                case SCENE_TIMELINE:
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;
                    break;
                case SCENE_SESSION:
                    req.scene = SendMessageToWX.Req.WXSceneSession;
                    break;
                default:
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;
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
                    Log.e(TAG, "Failed to build sharing message.", e);

                    // clear callback context
                    currentCallbackContext = null;

                    // send json exception error
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
                }

                if (api.sendReq(req)) {
                    Log.i(TAG, "Message has been sent successfully.");
                } else {
                    Log.i(TAG, "Message has been sent unsuccessfully.");

                    // clear callback context
                    currentCallbackContext = null;

                    // send error
                    callbackContext.error(ERROR_SEND_REQUEST_FAILED);
                }
            }
        });

        // send no result
        sendNoResultPluginResult(callbackContext);

        return true;
    }

    protected boolean sendAuthRequest(CordovaArgs args, CallbackContext callbackContext) {
        final IWXAPI api = getWXAPI();

        final SendAuth.Req req = new SendAuth.Req();
        try {
            req.scope = args.getString(0);
            req.state = args.getString(1);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());

            req.scope = "snsapi_userinfo";
            req.state = "wechat";
        }

        if (api.sendReq(req)) {
            Log.i(TAG, "Auth request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Auth request has been sent unsuccessfully.");

            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    protected boolean sendPaymentRequest(CordovaArgs args, CallbackContext callbackContext) {

        final IWXAPI api = getWXAPI();

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        PayReq req = new PayReq();

        try {
            req.appId = getAppId();
            req.partnerId = params.getString("mch_id");
            req.prepayId = params.getString("prepay_id");
            req.nonceStr = params.getString("nonce");
            req.timeStamp = params.getString("timestamp");
            req.sign = params.getString("sign");
            req.packageValue = "Sign=WXPay";
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        if (api.sendReq(req)) {
            Log.i(TAG, "Payment request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Payment request has been sent unsuccessfully.");

            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    protected boolean isInstalled(CallbackContext callbackContext) {
        final IWXAPI api = getWXAPI();

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
            wxMediaMessage.description = message.getString(KEY_ARG_MESSAGE_DESCRIPTION);

            // thumbnail
            Bitmap thumbnail = getThumbnail(message, KEY_ARG_MESSAGE_THUMB);
            if (thumbnail != null) {
                wxMediaMessage.setThumbImage(thumbnail);
                thumbnail.recycle();
            }

            // check types
            int type = media.has(KEY_ARG_MESSAGE_MEDIA_TYPE) ? media
                    .getInt(KEY_ARG_MESSAGE_MEDIA_TYPE) : TYPE_WECHAT_SHARING_WEBPAGE;

            switch (type) {
                case TYPE_WECHAT_SHARING_APP:
                    WXAppExtendObject appObject = new WXAppExtendObject();
                    appObject.extInfo = media.getString(KEY_ARG_MESSAGE_MEDIA_EXTINFO);
                    appObject.filePath = media.getString(KEY_ARG_MESSAGE_MEDIA_URL);
                    wxMediaMessage.mediaObject = appObject;
                    break;

                case TYPE_WECHAT_SHARING_EMOTION:
                    WXEmojiObject emoObject = new WXEmojiObject();
                    emoObject.emojiPath = media.getString(KEY_ARG_MESSAGE_MEDIA_EMOTION);
                    wxMediaMessage.mediaObject = emoObject;
                    break;

                case TYPE_WECHAT_SHARING_FILE:
                    WXFileObject fileObject = new WXFileObject();
                    fileObject.filePath = media.getString(KEY_ARG_MESSAGE_MEDIA_FILE);
                    wxMediaMessage.mediaObject = fileObject;
                    break;

                case TYPE_WECHAT_SHARING_IMAGE:
                    Bitmap image = getBitmap(message.getJSONObject(KEY_ARG_MESSAGE_MEDIA), KEY_ARG_MESSAGE_MEDIA_IMAGE, 0);
                    mediaObject = new WXImageObject(image);
                    image.recycle();
                    break;

                case TYPE_WECHAT_SHARING_MUSIC:
                    WXMusicObject musicObject = new WXMusicObject();
                    musicObject.musicUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_MUSICURL);
                    musicObject.musicDataUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_MUSICDATAURL);
                    wxMediaMessage.mediaObject = musicObject;
                    break;

                case TYPE_WECHAT_SHARING_VIDEO:
                    WXVideoObject videoObject = new WXVideoObject();
                    videoObject.videoUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_VIDEOURL);
                    wxMediaMessage.mediaObject = videoObject;
                    break;

                case TYPE_WECHAT_SHARING_WEBPAGE:
                default:
                    mediaObject = new WXWebpageObject(media.getString(KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL));
            }
        }

        wxMediaMessage.mediaObject = mediaObject;

        return wxMediaMessage;
    }

    protected IWXAPI getWXAPI() {
        return wxAPI;
    }

    private String buildTransaction() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String buildTransaction(final String type) {
        return type + System.currentTimeMillis();
    }

    protected Bitmap getThumbnail(JSONObject message, String key) {
        return getBitmap(message, key, MAX_THUMBNAIL_SIZE);
    }

    protected Bitmap getBitmap(JSONObject message, String key, int maxSize) {
        Bitmap bmp = null;
        String url = null;

        try {
            if (!message.has(key)) {
                return null;
            }

            url = message.getString(key);

            InputStream inputStream = null;

            if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {

                File image = Util.downloadAndCacheFile(webView.getContext(), url);

                if (image == null) {
                    Log.d(TAG, String.format("Bitmap from %s is null.", url));
                    return null;
                }

                url = image.getAbsolutePath();
                inputStream = new FileInputStream(image);

                Log.d(TAG, String.format("Bitmap was downloaded and cached to %s.", url));

            } else if (url.startsWith(EXTERNAL_STORAGE_IMAGE_PREFIX)) { // external path

                url = Environment.getExternalStorageDirectory().getAbsolutePath() + url.substring(EXTERNAL_STORAGE_IMAGE_PREFIX.length());
                inputStream = new FileInputStream(url);

                Log.d(TAG, String.format("Bitmap is located on external storage at %s.", url));

            } else if (!url.startsWith("/")) { // relative path

                inputStream = cordova.getActivity().getApplicationContext().getAssets().open(url);

                Log.d(TAG, String.format("Bitmap is located in assets folder at %s.", url));

            } else {

                inputStream = new FileInputStream(url);

                Log.d(TAG, String.format("Bitmap is located at %s.", url));

            }

            // decode it
            // @TODO make sure the image is not too big, or it will cause out of memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            bmp = BitmapFactory.decodeStream(inputStream, null, options);

            // scale
            if (maxSize > 0 && (options.outWidth > maxSize || options.outHeight > maxSize)) {

                Log.d(TAG, String.format("Bitmap was decoded, dimension: %d x %d, max allowed size: %d.",
                        options.outWidth, options.outHeight, maxSize));

                int width = 0;
                int height = 0;

                if (options.outWidth > options.outHeight) {
                    width = maxSize;
                    height = width * options.outHeight / options.outWidth;
                } else {
                    height = maxSize;
                    width = height * options.outWidth / options.outHeight;
                }

                Bitmap scaled = Bitmap.createScaledBitmap(bmp, width, height, true);
                bmp.recycle();

                bmp = scaled;
            }

        } catch (JSONException e) {
            bmp = null;
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            bmp = null;
            e.printStackTrace();
        } catch (IOException e) {
            bmp = null;
            e.printStackTrace();
        }

        return bmp;
    }

    protected String getAppId() {
        if (this.appId == null) {
            this.appId = preferences.getString(WXAPPID_PROPERTY_KEY, "");
        }

        return this.appId;
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        // save current callback context
        currentCallbackContext = callbackContext;

        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }
}
