package com.baima.mediaplayer.util;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageUtil {
    /**
     * 获取可用的存储卡路径
     * 通过反射
     *
     * @param context
     * @return
     */
    public static List<String> getMountedPaths(Context context) {
        List<String> mountedPaths = new ArrayList<>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<? extends StorageManager> aClass = storageManager.getClass();

        try {
            //获取所有
            Method getVolumePaths = aClass.getMethod("getVolumePaths");
            String[] volumePaths = (String[]) getVolumePaths.invoke(storageManager);

            //如果存储卡可用就添加到集合
            Method getVolumeState = aClass.getMethod("getVolumeState", String.class);
            for (String volumePath : volumePaths) {
                String volumeState = (String) getVolumeState.invoke(storageManager, volumePath);
                if (volumeState.equals(Environment.MEDIA_MOUNTED) || volumeState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                    mountedPaths.add(volumePath);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return mountedPaths;
    }
}
