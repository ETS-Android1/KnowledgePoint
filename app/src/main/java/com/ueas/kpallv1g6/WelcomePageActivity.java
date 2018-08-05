package com.ueas.kpallv1g6;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Presents the first page seen by the user, once authentication
 * and data initialization have occurred. It presents various options whereby the user can
 * start using the application: by means of text, animations, and voice activation. The layout
 * for the activity is provided by the layout resource file welcomepageactivity.xml
 * 
 * @author tony.hillman@ultra-as.com
 *
 *
 */
public class WelcomePageActivity extends Activity
{
	// NOTE: NEED TO SUBSTITUTE MORE DESCRIPTIVE NAMES FOR THESE BUTTONS.
	
	/**
	 * Button, for user-input. Allows user to view the Preface to the application, by
	 * means of the Chapter Activity.
	 * 
	 */
	public Button button1 = null;
	
	/**
	 * Button, for user-input. Allows user to access main contents listing for
	 * the documentation suite, by means of the Customized Content List View
	 * Activity.
	 * 
	 */
	public Button button2 = null;
	
	/**
	 * Button, for user-input. Allows user to view the About section, by means of the
	 * Chapter Activity.
	 * 
	 */
	public Button button3 = null;
	
	/**
	 * Button, for user-input. Allows user to view the Help section, by means of the
	 * Chapter Activity.
	 * 
	 */
	public Button button4 = null;
	
	/**
	 * Button, for user-input. Allows user to view start the mechanism whereby Feedback
	 * on the application can be provided.
	 * 
	 */
	public Button button5 = null;
	
	/**
	 * Button, for user-input. Allows the user to Exit the application.
	 * 
	 */
	public Button button6 = null;
	
	/**
	 * A two-dimensional string-array, used to hold the locations of the html files
	 * used for the "prior activities", which are Preface, About, and Help.
	 * 
	 */
	public static String[][] priorActivities = new String[4][2];
	
	/**
	 * A string tag for writing to Eclipse LogCat.
	 * 
	 */
	private final static String TAG0 = "WPA:onCreate";
	
