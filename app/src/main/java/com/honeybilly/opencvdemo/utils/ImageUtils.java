package com.honeybilly.opencvdemo.utils;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by liqi on 10:01.
 */
public class ImageUtils {
    public static void requestImage(Activity activity,int requestCode){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

}
