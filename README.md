# cordova-plugin-wechat

A cordova plugin, a JS version of Wechat SDK

# Feature

Share title, description, image, and link to wechat moment(朋友圈)

# Example

See [cordova-plugin-wechat-example](https://github.com/xu-li/cordova-plugin-wechat-example)

# Install(iOS)

1. ```cordova plugin add https://github.com/xu-li/cordova-plugin-wechat```, or using [plugman](https://npmjs.org/package/plugman), [phonegap](https://npmjs.org/package/phonegap), [ionic](http://ionicframework.com/)

2. Add ```<preference name="wechatappid" value="YOUR_WECHAT_APP_ID" />``` in your config.xml

3. ```cordova build ios```

4. Change the URL Type using XCode

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

# FAQ

Q: "Wechat not installed", even installed

A: Please make sure "wechatappid" is added in ```config.xml``` 

Q: After sharing in wechat, it will not get back to my app.

A: Please make sure the URL Type is correct(iOS)


# TODO

1. ~~Add android version~~

2. ~~Share to wechat session(聊天) and wechat favorite(收藏)~~

3. ~~Add other media types, including music etc.~~

4. Other APIs

5. Android Version update

# LICENSE

[MIT LICENSE](http://opensource.org/licenses/MIT)
