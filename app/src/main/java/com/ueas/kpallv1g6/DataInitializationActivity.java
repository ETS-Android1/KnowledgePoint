package com.ueas.kpallv1g6;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.w3c.dom.Document;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * Unpacks data on which the application depends from the
 * /sdcard/Android/obb folder, and installs it directly into the /sdcard folder. The data is initially
 * in the form of a zip file, and must be unzipped. 
 * 
 * Graphically, this activity is identical to the WelcomePage Activity, except that it also presents a 
 * progress notification. Once data-installation is complete, the Welcome Page proper is brought up. 
 * @author tony.hillman@ultra-as.com
**/
public class DataInitializationActivity extends Activity 
{
	/**
	 * Flag that determines failure to find zip.
	 * 
	 */
	private int failureFlag = 0;
	
	/**
	 * Tag for writing to Android LogCat.
	 * 
	 */
	private final static String TAG0 = "DIA:onCreate";
	
	/**
	 * A progress dialog.
	 * 
	 */
	ProgressDialog progressX = null;
	
	/** 
	 * Boolean to signify whether zip extraction works successfully.
	 * 
	 */
	private Boolean bool;
	
	/**
	 * The name of this application's domain, used in identifying the path to the zip file.
	 * 
	 */
	String domainName = null;
	
	/**
	 * The name of the zip file that is to be accessed and opened.
	 * 
	 */
	String zipFileName = null;
	
	/**
	 * Array of strings representing top-level directory contents of the previous version
	 * of the application. These are removed as part of the data-initialization process, and 
	 * the contents of the new version of the appliccation substituted.
	 * 
	 */
	// These are the expected prior contents. We need to extract these into an XML
	// file. FIX. Note that the routine below that performs deletion also does some clean-up,
	// removing stray html and xml files that might remain (this being the most
	// likely way that unexpected old versions leave files around).
	//
	// Note also that one file is omitted from this list, which is the html file. The
	// names of such files vary with each deployment. So, rather than trying to
	// specify exactly here, we'll handle with the aforementioned cleanup.
	//
	String[] topLevelDirectoryContents = new String[]{  "adminguides", "video",
														"images", "infrastructure",
														"prior-activities", "shows", "userguides",
														"voice", "all_documents.xml", 
														"ultraapexdocstyle.css",
													 };
	
	/**
	 * Establishes the layout as that
	 * described in data_initialization_activity.xml. It presents a progress dialog to the user,
	 * and starts a background process that determines whether up-to-date data is already in
	 * place on the sdcard. If data is in place, the WelcomePage Activity is started. If data is
	 * not in place, the zip file containing appropriate data is located and unzipped, and the
	 * data transferred to the sdcard. The WelcomePage Activity is then started.
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
				setContentView(R.layout.data_initialization_activity_for_zero___four_point_three);
				Log.w(TAG0, "Set Data Initialization content for 0.\n");
				break;
				
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				setContentView(R.layout.data_initialization_activity_for_one_zero___four_point_zero_etc);
				Log.w(TAG0, "Set Data Initialization content for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				setContentView(R.layout.data_initialization_activity_for_two_zero___four_point_three_etc);
				Log.w(TAG0, "Set Data Initialization content for 20.\n");
				break;
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				setContentView(R.layout.data_initialization_activity_for_three_zero___four_point_seven);
				Log.w(TAG0, "Set Data Initialization content for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				setContentView(R.layout.data_initialization_activity_for_four_zero___seven);
				Log.w(TAG0, "Set Data Initialization content for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				setContentView(R.layout.data_initialization_activity_for_five_zero___ten_v1);
				Log.w(TAG0, "Set Data Initialization content for 50.\n");
				break;
			
		    // This is a 1080x1920 generic-size device.
			//
			case 60:
				setContentView(R.layout.data_initialization_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Data Initialization content for 60.\n");
				break;
			
			// This is a 1080x1920 5.7 inch device.
			//
			case 70:
				setContentView(R.layout.data_initialization_activity_for_seven_zero___five_point_seven);
				Log.w(TAG0, "Set Data Initialization content for 70.\n");
				break;
			
			// This is a 1200x1920 7 inch device.
			//
			case 80:
				setContentView(R.layout.data_initialization_activity_for_eight_zero___seven);
				Log.w(TAG0, "Set Data Initialization content for 80.\n");
				break;
			
			// This is a 1200x1920 10 inch device.
			//
			case 90:
				setContentView(R.layout.data_initialization_activity_for_nine_zero___ten);
				Log.w(TAG0, "Set Data Initialization content for 90.\n");
				break;
				
			// This is a 1200x1920 10 inch device.
			//
			case 100:
				setContentView(R.layout.data_initialization_activity_for_one_zero_zero___seven);
				Log.w(TAG0, "Set Data Initialization content for 100.\n");
				break;

			// This is a 1200x1920 10 inch device.
			//
			case 110:
				setContentView(R.layout.data_initialization_activity_for_one_one_zero___five_point_one);
				Log.w(TAG0, "Set Data Initialization content for 110.\n");
				break;

			// If we are not sure, we use the following default.
			//
			default:
				setContentView(R.layout.data_initialization_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Data Initialization content for default.\n");
				break;			
		}
		 
		// Define a progress dialog, which can be shown during the background process used to
		// locate and open the zip file.
		//
		ProgressDialog progressX = new ProgressDialog(this, R.style.CustomDialog);
		progressX.setTitle("UltraAPEX Knowledge Point");
      	progressX.setMessage("Initializing application data...");
      	
      	// Create a background operation.
      	//
      	netOp50 dataInitOp = new netOp50(progressX); 
      	
      	// String passed here is a placeholder and is ignored.
      	//
		dataInitOp.execute("Hello"); 
		
		// Show the progress dialog to the user while the backgrounding is occurring.
		//
		progressX.show();	
	}
	
	/**
	 * Checks whether data appropriate
	 * to the current version of the application is indeed available on the sdcard. If it is not
	 * available, an appropriate zip file is sought for, unzipped, and its contents transferred to
	 * the sdcard.
	 *
	 */
	public class netOp50 extends AsyncTask<String, Void, Document>
	{		
		/**
		 * A progress notification object.
		 * 
		 */
		private ProgressDialog progressX;
		
