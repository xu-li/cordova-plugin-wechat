//
//  CDVWeixin.h
//  cordova-plugin-weixin
//
//  How to use this in JS:
//  cordova.exec(function () {
//      alert("Success");
//  }, function (reason) {
//      alert("Failed: " + reason);
//  }, 'Weixin', 'share', [{
//      message: {
//          title: "Message Title",
//          description: "Message Description(optional)",
//          mediaTagName: "Media Tag Name(optional)",
//          thumb: "http://YOUR_THUMBNAIL_IMAGE",
//          media: {
//              type: 1, // webpage
//              webpageUrl: "Web Page Url"  // webpage
//          }
//      },
//      scene: 1 // share to WXSceneTimeline
//  }]);
//
//  Created by xu.li on 12/23/13.
//
//

#import <Cordova/CDV.h>
#import "WXApi.h"

enum  CDVWeixinSharingType {
    CDVWXSharingTypeApp = 1,
    CDVWXSharingTypeEmotion,
    CDVWXSharingTypeFile,
    CDVWXSharingTypeImage,
    CDVWXSharingTypeMusic,
    CDVWXSharingTypeVideo,
    CDVWXSharingTypeWebPage
};

@interface CDVWeixin:CDVPlugin <WXApiDelegate>

@property (nonatomic, strong) NSString *currentCallbackId;
@property (nonatomic, strong) NSString *weixinAppId;

- (void)share:(CDVInvokedUrlCommand *)command;

@end
