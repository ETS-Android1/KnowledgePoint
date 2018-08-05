package com.ueas.kpallv1g6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Allows users to ask for the definition of a term, using their voice. The vocal
 * query is dispatched to the Google server infrastructure, and there is transformed into a string, and
 * returned to the VoiceActivity. The VoiceActivity attempts to match the string with ones maintained in
 * a local xml file. If a match is made, the corresponding definition-string is retrieved and itself
 * sent to the Google server infrastructure, where is is transformed into an audible statement. This is
 * returned to the VoiceActivity, and duly played to the user. 
 * 
 * The VoiceActivity also features a spinner, whose elements are populated once a user-query has been
 * identified. Each row-element corresponds to a text-segment in the User Guide that provides additional
 * information on the user's voiced query. Therefore, selecting a row-element takes the user to that
 * passage in the User Guide.
 * 
 * @author tony.hillman@ultra-as.com
 *
 */
public class VoiceActivity extends Activity implements OnClickListener, OnInitListener  
{
	/**
	 * A TextView used for displaying the system-interpretation of the user's spoken query,
	 * and for error messaging.
	 * 
	 */
	private TextView mText;
	
	/**
	 * A TextView employed as a coloured border for the TextView that displays interpretations
	 * and errors: which is to say, it is marginally the bigger of the two, and occupies a superset
	 * of the same visual space, so that all but its edges are occluded.
	 * 
	 */
	private TextView nText;
	
	/**
	 * A speech recognizer, used to capture the spoken query of a user.
	 * 
	 */
	private SpeechRecognizer sr;
	
	/**
	 * An object used for translating text (returned by the Google server in response to a query) 
	 * to audible speech.
	 * 
	 */
	private TextToSpeech tts;
	
	/**
	 * A graphical button that the user must press in order to start the listener that
	 * will capture a voiced query.
	 * 
	 */
	private Button speakButton = null;
	
	/**
	 * One of three image frames used to establish an animation that shows the user when
	 * to voice a query.
	 */
	private ImageView imgFrame;
	
	/**
	 * The second of three image frames used to establish an animation that shows the user when
	 * to voice a query.
	 * 
	 */
	private TextView imgFrame0;
	
	/**
	 * The third of three image frames used to establish an animation that shows the user when
	 * to voice a query.
	 * 
	 */
	private TextView imgFrame1;
	
	/**
	 * A progress dialog shown to the user while the activity is performing its set-up routine.
	 * 
	 */
	public static ProgressDialog progress5 = null;
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private final static String TAG0 = "onCreate: ";
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private final static String TAG1 = "returnDocumentObject: ";
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private static final String TAG2 = "onPostExecute";
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private static final String TAG3 = "onClick";
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private static final String TAG4 = "onInit";
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private static final String TAG5 = "RecognitionListener method";
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private static final String TAG6 = "onResults";
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private static final String TAG7 = "returnTomenu";
	
	/**
	 * A string used for writing to the LogCat.
	 */
	private static final String TAG8 = "setUpSpinner";
	
	/**
	 * An animation drawable, the animation derived from which indicates to the user that a query
	 * should be voiced.
	 * 
	 */
	private AnimationDrawable frameAnimation = new AnimationDrawable();
	
	/**
	 * A string that specifies the location of the xml file containing matchable queries and corresponding
	 * answers.
	 * 
	 */
	// NOTE: PERHAPS SHOULD HAVE THIS AND META DEFINITIONS FROM OTHER ACTIVITIES LISTED
	// IN A MASTER XML FILE. FIX.
	//
	private static String XML_DEFINITION_FILE_LOCATION = Environment.getExternalStorageDirectory() +
									"/knowledgePointSD/voice/userguide/xml/userguideTermDefinitionList.xml";
	
	/**
	 * An object that contains all information on term definitions and corresponding details from the
	 * xml file.
	 */
	private termDefinitionInfo currentTermDefinitionInfo = null;
	
	/**
	 * A string array used to house the references to User Guide passages, to which the user
	 * can transition via the spinner.
	 * 
	 */
	private String[] seeAlsoTitles;
	
	/**
	 * A string array used to house the reference the User Guide passages, to which the user
	 * can transition via the spinner. This differs from seeAlsoTitles, in that it is structured
	 * to contain an additional, initial member, which is the display notification for the spinner.
	 * 
	 */
	private String[] seeAlsoTitlesRevised;
	
	/**
	 * A string array used to house the locations of each text passage in the User Guide to which the
	 * user can transition.
	 * 
	 */
	private String[] seeAlsoLocations;
	
	/**
	 * A string array used to house the chapter numbers that correspond to User Guide text passage
	 * references. This will be used to establish the decoration for the Chapter Activity window.
	 * 
	 */
	private String[] seeAlsoChapNos;
	
	// ArrayLists used to grab string information from the xml file for all supportive
	// information. Once established, these ArrayLists will be used to instantiate
	// a termDefinitionInfo object, which in turn is used to populate the Activity.
	/**
	 * A dynamically expansible array list, used to acquire question strings from the xml
	 * file containing information for the activity. This array list is used to
	 * instantiate a termDefinition object, which is in turn used to populate the
	 * Activity.
	 * 
	 */
	private List<String> questionStrings = new ArrayList<String>();
	
	/**
	 * A dynamically expansible array list, used to acquire answer strings from the xml
	 * file containing information for the activity. This array list is used to
	 * instantiate a termDefinition object, which is in turn used to populate the
	 * Activity.
	 * 
	 */
	private List<String> answerStrings = new ArrayList<String>();
	
	// Each referenceStringArray (or imageStringArray) contains a string array for each reference/image
	// within a reference_list/image_list. The strings in each array are the reference/image items.
	/**
	 * A dynamically expansible array list of string arrays. Each string array corresponds to a
	 * reference within a reference_list for a located term. The strings in each array are the
	 * individual reference items.
	 * 
	 */
	private ArrayList<String[]> referenceStringArrays = new ArrayList<String[]>();
	
	// NOTE: This activity and the corresponding xml file have been established under the
	// expectation that an animation can be shown in parallel to the audible delivery of
	// the answer to the query. This has not been implemented at this point, but might be
	// at some stage. Therefore, the possibility of a sequence of multiple images being on-hand,
	// each with a stated duration, is somewhat accounted for. FIX.
	//
	/**
	 * A dynamically expansible array list of string arrays. Each string array corresponds to an
	 * image within an image_list for a located term. The strings in each array are the
	 * individual image items.
	 * 
	 */
	private ArrayList<String[]> imageStringArrays = new ArrayList<String[]>();
	
	// A copy of the ArrayList that contains the refMember/imageMember arrays for references/images. We
	// make this routinely, because our standard way of determine it's time to archive
	// the ArrayList we currently hold (that is the one for the previous term_definition),
	// is to recognize that the counter for the term_definitions has been incremented: this
	// way, we know that the data for the previous term_definition should be saved, and
	// our data-structures readied for a new term_definition's worth of data. This gives
	// us the problem that the final ArrayList we composed lacks a trigger for archiving,
	// since there is no incrementation of the term_definition counter left to occur. So,
	// at that point, we add the data in the ArrayList copy to the archive, and we are done.
	/**
	 * A dynamically expansible array list of string arrays, used in the archiving of
	 * information related to references for the current term definition.
	 * 
	 */
	private ArrayList<String[]> referenceStringArraysCopy = new ArrayList<String[]>();
	
	/**
	 * A dynamically expansible array list of string arrays, used in the archiving of
	 * information related to images for the current term definition.
	 * 
	 */
	private ArrayList<String[]> imageStringArraysCopy = new ArrayList<String[]>();
	
	// NOTE: At some point, we should move to using ArrayLists of ArrayLists, rather than
	// the static 3D arrays further below. 
	//private List<String[]> imageStringArrays = new ArrayList<String[]>();
	//private List<ArrayList<String[]>> termDefinitionReferenceItems = new ArrayList<ArrayList<String[]>>();
	
	// We currently hard-code the number of these that we support. 6 for the reference items is
	// standard; 3 for image. The references are likely never to be above 5 or 6, in practical terms,
	// so 15 gives us headroom. The figure quite likely to exceed the current maximum is
	// the term definitions (at 150). Number of images likely used as yet unclear.
	//
	// NOTE: ALL THESE SHOULD PROBABLY BE SET FROM A TOP-LEVEL XML FILE. FIX.
	//
	/**
	 * The maximim number of term definitions that we currently support. Used in length-assignment
	 * for a supportive data structure.
	 * 
	 */
	private final int MAXIMUM_NUMBER_OF_TERM_DEFINITIONS = 150;
	
