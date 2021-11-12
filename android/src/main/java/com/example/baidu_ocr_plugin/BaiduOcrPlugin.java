package com.example.baidu_ocr_plugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.baidu.ocr.demo.FileUtil;
import com.baidu.ocr.demo.IDCardActivity;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.baidu.ocr.ui.camera.CameraNativeHelper;
import com.baidu.ocr.ui.camera.CameraView;
import com.baidu.ocr.ui.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** BaiduOcrPlugin */
public class BaiduOcrPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {

  private static final int REQUEST_CODE_CAMERA = 102;
  private static final int REQUEST_PERMISSION_CODE = 99;
  private static final String TAG = "BaiduOcrPlugin";
  private static boolean hasGotToken = false;
  private MethodChannel channel;
  private static Activity activity;
  private static Context mContext;
  private Result mResult;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "baidu_ocr_plugin");
    channel.setMethodCallHandler(this);
    mContext = flutterPluginBinding.getApplicationContext();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }else if(call.method.equals("initSdk")){
      initSdk(call.argument("apiKey").toString(), call.argument("secretKey").toString());
    }else if(call.method.equals("requestPermissions")){
      mResult = result;
      requestPermissions(REQUEST_PERMISSION_CODE);
    }else if(call.method.equals("recognizeIDCardFont")){
      mResult = result;
      recognizeIDCardFont();
    }else if(call.method.equals("recognizeIDCardBack")){
      mResult = result;
      recognizeIDCardBack();
    } else {
      result.notImplemented();
    }
  }

  private void initSdk(String apikey, String secretKey) {
    if(hasGotToken){
      ToastUtils.showToast(mContext, "已经初始化");
      return;
    }
    OCR.getInstance(mContext).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
      @Override
      public void onResult(AccessToken result) {
        String token = result.getAccessToken();
        hasGotToken = true;
      }

      @Override
      public void onError(OCRError error) {
        Log.e(TAG, "onError: ", error.getCause());
      }
    }, mContext.getApplicationContext(),  apikey, secretKey);
  }

  // 请求权限
  public void requestPermissions(int requestCode) {
    try {
      if (Build.VERSION.SDK_INT >= 23) {
        ArrayList<String> requestPerssionArr = new ArrayList<>();
        int hasCamrea = mContext.checkSelfPermission(Manifest.permission.CAMERA);
        if (hasCamrea != PackageManager.PERMISSION_GRANTED) {
          requestPerssionArr.add(Manifest.permission.CAMERA);
        }

        int hasSdcardRead = mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasSdcardRead != PackageManager.PERMISSION_GRANTED) {
          requestPerssionArr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        int hasSdcardWrite = mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasSdcardWrite != PackageManager.PERMISSION_GRANTED) {
          requestPerssionArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        // 是否应该显示权限请求
        if (requestPerssionArr.size() >= 1) {
          String[] requestArray = new String[requestPerssionArr.size()];
          for (int i = 0; i < requestArray.length; i++) {
            requestArray[i] = requestPerssionArr.get(i);
          }
          activity.requestPermissions(requestArray, requestCode);
        }
      }
    } catch (Exception e) {
      mResult.success(false);
    }
  }

  private void recognizeIDCardFont() {
    if(hasGotToken){
      initIdCardRecognize();
      Intent intent = new Intent(activity, CameraActivity.class);
      intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
              FileUtil.getSaveFile(mContext).getAbsolutePath());
      intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
      activity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }else{
      ToastUtils.showToast(mContext, "token还未成功获取");
    }
  }

  private void recognizeIDCardBack() {
    if(hasGotToken){
      initIdCardRecognize();
      Intent intent = new Intent(activity, CameraActivity.class);
      intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
              FileUtil.getSaveFile(mContext).getAbsolutePath());
      intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
      activity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }else{
      ToastUtils.showToast(mContext, "token还未成功获取");
    }
  }

  private void initIdCardRecognize(){
    CameraNativeHelper.init(mContext, OCR.getInstance(mContext).getLicense(),
            new CameraNativeHelper.CameraNativeInitCallback() {
              @Override
              public void onError(int errorCode, Throwable e) {
                String msg;
                switch (errorCode) {
                  case CameraView.NATIVE_SOLOAD_FAIL:
                    msg = "加载so失败，请确保apk中存在ui部分的so";
                    break;
                  case CameraView.NATIVE_AUTH_FAIL:
                    msg = "授权本地质量控制token获取失败";
                    break;
                  case CameraView.NATIVE_INIT_FAIL:
                    msg = "本地质量控制";
                    break;
                  default:
                    msg = String.valueOf(errorCode);
                }
                Log.e(TAG, "onError: initIdCardRecognize:"+msg, e);
              }
            });
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    OCR.getInstance(mContext).release();
    CameraNativeHelper.release();
    channel.setMethodCallHandler(null);
    mContext = null;
    mResult = null;
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addActivityResultListener(this);
    binding.addRequestPermissionsResultListener(this);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    binding.removeActivityResultListener(this);
    binding.removeRequestPermissionsResultListener(this);
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
      if (data != null) {
        String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
        String filePath = FileUtil.getSaveFile(mContext).getAbsolutePath();
        if (!TextUtils.isEmpty(contentType)) {
          if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
            recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
          } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
            recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
          }
        }
      }
    }
    return false;
  }

  private void recIDCard(final String idCardSide, String filePath) {
    IDCardParams param = new IDCardParams();
    param.setImageFile(new File(filePath));
    // 设置身份证正反面
    param.setIdCardSide(idCardSide);
    // 设置方向检测
    param.setDetectDirection(true);
    // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
    param.setImageQuality(100);
    final String imageFilePath = FileUtil.savePic(mContext, param.getImageFile());
    OCR.getInstance(mContext).recognizeIDCard(param, new OnResultListener<IDCardResult>() {
      @Override
      public void onResult(IDCardResult result) {
        Log.i(TAG, "onResult: recIDCard"+ result.toString());
        Map<String,Object> resultMap = new HashMap<>();
        if (result != null) {
          resultMap.put("result", true);
          resultMap.put("image", imageFilePath);
          if("front".equals(idCardSide)){
            resultMap.put("address", result.getAddress().getWords());
            resultMap.put("idNumber", result.getIdNumber().getWords());
            resultMap.put("birthday", result.getBirthday().getWords());
            resultMap.put("name", result.getName().getWords());
            resultMap.put("gender", result.getGender().getWords());
            resultMap.put("ethnic", result.getEthnic().getWords());
          }else{
            resultMap.put("signDate",result.getSignDate().getWords());
            resultMap.put("expiryDate", result.getExpiryDate().getWords());
            resultMap.put("issueAuthority", result.getIssueAuthority().getWords());
          }
        }else{
          resultMap.put("result", false);
        }
        mResult.success(resultMap);
      }
      @Override
      public void onError(OCRError error) {
        Log.e(TAG, "onError: "+error.getMessage(), error.getCause());
        mResult.error(error.getErrorCode()+"", error.getMessage(), error.getCause());
      }
    });
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (REQUEST_PERMISSION_CODE == requestCode){
      boolean flag = false;
      for (int i = 0; i < permissions.length; i++) {
        if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
          flag = true;
        }
      }
      mResult.success(flag);
    }
    return false;
  }
}
