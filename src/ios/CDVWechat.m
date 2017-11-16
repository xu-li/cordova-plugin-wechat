//
//  CDVWechat.m
//  cordova-plugin-wechat
//
//  Created by xu.li on 12/23/13.
//
//

#import "CDVWechat.h"

static int const MAX_THUMBNAIL_SIZE = 320;

@implementation CDVWechat

#pragma mark "API"
- (void)pluginInitialize {
    NSString* appId = [[self.commandDelegate settings] objectForKey:@"wechatappid"];
    if (appId){
        self.wechatAppId = appId;
        [WXApi registerApp: appId];
    }

    NSLog(@"cordova-plugin-wechat has been initialized. Wechat SDK Version: %@. APP_ID: %@.", [WXApi getApiVersion], appId);
}

- (void)isWXAppInstalled:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[WXApi isWXAppInstalled]];

    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

- (void)share:(CDVInvokedUrlCommand *)command
{
    // if not installed
    if (![WXApi isWXAppInstalled])
    {
        [self failWithCallbackID:command.callbackId withMessage:@"未安装微信"];
        return ;
    }

    // check arguments
    NSDictionary *params = [command.arguments objectAtIndex:0];
    if (!params)
    {
        [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
        return ;
    }

    // save the callback id
    self.currentCallbackId = command.callbackId;

    SendMessageToWXReq* req = [[SendMessageToWXReq alloc] init];

    // check the scene
    if ([params objectForKey:@"scene"])
    {
        req.scene = (int)[[params objectForKey:@"scene"] integerValue];
    }
    else
    {
        req.scene = WXSceneTimeline;
    }

    // message or text?
    NSDictionary *message = [params objectForKey:@"message"];

    if (message)
    {
        req.bText = NO;

        // async
        [self.commandDelegate runInBackground:^{
            req.message = [self buildSharingMessage:message];
            if (![WXApi sendReq:req])
            {
                [self failWithCallbackID:command.callbackId withMessage:@"发送请求失败"];
                self.currentCallbackId = nil;
            }
        }];
    }
    else
    {
        req.bText = YES;
        req.text = [params objectForKey:@"text"];

        if (![WXApi sendReq:req])
        {
            [self failWithCallbackID:command.callbackId withMessage:@"发送请求失败"];
            self.currentCallbackId = nil;
        }
    }
}

- (void)sendAuthRequest:(CDVInvokedUrlCommand *)command
{

    SendAuthReq* req =[[SendAuthReq alloc] init];

    // scope
    if ([command.arguments count] > 0)
    {
        req.scope = [command.arguments objectAtIndex:0];
    }
    else
    {
        req.scope = @"snsapi_userinfo";
    }

    // state
    if ([command.arguments count] > 1)
    {
        req.state = [command.arguments objectAtIndex:1];
    }

    if ([WXApi sendAuthReq:req viewController:self.viewController delegate:self])
    {
        // save the callback id
        self.currentCallbackId = command.callbackId;
    }
    else
    {
        [self failWithCallbackID:command.callbackId withMessage:@"发送请求失败"];
    }
}

- (void)sendPaymentRequest:(CDVInvokedUrlCommand *)command
{
    // check arguments
    NSDictionary *params = [command.arguments objectAtIndex:0];
    if (!params)
    {
        [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
        return ;
    }

    // check required parameters
    NSArray *requiredParams;
    if ([params objectForKey:@"mch_id"])
    {
        requiredParams = @[@"mch_id", @"prepay_id", @"timestamp", @"nonce", @"sign"];
    }
    else
    {
        requiredParams = @[@"partnerid", @"prepayid", @"timestamp", @"noncestr", @"sign"];
    }

    for (NSString *key in requiredParams)
    {
        if (![params objectForKey:key])
        {
            [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
            return ;
        }
    }

    PayReq *req = [[PayReq alloc] init];
    req.partnerId = [params objectForKey:requiredParams[0]];
    req.prepayId = [params objectForKey:requiredParams[1]];
    req.timeStamp = [[params objectForKey:requiredParams[2]] intValue];
    req.nonceStr = [params objectForKey:requiredParams[3]];
    req.package = @"Sign=WXPay";
    req.sign = [params objectForKey:requiredParams[4]];

    if ([WXApi sendReq:req])
    {
        // save the callback id
        self.currentCallbackId = command.callbackId;
    }
    else
    {
        [self failWithCallbackID:command.callbackId withMessage:@"发送请求失败"];
    }
}
- (void)chooseInvoiceFromWX:(CDVInvokedUrlCommand *)command
{
    NSDictionary *params = [command.arguments objectAtIndex:0];
    WXChooseInvoiceReq *req = [[WXChooseInvoiceReq alloc] init];
    req.cardSign = [params objectForKey:@"cardSign"];
    req.timeStamp = [[params objectForKey:@"timeStamp"] intValue];
    req.appID = [params objectForKey:@"appId"];
    req.nonceStr = [params objectForKey:@"nonceStr"];
    req.signType = [params objectForKey:@"signType"];

    if ([WXApi sendReq:req])
    {
        // save th e callback id
        self.currentCallbackId = command.callbackId;
    }
    else
    {
        [self failWithCallbackID:command.callbackId withMessage:@"发送请求失败"];
    }

}

- (void)jumpToBizProfile:(CDVInvokedUrlCommand *)command
{
    // check arguments
    NSDictionary *params = [command.arguments objectAtIndex:0];
    if (!params)
    {
        [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
        return ;
    }

    // check required parameters
    NSArray *requiredParams;
    requiredParams = @[@"type", @"info"];

    for (NSString *key in requiredParams)
    {
        if (![params objectForKey:key])
        {
            [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
            return ;
        }
    }
    JumpToBizProfileReq *req = [JumpToBizProfileReq new];
    NSString *bizType =  [params objectForKey:requiredParams[0]];

    if ([bizType isEqualToString:@"Normal"]) {
        req.profileType = WXBizProfileType_Normal;
        req.username = [params objectForKey:requiredParams[1]];
    } else {
        req.profileType = WXBizProfileType_Device;
        req.extMsg = [params objectForKey:requiredParams[1]];
    }

    if ([WXApi sendReq:req])
    {
        // save the callback id
        self.currentCallbackId = command.callbackId;
    }
    else
    {
        [self failWithCallbackID:command.callbackId withMessage:@"发送请求失败"];
    }
}

- (void)jumpToWechat:(CDVInvokedUrlCommand *)command
{
    // check arguments
    NSString *url = [command.arguments objectAtIndex:0];
    if (!url || ![url hasPrefix:@"weixin://"])
    {
        [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
        return ;
    }

    NSURL *formatUrl = [NSURL URLWithString:[url stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    if ([[UIApplication sharedApplication] canOpenURL:formatUrl]) {
        [[UIApplication sharedApplication] openURL:formatUrl];
    } else{
        [self failWithCallbackID:command.callbackId withMessage:@"未安装微信或其他错误"];
    }
    return ;
}



#pragma mark "WXApiDelegate"

/**
 * Not implemented
 */
- (void)onReq:(BaseReq *)req
{
    NSLog(@"%@", req);
}

- (void)onResp:(BaseResp *)resp
{
    BOOL success = NO;
    NSString *message = @"Unknown";
    NSDictionary *response = nil;

    switch (resp.errCode)
    {
        case WXSuccess:
            success = YES;
            break;

        case WXErrCodeCommon:
            message = @"普通错误";
            break;

        case WXErrCodeUserCancel:
            message = @"用户点击取消并返回";
            break;

        case WXErrCodeSentFail:
            message = @"发送失败";
            break;

        case WXErrCodeAuthDeny:
            message = @"授权失败";
            break;

        case WXErrCodeUnsupport:
            message = @"微信不支持";
            break;

        default:
            message = @"未知错误";
    }

    if (success)
    {
        if ([resp isKindOfClass:[SendAuthResp class]])
        {
            // fix issue that lang and country could be nil for iPhone 6 which caused crash.
            SendAuthResp* authResp = (SendAuthResp*)resp;
            response = @{
                         @"code": authResp.code != nil ? authResp.code : @"",
                         @"state": authResp.state != nil ? authResp.state : @"",
                         @"lang": authResp.lang != nil ? authResp.lang : @"",
                         @"country": authResp.country != nil ? authResp.country : @"",
                         };

            CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];

            [self.commandDelegate sendPluginResult:commandResult callbackId:self.currentCallbackId];
        }
        else if([resp isKindOfClass:[WXChooseInvoiceResp class]]){
                    WXChooseInvoiceResp* invoiceResp = (WXChooseInvoiceResp *)resp;

        //            response = @{
        //                         @"data":invoiceResp.cardAry
        //                         }
                    NSMutableArray *arrM = [[NSMutableArray alloc] init];
                    NSDictionary *mutableDic = nil;
                    for(WXInvoiceItem *invoiceItem in invoiceResp.cardAry){
                        mutableDic = @{
                                       @"cardId": invoiceItem.cardId,
                                       @"encryptCode": invoiceItem.encryptCode,
                                       };
                        [arrM addObject:mutableDic];
                    }
                    response = @{
                                 @"data": arrM
                                 };
                    NSLog(@"response======= %@", response);
                    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
                    [self.commandDelegate sendPluginResult:commandResult callbackId:self.currentCallbackId];
                }
        else
        {
            [self successWithCallbackID:self.currentCallbackId];
        }
    }
    else
    {
        [self failWithCallbackID:self.currentCallbackId withMessage:message];
    }

    self.currentCallbackId = nil;
}

#pragma mark "CDVPlugin Overrides"

- (void)handleOpenURL:(NSNotification *)notification
{
    NSURL* url = [notification object];

    if ([url isKindOfClass:[NSURL class]] && [url.scheme isEqualToString:self.wechatAppId])
    {
        [WXApi handleOpenURL:url delegate:self];
    }
}

#pragma mark "Private methods"

- (WXMediaMessage *)buildSharingMessage:(NSDictionary *)message
{
    WXMediaMessage *wxMediaMessage = [WXMediaMessage message];
    wxMediaMessage.title = [message objectForKey:@"title"];
    wxMediaMessage.description = [message objectForKey:@"description"];
    wxMediaMessage.mediaTagName = [message objectForKey:@"mediaTagName"];
    wxMediaMessage.messageExt = [message objectForKey:@"messageExt"];
    wxMediaMessage.messageAction = [message objectForKey:@"messageAction"];
    if ([message objectForKey:@"thumb"])
    {
        [wxMediaMessage setThumbImage:[self getUIImageFromURL:[message objectForKey:@"thumb"]]];
    }

    // media parameters
    id mediaObject = nil;
    NSDictionary *media = [message objectForKey:@"media"];

    // check types
    NSInteger type = [[media objectForKey:@"type"] integerValue];
    switch (type)
    {
        case CDVWXSharingTypeApp:
            mediaObject = [WXAppExtendObject object];
            ((WXAppExtendObject*)mediaObject).extInfo = [media objectForKey:@"extInfo"];
            ((WXAppExtendObject*)mediaObject).url = [media objectForKey:@"url"];
            break;

        case CDVWXSharingTypeEmotion:
            mediaObject = [WXEmoticonObject object];
            ((WXEmoticonObject*)mediaObject).emoticonData = [self getNSDataFromURL:[media objectForKey:@"emotion"]];
            break;

        case CDVWXSharingTypeFile:
            mediaObject = [WXFileObject object];
            ((WXFileObject*)mediaObject).fileData = [self getNSDataFromURL:[media objectForKey:@"file"]];
            ((WXFileObject*)mediaObject).fileExtension = [media objectForKey:@"fileExtension"];
            break;

        case CDVWXSharingTypeImage:
            mediaObject = [WXImageObject object];
            ((WXImageObject*)mediaObject).imageData = [self getNSDataFromURL:[media objectForKey:@"image"]];
            break;

        case CDVWXSharingTypeMusic:
            mediaObject = [WXMusicObject object];
            ((WXMusicObject*)mediaObject).musicUrl = [media objectForKey:@"musicUrl"];
            ((WXMusicObject*)mediaObject).musicDataUrl = [media objectForKey:@"musicDataUrl"];
            break;

        case CDVWXSharingTypeVideo:
            mediaObject = [WXVideoObject object];
            ((WXVideoObject*)mediaObject).videoUrl = [media objectForKey:@"videoUrl"];
            break;

        case CDVWXSharingTypeWebPage:
        default:
            mediaObject = [WXWebpageObject object];
            ((WXWebpageObject *)mediaObject).webpageUrl = [media objectForKey:@"webpageUrl"];
    }

    wxMediaMessage.mediaObject = mediaObject;
    return wxMediaMessage;
}

- (NSData *)getNSDataFromURL:(NSString *)url
{
    NSData *data = nil;

    if ([url hasPrefix:@"http://"] || [url hasPrefix:@"https://"])
    {
        data = [NSData dataWithContentsOfURL:[NSURL URLWithString:url]];
    }
    else if ([url hasPrefix:@"data:image"])
    {
        // a base 64 string
        NSURL *base64URL = [NSURL URLWithString:url];
        data = [NSData dataWithContentsOfURL:base64URL];
    }
    else if ([url rangeOfString:@"temp:"].length != 0)
    {
        url =  [NSTemporaryDirectory() stringByAppendingPathComponent:[url componentsSeparatedByString:@"temp:"][1]];
        data = [NSData dataWithContentsOfFile:url];
    }
    else
    {
        // local file
        url = [[NSBundle mainBundle] pathForResource:[url stringByDeletingPathExtension] ofType:[url pathExtension]];
        data = [NSData dataWithContentsOfFile:url];
    }

    return data;
}

- (UIImage *)getUIImageFromURL:(NSString *)url
{
    NSData *data = [self getNSDataFromURL:url];
    UIImage *image = [UIImage imageWithData:data];

    if (image.size.width > MAX_THUMBNAIL_SIZE || image.size.height > MAX_THUMBNAIL_SIZE)
    {
        CGFloat width = 0;
        CGFloat height = 0;

        // calculate size
        if (image.size.width > image.size.height)
        {
            width = MAX_THUMBNAIL_SIZE;
            height = width * image.size.height / image.size.width;
        }
        else
        {
            height = MAX_THUMBNAIL_SIZE;
            width = height * image.size.width / image.size.height;
        }

        // scale it
        UIGraphicsBeginImageContext(CGSizeMake(width, height));
        [image drawInRect:CGRectMake(0, 0, width, height)];
        UIImage *scaled = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();

        return scaled;
    }

    return image;
}

- (void)successWithCallbackID:(NSString *)callbackID
{
    [self successWithCallbackID:callbackID withMessage:@"OK"];
}

- (void)successWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

- (void)failWithCallbackID:(NSString *)callbackID withError:(NSError *)error
{
    [self failWithCallbackID:callbackID withMessage:[error localizedDescription]];
}

- (void)failWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

@end