	/**
	 * The maximum number of references within a term definition, as currently supported. Used in
	 * length-assignment for a supportive data structure.
	 * 
	 */
	private final int MAXIMUM_NUMBER_OF_REFERENCES_IN_EACH_TERM_DEFINITION = 15;
	
	/**
	 * The maximum number of list items per reference that we currently support. Used in length-assignment
	 * for a supportive data structure.
	 * 
	 */
	private final int MAXIMUM_NUMBER_OF_LIST_ITEMS_IN_EACH_REFERENCE = 6;
	
	/**
	 * The maximum number of images that we currently support for each term definition. Used in
	 * length-assignment for a supportive data structure.
	 * 
	 */
	private final int MAXIMUM_NUMBER_OF_IMAGES_IN_EACH_TERM_DEFINITION = 20;
	
	/**
	 * The maximum number of list items per image that we currently support. Used in length-assignment
	 * for a supportive data structure.
	 * 
	 */
	private final int MAXIMUM_NUMBER_OF_LIST_ITEMS_IN_EACH_IMAGE = 3;
	
	// This 3D array is temporary storage for the information pulled from the
	// voice-supportive xml file. The info is to be transferred to the object.
	/**
	 * A temporary storage facility for information derived from the xml file supporting
	 * the activity.
	 * 
	 */
	private String[][][] temp3Darray = new String[MAXIMUM_NUMBER_OF_TERM_DEFINITIONS]
			 									 [MAXIMUM_NUMBER_OF_REFERENCES_IN_EACH_TERM_DEFINITION]
			 									 [MAXIMUM_NUMBER_OF_LIST_ITEMS_IN_EACH_REFERENCE];
	
	// This 3D array is temporary storage for the information pulled from the
	// voice-supportive xml file. The info is to be transferred to the object.
	/**
	 * A temporary storage facility for information derived from the xml file supporting
	 * the activity.
	 * 
	 */
	private String[][][] temp3Darray2 = new String[MAXIMUM_NUMBER_OF_TERM_DEFINITIONS]
				 									 [MAXIMUM_NUMBER_OF_IMAGES_IN_EACH_TERM_DEFINITION]
				 									 [MAXIMUM_NUMBER_OF_LIST_ITEMS_IN_EACH_IMAGE];
		
	// Length of the string arrays needed to retain info from xml file. This number
	// is incremented by one with every term definition we encounter. Therefore, its
	// meaning is "total number of term definitions arrived at so far".
	/**
	 * A counter, used to keep track of the number of term definitions we have so
	 * far processed, based on user-queries.
	 */
	int totalNumberOfTermDefinitions = 0;
	
	// Integer to help keep track of which term definition we are currently looking
	// at. When this is identical to totalNumberOfTermDefinitions, we know we are
	// continuing to look at information from indeed that particular term definition.
	// But when totalNumberOfTermDefintions is one greater, we infer that we have
	// entered the next loop, and that any outstanding data we have on hand must
	// be stored persistently under the number of the previous term definition, and
	// all data structures for the new term definition zeroed out.
	/**
	 * A counter, used to determine which term definition we are currently examining.
	 * 
	 */
	int currentNumberOfTermDefinitions = 0;
	
	/**
	 * A counter, used to correlate the identity of the current term definition with
	 * corresponding images.
	 * 
	 */
	int currentNumberOfImageTermDefinitions = 0;
	
	// Number of References in the current ReferenceItem/TermDefinition.
	/**
	 * A counter, used in establishing the number of references in the reference 
	 * list for the current term definition.
	 * 
	 */
	int currentNumberOfReferences = 0;
		
	// Number of Images in the current ImageList/TermDefinition.
	/**
	 * A counter, used in establishing the number of images in the image list for
	 * the current term definition.
	 * 
	 */
	int currentNumberOfImages = 0;
	
	// A flag set while we iterate over the contents of reference lists. The flag
	// is set to 1 when we detect a node that is an "element" node (rather than
	// a "#text" node, and this tells us that we have information to be saved. The
	// flag is reset to 0 once the reference items for the current reference have
	// been stored.
	/**
	 * A flag, set during the iterations used to produce a doc object containing supportive
	 * reference-information. The flag is set to indicate that we have found an "element" node,
	 * which consistutes saveable information. It is then reset to 0 once the information
	 * has been stored.
	 * 
	 */
	int elementNodeDetected = 0;
	
	/**
	 * A flag, set during the iterations used to produce a doc object containing supportive
	 * image-information. The flag is set to indicate that we have found an "element" node,
	 * which consistutes saveable information. It is then reset to 0 once the information
	 * has been stored.
	 * 
	 */
	int elementImageNodeDetected = 0;
	
	// When we add an element node, we keep a count.
	/**
	 * A counter, set during the iterations used to produce a doc object from xml. Incremented
	 * whenever an "element" node is discovered.
	 * 
	 */
	int numberOfElementNodesAdded = 0;
	
	/**
	 * A counter, set during the iterations used to produce a doc object from xml. Incremented
	 * whenever an "element" node for images is discovered.
	 * 
	 */
	int numberOfImageElementNodesAdded = 0;
	
	// Counts of the number of reference/image lists (which is the same as the number of
	// term definitions) that we encounter.
	/**
	 * A counter that reflects the number of reference_lists we encounter during creation of
	 * a doc object from xml.
	 * 
	 */
	int numberOfReferenceLists = 0;
	
	/**
	 * A counter that reflects the number of image_lists we encounter during creation of
	 * a doc object from xml.
	 * 
	 */
	int numberOfImageLists = 0;

