package com.honeybilly.opencvdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.honeybilly.opencvdemo.utils.LocalProcesser;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by liqi on 17:26.
 */
public class SimpleFaceDetectActivity extends AppCompatActivity implements CvCameraViewListener2 {

    private JavaCameraView cameraView;
    private int cameraIndex = 1;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean success;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        cameraView = findViewById(R.id.cameraView);
        cameraView.setCvCameraViewListener(this);
        cameraView.setCameraIndex(cameraIndex);
        File dir = getDir("cascade", Context.MODE_PRIVATE);
        File file = new File(dir.getAbsoluteFile(), "data_source.xml");
        boolean b = LocalProcesser.getInstance().initLoad(file.getAbsolutePath());
        success = b;
        if(b){
            Toast.makeText(this,"success",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"fail",Toast.LENGTH_SHORT).show();
        }
        cameraView.enableView();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        if (cameraIndex != 0) {
            Core.flip(rgba, rgba, 1);
        }
        if(success) {
            LocalProcesser.getInstance().faceDetection(rgba.nativeObj);
        }
        return rgba;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
        compositeDisposable.dispose();
    }
}
