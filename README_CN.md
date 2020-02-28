![Active](https://www.repostatus.org/badges/latest/active.svg)
![Downloads](https://img.shields.io/npm/dt/cordova-plugin-wechat.svg)
![version](https://img.shields.io/npm/v/cordova-plugin-wechat/latest.svg)

# 重要说明

由于苹果iOS 13系统版本安全升级，微信官方SDK在1.8.6版本进行了适配并且支持*Universal Links*方式跳转，以及分享时的合法性校验。

插件3.0.0版本接入了最新的微信SDK，在使用之前你需要配置Universal Links服务，并且在安装插件的时候注意传入`universallink`变量，否则将无法正常使用。

如果你不想使用新版本功能，可以回退3.0.0版本之前。

# 关于

微信sdk的cordova插件

# 功能

微信登录，微信app支付，微信分享，微信投票

# demo

[ionic3 demo](https://github.com/jasonz1987/ionic3-wechat-sdk-demo)

[ionic1/2 demo](https://github.com/xu-li/cordova-plugin-wechat-example)

# 安装

1. ```cordova plugin add cordova-plugin-wechat  --variable wechatappid=YOUR_WECHAT_APPID```, or using [plugman](https://npmjs.org/package/plugman), [phonegap](https://npmjs.org/package/phonegap), [ionic](http://ionicframework.com/)

2. ```cordova build ios``` or ```cordova build android```

3. (iOS only) if your cordova version <5.1.1,check the URL Type using XCode

# 用法

## 检查微信是否安装
```Javascript
Wechat.isInstalled(function (installed) {
    alert("Wechat installed: " + (installed ? "Yes" : "No"));
}, function (reason) {
    alert("Failed: " + reason);
});
```

## 微信认证
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

## 分享文本
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

## 分享媒体（例如链接，照片，音乐，视频等）
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

### 分享网页
```Javascript
Wechat.share({
    message: {
        ...
        media: {
            type: Wechat.Type.WEBPAGE,
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

### 分享到小程序
```Javascript
Wechat.share({
    message: {
        ...
        media: {
            type: Wechat.Type.MINI,
            webpageUrl: "http://www.jason-z.com", // 兼容低版本的网页链接
            userName: "wxxxxxxxx", // 小程序原始id
            path: "user/info", // 小程序的页面路径
            hdImageData: "http://wwww.xxx.com/xx.jpg", // 程序新版本的预览图二进制数据 不超过128kb
            withShareTicket: true, // 是否使用带shareTicket的分享
            miniprogramType: Wechat.Mini.RELEASE //正式版:0，测试版:1，体验版:2
        }
    },
    scene: Wechat.Scene.TIMELINE   // share to Timeline
}, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```

## 发送支付请求
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

Wechat.chooseInvoiceFromWX(data,function(data){
    console.log(data);
},function(){
    alert('error');
})
```

## 打开微信微信小程序
```Javascript
//offical doc https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=21526646437Y6nEC&token=&lang=zh_CN
var params = {
    userName: 'gh_d43f693ca31f', // 小程序userName
    path: 'pages/index/index?name1=key1&name2=key2', // 打开小程序路径
    miniprogramType: Wechat.Mini.RELEASE //正式版:0，测试版:1，体验版:2
};

Wechat.openMiniProgram(params,function(data){
    console.log(data); // data:{extMsg:""}  extMsg: 对应小程序组件 <button open-type="launchApp"> 中的 app-parameter 属性
},function(){
    alert('error');
})
```

# FAQ

See [FAQ](https://github.com/xu-li/cordova-plugin-wechat/wiki/FAQ).

QQ群：190808518  [![cordova-wechat官方交流群](https://pub.idqqimg.com/wpa/images/group.png)](http://shang.qq.com/wpa/qunwpa?idkey=8279476de172cacb72a51a5630744316c0069620ad8b33be3abee243af2cc001)


# TODO

1. 增加参数检查

# 捐赠

开源软件的发展离不开大家的推动和支持，如果我们的插件帮助到了你，欢迎捐赠（注：微信插件）。

![donate.png](donate.png)


# LICENSE

[MIT LICENSE](http://opensource.org/licenses/MIT)
