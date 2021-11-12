
import 'dart:async';

import 'package:baidu_ocr_plugin/model/idcard.dart';
import 'package:flutter/services.dart';
export 'package:baidu_ocr_plugin/model/idcard.dart';

class BaiduOcrPlugin {
  static const MethodChannel _channel =
      const MethodChannel('baidu_ocr_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future initSDK(String apiKey, String secretKey) async{
    await _channel.invokeMethod("initSdk", {"apiKey":apiKey, "secretKey":secretKey});

  }

  static Future<bool?> requestPermissions() async{
    final bool? result = await _channel.invokeMethod("requestPermissions");
    return result;
  }

  static Future<IdCardFontInfo?> recognizeIDCardFont()async{
    final Map? result = await _channel.invokeMethod("recognizeIDCardFont");
    return IdCardFontInfo.fromMap(result!);
  }

  static Future<IdCardBackInfo?> recognizeIDCardBack()async{
    final Map<String,dynamic>? result = await _channel.invokeMethod("recognizeIDCardBack");
    return IdCardBackInfo.fromMap(result!);
  }
}
