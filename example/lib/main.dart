import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:baidu_ocr_plugin/baidu_ocr_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  Map? result;
  bool? hasInit = false;
  bool? hasPermission=false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await BaiduOcrPlugin.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text("是否初始化:${hasInit}"),
              Text("是否获取权限:${hasPermission}"),
              Text("结果:${result}"),
              TextButton(onPressed: (){
                BaiduOcrPlugin.initSDK("K8IoTEW10jQ9lycpHGVp3e9G", "xFsc8D3FjOWvH6jxcv79giCXxUcgLyIq");
              }, child: Text("初始化sdk")),
              TextButton(onPressed: (){
                BaiduOcrPlugin.requestPermissions().then((value){
                  setState(() {
                    hasPermission = value;
                  });
                });
              }, child: Text("获取权限")),

              TextButton(onPressed: (){
                BaiduOcrPlugin.recognizeIDCardFont().then((value){
                  Map r = value!.toMap();
                  setState(() {
                    result = r;
                  });
                });
              }, child: Text("识别正面身份证")),
              TextButton(onPressed: (){
                BaiduOcrPlugin.recognizeIDCardBack().then((value){
                  Map r = value!.toMap();
                  setState(() {
                    result = r;
                  });
                });
              }, child: Text("识别反面身份证")),
            ],
          ),
        ),
      ),
    );
  }
}
