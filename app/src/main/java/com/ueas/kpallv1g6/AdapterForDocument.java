package com.ueas.kpallv1g6;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//An extension to the BaseAdapter class that allows the particular
//characteristics of the rows in a List View to be populated with
//multiple pieces of data. 
//
/**
 * Populates each row of a List View with multiple data items, each related to a selectable chapter.
 * 
 * @author tony.hillman@ultra-as.com
 * 
 */
public class AdapterForDocument extends BaseAdapter 
{ 
	/**
	 * A string used for LogCat output from the onCreate method.
	 * 
	 */
	private final static String TAG0 = "AfS:onCreate";
	
	/**
	 * An activity, to be specified as the current activity during UI inflation.
	 * 
	 */
    private Activity activity;
    
    /**
     * A dynamically extensible array list of string pairings, from which keys and values
     * can be extracted, in order to populate a GUI with values.
     * 
     */
    private ArrayList<HashMap<String, String>> data;
    
    /**
     * A Layout inflater.
     * 
     */
    private static LayoutInflater inflater=null;
    
    /**
     * An image loader.
     * 
     */
    public ImageLoader imageLoader; 
    
	/**
	 * The constructor for the AdapterForDocument class. Instantiates a LayoutInflater, so that an 
	 * xml-defined row can be populated with data; and an ImageLoader, so that a thumbnail image 
	 * can be displayed in the row.
	 * 
	 * @param activity An activity in which the LazyAdapter will be used.
	 *            
	 * @param data An array list of HashMap objects, each of which is a pairing
	 *             of strings. Each pairing represents a key and a value that  
	 *             will be used in the process whereby data elements are  
	 *             allocated to the row.  
	 *            
	 */
    public AdapterForDocument(Activity a, ArrayList<HashMap<String, String>> d) 
    {
    	// These are global variables, defined above.
    	//
    	activity = a;
        data = d;
        
        // An inflater is used to transform the content of an xml resource file into
        // a dynamically-modifiable object in java space. So, if we apply the inflater
        // to, say, a list-row, we are able to access every component in that
        // row programmatically.
        //
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

	/**
	 * Determines the size of the array list with which an adapter is
	 * initialized. This corresponds to the number of rows to be populated.
	 * 
	 * @return int An integer that is the size of the array list.
	 * 
	 */
    public int getCount() 
    {
        return data.size();
    }

	/**
	 * Returns an object that corresponds to a specified position in an array
	 * list.
	 * 
	 * @param position An integer that is the position.   
	 *         
	 * @return The object corresponding to the position.
	 * 
	 */
    public Object getItem(int position) 
    {
        return position;
    }

	/**
	 * Returns a specified array list position as a long.
	 * 
	 * @param position An integer that is the position. 
	 *       
	 * @return A long that is the position.
	 * 
	 */
    public long getItemId(int position) 
    {
        return position;
    }
    
	/**
	 * Ascribes values to each UI element within an inflated xml-defined row.
	 * It is called by the List View object once for each row to be populated.
	 * 
	 * @param position An integer that indicates which row we are currently populating.         
	 *                 
	 * @param convertView The row to be populated.
	 *            
	 * @param parent The parent view of the row to be populated.
	 *            
	 * @return A view that is the fully populated row.
	 * 
	 */ 
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        View vi=convertView;
        
        // Establish the content view for this activity. Do this conditionally, based on the screen-size/aspect-ratio
 		// combination we have already determined.
 		//
         if (convertView == null)
         {
 			switch (MetaDataForSuite.currentAspectRatio)
 			{
 				// This is a 480x800 4.3" device.
 				//
 				case 0:
 					vi = inflater.inflate(R.layout.list_row_content_for_document_for_zero___four_point_three, null);
 					Log.w(TAG0, "Inflated document row definition for 0.\n");
 					break;	
 					
 				// This is a 540x960 4.0" to 4.8" device.
 		        //
 		        case 10:
 		        	vi = inflater.inflate(R.layout.list_row_content_for_document_for_one_zero___four_point_zero_etc, null);
 					Log.w(TAG0, "Inflated document row definition for 10.\n");
 					break;
 					
	    		// This is a 720x1280 4.3" to 5.0" device.
	        	//
	        	case 20:
	        		vi = inflater.inflate(R.layout.list_row_content_for_document_for_two_zero___four_point_three_etc, null);
	        		Log.w(TAG0, "Inflated document row definition for 20.\n");
	        		break;	
	        		
    			// This is a 768x1280 4.7" device.
    		    //
    			case 30:
    				vi = inflater.inflate(R.layout.list_row_content_for_document_for_three_zero___four_point_seven, null);
    				Log.w(TAG0, "Inflated document row definition for 30.\n");
    				break;
    				
				// This is a 800x1280 7" device.
			    //
				case 40:
					vi = inflater.inflate(R.layout.list_row_content_for_document_for_four_zero___seven, null);
					Log.w(TAG0, "Inflated document row definition for 40.\n");
					break;
					
				// This is a 800x1280 10.1" device.
			    //
				case 50:
					vi = inflater.inflate(R.layout.list_row_content_for_document_for_five_zero___ten, null);
					Log.w(TAG0, "Inflated dialog row definition for 50.\n");
					break;
    		
 			    // This is a 1080x1920 generic-size device.
 				//
 				case 60:
 					vi = inflater.inflate(R.layout.list_row_content_for_document_for_six_zero___gen, null);
 					Log.w(TAG0, "Inflated document row definition for 60.\n");
 					break;
 				
 				// This is a 1080x1920 5.7 inch device.
 				//
 				case 70:
 					vi = inflater.inflate(R.layout.list_row_content_for_document_for_seven_zero___five_point_seven, null);
 					Log.w(TAG0, "Inflated document row definition for 70.\n");
 					break;
 				
 				// This is a 1200x1920 7 inch device.
 				//
 				case 80:
 					vi = inflater.inflate(R.layout.list_row_content_for_document_for_eight_zero___seven, null);
 					Log.w(TAG0, "Inflated document row definition for 80.\n");
 					break;
 				
 				// This is a 1200x1920 10 inch device.
 				//
 				case 90:
 					vi = inflater.inflate(R.layout.list_row_content_for_document_for_nine_zero___ten, null);
 					Log.w(TAG0, "Inflated document row definition for 90.\n");
 					break;

 	        	// This is a 1440x900 7 inch device.
 	        	//	
 	        	case 100:
 	        		vi = inflater.inflate(R.layout.list_row_content_for_document_for_one_zero_zero___seven, null);
 	        		Log.w(TAG0, "Inflated document row definition for 100.\n");
 	        		break;

				// This is a 1440x2560 5.1 inch device.
				//
				case 110:
					vi = inflater.inflate(R.layout.list_row_content_for_document_for_one_one_zero___five_point_one, null);
					Log.w(TAG0, "Inflated document row definition for 110.\n");
					break;
 	        		
 				// If we are not sure, we use the following default.
 				//
 				default:
 					vi = inflater.inflate(R.layout.list_row_content_for_document_for_six_zero___gen, null);
 					Log.w(TAG0, "Inflated document row definition for default.\n");
 					break;			
 			}
         }  

        // Now go through all of the views we know to be defined in list_row, and
        // make a java object of each of them individually. This is done according
        // to the unique id of each list_row view element.
        //
        TextView selected_chapter_id = (TextView)vi.findViewById(R.id.hidden_id); 
        
        // The title of thechapter.
        //
        TextView title = (TextView)vi.findViewById(R.id.chap_title); 
        
        // The description of the chapter.
        //
        TextView artist = (TextView)vi.findViewById(R.id.description); 

        // The thumbnail image for the chapter.
        //
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.thumbnail); 
        
        // Get the particular hash map for the selection that the user has made, in
        // CustomizedContentListViewForBook.java.
        //
        HashMap<String, String> selected_chapter = new HashMap<String, String>();
        selected_chapter = data.get(position);
        
        // Set all of the values in the current list row object according to the
        // values that are in the hash map we've received. 
        //
        selected_chapter_id.setText(selected_chapter.get(ListViewForDocumentActivity.KEY_ID));
        
        title.setText(selected_chapter.get(ListViewForDocumentActivity.KEY_CHAP_TITLE));
        
        artist.setText(selected_chapter.get(ListViewForDocumentActivity.KEY_DESCRIPTION));
        
        imageLoader.DisplayImage(selected_chapter.get(ListViewForDocumentActivity.KEY_THUMBNAIL), thumb_image);
        
        // Return the fully initialized row.
        //
        return vi;
    }
}