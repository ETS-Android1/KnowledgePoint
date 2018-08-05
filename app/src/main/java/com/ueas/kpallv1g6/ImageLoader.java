package com.ueas.kpallv1g6;

// Note: This file based closely on a demo by Ravi Tamada.
//

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

/**
 * Accesses image files and transforms them to bitmaps.
 * 
 * @author tony.hillman@ultra-as.com
 * 
 */
public class ImageLoader 
{
    MemoryCache memoryCache = new MemoryCache();
    
    FileCache fileCache;
    
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    
    ExecutorService executorService; 
    
	/**
	 * Creates a fileCache for the specified context and establishes a pool of
	 * five threads.
	 * 
	 * @param context 
	 * 			The context for which a fileCache is to be created.
	 *            
	 */
    public ImageLoader(Context context)
    {
        fileCache = new FileCache(context);
        
        executorService = Executors.newFixedThreadPool(5);
    }
    
    /**
     * The data member stub_id is a resource ID that indicates a default image 
     * to be displayed when a sought image cannot be located.
     * 
     */
    final int stub_id = R.drawable.no_image;
    
	/**
	 * Associates an image with a specified ImageView, transforms the image into
	 * a bitmap, and displays it at the location of the specified ImageView. If
	 * an image cannot immediately be located, DisplayImage temporarily displays
	 * a default image and continues to seek the preferred image through a
	 * concurrency mechanism.
	 * 
	 * @param url
	 *            A string that is the url of the image to be displayed.
	 *            
	 * @param imageView
	 *            The image view to be associated with the image.
	 *            
	 *            
	 */
    public void DisplayImage(String url, ImageView imageView)
    {
        imageViews.put(imageView, url);
        
        Bitmap bitmap = memoryCache.get(url);
        
        if (bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            queuePhoto(url, imageView);
            
            imageView.setImageResource(stub_id);
        }
    }
        
	/**
	 * Employs a concurrency mechanism to search for an image that has initially
	 * proved unavailable.
	 * 
	 * @param url
	 *            A string that is the location of the sought image.
	 *            
	 * @param imageView
	 *            The ImageView at whose location the sought image is to be
	 *            displayed.
	 *            
	 */
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
	/**
	 * Retrieves a bitmap based on a specified url. If the image has not already
	 * been cached locally as a bitmap, the image is retrieved from the url,
	 * transformed to a bitmap, and returned.
	 * 
	 * @param url
	 *            The url that was the original location of the image.
	 *            
	 * @return A bitmap derived from the image.
	 * 
	 */
    public Bitmap getBitmap(String url) 
    {
        File f=fileCache.getFile(url);
        
        // If data-access is across the network...
        //
        if (MetaDataForSuite.Connectivity == 0)
        {
            try 
            {
                Bitmap bitmap=null;
                
                URL imageUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
                
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                
                InputStream is=conn.getInputStream();
                OutputStream os = new FileOutputStream(f);
                
                Utils.CopyStream(is, os);
                os.close();
                
                bitmap = decodeFile(f);
                return bitmap;
            } 
            catch (Exception ex)
            {
               ex.printStackTrace();
               return null;
            }
        }
        
        // Otherwise, if data-access is local...
        //
        else
        {
            try 
            {
                Bitmap bitmap=null;

                bitmap = BitmapFactory.decodeFile(url);
                if (bitmap == null)
                {
                	//Log.w(TAG0, "Bitmap is NULL...");
                }
                return bitmap;
            } 
            catch (Exception ex)
            {
               ex.printStackTrace();
               return null;
            }
        }   
    }

	/**
	 * Decodes an images and scales it to reduce memory consumption.
	 * 
	 * @param f
	 *            The file to be decoded and reduced.
	 *            
	 * @return The bitmap resulting from the decoding and reduction.
	 * 
	 */
    private Bitmap decodeFile(File f)
    {
        try 
        {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            // Find the correct scale value. It should be the power of 2.
            //
            final int REQUIRED_SIZE=70;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            
            while(true)
            {
                if (width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                {
                    break;
                }
                
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            //
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } 
        catch (FileNotFoundException e) {}

        return null;
    }
    
	/**
	 * Stores data associated with an image that could not initially be found,
	 * and so will continue to be sought through a concurrency mechanism.
	 * 
	 */
    private class PhotoToLoad
    {
        public String url;
        
        public ImageView imageView;
        
        public PhotoToLoad(String u, ImageView i)
        {
            url = u;
            
            imageView = i;
        }
    }
    
	/**
	 * Causes a hitherto unavailable image to be sought through concurrency.
	 * 
	 * @author anon
	 * 
	 */
    class PhotosLoader implements Runnable 
    {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad)
        {
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() 
        {
            if(imageViewReused(photoToLoad))
            {
                return;
            }
            
            Bitmap bmp=getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            
            if(imageViewReused(photoToLoad))
            {
                return;
            }
            
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            
            a.runOnUiThread(bd);
        }
    }
    
	/**
	 * Specifies whether an ImageView previously intended to display an image
	 * has subsequently been used for a different image.
	 * 
	 * @param photoToLoad
	 *            The PhotoToLoad object that corresponds to the originally
	 *            sought image.
	 *            
	 * @return Either true, indicating that the ImageView has been reused, or
	 *         false.
	 *         
	 */
    boolean imageViewReused(PhotoToLoad photoToLoad)
    {
        String tag=imageViews.get(photoToLoad.imageView);
        
        if(tag==null || !tag.equals(photoToLoad.url))
        {
            return true;
        }
        
        return false;
    }
    
	/**
	 * Displays a bitmap that was not immediately available in an ImageView,
	 * provided that the ImageView has not been reused.
	 * 
	 * @author anon
	 * 
	 */
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
            {
                return;
            }
            
            if(bitmap!=null)
            {
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
            else
            {
                photoToLoad.imageView.setImageResource(stub_id);
            }
        }
    }

    /**
     * Clears memory and file caches.
     * 
     */
    public void clearCache() 
    {
        memoryCache.clear();
        
        fileCache.clear();
    }
}
