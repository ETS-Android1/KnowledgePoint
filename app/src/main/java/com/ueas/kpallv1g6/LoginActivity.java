package com.ueas.kpallv1g6;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Provides a GUI whereby the user inputs a login
 * and password, and submits these. If authentication succeeds, an
 * intent is created to take the user to the WelcomePage activity.
 * Otherwise, a notification of failure is presented, and the user
 * invited to try again. Additionally, determines the aspect ratio and
 * screen size of the current device: this information is provided to
 * all other activities, so that graphical elements can be appropriately
 * sized.
 * 
 * @author tony.hillman@ultra-as.com
 *
 */
public class LoginActivity extends Activity 
{
	// Tag for output to LogCat.
	//
	private final static String TAG0 = "LoginActivity";
	
	// Strings for retrieving user-submitted login, password, and server.
	//
	private static String theLoginString = "";
	private static String thePasswordString = "";
	public static String theServerAddressString = "";
	
	// Three text fields, respectively for login, password, and server.
	//
	public static EditText editText = null;
	public static EditText editText2 = null;
	public static EditText editText3 = null;
			
    /** 
     * Sets up the activity, and as such establishes the layout specified in
     * login_activity.xml. It also determines whether to exit the application, based on the user's
     * electing to do so via one of the application's notifications.
     * @param savedInstanceState The state of any saved instance.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Determine the aspect ratio and screen-dimension of the current device. Based on this, we determine
        // which layout files we use, and which html files get unpacked from the zip file. This allows us to
        // use a single binary plus accompanying zip file for all aspect ratios and screen dimensions.
        //
        DisplayMetrics met = new DisplayMetrics();                
        this.getWindowManager().getDefaultDisplay().getMetrics(met);// get display metrics object
        String strSize = new DecimalFormat("##.##").format(Math.sqrt(((met.widthPixels / met.xdpi) 
        																	* (met.widthPixels / met.xdpi)) 
        																	+ ((met.heightPixels / met.ydpi) 
        																			* (met.heightPixels / met.ydpi))));
        // Record what we've found to the console.
        //
        Log.w(TAG0, "Screen width is " + met.widthPixels + " pixels.\n");
        Log.w(TAG0, "Screen height is " + met.heightPixels + " pixels.\n");
        Log.w(TAG0, "Screen dimension is " + strSize + " inches.\n");
        
        // Make a record of the aspect ratio we've found. In each case, we check the width figure, since
        // this is the figure that appears always to correspond exactly with the device-specification (the
        // height in pixels always varies somewhat, as does the screen-dimension in inches).
        //
        if (met.widthPixels == 1080) 															// Meaning 1080x1920
        {
        	// A 1080 device may be a 5.7 screen-width, or 5.0 to 5.5. So, determine which, by examining
        	// the screen dimension.
        	//
        	// If we have one of the smaller 1080x1920 devices....
        	//
        	if (Float.valueOf(strSize) < 5.6)
        	{	
        		// Establish a value of 60, signifying 1080x1920 for generic (smaller) devices.
        		//
        		MetaDataForSuite.currentAspectRatio = 60;	
        	}
        	
        	// But if the screen-size is 5.7....
        	//
        	else
        	{
        		// Establish a value of 70, signifying 1080x1920 for 5.7 devices.
        		//
        		MetaDataForSuite.currentAspectRatio = 70;
        	}
        	
    		// Establish html sizing. (Note that all 1080x1920 devices use the same html sizing, regardless of 
    		// differences in screen-size.)
    		//
    		MetaDataForSuite.htmlSizingSpecification = 00;
    		
    		Log.w(TAG0, "Established htmlSizingSpecification as " + String.valueOf(MetaDataForSuite.htmlSizingSpecification));
        }
        else
        {
        	if (met.widthPixels == 1200)														// Meaning 1200x1920
        	{
        		// A 1200 device may be a 7 or a 10.1 inch. Determine which.
        		//
        		// If this is a 7" device...
        		//
        		if (Float.valueOf(strSize) < 8.0)
        		{
        			// Establish a value of 80, which means 7" for 1200x1920.
        			//
        			MetaDataForSuite.currentAspectRatio = 80;
        			
        			// Establish html sizing. For 7" 1200x1920 devices, this is size 40.
        			//
        			MetaDataForSuite.htmlSizingSpecification = 40;
        		}
        		
        		// If greater than 8, we have a 10.1 inch device.
        		//
        		else
        		{
        			// Establish a value of 90, which means 10" for 1200x1920.
        			//
        			MetaDataForSuite.currentAspectRatio = 90;
        			
        			// Establish html sizing. For 10.1" 1200x1920 devices, this is size 30.
        			//
        			MetaDataForSuite.htmlSizingSpecification = 30;
        		}
        	}
        	else
        	{
        		if (met.widthPixels == 480)														// Meaning 480x800
        		{
        			// Establish a value of 0, which means 4.3" for 480x800.
        			//
        			MetaDataForSuite.currentAspectRatio = 0;
        			
        			// A 480 device is assumed to be 4.3" (at least, that's all we
        			// support at this stage. Establish html sizing accordingly, which
        			// is 0.
        			//
        			MetaDataForSuite.htmlSizingSpecification = 00;
        		}
        		else
        		{
        			if (met.widthPixels == 540)													// Meaning 540x960
        			{
        				// Establish a value of 10, which means from 4.0 to 4.8" on
        				// 540x960 devices.
        				//
        				MetaDataForSuite.currentAspectRatio = 10;
        				
        				// These devices take an html sizing of zero.
        				//
        				MetaDataForSuite.htmlSizingSpecification = 00;
        			}
        			else
        			{
        				if (met.widthPixels == 720)												// Meaning 720x1280
        				{
        					// Establish a value of 20, which means from 4.3 to 5.0 inches,
        					// on 720x1280 devices.
        					//
        					MetaDataForSuite.currentAspectRatio = 20;
        					
        					// These devices take an html sizing of ten.
        					//
        					MetaDataForSuite.htmlSizingSpecification = 10;
        				}
        				else
        				{
        					if (met.widthPixels == 900)											// Meaning 1440x900
        					{
        						// Establish a value of 100, which means 1440x900 devices
        						// of 7 inches.
        						//
        						MetaDataForSuite.currentAspectRatio = 100;
        						
        						// These devices take an html sizing of forty.
        						//
        						MetaDataForSuite.htmlSizingSpecification = 40;
        					}
        					else
        					{
        						if (met.widthPixels == 768)										// Meaning 768x1280
        						{
        							// Establish a value of 30, which means 768x1280 devices
        							// of 4.7 inches.
        							MetaDataForSuite.currentAspectRatio = 30;
        							
        							// These devices take an html sizing of twenty.
        							//
        							MetaDataForSuite.htmlSizingSpecification = 20;
        						}
        						else
        						{
        							if (met.widthPixels == 800)									// Meaning 800x1280
        							{
        								// An 800x1200 device may be a 7 or a 10.1 inch. Determine which.
        								//
        								// If this is a 7" device...
        								//
        								if (Float.valueOf(strSize) < 8.0)
        								{
        									// Establish a value of 40, which means 800x1280 devices
                							// of 7 inches.
                							MetaDataForSuite.currentAspectRatio = 40;
                							
                							// These devices take an html sizing of 40.
                							//
                							MetaDataForSuite.htmlSizingSpecification = 40;
        								}
        								else
        								{

											// Otherwise, if this is a 10.1" device...
											//
											// Establish a value of 50, which means 800x1280 devices
											// of 10.1 inches.
											MetaDataForSuite.currentAspectRatio = 50;

											// These devices take an html sizing of 30
											//
											MetaDataForSuite.htmlSizingSpecification = 30;
        								}
        							}
									else
									{
										if (met.widthPixels == 1440)
										{
											Log.w(TAG0, "Yes, the screen width is 1440");

											// Establish a value of 110, which means devices of
											// the 1440x2560 aspect ratio.
											MetaDataForSuite.currentAspectRatio = 110;

											// These devices take an html sizing of 50??
											//
											MetaDataForSuite.htmlSizingSpecification = 50;
										}
										else
										{
											Log.w(TAG0, "Didn't find 1440...");
										}
									}
        						}
        					}
        				}
        			}
        		}
        	}
        }
        
        // Clear the cache, so that we are sure nothing remains from the last time the
        // application was run.
        //
        try 
        {
        	Log.w(TAG0, "Trimming cache now.");
            trimCache(this);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        // This is the launcher activity for the application. So, whenever the user elects to exit,
        // we come back to this activity. Therefore, the first thing we must do is check whether there is an intent
        // present, indicating that exit is required. If one is indeed present, then we duly exit. Otherwise,
        // we are here because the application was just started, and so continue with the activity.
        //
        Intent intent = getIntent();
        
        if (intent.hasExtra("exit"))
        {
        	finish();
        }
        
        // Establish an editor for using shared preferences.
        //
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        
        // The first value is the key, the second the value. NOTE THAT RIGHT NOW, THIS VALUE IS
        // A PLACE-HOLDER, SINCE WE ARE NOT REALLY USING THE NETWORK METHOD OF DATA-RETRIEVAL.
        //
        editor.putString("serveraddress", "127.0.0.1");
        
        // Save the value. It will be retained in-between runnings of the application.
        //
        editor.commit();
        
        // The first parameter to getString is the key whose value is being searched for. The
        // second is a default value, returned when no other is available.
        //
        theServerAddressString = preferences.getString("serveraddress", "not found");
        
        if (theServerAddressString.equals("not found"))
        {
        	Log.w(TAG0, "Cannot find the IP address of the server. Exiting");
        	System.exit(0);
        }
        else
        {
        	Log.w(TAG0, "Server IP address retrieved as " + theServerAddressString);
        }
 
        // Make sure that the main registry is empty in all places
        // where is is eventually used to store info on the current data-targets. This
        // ensures we don't have any potentially conflicting old references there.
        //
        MetaDataForSuite.allDocuments = null;
        MetaDataForSuite.allDocumentsArrayList = null;
        MetaDataForSuite.all_docs_doc_object = null;
        
        // Establish the content view for this activity. Do this conditionally, based on the screen-size/aspect-ratio
 		// combination we have already determined.
 		//
 		switch (MetaDataForSuite.currentAspectRatio)
 		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				setContentView(R.layout.login_activity_for_zero___four_point_three);
				Log.w(TAG0, "Set Login content for 0.\n");
				break;
				
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				setContentView(R.layout.login_activity_for_one_zero___four_point_zero_etc);
				Log.w(TAG0, "Set Login content for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				setContentView(R.layout.login_activity_for_two_zero___four_point_three_etc);
				Log.w(TAG0, "Set Login content for 20.\n");
				break;
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				setContentView(R.layout.login_activity_for_three_zero___four_point_seven);
				Log.w(TAG0, "Set Login content for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				setContentView(R.layout.login_activity_for_four_zero___seven);
				Log.w(TAG0, "Set Login content for 40.\n");
				break;
					
 		    // This is a 1080x1920 generic-size device.
 			//
 			case 60:
 				setContentView(R.layout.login_activity_for_six_zero___gen);
 				Log.w(TAG0, "Set Login content for 60.\n");
 				break;
 			
 			// This is a 1080x1920 5.7 inch device.
 			//
 			case 70:
 				setContentView(R.layout.login_activity_for_seven_zero___five_point_seven);
 				Log.w(TAG0, "Set Login content for 70.\n");
 				break;
 			
 			// This is a 1200x1920 7 inch device.
 			//
 			case 80:
 				setContentView(R.layout.login_activity_for_eight_zero___seven);
 				Log.w(TAG0, "Set Login content for 80.\n");
 				break;
 			
 			// This is a 1200x1920 10 inch device.
 			//
 			case 90:
 				setContentView(R.layout.login_activity_for_nine_zero___ten);
 				Log.w(TAG0, "Set Login content for 90.\n");
 				break;
 			
 			// This is a 1440x900 7 inch device.
 			//
 			case 100:
 				setContentView(R.layout.login_activity_for_one_zero_zero___seven);
 				Log.w(TAG0, "Set Login content for 100.\n");
 				break;

			// This is a 1440x2560 5.1 inch device.
			//
			case 110:
				setContentView(R.layout.login_activity_for_one_one_zero___five_point_one);
				Log.w(TAG0, "Set Login content for 110.\n");
				break;
 				
 				// If we are not sure, we use the following default.
 			//
 			default:
 				setContentView(R.layout.login_activity_for_six_zero___gen);
 				Log.w(TAG0, "Set Login content for default.\n");
 				break;			
 		}
        
        // Establish the layout for the current activity.
        //
        //setContentView(R.layout.login_activity);
        
        // Establish the visual content of the editable field containing the server IP address as
        // the value saved in shared preferences.
        //
        editText3 = (EditText) findViewById(R.id.ip_address_field);
        editText3.setText(theServerAddressString);
        
        setTitle("Enter login and password...");
        
        // Set the searchYetEmployed flag to 0, indicating that no text search
        // has yet been used in the Chapter Activity. This ensures that if and
        // when it is first used, an explanatory toast appears.
        //
        MetaDataForDocument.searchYetEmployed = 0;
    }
    
    /**
     * Invoked by user-interaction, this method retrieves
     * strings provided by the user in the login and password fields of
     * the login screen GUI, and passes these to the Authenticate class'
     * performRemoteAuthentication method, which attempts to authenticate
     * the user with the appropriate authority.
     * @param view The current view.
     * @see  Authenticate
     * 
     */
    public void sendMessage(View view)
    {  	
    	// Establish the text fields for retrieving login and password.
    	//
    	editText = (EditText) findViewById(R.id.login_field);
    	editText2 = (EditText) findViewById(R.id.password_field);
    	
    	// Establish a java identity for the radio button group defined
    	// in login_activity.xml.
    	//
    	RadioGroup rg = (RadioGroup)findViewById(R.id.radiogroup);
    	
    	// Now establish the radio button group such that we retrieve the
    	// accompanying text value of the button that is checked at
    	// the moment the Submit button is pressed. This indicates whether
    	// we are attempting local or networked data-access.
    	//
    	int id= rg.getCheckedRadioButtonId();
        View radioButton = rg.findViewById(id);
        int radioId = rg.indexOfChild(radioButton);
        RadioButton btn = (RadioButton) rg.getChildAt(radioId);
        String selection = (String) btn.getText();
        
        Log.w(TAG0, "Radio button selection is " + selection);

        // The values of SuiteMetaDataRegistry.Connectivity will be used later,
        // in various activities, to determine the style of data-access: http or
        // local file-read.
        //
    	if (selection.matches("Local"))
    	{
    		// Data-access will assume local availability.
    		//
    		MetaDataForSuite.Connectivity = 1;
    		
    		// Pull the value of the string from the login field, and add to the intent. 
        	//
        	theLoginString = editText.getText().toString();
        	Log.w(TAG0, "Got login as " + theLoginString);
        	
        	// Pull the value of the string from the password field. 
        	//
        	thePasswordString = editText2.getText().toString();   
        	Log.w(TAG0, "Got pwd as " + thePasswordString);
        	
        	// Create a progress dialog for showing while authentication takes place.
        	//
        	ProgressDialog progress = new ProgressDialog(this, R.style.CustomDialog);
        	progress.setTitle("UltraAPEX Knowledge Point");
          	progress.setMessage("Authenticating...");
        	
          	// Perform authentication in a background thread.
          	//
    		netOp myOp = new netOp(progress);
    		
    		// String passed here is just a place-holder, not currently needed.
    		//
    		myOp.execute("okay");
    		
    		// Set up a text view in which we will display the results of the authentication
    		// procedure - whether successful or not.
    		//
    		TextView newTextView = (TextView) findViewById(R.id.auth_notification);
    		newTextView.setText("");
    		
    		// Show the progress dialog while authentication occurs.
    		//
    		progress.show();
    	}
    	else
    	{
    		// Data-access will assume remote availability.
    		//
    		MetaDataForSuite.Connectivity = 0;
    		
    		// NOTE: Currently, remote access is not supported. All data is downloaded with
    		// the application, and accessed locally from the sdcard. I've left the remote
    		// interface and code in place, so that people know it is indeed there, and could
    		// one day be switched on. For now, attempting remote access just brings up the
    		// following dialog, stating that remote access is not supported, and requesting the
    		// user to try local instead.
    		//
    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    		alertDialogBuilder.setTitle("Remote Access Not Enabled");
    		alertDialogBuilder.setMessage("Click to dismiss, then try Local Access.");
    		
    		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
    		{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.cancel();
				}  	
    		});
    		
