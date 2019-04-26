package xu.li.cordova.wechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;

import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.opensdk.modelmsg.WXEmojiObject;
import com.tencent.mm.opensdk.modelmsg.WXFileObject;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import org.apache.cordova.CordovaPreferences;

public class Wechat extends CordovaPlugin {

    public static final String TAG = "Cordova.Plugin.Wechat";

    public static final String PREFS_NAME = "Cordova.Plugin.Wechat";
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
    public static final int REQUEST_CODE_ENABLE_PERMISSION = 55433;
    public static final String ANDROID_WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

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
    public static final String KEY_ARG_MESSAGE_MEDIA_USERNAME = "userName";
    public static final String KEY_ARG_MESSAGE_MEDIA_MINIPROGRAMTYPE = "miniprogramType";
    public static final String KEY_ARG_MESSAGE_MEDIA_MINIPROGRAM = "miniProgram";
    public static final String KEY_ARG_MESSAGE_MEDIA_PATH = "path";
    public static final String KEY_ARG_MESSAGE_MEDIA_WITHSHARETICKET = "withShareTicket";
    public static final String KEY_ARG_MESSAGE_MEDIA_HDIMAGEDATA = "hdImageData";

    public static final int TYPE_WECHAT_SHARING_APP = 1;
    public static final int TYPE_WECHAT_SHARING_EMOTION = 2;
    public static final int TYPE_WECHAT_SHARING_FILE = 3;
    public static final int TYPE_WECHAT_SHARING_IMAGE = 4;
    public static final int TYPE_WECHAT_SHARING_MUSIC = 5;
    public static final int TYPE_WECHAT_SHARING_VIDEO = 6;
    public static final int TYPE_WECHAT_SHARING_WEBPAGE = 7;
    public static final int TYPE_WECHAT_SHARING_MINI = 8;

    public static final int SCENE_SESSION = 0;
    public static final int SCENE_TIMELINE = 1;
    public static final int SCENE_FAVORITE = 2;

    public static final int MAX_THUMBNAIL_SIZE = 320;

    protected static CallbackContext currentCallbackContext;
    protected static IWXAPI wxAPI;
    protected static String appId;
    protected static CordovaPreferences wx_preferences;

    @Override
    protected void pluginInitialize() {

        super.pluginInitialize();

        String id = getAppId(preferences);

        // save app id
        saveAppId(cordova.getActivity(), id);

        // init api
        initWXAPI();

        Log.d(TAG, "plugin initialized.");
    }

    protected void initWXAPI() {
        IWXAPI api = getWxAPI(cordova.getActivity());
        if(wx_preferences == null) {
            wx_preferences = preferences;
        }
        if (api != null) {
            api.registerApp(getAppId(preferences));
        }
    }

    /**
     * Get weixin api
     * @param ctx
     * @return
     */
    public static IWXAPI getWxAPI(Context ctx) {
        if (wxAPI == null) {
            String appId = getSavedAppId(ctx);

            if (!appId.isEmpty()) {
                wxAPI = WXAPIFactory.createWXAPI(ctx, appId, true);
            }
        }

        return wxAPI;
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
        }else if (action.equals("chooseInvoiceFromWX")){
            return chooseInvoiceFromWX(args, callbackContext);
        }else if(action.equals("openMiniProgram")){
            return openMiniProgram(args,callbackContext);
        }

