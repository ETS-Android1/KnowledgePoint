package com.ueas.kpallv1g6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

// The typical procedure whereby the contents of a document are assembled is that (a) the user selects a document
// from the Customized Content List View for the entire suite; (b) when the bookcover appears, the user selects to
// proceed, to read the "Document Contents"; and (c) the Customized Content List View For Book starts, and accesses
// the xml file for the document's chapter-listing. 
//
// The exception to this is with the User Guide. 
//
// The User Guide provides additional options; these being Animation and Voice. Both of these activites allow the user
// to jump into key passages of the User Guide, to learn more about the presentation/answer they just received. For this
// lateral jump to work, the data structures typically established by the Customized Content List View For Book must
// have been properly instantiated.
//
// However, it is quite possible that the user wishes to visit the Animation and/or Voice activities prior to visiting
// the text of the user guide. Therefore, when Animation or Voice is selected from the User Guide bookcover, full
// instantiation, such as usually does not happen until the List View is accessed, must occur immediately, on a
// "just in time" basis.
//
// This, then, is the reason for User Guide instantiation taking place as part of the current activity, even though
// the User Guide is not directly being accessed.

/**
 * Presents a book cover for the currently selected document. The cover features options
 * for proceeding to the chapter listing for the document, and for returning to the documentation listing for the
 * whole suite. The cover for the User Guide also features options for visiting the Animation and Voice activities.
 * 
 * @author tony.hillman@ultra-as.com
 *
 */
public class BookCoverActivity extends Activity 
{
	// Strings to house per-chapter data. The URL string points at
	// the location of the xml file that holds all the basic
	// information on the chapter list. This will be used to create
	// the DOM document from which all information is then derived.
	//
	/**
	 * A string used to identify the node-name for the principal sections
	 * in the xml file used by the Activity for information on available
	 * documents.
	 * 
	 */
	static final String KEY_SECTION = "chapter"; 
	
	/**
	 * A string used to refer to the numeric identifer of a principal section
	 * (and thereby, a chapter-title) within the xml file containing
	 * the list of available chapters. Within the file, identifiers are
	 * unique, and are small integers, arranged in monotonically increasing
	 * sequence.
	 * 
	 */
	static final String KEY_ID = "id";
	
	/**
	 * A string used to refer to the title of a chapter referenced within
	 * the target xml file. The title will be displayed in the List View.
	 * 
	 */
	static final String KEY_CHAP_TITLE = "chap_title";
	
	/**
	 * A string used to refer to the description of a chapter referenced
	 * within the target xml file. The description will be displayed in
	 * the List View.
	 * 
	 */
	static final String KEY_DESCRIPTION = "description";
	
	/**
	 * A string used to refer to the thumbnail image for a given document, referenced within
	 * the target xml file. The thumbnail will be displayed in the List View.
	 * 
	 */
	static final String KEY_THUMBNAIL = "thumbnail";
	
	/**
	 * A string used to refer to the source pathname of a given chapter, referenced
	 * within the target xml file. If this row of the List View is selected by the
	 * user, this pathname will be passed to the Chapter Activity, so that the chapter
	 * can be viewed.
	 * 
	 */
	static final String KEY_SRC_DOC = "src_doc";
	
	/**
	 * A string used to refer to the pathname of the source of a given document's toc,
	 * referenced within the target xml file. 
	 * 
	 */
	static final String KEY_SRC_TOC = "src_toc";
	
	/**
	 * An integer representing the number of items in each node of the target xml document.
	 * 
	 */
	// NOTE: THIS NEEDS TO BE DERIVED SOME OTHER WAY. POSSIBLY FROM THE VALUES FILE IN RESOURCES.
	//
	private int NUMBER_OF_ITEMS_IN_NODE = 6; 
	
	/**
	 * An integer representing the number of nodes in the target xml document.
	 * 
	 */
	// This will be established dynamically, below.
	//
	private int numberOfContentNodes = 0;
	
	// NOTE: NEED DESCRIPTIVE NAMES FOR THIS BUTTONS. FIX.
	//
	/**
	 * A button whereby the user can return to the List View for the entire suite.
	 * 
	 */
	public Button button1 = null; 
	
	/**
	 * A button whereby the user can proceed to the List View for the currently selected
	 * document.
	 * 
	 */
	public Button button2 = null;
	
	/**
	 * A button, visible only on the cover of the User Guide, whereby the user can elect
	 * to proceed to the animation activity.
	 * 
	 */
	public Button button3 = null;
	
	/**
	 * A button, visible only on the cover of the User Guide, whereby the user can elect
	 * to proceed to the voice activity.
	 */
	public Button button4 = null;
	
	/**
	 * A string used for LogCat output from the onCreate method.
	 * 
	 */
	private final static String TAG0 = "BCA: onCreate";
	
