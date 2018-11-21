package com.honeybilly.opencvdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.honeybilly.opencvdemo.utils.BitmapUtils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MatCreateActivity extends AppCompatActivity {

    private EditText etWidth;
    private EditText etHeight;
    private EditText etColor;
    private Button btnCreate;
    private ImageView iv;

    public static void launch(Context context){
        Intent intent = new Intent(context,MatCreateActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp1);
        etWidth = findViewById(R.id.width);
        etHeight = findViewById(R.id.height);
        etColor = findViewById(R.id.color);
        btnCreate = findViewById(R.id.create);
        iv = findViewById(R.id.iv);
        btnCreate.setOnClickListener(view->{
            String widthstr = etWidth.getText().toString();
            String heightstr = etHeight.getText().toString();
            String colorstr = etColor.getText().toString();
            int width = Integer.parseInt(widthstr);
            int height = Integer.parseInt(heightstr);
            int color = Integer.parseInt(colorstr);
            Mat mat = new Mat();
            mat.create(width,height, CvType.CV_8UC3);
            mat.setTo(new Scalar(color,color,color));
            Bitmap bitmap = BitmapUtils.matToBitmap(mat);
            mat.release();
            iv.setImageBitmap(bitmap);
        });
    }
}