		/**
		 * A constructor for netOp50, establishing the progress notification object as that
		 * defined in and passed by the onCreate method for the DataInitializationActivity 
		 * class.
		 * @params ProgressDialog A progress dialog.
		 * 
		 */
		public netOp50(ProgressDialog progressX)
		{
			this.progressX = progressX;
		}
		
		/**
		 * Checks whether current data already exists
		 * on the sdcard. If it does exist, it takes no action. If it does not exist, it
		 * seeks a zip file in the /sdcard/Android/obb directory, and unpacks this onto the
		 * sdcard.
		 * 
		 * @params String... params Currently unused.
		 * 
		 * @return A Document object, currently unused.
		 * 
		 */
		@Override		
		protected Document doInBackground(String... params)
		{
			Log.w(TAG0, "In doInBackground now.....");
			
			// We have no need to return anything at this point. We may extend this
			// method later, however.
			//
			Document doc = null;
			
			// The domain name, the zip file name, and the data version are all used
			// in tracking down the zip file in /sdcard/Android/obb. All three values
			// are maintained for this version of the app in the resource file
			// strings.xml.
			//
			domainName = getResources().getString(R.string.domain_name);
			zipFileName = getResources().getString(R.string.zip_file_name);
			String dataVersionString = getResources().getString(R.string.data_version);
			
			// Establish the pathname to the html file within the knowledgePointSD folder,
			// in the sdcard top-level directory. The name of this file should accurately
			// reflect the data version registered in strings.xml.
			//
			File topLevelFile = new File(Environment.getExternalStorageDirectory() 
					+ "/" + "knowledgePointSD" + "/" +  dataVersionString);
			
			Log.w(TAG0, "Seeking top level file named: " + Environment.getExternalStorageDirectory() + "/" 
        			+ "knowledgePointSD" + "/" +  dataVersionString);
			
			// If the html file is there, we need do no unpacking, since we assume the
			// data is present and up-to-date. NOTE: NEED TO REVISE THIS, SINCE FALLING
			// BACK A VERSION CURRENTLY FINDS THE OLD HTML FILE, WHICH WE ARE NOT YET
			// REMOVING, AND SO DOES NOT PROVOKE THE APPROPRIATE UNPACKING. FIX.
			//
			if (topLevelFile.exists() )  
	        {
				Log.w(TAG0, "Top Level File exists: data is current, run is not first-time.\n");
				Log.w(TAG0, "Not unpacking zip.\n");  
	        } 
	        else   
	        { 
	        	// Otherwise, if there is no top-level file, or if there is a top-level file, but
	        	// the download is fresh, and so requires the data to be refreshed, then we
	        	// unpack the zip file and overwrite any previously existing data.
	        	//
	        	Log.w(TAG0, "Top Level File does not exist: this is a new download, or data is obsolete.\n");
	        	Log.w(TAG0, "Unpacking zip.\n");
	        	
	        	// Make a base dir. This is a workaround for an apparent Android bug, whereby the top-level
	        	// directory in the zip file is presumed already to have been brought to existence prior to
	        	// its extraction. 
	        	//
	        	File targetDirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/knowledgePointSD");
	        	targetDirPath.mkdirs();
	        	
	        	// Remove all old top-level directory contents.
	        	//
	        	Log.w(TAG0, "Deleting top-level directory contents.\n");
	        	
	        	for (int i = 0; i <= topLevelDirectoryContents.length - 1; i++)
	        	{
	        		File topLevelItem = new File(Environment.getExternalStorageDirectory().getAbsolutePath() 
		        			+ "/knowledgePointSD/" + topLevelDirectoryContents[i]);
	        		
		        	recursiveDelete(topLevelItem);
	        	}
	        	
	        	// Perform additional clean-up, finding any remaining html or xml
	        	// files (typically we shouldn't have to worry about directories being
	        	// left over, since their names won't change from release to release).
	        	//
	        	for (File remainingFile : targetDirPath.listFiles()) 
	        	{
	        	    if (remainingFile.isFile())
	        	    {
	        	        String remainingFileName = remainingFile.getName();
	        	        
	        	        // Make sure that the file contains all the substrings that identify
	        	        // it as an html version-declarator. Once it has been so identified,
	        	        // delete it.
	        	        //
	        	        if (	
	        	        		remainingFileName.contains(".html") 
	        	        		&& remainingFileName.contains("-ua-") 
	        	        		&& remainingFileName.contains("x")
	        	           )
	        	        {
	        	        	Log.w(TAG0, "Deleting remaining file: " + remainingFileName);
	        	        	remainingFile.delete();
	        	        }
	        	    }
	        	}
	        	
	        	Log.w(TAG0, "Deletion of top-level directory contents complete.\n");
	        	
	        	// Check to see whether the zip file is there. If it is not, set the failureFlag, so that
	        	// we can avoid trying to access it further, and can return to the login activity for
	        	// a prompt exit..
	        	//
				File zipExistenceTester = new File(Environment.getExternalStorageDirectory().getAbsolutePath() 
						+ "/Android/obb" + "/" + domainName + "/" + zipFileName);

				// FIX. Find a way of exiting with a notification if the zip file is not
				// present. Right now, we simply fail, and the app disappears. 
				//
				//
				if (!zipExistenceTester.exists())
				{
					Log.w(TAG0, "The zip file is not present...\n");

					failureFlag = 1;
				}
	        	
	        	// The zip file that contains the data for the application is divided internally into two principal areas, which
	        	// are the generic and the specific. The generic contents get unpacked onto every Android platform. The specific
	        	// section is divided into five areas, only one of which is unpacked for the current platform. The specific contents
	        	// are the html files, and are integrated seamlessly into the knowledgePointSD directory structure when unpacking
	        	// occurs. 
	        	
	        	// Extract the generic elements from the zip file. This results in the file-hierarchy underneath "generic" being
	        	// copied into the file system of the sdcard, with the exception of the "generic" directory itself, this being
	        	// omitted. Note that this routine also copies over the current data version html file.
	        	//
	        	Log.w(TAG0, "Calling extract routine.\n"); 
	        	
	        	if (failureFlag == 0)
	        	{
		        	bool = extractGenericsFromZip(Environment.getExternalStorageDirectory().getAbsolutePath()
		        			+ "/Android/obb" + "/" + domainName + "/" + zipFileName, 
		        					  Environment.getExternalStorageDirectory().getAbsolutePath(), "generic");
		        	
		        	// Prepare to extract the platform-specific elements from the zip file. These are divided into multiple
		        	// areas, each of which bears a unique number.
		        	//
		        	String htmlSize = "";
					
		        	// Prepare to identify which specific area is which. The following is a bit unnecessary, but helps to avoid
		        	// confusion with number 0 being interpreted as "0" instead of "00".
		        	//
					switch (MetaDataForSuite.htmlSizingSpecification)
					{
						case 0:
							htmlSize = "00";
							break;
							
						case 10:
							htmlSize = "10";
							break;
							
						case 20:
							htmlSize = "20";
							break;
							
						case 30:
							htmlSize = "30";
							break;
							
						case 40:
							htmlSize = "40";
							break;
							
						default:
							htmlSize = "00";
							break;	
					}
					
					// Extract the specific elements from the zip file. This results in all the files underneath one
					// of the above-specified areas("00", "10", "20", etc) being integrated into the knowledgePointSD
					// file-system hierarchy. All the files are html files.
					//
					bool = extractSpecificsFromZip(Environment.getExternalStorageDirectory().getAbsolutePath()
		        							+ "/Android/obb" + "/" + domainName + "/" + zipFileName,
		        									Environment.getExternalStorageDirectory().getAbsolutePath(), "specific", htmlSize);	
	        	}
	        	else
	        	{
	        		Log.w(TAG0, "Made no attempt to unzip, since file is not present.");
	        	}
	        }	

			return doc;
		}
	