	/**
	 * A string used for LogCat output from the doInBackground method.
	 * 
	 */
	private final static String TAG1 = "BCA: doInBackground";
	
	/**
	 * A string used for LogCat output from the onPostExecute method.
	 * 
	 */
	private final static String TAG2= "BCA: onPostExecute";
	
	/**
	 * An integer that corresponds to the unique ID of the document selected in the previous
	 * activity, whereby the current book cover was selected. If this is 0, then the User
	 * Guide has been selected, and the current activity must display special options for
	 * the user.
	 * 
	 */
	private static int idInt = 0;
	
	/**
	 * An activity that is set to the current activity, for purposes of List View
	 * instantiation.
	 * 
	 */
	// We pass the current activity to the LazyContentAdapterForBook, so that the ListView
	// can be prepared. But we do so in a PostExecution method, after
	// all the network stuff is completed. So, we can't just use the "this" keyword,
	// and must pass an activity variable.
	//
	public Activity activity = this;
	
	/**
	 * A hash map used in the preparation of information for access to the 
	 * User Guide.
	 * 
	 */
	private HashMap<String, String> map;
	
	/**
	 * A dynamically extensible array containing as many hash maps as are needed
	 * to store information on available chapters, provided by the target xml
	 * file. Each map will contain string pairings, which are the key and value
	 * for each attribute of a given document (ie, "id" of "0", "title" of "User Guide",
	 * etc).
	 * 
	 */
	private ArrayList<HashMap<String, String>> chaptersList = new ArrayList<HashMap<String, String>>();
	
	/**
	 * A string used to store the current location of the book cover.
	 * 
	 */
	private String currentBookCoverImageLocation = null;
	
	// Flag to indicate whether this is the User Guide or not (if it is, we
	// need a cover that displays additional features.
	//
	private int whetherUG = 0;
	
	// The image Frame that will house the book cover.
	//
	private ImageView imgFrame;   
	
	// Flag to indicate whether we have chosen to go to the Animation or the Voice Activity. Both
	// activities currently require that certain data-structures specific to the User Guide be
	// initialised. Typically, these are initialised by the ListView for the text chapters; but
	// the ongoing development of this application has necessitated that the work be brought
	// forward in the case of either Animation or Voice being chosen from the User Guide cover
	// page. A better architecture needs to be devised. At any rate, once the network operation
	// required prior to initialisation has been completed, the one activity or the other must
	// be commenced from the onPostExecute method. This flag allows the correct determination to
	// be made. If the flag is 0, neither has been chosen. If it is 1, the Animation Activity has
	// been chosen. If it is 2, the Voice Activity.
	//
	// Note that this is only used for the User Guide (at present).
	//
	int whichUGActivityChosen = 0;
	
	/**
	 * String used to hold the location of the xml file for the animations, which are accessed
	 * from the User Guide bookcover.
	 */
	// NOTE: THIS SHOULD BE ESTABLISHED IN THE VALUES AREA OF THE RESOURCES, IDEALLY.
	//
	//private static String LOCATION_OF_ANIMATION_XML_FILE = "/sdcard/knowledgePointSD/shows/all_shows.xml";
	private static String LOCATION_OF_ANIMATION_XML_FILE = "/sdcard/knowledgePointSD/video/all_videos.xml";
	
	/**
	 * String used to hold the location of the xml file that contains information on all 
	 * the documents in the suite.
	 * 
	 */
	private static String LOCATION_OF_SUITE_XML_FILE = "/sdcard/knowledgePointSD/all_documents.xml";
	
	/**
	 * A string that will hold the location of the xml file containing chapter information for
	 * the current document.
	 * 
	 */
	public String xml = "";

