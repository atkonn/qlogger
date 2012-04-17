
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ps

LOCAL_SRC_FILES := ps.c
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
