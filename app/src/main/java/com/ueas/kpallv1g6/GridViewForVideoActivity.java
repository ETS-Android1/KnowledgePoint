package com.ueas.kpallv1g6;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;  
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
 
/**
 * Displays a grid view containing all videos available for user-viewing. When a grid-element
 * is selected, the Video activity is started, and the corresponding video is played.
 *  
 * @author tony.hillman
 *
 */
public class GridViewForVideoActivity extends Activity 
{    
	
	// Strings to house per-animation data. The URL string points at
	// the location of the xml file that holds all the basic
	// information on the song list. This will be used to create
	// the DOM document from which all information is then derived.
	// 
	
	/**
	 * A string used to identify the node-name for the principal sections
	 * in the xml file used by the Activity for information on available
	 * animations.
	 * 
	 */
	static final String KEY_VIDEO = "video";  
	
	/**
	 * A string used to refer to the numeric identifer of a principal section
	 * (and thereby, an animation-title) within the xml file containing
	 * the list of available animations. Within the file, identifiers are
	 * unique, and are small integers, arranged in monotonically increasing
	 * sequence.
	 * 
	 */
	static final String KEY_ID = "id";
	
	/**
	 * A string used to refer to the title of an animation referenced within
	 * the target xml file. The title will be displayed in the List View.
	 * 
	 */
	static final String KEY_TITLE = "title"; 
	
	/**
	 * A string used to refer to the description of an animation referenced
	 * within the target xml file. The description will be displayed in
	 * the List View.
	 * 
	 */
	static final String KEY_DESCRIPTION = "description";
	
	/**
	 * A string used to refer to the duration of an animation referenced
	 * within the target xml file. The duration will be displayed in
	 * the List View.
	 * 
	 */
	static final String KEY_DURATION = "duration";
	
	/**
	 * A string used to refer to the pathname of the source of a given animation,
	 * referenced within the target xml file. 
	 * 
	 */
	static final String KEY_SOURCE = "source"; 
	
	/**
	 * A string used to refer to the source of the thumbnail image for a given animation, 
	 * referenced within the target xml file. The thumbnail will be displayed in the List 
	 * View.
	 * 
	 */
	static final String KEY_THUMB_URL = "thumb_url";
	
	/**
	 * A string used to refer to the location of the subsidiary xml file that
	 * contains information for the spinner, which allows users to visit text
	 * passages in the User Guide directly from the animation activity.
	 * 
	 */
	static final String KEY_SEE_ALSO_LIST = "see_also_list"; 
	
	/**
	 * A dynamically extensible array containing as many hash maps as are needed
	 * to store information on available videos, provided by the target xml
	 * file. Each map will contain string pairings, which are the key and value
	 * for each attribute of a given document (ie, "id" of "0", "title" of "What
	 * is UltraAPEX", etc).
	 * 
	 */
	private ArrayList<HashMap<String, String>> videoList = new ArrayList<HashMap<String, String>>();
	
	/**
	 * A string used for LogCat output from the onCreate method.
	 * 
	 */
	private final static String TAG0 = "CCLVFP: onCreate";
	
	/**
	 * A string used for LogCat output from the doInBackground method.
	 * 
	 */
	private final static String TAG1 = "CCLVFP: doInBackground";
	
	/**
	 * A string used for LogCat output from the onPostExecute method.
	 * 
	 */
	private final static String TAG2 = "CCLVFP: onPostExecute";
	
	/**
	 * Integer representing the number of content nodes in the target xml file.
	 * 
	 */
	private int numberOfContentNodes = 0;
	
	/**
	 * A Progress Dialog, to be shown to users while background operations occur.
	 * 
	 */
	public ProgressDialog progress2 = null;
	
	/**
	 * A hash map, used in the preparation of animation details for display
	 * in the List View.
	 */
	private HashMap<String, String> map; 

	/**
	 * A string used in retrieval of the string contained in the intent that
	 * calls the current activity, and indicates the location of the xml file
	 * containing animation-information.
	 * 
	 */
	public static String theString = "";
	
