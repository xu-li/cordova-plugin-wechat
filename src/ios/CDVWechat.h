//
//  CDVWechat.h
//  cordova-plugin-wechat
//
//  Created by xu.li on 12/23/13.
//
//

#import <Cordova/CDV.h>
#import "WXApi.h"
#import "WXApiObject.h"

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

@property (nonatomic, copy) NSString *currentCallbackId;
@property (nonatomic, copy) NSString *wechatAppId;
@property (nonatomic, copy)   NSString *wechatAppSecret;

- (void)isWXAppInstalled:(CDVInvokedUrlCommand *)command;
- (void)share:(CDVInvokedUrlCommand *)command;
- (void)sendAuthRequest:(CDVInvokedUrlCommand *)command;
- (void)getUserInformation:(CDVInvokedUrlCommand *)command;
- (void)sendPaymentRequest:(CDVInvokedUrlCommand *)command;
- (void)jumpToBizProfile:(CDVInvokedUrlCommand *)command;
- (void)jumpToWechat:(CDVInvokedUrlCommand *)command;

@end
