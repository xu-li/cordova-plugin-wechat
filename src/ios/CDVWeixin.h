//
//  CDVWeixin.h
//  phonegap-weixin
//
//  Created by xu.li on 12/23/13.
//
//

#import <Cordova/CDV.h>
#import "WXApi.h"

@interface CDVWeixin:CDVPlugin <WXApiDelegate>

@property (nonatomic, strong) NSString *currentCallbackId;
@property (nonatomic, strong) NSString *weixinAppId;

- (void)share:(CDVInvokedUrlCommand *)command;

@end
