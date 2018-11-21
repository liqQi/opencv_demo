package com.honeybilly.opencvdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.honeybilly.opencvdemo.utils.BitmapUtils;
import com.honeybilly.opencvdemo.utils.FileUtils;
import com.honeybilly.opencvdemo.utils.ImageUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CP3Activity extends AppCompatActivity {

    private static final String TAG = CP3Activity.class.getSimpleName();
    private ImageView ivSrc2;
    private EditText etScale;

    public static void launch(Context context) {
        Intent intent = new Intent(context, CP3Activity.class);

        context.startActivity(intent);
    }

    public static final int REQUEST_IMAGE = 101;
    public static final int REQUEST_IMAGE_2 = 102;
    protected ImageView ivSrc;
    protected ImageView ivDes;
    protected Bitmap bitmap;
    protected Bitmap bitmap2;
    protected String path1;
    protected String path2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp3);
        ivSrc = findViewById(R.id.src);
        ivSrc2 = findViewById(R.id.src2);
        ivDes = findViewById(R.id.des);
        etScale = findViewById(R.id.scale);
    }

    public void selectImage(View view) {
        ImageUtils.requestImage(this, REQUEST_IMAGE);
    }

    public void transform(View view) {
        xor();
    }
    public void transform2(View view) {
       yuCaoZuo();
    }

    /**
     * 异或
     */
    private void xor(){
        Mat img1 = Imgcodecs.imread(path1);
        Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Core.bitwise_xor(img1,img2,des);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
    }

    /**
     * 取反
     */
    private void quFan(){
        Mat img1 = Imgcodecs.imread(path1);
        Mat mat = new Mat();
        Core.bitwise_not(img1,mat);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(mat,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
    }

    /**
     * 与操作
     */
    private void yuCaoZuo(){
        Mat img1 = Imgcodecs.imread(path1);
        Mat img2 = Imgcodecs.imread(path2);
        Mat des = new Mat();
        Core.bitwise_and(img1,img2,des);
        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(des,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);

    }

    /**
     * 图像叠加
     */
    private void tuXiangDieJia(){
        String s = etScale.getText().toString();
        if(s.isEmpty()){
            return;
        }
        double a = Double.parseDouble(s);
        double b = 1.0-a;

        Mat img1 = Imgcodecs.imread(path1);
        Mat img2 = Imgcodecs.imread(path2);

        Mat mat = new Mat();
        Core.addWeighted(img1,a,img2,b,1,mat);

        Bitmap bitmap = Bitmap.createBitmap(img1.width(), img1.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(mat,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
    }

    /**
     * 乘
     */
    private void mutiply() {
        String s = etScale.getText().toString();
        if(s.isEmpty()){
            return;
        }
        double d = Double.parseDouble(s);
        //Mat mat = BitmapUtils.drawable2Mat(ivSrc.getDrawable());
        ////Mat color = new Mat();
        ////Imgproc.cvtColor(mat,color,Imgproc.COLOR_RGBA2BGR);
        //Mat dst = new Mat();
        //Core.multiply(mat,new Scalar(d,d,d,d),dst);
        ////Mat result = new Mat();
        ////Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        //ivDes.setImageBitmap(BitmapUtils.matToBitmap(dst));
        Mat imread = Imgcodecs.imread(path1);
        Mat dst = new Mat();
        Core.multiply(imread,new Scalar(d,d,d),dst);
        Bitmap bitmap = Bitmap.createBitmap(imread.width(), imread.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
    }

    /**
     * 提升指定亮度
     */
    private void addScalar(){
        String s = etScale.getText().toString();
        if(s.isEmpty()){
            return;
        }
        int d = Integer.parseInt(s);
        Mat imread = Imgcodecs.imread(path1);
        Mat dst = new Mat();
        Core.add(imread,new Scalar(d,d,d),dst);
        Bitmap bitmap = Bitmap.createBitmap(imread.width(), imread.height(), Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ivDes.setImageBitmap(bitmap);
    }

    /**
     * 简单叠加
     */
    private void add() {
        Mat mat = BitmapUtils.drawable2Mat(ivSrc.getDrawable());

        Mat moon = Mat.zeros(mat.rows(), mat.cols(), mat.type());

        int cx = mat.cols()-60;
        int cy = 60;

        Imgproc.circle(moon,new Point(cx,cy),50,new Scalar(30,30,30),-1,8,0);

        Mat dst = new Mat();
        Core.add(mat,moon,dst);

        ivDes.setImageBitmap(BitmapUtils.matToBitmap(dst));
    }

    private void erZhiHua() {
        Mat mat = BitmapUtils.drawable2Mat(ivSrc.getDrawable());
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY);
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(gray, mean, stddev);

        double[] doublesMean = mean.toArray();
        double[] doublesStddev = stddev.toArray();
        int width = gray.width();
        int height = gray.height();
        Log.d(TAG, "transform: "+width+" : "+height);
        byte[] data = new byte[width * height];
        gray.get(0, 0, data);

        int pixiv = 0;
        int t = (int) (doublesMean[0]);
        int length = data.length;
        for (int i = 0; i < length; i++) {
            pixiv = data[i]&0xff;
            if (pixiv > t) {
                data[i] = (byte) 255;
            } else {
                data[i] = (byte) 0;
            }
        }
        gray.put(0, 0, data);
        ivDes.setImageBitmap(BitmapUtils.matToBitmap(gray));
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
            }else if(requestCode == REQUEST_IMAGE_2){
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

    private void split(Mat mat) {
        List<Mat> mv = new ArrayList<>();
        Core.split(mat, mv);
        Mat alpha = mv.get(1);
        File filesDir = getFilesDir();
        File file = new File(filesDir, "red" + System.currentTimeMillis() + ".jpg");
        try {
            boolean newFile = file.createNewFile();
            if (newFile) {
                Imgcodecs.imwrite(file.getAbsolutePath(), alpha);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                ivDes.setImageBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectImage2(View view) {
        ImageUtils.requestImage(this, REQUEST_IMAGE_2);
    }
}
