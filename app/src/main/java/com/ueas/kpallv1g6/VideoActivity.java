package com.ueas.kpallv1g6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.net.Uri;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Plays a video, previously selected by the user from the Grid View For Video activity. Provides
 * buttons allowing the next or previous videos to be switched to, allowing the Grid View For Video
 * activity to be returned to, and allowing a dialog to be brought up, selection from which returns
 * the user to a textual passage within the documentation set, relevant to the subject of the
 * current video.
 * 
 * @author tony.hillman
 *
 */
public class VideoActivity extends Activity     
{	
	/**
	 * The number of chapters in the current document. This is always equal to the number of
	 * nodes in the source XML file, minus 3: since the last three nodes represent only the
	 * navigation elements that appear at the end of the List View.
	 * 
	 */
	private static int HIGHESTVIDEONUMBER = 0; 
	
	public static int idInt = 0;
	
	/**
	 * A progress dialog, to be displayed to the user while background
	 * operations are in progress.
	 * 
	 */
	public static ProgressDialog progress4 = null;
	
	public static int numberOfNodes = 0;
			
	private final static String TAG0 = "VideoPlayer: ";
	
	static final String KEY_SA = "see_also_item";

	static final String KEY_ID = "id";
	
	static final String KEY_TITLE = "title"; 
	
	static final String KEY_LOCATION = "location";
	
	static final String KEY_CHAP_NO = "chap_no";
	
	static final String KEY_CHAP_REF = "chap_ref";
	
	static final String KEY_DESCRIPTION = "description"; 
	
	static final String KEY_THUMBNAIL = "thumbnail";
	
	public static String videoInformation[] = null;
	
	/** 
	 * Flag to indicate whether or not this is the first time that the "More Details" dialog
	 * has been shown. If it has not previously been shown, then the data is added. Otherwise,
	 * no data is added, since such preparation has already occurred.
	 * 
	 */
	public static int whetherDialogPreviouslyShown = 0;  
	
	public static AdapterForDialog adapter = null;

	private ArrayList<HashMap<String, String>> seeAlsoList = new ArrayList<HashMap<String, String>>();
	private HashMap<String, String> map;
 
