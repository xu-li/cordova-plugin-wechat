package xu.li.cordova.wechat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;

public class Wechat extends CordovaPlugin {

	public static final String WXAPPID_PROPERTY_KEY = "wechatappid";

	public static final String ERROR_WX_NOT_INSTALLED = "未安装微信";
	public static final String ERROR_ARGUMENTS = "参数错误";

	public static final String KEY_ARG_MESSAGE = "message";
	public static final String KEY_ARG_SCENE = "scene";
	public static final String KEY_ARG_MESSAGE_TITLE = "title";
	public static final String KEY_ARG_MESSAGE_DESCRIPTION = "description";
	public static final String KEY_ARG_MESSAGE_THUMB = "thumb";
	public static final String KEY_ARG_MESSAGE_MEDIA = "media";
	public static final String KEY_ARG_MESSAGE_MEDIA_TYPE = "type";
	public static final String KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL = "webpageUrl";
	public static final String KEY_ARG_MESSAGE_MEDIA_TEXT = "text";

	public static final int TYPE_WX_SHARING_APP = 1;
	public static final int TYPE_WX_SHARING_EMOTION = 2;
	public static final int TYPE_WX_SHARING_FILE = 3;
	public static final int TYPE_WX_SHARING_IMAGE = 4;
	public static final int TYPE_WX_SHARING_MUSIC = 5;
	public static final int TYPE_WX_SHARING_VIDEO = 6;
	public static final int TYPE_WX_SHARING_WEBPAGE = 7;
	public static final int TYPE_WX_SHARING_TEXT = 8;
	

	protected IWXAPI wxAPI;
	protected CallbackContext currentCallbackContext;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		
		if (action.equals("share")) {
			// sharing
			return share(args, callbackContext);
		}

		return super.execute(action, args, callbackContext);
	}

	protected IWXAPI getWXAPI() {
		if (wxAPI == null) {
			String appId = webView.getProperty(WXAPPID_PROPERTY_KEY, "");
			wxAPI = WXAPIFactory.createWXAPI(webView.getContext(), appId, true);
		}

		return wxAPI;
	}

	protected boolean share(JSONArray args, CallbackContext callbackContext)
			throws JSONException {
		final IWXAPI api = getWXAPI();

		api.registerApp(webView.getProperty(WXAPPID_PROPERTY_KEY, ""));

		// check if installed
		if (!api.isWXAppInstalled()) {
			callbackContext.error(ERROR_WX_NOT_INSTALLED);
			return false;
		}

		// check if # of arguments is correct
		if (args.length() != 1) {
			callbackContext.error(ERROR_ARGUMENTS);
		}

		final JSONObject params = args.getJSONObject(0);
		final SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());

		if (params.has(KEY_ARG_SCENE)) {
			req.scene = params.getInt(KEY_ARG_SCENE);
		} else {
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		}

		// run in background
		cordova.getThreadPool().execute(new Runnable() {

			@Override
			public void run() {
				try {
					req.message = buildSharingMessage(params
							.getJSONObject(KEY_ARG_MESSAGE));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				api.sendReq(req);
			}

		});

		// save the current callback context
		currentCallbackContext = callbackContext;
		return true;
	}

	protected WXMediaMessage buildSharingMessage(JSONObject message)
			throws JSONException {
		URL thumbnailUrl = null;
		Bitmap thumbnail = null;

		try {
			thumbnailUrl = new URL(message.getString(KEY_ARG_MESSAGE_THUMB));
			thumbnail = BitmapFactory.decodeStream(thumbnailUrl
					.openConnection().getInputStream());

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		WXMediaMessage wxMediaMessage = new WXMediaMessage();
		wxMediaMessage.title = message.getString(KEY_ARG_MESSAGE_TITLE);
		wxMediaMessage.description = message
				.getString(KEY_ARG_MESSAGE_DESCRIPTION);
		if (thumbnail != null) {
			wxMediaMessage.setThumbImage(thumbnail);
		}

		// media parameters
		WXMediaMessage.IMediaObject mediaObject = null;
		JSONObject media = message.getJSONObject(KEY_ARG_MESSAGE_MEDIA);

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
			break;

		case TYPE_WX_SHARING_MUSIC:
			break;

		case TYPE_WX_SHARING_VIDEO:
			break;
			
		case TYPE_WX_SHARING_TEXT:
			mediaObject = new WXTextObject();
			((WXTextObject)mediaObject).text = media.getString(KEY_ARG_MESSAGE_MEDIA_TEXT);
			break;

		case TYPE_WX_SHARING_WEBPAGE:
		default:
			mediaObject = new WXWebpageObject();
			((WXWebpageObject) mediaObject).webpageUrl = media
					.getString(KEY_ARG_MESSAGE_MEDIA_WEBPAGEURL);
		}

		wxMediaMessage.mediaObject = mediaObject;

		return wxMediaMessage;
	}
}