	/**
	 * Establishes the layout as that specified
	 * in voice_activity.xml. It creates Java objects for all image frame and text view
	 * objects, and sets up the animation run in order to prompt for user-speech. It also
	 * sets up the speech recognizer, so that spoken input can be captured. Finally, it
	 * puts up a progress notification, while initiating a background process to retrieve
	 * supportive data from an xml file.
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// Establish the content view for the voice activity. Do this conditionally, based on the 
		// screen-size/aspect-ratio combination we have already determined.  
		//
		switch (MetaDataForSuite.currentAspectRatio)
		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				setContentView(R.layout.voice_activity_for_zero___four_point_three);
				Log.w(TAG0, "Set Voice content for 0.\n");
				break;		
				
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				setContentView(R.layout.voice_activity_for_one_zero___four_point_zero_etc);
				Log.w(TAG0, "Set Voice content for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				setContentView(R.layout.voice_activity_for_two_zero___four_point_three_etc);
				Log.w(TAG0, "Set Voice content for 20.\n");
				break;
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				setContentView(R.layout.voice_activity_for_three_zero___four_point_seven);
				Log.w(TAG0, "Set Voice content for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				setContentView(R.layout.voice_activity_for_four_zero___seven);
				Log.w(TAG0, "Set Voice content for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				setContentView(R.layout.voice_activity_for_five_zero___ten);
				Log.w(TAG0, "Set Voice content for 50.\n");
				break;
				
		    // This is a 1080x1920 generic-size device.
			//
			case 60:
				setContentView(R.layout.voice_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Voice content for 60.\n");
				break;
			
			// This is a 1080x1920 5.7 inch device.
			//
			case 70:
				setContentView(R.layout.voice_activity_for_seven_zero___five_point_seven);
				Log.w(TAG0, "Set Voice content for 70.\n");
				break;
			
			// This is a 1200x1920 7 inch device.
			//
			case 80:
				setContentView(R.layout.voice_activity_for_eight_zero___seven);
				Log.w(TAG0, "Set Voice content for 80.\n");
				break;
			
			// This is a 1200x1920 10 inch device.
			//
			case 90:
				setContentView(R.layout.voice_activity_for_nine_zero___ten);
				Log.w(TAG0, "Set Voice content for 90.\n");
				break;
				
			// This is a 1440x900 7 inch device.
			//
			case 100:
				setContentView(R.layout.voice_activity_for_one_zero_zero___seven);
				Log.w(TAG0, "Set Voice content for 100.\n");
				break;

			// This is a 1440x2650 5.1 inch device.
			//
			case 110:
				setContentView(R.layout.voice_activity_for_one_one_zero___five_point_one);
				Log.w(TAG0, "Set Voice content for 110.\n");
				break;
			
			// If we are not sure, we use the following default.
			//
			default:
				setContentView(R.layout.voice_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Voice content for default.\n");
				break;			
		}
		
		// Set the Android titlebar for the current activity.
		//
		this.setTitle("Ask a question, using...");
		
		// Establish the application titlebar for the current activity.
		//
		imgFrame = (ImageView) findViewById(R.id.imgFrame);
		imgFrame1 = (TextView) findViewById(R.id.imgFrame1);
		imgFrame1.setBackgroundColor(Color.parseColor("#FFFFFF"));
		
		imgFrame0 = (TextView) findViewById(R.id.imgFrame0);
		imgFrame0.setBackgroundColor(Color.parseColor("#335495"));
		
		TextView tv = (TextView)findViewById(R.id.my_textview);
		
		tv.setText("\u0009" + "UltraAPEX Voice-Activated Guidance");
		
		// Establish the button that the user presses, in order to start the listener
		// and so make voice-recognition posssible.
		//
		speakButton = (Button) findViewById(R.id.btn_speak);
		speakButton.setText("Press here to begin.");
		speakButton.setTextColor(Color.parseColor("#335495"));
		
		// Establish the text view window that presents a textual interpretation of
		// what the user said, and also, when necessary, presents error information.
		//
		mText = (TextView) findViewById(R.id.textView1);
		nText = (TextView) findViewById(R.id.textView0);
		
		// The nText text view actually functions as a border for the mText text view. Therefore,
		// we give it a color.
		//
		nText.setBackgroundColor(Color.parseColor("#335495"));
		
		// Establish the infrastructure for the animation that is played in order to
		// tell the user that it's time to speak.
		//
		imgFrame.setBackgroundResource(R.layout.animation_frames);
		frameAnimation = (AnimationDrawable) imgFrame.getBackground();	
		
		// Initiate the process whereby listening can be made to begin, when the
		// button is pressed by the user.
		//
		speakButton.setOnClickListener(this);
		sr = SpeechRecognizer.createSpeechRecognizer(this);
		sr.setRecognitionListener(new listener());
		tts = new TextToSpeech(this, this); 
		
		// Now, in prepration for seeking the supportive xml file, establish a progress
		// notification, which will be shown while the background activity occurs.
		//
		progress5 = new ProgressDialog(this, R.style.CustomDialog);
		progress5.setTitle("UltraAPEX Knowledge Point");
      	progress5.setMessage("Accessing user-supportive data...");
		
		// The netOp operation is the network activity we do in the background,
		// in order to get the xml file with the data for the chapter, which will
		// be used to create the ListView object.
		netOp5 myOp5 = new netOp5(progress5); 
		
		Log.w(TAG0, "Starting background operation myOp4 now...\n");
		
		// No need to pass a string at this point. This is just a placeholder for
		// now.
		//
		myOp5.execute("hello");
		
		// Show the progress notification while the xml file is accessed in
		// the background.
		//
		progress5.show(); 
	}
	
	/**
	 * Supports the asynchronous access of an xml that contains data
	 * in support of the current activity. 
	 *
	 */
	public class netOp5 extends AsyncTask<String, Void, Document[]>
	{
		/**
		 * A progress dialog that will be shown to the user while the background
		 * processing occurs.
		 * 
		 */
		private ProgressDialog progress5;
		
		/**
		 * A constructor that initialises a progress dialog, to be shown to users during background processing.
		 * 
		 * @param prog A progress dialog object.
		 */
		public netOp5(ProgressDialog prog) 
		{	
			this.progress5 = prog;
		}

		// NOTE: CURRENTLY THE DOC ARRAY IS NOT NEEDED, SINCE WE ARE JUST RETURNING A SINGLE
		// DOC OBJECT. FIX.
		//
		/**
		 * Accesses an xml file from a globally established
		 * location, and returns an array of document objects based on it.
		 * 
		 */
		@Override
		protected Document[] doInBackground(String... arg0) 
		{
			// Create a two-member document object array.
			//
			Document[] docArray = new Document[1];
			
			// Create an initialise a document object.
			//
			Document currentTermDefinitionDoc = null;
			
			// Instantiate the document object, based on the contents of the specified
			// xml file.
			//
			currentTermDefinitionDoc = returnDocumentObject(XML_DEFINITION_FILE_LOCATION);
			
			// Set the first slot in the doc array to the value of the doc object.
			//
			docArray[0] = currentTermDefinitionDoc;
			
			// Return the doc array.
			//
			return docArray;
		}
		