		/**
		 * Dismisses the progress notification that was displayed, and starts the intent that
		 * initializes the Welcome Page Activity. It then finishes the Data Initialization
		 * Activity.
		 * 
		 * @params Document Currently unused.
		 * 
		 */
		protected void onPostExecute(Document thedoc)
		{
			// Get rid of the "Preparing..." notification.
		    progressX.dismiss();
		    
		    Log.w(TAG0, "onPostExecute starting..."); // data_not_found_activity_for_seven_zero___five_point_seven.xml
		    
		    // If things have not gone well, exit. 
		    //
		    if (failureFlag == 1)
		    {
		    	Log.w(TAG0, "No data. Need to exit.");
		    	
		    	// We exit by first going to the DataNotFound Activity. This allows us to
		    	// show a notification to the user, explaining the situation. The DataNotFound
		    	// Activity will, other than for the notification, look identical to the Login
		    	// Activity. Note that the transition is so fast that the DataInitialization Activity
		    	// itself does not become visually identifiable: it just looks as if the notification
		    	// appears over the Login Activity.
		    	//
		    	// Once at the DataNotFound Activity, we will exit in the standard way once the
		    	// user has read the notification: this requires an actual return to the Login Activity
		    	// (since it is the initial activity, from which clean exit can occur).
		    	//
				Intent intent = new Intent(DataInitializationActivity.this, DataNotFoundActivity.class);
				startActivity(intent);
		    }
		    
		    // Otherwise, proceed to the welcome page.
		    //
		    else
		    {
		    	Intent newIntent = new Intent(DataInitializationActivity.this, WelcomePageActivity.class);
				Log.w(TAG0, "Now starting WelcomePageActivity.");
				startActivity(newIntent);
		    }
					
			finish();
		    
			Log.w(TAG0, "doInBackground completed.");
		}
	    
