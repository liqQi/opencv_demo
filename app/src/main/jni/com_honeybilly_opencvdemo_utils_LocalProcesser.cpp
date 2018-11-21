//
// Created by Administrator on 2018/11/20.
//
#include<com_honeybilly_opencvdemo_utils_LocalProcesser.h>

using namespace cv;
using namespace std;

CascadeClassifier face_detector;

JNIEXPORT void JNICALL
Java_com_honeybilly_opencvdemo_utils_LocalProcesser_faceDetection(JNIEnv *env, jobject,
                                                                  jlong addrRgba) {
    Mat &mRgb = *(Mat *) addrRgba;
    Mat gray;
    cvtColor(mRgb, gray, COLOR_BGR2GRAY);
    Mat scale;
    pyrDown(gray, scale, Size(gray.cols / 2, gray.rows / 2));
    pyrDown(scale, scale, Size(scale.cols / 2, scale.rows / 2));
    vector<Rect> faces;
    face_detector.detectMultiScale(scale, faces, 1.1, 3, 0, Size(60, 60), Size(160, 160));
    if (faces.empty()) return;
    for (int i = 0; i < faces.size(); i++) {
        Rect rect = faces[i];
        rect.width *= 4;
        rect.height *= 4;
        rect.x = static_cast<int>(gray.cols * (rect.x / (float) scale.cols));
        rect.y = static_cast<int>(gray.rows * (rect.y / (float) scale.rows));
        rectangle(mRgb, rect, Scalar(255, 0, 0), 2, 8, 0);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_honeybilly_opencvdemo_utils_LocalProcesser_initLoad(JNIEnv *env, jobject,
                                                             jstring haarfilePath) {
    const char *nativeString = env->GetStringUTFChars(haarfilePath, 0);
    bool result = face_detector.load(nativeString);
    env->ReleaseStringUTFChars(haarfilePath, nativeString);
    return static_cast<jboolean>(result);
}

void scaleIntervalSampling(const Mat &src, Mat &dst, double xRatio, double yRatio) {
    //只处理uchar型的像素
    CV_Assert(src.depth() == CV_8U);

    // 计算缩小后图像的大小
    //没有四舍五入，防止对原图像采样时越过图像边界
    int rows = static_cast<int>(src.rows * xRatio);
    int cols = static_cast<int>(src.cols * yRatio);

    dst.create(rows, cols, src.type());

    const int channesl = src.channels();

    switch (channesl) {
        case 1: //单通道图像
        {
            uchar *p;
            const uchar *origal;

            for (int i = 0; i < rows; i++) {
                p = dst.ptr<uchar>(i);
                //四舍五入
                //+1 和 -1 是因为Mat中的像素是从0开始计数的
                int row = static_cast<int>((i + 1) / xRatio + 0.5) - 1;
                origal = src.ptr<uchar>(row);
                for (int j = 0; j < cols; j++) {
                    int col = static_cast<int>((j + 1) / yRatio + 0.5) - 1;
                    p[j] = origal[col];  //取得采样像素
                }
            }
            break;
        }

        case 3://三通道图像
        {
            Vec3b *p;
            const Vec3b *origal;

            for (int i = 0; i < rows; i++) {
                p = dst.ptr<Vec3b>(i);
                int row = static_cast<int>((i + 1) / xRatio + 0.5) - 1;
                origal = src.ptr<Vec3b>(row);
                for (int j = 0; j < cols; j++) {
                    int col = static_cast<int>((j + 1) / yRatio + 0.5) - 1;
                    p[j] = origal[col]; //取得采样像素
                }
            }
            break;
        }
    }
}

