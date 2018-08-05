package com.ueas.kpallv1g6;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * A variety of utility methods, used throughout the
 * application.
 * 
 * @author tony.hillman@ultra-as.com
 *
 */
public class Utils 
{	
	/**
	 * Tag string for writing to the LogCat.
	 * 
	 */
	private final static String TAG0 = "Utils: ";
	
	/**
	 * Copies the contents of one stream to another.
	 * 
	 * @param is The stream from which contents are to be read.
	 * 
	 * @param os The stream to which contents are to be written.
	 * 
	 */
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        
        try
        {
            byte[] bytes=new byte[buffer_size];
            
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
	/**
	 * Fetches an image file and creates a bitmap from it.
	 * 
	 * @param urlstr
	 *            The URL location of the image to be transformed.
	 *            
	 * @return 
	 * The bitmap created from the image that was fetched.
	 * 
	 */
    public static Bitmap fetchImage( String urlstr )
    {
    	// If we are accessing data over the network...
    	//
    	if (MetaDataForSuite.Connectivity == 0)
    	{
	        try
	        {
	            java.net.URL url;
	            
	            url = new java.net.URL( urlstr );
	            
	            Log.w(TAG0, "Opening URL connection.");
	            HttpURLConnection c = ( HttpURLConnection ) url.openConnection();
	            
	            if (c == null)
	            { 
	            	Log.w(TAG0, "The connection is null.");
	            }
	            
	            Log.w(TAG0, "Setting connection to do input.");
	            
	            c.setDoInput( true );
	            
	            Log.w(TAG0, "Connecting on connection.");
	            
	            try 
	            {
	            	c.connect();
	            } 
	            catch ( IOException e)
	            {
	            	Log.w(TAG0, "IO Exception on connect.");
	            }
	            
	            Log.w(TAG0, "Getting input stream.");
	            InputStream is = c.getInputStream();
	            Bitmap img;
	            
	            Log.w(TAG0, "Decoding stream.");
	            img = BitmapFactory.decodeStream( is );
	            Log.w(TAG0, "Returning bitmap image from utils.");
	            return img;
	        }
	        catch ( MalformedURLException e )
	        {
	            Log.w( "RemoteImageHandler", "fetchImage passed invalid URL: " + urlstr );
	        }
	        catch ( IOException e )
	        {
	            Log.w( "RemoteImageHandler", "fetchImage IO exception: " + e );
	        }
        return null;
    	}
    	
    	// Otherwise, if we are accessing local data...
    	//
    	else
    	{
    		Bitmap bitmap = null;
    		
	        bitmap = BitmapFactory.decodeFile(urlstr);
	        
	        if (bitmap == null)
	        {
	        	Log.w(TAG0, "Bitmap is NULL...");
	            return null;
	        }
	            
	        return bitmap;
    	}
    }
    
    /**
     * Unbinds specified drawable bitmaps from their allocated layout
     * elements.
     * 
     * @param view	The view with which the bitmap has been associated.
     */
	public static void unbindDrawables(View view)
	{
		if (view.getBackground() != null)
		{
			view.getBackground().setCallback(null);
		}
		
		if (view instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			{
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}
	
	/**
	 * Writes an array of bytes to disk.
	 * 
	 * @param fullPath
	 *            A string that is the full path of the file to which the bytes
	 *            are written.
	 * @param bytes
	 *            The bytes to be written to disk.
	 *            
	 * @throws IOException
	 * 
	 */	
	public static void writeFileAsBytes(String fullPath, byte[] bytes) 
			throws IOException
	{
	  OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fullPath));
	  InputStream inputStream = new ByteArrayInputStream(bytes);
	  
	  int token = -1;

	  while((token = inputStream.read()) != -1)
	  {
	    bufferedOutputStream.write(token);
	  }
	  
	  bufferedOutputStream.flush();
	  bufferedOutputStream.close();
	  
	  inputStream.close();
	}
}