		/**
		 * Accesses an xml file from a specified location, parses it, and returns a document object based on it.
		 * 
		 * @param xmlFileLocation The location of the xml file.
		 * 
		 * @return A document object, based on the xml file specified.
		 * 
		 */
		protected Document returnDocumentObject(String xmlFileLocation)
		{
			// Initialize a document object, which will be returned.
			//
			Document docToBeReturned = null;
				
			// Get the xml file, and parse it into a DOM document.
			//
			XMLParser parser = new XMLParser();
			
			String xmlText = "";
			
			// If we are in network-access mode, and need to deal with a URL...
			//
			if (MetaDataForSuite.Connectivity == 0)
			{
				xmlText = parser.getXmlFromUrl(xmlFileLocation); 
			}
			
			// Alternatively, if we are in local-access mode, and so need simply
			// to access a file from the SD card...
			//
			else
			{
				try 
				{
					xmlText = MyFileReader.readFile(xmlFileLocation);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
				
			if (xmlText == null)
			{
				Log.w(TAG1, "Looks like the xml content has turned out to be null...");
			}
			else
			{
				// Do nothing.
			}
			
			// Parse the text of the xml file into a document object.
			//
			docToBeReturned = parser.getDomElement(xmlText); 
			
			// Return the document object.
			//
			return docToBeReturned;
		}
		
		// Once we have grabbed the xml file and generated the corresponding doc object, we
		// must iterate over it, and store all values in a termDefinitionInfo object. 
		/**
		 * Accepts an array of document objects
		 * from the doInBackground method, and instantiates various Java objects, based on the
		 * contents. 
		 * 
		 */
		protected void onPostExecute(Document[] docArray)
		{
			// A currentTermDefinitionInfobject, to hold string data returned from the xml file.
			//
			currentTermDefinitionInfo = new termDefinitionInfo();
	
			// Create a doc object, instantiating it as the value of the first slot in
			// the document object array passed as an argument.
			//
			Document termsListDoc = docArray[0]; 
			Log.w(TAG2, "Root element is: " + termsListDoc.getDocumentElement().getNodeName() + '\n' + '\n');
			
			// Iterate over the doc object, pulling out strings, and instantiating the currentTermDefinitionInfo
			// object with them.
			//
			termsListDoc.getDocumentElement().normalize();
			Element root = termsListDoc.getDocumentElement();
			NodeList children = root.getChildNodes();
			
			// Examine first the individual term definitions that are the top level members
			// of the doc object. Note that each contains an ID, a question, an answer (all
			// of which contain text values), plus a reference list and an image list (each
			// of which contain two level nestings of a reference/image list, the individual
			// references/images within the list, and the individual members within each
			// reference/image.
			//
			for (int i = 0; i < children.getLength(); i++)
			{
				// This counter gives us the number of references within the single reference_list
				// of each term_definition. Therefore, when we commence our analysis of a new
				// term_definition, we put it back to zero.
				//
				currentNumberOfReferences = 0;
				
				// Likewise, this counter gives us the number of images within the image_list of
				// each term_definition.
				//
				currentNumberOfImages = 0;
				
				Node childNode = children.item(i);
			
				if (childNode.getNodeType() == Node.ELEMENT_NODE)
				{
					// Print out the child-node names, in sequence.
					//
					Log.w(TAG2, "Current element is: " + childNode.getNodeName() + '\n' + '\n');
					
					// We have found a term definition, so increment the corresponding object.
					//
					totalNumberOfTermDefinitions++;
					
					if (currentNumberOfTermDefinitions == 0)
					{
						currentNumberOfTermDefinitions++;
						currentNumberOfImageTermDefinitions++;
					}
					else
					{
						// Do nothing. We'll increment below, when we have archived
						// the references we've gathered for the last term definition.
					}
					
					// Iterate over the object, and retrieve all the information we required
					// from it.
					//
					NodeList grandChildren = childNode.getChildNodes();
					
					for (int j = 0; j < grandChildren.getLength(); j++)
					{	
						Node grandChildNode = grandChildren.item(j);
						
						if (grandChildNode.getNodeType() == Node.ELEMENT_NODE)
						{
							// Print out the grand-child node names (eg, "question", "answer"), in sequence.
							//
							Log.w(TAG2, '\t' + "Current sub-element is: " + grandChildNode.getNodeName() + '\n' + '\n');
							
							// Print out grand-child text values (eg, "what is UltraAPEX", "UltraAPEX is a..." etc).
							//
							Log.w(TAG2, '\t' + "     Current text-value is: \"" 
									+ grandChildNode.getTextContent() 
									+ "\""
									);
							
							// No need to gather the ID information.
							//
							if ((grandChildNode.getNodeName().equalsIgnoreCase("term_id")))
							{
								// Do nothing
							}
							else
							{
								// But we do want the question-strings.
								//
								if ((grandChildNode.getNodeName().equalsIgnoreCase("question")))
								{
									questionStrings.add(grandChildNode.getTextContent());
								}
								else
								{
									// Likewise the answer-strings.
									//
									if ((grandChildNode.getNodeName().equalsIgnoreCase("answer")))
									{
										answerStrings.add(grandChildNode.getTextContent());
									}
								}
							}
							
							// If the current grandChildNode is a <reference_list>...
							//
							if ((grandChildNode.getNodeName().equalsIgnoreCase("reference_list")))
							{
								Log.w(TAG2, "Found a reference_list...");
								numberOfReferenceLists++;

								// Create a NodeList of <references>. This will likely be 2 to 4 elements long.
								//
								NodeList greatGrandChildren = grandChildNode.getChildNodes();
							
								// For each of the <reference> children...
								//
								for (int k = 0; k < greatGrandChildren.getLength(); k++)
								{
									// Everytime we find a valid "reference" node-name, increment the
									// count of references for the current reference-list.
									//
									String currentNodeName = greatGrandChildren.item(k).getNodeName();
									Log.w(TAG2, "Current node name is " + greatGrandChildren.item(k).getNodeName());
									
									if (currentNodeName.equalsIgnoreCase("reference"))
									{
										currentNumberOfReferences++;
										Log.w(TAG2, "currentNumberOfReferences is now " + currentNumberOfReferences);
									}
									
									// Create an array, to hold the members.
									//
									String refMembers[] = new String[6];
									
									// Create a single <reference> Node...
									//
									Node greatGrandChildNode = greatGrandChildren.item(k);
									
									// Get the 6 reference_item children from the <reference> Node...
									//
									NodeList greatGreatGrandChildren = greatGrandChildNode.getChildNodes();
									
									// Establish an index for incrementing the stringArray that is
									// independent of the for-loop (which itself is numerically greater
									// than the length of the array, due to many nodes not being
									// "element" nodes - which is to say, not constituting useful
									// information.) NOTE: THERE HAS TO BE A BETTER WAY OF DOING THIS.
									// FIX.
									//
									int refMemberCount = 0;
									
									// Examine each Node. Only a subset will be element nodes (that is,
									// with real value), so we need to pick these out.
									//
									for (int m = 0; m < greatGreatGrandChildren.getLength(); m++)
									{
										Node greatGreatGrandChildNode = greatGreatGrandChildren.item(m);
								
										if (greatGreatGrandChildNode.getNodeType() == Node.ELEMENT_NODE)
										{
											// Raise a flag to indicate that we have real content in this
											// loop-repetition, and so will need to save it.//
											elementNodeDetected = 1;
											
											Log.w(TAG2, '\t' + "     Current sub-sub-element is: " 
													+ greatGreatGrandChildNode.getNodeName() 
													);

											Log.w(TAG2, "text content of greatGreatGrandChild is "
													+ greatGreatGrandChildNode.getTextContent()
													); 
											
											// Add the string that is the value of the reference member to
											// the array for the current reference.
											//
											refMembers[refMemberCount] = greatGreatGrandChildNode.getTextContent();
											Log.w(TAG2, "refMember " + refMemberCount +" for " + k + " is: " + refMembers[refMemberCount]);
											
											// Increment the index, so that the next string goes into the
											// next slot of the refMembers array.
											//
											if (refMemberCount < 6)
											{
												refMemberCount++;
											}
										} 
									}
									
									// Check to see if the last repetition involved real values - in which
									// case the elementNodeDetected flag will have been set to 1. (We only
									// add our latest array to the array of arrays if there are real values
									// involved.)
									//
									if (elementNodeDetected == 1)
									{						
										// If we are looking at the same term definition that we were looking
										// at the last time we had to add a reference...
										//
										if (totalNumberOfTermDefinitions == currentNumberOfTermDefinitions) 
										{			
											// Add the current array of reference items (the reference) to the reference Array List
											// that will contain all references. 
											//
											referenceStringArrays.add(refMembers);

											// CHECK the composition of the ArrayList. It should grow to contain
											// a String[] for every reference. The numberOfElementNodesAdded is 0
											// the first time around, and is incremented. It must be set back to
											// 0 at the end of the current reference_list.
											//
											for (int y = 0; y <= numberOfElementNodesAdded; y++)
											{
												String[] temp = referenceStringArrays.get(y);
												for (int x = 0; x < 6; x++)
												{
													// As counted from zero, the position of the reference is one less than the
													// current number of references found.
													//
													Log.w(TAG2, "String " + x + " in temp for position " + y + " at " + (numberOfReferenceLists - 1) + " is: " + temp[x]);
												}
											}
											
											// Make a copy of the referenceStringArrays object. We will use this when the
											// overall iteration is completed, in order to ensure that we have a way of
											// adding the last referenceStringArray to the archive (even though there is no
											// "trigger", caused by a further incrementation of the top-level counter.
											//
											referenceStringArraysCopy = referenceStringArrays;
											for (int y = 0; y <= numberOfElementNodesAdded; y++)
											{
												String[] temp2 = referenceStringArraysCopy.get(y);
												for (int x = 0; x < 6; x++)
												{
													// As counted from zero, the position of the reference is one less than the
													// current number of references found.
													//
													Log.w(TAG2, "Copied String in temp2 for position " + y + " at " + (numberOfReferenceLists - 1) + " is: " + temp2[x]);
												}
											}
											// Increment the count.
											//
											numberOfElementNodesAdded++;
										}
										// But in the event we are now looking at a different term definition, we
										// must archive the current contents of referenceStringArrays under the
										// number for the last term definition, and start afresh.
										//
										else
										{	
											Log.w(TAG2, "The term definition counts now differ...");
											
											Log.w(TAG2, "The size of the referenceStringArrays object is " + referenceStringArrays.size());
											
											// Archive the current contents of referenceStringArrays into the
											// temp3Dstring we'll ultimately use to populate the object.
											//
											for (int q = 0; q < referenceStringArrays.size(); q++)	
											{
												String[] archiver = referenceStringArrays.get(q);
												
												Log.w(TAG2, "Content of archiver string array is...");
												
												for (int r = 0; r < 6; r++)
												{
													Log.w(TAG2, "\tString: " + archiver[r]);
													temp3Darray[currentNumberOfTermDefinitions][q][r] = archiver[r];													
												}
											}
											
											// Make the ArrayList null, because we are starting over for the new
											// term definition.
											//
											referenceStringArrays.clear();
											referenceStringArrays.add(refMembers);
											
											numberOfElementNodesAdded = 0;
											
											String[] temp = referenceStringArrays.get(numberOfElementNodesAdded);
											for (int x = 0; x < 6; x++)
											{
												// As counted from zero, the position of the reference is one less than the
												// current number of references found.
												//
												Log.w(TAG2, "String in temp for position " + (numberOfReferenceLists - 1) + " is: " + temp[x]);
											}
										
											// Register that we are now looking at a new term definition.
											//
											currentNumberOfTermDefinitions++;
											
											// Set the count of nodes added to the array string back to zero.
											//
											numberOfElementNodesAdded = 1;
										}

										// Set the flag back to zero.
										//
										elementNodeDetected = 0;
									}	
								}
							}
							else
							{
								// If the node is an <image_list>...
								//
								if((grandChildNode.getNodeName().equalsIgnoreCase("image_list")))
								{
									Log.w(TAG2, "Found an image_list...");
									numberOfImageLists++;
									
									// Create a NodeList of <images>. This will likely be 1 to 8 elements long.
									//
									NodeList greatGrandChildren = grandChildNode.getChildNodes();
									
									// For each of the 2 to 4 <image> children...
									//
									for (int k = 0; k < greatGrandChildren.getLength(); k++)
									{
										String currentNodeName = greatGrandChildren.item(k).getNodeName();
										Log.w(TAG2, "Current node name is " + greatGrandChildren.item(k).getNodeName());
										if (currentNodeName.equalsIgnoreCase("image"))
										{
											currentNumberOfImages++;
											Log.w(TAG2, "currentNumberOfImages is now " + currentNumberOfImages);
										}
										
										// Create an array, to hold the image members.
										//
										String imageMembers[] = new String[3];
										
										// Create a single <image> Node...
										//
										Node greatGrandChildNode = greatGrandChildren.item(k);
										
										// Get the 3 reference_item children from the <reference> Node...
										//
										NodeList greatGreatGrandChildren = greatGrandChildNode.getChildNodes();
										
										// Establish an index for incrementing the stringArray that is
										// independent of the for-loop (which itself is numerically greater
										// than the length of the array.
										//
										int imageMemberCount = 0;
										
										// Examine each node. Only a subset will contain real data, so we
										// need to establish which are these "element" nodes.
										//
										for (int m = 0; m < greatGreatGrandChildren.getLength(); m++)
										{
											Node greatGreatGrandChildNode = greatGreatGrandChildren.item(m);
									
											if (greatGreatGrandChildNode.getNodeType() == Node.ELEMENT_NODE)
											{
												// Raise a flag to indicate that we have real content in this
												// loop-repetition, and so will need to save it.
												//
												elementImageNodeDetected = 1;
												
												Log.w(TAG2, '\t' + "     Current sub-sub-element is: " 
														+ greatGreatGrandChildNode.getNodeName() 
														);

												Log.w(TAG2, "text content of greatGreatGrandChild is "
														+ greatGreatGrandChildNode.getTextContent()
														); 
												
												// Add the string that is the value of the image member to
												// the array for the current image.
												//
												imageMembers[imageMemberCount] = greatGreatGrandChildNode.getTextContent();
												Log.w(TAG2, "imageMember " + imageMemberCount +" for " + k + " is: " + imageMembers[imageMemberCount]);
												
												if (imageMemberCount < 3)
												{
													imageMemberCount++;
												}
											} 
										}
										
										// Check to see if the last repetition involved real values - in which
										// case the elementNodeDetected flag will have been set to 1. (We only
										// add our latest array to the array of arrays if there are real values
										// involved.)
										//
										if (elementImageNodeDetected == 1)
										{
											Log.w(TAG2, "Element Node Detected!");
											Log.w(TAG2, "totalNumberOfTermDefinitions is " + totalNumberOfTermDefinitions);
											Log.w(TAG2, "currentNumberOfImageTermDefinitions is " + currentNumberOfImageTermDefinitions);
											Log.w(TAG2, "numberOfImageElementNodesAdded is: " + numberOfImageElementNodesAdded);
											
											// If we are looking at the same term definition that we were looking
											// at the last time we had to add a reference...
											//
											if (totalNumberOfTermDefinitions == currentNumberOfImageTermDefinitions) 
											{		
												// Add the current array of reference items (the reference) to the reference Array List
												// that will contain all references. 
												//
												imageStringArrays.add(imageMembers);

												// CHECK the composition of the ArrayList. It should grow to contain
												// a String[] for every reference. The numberOfElementNodesAdded is 0
												// the first time around, and is incremented. It must be set back to
												// 0 at the end of the current reference_list.
												//
												for (int y = 0; y <= numberOfImageElementNodesAdded; y++)
												{
													String[] temp = imageStringArrays.get(y);
													for (int x = 0; x < 3; x++)
													{
														// As counted from zero, the position of the reference is one less than the
														// current number of references found.
														//
														Log.w(TAG2, "Sstring " + x + " in temp for position " + y + " at " + (numberOfImageLists - 1) + " is: " + temp[x]);
													}
												}
												
												// Make a copy of the referenceStringArrays object. We will use this when the
												// overall iteration is completed, in order to ensure that we have a way of
												// adding the last referenceStringArray to the archive (even though there is no
												// "trigger", caused by a further incrementation of the top-level counter.
												//
												imageStringArraysCopy = imageStringArrays;
												for (int y = 0; y <= numberOfImageElementNodesAdded; y++)
												{
													String[] temp2 = imageStringArraysCopy.get(y);
													for (int x = 0; x < 3; x++)
													{
														// As counted from zero, the position of the reference is one less than the
														// current number of references found.
														//
														Log.w(TAG2, "Copied String in temp2 for position " + y + " at " + (numberOfImageLists - 1) + " is: " + temp2[x]);
													}
												}
												
												// Increment the count.
												//
												numberOfImageElementNodesAdded++;
											}
											
											// But in the event we are now looking at a different term definition, we
											// must archive the current contents of referenceStringArrays under the
											// number for the last term definition, and start afresh.
											//
											else
											{	
												Log.w(TAG2, "The term definition counts now differ...");
												
												Log.w(TAG2, "The size of the imageStringArrays object is " + imageStringArrays.size());
												
												// Archive the current contents of referenceStringArrays into the
												// temp3Dstring we'll ultimately use to populate the object.
												//
												for (int q = 0; q < imageStringArrays.size(); q++)	
												{
													String[] archiver = imageStringArrays.get(q);
													
													Log.w(TAG2, "Content of archiver string array is...");
													
													for (int r = 0; r < 3; r++)
													{
														Log.w(TAG2, "\tString: " + archiver[r]);
														temp3Darray2[currentNumberOfImageTermDefinitions][q][r] = archiver[r];													
													}
												}
												
												// Make the ArrayList null, because we are starting over for the new
												// term definition.
												//
												imageStringArrays.clear();
												imageStringArrays.add(imageMembers);
												
												numberOfImageElementNodesAdded = 0;
												
												String[] temp = imageStringArrays.get(numberOfImageElementNodesAdded);
												for (int x = 0; x < 3; x++)
												{
													// As counted from zero, the position of the reference is one less than the
													// current number of references found.
													//
													Log.w(TAG2, "String in temp for position " + (numberOfImageLists - 1) + " is: " + temp[x]);
												}
												// Register that we are now looking at a new term definition.
												//
												currentNumberOfImageTermDefinitions++;
												
												// Set the count of nodes added to the array string back to zero.
												//
												numberOfImageElementNodesAdded = 1;
											}

											// Set the flag back to zero.
											//
											elementImageNodeDetected = 0;
										}
									}
								}
							}
						}
					}
				}	
			}
			
			// CHECK contents of question and answer array lists, acknowledging that
			// the lengths are going to be the same.
			//
			for (int e = 0; e < questionStrings.size(); e++)
			{
				Log.w(TAG2, "ArrayList Question String " + e + ": " + questionStrings.get(e));
				Log.w(TAG2, "ArrayList Answer String " + e + ": " + answerStrings.get(e));
			}
			
			// Put the contents of the ArrayLists into the termDefinitionInfo object in
			// the form of Arrays.
			//
			currentTermDefinitionInfo.questions = new String[questionStrings.size()];
			currentTermDefinitionInfo.questions = questionStrings.toArray(currentTermDefinitionInfo.questions);
			
			currentTermDefinitionInfo.answers = new String[answerStrings.size()];
			currentTermDefinitionInfo.answers = answerStrings.toArray(currentTermDefinitionInfo.answers);
			
			// Archive the current contents of the final version of referenceStringArraysCopy into the
			// final slot temp3Dstring we'll ultimately use to populate the object.
			//
			for (int q = 0; q < referenceStringArraysCopy.size(); q++)	
			{
				String[] archiver2 = referenceStringArraysCopy.get(q);
				
				Log.w(TAG2, "Content of archiver2 string array is...");
				
				for (int r = 0; r < 6; r++)
				{
					Log.w(TAG2, "\tString: " + archiver2[r]);
					
					temp3Darray[currentNumberOfTermDefinitions][q][r] = archiver2[r];
					
					Log.w(TAG2, "\t\tIn 3D Array: " + temp3Darray[currentNumberOfTermDefinitions][q][r]);
				}
			}
			
			// Archive the current contents of the final version of imageStringArraysCopy into the
			// final slot temp3Dstring2 we'll ultimately use to populate the object.
			//
			for (int q = 0; q < imageStringArraysCopy.size(); q++)	
			{
				String[] archiver2 = imageStringArraysCopy.get(q);
				
				Log.w(TAG2, "Content of archiver2 string array is...");
				
				for (int r = 0; r < 3; r++)
				{
					Log.w(TAG2, "\tString: " + archiver2[r]);
					
					temp3Darray2[currentNumberOfImageTermDefinitions][q][r] = archiver2[r];
					
					Log.w(TAG2, "\t\tIn 3D Array: " + temp3Darray2[currentNumberOfImageTermDefinitions][q][r]);
				} 
			}
			
			// CHECK contents of object-arrays, assuming that lengths are identical. First, within
			// the question and answer arrays.
			//
			for (int p = 0; p < questionStrings.size(); p++)
			{
				Log.w(TAG2, "Within the final question array, element " + p + " is: " + currentTermDefinitionInfo.questions[p]);
				Log.w(TAG2, "Within the final answer array, element " + p + " is: " + currentTermDefinitionInfo.answers[p]);
			}
			
			// CHECK now the contents of the temp 3D array.
			//
			Log.w(TAG2, "Within final 3D array...");
			for (int q = 1; q <= currentNumberOfTermDefinitions; q++)
			{
				for (int r = 0; r < temp3Darray[q].length; r++)
				{
					for (int s = 0; s < 6; s++)
					{
						// Don't bother printing out null values: the maximum length of
						// r is 15. Most will be null values.
						//
						if (temp3Darray[q][r][s] != null)
						{
							Log.w(TAG2, "For TD " + q + " in ref " + r + " string " + s + " is: " 
										+ temp3Darray[q][r][s]);
						}
					}
				}		
			}
			
			// CHECK now the contents of the temp 3D 2 array.
			//
			Log.w(TAG2, "Within final 3D array...");
			for (int q = 1; q <= currentNumberOfImageTermDefinitions; q++)
			{
				for (int r = 0; r < temp3Darray2[q].length; r++)
				{
					for (int s = 0; s < 3; s++)
					{
						// Don't bother printing out null values: the maximum length of
						// r is 15. Most will be null values.
						//
						if (temp3Darray2[q][r][s] != null)
						{
							Log.w(TAG2, "For TD " + q + " in ref " + r + " string " + s + " is: " 
										+ temp3Darray2[q][r][s]);
						}
					}
				}		
			}
			
			// Copy the temp 3D array into the object.
			//
			currentTermDefinitionInfo.references = temp3Darray;
			
			// CHECK that the references 3D array within the object now contains the
			// correct data.
			//
			Log.w(TAG2, "Within 3D array inside object...");
			for (int q = 1; q <= currentNumberOfTermDefinitions; q++)
			{
				for (int r = 0; r < currentTermDefinitionInfo.references[q].length; r++)
				{
					for (int s = 0; s < 6; s++)
					{
						// Don't bother printing out null values: the maximum length of
						// r is 15. Most will be null values.
						//
						if (currentTermDefinitionInfo.references[q][r][s] != null)
						{
							Log.w(TAG0, "For Final Object TD " + q + " in ref " + r + " string " + s + " is: " 
										+ currentTermDefinitionInfo.references[q][r][s]);
						}
					}
				}		
			}
			
			// Copy the temp 3D array 2 into the object.
			//
			currentTermDefinitionInfo.images = temp3Darray2;
			
			// CHECK that the references 3D array within the object now contains the
			// correct data.
			//
			Log.w(TAG2, "Within 3D array inside object...");
			for (int q = 1; q <= currentNumberOfImageTermDefinitions; q++)
			{
				for (int r = 0; r < currentTermDefinitionInfo.images[q].length; r++)
				{
					for (int s = 0; s < 3; s++)
					{
						// Don't bother printing out null values: the maximum length of
						// r is 15. Most will be null values.
						//
						if (currentTermDefinitionInfo.images[q][r][s] != null)
						{
							Log.w(TAG2, "For Object TD " + q + " in ref " + r + " string " + s + " is: " 
										+ currentTermDefinitionInfo.images[q][r][s]);
						}
					}
				}		
			}
			
			// Preparations are now complete, so dismiss the progress.
			//
			progress5.dismiss();	

		}	// End of onPostExecute method.
	}  	    // End of netOp5 class.
	
	/**
	 * Runnable used to start the frame animation that prompts the user for vocal
	 * input.
	 * 
	 */
	Runnable run = new Runnable()
	{
		@Override
		public void run()
		{
			Log.w(TAG1,"Starting frameAnimation now");
			frameAnimation.start();
		}
	};
	
	/**
	 * Runnable used to stop the frame animation.
	 * 
	 */
	Runnable stopAnimation = new Runnable()
	{
		@Override
		public void run()
		{
			frameAnimation.stop();
		}
	};

	/**
	 * A listener class, used to grab audible user-input.
	 *
	 */
	class listener implements RecognitionListener 
	{
		/**
		 * Writes to LogCat, confirming readiness to take speech input.
		 * 
		 */
		public void onReadyForSpeech(Bundle params) 
		{
			Log.d(TAG5, "onReadyForSpeech");
		}

		/**
		 * Writes to LogCat, confirming the beginning of speech.
		 * 
		 */
		public void onBeginningOfSpeech() 
		{
			Log.d(TAG5, "onBeginningOfSpeech");
		}

		/**
		 * Writes to LogCat, confirming that RMS has changed.
		 * 
		 */
		public void onRmsChanged(float rmsdB) 
		{
			Log.d(TAG5, "onRmsChanged");
		}

		/**
		 * Writes to LogCat, confirming that a buffer of information has been
		 * received.
		 * 
		 */
		public void onBufferReceived(byte[] buffer) 
		{
			Log.d(TAG5, "onBufferReceived");
		}

		/**
		 * Writes to LogCat, confirming that speech has ended.
		 * 
		 */
		public void onEndOfSpeech() 
		{
			Log.d(TAG5, "onEndofSpeech");
		}

		/**
		 * The onError method resets the Voice Activity UI, in the event of a anomaly.
		 * 
		 */
		public void onError(int error) 
		{
			Log.d(TAG5, "error " + error);
			//mText.setText("error " + error);
			
			// Tell the user that they must try again...
			//
			mText.setText("Please try again...");
			
			// Reset the appearance of button, text field, and animation
			// frame...
			//
			speakButton.setTextColor(Color.parseColor("#335495"));
			speakButton.setText("Press here to begin...");
			nText.setBackgroundColor(Color.parseColor("#335495"));
			imgFrame0.setBackgroundColor(Color.parseColor("#335495"));
			
			// Stop the animation, and show the corresponding still image.
			//
			imgFrame.post(stopAnimation);
			imgFrame.setBackgroundResource(R.drawable.nowave);
		}

		/**
		 * Receives a text string, based on
		 * a user-voiced query. It strives to match the text string with ones locally
		 * maintained, in correspondence with the contents of the supportive xml file. If
		 * it finds a match, it produces a spoken answer to the user. If it does not,
		 * it produces a spoken message of regret. In each phase, it changes the appearance
		 * of certain UI elements, so providing helpful feedback to the user.
		 * 
		 */
		public void onResults(Bundle results) 
		{
			// Change the appearance of the button, in preparation for giving
			// the answer.
			//
			speakButton.setTextColor(Color.parseColor("#000000"));
			speakButton.setText("Your answer is...");
			
			// Stop the animation, and restore the still image to the image frame.
			//
			imgFrame.post(stopAnimation);
			imgFrame.setBackgroundResource(R.drawable.nowave);
			
			// Reset the text view border-colour.
			//
			nText.setBackgroundColor(Color.parseColor("#000000"));
			
			// Establish a string object for holding the result-data.
			//
			String str = new String();
			
			Log.d(TAG6, "onResults " + results);
			
			// Grab the data that was obtained by the speech recognizer, as a result
			// of the user's voiced query.
			//
			ArrayList<?> data = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			
			// Add the data in the array list to the string object.
			// NOTE: PROBABLY JUST DELETE THIS. FIX.
			//
			for (int i = 0; i < data.size(); i++) 
			{
				Log.d(TAG6, "result " + data.get(i));
				str += data.get(i);
			}
			
			// Now set the text displayed in the text view to correspond to the
			// string recognized by the system as that voiced by the user.
			//
			mText.setText("" + data.get(0));
			
			// Save that data as a string object.
			//
			String text = (String) data.get(0);
			
			// Make a duplicate, since the value of text will be changed to the answer. NOTE: NAME
			// THESE VARIABLES MORE EXPLICITLY. FIX.
			//
			String repeatText = text;
			
			System.out.print("Got string as: " + text + '\n');
			
			// Integer to help us index our attempt to match the string that
			// we've retrieved from the user-input.
			//
			int queryIndex = 0;
			
			Log.w(TAG6, "Starting matching now:\n");
			
			// Go through the list of questions we gathered from the xml file.
			//
			while(queryIndex <= currentTermDefinitionInfo.questions.length - 1)
			{
				Log.w(TAG6, "String " + queryIndex + " is: " + currentTermDefinitionInfo.questions[queryIndex]);
				
				// If we find a match to a given question...
				//
				if (text.equalsIgnoreCase(currentTermDefinitionInfo.questions[queryIndex]))
				{
					// Let the value of "text" become that of the corresponding answer-string.
					//
					text = currentTermDefinitionInfo.answers[queryIndex];
					
					// Based on the value we've found, establish a spinner, which will thus display
					// references associated with this question-answer combo.
					//
					setUpSpinner(queryIndex);
				}
				else
				{
					// Otherwise, if there's no match, continue to search for one.
					//
					queryIndex++;
				}
			}
			
			// If, in the end, we don't have an answer, rather than repeating the question (which is
			// what the default Google interface provides for, specifically tell the user we don't
			// have the answer.
			//
			// So, if we have finished iterating, and the value of text still equals that of repeatText,
			// indicating that no match was found...
			//
			if (text.equalsIgnoreCase(repeatText))
			{
				text = "Sorry, I don't have an answer.";
			}

			// Now speak to the user, using the value of "text", whatever it has turned out
			// to be.
			//
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
			
			// Reset the appearance of the speak button and the border of the text-display
			// field.
			//
			speakButton.setTextColor(Color.parseColor("#335495"));
			speakButton.setText("Press here to begin...");
			nText.setBackgroundColor(Color.parseColor("#335495"));
			
			// Stop the animation, and re-establish the still image in the image frame.
			//
			imgFrame.post(stopAnimation);
			imgFrame.setBackgroundResource(R.drawable.nowave);
			imgFrame0 = (TextView) findViewById(R.id.imgFrame0);
			imgFrame0.setBackgroundColor(Color.parseColor("#335495"));
		}

		/**
		 * Writes to the LogCat, indicating that partial results were found.
		 * 
		 */
		public void onPartialResults(Bundle partialResults) 
		{
			Log.d(TAG6, "onPartialResults");
		}

		/**
		 * Writes to the LogCat, indicating the type of a received event.
		 * 
		 */
		public void onEvent(int eventType, Bundle params) 
		{
			Log.d(TAG6, "onEvent " + eventType);
		}
	}

	/**
	 * Starts the listener and associated processes
	 * that allow a user to present a voiced query.
	 * 
	 */
	public void onClick(View v) 
	{
		if (v.getId() == R.id.btn_speak) 
		{
			// Blank out the text display field.
			//
			mText.setText("");
			
			// Set up and start the animation that plays while the listener is active.
			//
			imgFrame.setBackgroundResource(R.layout.animation_frames);
			frameAnimation = (AnimationDrawable) imgFrame.getBackground();	
			imgFrame.post(run);
			
			// Change the color of the button, and change its displayed text.
			//
			speakButton.setTextColor(Color.parseColor("#00CC00"));
			speakButton.setText("Ask your question now...");
			
			// Likewise change the border colors of the text display field and
			// the image frame for the animation.
			//
			nText.setBackgroundColor(Color.parseColor("#00CC00"));
			imgFrame0.setBackgroundColor(Color.parseColor("#00CC00"));
			
			// Establish an intent for the listener.
			//
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
					"voice.recognition.test");

			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
			
			// Start listening, specifying the intent.
			sr.startListening(intent);
		}
	}

