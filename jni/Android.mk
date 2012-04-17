
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ps

LOCAL_SRC_FILES := ps.c

include $(BUILD_SHARED_LIBRARY)