    		alertDialogBuilder.show();
    	}
    }
    
    /**
     * Blanks out all of the
     * visible user-input fields, which are login, password, and authentication
     * notification. The server address string is returned to the default.
     * 
     */
    public void onResume()
    {
    	super.onResume();
    	
    	EditText editText = (EditText) findViewById(R.id.login_field); 
    	editText.setText("");
    	
    	EditText editText2 = (EditText) findViewById(R.id.password_field); 
    	editText2.setText("");
    	
    	TextView newTextView = (TextView) findViewById(R.id.auth_notification);
    	newTextView.setText(""); 
    	
        editText3 = (EditText) findViewById(R.id.ip_address_field);
        editText3.setText(theServerAddressString);
    }
    
    /** 
     *  Allows authentication to be
     *  performed on a background thread, by means of the doInBackground method. Authentication
     *  is performed by the creation of an Authenticate object.
     *  
     *  @see Authenticate
     *  
     *
     */
	public class netOp extends AsyncTask<String, String, String>
	{	
		/**
		 *  A progress dialog to be displayed while the background operation is occurring.
		 *  
		 */
		private ProgressDialog progress;
		
		/**
		 *  Constructor for netOp, which establishes the progress dialog.
		 *  
		 */
		public netOp(ProgressDialog progress)
		{
			this.progress = progress;
		}
		
		/**
		 * Creates an Authenticate object, and uses it to authenticate the current 
		 * user, by means of the submitted login and password. 
		 * 
		 * @param params Optional string parameters
		 * 
		 * @return A string that specifies the result of authentication, whether or not successful.
		 * 
		 */
		@Override
		protected String doInBackground(String... params)
		{
			Log.w(TAG0, "doing in background\n");
			
			// String to contain the result of authentication.
			String authResult = null;
			
			// If this is networked connectivity, get appropriate authentication.
			if (MetaDataForSuite.Connectivity == 0)
			{
				Authenticate auth = new Authenticate();
			
				try 
				{
					// Call the servlet that handles authentication, specifying the submitted login and password.
					authResult = auth.performRemoteAuthentication(theLoginString, thePasswordString);
					
					Log.w(TAG0, "have returned..." + '\n');
					Log.w(TAG0, "result is " + authResult + '\n');
					
					if (authResult.startsWith("Y"))
					{
						Log.w(TAG0, "it starts with Y." + '\n');
					}
				} 
				catch (IOException e) 
				{
					Log.w(TAG0, "Attempt to authenticate produced an exception." + '\n');
					e.printStackTrace();
				}
			}
			
			// Otherwise, if this is local connectivity, just grant authentication here directly.
			else
			{
				authResult = "Y";
			}
			
				return authResult;
		}
	
		/**
		 * Performs actions according to
		 * whether or not authentication has been successful. If authentication succeeded, the
		 * DataInitialization Activity is started. If authentication failed, a notification is
		 * displayed.
		 * 
		 */
		@Override
		protected void onPostExecute(String theResult)
		{
			// Get rid of the "Preparing..." notification.
		    progress.dismiss();
		    
			Log.w(TAG0, "PostExecute: theResult is " + theResult + '\n');
		
			// If this hasn't worked out, simply display a notification to the user. They are
			// still in the login activity.
			
			// NOTE: THIS NEEDS FIXING. IF THEY CONTINUE TO FAIL, SINCE THE LOGIN ACTIVITY HAS
			// NO EXIT BUTTON, THEY ARE STUCK HERE!
			if (theResult == null)
			{
				Log.w(TAG0, "it was null" + '\n');
				TextView newTextView = (TextView) findViewById(R.id.auth_notification);
				newTextView.setText("Could not authenticate. Please re-try.");
			}
			else
			{
				// The string that is returned by the Authenticate class consists
				// either of a Y or an N, indicating success or failure respectively.
				if (theResult.startsWith("Y"))
				{
					// If we failed, give the user an onscreen notification.
					TextView newTextView = (TextView) findViewById(R.id.auth_notification);
					newTextView.setText("Authentication succeeded.");
					
					Log.w(TAG0, "it starts with Y" + '\n');
					
					// Since authentication was successful, we now proceed to the
					// DataInitialization activity, where we determine whether data is currently available,
					// or needs first to be initialized.
					Log.w(TAG0, "Login: creating intent for DataInitializationActivity.");
					Intent newIntent = new Intent(LoginActivity.this, DataInitializationActivity.class);
        		
					Log.w(TAG0, "Login: starting DataInitializationActivity.");
					startActivity(newIntent);
				}
				else
				{
					// If authentication failed, give the user an on-screen notification.
					TextView newTextView = (TextView) findViewById(R.id.auth_notification);
					newTextView.setText("Authentication failed. Please re-try.");
				}
			}
		}
	}
	
	/**
	 * Clears the application's cache, so ensuring that no data is held over
	 * from previous runs.
	 * 
	 * @param context The current context.
	 * 
	 */
    public void trimCache(Context context) 
    {
       try 
       {
    	   // Get the cache directory for the current context. If it exists and is
    	   // indeed a directory, delete it.
          File dir = context.getCacheDir();
          if (dir != null && dir.isDirectory()) 
          {
             deleteDir(dir);
          }
       } 
       catch (Exception e) 
       {
          Log.w(TAG0, "Could not delete cache directory.\n");
       }
    }

    /**
     * Deletes a directory. It is employed by the trimCache method.
     * 
     * @param dir A directory.
     * 
     * @return A boolean indicating success or failure of the directory-deletion.
     * 
     */
    public boolean deleteDir(File dir) 
    {
       if (dir != null && dir.isDirectory()) 
       {
    	  // If the directory exists and is indeed a directory, call this method recursively to
    	  // delete all of its children and their content.
          String[] children = dir.list();
          
          for (int i = 0; i < children.length; i++) 
          {
             boolean success = deleteDir(new File(dir, children[i]));
             
             if (!success) 
             {
                return false;
             }
          }
       }

       // The directory is now empty, so delete it.
       //
       return dir.delete();
    }
    
   /**
    * Called by the onClick specified for the second radio
    * button in login_activity.xml. When the radio button is selected, the contents of
    * password fields are changed, to show rows of stars. (Note that in an obfuscated field,
    * such as that for the password, the actual appearance is of dots.)
    * 
    * @param view The current view.
    * 
    */
   public void starOutFields(View view)
   {
	   // Establish the text fields for the login and password.
	   editText = (EditText) findViewById(R.id.login_field);
	   editText2 = (EditText) findViewById(R.id.password_field);

	   // Set the fields as appropriate for local access.
	   editText.setText("*****");
	   editText2.setText("*****");
	   editText3.setText("/sdcard");
   }
   
   /**
    * Called by the onClick specified for the first radio
    * button in login_activity.xml. Note that this is selected by default. If the
    * first radio button is re-selected, following selection of the second radio button,
    * the stars and dots currently in the login and password fields are removed, and
    * default appearances to all fields are imposed. This is achieved by calling the
    * onResume method, which restarts the activity.
    * 
    * @param view The current view.
    */
   public void restoreFields(View view)
   {
	   	this.onResume();
   }
}
	

	


