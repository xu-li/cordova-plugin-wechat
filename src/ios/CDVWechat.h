//
//  CDVWechat.h
//  cordova-plugin-wechat
//
//  Created by xu.li on 12/23/13.
//
//

#import <Cordova/CDV.h>
#import "WXApi.h"

enum  CDVWechatSharingType {
    CDVWXSharingTypeApp = 1,
    CDVWXSharingTypeEmotion,
    CDVWXSharingTypeFile,
    CDVWXSharingTypeImage,
    CDVWXSharingTypeMusic,
    CDVWXSharingTypeVideo,
    CDVWXSharingTypeWebPage
};

@interface CDVWechat:CDVPlugin <WXApiDelegate>

@property (nonatomic, strong) NSString *currentCallbackId;
@property (nonatomic, strong) NSString *wechatAppId;

- (void)share:(CDVInvokedUrlCommand *)command;

@end