		/**
		 * Extracts platform-generic elements of a zip file from a given source to a given destination. 
		 * 
		 * @param pathOfZip The path of the zip file to be located and extracted.
		 * 	
		 * @param pathToExtract The top-level destination of the extracted contents.
		 * 
		 * @param keyFolder The name of the folder, within the zip file, that contains the contents
		 * to be extracted. The folder itself is not copied, nor does it appear in the fully copied
		 * file hierarchy: only its contents are copied.
		 * 
		 * @return boolean A boolean specifying success or failure.
		 * 
		 * 
		 */
	    private boolean extractGenericsFromZip(String pathOfZip, String pathToExtract, String keyFolder)
	    {
	    	// Along with the file-contents under "generic", we'll also want to extract the
	    	// data version file-indicator, which is an html file, located at the top level of
	    	// knowledgePointSD.
	    	//
	    	String dataVersionString = getResources().getString(R.string.data_version);
	    	
	    	// Establish variables for copying buffers of data from the zip file to
	    	// the sdcard.
	    	//
	    	int BUFFER_SIZE = 1024;
	    	int size;
	    	byte[] buffer = new byte[BUFFER_SIZE];
	    	
	    	try 
	    	{
	    		// Create a file that will be the top-level directory into which files get
	    		// copied.
	    		//
	    		File f = new File(pathToExtract);
	    		
	    		// If the folder to which we are going to extract the contents does not yet exist,
	    		// create it on the sdcard. (Note that we currently expect this to be the /sdcard
	    		// directory, which does indeed already exist: so, this routine is not used.)
	    		//
	    		if(!f.isDirectory()) 
	    		{
	    			//Log.w(TAG0, "Making directory for " + pathToExtract);
	    			
	    			// If necessary, make the directory, along with any super-directories that
	    			// constitute its path.
	    			f.mkdirs();
	    		}
	           
	    		// Create a zip input stream for the zip to be unpacked.
	    		//
	    		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(pathOfZip), BUFFER_SIZE));
	    		
