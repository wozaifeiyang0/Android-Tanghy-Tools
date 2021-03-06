package com.youyoucy.tanghy.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import com.youyoucy.tanghy.utils.ImageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Tanghy000 on 2015/1/28.
 */
public class ImageLoader {


    // 图片软引用
    private HashMap<String, SoftReference<Bitmap>> imageCache;
    // 显示图片的ImageView
    private HashMap<String, ImageView> imageViews;

    public ImageLoader() {// 构造
        imageCache = new HashMap<String, SoftReference<Bitmap>>();
        imageViews = new HashMap<String, ImageView>();
    }

    /**
     * 从网络上获取图片
     *
     * @param imageView
     *            显示图片的ImageView
     * @param imageUrl
     *            图片的地址
     * @param isCompress
     *            是否压缩图片
     * @return 图片
     */
    public Bitmap loadDrawableFromNet(final ImageView imageView,
                                      final String imageUrl,boolean isCompress) {
        return loadDrawable(imageView, imageUrl,isCompress, new LoadCallBack() {
            public Bitmap load(String uri) {
                return loadImageFromNet(uri);
            }
        });
    }

    /**
     * 从本地获取图片
     *
     * @param imageView
     *            显示图片的ImageView
     * @param imageUrl
     *            图片的路径
     * @param isCompress
     *            是否压缩图片
     * @return 图片
     */
    public Bitmap loadDrawableFromLocal(final ImageView imageView,
                                        final String imageUrl,final boolean isCompress) {
        return loadDrawable(imageView, imageUrl, isCompress,new LoadCallBack() {
            public Bitmap load(String uri) {
                return loadImageFromLocal(uri);
            }
        });
    }

    /**
     * 获取图片
     *
     * @param imageView
     *            显示图片的ImageView
     * @param imageUrl
     *            图片路径或网络地址
     * @param load
     *            回调方法 加载本地图片或者加载网络图片
     * @return
     */
    private Bitmap loadDrawable(final ImageView imageView,
                                final String imageUrl,final boolean isCompress, final LoadCallBack load) {

        // 判断软引用里是否有图片
        if (imageCache.containsKey(imageUrl)) {
            SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
            Bitmap bitmap = softReference.get();
            if (bitmap != null) {
                return bitmap;// 有则返回
            }
        }

        // 将为添加到图片显示集合的 ImageViwe 加入到集合
        if (!imageViews.containsKey(imageUrl)) {
            imageViews.put(imageUrl, imageView);
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageViews.get(imageUrl).setImageBitmap((Bitmap) message.obj);
            }
        };

        //启动线程获取图片
        new Thread() {
            public void run() {
                Bitmap bitmap = load.load(imageUrl);//执行回调
                if (isCompress) bitmap = ImageUtils.compressImage(bitmap);//如果isCompress为真压缩图片
                imageCache.put(imageUrl, new SoftReference<Bitmap>(bitmap));
                Message message = handler.obtainMessage(0, bitmap);
                handler.sendMessage(message);
            }
        }.start();
        return null;
    }

    private interface LoadCallBack {
        public Bitmap load(String uri);
    }

    /**
     * 从网络加载图片
     *
     * @param url
     * @return
     */
    public Bitmap loadImageFromNet(String url) {
        Bitmap bitmap = null;
        URL m;
        InputStream i = null;
        try {
            m = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)m.openConnection();
            i = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(i);
            //关闭流
            if (i != null )i.close();
            //如果网络连接者断开连接
            if (conn != null)conn.disconnect();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 从本地加载图片
     *
     * @param path
     * @return
     */
    public Bitmap loadImageFromLocal(String path) {
        return BitmapFactory.decodeFile(path);
    }
}