	/**
	 * Obtains the URL for the animation information, parses this as a DOM document
	 * object, then uses a LazyAdapterForPlayer object to prepare and display a
	 * corresponding ListView. When a selection is made by the user, the unique
	 * ID for the selection is retrieved and passed to the Player Activity,
	 * which duly starts the animation.   
	 * 
	 * @see PlayerActivity
	 * 
	 * @see LazyAdapterForPlayer
	 * 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    { 
        super.onCreate(savedInstanceState);
        
        // Set the content view for the grid. Note that this is a generic layout, and therefore
        // works for all aspect ratios and screen sizes.
        //
        setContentView(R.layout.grid_layout); 
        
        Log.w(TAG0, "Starting create now...\n"); 
		super.onCreate(savedInstanceState);
		
		// Get the string that specifies the location of the xml file on which the listView
		// will be based.
		//
		Intent intent = getIntent(); 
		theString = intent.getStringExtra("here");
		Log.w(TAG0, "Got string as " + theString);
		
		// Establish a progress dialog, which will be shown while the background
		// operation is taking place.
		//
		progress2 = new ProgressDialog(this, R.style.CustomDialog);
		progress2.setTitle("UltraAPEX Knowledge Point");
      	progress2.setMessage("Accessing video content...");
		
		// The netOp operation is the network activity we do in the background,
		// in order to get the xml file with the data for the animations, which will
		// be used to create the ListView object.
      	//
		netOp2 myOp2 = new netOp2(progress2); 

		myOp2.execute(theString);
		
		// Show the progress dialog while the background operation is taking place.
		//
		progress2.show();    
    }
    
    /**
	 * Handles the network-retrieval
	 * of the xml file that contains information on the list of videos to be
	 * displayed in the list view. This operation is performed in the background, with the Document
	 * object derived from the xml file then handled by the onPostExecute method.
	 * 
	 * @author tony.hillman@ultra-as.com
	 *
	 */
	public class netOp2 extends AsyncTask<String, Void, Document>
	{		
		/**
		 * A progress notification object, to be shown to the user while retrieval of
		 * the xml file is taking place.
		 * 
		 */
		private ProgressDialog progress2;
		
		/**
		 * A constructor that establishes the progress dialog created in the onCreate of
		 * CustomizedContentListViewForPlayer as the dialog to be shown during retrieval of the
		 * xml file.
		 * 
		 * @param progress The progress dialog to be shown the user.
		 * 
		 */
		public netOp2(ProgressDialog p)
		{
			this.progress2 = p;
		}
		
		/**
		 * Performs retrieval of the xml file required by the ListView to be displayed.
		 * 
		 * @param params A url or file pathname that is the location of the xml file.
		 * 
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
			
			/**
			 * An xml parser used to derive information from an xml file's pathname.
			 * See XMLParser.java for information.
			 * 
			 */
			XMLParser parser = new XMLParser();
			
			/**
			 * A string used to maintain the location of the xml file containing information
			 * on the animations
			 * 
			 */
			String xml = "";
			
			// If Connectivity is 0, then we are going over the network, and treat the string
			// as a URL.
			//
			if (MetaDataForSuite.Connectivity == 0)
			{
				xml = parser.getXmlFromUrl(params[0]); 
				
				if (xml == null)
				{
					Log.w(TAG1, "Looks like the xml file came as null...");
				}
				else
				{
					Log.w(TAG1, "Xml file is..... " + xml); 
				}
			} 
			
			// Otherwise, we are dealing with a file pathname.
			//
			else 
			{
				Log.w(TAG1, "Seeking file from disk now." + '\n');
				try 
				{
					xml = MyFileReader.readFile(params[0]);
					
					Log.w(TAG1, "Got file as: " + xml);	
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			
			// Now instantiate the doc object with an appropriate parsing of the xml file.
			//
			doc = parser.getDomElement(xml); 
			
			// Now that we have a doc object, save it globally, for further
			// use, so that we can avoid going back to the network
			// more than once.
			//
			MetaDataForVideo.all_videos_doc_object = doc;
		
			// Return the doc object that contains the full list of animations, so that it can be
			// employed in post-execution.
			//
			return doc;
		}
		
		/**
		 * Performs the majority of work required in the ListViewForPlayer class. Once a Document
		 * has been retrieved, containing information on all documents in the suite, onPostExecute
		 * accepts this as an argument, retrieves the information within, and uses it to
		 * display the List View: and, when the user makes a selection, performs an
		 * appropriate routine.
		 * 
		 * @param Document A Document object containing information on available animations.
		 * 
		 */
		protected void onPostExecute(Document thedoc)
		{
			// Get rid of the "Preparing..." notification.
			//
			progress2.dismiss();
			    
			Log.w(TAG2, "doInBackground completed.");

			// Set up a new parser. See XMLParser.java for details.
			//
			XMLParser parser = new XMLParser();
			
			// Grab the document object passed to the current method as an argument by
			// doInBackground.
			//
			final Document doc = thedoc;
		
			// Get all of the song nodes from the DOM document, and put them
			// into a node list structure.
			//
			Log.w(TAG2, "Getting nodelist now.");
			NodeList nl = doc.getElementsByTagName(KEY_VIDEO);
			numberOfContentNodes = nl.getLength();
			Log.w(TAG2, "number of content nodes is " + numberOfContentNodes);
			
			// Save the number of content nodes within the BookMetaDataRegistry.
			//
			MetaDataForVideo.nodesInCurrentXMLsourcefile = numberOfContentNodes;
		
			// Loop through the node list. Each time you loop, create
			// a hash table, which associates the node name with the node
			// value: for example the value of "title" might be "What is
			// UltraPortal", while the value of "duration" would be "01:25". These
			// names and values go as pairs into the hash table. 
			//
			for (int i = 0; i < nl.getLength(); i++) 
			{
				map = new HashMap<String, String>();
				Element e = (Element) nl.item(i);
				
				// Add each child node and its value to the hash table.
				//
				map.put(KEY_ID, parser.getValue(e, KEY_ID));
				map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
				map.put(KEY_DESCRIPTION, parser.getValue(e, KEY_DESCRIPTION));
				map.put(KEY_DURATION, parser.getValue(e, KEY_DURATION));
				map.put(KEY_SOURCE, parser.getValue(e, KEY_SOURCE));
				map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));
				map.put(KEY_SEE_ALSO_LIST, parser.getValue(e, KEY_SEE_ALSO_LIST));

				// Every time you loop, add your hash table of names and
				// values for this particular animation to the array list you
				// created.
				//
				videoList.add(map);
			}
			
