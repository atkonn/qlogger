package jp.co.qsdn.android.qlogger.cache;


import android.graphics.Bitmap;

import java.lang.ref.SoftReference;

import java.util.HashMap;

public class ImageCache {    
  private static HashMap<String,SoftReference<Bitmap>> cache = new HashMap<String,SoftReference<Bitmap>>();    
  public static Bitmap getImage(String key) {    
    SoftReference<Bitmap> ref = cache.get(key);  
    if (ref != null) {  
      return ref.get();  
    }  
    return null;    
  }    
           
  public static void setImage(String key, Bitmap image) {    
    cache.put(key, new SoftReference<Bitmap>(image));    
  }    
}
