/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.ocr.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.baidu.ocr.sdk.utils.ExifUtil;

public class FileUtil {
    public static File getSaveFile(Context context) {
        File file = new File(context.getFilesDir(), "pic.jpg");
        return file;
    }

    public static String savePic(Context mContext,File param) {
        // 首先保存图片
        File imageFile = param;
        File saveFile = new File(mContext.getExternalCacheDir(), "hsh");
        if(!saveFile.exists()){
            saveFile.mkdir();
        }
        String fileName = String.valueOf(System.currentTimeMillis())+".jpg";
        File saveImg = new File(saveFile, fileName);
        resize(imageFile.getAbsolutePath(), saveImg.getAbsolutePath(), 1280, 1280, 80);
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                    saveImg.getAbsolutePath(), saveImg.getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(saveImg);
        intent.setData(uri);
        mContext.sendBroadcast(intent);
        return saveImg.getAbsolutePath();
    }

    public static void resize(String inputPath, String outputPath, int dstWidth, int dstHeight, int quality) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(inputPath, options);
            int inWidth = options.outWidth;
            int inHeight = options.outHeight;
            Matrix m = new Matrix();
            ExifInterface exif = new ExifInterface(inputPath);
            int rotation = exif.getAttributeInt("Orientation", 1);
            if (rotation != 0) {
                m.preRotate((float) ExifUtil.exifToDegrees(rotation));
            }

            int maxPreviewImageSize = Math.max(dstWidth, dstHeight);
            int size = Math.min(options.outWidth, options.outHeight);
            size = Math.min(size, maxPreviewImageSize);
            options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(options, size, size);
            options.inScaled = true;
            options.inDensity = options.outWidth;
            options.inTargetDensity = size * options.inSampleSize;
            Bitmap roughBitmap = BitmapFactory.decodeFile(inputPath, options);
            FileOutputStream out = new FileOutputStream(outputPath);
            try {
                roughBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            } catch (Exception var25) {
                var25.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (Exception var24) {
                    var24.printStackTrace();
                }
            }
        } catch (IOException var27) {
            var27.printStackTrace();
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            for(int halfWidth = width / 2; halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth; inSampleSize *= 2) {
                ;
            }
        }
        return inSampleSize;
    }
}
