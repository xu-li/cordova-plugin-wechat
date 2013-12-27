cordova-plugin-wechat
===============

A cordova plugin, a JS version of Wechat SDK

Feature
===============

Share title, description, image, and link to wechat moment(朋友圈)

Example
===============

See [cordova-plugin-wechat-example](https://github.com/xu-li/cordova-plugin-wechat-example)

Install(iOS)
===============

1. Add [wechat lib](http://open.weixin.qq.com/document/gettingstart/ios/) to your project. Don't forget to add the "URL Type".

2. ```cordova plugin add https://github.com/xu-li/cordova-plugin-wechat```, or using [plugman](https://npmjs.org/package/plugman), [phonegap](https://npmjs.org/package/phonegap)

3. ```cordova build ios``` (it will fail if you haven't include the wechat lib yet.)

4. Open ```config.xml``` in xcode at the root.

5. Add ```<preference name="wechatappid" value="YOUR_WECHAT_APP_ID" />```

Usage
===============

```Javascript
Wechat.share({
    message: {
       title: "Message Title",
       description: "Message Description(optional)",
       mediaTagName: "Media Tag Name(optional)",
       thumb: "http://YOUR_THUMBNAIL_IMAGE",
       media: {
           type: Wechat.Type.WEBPAGE,   // webpage
           webpageUrl: "https://github.com/xu-li/cordova-plugin-wechat"    // webpage
       }
   },
   scene: Wechat.Scene.TIMELINE   // share to Timeline
}, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```


FAQ
===============

Q: "Wechat not installed", even installed

A: Please make sure "wechatappid" is added in ```config.xml``` 


TODO
===============

1. ~~Add android version~~

2. ~~Share to wechat session(聊天) and wechat favorite(收藏)~~

3. Add other media types, including music etc.

LICENSE
===============

[MIT LICENSE](http://opensource.org/licenses/MIT)