        return false;
    }

    protected boolean share(CordovaArgs args, final CallbackContext callbackContext)
            throws JSONException {
        final IWXAPI api = getWxAPI(cordova.getActivity());

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

        if (params.has(KEY_ARG_MESSAGE)) {
            //小程序卡片单独构建
            JSONObject message = params.getJSONObject(KEY_ARG_MESSAGE);
            if (message.has(KEY_ARG_MESSAGE_MEDIA)) {
                JSONObject media = message.getJSONObject(KEY_ARG_MESSAGE_MEDIA);
                int type = media.has(KEY_ARG_MESSAGE_MEDIA_TYPE) ? media
                        .getInt(KEY_ARG_MESSAGE_MEDIA_TYPE) : TYPE_WECHAT_SHARING_MINI;
                if (type == TYPE_WECHAT_SHARING_MINI) {
                    req.transaction = buildTransaction(KEY_ARG_MESSAGE_MEDIA_MINIPROGRAM);
                }
            }
        }

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
        final IWXAPI api = getWxAPI(cordova.getActivity());

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
            // final String appid = params.getString("appid");
            // final String savedAppid = getSavedAppId(cordova.getActivity());
            // if (!savedAppid.equals(appid)) {
            //     this.saveAppId(cordova.getActivity(), appid);
            // }
            req.appId = getAppId(preferences);
            req.partnerId = params.has("mch_id") ? params.getString("mch_id") : params.getString("partnerid");
            req.prepayId = params.has("prepay_id") ? params.getString("prepay_id") : params.getString("prepayid");
            req.nonceStr = params.has("nonce") ? params.getString("nonce") : params.getString("noncestr");
            req.timeStamp = params.getString("timestamp");
            req.sign = params.getString("sign");
            req.packageValue = "Sign=WXPay";
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final IWXAPI api = getWxAPI(cordova.getActivity());

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

    protected boolean chooseInvoiceFromWX(CordovaArgs args, CallbackContext callbackContext) {

        final IWXAPI api = getWxAPI(cordova.getActivity());

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        ChooseCardFromWXCardPackage.Req req = new ChooseCardFromWXCardPackage.Req();

        try {
            req.appId = getAppId(preferences);
            req.cardType = "INVOICE";
            req.signType = params.getString("signType");
            req.cardSign = params.getString("cardSign");
            req.nonceStr = params.getString("nonceStr");
            req.timeStamp = params.getString("timeStamp");
            req.canMultiSelect = "1";
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        if (api.sendReq(req)) {
            Log.i(TAG, "Invoice request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Invoice request has been sent unsuccessfully.");

            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    protected boolean isInstalled(CallbackContext callbackContext) {
        final IWXAPI api = getWxAPI(cordova.getActivity());

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
                    mediaObject = appObject;
                    break;

                case TYPE_WECHAT_SHARING_EMOTION:
                    WXEmojiObject emoObject = new WXEmojiObject();
                    InputStream emoji = getFileInputStream(media.getString(KEY_ARG_MESSAGE_MEDIA_EMOTION));
                    if (emoji != null) {
                        try {
                            emoObject.emojiData = Util.readBytes(emoji);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mediaObject = emoObject;
                    break;

                case TYPE_WECHAT_SHARING_FILE:
                    WXFileObject fileObject = new WXFileObject();
                    InputStream file = getFileInputStream(media.getString(KEY_ARG_MESSAGE_MEDIA_FILE));
                    if (file != null) {
                        try {
                            fileObject.fileData = Util.readBytes(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mediaObject = fileObject;
                    break;

                case TYPE_WECHAT_SHARING_IMAGE:
                    Bitmap image = getBitmap(message.getJSONObject(KEY_ARG_MESSAGE_MEDIA), KEY_ARG_MESSAGE_MEDIA_IMAGE, 0);
                    // give some tips to user           
                    if(image != null) {
                        mediaObject = new WXImageObject(image);
                        image.recycle();
                    }
                    break;

                case TYPE_WECHAT_SHARING_MUSIC:
                    WXMusicObject musicObject = new WXMusicObject();
                    musicObject.musicUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_MUSICURL);
                    musicObject.musicDataUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_MUSICDATAURL);
                    mediaObject = musicObject;
                    break;

                case TYPE_WECHAT_SHARING_VIDEO:
                    WXVideoObject videoObject = new WXVideoObject();
                    videoObject.videoUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_VIDEOURL);
                    mediaObject = videoObject;
                    break;

                case TYPE_WECHAT_SHARING_MINI:
                    WXMiniProgramObject miniProgramObj = new WXMiniProgramObject();
                    try {
                        miniProgramObj.webpageUrl = media.getString(KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL); // 兼容低版本的网页链接
                        miniProgramObj.miniprogramType = media.getInt(KEY_ARG_MESSAGE_MEDIA_MINIPROGRAMTYPE);// 正式版:0，测试版:1，体验版:2
                        miniProgramObj.userName = media.getString(KEY_ARG_MESSAGE_MEDIA_USERNAME);     // 小程序原始id
                        miniProgramObj.path = media.getString(KEY_ARG_MESSAGE_MEDIA_PATH);            //小程序页面路径
                        miniProgramObj.withShareTicket = media.getBoolean(KEY_ARG_MESSAGE_MEDIA_WITHSHARETICKET); // 是否使用带shareTicket的分享
                        wxMediaMessage = new WXMediaMessage(miniProgramObj);
                        wxMediaMessage.title = message.getString(KEY_ARG_MESSAGE_TITLE);                    // 小程序消息title
                        wxMediaMessage.description = message.getString(KEY_ARG_MESSAGE_DESCRIPTION);               // 小程序消息desc
                        wxMediaMessage.thumbData = Util.readBytes(getFileInputStream(media.getString(KEY_ARG_MESSAGE_MEDIA_HDIMAGEDATA))); // 小程序消息封面图片，小于128k
                        return wxMediaMessage;
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                    }
                    break;

                case TYPE_WECHAT_SHARING_WEBPAGE:
                default:
                    mediaObject = new WXWebpageObject(media.getString(KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL));
            }
        }

        wxMediaMessage.mediaObject = mediaObject;

        return wxMediaMessage;
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

            // get input stream
            InputStream inputStream = getFileInputStream(url);
            if (inputStream == null) {
                return null;
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

                int length = scaled.getRowBytes() * scaled.getHeight();

                if(length > (maxSize/10)*1024) {
                    scaled = compressImage(scaled,(maxSize/10));
                }

                bmp = scaled;
            }

            inputStream.close();

        } catch (JSONException e) {
            bmp = null;
            e.printStackTrace();
        } catch (IOException e) {
            bmp = null;
            e.printStackTrace();
        }

        return bmp;
    }


    /**
     * compress bitmap by quility
     */
    protected  Bitmap compressImage(Bitmap image,Integer maxSize) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;

        while (baos.toByteArray().length / 1024 > maxSize) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * Get input stream from a url
     */
    protected InputStream getFileInputStream(String url) {
        InputStream inputStream = null;
        try {

            if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    if (!cordova.hasPermission(ANDROID_WRITE_EXTERNAL_STORAGE)) {
                        cordova.requestPermission(this, REQUEST_CODE_ENABLE_PERMISSION, ANDROID_WRITE_EXTERNAL_STORAGE);
                    }
                }

                File file = Util.downloadAndCacheFile(webView.getContext(), url);

                if (file == null) {
                    Log.d(TAG, String.format("File could not be downloaded from %s.", url));
                    return null;
                }

                // url = file.getAbsolutePath();
                inputStream = new FileInputStream(file);

                Log.d(TAG, String.format("File was downloaded and cached to %s.", file.getAbsolutePath()));

            } else if (url.startsWith("data:image")) {  // base64 image

                String imageDataBytes = url.substring(url.indexOf(",") + 1);
                byte imageBytes[] = Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT);
                inputStream = new ByteArrayInputStream(imageBytes);

                Log.d(TAG, "Image is in base64 format.");

            } else if (url.startsWith(EXTERNAL_STORAGE_IMAGE_PREFIX)) { // external path

                url = Environment.getExternalStorageDirectory().getAbsolutePath() + url.substring(EXTERNAL_STORAGE_IMAGE_PREFIX.length());
                inputStream = new FileInputStream(url);

                Log.d(TAG, String.format("File is located on external storage at %s.", url));

            } else if (!url.startsWith("/")) { // relative path

                inputStream = cordova.getActivity().getApplicationContext().getAssets().open(url);

                Log.d(TAG, String.format("File is located in assets folder at %s.", url));

            } else {

                inputStream = new FileInputStream(url);

                Log.d(TAG, String.format("File is located at %s.", url));

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputStream;
    }

    public static String getAppId(CordovaPreferences f_preferences) {
        if (appId == null) {
            if(f_preferences != null) {
                appId = f_preferences.getString(WXAPPID_PROPERTY_KEY, "");
            }else if(wx_preferences != null){
                appId = wx_preferences.getString(WXAPPID_PROPERTY_KEY, "");
            }
        }

        return appId;
    }

    /**
     * Get saved app id
     * @param ctx
     * @return
     */
    public static String getSavedAppId(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(WXAPPID_PROPERTY_KEY, "");
    }

    /**
     * Save app id into SharedPreferences
     * @param ctx
     * @param id
     */
    public static void saveAppId(Context ctx, String id) {
        if (id!=null && id.isEmpty()) {
            return ;
        }

        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(WXAPPID_PROPERTY_KEY, id);
        editor.commit();
    }

    public static CallbackContext getCurrentCallbackContext() {
        return currentCallbackContext;
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        // save current callback context
        currentCallbackContext = callbackContext;

        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    protected boolean openMiniProgram(CordovaArgs args, CallbackContext callbackContext){
        currentCallbackContext = callbackContext;
        String appId = getAppId(preferences);; // 填应用AppId
        IWXAPI api = WXAPIFactory.createWXAPI(cordova.getActivity(), appId);

        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        try {
            req.userName = params.getString(KEY_ARG_MESSAGE_MEDIA_USERNAME); // 填小程序原始id
            req.path = params.getString(KEY_ARG_MESSAGE_MEDIA_PATH);                  //拉起小程序页面的可带参路径，不填默认拉起小程序首页
            req.miniprogramType = params.getInt(KEY_ARG_MESSAGE_MEDIA_MINIPROGRAMTYPE);// 可选打开 开发版，体验版和正式版
            api.sendReq(req);
        }catch (Exception e){
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            Log.e(TAG,e.getMessage());
        }
        return true;
    }

}