			MetaDataForVideo.allVideosArrayList = videoList; 
			
			Log.w(TAG0, "meta list now of size " + MetaDataForVideo.allVideosArrayList.size());
		
			// The list view is associated with the "list" element in the
			// listview.xml file. 
			//
			GridView grid=(GridView)findViewById(R.id.grid_view);
		
			// Create a Lazy Content Adapter. This associates the values in
			// each hash map contained with a particular graphical region of the list 
			// view UI.
			//
	        AdapterForVideo adapter = 
	        		new AdapterForVideo(GridViewForVideoActivity.this, videoList); 
	        
	        // Display the List View.
	        //
	        grid.setAdapter(adapter);
	        
	        // Now determine what happens when a user clicks on an individual
	        // row in the list view UI. 
	        //
	        grid.setOnItemClickListener(new OnItemClickListener() 
	        {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
				{ 	
					// Grab the hidden id value for the row that was clicked on.
					//
					TextView tv=(TextView)view.findViewById(R.id.hidden_id);
					int idInt = Integer.parseInt(tv.getText().toString());
					
					// If the id signifies the last item in the menu, it requires we exit the
					// application.
					//
					if (idInt == (videoList.size() - 1))
					{
						new AlertDialog.Builder(GridViewForVideoActivity.this, R.style.CustomDialog)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("Exit Application")
						.setMessage("Are you sure you want to exit?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								Intent intent = new Intent(GridViewForVideoActivity.this, LoginActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.putExtra("exit", "true");
								startActivity(intent);
							}
						})
						.setNegativeButton("No", null)
						.show();
					}
					else
					{
						// If this is the penultimate item, we must go back to the
						// welcome page for the overall doc suite.
						//
						if (idInt == (videoList.size() - 2))
						{
						
							Intent intent = new Intent(GridViewForVideoActivity.this, 
														WelcomePageActivity.class);
						
							startActivity(intent);
						}
						else
						{
							// If this is the last item but 2, we must go back to the titlepage for
							// the user guide.
							//
							if (idInt == (videoList.size() - 3))
							{
								Intent intent = new Intent(GridViewForVideoActivity.this, 
										BookCoverActivity.class);
								
								// Go back to the cover page for the current book, making sure
								// that the current book is indeed the User Guide.
								//
								MetaDataForDocument.currentDocument = 0;
								intent.putExtra("intVar", MetaDataForDocument.currentDocument);
								
								startActivity(intent);
							}
							else
							{
								// A video has been selected. So, create a new hash mapping in which 
								// to contain the information for the selected song.
								//
								HashMap<String, String> newMap = new HashMap<String, String>();

								// Make the new hash mapping equal to the hash mapping element in the
								// videoList that contains the information for the selected song.
								//
								newMap = videoList.get(idInt);
								
								// Pull the strings out of the hash mapping, and save them in a string array,
								// ready to be sent to the video activity.
								//
								String selectedVideo[] = getSongInfo(newMap);
								
								// Define the Video Activity, specifying the number of the 
								// selected animation.
								//
								Intent intent = new Intent(GridViewForVideoActivity.this, VideoActivity.class);
								
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
						}
					}
				}			
	        }); 			    
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
	
	/**
	 * Restores the ListView  to its previous state, once the user has returned from another 
	 * activity. It also performs an explicit garbage collection.
	 * 
	 */
	public void onResume()
	{
	    super.onResume();   	

	    System.gc();
	}
	
	/**
	 * Performs an explicit garbage collection.
	 * 
	 */
	@Override
	public void onDestroy() 
	{
		super.onDestroy();

		System.gc();
	}
}
