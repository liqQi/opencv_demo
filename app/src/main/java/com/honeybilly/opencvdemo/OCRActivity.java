package com.honeybilly.opencvdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.honeybilly.opencvdemo.utils.BitmapUtils;
import com.honeybilly.opencvdemo.utils.FileUtils;
import com.honeybilly.opencvdemo.utils.ImageUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liqi on 11:45.
 */
public class OCRActivity extends AppCompatActivity {
    private static final int REQUEST_SELECT = 100;
    private static final int REQUEST_CAPTURE = 101;
    private String path;
    private Bitmap bitmap;
    private ImageView ivSrc;
    private TextView tv;
    private TessBaseAPI tessBaseAPI;
    private LinearLayout container;
    private Mat corrent = null;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        innerHandler = new InnerHandler(this);
        container = findViewById(R.id.container);
        findViewById(R.id.select_img).setOnClickListener(v -> ImageUtils.requestImage(this, REQUEST_SELECT));
        findViewById(R.id.capture_img).setOnClickListener(v -> {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            path = file.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(OCRActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
            if (uri != null) {
                ImageUtils.requestCapture(uri, this, REQUEST_CAPTURE);
            }
        });
        ivSrc = findViewById(R.id.src);
        tv = findViewById(R.id.text);
        findViewById(R.id.transform).setOnClickListener(v -> {
            Disposable subscribe = Observable.create((ObservableOnSubscribe<Object>) emmiter -> {
                if (path == null || tessBaseAPI == null) {
                    return;
                }
                Mat inputSrc = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_COLOR);
                Message message = new Message();
                message.what = 1;
                message.obj = "开始读取";
                innerHandler.sendMessage(message);
                emmiter.onNext(inputSrc);
                Mat inputModel = Imgcodecs.imread("/storage/emulated/0/Android/data/com.honeybilly.opencvdemo/files/Pictures/model.png", Imgcodecs.CV_LOAD_IMAGE_COLOR);
                Mat input = new Mat();
                Mat model = new Mat();
                Imgproc.cvtColor(inputSrc, input, Imgproc.COLOR_BGRA2GRAY);
                Imgproc.cvtColor(inputModel, model, Imgproc.COLOR_BGRA2GRAY);
                emmiter.onNext(model);
                emmiter.onNext(input);
                message = new Message();
                message.what = 1;
                message.obj = "边缘检测";
                innerHandler.sendMessage(message);
                //Canny边缘检测
                Imgproc.Canny(input, input, 200, 400, 3, false);
                emmiter.onNext(input);
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierachy = new Mat();
                message = new Message();
                message.what = 1;
                message.obj = "查找轮廓";
                innerHandler.sendMessage(message);
                Imgproc.findContours(input, contours, hierachy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                int width = input.cols();
                int height = input.rows();
                Rect roiArea = null;
                for (int i = 0; i < contours.size(); i++) {
                    //List<Point> points = contours.get(i).toList();
                    Rect rect = Imgproc.boundingRect(contours.get(i));
                    if (rect.width < width && rect.width > (width / 2)) {
                        if (rect.height < (height / 4)) {
                            continue;
                        }
                        roiArea = rect;
                    }
                }
                if (roiArea == null) {
                    return;
                }
                message = new Message();
                message.what = 1;
                message.obj = "剪下最后一行";
                innerHandler.sendMessage(message);
                //剪下身份证的部分，并将其大小设为547*342
                Mat card = inputSrc.submat(roiArea);
                Imgproc.cvtColor(card,card,Imgproc.COLOR_BGRA2GRAY);
                emmiter.onNext(card);
                Size size = new Size(547, 342);
                Imgproc.resize(card, card, size);
                //准备模版匹配
                int resultCols = card.cols() - model.cols() + 1;
                int resultRows = card.rows() - model.rows() + 1;
                Mat numberLineSubMat = new Mat(resultRows, resultCols, CvType.CV_32FC1);
                message = new Message();
                message.what = 1;
                message.obj = "模版匹配";
                innerHandler.sendMessage(message);
                Imgproc.matchTemplate(card, model, numberLineSubMat, Imgproc.TM_CCORR_NORMED);
                Core.normalize(numberLineSubMat, numberLineSubMat, 0, 1, Core.NORM_MINMAX, -1);
                MinMaxLocResult minMaxLocResult = Core.minMaxLoc(numberLineSubMat);

                Point maxLoc = minMaxLocResult.maxLoc;
                Config config = Config.ARGB_8888;
                message = new Message();
                message.what = 1;
                message.obj = "查找数字部分";
                innerHandler.sendMessage(message);
                Rect idNumberRect = new Rect((int) (maxLoc.x + model.cols()*1.5f), (int) maxLoc.y, (int) (card.cols() - (maxLoc.x + model.cols()*1.5f)-40), (int) (model.rows() - 10));
                Mat idNumber = card.submat(idNumberRect);
                emmiter.onNext(idNumber);
                //Mat threshold = new Mat();
                //Imgproc.adaptiveThreshold(idNumber,threshold,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,15,10);
                //Imgproc.bilateralFilter(idNumber,threshold,0,150,15);
                //Imgproc.threshold(threshold,threshold,0,255,Imgproc.THRESH_OTSU);
                //emmiter.onNext(threshold);
                //Bitmap bitmap = Bitmap.createBitmap(threshold.width(), threshold.height(), config);
                //Utils.matToBitmap(threshold, bitmap);
                Bitmap bitmap = Bitmap.createBitmap(idNumber.width(), idNumber.height(), config);
                Utils.matToBitmap(idNumber, bitmap);
                message = new Message();
                message.what = 1;
                message.obj = "开始识别";
                innerHandler.sendMessage(message);
                tessBaseAPI.setImage(bitmap);
                String utf8Text = tessBaseAPI.getUTF8Text();
                emmiter.onNext(utf8Text);
                emmiter.onComplete();
            }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(text -> {
                if(text instanceof String) {
                    text = tv.getText().toString() + System.getProperty("line.separator") + text;
                    tv.setText((String) text);
                }else if(text instanceof Mat){
                    addIv((Mat) text);
                }
            }, Throwable::printStackTrace);
            compositeDisposable.add(subscribe);
        });
        initTrainingData();
    }

    private void ocrChiText() {
        if (path == null || tessBaseAPI == null) {
            return;
        }
        Disposable subscribe = Observable.just(path).flatMap((Function<String, ObservableSource<String>>) path -> Observable.create(emitter -> {
            Log.d(TAG, "start decode image");
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            tessBaseAPI.setImage(bitmap);
            Log.d(TAG, "start detect");
            String utf8Text = tessBaseAPI.getUTF8Text();
            Log.d(TAG, "complete");
            emitter.onNext(utf8Text);
        })).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(text -> tv.setText(text), Throwable::printStackTrace);
        compositeDisposable.add(subscribe);
    }

    private static final String TAG = OCRActivity.class.getSimpleName();

    private void initTrainingData() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            File file = getChiTrainData();
            if (!file.exists()) {
                boolean result = file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                InputStream inputStream = getResources().openRawResource(R.raw.nums_traineddata);
                byte[] bytes = new byte[1024];
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                }
                fos.flush();
                fos.close();
                inputStream.close();
            }
            tessBaseAPI = new TessBaseAPI();
            boolean zho = tessBaseAPI.init(getFilesDir().getAbsolutePath(), "nums");
            emitter.onNext(zho);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {

            if (o) {
                Toast.makeText(this, "init success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "init fail", Toast.LENGTH_SHORT).show();
            }
        }, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @NonNull
    private File getChiTrainData() {
        File file = new File(getFilesDir(), "tessdata/");
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(file, "nums.traineddata");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        File file = FileUtils.getFile(this, uri);
                        path = file.getAbsolutePath();
                        Mat mat = Imgcodecs.imread(file.getAbsolutePath());
                        Mat result = BitmapUtils.BGR2RGBA(mat);
                        bitmap = BitmapUtils.matToBitmap(result);
                        ivSrc.setImageBitmap(bitmap);
                    }
                }
            } else if (requestCode == REQUEST_CAPTURE) {
                File file = new File(path);
                Mat mat = Imgcodecs.imread(file.getAbsolutePath());
                Mat result = BitmapUtils.BGR2RGBA(mat);
                bitmap = BitmapUtils.matToBitmap(result);
                ivSrc.setImageBitmap(bitmap);
            }
        }
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, OCRActivity.class);

        context.startActivity(intent);
    }

    private static class InnerHandler extends Handler {
        private WeakReference<OCRActivity> activityRef;

        public InnerHandler(OCRActivity activity) {
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
                case 2:
                    Mat mat = (Mat) msg.obj;
                    if (getActivity() != null) {
                        getActivity().addIv(mat);
                    }
                    Log.d(TAG, "handleMessage: 2");
                    break;

            }
        }

        private OCRActivity getActivity() {
            if (activityRef != null) {
                if (activityRef.get() != null) {
                    return activityRef.get();
                }
            }
            return null;
        }
    }

    private void addIv(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().density * 160));
        iv.setLayoutParams(lp);
        iv.setImageBitmap(bitmap);
        container.addView(iv);
    }

    private InnerHandler innerHandler;

    private void setMsg(String msgStr) {
        String text = tv.getText().toString() + System.getProperty("line.separator") + msgStr;
        tv.setText(text);

    }
}
