var exec = require('cordova/exec');

module.exports = {
    Scene: {
        SESSION:  0, // 聊天界面
        TIMELINE: 1, // 朋友圈
        FAVORITE: 2  // 收藏
    },

    Type: {
        APP:     1,
        EMOTION: 2,
        FILE:    3,
        IMAGE:   4,
        MUSIC:   5,
        VIDEO:   6,
        WEBPAGE: 7,
        MINI:    8
    },

    Mini: {
        RELEASE: 0, // 正式版
        TEST:    1, // 测试版
        PREVIEW: 2  // 体验版
    },

    isInstalled: function (onSuccess, onError) {
        exec(onSuccess, onError, "Wechat", "isWXAppInstalled", []);
    },

    /**
     * Share a message to wechat app
     *
     * @example
     * <code>
     * Wechat.share({
     *     message: {
     *        title: "Message Title",
     *        description: "Message Description(optional)",
     *        mediaTagName: "Media Tag Name(optional)",
     *        thumb: "http://YOUR_THUMBNAIL_IMAGE",
     *        media: {
     *            type: Wechat.Type.WEBPAGE,   // webpage
     *            webpageUrl: "https://github.com/xu-li/cordova-plugin-wechat"    // webpage
     *        }
     *    },
     *    scene: Wechat.Scene.TIMELINE   // share to Timeline
     * }, function () {
     *     alert("Success");
     * }, function (reason) {
     *     alert("Failed: " + reason);
     * });
     * </code>
     */
    share: function (message, onSuccess, onError) {
        exec(onSuccess, onError, "Wechat", "share", [message]);
    },

    /**
     * Sending an auth request to Wechat
     *
     * @example
     * <code>
     * Wechat.auth(function (response) { alert(response.code); });
     * </code>
     */
    auth: function (scope, state, onSuccess, onError) {
        if (typeof scope == "function") {
            // Wechat.auth(function () { alert("Success"); });
            // Wechat.auth(function () { alert("Success"); }, function (error) { alert(error); });
            return exec(scope, state, "Wechat", "sendAuthRequest");
        }

        if (typeof state == "function") {
            // Wechat.auth("snsapi_userinfo", function () { alert("Success"); });
            // Wechat.auth("snsapi_userinfo", function () { alert("Success"); }, function (error) { alert(error); });
            return exec(state, onSuccess, "Wechat", "sendAuthRequest", [scope]);
        }

        return exec(onSuccess, onError, "Wechat", "sendAuthRequest", [scope, state]);
    },

    /**
     * Send a payment request
     *
     * @link https://pay.weixin.qq.com/wiki/doc/api/app.php?chapter=9_1
     * @example
     * <code>
     * var params = {
     *     mch_id: '10000100', // merchant id
     *     prepay_id: 'wx201411101639507cbf6ffd8b0779950874', // prepay id returned from server
     *     nonce: '1add1a30ac87aa2db72f57a2375d8fec', // nonce string returned from server
     *     timestamp: '1439531364', // timestamp
     *     sign: '0CB01533B8C1EF103065174F50BCA001', // signed string
     * };
     * Wechat.sendPaymentRequest(params, function () {
     *     alert("Success");
     * }, function (reason) {
     *     alert("Failed: " + reason);
     * });
     * </code>
     */
    sendPaymentRequest: function (params, onSuccess, onError) {
        exec(onSuccess, onError, "Wechat", "sendPaymentRequest", [params]);
    },

    /**
     * jumpToBizProfile （跳转到某个微信公众号）2016-11-11 测试是失效的，囧
     *
     * @link https://segmentfault.com/a/1190000007204624
     * @link https://segmentfault.com/q/1010000003907796
     * @example
     * <code>
     * var params = {
     *     info: 'gh_xxxxxxx', // 公众帐号原始ID
     *     type:  'Normal' // 普通号
     * }
     * or
     * var params = {
     *     info: 'extMsg', // 相关的硬件二维码串
     *     type:  'Device' // 硬件号
     * };
     * Wechat.jumpToBizProfile(params, function () {
     *     alert("Success");
     * }, function (reason) {
     *     alert("Failed: " + reason);
     * });
     * </code>
     */

    jumpToBizProfile: function (params, onSuccess, onError) {
        exec(onSuccess, onError, "Wechat", "jumpToBizProfile", [params]);
    },

    /**
     * jumpToWechat （因为jumpToBizProfile失效了，暂时新增了一个临时的api)
     *
     * @link https://segmentfault.com/a/1190000007204624
     * @example
     * <code>
     * var url = "wechat://" 现阶段貌似只支持这一个协议了
     * Wechat.jumpToWechat(url, function () {
     *     alert("Success");
     * }, function (reason) {
     *     alert("Failed: " + reason);
     * });
     * </code>
     */
    jumpToWechat: function (url, onSuccess, onError) {
        exec(onSuccess, onError, "Wechat", "jumpToWechat", [url]);
    },

    /**
     * chooseInvoiceFromWX exq:choose invoices from Wechat card list
     *
     * @example
     * <code>
     * params: signType, cardSign, nonceStr, timeStamp  all required
     * Wechat.chooseInvoiceFromWX(params, function () {
     *     alert("Success");
     * }, function (reason) {
     *     alert("Failed: " + reason);
     * });
     * </code>
     */
    chooseInvoiceFromWX: function (params, onSuccess, onError) {
        exec(onSuccess, onError, "Wechat", "chooseInvoiceFromWX", [params]);
    },

    /**
     * openMiniProgram exq:app opens wechat mini program
     *
     * @example
     * <code>
     * params: userName, path, miniprogramType  all required
     * Wechat.openMiniProgram(params, function (data) {
     *     alert(data.extMsg);
     * }, function (reason) {
     *     alert("Failed: " + reason);
     * });
     * </code>
     */
    openMiniProgram: function (params, onSuccess, onError) {
        exec(onSuccess, onError, "Wechat", "openMiniProgram", [params]);
    }

};