	/**
	 * Sets up the cover for a chosen document, and populates it with active buttons, whereby
	 * the user makes navigational choices.
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		idInt = intent.getIntExtra("intVar", 0);
		
		Log.w(TAG0, "idInt is " + idInt + '\n');
		
		// Determine whether this is the User Guide. If so, it has an additional
		// button on the cover, so as to access the animations.
		//
		// If the integer is non-zero, we present a standard cover with two options only. This
		// is defined in the bookcoveractivity.xml layout file.
		//
		if (idInt != 0)
		{
			Log.w(TAG0, "idInt is " + idInt + ", so using regular cover..." + '\n');
			
			// Establish the content view for the admin version of this activity. Do this conditionally, based on the 
			// screen-size/aspect-ratio combination we have already determined.
			//
			switch (MetaDataForSuite.currentAspectRatio)
			{
				// This is a 480x800 4.3" device.
				//
				case 0:
					setContentView(R.layout.book_cover_activity_for_zero___four_point_three);
					Log.w(TAG0, "Set Book Cover content for 0.\n");
					break;

				// This is a 540x960 4.0" to 4.8" device.
			    //
				case 10:
					setContentView(R.layout.book_cover_activity_for_one_zero___four_point_zero_etc);
					Log.w(TAG0, "Set Book Cover content for 10.\n");
					break;
					
				// This is a 720x1280 4.3" to 5.0" device.
			    //
				case 20:
					setContentView(R.layout.book_cover_activity_for_two_zero___four_point_three_etc);
					Log.w(TAG0, "Set Book Cover content for 20.\n");
					break;
					
				// This is a 768x1280 4.7" device.
			    //
				case 30:
					setContentView(R.layout.book_cover_activity_for_three_zero___four_point_seven);
					Log.w(TAG0, "Set Book Cover content for 30.\n");
					break;
					
				// This is a 800x1280 7" device.
			    //
				case 40:
					setContentView(R.layout.book_cover_activity_for_four_zero___seven);
					Log.w(TAG0, "Set Book Cover content for 40.\n");
					break;
					
				// This is a 800x1280 10.1" device.
			    //
				case 50:
					setContentView(R.layout.book_cover_activity_for_five_zero___ten);
					Log.w(TAG0, "Set Book Cover content for 50.\n");
					break;
					
			    // This is a 1080x1920 generic-size device.
				//
				case 60:
					setContentView(R.layout.book_cover_activity_for_six_zero___gen);
					Log.w(TAG0, "Set Book Cover content for 60.\n");
					break;
				
				// This is a 1080x1920 5.7 inch device.
				//
				case 70:
					setContentView(R.layout.book_cover_activity_for_seven_zero___five_point_seven);
					Log.w(TAG0, "Set Book Cover content for 70.\n");
					break;
				
				// This is a 1200x1920 7 inch device.
				//
				case 80:
					setContentView(R.layout.book_cover_activity_for_eight_zero___seven);
					Log.w(TAG0, "Set Book Cover content for 80.\n");
					break;
				
				// This is a 1200x1920 10 inch device.
				//
				case 90:
					setContentView(R.layout.book_cover_activity_for_nine_zero___ten);
					Log.w(TAG0, "Set Book Cover content for 90.\n");
					break;
				
				// This is a 1440x900 7 inch device.
				//
				case 100:
					setContentView(R.layout.book_cover_activity_for_one_zero_zero___seven);
					Log.w(TAG0, "Set Book Cover content for 100.\n");
					break;

				// This is a 1440x2560 5.1 inch device.
				//
				case 110:
					setContentView(R.layout.book_cover_activity_for_one_one_zero___five_point_one);
					Log.w(TAG0, "Set Book Cover content for 110.\n");
					break;

				// If we are not sure, we use the following default.
				//
				default:
					setContentView(R.layout.book_cover_activity_for_six_zero___gen);
					Log.w(TAG0, "Set Book Cover content for default.\n");
					break;			
			}
			
			// The whetherWidelyEstablished flag is used to indicate that infrastructure has been
			// instantiated with information related to documents other than the User Guide. This is
			// useful in determining whether re-instantiation is necessary, when subsections of the
			// User Guide are to be accessed from the Animation and Voice activities.
			//
			MetaDataForDocument.whetherWidelyEstablished = 1;
		}
		
		// If the integer is 0, then the User Guide has indeed been selected, and we need to
		// present additional features to the user. These are specified in the bookcoveractivityforug.xml
		// layout file.
		//
		else
		{
			Log.w(TAG0, "idInt is " + idInt + ", so using user guide cover..." + '\n');
			
			// Establish the content view for the user guide version of this activity. Do this conditionally, based 
			// on the screen-size/aspect-ratio combination we have already determined.
			//
			switch (MetaDataForSuite.currentAspectRatio)
			{
				// This is a 480x800 4.3" device.
				//
				case 0:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_zero___four_point_three);
					Log.w(TAG0, "Set Book Cover content for 0.\n");
					break;
				
				// This is a 540x960 4.0" to 4.8" device.
			    //
				case 10:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_one_zero___four_point_zero_etc);
					Log.w(TAG0, "Set Book Cover content for 10.\n");
					break;
					
				// This is a 720x1280 4.3" to 5.0" device.
			    //
				case 20:
					setContentView(R.layout.book_cover_activity_for_user_guide_two_zero___four_point_three_etc);
					Log.w(TAG0, "Set Book Cover content for 20.\n");
					break;
			
				// This is a 768x1280 4.7" device.
			    //
				case 30:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_three_zero___four_point_seven);
					Log.w(TAG0, "Set Book Cover content for 30.\n");
					break;
					
				// This is a 800x1280 7" device.
			    //
				case 40:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_four_zero___seven);
					Log.w(TAG0, "Set Book Cover content for 40.\n");
					break;
					
				// This is a 800x1280 10.1" device.
			    //
				case 50:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_five_zero___ten);
					Log.w(TAG0, "Set Book Cover content for 50.\n");
					break;
					
			    // This is a 1080x1920 generic-size device.
				//
				case 60:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_six_zero___gen);
					Log.w(TAG0, "Set Book Cover content for 60.\n");
					break;
				
				// This is a 1080x1920 5.7 inch device.
				//
				case 70:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_seven_zero___five_point_seven);
					Log.w(TAG0, "Set UG Book Cover content for 70.\n");
					break;
				
				// This is a 1200x1920 7 inch device.
				//
				case 80:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_eight_zero___seven);
					Log.w(TAG0, "Set Book Cover content for 80.\n");
					break;
				
				// This is a 1200x1920 10 inch device.
				//
				case 90:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_nine_zero___ten);
					Log.w(TAG0, "Set Book Cover content for 90.\n");
					break;
					
				// This is a 1440x900 7 inch device.
				//
				case 100:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_one_zero_zero___seven);
					Log.w(TAG0, "Set Book Cover content for 100.\n");
					break;

				// This is a 1440x900 7 inch device.
				//
				case 110:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_one_one_zero___five_point_one);
					Log.w(TAG0, "Set Book Cover content for 110.\n");
					break;


				// If we are not sure, we use the following default.
				//
				default:
					setContentView(R.layout.book_cover_activity_for_user_guide_for_six_zero___gen);
					Log.w(TAG0, "Set Book Cover content for default.\n");
					break;			
			}

			// Use this integer variable to check on whether we are indeed looking at the
			// User Guide cover.
			//
			whetherUG = 1;
		}
		
        // Instantiate an Image View. 
		//
      	imgFrame = (ImageView) findViewById(R.id.bookcoverimage);
		
      	// Set the title for the current activity, along the top bar.
      	//
		this.setTitle("Your selection: " + MetaDataForSuite.allDocuments[idInt][1]); 
		
      	currentBookCoverImageLocation = 
      			MetaDataForSuite.allDocuments[idInt][5];
      	
      	// The BackLoad class allows a book cover to be downloaded in
      	// the background.
      	//
		BackLoad myOp = new BackLoad();
		
		// No string needed at this point for execution of this method, but we may add one later.
		//
		myOp.execute("placeholder");

		// Set up the button whereby the List View for the suite is re-accessed.
		//
		button1 = (Button)this.findViewById(R.id.suiteContentsButton);
		button1.setTextColor(Color.rgb(0, 0, 138));

		button1.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v)  
			{
				// Selecting Documentation takes the reader to the ListView containing the full
				// list of documents.
				//
				Intent intent = new Intent(BookCoverActivity.this, ListViewForSuiteActivity.class);
				           						
				intent.putExtra("here", LOCATION_OF_SUITE_XML_FILE);
				startActivity(intent);
				//finish();
			}
		});

		// Set up the button whereby the List View for the document is accessed.
		//
		button2 = (Button)this.findViewById(R.id.documentContentsButton);
		button2.setTextColor(Color.rgb(0, 0, 138));
		
		button2.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v)  
			{		
				// Since we are entering a contents list "from above", which is to say, from
				// the book-cover, we need to access the network to get values for the contents
				// list. Since we may previously have saved values in the BookMetaDataRegistry
				// object (allowing us to jump into the content-list "from below" without going
				// back to the network unnecessarily), we must now erase such saved values, since they
				// may not apply to the book we now wish to see the contents for.
				//
				// Make a saved doc object null, if one exists.
				//
				if (MetaDataForDocument.all_chapters_doc_object != null)
				{
					MetaDataForDocument.all_chapters_doc_object = null;
				}
				
				// Make a saved allChapters string 2D array null, if one exists.
				//
				if (MetaDataForDocument.allChapters != null)
				{
					MetaDataForDocument.allChapters = null;
				}
				
				// Make a saved allChapters arrayList null, if one exists.
				//
				if (MetaDataForDocument.allChaptersArrayList != null)
				{
					MetaDataForDocument.allChaptersArrayList = null;
				}
				
				// Now go to the chapter contents activity, in order to assemble and display, and save
				// new values to be substituted for those we just deleted.
				//
				Intent intent = new Intent(BookCoverActivity.this, 
								           ListViewForDocumentActivity.class);
				
				String XMLfileLocation = MetaDataForSuite.allDocuments[idInt][4];
		        intent.putExtra("here", XMLfileLocation);
		        intent.putExtra("intVar", idInt);
				startActivity(intent);
				//finish();
			}
		});	
		
		// Only instantiate the third and fourth buttons in addition if this is the User Guide. 
		// 
		if (whetherUG == 1)
		{
			// Set up the button for accessing the animation activity.
			//
			button3 = (Button)this.findViewById(R.id.animationContentsButton);
			button3.setTextColor(Color.rgb(0, 0, 138));
	
			button3.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v)  
				{

					// Specify that the animation activity has indeed been selected by
					// the user.
					//
					whichUGActivityChosen = 1;
					
					// Since we are going to the Animation Activity from the User Guide book cover, we must
					// first ensure that all our infrastructure pertinent to the User Guide text is in place.
					// This is because it may be accessed directly from either the Animation or the Voice
					// activity.
					//
					// Establish a progress dialog, to be shown while background operations are in progress.
					//
					ProgressDialog progress = new ProgressDialog(BookCoverActivity.this, R.style.CustomDialog);
					progress.setTitle("UltraAPEX Knowledge Point");
			      	progress.setMessage("Assembling contents...");
			      	
					// The netOp operation is the network activity we do in the background,
					// in order to get the xml file with the User Guide chapter data.
			      	//
					netOp mySecondOp = new netOp(progress);
					
					// We currently know that this is the user guide. So send the network operation off
					// explicitly to get the User Guide pathname.
					//
					String XMLfileLocationForUG = MetaDataForSuite.allDocuments[idInt][4];
					mySecondOp.execute(XMLfileLocationForUG);
				}
			});
			
			// Set up the button for accessing the Voice Activity.
			//
			button4 = (Button)this.findViewById(R.id.voiceContentsButton);
			button4.setTextColor(Color.rgb(0, 0, 138));
	
			button4.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v)  
				{
					// Specify that the voice activity has indeed been selected.
					//
					whichUGActivityChosen = 2;
					
					// Only run the voice activity if the OS version is 4.4 or
					// higher.
					//
					// Get the full OS version (eg, 4.2.1)
					//
					String androidOS = Build.VERSION.RELEASE;
					Log.w(TAG0, "Version is " + androidOS);
				    
					// Get the second integer (eg. the "2"), which corresponds to
					// the version.
					//
				    String minorVersion = "";
				    String majorVersion = "";
				    majorVersion = majorVersion + androidOS.charAt(0);
				    minorVersion = minorVersion + androidOS.charAt(2);
				    int minorVersionInteger = Integer.parseInt(minorVersion);
				    int majorVersionInteger = Integer.parseInt(majorVersion);
				    Log.w(TAG0, "Minor Version integer is " + minorVersionInteger);
				    Log.w(TAG0, "Major Version integer is " + majorVersionInteger);
				    
				    // If the version is less than 4, we can't run the Voice Activity.
				    //
				    if (
				    		(majorVersionInteger < 4) ||
				    		(majorVersionInteger == 4) && (minorVersionInteger < 4)
				    	)
				    {
				    	Toast.makeText(getApplicationContext(), "Unfortunately, your operating "
				    				+ "system version is less than 4.4, which is the minimum level "
				    				+ "for using the Voice facility.",
				    			   Toast.LENGTH_LONG).show();
				    }
				    
				    // Otherwise, initiate the Voice Activity: since the OS version is 4.4 or above.
				    //
				    else
				    {
						// Set up a progress notification for display while the network access of the User Guide
						// data is in progress.
						//
						ProgressDialog progress = new ProgressDialog(BookCoverActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
						progress.setTitle("UltraAPEX Knowledge Point");
				      	progress.setMessage("Starting voice activation...");
				      	
				      	// The netOp operation is the network activity we do in the background,
						// in order to get the xml file with the data for the chapter.
				      	//
						netOp mySecondOp = new netOp(progress);
						
						// We currently know that this is the user guide. So send the network operation off
						// explicitly to get the User Guide pathname.
						//
						String XMLfileLocationForUG = MetaDataForSuite.allDocuments[idInt][4];
						mySecondOp.execute(XMLfileLocationForUG);
				      	
						
						// Start the voice activity.
						//
				      	Intent intent = new Intent(BookCoverActivity.this, VoiceActivity.class);     
				      	startActivity(intent);
				    }
				}
			});
		}
	}
	
	/**
	 * Handles the network-retrieval of the xml 
	 * file that contains information on the list of chapters to be
	 * displayed in the list view, if the User Guide test is accessed from the spinner of
	 * the animation or voice activity. This operation is performed in the background, with the Document
	 * object derived from the xml file then handled by the onPostExecute method.
	 * 
	 * @author tony.hillman@ultra-as.com
	 *
	 */
	public class netOp extends AsyncTask<String, Void, Document>
	{		
		/**
		 * A progress notification object, to be shown to the user while retrieval of
		 * the xml file is taking place.
		 * 
		 */
		private ProgressDialog progress;
		
