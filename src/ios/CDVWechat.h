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
    CDVWXSharingTypeWebPage,
    CDVWXSharingTypeMini
};

@interface CDVWechat:CDVPlugin <WXApiDelegate>

@property (nonatomic, strong) NSString *currentCallbackId;
@property (nonatomic, strong) NSString *wechatAppId;
@property (nonatomic, strong) NSString *wechatData;

- (void)isWXAppInstalled:(CDVInvokedUrlCommand *)command;
- (void)share:(CDVInvokedUrlCommand *)command;
- (void)sendAuthRequest:(CDVInvokedUrlCommand *)command;
- (void)sendPaymentRequest:(CDVInvokedUrlCommand *)command;
- (void)jumpToBizProfile:(CDVInvokedUrlCommand *)command;
- (void)jumpToWechat:(CDVInvokedUrlCommand *)command;
- (void)chooseInvoiceFromWX: (CDVInvokedUrlCommand *)command;
- (void)openMiniProgram: (CDVInvokedUrlCommand *)command;
- (void)openApp:(CDVInvokedUrlCommand *)command;
- (BOOL)handleUserActivity:(NSUserActivity *)userActivity;
- (BOOL)handleWechatOpenURL:(NSURL *)url;
@end
