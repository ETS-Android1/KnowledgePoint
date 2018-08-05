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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Populates a grid view with multiple data items, each related to a selectable video.
 * 
 * @author tony.hillman@ultra-as.com
 * 
 */
public class AdapterForVideo extends BaseAdapter 
{
	/**
	 * A string used for LogCat output from the onCreate method.
	 * 
	 */
	private final static String TAG0 = "AfS:onCreate";
	
	/**
     * An image loader.
     * 
     */
    public ImageLoader imageLoader; 
    
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
    
    public AdapterForVideo(Activity a, ArrayList<HashMap<String, String>> d) 
    {
    	activity = a;
        data=d;
        
        // An inflater is used to transform the content of an xml resource file into
        // a dynamically-modifiable object in java space. So, if we apply the inflater
        // to, say, a list-row, we should be able to access every component in that
        // row programmatically.        
        
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);        
        
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }
 
    @Override
    public int getCount() {
    	return data.size();
    }
 
    @Override
    public Object getItem(int position) {
        return position;
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {  
    	 View vi=convertView; 
    	/*
        ImageView imageView = new ImageView(mContext);  
        imageView.setImageResource(mThumbIds[position]);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(70, 70));    
        */
        
        // Establish the content view for the video activity. Do this conditionally, based on the 
  		// screen-size/aspect-ratio combination we have already determined.
  		//
        if (convertView == null)
          {
        	switch (MetaDataForSuite.currentAspectRatio)
        	{
        		// This is a 480x800 4.3" device.
				//
			case 0:
				vi = inflater.inflate(R.layout.list_row_content_for_video_for_zero___four_point_three, null);
				Log.w(TAG0, "Inflated video row definition for 0.\n");
				break;
				
				// This is a 540x960 4.0" to 4.8" device.
				//
	        case 10:
	        	vi = inflater.inflate(R.layout.list_row_content_for_video_for_one_zero___four_point_zero_etc, null);
				Log.w(TAG0, "Inflated video row definition for 10.\n");
				break;
				
    		// This is a 720x1280 4.3" to 5.0" device.
        	//
        	case 20:
        		vi = inflater.inflate(R.layout.list_row_content_for_video_for_two_zero___four_point_three_etc, null);
        		Log.w(TAG0, "Inflated video row definition for 20.\n");
        		break;
        		
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				vi = inflater.inflate(R.layout.list_row_content_for_video_for_three_zero___four_point_seven, null);
				Log.w(TAG0, "Inflated video row definition for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				vi = inflater.inflate(R.layout.list_row_content_for_video_for_four_zero___seven, null);
				Log.w(TAG0, "Inflated video row definition for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				vi = inflater.inflate(R.layout.list_row_content_for_video_for_five_zero___ten, null);
				Log.w(TAG0, "Inflated dialog row definition for 50.\n");
				break;
				
				// This is a 1080x1920 generic-size device.
				//
        	case 60:
        		vi = inflater.inflate(R.layout.list_row_content_for_video_for_six_zero___gen, null);
        		Log.w(TAG0, "Inflated video row definition for 60.\n");
        		break;

        		// This is a 1080x1920 5.7 inch device.
        		//
        	case 70:
        		vi = inflater.inflate(R.layout.list_row_content_for_video_for_seven_zero___five_point_seven, null);
        		Log.w(TAG0, "Inflated video row definition for 70.\n");
        		break;

        		// This is a 1200x1920 7 inch device.
        		//
        	case 80:
        		vi = inflater.inflate(R.layout.list_row_content_for_video_for_eight_zero___seven, null);
        		Log.w(TAG0, "Inflated video row definition for 80.\n");
        		break;

        		// This is a 1200x1920 10 inch device.
        		//
        	case 90:
        		vi = inflater.inflate(R.layout.list_row_content_for_video_for_nine_zero___ten, null);
        		Log.w(TAG0, "Inflated video row definition for 90.\n");
        		break;
        		
	        	// This is a 1440x900 7 inch device.
 	        	//	
        	case 100:
        		vi = inflater.inflate(R.layout.list_row_content_for_video_for_one_zero_zero___seven, null);
        		Log.w(TAG0, "Inflated video row definition for 100.\n");
        		break;

				// This is a 1440x2560 5.1 inch device.
				//
			case 110:
				vi = inflater.inflate(R.layout.list_row_content_for_video_for_one_one_zero___five_point_one, null);
				Log.w(TAG0, "Inflated video row definition for 110.\n");
				break;

        		// If we are not sure, we use the following default.
        		//
        	default:
        		vi = inflater.inflate(R.layout.list_row_content_for_video_for_six_zero___gen, null);
        		Log.w(TAG0, "Inflated video row definition for default.\n");
        		break;			
        	}
          } 
        
        // The title.
        //
        //TextView title = (TextView)vi.findViewById(R.id.singleItem); 
        
        //title.setText(strings[position]);
        
       // TextView description = (TextView)vi.findViewById(R.id.finePrint);
        
        //description.setText("This is a string of stand-in text, "
        //		+ "used to demonstrate the appearance of the description within each "
        //		+ "element of the grid.");
        
  
        TextView video_id = (TextView)vi.findViewById(R.id.hidden_id); 
        
        // The thumb image.
        //
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.thumbnail);   
        
        // The title.
        //
        TextView title = (TextView)vi.findViewById(R.id.title); 
        
        // The chapter reference.
        //
        TextView description = (TextView)vi.findViewById(R.id.description); 
        
        // The description.
        //
        TextView duration = (TextView)vi.findViewById(R.id.duration);       
        
        // Set all of the values in the current list row object according to the
        // values that are in the hash map we've received. 
        //
        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);

        video_id.setText(song.get(GridViewForVideoActivity.KEY_ID));     
        
        imageLoader.DisplayImage(song.get(GridViewForVideoActivity.KEY_THUMB_URL), thumb_image); 

        title.setText(song.get(GridViewForVideoActivity.KEY_TITLE));   

        description.setText(song.get(GridViewForVideoActivity.KEY_DESCRIPTION));       
        
        // The final three grid-elements feature no duration-figure, so need no accompanying
        // textual caption, and no movie-camera icon.  
        //
        String durationText = "Duration, ";        
        
        if ( 
        		(song.get(GridViewForVideoActivity.KEY_TITLE).equals("To the UltraAPEX User Guide")) ||
        		(song.get(GridViewForVideoActivity.KEY_TITLE).equals("To the Welcome Page")) ||
        		(song.get(GridViewForVideoActivity.KEY_TITLE).equals("Exit the Application")) 	
        		)
        {
        	// This will be blank.
        	//
        	duration.setText(song.get(GridViewForVideoActivity.KEY_DURATION));
        	
        	// Display a signpost, rather than the movie-camera image.
            //
            ImageView movieCameraImage = (ImageView)vi.findViewById(R.id.movie_camera);
            movieCameraImage.setImageResource(R.drawable.signpost); 
        }
        else
        {
        	// This will be the text caption plus the duration-figure.
        	//
            duration.setText(durationText.concat(song.get(GridViewForVideoActivity.KEY_DURATION)));
            
            // Display the movie-camera icon.
            //
            ImageView movieCameraImage = (ImageView)vi.findViewById(R.id.movie_camera);
            movieCameraImage.setImageResource(R.drawable.moviecamera);
        }
        
        
        return vi;
    }
}