	@Override 
	protected void onCreate(Bundle savedInstanceState)   
	{
		super.onCreate(savedInstanceState);    
		
		// Establish the content view for the video activity. Do this conditionally, based on the 
		// screen-size/aspect-ratio combination we have already determined.
		//
		switch (MetaDataForSuite.currentAspectRatio)
		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				setContentView(R.layout.video_activity_for_zero___four_point_three);
				Log.w(TAG0, "Set Video content for 0.\n");
				break;
				
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				setContentView(R.layout.video_activity_for_one_zero___four_point_zero_etc);
				Log.w(TAG0, "Set Video content for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				setContentView(R.layout.video_activity_for_two_zero___four_point_three_etc);
				Log.w(TAG0, "Set Video content for 20.\n");
				break;
					
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				setContentView(R.layout.video_activity_for_three_zero___four_point_seven);
				Log.w(TAG0, "Set Video content for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				setContentView(R.layout.video_activity_for_four_zero___seven);
				Log.w(TAG0, "Set Video content for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				setContentView(R.layout.video_activity_for_five_zero___ten);
				Log.w(TAG0, "Set Video content for 50.\n");
				break;
		
		    // This is a 1080x1920 generic-size device.
			//
			case 60:
				setContentView(R.layout.video_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Video content for 60.\n");
				break;
			
			// This is a 1080x1920 5.7 inch device.
			//
			case 70:
				setContentView(R.layout.video_activity_for_seven_zero___five_point_seven);
				Log.w(TAG0, "Set Video content for 70.\n");
				break;
			
			// This is a 1200x1920 7 inch device.
			//
			case 80:
				setContentView(R.layout.video_activity_for_eight_zero___seven);
				Log.w(TAG0, "Set Video content for 80.\n");
				break;
			
			// This is a 1200x1920 10 inch device.
			//
			case 90:
				setContentView(R.layout.video_activity_for_nine_zero___ten);
				//setContentView(R.layout.video_activity);
				Log.w(TAG0, "Set Video content for 90....\n");
				break;
				
			// This is a 1440x900 7 inch device.
			//
			case 100:
				setContentView(R.layout.video_activity_for_one_zero_zero___seven);
				Log.w(TAG0, "Set Video content for 100.\n");
				break;

			// This is a 1440x900 7 inch device.
			//
			case 110:
				setContentView(R.layout.video_activity_for_one_one_zero___five_point_one);
				Log.w(TAG0, "Set Video content for 100.\n");
				break;
			
			// If we are not sure, we use the following default.
			//
			default:
				setContentView(R.layout.video_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Video content for default.\n");
				break;			
		}  
		
		// Establish the number of the last video. This will always be 3 less than the number of nodes, since
		// the last four are used solely for navigational purposes.
		//
		HIGHESTVIDEONUMBER = (MetaDataForVideo.nodesInCurrentXMLsourcefile - 3);
		
		Log.w(TAG0, "HIGHESTVIDEONUMBER is initially " + HIGHESTVIDEONUMBER);
		
		// Get the bundle sent by the calling Activity.
		//
		Bundle b = this.getIntent().getExtras();
		
		videoInformation = b.getStringArray("key0");
		
		idInt = this.getIntent().getIntExtra("showNumber", 0);

		// Set up the progress to be displayed during the background operations.
		//
		progress4 = new ProgressDialog(this, R.style.CustomDialog);
		progress4.setTitle("UltraAPEX Knowledge Point");
      	progress4.setMessage("Accessing media contents...");
		
		// The netOp4 operation is the network activity we do in the background,
		// in order to get the xml file with the data for the animation, which will
		// be used by the Player.
      	//
		netOp4 myOp4 = new netOp4(progress4);
		
		Log.w(TAG0, "See also file location is " + videoInformation[6]);
		
		// Pass the location of the xml see also file.
		//
		myOp4.execute(videoInformation[6]);
		
		// Display the progress notification.
		//
		progress4.show(); 
	}
	
	public class netOp4 extends AsyncTask<String, Void, Document[]>
	{
        private ProgressDialog progress4;
		
		/**
		 * A constructor for the netOp4 class, which specifies the progress dialog defined
		 * in the onCreate for Prepare Player Data as the one to be used in netOp4's background
		 * operation.
		 * 
		 * @param prog A progress dialog.
		 * 
		 */
		public netOp4(ProgressDialog prog) 
		{	
			this.progress4 = prog;
		}
		
		// FIX. NO NEED FOR A DOC ARRAY TO BE PASSED BACK FROM DOINBACKGROUND, BECAUSE IN
		// THIS INSTANCE, WE ONLY NEED A SINGLE OBJECT (UNLIKE IN OTHER ACTIVITIES).
		/**
		 * Accesses the xml files for the
		 * see also content for the currently selected video, and returns these as a document
		 * object. This are passed as an array of document objects to the onPostExecute method.
		 * 
		 * @param arg0 Currently unused.
		 * 
		 */
		@Override
		protected Document[] doInBackground(String... arg0) 
		{
			String fileLocation = arg0[0];
			
			/**
			 * A document object, to contain information on the see also content for
			 * the selected animation.
			 * 
			 */
			Document seeAlsoDoc = null;
			
			// The returnDocumentObject method accesses a specified xml file and returns
			// a document object derived from it. Each of the document variables is thus
			// instantiated.
			//
			seeAlsoDoc = returnDocumentObject(fileLocation);
			
			// A document array is created, to be passed to the onPostExecute method.
			//
			Document[] docArray = new Document[2];
			docArray[1] = seeAlsoDoc; 

			// Return the document array.
			//
			return docArray; 
		}

	 /**
		 * Presents a popup menu from which either the table of contents or the
		 * text-search facility can be accessed for the current chapter. 
		 * 
		 * @param v The current view.
		 * 
		 * @return A boolean indicating success or failure of menu-inflation.
		 * 
		 */
		protected Document returnDocumentObject(String seeAlsoXMLfileLocation)
		{
			/**
			 * A document object, that will be instantiated based on the content of the
			 * specified xml file.
			 * 
			 */
			Document docToBeReturned = null;
	
			// Set up a new xml parser.
			//
			XMLParser parser = new XMLParser();
			
			// Establish a string for the text of the xml file.
			//
			String xmlText = "";
			
			// If we are using network access, treat the file location as a URL.
			//
			if (MetaDataForSuite.Connectivity == 0)
			{
				xmlText = parser.getXmlFromUrl(seeAlsoXMLfileLocation); 
			}
			
			// Otherwise, assume the file is on the sdcard, and access it indeed as a file.
			//
			else
			{
				try 
				{
					xmlText = MyFileReader.readFile(seeAlsoXMLfileLocation);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
				
			if (xmlText == null)
			{
				//Log.w(TAG0, "Looks like the xml content has turned out to be null...");
			}
			else
			{
				//Log.w(TAG0, "String of xml content received as..... " + xmlText + '\n'); 
			}
			
			// Use the parser to generate a document object from the specified xml text.
			//
			docToBeReturned = parser.getDomElement(xmlText); 
			
			// Return the document object so created.
			//
			return docToBeReturned;
		}
		
		protected void onPostExecute(Document[] docArray)
		{
			// Establish two document objects, one for each object in the array.
			// One contains information on images and durations, the other on see also
			// references, whereby text passages in the User Guide can be accessed.
			//
			Document seeAlsoDoc = docArray[1];
			
			// GET INFO FROM THE SEE ALSO DOC OBJECT
			//
			// Again, get a node list of all the images.
			//
			NodeList nl2 = seeAlsoDoc.getElementsByTagName(KEY_SA);
			
			// Indices used as we loop through the xml data, retrieving string
			// values, plus arrays used to house those values.
			//
			int innerIndex2 = 0;
			int titleArrayIndex = 0;
			int hrefArrayIndex = 0;
			int chapNoArrayIndex = 0;
			int ChapRefArrayIndex = 0;
			int DescriptionsArrayIndex = 0;
			int ThumbnailsArrayIndex = 0;
			
			//final String[] stringArrayIDs = new String[nl2.getLength()];
			final String[] stringArrayTitles = new String[nl2.getLength()];
			final String[] stringArrayHrefs = new String[nl2.getLength()];
			final String[] stringArrayChapNos = new String[nl2.getLength()];
			final String[] stringArrayChapRefs = new String[nl2.getLength()];
			final String[] stringArrayDescriptions = new String[nl2.getLength()];
			final String[] stringArrayThumbnails = new String[nl2.getLength()];
			
			// Strings used in obtaining and transferring values, as the iteration
			// proceeds.
			//
			String myString2 = null;
			String myString3 = null;
			String myString4 = null;
			String myString5 = null;
			String myString6 = null;
			
			numberOfNodes = nl2.getLength();
			
			// Loop through the images, find the values, and put them in the
			// single string-array..
			//
			for (int i = 0; i < nl2.getLength(); i++)
			{	
				// Get a node list of all the child nodes.
				//
				NodeList nlc = (nl2.item(i)).getChildNodes();
				 
				// Loop again, to get image_id, image_location, and image_duration.
				//
				for (int j = 0; j < nlc.getLength(); j++)
				{
					NodeList nlgc = (nlc.item(j)).getChildNodes();
					 
					// Loop through the one element (!) of each text node, so as
					// to retrieve the actual text value.
					//
					for (int k = 0; k < nlgc.getLength(); k++)
					{	
						// Find the elements we are interested in, and put
						// them in their respective string arrays.
						//
						switch(innerIndex2)
						{
							// Every zeroeth element is the unique id, so we don't
							// need to do anything with it. 
						    //
							case(0):
								innerIndex2++;
							    break;
							    
							// Every first element is the title. So, put it in 
							// the appropriate slot in the stringArrayTitle array. Then
							// rev the indices.
							//
							case(1):
								myString2 = nlgc.item(k).getNodeValue();
								Log.w(TAG0, "myString2 is " + myString2);
							    stringArrayTitles[titleArrayIndex] = "" + myString2;
							    
							    innerIndex2++;
							    titleArrayIndex++;
							    
							    break;
							    
							// Every second element is the href location. So, put it in
							// appropriate slot in the stringArrayHrefs array. Then rev
							// the indices.  
							//
							case(2):
								myString2 = nlgc.item(k).getNodeValue();  
								
							    stringArrayHrefs[hrefArrayIndex] = "" + myString2;
							    Log.w(TAG0, "myString2 is " + myString2);
							       
							    innerIndex2++;
							    hrefArrayIndex++;
										    
							    break; 
							 
							// Every third element is the chapter number. Put it in the appropriate
							// slot in the stringArrayChapNos array.
							//
							case(3):
								myString3 = nlgc.item(k).getNodeValue();
							
								stringArrayChapNos[chapNoArrayIndex] = "" + myString3;
								Log.w(TAG0, "myString3 is " + myString3);
								
								innerIndex2++;
								chapNoArrayIndex++;
								
								break;
								
							// Every fourth element is the chapter string reference. Put it in the appropriate
							// slot in the stringArrayChapRefs array.
							//
							case(4):
								myString4 = nlgc.item(k).getNodeValue();
							
								stringArrayChapRefs[ChapRefArrayIndex] = "" + myString4;
								Log.w(TAG0, "myString4 is " + myString4);
								
								innerIndex2++;
								ChapRefArrayIndex++;
								
								break;
							
							// Every fifth element is the description. Put it in the appropriate
							// slot in the stringArrayDescriptions array.
							//
							case(5):
								myString5 = nlgc.item(k).getNodeValue();
							
								stringArrayDescriptions[DescriptionsArrayIndex] = "" + myString5;
								Log.w(TAG0, "myString5 is " + myString5);
								
								innerIndex2++;
								DescriptionsArrayIndex++;
								
								break;
								
							// Every sixth element is the thumbnail-location. Put it in the appropriate
							// slot in the stringArrayThumbnails array.
							//
							case(6):
								myString6 = nlgc.item(k).getNodeValue();
							
								stringArrayThumbnails[ThumbnailsArrayIndex] = "" + myString6;
								Log.w(TAG0, "myString6 is " + myString6);
								
								innerIndex2 = 0;
								ThumbnailsArrayIndex++;
								
								break;
								
							default:
							    Log.w(TAG0, "Default reached.");
						 }
					}
				}
			} 
			
			/**
			 * Button, for user-input. Allows user to view the Preface to the application, by
			 * means of the Chapter Activity.
			 * 
			 */
			Button button1 = null;
			
			/**
			 * Button, for user-input. Allows user to access main contents listing for
			 * the documentation suite, by means of the Customized Content List View
			 * Activity.
			 * 
			 */
			Button button2 = null;   
			
			/**
			 * Button, for user-input. Allows user to view the About section, by means of the
			 * Chapter Activity.
			 * 
			 */
			Button button3 = null;
			 
			/**
			 * Button, for user-input. Allows user to view the Help section, by means of the
			 * Chapter Activity.
			 * 
			 */
			Button button4 = null;

			VideoActivity.this.setTitle(videoInformation[1]);

			// Instantiate the button for the "More Information" Dialog.
			//
			button1 = (Button)VideoActivity.this.findViewById(R.id.moreButton);
			button1.setTextColor(Color.rgb(0, 0, 138));
			button1.clearFocus();
			
			for (int i = 0; i < numberOfNodes; i++)  
			{ 
				map = new HashMap<String, String>();        
				
				// Add each child node and its value to the hash table.
				//					
				map.put(KEY_TITLE, stringArrayTitles[i]);
				map.put(KEY_LOCATION, stringArrayHrefs[i]);
				map.put(KEY_CHAP_NO, stringArrayChapNos[i]);
				map.put(KEY_CHAP_REF, stringArrayChapRefs[i]);
				map.put(KEY_DESCRIPTION, stringArrayDescriptions[i]);
				map.put(KEY_THUMBNAIL, stringArrayThumbnails[i]);
				 
				seeAlsoList.add(map);          
			}
	        
			// Establish the style for the dialog.
			//
	        final Dialog dialog = new Dialog(VideoActivity.this, R.style.CustomDialogForDocReferences);
	        dialog.setContentView(R.layout.list_layout);
	        dialog.setTitle("More details on this topic:"); 
	        
	        // Set the size of the dialog based on the aspect ratio and screen size of the
	        // current platform, as determined in the Login Activity, and now represented
	        // in MetaDataForSuite.
	        //
	        switch (MetaDataForSuite.currentAspectRatio)
        	{
	        // This is a 480x800 4.3" device.
        	//
        	case 0:
        		dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_zero___four_point_three), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_zero___four_point_three));
        		
        		Log.w(TAG0, "Set dialog width and height for 0.\n");
        		break;
        		
    		// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_one_zero___four_point_zero_etc), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_one_zero___four_point_zero_etc));
        		
        		Log.w(TAG0, "Set dialog width and height for 10.\n");
        		break;
        		
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_two_zero___four_point_three_etc), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_two_zero___four_point_three_etc));
        		
        		Log.w(TAG0, "Set dialog width and height for 20.\n");
        		break;
        		
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_three_zero___four_point_seven), 
						getResources().getInteger(R.integer.video_more_info_dialog_width_for_three_zero___four_point_seven));
        		
        		Log.w(TAG0, "Set dialog width and height for 30.\n");
        		break;
        		
			// This is a 800x1280 7" device.
		    //
			case 40:
				dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_four_zero___seven), 
						getResources().getInteger(R.integer.video_more_info_dialog_width_for_four_zero___seven));
        		
        		Log.w(TAG0, "Set dialog width and height for 40.\n");
        		break;
        		
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_five_zero___ten), 
						getResources().getInteger(R.integer.video_more_info_dialog_width_for_five_zero___ten));
        		
        		Log.w(TAG0, "Set dialog width and height for 50.\n");
        		break;
        
        	// This is a 1080x1920 generic-size device.
        	//
        	case 60:
        		dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_six_zero___gen), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_six_zero___gen));
        		
        		Log.w(TAG0, "Set dialog width and height for 60.\n");
        		break;

        		// This is a 1080x1920 5.7 inch device.
        		//
        	case 70:
        		dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_seven_zero___five_point_seven), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_seven_zero___five_point_seven));
        		
        		Log.w(TAG0, "Set dialog width and height for 70.\n");
        		break;

        		// This is a 1200x1920 7 inch device.
        		//
        	case 80:
        		dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_eight_zero___seven), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_eight_zero___seven));
        		Log.w(TAG0, "Set dialog width and height for for 80.\n");
        		break;

        		// This is a 1200x1920 10 inch device.
        		//
        	case 90:
        		dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_nine_zero___ten), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_nine_zero___ten));
        		Log.w(TAG0, "Set dialog width and height for for 90.\n");
        		break;
        		
        		// This is a 1440x900 7 inch device.
        		//
        	case 100:
        		dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_one_zero_zero___seven), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_one_zero_zero___seven));
        		Log.w(TAG0, "Set dialog width and height for for 100.\n");
        		break;

				// This is a 1440x900 7 inch device.
				//
			case 110:
				dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_one_one_zero___five_point_one));
				Log.w(TAG0, "Set dialog width and height for for 110.\n");
				break;

        		// If we are not sure, we use the following default.
        		//
        	default:
        		dialog.getWindow().setLayout(getResources().getInteger(R.integer.video_more_info_dialog_width_for_six_zero___gen), 
						getResources().getInteger(R.integer.video_more_info_dialog_height_for_six_zero___gen));
        		Log.w(TAG0, "Set dialog width and height for for default.\n");
        		break;			
        	}
	        
	        brandAlertDialog(dialog);  

			button1.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v) 
				{					   
			        ListView listView = (ListView) dialog.findViewById(R.id.list);
			        
			        adapter = new AdapterForDialog(VideoActivity.this, seeAlsoList); 
			        
			        // Display the List View. First, nullify it to erase any content from the last use.
			        // Otherwise, the new content will be concatenated onto the old.
			        //
			        listView.setAdapter(null);
			        listView.setAdapter(adapter);

			        listView.setOnItemClickListener(new OnItemClickListener() 
			        {
			        	// Note that 
			            @Override
			            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			            {	
				        	// From this point, our ulterior destination is the chapter
				        	// activity. This involves a
				        	// physical reorienting of the device from landscape to portrait. If this
				        	// is the first time we have done this, instead of going directly to
				        	// the chapter, we first visit a transitional activity, which shows an
				        	// animation encouraging the user to reorient. 
				        	//
				        	// If we have already done this, don't do it again. We only do it
				        	// one time, then assume the user understands.
				        	//
			            	Intent intent = null;
			            	
							// If we have shown the animation before, don't do it again. 
							//
			            	if (MetaDataForSuite.goPortraitUsed == 1)
			            	{
				        		// Prepare to go directly to the chapter activity, instead.
				        		//
			            		intent = new Intent(VideoActivity.this, ChapterActivity.class);
			            	}
			            	
			            	// But if we have not shown the animation before...
			            	//
			            	else
			            	{
			            		// Prepare to go to the animation activity.
			            		//
				            	intent = new Intent(VideoActivity.this, GoPortraitActivity.class);
				            	
				            	// Make sure we don't do this again.
				            	//
				            	MetaDataForSuite.goPortraitUsed = 1;
			            	}
			            	
			            	// Note that even if we are making a detour to the goPortrait activity, now, we
			            	// still load up the intent with info appropriate for arriving at the chapter-location.
			            	// Within goPortrait, we'll grab all this info, and repack it into a different intent,
			            	// as we make our final transition into the chapter.
							//
							// Establish the chapter to which we are going as one less than the
							// stated value, since we'll count from 0.
							//
					    	intent.putExtra("intVar", Integer.parseInt(stringArrayChapNos[position]) - 1 );
					    	intent.putExtra("urlOfChapter", stringArrayHrefs[position]); 
					    	
							// Also specify that our departure and our target are voice and chapter-activities
							// respectively: 7 and 3. Then specify the corresponding
							// transition profile; which for chapter to chapter, is 700.
							//
							intent.putExtra("placeOfDeparture", 7);
					    	intent.putExtra("targetChapterActivityType", 3);
					    	intent.putExtra("transitionProfile", 700);
					    	
					    	Log.w(TAG0, "Starting intent with chap number of " + stringArrayChapNos[position]);
					    	Log.w(TAG0, "Starting intent with chap numeric value of " + Integer.parseInt(stringArrayChapNos[position]));
					    	Log.w(TAG0, "Starting intent with url of " + stringArrayHrefs[position]);
					    	
					    	// Make sure we have the right data for the User Guide, which is where we are
					    	// going. Note that we can't otherwise be sure to get it right, since we are
					    	// not traversing the UG book cover, and another book cover might have recently
					    	// reset this data.
					    	//
					    	// Following an appropriate re-architecture, this can be handled differently.
					    	
					    	Log.w(TAG0, "whetherWidelyEstablished is now " + MetaDataForDocument.whetherWidelyEstablished);
					    	
					    	if(MetaDataForDocument.allChapters != null)
					    	{
					    		Log.w(TAG0, "Before reassignment, allChapters is non-null\n");
					    	}
					    	
					    	if (MetaDataForDocument.userGuideChapters != null)
					    	{
					    		Log.w(TAG0, "Before assignment, userGuideChapters is non-null\n");
					    	}
					    	else
					    	{
					    		Log.w(TAG0, "Before assignment, userGuideChapters is null\n");
					    	}
					    	
					    	// The whetherWidelyEstablished variable is set to 1 as soon as the cover page of any
					    	// book other than the user guide has been visited. If it is 1, then all the structures
					    	// that we use to orient ourselves within that particular book have been set. Therefore,
					    	// we must now explicitly reset them back to the user guide. 
					    	//
					    	if (MetaDataForDocument.whetherWidelyEstablished == 1) 
					    	{
					    		MetaDataForDocument.currentDocument = 0;
					    		MetaDataForDocument.nodesInCurrentXMLsourcefile = MetaDataForDocument.nodesInUserGuideXMLsourcefile;
					    		MetaDataForDocument.allChapters = MetaDataForDocument.userGuideChapters;
					    		MetaDataForDocument.chapterTOCfiles = MetaDataForDocument.userGuideChapterTOCfiles;
					    		MetaDataForDocument.allChaptersArrayList = MetaDataForDocument.userGuideChaptersArrayList;
					    		
					    		if(MetaDataForDocument.allChapters != null)
						    	{
						    		Log.w(TAG0, "After assignment, allChapters is non-null\n");
						    	}
					    		else
					    		{
							    	Log.w(TAG0, "After assignment, allChapters is null\n");
					    		}
					    	}
					    	
					    	// Start the Chapter Activity with the provided reference.
					    	//
					    	startActivity(intent);	
					    	
					    	// Dismiss the dialog, now that a selection has been made.
					    	//
			    			dialog.dismiss();
			    			
			    			// Finish the current Video Activity, since we are now transitioning to the
			    			// Chapter activity.
			    			//
			    			finish(); 
			            }
			        });

			        dialog.show();		
				}
			});

			// Instantiate the button for accessing the various documents. Then establish
			// the button's routine.
			//
			button2 = (Button)VideoActivity.this.findViewById(R.id.upButton);
			button2.setTextColor(Color.rgb(0, 0, 138));
			button2.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v) 
				{	
					Intent intent = new Intent(VideoActivity.this, 
					           GridViewForVideoActivity.class);
					intent.putExtra("here", Environment.getExternalStorageDirectory() 
												+ "/knowledgePointSD/video/all_videos.xml");
					startActivity(intent);
					finish();
				}
			});		

			// Instantiate the button for the Previous video in sequence, and establish its routine.
			//
			button3 = (Button)VideoActivity.this.findViewById(R.id.leftButton);
			button3.setTextColor(Color.rgb(0, 0, 138));
			button3.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v) 
				{	
					// A video has been selected. So, create a new hash mapping in which 
					// to contain the information for the selected song.
					//
					HashMap<String, String> newMap = new HashMap<String, String>();
					
					Log.w(TAG0, "HIGHESTVIDEONUMBER is initially " + HIGHESTVIDEONUMBER);
					
					if (idInt <= 0)
					{
						idInt = (HIGHESTVIDEONUMBER - 1);
					}
					else
					{
						idInt--;
					}
					
					Log.w(TAG0, "HIGHESTVIDEONUMBER is subsequently " + HIGHESTVIDEONUMBER);

					// Make the new hash mapping equal to the hash mapping element in the
					// videoList that contains the information for the selected song.
					//
					newMap = MetaDataForVideo.allVideosArrayList.get(idInt);
					
					// Pull the strings out of the hash mapping, and save them in a string array,
					// ready to be sent to the video activity.
					//
					String selectedVideo[] = getSongInfo(newMap);
					
					// Define the Video Activity, specifying the number of the 
					// selected animation.
					//
					Intent intent = new Intent(VideoActivity.this, VideoActivity.class);
					
					// Create a bundle, into which we'll place the string array to be dispatched.
					//
					Bundle b = new Bundle();
					
					// Put the array into the bundle, with a key for retrieval. 
					//
					b.putStringArray("key0", selectedVideo);
					
					// Add the bundle to the intent.
					//
					intent.putExtras(b);
					
					// Add the id of the selected video.
					//
					intent.putExtra("showNumber", idInt);
					
					// Start the intent.
					//
					startActivity(intent);
				}
			});	
			
			// Instantiate the button for the Next video in sequence, and establish its routine.
			//
			button4 = (Button)VideoActivity.this.findViewById(R.id.rightButton);        
			button4.setTextColor(Color.rgb(0, 0, 138));   
			button4.setOnClickListener(new View.OnClickListener()       
			{
				public void onClick(View v)  
				{   
					// A video has been selected. So, create a new hash mapping in which 
					// to contain the information for the selected song.
					//
					HashMap<String, String> newMap = new HashMap<String, String>();
					
					Log.w(TAG0, "HIGHESTVIDEONUMBER is initially " + HIGHESTVIDEONUMBER);
					Log.w(TAG0,"idInt is initially " + idInt);
					
					if (idInt >= (HIGHESTVIDEONUMBER - 1))
					{
						idInt = 0;
					}
					else 
					{
						idInt++;
					}
					
					Log.w(TAG0, "idInt is subsequently " + idInt);

					// Make the new hash mapping equal to the hash mapping element in the
					// videoList that contains the information for the selected song.
					//
					newMap = MetaDataForVideo.allVideosArrayList.get(idInt);
					
					// Pull the strings out of the hash mapping, and save them in a string array,
					// ready to be sent to the video activity.
					//
					String selectedVideo[] = getSongInfo(newMap);
					
					// Define the Video Activity, specifying the number of the 
					// selected animation.
					//
					Intent intent = new Intent(VideoActivity.this, VideoActivity.class);
					
					// Create a bundle, into which we'll place the string array to be dispatched.
					//
					Bundle b = new Bundle();
					
					// Put the array into the bundle, with a key for retrieval. 
					//
					b.putStringArray("key0", selectedVideo);
					
					// Add the bundle to the intent.
					//
					intent.putExtras(b);
					
					// Add the id of the selected video.
					//
					intent.putExtra("showNumber", idInt);
					
					// Start the intent.
					//
					startActivity(intent);	
				}
			});
	
			// Set up the video player.  
			//
			VideoView vidView = (VideoView)findViewById(R.id.imgFrame); 
			String vidAddress = videoInformation[4];
			Uri vidUri = Uri.parse(vidAddress);
			vidView.setVideoURI(vidUri);   
			
			MediaController vidControl = new MediaController(VideoActivity.this);
			vidControl.setAnchorView(vidView);
			vidView.setMediaController(vidControl); 
			
			// Get rid of the progress notification.
			//
			progress4.dismiss();
			
			// Start the video.
			//
			vidView.start();
		}
	}
	
	public static void brandAlertDialog(Dialog dialog)         
    {
        try 
        {
            Resources resources = dialog.getContext().getResources();
            int color = resources.getColor(R.color.my_white); // your color here

            int alertTitleId = resources.getIdentifier("alertTitle", "id", "android");
            TextView alertTitle = (TextView) dialog.getWindow().getDecorView().findViewById(alertTitleId);
            //alertTitle.setTextColor(color); // change title text color
            //alertTitle.setHeight(30);

            int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
            View titleDivider = dialog.getWindow().getDecorView().findViewById(titleDividerId);
            titleDivider.setBackgroundColor(color); // change divider color
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
    }
	
	/**
	 * Takes as its argument a hash map of string pairings,
	 * and derives and returns from this a string array, containing the values
	 * retrieved.
	 * 
	 * @param map A hash map of string pairings.
	 * 
	 * @return A string array.
	 * 
	 */
	private String[] getSongInfo(HashMap<String, String> map) 
	{
		// Declare a string array, which we will use to contain the information
		// from a one-strip hash mapping.
		//
		String songInfo[] = new String[7];

		// Iterate over the hash map, pulling out keys and associated values.
		//
		Set<String> keys = map.keySet();
		Iterator<String> i = keys.iterator();
		
		while (i.hasNext()) 
		{
			String key_string = (String) i.next();
			String value = (String) map.get(key_string);

			// Since it is not clear that the keys and values always go in
			// and come out in a predictable order, we now have to ensure that
			// the values are put into exactly the right slots in the string
			// array, as follows:
			//
			if (key_string == "id") 
			{
				songInfo[0] = value;
			} 
			else 
			{
				if (key_string == "title") 
				{
					songInfo[1] = value;
				} 
				else 
				{
					if (key_string == "description") 
					{
						songInfo[2] = value;
					} 
					else 
					{
						if (key_string == "duration") 
						{
							songInfo[3] = value;
						} 
						else 
						{
							if (key_string == "source") 
							{
								songInfo[4] = value;
							} 
							else 
							{
								if (key_string == "thumb_url") 
								{
									songInfo[5] = value;
								} 
								else 
								{
									if (key_string == "see_also_list") 
									{
										songInfo[6] = value;
									}
								}
							}
						}
					}
				}
			}
		}

		// Return the fully instantiated string array.
		//
		return songInfo;
	}
}

