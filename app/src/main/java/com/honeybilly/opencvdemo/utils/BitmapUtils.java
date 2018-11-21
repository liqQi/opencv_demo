package com.honeybilly.opencvdemo.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by liqi on 14:56.
 */
public class BitmapUtils {

    public static Bitmap matToBitmap(Mat mat){
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Config.ARGB_8888);

        Utils.matToBitmap(mat,bitmap);
        return bitmap;
    }

    public static Mat drawable2Mat(Drawable drawable){
        Bitmap bitmap;
        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        drawable.draw(canvas);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap,mat);
        return mat;
    }

    public static Mat BGR2RGBA(Mat mat){
        Mat des  = new Mat();
        Imgproc.cvtColor(mat,des,Imgproc.COLOR_BGR2RGBA);
        return des;
    }

    public static Mat reverseColor(Mat mat) {
        int channels = mat.channels();
        int width = mat.width();
        int height = mat.height();
        byte[] data = new byte[channels];
        int r=0,g=0,b=0;
        for(int row = 0;row<height;row++){
            for(int col = 0;col<width;col++){
                mat.get(row,col,data);
                b = data[0] & 0xff;
                g = data[1]& 0xff;
                r = data[2]& 0xff;
                b = 255-b;
                g = 255-g;
                r = 255-r;
                data[0] = (byte) b;
                data[1] = (byte) g;
                data[2] = (byte) r;
                mat.put(row,col,data);

            }
        }
        return mat;
    }
}
