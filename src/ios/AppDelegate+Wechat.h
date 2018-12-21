//
//  AppDelegate+Wechat.h
//  cordova-plugin-wechat
//
//  Created by DerekChia on 2018/12/20.
//

#ifndef AppDelegate_Wechat_h
#define AppDelegate_Wechat_h


#endif /* AppDelegate_Wechat_h */

#import "AppDelegate.h"

@interface AppDelegate (CordovaWechat)

@property (nonatomic) BOOL cordovaWechatIsReplaceMethod;

- (BOOL)CordovaWechatApplication:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options;
@end