	    		// Descend through the zip file, unpacking contents as appropriate.
	    		//
	    		try 
	    		{
	    			// The first getNextEntry sets the entry to the first element in the zip hierarchy. Subsequent
	    			// calls move it to each next entry in turn. When there are no more entries, its value becomes
	    			// null, and this is used (below) to end the main while loop.
	    			//
	    			ZipEntry ze = zin.getNextEntry();
	    			
	    			// Note that the keyFolder is simply the name of the directory itself. The name of the
	    			// entry is a string that also consists of the super-directories that constitute the
	    			// directory's full path-name.
	    			//
	    			//Log.w(TAG0, "keyFolder is " + keyFolder);
	    			//Log.w(TAG0, "ze is " + ze.getName());
				    
				    while (ze != null)
				    {
				    	// The ze.getName() call returns the complete pathname of the file or directory we
				    	// are currently looking at. So, split its principal names into an array, for
				    	// inspection and possible modification.
				    	//
				    	String parts[] = ze.getName().split("/");
				    	
				    	// See what we currently have.
				    	//
				    	for (int x = 0; x < parts.length; x++)
				    	{
				    		//Log.w(TAG0, "Part number " + x + " is: " + parts[x]);
				    	}
				    	
				    	// Just make clear the current file target.
				    	//
				    	String fileName = parts[parts.length - 1];
				    	//Log.w(TAG0, "The current file name at the end of the path is " + fileName);
				    	
				    	// If the current pathname contains the name of the keyFolder, that means we want its
				    	// subcontents. However, we do not want the keyFolder itself, which has only the
				    	// function of a repository within the zipfile: it must not appear within the final
				    	// filesystem.
				    	//
				    	if (ze != null && ze.getName().contains(keyFolder) && !fileName.equals(keyFolder))
		    			{
		    				//Log.w(TAG0, "keyFolder determined a valid constituent. Will create a copy-path. \n");
		    				
		    				// Create a target path that is identical to the entry-name within 
		    				// the zip file...EXCEPT THAT IT OMITS THE KEY FOLDER. So, /alpha/keyFolder/beta
		    				// will become /alpha/beta.
		    				//
		    				// String to hold the new path.
		    				//
		    				String newPath = "";
		    				
		    				// Temporary string for manipulating subcomponents of the path.
		    				//
		    				String tempString = "";
		    				
		    				// Go through the array, examining each part of the path. If it is a part
		    				// we want in the final path for copying, add it to the result-string. Otherwise,
		    				// if it specifies the keyFolder, omit it.
		    				//
		    				for (int y = 0; y < parts.length; y++)
		    				{
		    					//Log.w(TAG0, "Part number " + y + " is: " + parts[y]);
		    					
		    					// If this part is the name of the keyFolder...
		    					//
		    					if (keyFolder.equals(parts[y]))
		    					{
		    						// Do nothing. We don't concatenate the name of the key folder, just
		    						// omit it, and use only the super and sub folders in the path to which
		    						// we copy.
		    					}
		    					
		    					// But if this part is not the name of the keyFolder...
		    					//
		    					else
		    					{
		    						// Add the current part to the result-string.
		    						//
		    						tempString = parts[y];
		    						newPath = newPath + "/" + tempString;
		    						//Log.w(TAG0, "newPath is now: " + newPath);
		    						tempString = "";
		    					}
		    				}
		    				
		    				//Log.w(TAG0, "newPath has been finalised as: " + newPath);
		    				
		    				// Now append the new path to the path for the base dir of
		    				// the sdcard.
		    				//
		    				String pathForCopying = pathToExtract + newPath;
		    				
		    				//Log.w(TAG0, "pathForCopying is thus finalised as: " + pathForCopying);
		
		    				// If this entry within the zip file is a directory, make a corresponding
		    				// directory in the file system.
		    				//
		    				if (ze.isDirectory()) 				
		    				{
		    					//Log.w(TAG0, "Making a directory in the filesystem for " + pathForCopying);

		    					// Create the file in the filesystem.
		    					//
		    					File unzipFile = new File(pathForCopying);
		    					
		    					// If the file is not a directory, make it one.
		    					//
		    					if(!unzipFile.isDirectory()) 
		    					{
		    						// Make the directory, plus any super-directories in the filepath.
		    						//
		    						unzipFile.mkdirs();
		    					}
		    				}
		    				
	    					// Now, since we know we are in a part of the zipfile that lies directly under the keyFolder, we
		    				// know that these are the files we do want to copy over. So, recursively copy the remainder of the 
		    				// folder: always making sure that the folder-name is part of the path, before copying.
	    					//
		    				// New string, to hold the path for copying to.
		    				//
		    				String newerPath = "";
		    				
		    				// As long as we are not at the end...
		    				//
		    				if ((ze = zin.getNextEntry()) != null)
			    			{
			    				// The ze.getName() call returns the complete pathname of the file or directory we
						    	// are currently looking at. So, split its principals names into an array, for
						    	// inspection and possible modification.
						    	//
			    				String newParts[] = ze.getName().split("/");
	
						    	// See what we currently have.
						    	//
						    	for(int x = 0; x < newParts.length; x++)
						    	{
						    		//Log.w(TAG0, "newPart number " + x + " is: " + newParts[x]);
						    	}
						    	
						    	// Check each part of the path. If it is the name of the keyFolder, omit
						    	// it from the result-string (which is the final path to be copied to). If
						    	// it is not the keyFolder, add it to the result-string.
						    	//
			    				for (int y = 0; y < newParts.length; y++)
			    				{
			    					// If this part is the name of the keyFolder...
			    					//
			    					if (keyFolder.equals(newParts[y]))
			    					{
			    						// Do nothing. We don't concatenate the name of the keyFolder, just
			    						// omit it, and use only the super and sub folders in the path to which
			    						// we copy.
			    					}
			    					
			    					// Otherwise, if it is not the name of the keyFolder...
			    					//
			    					else
			    					{
			    						// Add the current part to the result-string.
			    						//
			    						tempString = newParts[y];
			    						newerPath = newerPath + "/" + tempString;
			    						//Log.w(TAG0, "newerPath is now: " + newerPath);
			    						tempString = "";
			    					}
			    				}
			    				
			    				//Log.w(TAG0, "newerPath has been constructed as: " + newerPath);
			    				
			    				// Now append the new path to the path for the base dir of
			    				// the sdcard.
			    				//
			    				String newerPathForCopying = pathToExtract + newerPath;
			    				
			    				//Log.w(TAG0, "newerPathForCopying is thus finalised as: " + newerPathForCopying);
			    				
			    				// Copy the current entry in the zip file to the newly specified target
			    				// location on the sdcard.
			    				//
			    				if (ze.getName().contains(keyFolder))
			    				{
			    					//Log.w(TAG0, "newerPath contains " + keyFolder);
			
				    				// If the entry within the zip file is a directory, make a corresponding
				    				// directory in the file system.
				    				//
				    				if (ze.isDirectory()) 
				    				{
				    					//Log.w(TAG0, "newerPath specifies a directory. Copying.");
				    					File unzipFile = new File(newerPathForCopying);
				    					
				    					if(!unzipFile.isDirectory()) 
				    					{
				    						unzipFile.mkdirs();
				    					}
				    				}
				    				
				    				// Otherwise, if the entry within the zip file is a file, rather than a
				    				// directory...
				    				//
				    				else 
				    				{
				    					//Log.w(TAG0, "newerPath specifies a file.");
				    					
				    					// If the entry within the zip file is not a directory, copy this
				    					// file into the file system - as long as it is one of the expected
				    					// formats.
				    					//
				    					if (newerPath.contains(".html") 
				    							|| newerPath.contains(".css") 
				    							|| newerPath.contains(".mp3")
				    							|| newerPath.contains(".mp4")
				    							|| newerPath.contains(".xml")
				    							|| newerPath.contains(".png")
				    							|| newerPath.contains(".jpg")
				    							|| newerPath.contains(".gif")
				    						) 															
				    					{
				    						//Log.w(TAG0, "newerPath contains " + keyFolder + " and .html or .css, so performing copy.\n");
				    						
				    						// Create the output stream for copying the file across.
				    						//
				    						FileOutputStream out = new FileOutputStream(newerPathForCopying, false);
				    						BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
				    						
				    						// Perform the copy routine.
				    						//
				    						try 
				    						{
				    							while ( (size = zin.read(buffer, 0, BUFFER_SIZE)) != -1 ) 
				    							{
				    								fout.write(buffer, 0, size);
				    							}
				
				    							zin.closeEntry();
				    						}
				    						catch (Exception e) 
				    						{
				    							//Log.w(TAG0, "Unzip exception 1:" + e.toString());
				    						}
				    						
				    						// Clear and close the output stream.
				    						//
				    						finally 
				    						{
				    							fout.flush();
				    							fout.close();
				    						}
				    					}
				    					else
				    					{
				    						//Log.w(TAG0, "newerPath did not point to an html file. So, no copy has occurred.");
				    					}
				    				}
			    				}
			    				else
			    				{
			    					//Log.w(TAG0, "Path does not contain " + keyFolder + ".\n");
			    				}
			    			}		
	    				}
				    	
				    	// If the current entry within the zip file did not include the keyFolder in its path (eg /alpha/beta), or included
				    	// the keyFolder as the conclusion of the path (eg /alpha/keyFolder), or was not the data version indicator, we are not 
				    	// interested in copying it; so just move on to the next entry in the zip file, and evaluate that.
				    	//
		    			else
		    			{
		    				//Log.w(TAG0, "Either didn't find the keyFolder, or found it as end-target. So, leaving this while rep, thereby seeking the next item.\n");
		    				
		    				// Move to the next entry in the zip file, ready for evaluating whether it
		    				// should be copied.
		    				//
		    				ze = zin.getNextEntry();
		    			}
		    			
		    			//Log.w(TAG0, "Ending current while block.\n");
		    			
			    	}		// End of the while expression. As long as ze is not null, the while now runs again,
				    		// and the next zip-file entry thus evaluated.
	    		}
	    		catch (Exception e) 
	    		{
	    			//Log.w(TAG0, "Unzip exception2 :" + e.toString());
	    		}
	           