	/**
	 * Initializes the text to speech facility, whereby an answer
	 * to the user's query can be made audible.
	 * 
	 */
	@Override
	public void onInit(int arg0) 
	{	
		if (arg0 == TextToSpeech.SUCCESS) 
		{
			int result = tts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
								|| result == TextToSpeech.LANG_NOT_SUPPORTED) 
			{
				Log.w(TAG4, "Language is not supported");
			} 
			else 
			{
				// Do nothing.
			}

		} 
		else 
		{
			Log.w(TAG4, "Initilization Failed");
		}
	}
	
	/**
	 * Closes the activity and returns the user 
	 * to the Book Cover Activity for the User Guide. 
	 * 
	 * @param view
	 */
	public void returnToMenu(View view) 
	{	
		Log.w(TAG0, "GOING BACK TO TITLEPAGE NOW " + '\n');
		
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
	    		Log.w(TAG1, "After assignment, allChapters is non-null\n");
	    	}
    		else
    		{
		    		Log.w(TAG6, "After assignment, allChapters is null\n");
    		}
    	}
		
		// Set up the intent for returning to the book cover activity.
		//
		Intent intent = new Intent(VoiceActivity.this,  BookCoverActivity.class);
		intent.putExtra("intVar", MetaDataForDocument.currentDocument);
		
		// Start the book cover activity.
		//
		startActivity(intent);
		
		// Release the memory held by the images used by the Voice Activity.
		//
		Utils.unbindDrawables(findViewById(R.id.imgFrame));	
	}
	
	/*
	 * The termDefinitionInfo class contains four members, which respectively
	 * contain the questions, answers, references, and images available for
	 * use in the voice activity.
	 */
	public class termDefinitionInfo 
	{
		// Every term definition has one question and one answer. These string arrays
		// list them in order of term definitions.
		private String[] questions = null;
		private String[] answers = null;
		
		// Every term definition has multiple references, each with 6 members. It also has
		// multiple images, each with 3 members. The 3D string array thus provides 3 indices,
		// which are respectively for term definition, reference/image, and member.
		private String[][][] references = null;
		private String[][][] images = null;
	}
	
	/**
	 * Supports the spinner, which presents to the user options for
	 * accessing text passage in the User Guide, such as offer further support related
	 * to the processed audible query.
	 *
	 */
	public class MyAdapter extends ArrayAdapter<Object>
    {
		/**
    	 * The titles of the rows in the spinner.
    	 */
    	String[] titles = null;
    	
    	/**
    	 * The images employed by the row in the spinner.
    	 */
    	int[] images = null;
    	
    	/**
    	 * A constructor, which establishes the title strings and images to be used in the
    	 * rows of the spinner.
    	 * 
    	 * @param context The current context for the spinner.
    	 * 
    	 * @param textViewResourceId The text view to be employed for the spinner.
    	 * 
    	 * @param objects The strings to be used in the rows of the spinner.
    	 * 
    	 * @param arr_images The images to be used in the rows of the spinner.
    	 * 
    	 */
        public MyAdapter(Context context, int textViewResourceId, String[] objects, int[] arr_images)
        {		
            super(context, textViewResourceId, objects);
            
            titles = objects;
        	images = arr_images;
        }
        
        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) 
        {
            return getCustomView(position, convertView, parent);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            return getCustomView(position, convertView, parent);
        }
        
        public View getCustomView(int position, View convertView, ViewGroup parent) 
        {
            LayoutInflater inflater=getLayoutInflater(); 
            View row = null;
            
            // Inflate in accordance with the layout specification for the current underlying platform.
            //
            switch (MetaDataForSuite.currentAspectRatio)
 			{
 				// This is a 480x800 4.3" device.
 				//
 				case 0:
 					row = inflater.inflate(R.layout.spinner_row_for_zero___four_point_three, null, false);
 					Log.w(TAG0, "Inflated spinner row definition for 0.\n");
 					break;	
 					
 				// This is a 540x960 4.0" to 4.8" device.
 				//
 				case 10:
 					row = inflater.inflate(R.layout.spinner_row_for_one_zero___four_point_zero_etc, null, false);
 					Log.w(TAG0, "Inflated spinner row definition for 10.\n");
 					break;
 					
 				// This is a 720x1280 4.3" to 5.0" device.
 				//
 				case 20:
 					row = inflater.inflate(R.layout.spinner_row_for_two_zero___four_point_three_etc, null, false);
 					Log.w(TAG0, "Inflated spinner row definition for 20.\n");
 					break;
 					
 				// This is a 720x1280 4.3" to 5.0" device.
 				//
 				case 30:
 					row = inflater.inflate(R.layout.spinner_row_for_three_zero___four_point_seven, null, false);
 					Log.w(TAG0, "Inflated spinner row definition for 30.\n");
 					break;
 					
				// This is a 800x1280 7" device.
			    //
				case 40:
					row = inflater.inflate(R.layout.spinner_row_for_four_zero___seven, null, false);
					Log.w(TAG0, "Inflated spinner row definition for 40.\n");
					break;
					
				// This is a 800x1280 10.1" device.
			    //
				case 50:
					row = inflater.inflate(R.layout.spinner_row_for_five_zero___ten, null, false);
					Log.w(TAG0, "Inflated spinner row definition for 50.\n");
					break;
    		
 			    // This is a 1080x1920 generic-size device.
 				//
 				case 60:
 					row = inflater.inflate(R.layout.spinner_row_for_six_zero___gen, parent, false);
 					Log.w(TAG0, "Inflated spinner row definition for 60.\n");
 					break;
 				
 				// This is a 1080x1920 5.7 inch device.
 				//
 				case 70:
 					row = inflater.inflate(R.layout.spinner_row_for_seven_zero___five_point_seven, parent, false);
 					Log.w(TAG0, "Inflated spinner row definition for 70.\n");
 					break;
 				
 				// This is a 1200x1920 7 inch device.
 				//
 				case 80:
 					row = inflater.inflate(R.layout.spinner_row_for_document_for_eight_zero___seven, null, false);
 					Log.w(TAG0, "Inflated spinner row definition for 80.\n");
 					break;
 				
 				// This is a 1200x1920 10 inch device.
 				//
 				case 90:
 					row = inflater.inflate(R.layout.spinner_row_for_nine_zero___ten, null, false);
 					Log.w(TAG0, "Inflated spinner row definition for 90.\n");
 					break;
 					
 				// This is a 1440x900 7 inch device.
 				//
 				case 100:
 					row = inflater.inflate(R.layout.spinner_row_for_one_zero_zero___seven, null, false);
 					Log.w(TAG0, "Inflated spinner row definition for 100.\n");
 					break;

				// This is a 1440x2560 5.1 inch device.
				//
				case 110:
					row = inflater.inflate(R.layout.spinner_row_for_one_one_zero___five_point_one, null, false);
					Log.w(TAG0, "Inflated spinner row definition for 110.\n");
					break;
 				
 				// If we are not sure, we use the following default.
 				//
 				default:
 					row = inflater.inflate(R.layout.spinner_row_for_six_zero___gen, null, false);
 					Log.w(TAG0, "Inflated spinner row definition for default.\n");
 					break;			
 			}
            
            TextView label=(TextView)row.findViewById(R.id.text_reference);
            label.setText(titles[position]);

            ImageView icon=(ImageView)row.findViewById(R.id.image);
            icon.setImageResource(images[position]);
            return row; 
        }
    }
	
	/**
	 * Sets up the spinner within the Voice Activity.
	 * 
	 * @param indexIntoArray 
	 * 			An integer that specifies an index into an array, so that text and images can be located.
	 * 
	 */
	public void setUpSpinner(int indexIntoArray)
	{
		Log.w(TAG7, "Setting up spinner now.");
		
		// Now that a question has been asked, we can match it against the corresponding
		// array and accordingly set up the text and images for the spinner.
		//
		int[] arr_images = new int[currentTermDefinitionInfo.questions.length + 1];
		arr_images[0] = R.drawable.spinnerblank;
		
		// A dynamically expansible array list of 2D strings, which will hold the
		// references for the question that has been asked.
		//
		ArrayList<String[]> refsForQ = new ArrayList<String[]>();
		
		// Find all the non-null references for the selected question, and add each of
		// these to the ArrayList.
		//
		for (int x = 0; x < 15; x++)
		{
			if (currentTermDefinitionInfo.references[indexIntoArray + 1][x][0] != null)
			{
				refsForQ.add(currentTermDefinitionInfo.references[indexIntoArray + 1][x]);
			}
		}
		
		Log.w(TAG7, "Resulting size of refsForQ is " + refsForQ.size());
		
		// The spinner array for the references should be the length of the number of references,
		// plus one more space, for the label in the first position. Meanwhile, the arrays
		// for preparation of the intent should just be the same length as the arraylist, since
		// they require no additional element in position zero.
		//
		seeAlsoTitles = new String[refsForQ.size()];
		seeAlsoTitlesRevised = new String[refsForQ.size() + 1];
		seeAlsoLocations = new String[refsForQ.size()];
		seeAlsoChapNos = new String[refsForQ.size()];
		
		// The first item in the spinner is always a generic instruction with a blank
		// image in accompaniment.
		//
		String label = "For additional, textual information, see here:";
		seeAlsoTitlesRevised[0] = label;
			
		// Now iterate over the array list, pulling out strings and placing them
		// in static arrays.
		//
		for (int j = 0; j < refsForQ.size(); j++)
		{
			Log.w(TAG7, "Iteration " + j + ": " + "refsForQ.size() is " + refsForQ.size());
			String[] refItems = new String[6];
			refItems = refsForQ.get(j);
			Log.w(TAG7, "Loaded up refItems from refsForQ's item " + j);
			
			// Populate individual arrays with the appropriate contents for this particular
			// row of the spinner.
			//
			seeAlsoTitles[j] = refItems[4];
			seeAlsoTitlesRevised[j + 1] = refItems[4];
			seeAlsoLocations[j] = refItems[5];
			seeAlsoChapNos[j] = refItems[2];
			
			Log.w(TAG7, "Loaded up seeAlsoTitlesRevised's " + (j + 1) + " with refItems[4]: " + refItems[4]);
			Log.w(TAG7, "Loaded up seeAlsoLocations' " + (j + 1) + " with refItems[5]: " + refItems[5]);
			arr_images[j + 1] = R.drawable.arrow_red_xxsmall_right;
		}
		
		Log.w(TAG7, "See also titles revised is of length: ..." + seeAlsoTitlesRevised.length);
		
		// Establish the spinner, with which you will reference text documents
		// that support the current animation with additional information.
		//
		Spinner mySpinner = (Spinner)findViewById(R.id.theSpinner);
        mySpinner.setAdapter(new MyAdapter(VoiceActivity.this, R.layout.spinner_row_for_zero___four_point_three, seeAlsoTitlesRevised, arr_images));

        // Establish the listener for the spinner.
        //
        mySpinner.setOnItemSelectedListener(new OnItemSelectedListener() 
        {
        	@Override
        	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
			{
        		String selected = parentView.getItemAtPosition(position).toString();

				Log.w(TAG7, "Click: Current selection from spinner is " + selected + '\n');	
				
				if (selected.equalsIgnoreCase("For additional, textual information, see here:"))
				{
					// Do Nothing...
				}
				else
				{
					String current = ""; 
					
					for (int i = 0; i <= seeAlsoTitles.length - 1; i++)
					{
						current = seeAlsoTitles[i];

						if (selected.equalsIgnoreCase(current))
						{
							// Go to the chapter and section so referenced...
							//
							Intent intent = new Intent(VoiceActivity.this, ChapterActivity.class);
							
							// Establish the chapter to which we are going as one less than the
							// stated value, since we'll count from 0.
							//
					    	intent.putExtra("intVar", Integer.parseInt(seeAlsoChapNos[i]) - 1 );
					    	intent.putExtra("urlOfChapter", seeAlsoLocations[i]); 
					    	
							// Also specify that our departure and our target are voice and chapter-activities
							// respectively: 7 and 3. Then specify the corresponding
							// transition profile; which for chapter to chapter, is 700.
							//
							intent.putExtra("placeOfDeparture", 7);
					    	intent.putExtra("targetChapterActivityType", 3);
					    	intent.putExtra("transitionProfile", 700);
					    	
					    	Log.w(TAG7, "Starting intent with chap number of " + seeAlsoChapNos[i]);
					    	Log.w(TAG7, "Starting intent with chap numeric value of " + Integer.parseInt(seeAlsoChapNos[i]));
					    	Log.w(TAG7, "Starting intent with url of " + seeAlsoLocations[i]);
					    	
					    	// Make sure we have the right data for the User Guide, which is where we are
					    	// going. Note that we can't otherwise be sure to get it right, since we are
					    	// not traversing the UG book cover, and another book cover might have recently
					    	// reset this data.
					    	//
					    	// Following an appropriate re-architecture, this can be handled differently.
					    	
					    	
					    	Log.w(TAG7, "whetherWidelyEstablished is now " + MetaDataForDocument.whetherWidelyEstablished);
					    	
					    	if(MetaDataForDocument.allChapters != null)
					    	{
					    		Log.w(TAG7, "Before reassignment, allChapters is non-null\n");
					    	}
					    	
					    	if (MetaDataForDocument.userGuideChapters != null)
					    	{
					    		Log.w(TAG7, "Before assignment, userGuideChapters is non-null\n");
					    	}
					    	else
					    	{
					    		Log.w(TAG7, "Before assignment, userGuideChapters is null\n");
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
						    		Log.w(TAG7, "After assignment, allChapters is non-null\n");
						    	}
					    		else
					    		{
							    		Log.w(TAG7, "After assignment, allChapters is null\n");
					    		}
					    	}
					    	
					    	// Start the Chapter Activity with the provided reference.
					    	//
					    	startActivity(intent);

			    			finish(); 
						}
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) 
			{
				// Do nothing.
			} 
        });
	}
}