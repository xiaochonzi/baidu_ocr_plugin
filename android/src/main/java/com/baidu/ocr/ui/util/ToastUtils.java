package com.baidu.ocr.ui.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void showToast(Context mContext,String msg) {
        Toast toast = Toast.makeText(mContext, "",
                Toast.LENGTH_SHORT);
        toast.setText(msg);
        toast.show();
    }
}
