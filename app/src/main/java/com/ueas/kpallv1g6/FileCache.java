package com.ueas.kpallv1g6;

// Note: This file is based closely on a demo by Ravi Tamada.        
// 
//
import java.io.File;
import android.content.Context;

/**
 * Saves image files to local storage and retrieves them. 
 * 
 * @author tony.hillman@ultra-as.com
 * 
 */
public class FileCache 
{
    private File cacheDir;
    
	/**
	 * Finds or creates a directory in which images can be saved.
	 * 
	 * @param context The context in which image-cacheing is required.          
	 *            
	 */
    public FileCache(Context context)
    {
        // Find the directory in which images will be cached.
    	//
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(), "LazyList");
        }
        else
        {
            cacheDir=context.getCacheDir();
        }
        
        if (!cacheDir.exists())
        {
            cacheDir.mkdirs();
        }
    }
    
	/**
	 * Obtains a file from a URL, saves the file into the cache directory, and
	 * returns the file.
	 * 
	 * @param url
	 *            A string that specifies the url of the file.
	 *            
	 * @return The file that has been saved in the cache directory.
	 * 
	 */
    public File getFile(String url)
    {
        String filename=String.valueOf(url.hashCode());

        File f = new File(cacheDir, filename);
        return f;
    }
    
	/**
	 * Removes all files from the cache directory.
	 * 
	 */
    public void clear()
    {
        File[] files=cacheDir.listFiles();
        
        if (files==null)
        {
            return;
        }
        
        for (File f:files)
        {
            f.delete();
        }
    }
}