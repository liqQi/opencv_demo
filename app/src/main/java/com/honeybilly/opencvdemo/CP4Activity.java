package com.honeybilly.opencvdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeybilly.opencvdemo.utils.BitmapUtils;
import com.honeybilly.opencvdemo.utils.FileUtils;
import com.honeybilly.opencvdemo.utils.ImageUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liqi on 14:22.
 */
public class CP4Activity extends AppCompatActivity {

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static void launch(Context context) {
        Intent intent = new Intent(context, CP4Activity.class);

        context.startActivity(intent);
    }

    public static final int REQUEST_IMAGE = 101;
    public static final int REQUEST_IMAGE_2 = 102;

    protected ImageView ivSrc2;
    protected EditText etScale;
    protected ImageView ivSrc;
    protected ImageView ivDes;
    protected Bitmap bitmap;
    protected Bitmap bitmap2;
    protected String path1;
    protected String path2;
    protected TextView tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp4);
        ivSrc = findViewById(R.id.src);
        ivSrc2 = findViewById(R.id.src2);
        ivDes = findViewById(R.id.des);
        etScale = findViewById(R.id.scale);
        tvMsg = findViewById(R.id.msg);
        findViewById(R.id.select_1).setOnClickListener(v -> ImageUtils.requestImage(CP4Activity.this, REQUEST_IMAGE));
        findViewById(R.id.select_2).setOnClickListener(v -> ImageUtils.requestImage(CP4Activity.this, REQUEST_IMAGE_2));
        findViewById(R.id.transform_1).setOnClickListener(v -> {
            adaptiveThreshold();
        });
        findViewById(R.id.transform_2).setOnClickListener(v -> {
            threshold();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        File file = FileUtils.getFile(this, uri);
                        path1 = file.getAbsolutePath();
                        Mat mat = Imgcodecs.imread(file.getAbsolutePath());
                        Mat result = BitmapUtils.BGR2RGBA(mat);
                        bitmap = BitmapUtils.matToBitmap(result);
                        ivSrc.setImageBitmap(bitmap);
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_2) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        File file = FileUtils.getFile(this, uri);
                        path2 = file.getAbsolutePath();
                        Mat mat = Imgcodecs.imread(file.getAbsolutePath());
                        Mat result = BitmapUtils.BGR2RGBA(mat);
                        bitmap2 = BitmapUtils.matToBitmap(result);
                        ivSrc2.setImageBitmap(bitmap2);
                    }
                }
            }
        }
    }

    /**
     * 均值模糊
     */
    private void blur() {
        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Imgproc.blur(img1, des, new Size(5, 5), new Point(0, 0), Core.BORDER_DEFAULT);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
    }

    /**
     * 均值模糊
     */
    private void blur2() {
        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Imgproc.blur(img1, des, new Size(15, 1), new Point(0, 0), Core.BORDER_DEFAULT);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
    }

    private void gussianBlur() {
        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Imgproc.GaussianBlur(img1, des, new Size(5, 5), 0);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
    }

    private void gussianBlur2() {
        String s = etScale.getText().toString();
        if (s.isEmpty()) {
            return;
        }
        int i = Integer.parseInt(s);

        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Imgproc.GaussianBlur(img1, des, new Size(i, i), 0);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
    }

    private void medianBlur() {
        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Imgproc.medianBlur(img1, des, 15);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
    }

    /**
     * 最大值滤波
     */
    private void max() {
        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.dilate(img1, des, structuringElement);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
        structuringElement.release();
    }

    /**
     * 最小值滤波
     */
    private void min() {
        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.erode(img1, des, structuringElement);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
        structuringElement.release();
    }

    /**
     * 高斯双边滤波
     */
    private void bilateralFilter() {
        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Imgproc.bilateralFilter(img1, des, 0, 150, 15);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
    }

    /**
     * 均值迁移滤波
     */
    private void meanShift() {
        Mat img1 = Imgcodecs.imread(path1);
        //Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Imgproc.pyrMeanShiftFiltering(img1, des, 10, 50);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        img1.release();
        des.release();
        result.release();
    }

    private void adaptiveThreshold() {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Mat>) emitter -> {
            Mat img = Imgcodecs.imread(path1);
            Mat img1 = new Mat();
            Imgproc.cvtColor(img, img1, Imgproc.COLOR_BGR2GRAY);
            Mat des = new Mat();
            Imgproc.adaptiveThreshold(img1, des, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 0);
            img1.release();
            emitter.onNext(des);
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(mat -> {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);
            ivDes.setImageBitmap(bitmap);
            mat.release();
        }, Throwable::printStackTrace);
        compositeDisposable.add(subscribe);
    }

    private void threshold() {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Mat>) emitter -> {
            Mat img1 = Imgcodecs.imread(path1);
            Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
            Mat des = new Mat();
            Imgproc.threshold(img1, des, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
            img1.release();
            emitter.onNext(des);
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(mat -> {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);
            ivDes.setImageBitmap(bitmap);
            mat.release();
        }, Throwable::printStackTrace);
        compositeDisposable.add(subscribe);
    }

}
