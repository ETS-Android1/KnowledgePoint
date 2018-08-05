package com.ueas.kpallv1g6;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

/**
 * Notifies the user that the data required by the application could not be found. Advises
 * the user to quit the application and try to re-install.
 * 
 * @author tony.hillman
 *
 */
public class DataNotFoundActivity extends Activity 
{
	/**
	 * Tag for output to LogCat.
	 * 
	 */
	private final static String TAG0 = "DataNotFoundActivity";
	
	/**
	 * Sets up the activity as having the same layout and general appearance as the Login Activity.
	 * Presents a notification to the user, which is clicked on the exit the application, so that
	 * re-install can be attempted.
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// Establish the content view for this activity. Do this conditionally, based on the screen-size/aspect-ratio
 		// combination we have already determined. Note that this activity uses the same layout as for
		// the login activity: thus, the user has not perception of the activity having changed; a notification
		// alone is seen to appear.
 		//
 		switch (MetaDataForSuite.currentAspectRatio)
 		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				setContentView(R.layout.login_activity_for_zero___four_point_three);
				Log.w(TAG0, "Set Data Not Found content for 0.\n");
				break;
				
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				setContentView(R.layout.login_activity_for_one_zero___four_point_zero_etc);
				Log.w(TAG0, "Set Data Not Found content for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				setContentView(R.layout.login_activity_for_two_zero___four_point_three_etc);
				Log.w(TAG0, "Set Data Not Found content for 20.\n");
				break;
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				setContentView(R.layout.login_activity_for_three_zero___four_point_seven);
				Log.w(TAG0, "Set Data Not Found content for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				setContentView(R.layout.login_activity_for_four_zero___seven);
				Log.w(TAG0, "Set Data Not Found content for 40.\n");
				break;
					
 		    // This is a 1080x1920 generic-size device.
 			//
 			case 60:
 				setContentView(R.layout.login_activity_for_six_zero___gen);
 				Log.w(TAG0, "Set Data Not Found content for 60.\n");
 				break;
 			
 			// This is a 1080x1920 5.7 inch device.
 			//
 			case 70:
 				setContentView(R.layout.login_activity_for_seven_zero___five_point_seven);
 				Log.w(TAG0, "Set Data Not Found content for 70.\n");
 				break;
 			
 			// This is a 1200x1920 7 inch device.
 			//
 			case 80:
 				setContentView(R.layout.login_activity_for_eight_zero___seven);
 				Log.w(TAG0, "Set Data Not Found content for 80.\n");
 				break;
 			
 			// This is a 1200x1920 10 inch device.
 			//
 			case 90:
 				setContentView(R.layout.login_activity_for_nine_zero___ten);
 				Log.w(TAG0, "Set Data Not Found content for 90.\n");
 				break;
 			
 			// This is a 1440x900 7 inch device.
 			//
 			case 100:
 				setContentView(R.layout.login_activity_for_one_zero_zero___seven);
 				Log.w(TAG0, "Set Data Not Found content for 100.\n");
 				break;
 				
 				// If we are not sure, we use the following default.
 			//
 			default:
 				setContentView(R.layout.login_activity_for_six_zero___gen);
 				Log.w(TAG0, "Set Data Not Found content for default.\n");
 				break;			
 		}
		
 		// We have arrived in this activity because the data zip-file required for successful
 		// running of the application has not been found. So, we now notify the user of the
 		// problem, and recommend a re-install.
 		//
 		// Create an alert dialog, with which to inform the user of the situation.
 		//
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DataNotFoundActivity.this);
		alertDialogBuilder.setTitle("Data not available.");
		alertDialogBuilder.setMessage("Click to exit application, then try re-installing.");
		
		// When the user duly clicks on the single "OK" button, we perform the exit
		// routine.
		//
		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			// When the user clicks on the "OK" button...
			//
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				// Prepare to go back to the Login (the initial) Activity, and exit
				// from there.
				//
				Intent intent = new Intent(DataNotFoundActivity.this, LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("exit", "true");
				
				// Dismiss the alert.
				//
				dialog.cancel();
				
				// Start the Login Activity, so that exit can occur.
				//
				startActivity(intent);
			}  	
		});
		
		// Show the dialog that has been created.
		//
		alertDialogBuilder.show();	
	}
}
