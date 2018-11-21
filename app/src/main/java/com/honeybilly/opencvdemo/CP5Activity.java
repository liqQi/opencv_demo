package com.honeybilly.opencvdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.honeybilly.opencvdemo.utils.ImageUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liqi on 14:25.
 */
public class CP5Activity extends CP4Activity {


    private CascadeClassifier cascadeClassifier;

    public static void launch(Context context) {
        Intent intent = new Intent(context, CP5Activity.class);

        context.startActivity(intent);
    }

    private InnerHandler innerHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp4);
        innerHandler = new InnerHandler(this);
        ivSrc = findViewById(R.id.src);
        ivSrc2 = findViewById(R.id.src2);
        ivDes = findViewById(R.id.des);
        etScale = findViewById(R.id.scale);
        findViewById(R.id.select_1).setOnClickListener(v -> ImageUtils.requestImage(CP5Activity.this, REQUEST_IMAGE));
        findViewById(R.id.select_2).setOnClickListener(v -> ImageUtils.requestImage(CP5Activity.this, REQUEST_IMAGE_2));
        findViewById(R.id.transform_1).setOnClickListener(v -> displayHistogram());
        findViewById(R.id.transform_2).setOnClickListener(v -> faceDetect());
    }

    private void sobel() {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Mat>) emitter -> {
            Mat img1 = Imgcodecs.imread(path1);
            //Mat img2 = Imgcodecs.imread(path2);
            Mat grayx = new Mat();
            Imgproc.Sobel(img1, grayx, CvType.CV_32F, 1, 0);
            Core.convertScaleAbs(grayx, grayx);
            Mat grayy = new Mat();
            Imgproc.Sobel(img1, grayy, CvType.CV_32F, 0, 1);
            Core.convertScaleAbs(grayy, grayy);
            Mat des = new Mat();
            Core.addWeighted(grayx, 0.5, grayy, 0.5, 0, des);
            img1.release();
            grayx.release();
            grayy.release();
            emitter.onNext(des);
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(mat -> {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);
            ivDes.setImageBitmap(bitmap);
            mat.release();
        }, Throwable::printStackTrace);
        compositeDisposable.add(subscribe);
    }

    private void schaar() {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Mat>) emitter -> {
            Mat img1 = Imgcodecs.imread(path1);
            //Mat img2 = Imgcodecs.imread(path2);
            Mat grayx = new Mat();
            Imgproc.Sobel(img1, grayx, CvType.CV_32F, 1, 0);
            Core.convertScaleAbs(grayx, grayx);
            Mat grayy = new Mat();
            Imgproc.Sobel(img1, grayy, CvType.CV_32F, 0, 1);
            Core.convertScaleAbs(grayy, grayy);
            Mat des = new Mat();
            Core.addWeighted(grayx, 0.5, grayy, 0.5, 0, des);
            img1.release();
            grayx.release();
            grayy.release();
            emitter.onNext(des);
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(mat -> {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);
            ivDes.setImageBitmap(bitmap);
            mat.release();
        }, Throwable::printStackTrace);
        compositeDisposable.add(subscribe);
    }

    private void displayHistogram() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Mat>) emitter -> {
            Mat src = Imgcodecs.imread(path1);
            Mat dst = new Mat();
            Mat gray = new Mat();
            Message msg = new Message();
            msg.what = 1;
            msg.obj = "转换灰度图...";
            innerHandler.sendMessage(msg);
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

            // 计算直方图数据并归一化
            List<Mat> images = new ArrayList<>();
            images.add(gray);
            Mat mask = Mat.ones(src.size(), CvType.CV_8UC1);
            Mat hist = new Mat();
            msg = new Message();
            msg.what = 1;
            msg.obj = "计算直方图...";
            innerHandler.sendMessage(msg);
            Imgproc.calcHist(images, new MatOfInt(0), mask, hist, new MatOfInt(256), new MatOfFloat(0, 255));
            Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
            int height = hist.rows();

            dst.create(400, 400, src.type());
            dst.setTo(new Scalar(200, 200, 200));
            float[] histdata = new float[256];
            hist.get(0, 0, histdata);
            int offsetx = 50;
            int offsety = 350;

            // 绘制直方图
            Imgproc.line(dst, new Point(offsetx, 0), new Point(offsetx, offsety), new Scalar(0, 0, 0));
            Imgproc.line(dst, new Point(offsetx, offsety), new Point(400, offsety), new Scalar(0, 0, 0));
            msg = new Message();
            msg.what = 1;
            msg.obj = "绘制直方图...";
            innerHandler.sendMessage(msg);
            for (int i = 0; i < height - 1; i++) {
                int y1 = (int) histdata[i];
                int y2 = (int) histdata[i + 1];
                Rect rect = new Rect();
                rect.x = offsetx + i;
                rect.y = offsety - y1;
                rect.width = 1;
                rect.height = y1;
                Imgproc.rectangle(dst, rect.tl(), rect.br(), new Scalar(15, 15, 15));
            }
            // 释放内存
            gray.release();
            src.release();
            emitter.onNext(dst);
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(dst -> {
            // 转换为Bitmap，显示
            Bitmap bm = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
            Mat result = new Mat();
            Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA);
            Utils.matToBitmap(result, bm);
            ivDes.setImageBitmap(bm);
            Message msg = new Message();
            msg.what = 1;
            msg.obj = "";
            innerHandler.sendMessage(msg);
            // release memory
        }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void match() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Mat>) emmiter -> {
            Mat src = Imgcodecs.imread(path1);
            Mat tp = Imgcodecs.imread(path2);

            Mat dst = new Mat();

            int height = src.rows() - tp.rows() + 1;
            int width = src.cols() - tp.cols() + 1;

            Mat result = new Mat(width, height, CvType.CV_32FC1);

            // 模板匹配
            int method = Imgproc.TM_CCOEFF_NORMED;
            Imgproc.matchTemplate(src, tp, result, method);
            Core.MinMaxLocResult minMaxResult = Core.minMaxLoc(result);
            Point maxloc = minMaxResult.maxLoc;
            Point minloc = minMaxResult.minLoc;

            Point matchloc = null;
            if (method == Imgproc.TM_SQDIFF || method == Imgproc.TM_SQDIFF_NORMED) {
                matchloc = minloc;
            } else {
                matchloc = maxloc;
            }
            src.copyTo(dst);
            Imgproc.rectangle(dst, matchloc, new Point(matchloc.x + tp.cols(), matchloc.y + tp.rows()), new Scalar(0, 0, 255), 2, 8, 0);
            src.release();
            tp.release();
            result.release();
            emmiter.onNext(dst);
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(mat -> {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);
            ivDes.setImageBitmap(bitmap);
            mat.release();
        }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    private void faceDetect() {
        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface_improved);
        File dir = getDir("cascade", Context.MODE_PRIVATE);
        File file = new File(dir.getAbsoluteFile(), "haarcascade_frontalface_alt_tree.xml");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = is.read(buff)) != -1) {
                fos.write(buff, 0, len);
            }
            fos.close();
            is.close();
            cascadeClassifier = new CascadeClassifier(file.getAbsolutePath());
            file.delete();
            dir.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Mat imread = Imgcodecs.imread(path1);
        Mat gray = new Mat();
        Imgproc.cvtColor(imread, gray, Imgproc.COLOR_BGR2GRAY);
        MatOfRect rect = new MatOfRect();
        Mat mat = new Mat();
        imread.copyTo(mat);
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(gray, rect, 1.1, 3, 0, new Size(50, 50), new Size());
            Rect[] rects = rect.toArray();
            for (Rect r : rects) {
                Imgproc.rectangle(mat, r.tl(), r.br(), new Scalar(0, 0, 255), 2, 8, 0);
            }
        }
        Mat result = new Mat();
        Imgproc.cvtColor(mat, result, Imgproc.COLOR_BGR2RGBA);
        Bitmap bitmap = Bitmap.createBitmap(result.width(), result.height(), Config.ARGB_8888);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
        mat.release();
        result.release();
        imread.release();
        gray.release();
        mat.release();
    }

    private static class InnerHandler extends Handler {
        private WeakReference<CP5Activity> activityRef;

        public InnerHandler(CP5Activity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String msgStr = (String) msg.obj;
                    if (getActivity() != null) {
                        getActivity().setMsg(msgStr);
                    }
                    break;

            }
        }

        private CP5Activity getActivity() {
            if (activityRef != null) {
                if (activityRef.get() != null) {
                    return activityRef.get();
                }
            }
            return null;
        }
    }

    private void setMsg(String msgStr) {

    }
}