	    		finally 
	    		{
	    			// We are done with the copying, so close the input stream.
	    			//
	    			zin.close();
	    		}
	           
	    		return true;
	      	}
	    	catch (Exception e) 
	    	{
	    		Log.w(TAG0, "Unzip exception :" + e.toString());
	    	}
	       
	    	return false;
	    }
	}
	
	/**
	 * Extracts platform-specific elements of a zip file from a given source to a given destination. The source and
	 * destination paths are expected to differ. This allows the conditional loading of files, in
	 * accordance with details of the current underlying hardware platform.
	 * 
	 * @param pathOfZip The path of the zip file to be located and extracted.
	 * 	
	 * @param pathToExtract The top-level destination of the extracted contents.
	 * 
	 * @param keyFolder The name of the folder, within the zip file, that contains the contents
	 * to be extracted. The folder itself is not copied, nor does it appear in the fully copied
	 * file hierarchy: only its html-file contents are copied.
	 * 
	 * @return boolean A boolean specifying success or failure.
	 * 
	 * 
	 */
    private boolean extractSpecificsFromZip(String pathOfZip, String pathToExtract, String keyFolder, String htmlSizing)
    {
    	Log.w(TAG0, "extractSomeOfZip called...\n"); 
    	
    	// Establish variables for copying buffers of data from the zip file to
    	// the sdcard.
    	//
    	int BUFFER_SIZE = 1024;
    	int size;
    	byte[] buffer = new byte[BUFFER_SIZE];
    	
    	try 
    	{
    		// Create a file that will be the top-level directory into which files get
    		// copied.
    		//
    		File f = new File(pathToExtract);
    		
    		// If the folder to which we are going to extract the contents does not yet exist,
    		// create it on the sdcard. (Note that we currently expect this to be the /sdcard
    		// directory, which does indeed already exist: so, this routine is not used.)
    		//
    		if(!f.isDirectory()) 
    		{
    			Log.w(TAG0, "ES: Making directory for " + pathToExtract);
    			
    			// If necessary, make the directory, along with any super-directories that
    			// constitute its path.
    			f.mkdirs();
    		}
           
    		// Create a zip input stream for the zip to be unpacked.
    		//
    		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(pathOfZip), BUFFER_SIZE));
    		
    		// Descend through the zip file, unpacking contents as appropriate.
    		//
    		try 
    		{
    			// The first getNextEntry sets the entry to the first element in the zip hierarchy. Subsequent
    			// calls move it to each next entry in turn. When there are no more entries, its value becomes
    			// null, and this is used (below) to end the main while loop.
    			//
    			ZipEntry ze = zin.getNextEntry();
    			
    			// Note that the keyFolder is simply the name of the directory itself. The name of the
    			// entry is a string that also consists of the super-directories that constitute the
    			// directory's full path-name.
    			//
    			//Log.w(TAG0, "keyFolder is " + keyFolder);
    			//Log.w(TAG0, "ze is " + ze.getName());
			    
			    while (ze != null)
			    {
			    	// The ze.getName() call returns the complete pathname of the file or directory we
			    	// are currently looking at. So, split its principal names into an array, for
			    	// inspection and possible modification.
			    	//
			    	String parts[] = ze.getName().split("/");
			    	
			    	// See what we currently have.
			    	//
			    	for (int x = 0; x < parts.length; x++)
			    	{
			    		//Log.w(TAG0, "ES1: Part number " + x + " is: " + parts[x]);
			    	}
			    	
			    	// Just make clear the current file target.
			    	//
			    	String fileName = parts[parts.length - 1];
			    	//Log.w(TAG0, "The current file name at the end of the path is " + fileName);
			    	
			    	// If the current pathname contains the name of the keyFolder, and the htmlSizing, that means we want its
			    	// subcontents. However, we do not want the keyFolder of the htmlSizing itself.
			    	//
			    	if (ze != null && ze.getName().contains(keyFolder) 
			    									&& ze.getName().contains(htmlSizing) 
			    									&& !fileName.equals(htmlSizing)
			    									&& !fileName.equals(keyFolder))
	    			{
	    				//Log.w(TAG0, "keyFolder determined a valid constituent. Will create a copy-path. \n");
	    				
	    				// Create a target path that is identical to the entry-name within 
	    				// the zip file...EXCEPT THAT IT OMITS BOTH THE KEY FOLDER AND THE HTMLSIZING. So, /alpha/keyFolder/10/beta
	    				// will become /alpha/beta.
	    				//
	    				// String to hold the new path.
	    				//
	    				String newPath = "";
	    				
	    				// Temporary string for manipulating subcomponents of the path.
	    				//
	    				String tempString = "";
	    				
	    				// Go through the array, examining each part of the path. If it is a part
	    				// we want in the final path for copying, add it to the result-string. Otherwise,
	    				// if it specifies the keyFolder, omit it.
	    				//
	    				for (int y = 0; y < parts.length; y++)
	    				{
	    					//Log.w(TAG0, "Part number " + y + " is: " + parts[y]);
	    					//Log.w(TAG0, "htmlSizing is " + htmlSizing);
	    					//Log.w(TAG0, "keyFolder is " + keyFolder);
	    					
	    					// If this part is the name of the keyFolder or the htmlSizing...
	    					//
	    					if (keyFolder.equals(parts[y]) || htmlSizing.equals(parts[y]))
	    					{
	    						// Do nothing. We don't concatenate the name of the key folder, just
	    						// omit it, and use only the super and sub folders in the path to which
	    						// we copy.
	    					}
	    					
	    					// But if this part is not the name of the keyFolder...
	    					//
	    					else
	    					{
	    						// Add the current part to the result-string.
	    						//
	    						if (!htmlSizing.equals(parts[y]))
	    						{
	    							tempString = parts[y];
	    							newPath = newPath + "/" + tempString;
	    							//Log.w(TAG0, "newPath is now: " + newPath);
	    							tempString = "";
	    						}
	    					}
	    				}
	    				
	    				//Log.w(TAG0, "newPath has been finalised as: " + newPath);
	    				
	    				// Now append the new path to the path for the base dir of
	    				// the sdcard.
	    				//
	    				String pathForCopying = pathToExtract + newPath;
	    				
	    				//Log.w(TAG0, "ES1: pathForCopying is thus finalised as: " + pathForCopying);
	
	    				// If this entry within the zip file is a directory, make a corresponding
	    				// directory in the file system.
	    				//
	    				if (ze.isDirectory()) 				
	    				{
	    					Log.w(TAG0, "Making a directory in the filesystem for " + pathForCopying);

	    					// Create the file in the filesystem.
	    					//
	    					File unzipFile = new File(pathForCopying);
	    					
	    					// If the file is not a directory, make it one.
	    					//
	    					if(!unzipFile.isDirectory()) 
	    					{
	    						// Make the directory, plus any super-directories in the filepath.
	    						//
	    						unzipFile.mkdirs();
	    					}
	    				}
	    				
    					// Now, since we know we are in a part of the zipfile that lies directly under the keyFolder, we
	    				// know that these are the files we do want to copy over. So, recursively copy the remainder of the 
	    				// folder: always making sure that the folder-name is part of the path, before copying.
    					//
	    				// New string, to hold the path for copying to.
	    				//
	    				String newerPath = "";
	    				
	    				// As long as we are not at the end...
	    				//
	    				if ((ze = zin.getNextEntry()) != null)
		    			{
		    				// The ze.getName() call returns the complete pathname of the file or directory we
					    	// are currently looking at. So, split its principals names into an array, for
					    	// inspection and possible modification.
					    	//
		    				String newParts[] = ze.getName().split("/");

					    	// See what we currently have.
					    	//
					    	for (int x = 0; x < newParts.length; x++)
					    	{
					    		//Log.w(TAG0, "ES2: newPart number " + x + " is: " + newParts[x]);
					    	}
					    	
					    	// Check each part of the path. If it is the name of the keyFolder, omit
					    	// it from the result-string (which is the final path to be copied to). If
					    	// it is not the keyFolder, add it to the result-string.
					    	//
		    				for (int y = 0; y < newParts.length; y++)
		    				{
		    					// If this part is the name of the keyFolder...
		    					//
		    					if (keyFolder.equals(newParts[y]) || htmlSizing.equals(newParts[y]))
		    					{
		    						// Do nothing. We don't concatenate the name of the keyFolder, just
		    						// omit it, and use only the super and sub folders in the path to which
		    						// we copy.
		    					}
		    					
		    					// Otherwise, if it is not the name of the keyFolder...
		    					//
		    					else
		    					{
		    						// Add the current part to the result-string.
		    						//
		    						tempString = newParts[y];
		    						newerPath = newerPath + "/" + tempString;
		    						//Log.w(TAG0, "newerPath is now: " + newerPath);
		    						tempString = "";
		    					}
		    				}
		    				
		    				//Log.w(TAG0, "newerPath has been constructed as: " + newerPath);
		    				
		    				// Now append the new path to the path for the base dir of
		    				// the sdcard.
		    				//
		    				String newerPathForCopying = pathToExtract + newerPath;
		    				
		    				//Log.w(TAG0, "E2S: newerPathForCopying is thus finalised as: " + newerPathForCopying);
		    				
		    				// Copy the current entry in the zip file to the newly specified target
		    				// location on the sdcard.
		    				//
		    				if (ze.getName().contains(keyFolder))
		    				{
		    					//Log.w(TAG0, "newerPath contains " + keyFolder);
		
			    				// If the entry within the zip file is a directory, make a corresponding
			    				// directory in the file system.
			    				//
			    				if (ze.isDirectory()) 
			    				{
			    					Log.w(TAG0, "newerPath specifies a directory. Copying.");
			    					File unzipFile = new File(newerPathForCopying);
			    					
			    					// FIX. This is a workaround for a bug whereby the htmlSizing
			    					// directory that follows the current one is copied into the sdcard
			    					// space. Somehow, it is evading the logic above that is designed to
			    					// catch it. It only happens to the one, subsequent directory, and that
			    					// directory does not get populated. So, pending further inquiry, I'll
			    					// just check for it here, and make sure we don't do such a copy.
			    					//
			    					if (!unzipFile.isDirectory() && !newerPath.contains("00")
																 && !newerPath.contains("10")
																 && !newerPath.contains("20")
																 && !newerPath.contains("30")
																 && !newerPath.contains("40")
			    						)  
			    					{
			    						unzipFile.mkdirs();
			    					}
			    				}
			    				
			    				// Otherwise, if the entry within the zip file is a file, rather than a
			    				// directory...
			    				//
			    				else 
			    				{
			    					//Log.w(TAG0, "newerPath specifies a file.");
			    					
			    					// If the entry within the zip file is not a directory, copy this
			    					// file into the file system - as long as it is one of the expected
			    					// formats.
			    					//
			    					if (newerPath.contains(".html") 
			    							|| newerPath.contains(".css") 
			    							|| newerPath.contains(".mp3")
			    							|| newerPath.contains(".mp4")
			    							|| newerPath.contains(".xml")
			    							|| newerPath.contains(".png")
			    							|| newerPath.contains(".jpg")
			    							|| newerPath.contains(".gif")
			    						) 															
			    					{
			    						//Log.w(TAG0, "ES2: newerPath contains " + keyFolder + " and .html or .css, so performing copy.\n");
			    						
			    						// Create the output stream for copying the file across.
			    						//
			    						FileOutputStream out = new FileOutputStream(newerPathForCopying, false);
			    						BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
			    						
			    						// Perform the copy routine.
			    						//
			    						try 
			    						{
			    							while ( (size = zin.read(buffer, 0, BUFFER_SIZE)) != -1 ) 
			    							{
			    								fout.write(buffer, 0, size);
			    							}
			
			    							zin.closeEntry();
			    						}
			    						catch (Exception e) 
			    						{
			    							Log.w(TAG0, "Unzip exception 1:" + e.toString());
			    						}
			    						
			    						// Clear and close the output stream.
			    						//
			    						finally 
			    						{
			    							fout.flush();
			    							fout.close();
			    						}
			    					}
			    					else
			    					{
			    						//Log.w(TAG0, "newerPath did not point to an html file. So, no copy has occurred.");
			    					}
			    				}
		    				}
		    				else
		    				{
		    					//Log.w(TAG0, "Path does not contain " + keyFolder + ".\n");
		    				}
		    			}		
    				}
			    	
			    	// If the current entry within the zip file did not include the keyFolder in its path (eg /alpha/beta), or included
			    	// the keyFolder as the conclusion of the path (eg /alpha/keyFolder), we are not interested in copying it; so just
			    	// move on to the next entry in the zip file, and evaluate that.
			    	//
	    			else
	    			{
	    				//Log.w(TAG0, "Either didn't find the keyFolder, or found it as end-target. So, leaving this while rep, thereby seeking the next item.\n");
	    				
	    				// Move to the next entry in the zip file, ready for evaluating whether it
	    				// should be copied.
	    				//
	    				ze = zin.getNextEntry();
	    			}
	    			
	    			//Log.w(TAG0, "Ending current while block.\n");
	    			
		    	}		// End of the while expression. As long as ze is not null, the while now runs again,
			    		// and the next zip-file entry thus evaluated.
    		}
    		catch (Exception e) 
    		{
    			Log.w(TAG0, "Unzip exception2 :" + e.toString());
    		}
           
    		finally 
    		{
    			// We are done with the copying, so close the input stream.
    			//
    			zin.close();
    		}
           
    		return true;
      	}
    	catch (Exception e) 
    	{
    		Log.w(TAG0, "Unzip exception :" + e.toString());
    	}
       
    	return false;
    }
	
	/**
	 * Deletes a specified file. If the specified file is a directory, deletes all contents recursively,
	 * then deletes the empty directory.
	 * 
	 * @param fileOrDirectory
	 */
	private void recursiveDelete(File fileOrDirectory) 
	{
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                recursiveDelete(child);

        fileOrDirectory.delete();
    }
}

