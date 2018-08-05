package com.ueas.kpallv1g6;

import android.os.Vibrator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Plays a video encouraging the user to reorient their device physically from
 * portrait to landscape. This assists the user in transitioning from the book
 * cover activity to the grid view activity for the list of videos.
 * 
 * @author tony.hillman
 *
 */
@SuppressLint("InlinedApi")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GoLandscapeActivity extends Activity 
{
	// Initialize the vibrator.
	//
	public static Vibrator myVib = null;
	
	/**
	 * A tag used for writing information to LogCat.
	 * 
	 */
	private final static String TAG0 = "GoLandscapeActivity: ";
	
	/**
	 * The view employed to house the video that will be played.
	 * 
	 */
	public static VideoView vidView = null;

	/**
	 * Plays an animation to encourage the user to reorient their device
	 * physically to landscape-mode, in preparation for transition from the
	 * portrait-mode of the book cover activity to the landscape of the grid
	 * view activity.
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		// Establish the content view for the GoLandscape activity. Do this conditionally, based on the 
		// screen-size/aspect-ratio combination we have already determined.
		//
		switch (MetaDataForSuite.currentAspectRatio)
		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				setContentView(R.layout.go_landscape_activity_for_zero___four_point_three);
				Log.w(TAG0, "Set GoLandscape content for 0.\n");
				break;
				
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				setContentView(R.layout.go_landscape_activity_for_one_zero___four_point_zero_etc);
				Log.w(TAG0, "Set GoLandscape content for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				setContentView(R.layout.go_landscape_activity_for_two_zero___four_point_three_etc);
				Log.w(TAG0, "Set GoLandscape content for 20.\n");
				break;	
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				setContentView(R.layout.go_landscape_activity_for_three_zero___four_point_seven);
				Log.w(TAG0, "Set GoLandscape content for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				setContentView(R.layout.go_landscape_activity_for_four_zero___seven);
				Log.w(TAG0, "Set GoLandscape content for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				setContentView(R.layout.go_landscape_activity_for_five_zero___ten_v1);
				Log.w(TAG0, "Set GoLandscape content for 50.\n");
				break;
		
		    // This is a 1080x1920 generic-size device.
			//
			case 60:
				setContentView(R.layout.go_landscape_activity_for_six_zero___gen);
				Log.w(TAG0, "Set GoLandscape content for 60.\n");
				break;
			
			// This is a 1080x1920 5.7 inch device.
			//
			case 70:
				setContentView(R.layout.go_landscape_activity_for_seven_zero___five_point_seven);
				Log.w(TAG0, "Set GoLandscape content for 70.\n");
				break;
			
			// This is a 1200x1920 7 inch device.
			//
			case 80:
				setContentView(R.layout.go_landscape_activity_for_eight_zero___seven);
				Log.w(TAG0, "Set GoLandscape content for 80.\n");
				break;
			
			// This is a 1200x1920 10 inch device.
			//
			case 90:
				setContentView(R.layout.go_landscape_activity_for_nine_zero___ten);
				Log.w(TAG0, "Set GoLandscape content for 90.\n");
				break;
				
			// This is a 1440x900 7 inch device.
			//
			case 100:
				setContentView(R.layout.go_landscape_activity_for_one_zero_zero___seven);
				Log.w(TAG0, "Set GoLandscape content for 100.\n");
				break;

			// This is a 1440x2560 5.1 inch device.
			//
			case 110:
				setContentView(R.layout.go_landscape_activity_for_one_one_zero___five_point_one);
				Log.w(TAG0, "Set GoLandscape content for 110.\n");
				break;
			
			// If we are not sure, we use the following default.
			//
			default:
				setContentView(R.layout.go_landscape_activity_for_six_zero___gen);
				Log.w(TAG0, "Set GoLandscape content for default.\n");
				break;			
		}
		
		// Initialize the vibrator.
		//
		myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

		// Show no title-bar, no decoration: just a black screen-background.
		//
		getWindow().getDecorView().setSystemUiVisibility(
		          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		        | View.SYSTEM_UI_FLAG_FULLSCREEN
		        | View.SYSTEM_UI_FLAG_IMMERSIVE);        // Set project > properties > android
														 // to api 19, to ensure this is not flagged
														 // as an error.
		// Set up the video player.
		//
		vidView = (VideoView)findViewById(R.id.imgFrame); 
		String vidAddress = Environment.getExternalStorageDirectory()
		//					+ "/knowledgePointSD/reorientWiderWithCaption.mp4";
		 + "/knowledgePointSD/infrastructure/video/goLandscape.mp4";
		Uri vidUri = Uri.parse(vidAddress);
		vidView.setVideoURI(vidUri);   
		MediaController vidControl = new MediaController(GoLandscapeActivity.this);
		
		// Note that we won't actually show any video controls here, because this
		// is intended as a read-only video segment.
		//
		vidControl.setAnchorView(vidView);
		//vidView.setMediaController(vidControl); 
		vidView.setMediaController(null); 

		// Start the video.
		//
		vidView.start();
		myVib.vibrate(250);

		// Set an onCompletionListener, so that once the video has completely played, we
		// just go to the next activity.
		//
		vidView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() 
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				
				Intent intent = new Intent(GoLandscapeActivity.this, GridViewForVideoActivity.class);
				intent.putExtra("here", Environment.getExternalStorageDirectory() 
												+ "/knowledgePointSD/video/all_videos.xml");
				startActivity(intent);
			}
		});
	}
}
