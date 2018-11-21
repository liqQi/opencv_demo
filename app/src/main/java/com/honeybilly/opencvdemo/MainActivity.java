package com.honeybilly.opencvdemo;

import android.Manifest;
import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.honeybilly.opencvdemo.utils.BitmapUtils;
import com.honeybilly.opencvdemo.utils.FileUtils;
import com.honeybilly.opencvdemo.utils.ImageUtils;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by liqi on 10:37.
 */
@RuntimePermissions
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_GET_IMAGE = 101;
    private ImageView iv;
    private ImageView ivTrans;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoadOpenCV();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        findViewById(R.id.get).setOnClickListener(v -> MainActivityPermissionsDispatcher.getFileFromPhotoWithPermissionCheck(this));
        findViewById(R.id.transform).setOnClickListener(v -> {
            Mat mat = BitmapUtils.drawable2Mat(iv.getDrawable());
            //Mat des = new Mat();
            //Imgproc.cvtColor(mat,des,Imgproc.COLOR_BGR2GRAY);
            Mat result = BitmapUtils.reverseColor(mat);
            Bitmap bitmap = BitmapUtils.matToBitmap(result);
            result.release();
            ivTrans.setImageBitmap(bitmap);
        });
        ivTrans = findViewById(R.id.iv_transform);
        iv = findViewById(R.id.iv);
    }

    private void initLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            toast("OpenCV加载成功");
        } else {
            toast("OpenCV加载失败");
        }
        Disposable subscribe = Observable.create(emitter -> {
            File dir = getDir("cascade", Context.MODE_PRIVATE);
            File file = new File(dir.getAbsoluteFile(), "data_source.xml");
            if(file.exists()){
                emitter.onNext("分类器已存在");
                return;
            }
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buff = new byte[1024];
                int len = 0;
                while ((len = is.read(buff)) != -1) {
                    fos.write(buff, 0, len);
                }
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onNext("分类器加载成功");
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
            Toast.makeText(MainActivity.this, (CharSequence) o, Toast.LENGTH_SHORT).show();
        }, Throwable::printStackTrace);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @NeedsPermission(permission.READ_EXTERNAL_STORAGE)
    void getFileFromPhoto() {
        ImageUtils.requestImage(this, REQUEST_GET_IMAGE);
    }

    @OnShowRationale(permission.READ_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this).setMessage("读取图片需要储存权限").setPositiveButton("好的", (dialog, button) -> request.proceed()).setNegativeButton("取消", (dialog, button) -> request.cancel()).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        toast("已拒绝");
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        toast("不再询问");
    }

    //content://media/external/images/media/517
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GET_IMAGE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        File file = FileUtils.getFile(this, uri);
                        Mat imread = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
                        iv.setImageBitmap(BitmapUtils.matToBitmap(BitmapUtils.BGR2RGBA(imread)));
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            MatCreateActivity.launch(this);
        } else if (id == R.id.nav_gallery) {
            SimpleTransFormActivity.launch(this);
        } else if (id == R.id.nav_slideshow) {
            FilterActivity.launch(this);
        } else if (id == R.id.nav_manage) {
            FeatureDetectionActivity.launch(this);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.camera) {
            MainActivityPermissionsDispatcher.startCameraActivityWithPermissionCheck(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @NeedsPermission({permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE})
    void startCameraActivity() {
        Intent intent = new Intent(this, SimpleFaceDetectActivity.class);
        startActivity(intent);
    }

    @OnShowRationale({permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleStartCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this).setMessage("需要相机权限").setPositiveButton("好的", (dialog, button) -> request.proceed()).setNegativeButton("取消", (dialog, button) -> request.cancel()).show();
    }

    @OnPermissionDenied({permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE})
    void showDeniedStartCamera() {
        toast("已拒绝");
    }

    @OnNeverAskAgain({permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE})
    void showNeverAskStartCamera() {
        toast("不再询问");
    }

}