	/**
	 * Establishes the locations of key source files and displays buttons, whereby 
	 * users can access different activities.
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// Establish the content view for this activity. Do this conditionally, based on the screen-size/aspect-ratio
		// combination we have already determined.
		//
		switch (MetaDataForSuite.currentAspectRatio)
		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				setContentView(R.layout.welcome_page_activity_for_zero___four_point_three);
				Log.w(TAG0, "Set Welcome Page content for 0.\n");
				break;
		
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				setContentView(R.layout.welcome_page_activity_for_one_zero___four_point_zero_etc);
				Log.w(TAG0, "Set Welcome Page content for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				setContentView(R.layout.welcome_page_activity_for_two_zero___four_point_three_etc);
				Log.w(TAG0, "Set Welcome Page content for 20.\n");
				break;
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				setContentView(R.layout.welcome_page_activity_for_three_zero___four_point_seven);
				Log.w(TAG0, "Set Welcome Page content for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				setContentView(R.layout.welcome_page_activity_for_four_zero___seven);
				Log.w(TAG0, "Set Welcome Page content for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				setContentView(R.layout.welcome_page_activity_for_five_zero___ten);
				Log.w(TAG0, "Set Welcome Page content for 50.\n");
				break;
				
		    // This is a 1080x1920 generic-size device.
			//
			case 60:
				setContentView(R.layout.welcome_page_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Welcome Page content for 60.\n");
				break;
			
			// This is a 1080x1920 5.7 inch device.
			//
			case 70:
				setContentView(R.layout.welcome_page_activity_for_seven_zero___five_point_seven);
				Log.w(TAG0, "Set Welcome Page content for 70.\n");
				break;
			
			// This is a 1200x1920 7 inch device.
			//
			case 80:
				setContentView(R.layout.welcome_page_activity_for_eight_zero___seven);
				Log.w(TAG0, "Set Welcome Page content for 80.\n");
				break;
			
			// This is a 1200x1920 10 inch device.
			//
			case 90:
				setContentView(R.layout.welcome_page_activity_for_nine_zero___ten);
				Log.w(TAG0, "Set Welcome Page content for 90.\n");
				break;
				
			// This is a 1200x1920 10 inch device.
			//
			case 100:
				setContentView(R.layout.welcome_page_activity_for_one_zero_zero___seven);
				Log.w(TAG0, "Set Welcome Page content for 100.\n");
				break;

			// This is a 1440x2560 5.1 inch device.
			//
			case 110:
				setContentView(R.layout.welcome_page_activity_for_one_one_zero___five_point_one);
				Log.w(TAG0, "Set Welcome Page content for 110.\n");
				break;
			
			// If we are not sure, we use the following default.
			//
			default:
				setContentView(R.layout.welcome_page_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Welcome Page content for default.\n");
				break;			
		}
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// Establish the locations of "prior" activity html files (such as Preface, About, Feedback) and
		// their accompanying toc files.
		//
		String default_path_for_priorActivities = "/sdcard/knowledgePointSD/prior-activities/";
		
		// NOTE: THE TOCs FOR THE PREFACE, HELP, AND ABOUT FILES ARE NO LONGER IMPLEMENTED:
		// REMOVED JUST FOR THE SAKE OF SIMPLICITY. THEY CONTINUE TO RECEIVE DEFINITION HERE,
		// AND SO SHOULD BE OMITTED. UNLESS WE FIND THERE IS A REASON TO RETURN TO THEM. AT
		// THIS POINT, THESE ACTIVITIES ARE SHORT ENOUGH FOR US NOT TO NEED TO BOTHER.
		//
		// REGARDING THE ACTIVITY-LOCATIONS, THESE SHOULD BE DERIVED FROM AN XML FILE, LIKE
		// OTHER HTML DOCUMENTS. 
		//
		priorActivities[0][0] = default_path_for_priorActivities + "preface/html/preface.html";
		priorActivities[0][1] = default_path_for_priorActivities + "preface/html/preface-toc.html";
		priorActivities[1][0] = default_path_for_priorActivities + "about/html/about.html";
		priorActivities[1][1] = default_path_for_priorActivities + "about/html/about-toc.html";
		priorActivities[2][0] = default_path_for_priorActivities + "feedback/html/feedback.html";
		priorActivities[2][1] = default_path_for_priorActivities + "feedback/html/feedback-toc.html";
		priorActivities[3][0] = default_path_for_priorActivities + "help/html/help.html";
		priorActivities[3][1] = default_path_for_priorActivities + "help/html/help-toc.html";
		
		// The Suite Meta Data Registry contains information on the entire suite that we wish
		// to make persistent throughout the session. Here, we record the locations of all the
		// prior activities.
		//
		MetaDataForSuite.prior_activity_files = priorActivities;
		
		// Instantiate the button for the Preface.
		//
		button1 = (Button)this.findViewById(R.id.prefaceButton);
		button1.setTextColor(Color.rgb(0, 0, 138));
		button1.clearFocus();

		// Establish the routine for the Preface button. When clicked, it starts the Chapter
		// Activity with a "prior_activity_number" and "placeOfDeparture" number that is appropriate.
		// See the Chapter Activity for how these are interpreted. 	NOTE: THE METHODOLOGY IS
		// CONFUSING, AND NEEDS TO BE CLARIFIED GREATLY.
		//
		button1.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				Intent intent = new Intent(WelcomePageActivity.this, 
				           ChapterActivity.class);
				
				// Specify that our departure is 1, and our target 6, meaning Welcome Page and
				// prior activity, respectively. This means a transition profile of 100. See
				// the Chapter Activity for a full description of the numbering system.
				//
				intent.putExtra("placeOfDeparture", 1);
		    	intent.putExtra("targetChapterActivityType", 6);
		    	intent.putExtra("transitionProfile", 100);
				
				// Find the file for the About section.
		    	//
				String prior_activity_file_location = priorActivities[0][0];
				intent.putExtra("prior_activity_file_location", prior_activity_file_location);
				Log.w(TAG0, "About location is " + prior_activity_file_location);
				
				// Tell the Chapter Activity which "prior" we are going to, and start
				// the new Activity.
				//
				intent.putExtra("prior_activity_number", 0);
				startActivity(intent);
			}
		});

		// Instantiate the button for accessing the various documents. Then establish
		// the button's routine.
		//
		button2 = (Button)this.findViewById(R.id.documentationButton);
		button2.setTextColor(Color.rgb(0, 0, 138));
		button2.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{	
				String XMLfileLocation = "";
				// Selecting Documentation takes the reader to the ListView containing the full
				// list of documents.
				//
				Intent intent = new Intent(WelcomePageActivity.this, 
				           ListViewForSuiteActivity.class);
				
				// If we are accessing data over the network, use an http reference. 
				// Connectivity variable is used globally to indicate that we are
				// using the network.
				//
				if (MetaDataForSuite.Connectivity == 0)
				{
					// An old reference to the IP of my machine. NOTE: NEED TO ESTABLISH
					// THIS FROM AN XML FILE. AT LEAST MAKE THIS A VARIABLE SET AT THE TOP
					// OF THIS FILE.
					//
					XMLfileLocation = "http://192.168.0.7/knowledgePointSD/all_documents.xml";
				}
				
				// Otherwise, if we are accessing data locally, use an address on the sdcard.
				//
				else 
				{
					// NOTE: AGAIN, SHOULD BE DERIVED FROM AN XML SOURCE, AND AT LEAST
					// SET AT THE TOP OF THIS FILE.
					//
					XMLfileLocation = "/sdcard/knowledgePointSD/all_documents.xml";
				}
				
				Log.w(TAG0, "File location is " + XMLfileLocation);
				
				// Start the ListView for the whole document, specifying the xml file that
				// contains the document information.
				//
				intent.putExtra("here", XMLfileLocation);
				startActivity(intent);
				//finish();
			}
		});		

		// Instantiate the button for the About section, and establish its routine.
		//
		button3 = (Button)this.findViewById(R.id.aboutButton);
		button3.setTextColor(Color.rgb(0, 0, 138));
		button3.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{	
				Intent intent = new Intent(WelcomePageActivity.this, 
				           ChapterActivity.class);
				
				// Specify that our departure is 1, and our target 6, meaning Welcome Page and
				// prior activity, respectively. This means a transition profile of 100. See
				// the Chapter Activity for a full description of the numbering system.
				//
				intent.putExtra("placeOfDeparture", 1);
		    	intent.putExtra("targetChapterActivityType", 6);
		    	intent.putExtra("transitionProfile", 100);
				
				// Find the file for the About section.
		    	//
				String prior_activity_file_location = priorActivities[1][0];
				intent.putExtra("prior_activity_file_location", prior_activity_file_location);
				Log.w(TAG0, "About location is " + prior_activity_file_location);
				
				// Tell the Chapter Activity which "prior" we are going to, and start
				// the new Activity.
				//
				intent.putExtra("prior_activity_number", 1);
				startActivity(intent);
			}
		});	
		
		// Instantiate the button for the Help section, and establish its routine.
		//
		button4 = (Button)this.findViewById(R.id.helpButton);
		button4.setTextColor(Color.rgb(0, 0, 138));
		button4.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				Intent intent = new Intent(WelcomePageActivity.this, 
				           ChapterActivity.class);
				
				// Specify that our departure is 1, and our target 6, meaning Welcome Page and
				// prior activity, respectively. This means a transition profile of 100. See
				// the Chapter Activity for a full description of the numbering system.
				//
				intent.putExtra("placeOfDeparture", 1);
		    	intent.putExtra("targetChapterActivityType", 6);
		    	intent.putExtra("transitionProfile", 100);
				
				// Find the file for the About section.
		    	//
				String prior_activity_file_location = priorActivities[3][0];
				intent.putExtra("prior_activity_file_location", prior_activity_file_location);
				Log.w(TAG0, "About location is " + prior_activity_file_location);
				
				// Tell the Chapter Activity which "prior" we are going to, and start
				// the new Activity.
				//
				intent.putExtra("prior_activity_number", 3);
				startActivity(intent);
			}
		});	
		
		// Instantiate the Feedback button, and supply a routine. For now, this is just
		// an email, with address and subject already filled in.
		//
		button5 = (Button)this.findViewById(R.id.feedbackButton);
		button5.setTextColor(Color.rgb(0, 0, 138));
		button5.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				String subject = "All feedback on this prototype application is gratefully received!";
				String body = "";
				String uri_fodder="mailto:tony.hillman@ultra-as.com?subject=" + subject + "&body=" + body;
				
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri data = Uri.parse(uri_fodder);
				intent.setData(data);
				startActivity(intent);  
			}
		});	
		
		// Instantiate the Exit button, and supply a routine. This involves a notification,
		// which allows the user to confirm exit. Pressing "No" just dismisses the
		// notification. Note that the exit mechanism actually resides in the Login
		// Activity, so pressing "Yes" takes us back there.
		//
		button6 = (Button)this.findViewById(R.id.exitButton);
		button6.setTextColor(Color.rgb(0, 0, 138));
		button6.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{	
				new AlertDialog.Builder(WelcomePageActivity.this, R.style.CustomDialog)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Exit Application")
					.setMessage("Are you sure you want to exit?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Intent intent = new Intent(WelcomePageActivity.this, LoginActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra("exit", "true");
							startActivity(intent);
						}
					})
					.setNegativeButton("No", null)
					.show();
			}
		});	
	}
	
	/**
	 * Unbinds all drawables from the activity and then performs a garbage collection.
	 * 
	 */
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		
		Utils.unbindDrawables(findViewById(R.id.prefaceButton));
		Utils.unbindDrawables(findViewById(R.id.exitButton));
		Utils.unbindDrawables(findViewById(R.id.documentationButton));
		Utils.unbindDrawables(findViewById(R.id.aboutButton));
		Utils.unbindDrawables(findViewById(R.id.helpButton));
		Utils.unbindDrawables(findViewById(R.id.feedbackButton));
		Utils.unbindDrawables(findViewById(R.id.theSplashScreenLayout));

		System.gc();
	}
}
