# cordova-plugin-wechat

A cordova plugin, a JS version of Wechat SDK

# Feature

Share title, description, image, and link to wechat moment(朋友圈)

# Example

See [cordova-plugin-wechat-example](https://github.com/xu-li/cordova-plugin-wechat-example)

# Install

1. ```cordova plugin add https://github.com/xu-li/cordova-plugin-wechat  --variable wechatappid=YOUR_WECHAT_APPID```, or using [plugman](https://npmjs.org/package/plugman), [phonegap](https://npmjs.org/package/phonegap), [ionic](http://ionicframework.com/)

2. ```cordova build ios``` or ```cordova build android```

3. (iOS only) if your cordova version <5.1.1,check the URL Type using XCode

# Usage

## Check if wechat is installed
```Javascript
Wechat.isInstalled(function (installed) {
    alert("Wechat installed: " + (installed ? "Yes" : "No"));
}, function (reason) {
    alert("Failed: " + reason);
});
```

## Authenticate using Wechat
```Javascript
var scope = "snsapi_userinfo";
Wechat.auth(scope, function (response) {
    // you may use response.code to get the access token.
    alert(JSON.stringify(response));
}, function (reason) {
    alert("Failed: " + reason);
});
```

## Share text
```Javascript
Wechat.share({
    text: "This is just a plain string",
    scene: Wechat.Scene.TIMELINE   // share to Timeline
}, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```

## Share media(e.g. link, photo, music, video etc)
```Javascript
Wechat.share({
    message: {
        title: "Hi, there",
        description: "This is description.",
        thumb: "www/img/thumbnail.png",
        mediaTagName: "TEST-TAG-001",
        messageExt: "这是第三方带的测试字段",
        messageAction: "<action>dotalist</action>",
        media: "YOUR_MEDIA_OBJECT_HERE"
    },
    scene: Wechat.Scene.TIMELINE   // share to Timeline
}, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```

### Share link
```Javascript
Wechat.share({
    message: {
        ...
        media: {
            type: Wechat.Type.LINK,
            webpageUrl: "http://tech.qq.com/zt2012/tmtdecode/252.htm"
        }
    },
    scene: Wechat.Scene.TIMELINE   // share to Timeline
}, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```

## Send payment request
```Javascript
// See https://github.com/xu-li/cordova-plugin-wechat-example/blob/master/server/payment_demo.php for php demo
var params = {
    mch_id: '10000100', // merchant id
    prepay_id: 'wx201411101639507cbf6ffd8b0779950874', // prepay id
    nonce: '1add1a30ac87aa2db72f57a2375d8fec', // nonce
    timestamp: '1439531364', // timestamp
    sign: '0CB01533B8C1EF103065174F50BCA001', // signed string
};

Wechat.sendPaymentRequest(params, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```

# FAQ

See [FAQ](https://github.com/xu-li/cordova-plugin-wechat/wiki/FAQ).

# TODO

1. ~~Add android version~~

2. ~~Share to wechat session(聊天) and wechat favorite(收藏)~~

3. ~~Add other media types, including music etc.~~

4. Other APIs

5. ~~Android Version update~~

# LICENSE

[MIT LICENSE](http://opensource.org/licenses/MIT)
