cordova-plugin-weixin
===============

A cordova plugin for weixin

Feature
===============

Share title, description, image, and link to weixin moment(朋友圈)

Example
===============

See [cordova-plugin-weixin-example](https://github.com/xu-li/cordova-plugin-weixin-example)

Install(iOS)
===============

1. Add [weixin lib](http://open.weixin.qq.com/document/gettingstart/ios/) to your project. Don't forget to add the "URL Type".

2. ```cordova plugin add https://github.com/xu-li/cordova-plugin-weixin```, or using [plugman](https://npmjs.org/package/plugman), [phonegap](https://npmjs.org/package/phonegap)

3. ```cordova build ios``` (it will fail if you haven't include the weixin lib yet.)

4. Open ```config.xml``` in xcode at the root.

5. Add ```<preference name="weixinappid" value="YOUR_WEIXIN_APP_ID" />```

Usage
===============

```Javascript
cordova.exec(function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
}, 'Weixin', 'share', [{
    title: "Awesome!",
    description: "This is an awesome cordova plugin!",
    thumb: "http://cordova.apache.org/images/cordova_bot.png",
    url: "https://github.com/xu-li/cordova-plugin-weixin"
}]);
```


FAQ
===============


TODO
===============

1. Add android version

2. Share to weixin session(聊天) and weixin favorite(收藏) 

3. Add other media types, including music etc.

LICENSE
===============

[MIT LICENSE](http://opensource.org/licenses/MIT)
