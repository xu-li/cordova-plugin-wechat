//
//  AppDelegate+Wechat.h
//  cordova-plugin-wechat
//
//  Created by Jason.z on 26/2/20.
//
//

#import "AppDelegate.h"

@interface AppDelegate (Wechat)

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation;
- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray * _Nullable))restorationHandler;

@end
