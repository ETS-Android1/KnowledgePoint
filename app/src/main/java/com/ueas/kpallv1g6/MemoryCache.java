package com.ueas.kpallv1g6;

// Based on a free internet demo by Ravi Tamada.
//

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.graphics.Bitmap;

/**
 * Creates and maintains a HashMap that contains string-bitmap pairs.
 * 
 * @author tony.hillman@ultra-as.com
 * 
 */
public class MemoryCache 
{
	/**
	 * A HashMap of string-bitmap pairs. The bitmaps are
	 * represented as soft references.
	 * 
	 */
    private Map<String, SoftReference<Bitmap>> cache=Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());
    
	/**
	 * Returns a bitmap paired with a specified string within a HashMap.
	 * 
	 * @param id
	 *            A string used to find the bitmap.
	 *            
	 * @return The bitmap associated with the specified string.
	 * 
	 */
    public Bitmap get(String id)
    {
        if(!cache.containsKey(id))
        {
            return null;
        }
        
        SoftReference<Bitmap> ref=cache.get(id);
        
        return ref.get();
    }
    
	/**
	 * Adds a string-bitmap pair to a HashMap.
	 * 
	 * @param id
	 *            The string to be associated with the bitmap.
	 *            
	 * @param bitmap
	 *            The bitmap to be associated with the string.
	 *            
	 */
    public void put(String id, Bitmap bitmap)
    {
        cache.put(id, new SoftReference<Bitmap>(bitmap));
    }

	/**
	 * Clears a HashMap.
	 * 
	 */
    public void clear() 
    {
        cache.clear();
    }
}