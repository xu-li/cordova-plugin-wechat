//
//  CDVWeixin.m
//  phonegap-weixin
//
//  Created by xu.li on 12/23/13.
//
//

#import "CDVWeixin.h"

@implementation CDVWeixin

#pragma mark "API"

- (void)share:(CDVInvokedUrlCommand *)command
{
    [WXApi registerApp:self.weixinAppId];
    
    NSDictionary *params = [command.arguments objectAtIndex:0];
    NSString *title = [params objectForKey:@"title"];
    NSString *description = [params objectForKey:@"description"];
    NSString *imageUrl = [params objectForKey:@"thumb"];
    NSString *url = [params objectForKey:@"url"];
    
    // @TODO add other media types
    // @TODO add WXSceneSession
    SendMessageToWXReq* req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = [self buildMediaMessage:title description:description thumb:imageUrl url:url];
    req.scene = WXSceneTimeline;
    
    [WXApi sendReq:req];
    
    self.currentCallbackId = command.callbackId;
}

#pragma mark "WXApiDelegate"

/**
 * Not implemented
 */
//- (void)onReq:(BaseReq *)req
//{
//
//}

- (void)onResp:(BaseResp *)resp
{
    CDVPluginResult *result = nil;
    
    BOOL success = NO;
    if([resp isKindOfClass:[SendMessageToWXResp class]])
    {
        switch (resp.errCode)
        {
            case WXSuccess:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                success = YES;
            break;
            
            case WXErrCodeCommon:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"普通错误类型"];
            break;
            
            case WXErrCodeUserCancel:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"用户点击取消并返回"];
            break;
            
            case WXErrCodeSentFail:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"发送失败"];
            break;
            
            case WXErrCodeAuthDeny:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"授权失败"];
            break;
            
            case WXErrCodeUnsupport:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"微信不支持"];
            break;
        }
    }
    
    if (!result)
    {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unknown"];
    }
    
    if (success)
    {
        [self success:result callbackId:self.currentCallbackId];
    }
    else
    {
        [self error:result callbackId:self.currentCallbackId];
    }
    
    self.currentCallbackId = nil;
}

#pragma mark "CDVPlugin Overrides"

- (void)handleOpenURL:(NSNotification *)notification
{
    NSURL* url = [notification object];
    
    if ([url isKindOfClass:[NSURL class]] && [url.scheme isEqualToString:self.weixinAppId])
    {
        [WXApi handleOpenURL:url delegate:self];
    }
}

#pragma mark "Private methods"

- (NSString *)weixinAppId
{
    if (!_weixinAppId)
    {
        CDVViewController *viewController = (CDVViewController *)self.viewController;
        _weixinAppId = [viewController.settings objectForKey:@"weixinappid"];
    }
    
    return _weixinAppId;
}

- (WXMediaMessage *)buildMediaMessage:(NSString *)title description:(NSString *)description thumb:(NSString *)thumb url:(NSString *)url
{
    NSURL *thumbUrl = [NSURL URLWithString:thumb];
    
    // @TODO async loading
    // @TODO disable caching?
    NSData *thumbImageData = [NSData dataWithContentsOfURL:thumbUrl];
    UIImage *thumbImage = [UIImage imageWithData:thumbImageData];
    
    WXMediaMessage *message = [WXMediaMessage message];
    message.title = title;
    message.description = description;
    [message setThumbImage:thumbImage];
    
    WXWebpageObject *page = [WXWebpageObject object];
    page.webpageUrl = url;

    message.mediaObject = page;
    
    return message;
}
@end