		/**
		 * A constructor that establishes the progress dialog created in the onCreate of
		 * CustomizedContentListViewForBook as the dialog to be shown during retrieval of the
		 * xml file.
		 * 
		 * @param progress The progress dialog to be shown the user.
		 * 
		 */
		public netOp(ProgressDialog progress)
		{
			this.progress = progress;
		}
		
		/**
		 * Performs retrieval of the xml file required by the ListView to be displayed.
		 * 
		 * @param params A url or file pathname that is the location of the xml fiile.
		 * @return A document object potentially to be used in post-execution.
		 * 
		 */
		protected Document doInBackground(String... params)
		{
			/**
			 * A document object that is instantiated according to the contents of the
			 * retrieved xml file, and is passed on for post-execution handling.
			 * 
			 */
			Document doc = null;
			
			// If we have been through this routine before, then the doc
			// object we need to derive from the XML file already exists.
			// So, we can skip the file-access and transformation routine.
			// Otherwise, we do indeed go out to the network.
			//
			if (MetaDataForDocument.all_chapters_doc_object == null || whetherUG == 1)
			{
				// Prepare an xml parser and a string in which to contain the xml file's pathname.
				// See XMLParser.java.
				//
				XMLParser parser = new XMLParser();
				
				if (MetaDataForSuite.Connectivity == 0)
				{
					xml = parser.getXmlFromUrl(params[0]); 
				}
				
				// Otherwise, we are dealing with a file pathname.
				//
				else
				{
					try 
					{
						xml = MyFileReader.readFile(params[0]);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
					
				if (xml == null)
				{
					Log.w(TAG1, "Looks like the string is null...");
				}
				else
				{
					Log.w(TAG1, "String is..... " + xml); 
				}
				
				// Now instantiate the doc object with an appropriate parsing of the xml file.
				//
				doc = parser.getDomElement(xml); 
				
				// Now that we have a doc object, save it for further
				// use, so that we can avoid going back to the network
				// more than once.
				//
				MetaDataForDocument.all_chapters_doc_object = doc;
				
				// If we are currently dealing with the user guide, make a separate copy of
				// the object, which thus will not change its value as we access different
				// documents.
				//
				if(whetherUG == 1)
				{
					MetaDataForDocument.user_guide_chapters_doc_object = MetaDataForDocument.all_chapters_doc_object;
				}
			}
			
			// If at some earlier stage we already instantiated the all_docs_doc_object, simply
			// re-use it, and so avoid going back out over the network again.
			//
			else
			{
				doc = MetaDataForDocument.all_chapters_doc_object;
			}
			
			// Return the doc object that contains the full list of documents, so that it can be
			// employed in post-execution.
			//
			return doc;
		}
		
		/**
		 * Performs the majority of work required for getting the User 
		 * Guide chapter information. Once a Document has been
		 * retrieved, containing information on all the chapters, onPostExecute
		 * accepts this as an argument, retrieves the information within, and uses it to
		 * display the List View: and, when the user makes a selection, performs an
		 * appropriate routine.
		 * 
		 * @param Document A Document object containing information on available chapters.
		 * 
		 */
		protected void onPostExecute(Document thedoc)
		{
			// Get rid of the "Preparing..." notification.
			//
		    progress.dismiss();
		    
			Log.w(TAG2, "doInBackground completed.");
			
			// It may be that as well as an existing doc object, we also have
			// the entire array list that we derive from the doc object. In which
			// can, we can by-pass the arrayList creation routine, and save some
			// time. Otherwise, we do the creation:
			//
			if (MetaDataForDocument.allChaptersArrayList == null || whetherUG == 1)
			{
				XMLParser parser = new XMLParser();
				final Document doc = thedoc;
			
				// Get all of the document nodes from the DOM document, and put them
				// into a node list structure.
				//
				Log.w(TAG2, "Getting nodelist now.");
				NodeList nl = doc.getElementsByTagName(KEY_SECTION);
				numberOfContentNodes = nl.getLength();
				Log.w(TAG2, "number of content nodes is " + numberOfContentNodes);
				
				// Save the number of content nodes within the BookMetaDataRegistry.
				//
				MetaDataForDocument.nodesInCurrentXMLsourcefile = numberOfContentNodes;
				
				// We need to keep a separate track of this value for the User Guide, since it maintains
				// "horizontal access" from the spinner in the animations area. Thus, we substitute this
				// for any other value of nodesInCurrentXMl sourcefile that has been otherwise established
				// each time we access the user guide from the spinner.
				//
				if (whetherUG == 1)
				{
					MetaDataForDocument.nodesInUserGuideXMLsourcefile = numberOfContentNodes;
				}
			
				// Loop through the node list. Each time you loop, create
				// a hash table, which associates the node name with the node
				// value: for example the value of "title" might be "1: About Ultra Apex", 
				// while the value of "id" might be "0". These
				// names and values go as pairs into the hash table. 
				//
				for (int i = 0; i < nl.getLength(); i++) 
				{
					Log.w(TAG2, "nl.getLength is " + nl.getLength());
					map = new HashMap<String, String>();
					Element e = (Element) nl.item(i);
				
					// Add each child node and its value to the hash table.
					//
					map.put(KEY_ID, parser.getValue(e, KEY_ID));
					map.put(KEY_CHAP_TITLE, parser.getValue(e, KEY_CHAP_TITLE));
					map.put(KEY_DESCRIPTION, parser.getValue(e, KEY_DESCRIPTION));
					map.put(KEY_THUMBNAIL, parser.getValue(e, KEY_THUMBNAIL));
					map.put(KEY_SRC_DOC, parser.getValue(e, KEY_SRC_DOC));
					map.put(KEY_SRC_TOC, parser.getValue(e, KEY_SRC_TOC));

					// Every time you loop, add your hash table of names and
					// values for this particular song to the array list you
					// created.
					//
					chaptersList.add(map);
				
					Log.w(TAG2, "size of chaptersList is " + chaptersList.size());
				}
				
				// Save the newly created array list as part of the BookMetaDataRegistry
				// object, for further use:
				//
				MetaDataForDocument.allChaptersArrayList = chaptersList;
				
				if (whetherUG == 1)
				{
					MetaDataForDocument.userGuideChaptersArrayList = chaptersList;
				}
			}
			
			// Otherwise, if we already have an array list saved, use that instead.	
			//
			else
			{
				chaptersList = MetaDataForDocument.allChaptersArrayList;
			}
	        
	        // Convert the chapter array list, which we have already established as a value
	        // in the Book Meta Data Registry, to a 2D string array, and save that also in
	        // the same registry. If it already exists there, no need to bother.
	        //
	        if (MetaDataForDocument.allChapters == null || whetherUG == 1)
	        {
	        	// Set up the BookMetaDataRegistry to contain all the data in the
	        	// XML file, in the form of a 2D java string array.
	        	//
	        	MetaDataForDocument.allChapters = new String [numberOfContentNodes]
	        													[NUMBER_OF_ITEMS_IN_NODE];
	        	
	        	// The routine assembleAllContentInfo transforms the document array list into
	        	// a 2D string.
	        	//
	        	MetaDataForDocument.allChapters = assembleAllContentInfo(chaptersList);
	        	Log.w(TAG2, "Set BookMetaDataRegistry." + '\n');
	        	
	        	// If this is the user guide that we are looking at, keep a copy.
	        	//
	        	if (whetherUG ==1)
	        	{
	        		MetaDataForDocument.userGuideChapters = assembleAllContentInfo(chaptersList);
	        	}
	        	
	        	// Save the locations of the chapter TOC files for the current document in the Book
	        	// Meta Data Registry.
	        	//
        		MetaDataForDocument.chapterTOCfiles = new String[numberOfContentNodes];
	        
        		// Save the chapter toc file for each node.
        		//
        		// Note that this is currently suboptimal, because it involves iterating over the final nodes for the
        		// chapter, which in each case are intended for navigation, and so don't have a toc file
        		// accompanying them. Once we've standardized on the navigation elements, we'll subtract from
        		// the require iterations here.
        		//
	        	for (int k = 0; k <= (numberOfContentNodes - 1); k++)
	        	{
	        		// The sixth position, represented by the integer 5, is always the specification of the toc
	        		// file for the current chapter.
	        		//
	        		MetaDataForDocument.chapterTOCfiles[k] = MetaDataForDocument.allChapters[k][5];
	        	}
	        }
	        else
	        {
	        	Log.w(TAG2, "We already have established values for BookMetaDataRegistry.allChapters." + '\n');
	        } 
	        
	        // Now, depending on which button was clicked, start either the Animation or the Voice
	        // Activity.
	        //
	        // If the Animations Activity has been chosen...
	        //
	        if (whichUGActivityChosen == 1) 
	        {
	        	// Having initialised the User Guide text components, we can now access the animations.
	        	//
	        	//Intent intent = new Intent(BookCoverActivity.this, ListViewForPlayer.class);
	        	//Intent intent = new Intent(BookCoverActivity.this, VideoActivity.class);
	        	
	        	Log.w(TAG0, "Performed user-guide setup...\n"); 
	        	
	        	// Prepare the intent for transitioning to the next activity.
	        	//
	        	Intent intent = null;
	        	
	        	// From this point, our ulterior destination is the grid view that
	        	// presents what videos are available for viewing. This involves a
	        	// physical reorienting of the device from portrait to landscape. If this
	        	// is the first time we have done this, instead of going directly to
	        	// the grid, we first visit a transitional activity, which shows an
	        	// animation encouraging the user to reorient. 
	        	//
	        	// If we have already done this, don't do it again. We only do it
	        	// one time, then assume the user understands.
	        	//
	        	if (MetaDataForSuite.goLandscapeUsed == 1)
	        	{
	        		// Prepare to go directly to the grid.
	        		//
	        		intent = new Intent(BookCoverActivity.this, GridViewForVideoActivity.class);
	        	}
	        	else
	        	{
	        		// Prepare to go to the supportive animation.
	        		//
	        		intent = new Intent(BookCoverActivity.this, GoLandscapeActivity.class);
	        		
	        		// Set a flag to ensure we don't do this again.
	        		//
	        		MetaDataForSuite.goLandscapeUsed = 1;
	        	}
	        	
	        	// Bring along the address of the xml file that contains the
	        	// list of videos.
	        	//
	        	intent.putExtra("here", Environment.getExternalStorageDirectory() +
	        								"/knowledgePointSD/video/all_videos.xml");
			
	        	// Start the activity, whichever it may be.
	        	//
	        	startActivity(intent); 
	        }
	        else
	        {
	        	// If the Voice Activity has been chosen...
	        	//
	        	if (whichUGActivityChosen == 2)
	        	{
	        		// Having initialised the User Guide text components, we can now access the voice activity.
	        		//
		        	Intent intent = new Intent(BookCoverActivity.this, VoiceActivity.class);
				
		        	startActivity(intent);
	        	}
	        }
		}	
	}	
	
	/**
	 * Transforms an Array List of Hash Maps into
	 * a two-dimensional string. It is used to allow document locations to be ascertained with
	 * ease when selected by the user.
	 *
	 * @param map An array list of hash maps, each of which features string pairings.
	 * @return A two-dimensional string.
	 * 
	 */
	private String[][] assembleAllContentInfo(ArrayList<HashMap<String, String>> map)
	{	
		// Establish a 2D string that will contain all information from the xml file. 
		//
		String[][] sumTotalContent = new String[numberOfContentNodes][NUMBER_OF_ITEMS_IN_NODE];
		
		// Get each map-strip in turn.
		//
		for (int i = 0; i <= (numberOfContentNodes - 1); i++)
		{
			// Create a hash map to hold the data for the current entry in the array list.
			//
			HashMap<String, String> subMap = new HashMap<String, String>();
			subMap = map.get(i);
			
			// Create a hash map to hold the data for the current entry in the array list.
			//
			String[] stripString = getContentInfo(subMap);
			sumTotalContent[i] = stripString;
		}
		
		// Return the fully instantiated 2D string array.
		//
		return sumTotalContent;
	}	
	
	/**
	 * Returns a string array that contains the information on a chosen topic.
	 * It takes as its argument a hash map of string pairs, which correspond to the node and value
	 * data for the selected topic.
	 * 
	 * @param map A hash map of string pairings.     	
	 * @return A string array containing information on the chosen topic.
	 * 				
	 */
	private String[] getContentInfo(HashMap<String, String> map) 
	{		
		// Declare a string array, which we will use to contain the information
		// from a one-strip hash mapping.
		//
		String contentInfo[] = new String[NUMBER_OF_ITEMS_IN_NODE];

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
				contentInfo[0] = value;
			} 
			else 
			{
				if (key_string == "chap_title") 
				{
					contentInfo[1] = value;
				} 
				else 
				{
					if (key_string == "description") 
					{
						contentInfo[2] = value;
					} 
					else 
					{
						if (key_string == "thumbnail") 
						{
							contentInfo[3] = value;
						} 
						else 
						{
							if (key_string == "src_doc") 
							{
								contentInfo[4] = value;
							}
							else
							{
								if (key_string == "src_toc")
								{
									contentInfo[5] = value;
								}
							}
						}
					}
				}
			}
		}

