LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := native-lib

# 添加你的源文件
LOCAL_SRC_FILES := interfaces.c functions.c native-lib.cpp
# 如果有其他源文件，可以继续添加，例如：
# LOCAL_SRC_FILES += native-lib.cpp

LOCAL_LDLIBS+= -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)