LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
include E:/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
LOCAL_MODULE := local-processer
LOCAL_SRC_FILES := com_honeybilly_opencvdemo_utils_LocalProcesser.cpp
LOCAL_LDLIBS += -llog -ldl
include $(BUILD_SHARED_LIBRARY)