		// Return the fully instantiated string array.
		//
		return contentInfo;
	}	
	
	/**
	 * The BackLoad class fetches the image used for the cover of the
	 * currently selected document.
	 * 
	 * @author tony.hillman@ultra-as.com
	 *
	 */
	public class BackLoad extends AsyncTask<String, Void, Bitmap>
	{
		/**
		 * Fetches the image required for the cover, and returns it as a bitmap.
		 * 
		 * @param params One or more strings. Currently unused.
		 * 
		 * @return A bitmap for the image.
		 * 
		 */
		@Override
		protected Bitmap doInBackground(String... params)
		{	
			Bitmap cover_bitmap = Utils.fetchImage(currentBookCoverImageLocation);
			
			return cover_bitmap;
		}
		
		/**
		 * Establishes the bitmap returned by doInBackground as the image bitmap for the image
		 * frame used for the book cover.
		 * 
		 * @param theBitmap The bitmap returned by doInBackground for use as the cover image.
		 * 
		 */
		@Override
		protected void onPostExecute(Bitmap theBitmap)
		{
			imgFrame.setImageBitmap(theBitmap);
		}
	}
	
	/**
	 * Unbinds drawables and explicitly forces a garbage collection.
	 * 
	 */
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		
		// Unbinding is required to ensure that the VM heap space is not occupied by old
		// images - these not being garbage-collected as regular Java objects.
		//
		Utils.unbindDrawables(findViewById(R.id.suiteContentsButton));
		Utils.unbindDrawables(findViewById(R.id.documentContentsButton));
		Utils.unbindDrawables(findViewById(R.id.bookcoverimage));

		System.gc();
	}
}

