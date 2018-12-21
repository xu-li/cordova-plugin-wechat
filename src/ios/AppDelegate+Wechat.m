//
//  AppDelegate+Wechat.m
//  cordova-plugin-wechat
//
//  Created by DerekChia on 2018/12/20.
//

#import <Foundation/Foundation.h>

#import "AppDelegate+Wechat.h"
#import "WXApi.h"
#import "WXApiObject.h"
#import "CDVWechat.h"
#import <objc/runtime.h>

@implementation AppDelegate (CordovaWechat)

BOOL _cordovaWechatIsReplaceMethod = YES;

+ (void)load
{
    static dispatch_once_t token;
    dispatch_once(&token, ^{
        
        SEL orginSel = @selector(application:openURL:options:);
        SEL overrideSel = @selector(CordovaWechatApplication:openURL:options:);
        
        Method originMethod = class_getInstanceMethod([self class], orginSel);
        Method overrideMethod = class_getInstanceMethod([self class], overrideSel);
        
        if (class_addMethod([self class], orginSel, method_getImplementation(overrideMethod) , method_getTypeEncoding(originMethod))) {
            class_replaceMethod([self class], overrideSel, method_getImplementation(originMethod), method_getTypeEncoding(originMethod));
            _cordovaWechatIsReplaceMethod = YES;
        }else{
            method_exchangeImplementations(originMethod, overrideMethod);
            _cordovaWechatIsReplaceMethod = NO;
        }
    });
}

- (BOOL)CordovaWechatApplication:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options
{
    if ([url.host isEqualToString:@"pay"]) {
        NSLog(@"current CDVWechat sharedManager: %@", [CDVWechat sharedManager]);
        if ([CDVWechat sharedManager] != nil) {
            [WXApi handleOpenURL:url delegate: [CDVWechat sharedManager] ];
        }
    }
    
    if (!_cordovaWechatIsReplaceMethod) {
        [self CordovaWechatApplication:app openURL:url options:options];
    }
}

@end
