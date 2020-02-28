![Active](https://www.repostatus.org/badges/latest/active.svg)
![Downloads](https://img.shields.io/npm/dt/cordova-plugin-wechat.svg)
![version](https://img.shields.io/npm/v/cordova-plugin-wechat/latest.svg)


# 重要说明

Due to the security upgrade of iOS 13 system version, the official WeChat SDK has been adapted from 1.8.6 with supports * Universal Links * mode jumps, and the validity check when sharing.


From version 3.0.0 of our plugin, we changed to the latest WeChat SDK. Before using it, you need to configure the *Universal Links* service, and pay attention to passing  the `universallink` variable when installing the plugin, otherwise it will not work properly.

If you don't want to use the new version features, you can fall back to version 3.0.0 prior

# cordova-plugin-wechat

A cordova plugin, a JS version of Wechat SDK


# Feature

* check wechat client is installed;
* Share text, image, link,music,video,miniprogram to wechat timeline,session or favorite;
* open wechat auth;
* send wechat payment request;
* open wechat miniprogram;

# Documentation

[文档](https://jasonz1987.github.io/cordova-wechat-docs/) · [教程](https://www.jason-z.com/course/3) · [FAQ](https://jasonz1987.github.io/cordova-wechat-docs/docs/faq) · [DEMO](https://jasonz1987.github.io/cordova-wechat-docs/docs/demo)

# Install

```shell
cordova plugin add cordova-plugin-wechat  --variable wechatappid=YOUR_WECHAT_APPID --variable universallink=YOUR_UNIVERSAL_LINK
```

```shell
cordova build ios
cordova build android
```

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
var scope = "snsapi_userinfo",
    state = "_" + (+new Date());
Wechat.auth(scope, state, function (response) {
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
            type: Wechat.Type.WEBPAGE,
            webpageUrl: "http://www.jason-z.com"
        }
    },
    scene: Wechat.Scene.TIMELINE   // share to Timeline
}, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```

### Share mini program
```Javascript
Wechat.share({
    message: {
        ...
        media: {
            type: Wechat.Type.MINI,
            webpageUrl: "https://www.jason-z.com", // 兼容低版本的网页链接
            userName: "wxxxxxxxx", // 小程序原始id
            path: "user/info", // 小程序的页面路径
            hdImageData: "http://wwww.xxx.com/xx.jpg", // 程序新版本的预览图二进制数据 不超过128kb 支持 地址 base64 temp
            withShareTicket: true, // 是否使用带shareTicket的分享
            miniprogramType: Wechat.Mini.RELEASE 
        }
    },
    scene: Wechat.Scene.SESSION   // 小程序仅支持聊天界面
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
    partnerid: '10000100', // merchant id
    prepayid: 'wx201411101639507cbf6ffd8b0779950874', // prepay id
    noncestr: '1add1a30ac87aa2db72f57a2375d8fec', // nonce
    timestamp: '1439531364', // timestamp
    sign: '0CB01533B8C1EF103065174F50BCA001', // signed string
};

Wechat.sendPaymentRequest(params, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```

## Choose invoices from card list
```Javascript
//offical doc https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1496561749_f7T6D
var params = {
    timeStamp: '1510198391', // timeStamp
    signType: 'SHA1', // sign type
    cardSign: 'dff450eeeed08120159d285e79737173aec3df94', // cardSign
    nonceStr: '5598190f-5fb3-4bff-8314-fd189ab4e4b8', // nonce
};

Wechat.chooseInvoiceFromWX(params,function(data){
    console.log(data);
},function(){
    alert('error');
})
```

## open wechat mini program 
```Javascript
//offical doc https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=21526646437Y6nEC&token=&lang=zh_CN
var params = {
    userName: 'gh_d43f693ca31f', // userName
    path: 'pages/index/index?name1=key1&name2=key2', // open mini program page
    miniprogramType: Wechat.Mini.RELEASE // Developer version, trial version, and official version are available for selection
};

Wechat.openMiniProgram(params,function(data){
    console.log(data); // data:{extMsg:""}  extMsg: Corresponds to the app-parameter attribute in the Mini Program component <button open-type="launchApp">
},function(){
    alert('error');
})
```

more usage  please see [https://jasonz1987.github.io/cordova-wechat-docs/docs/usages](https://jasonz1987.github.io/cordova-wechat-docs/docs/usages)

# Join us

QQ群：190808518 
[![cordova-wechat官方交流群](https://pub.idqqimg.com/wpa/images/group.png)](http://shang.qq.com/wpa/qunwpa?idkey=8279476de172cacb72a51a5630744316c0069620ad8b33be3abee243af2cc001)

# Donate

we need your support to improve open source software ,if we induce your develop time ,welcome to donate us.

[![paypal](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.me/jasonz1987/6.66)

![donate.png](donate.png)

# LICENSE

[MIT LICENSE](http://opensource.org/licenses/MIT)
