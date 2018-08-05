package com.ueas.kpallv1g6;

// Significant input provided by Ravi Tamada.
//

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import android.util.Log;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Presents a List View that the user employs to select 
 * from a range of documents. Selection duly takes the user to
 * the cover page of the specified document.
 * 
 * 
 * @author tony.hillman@ultra-as.com
 * 
 */
public class ListViewForSuiteActivity extends Activity 
{
	// Strings to house per-document data. The URL string points at
	// the location of the xml file that holds all the basic
	// information on the song list. This will be used to create
	// the DOM document from which all information is then derived.
	//
	/**
	 * A string used to identify the node-name for the principal sections
	 * in the xml file used by the Activity for information on available
	 * documents.
	 * 
	 */
	static final String KEY_SECTION = "document"; 
	
	/**
	 * A string used to refer to the numeric identifer of a principal section
	 * (and thereby, a document-title) within the xml file containing
	 * the list of available documents. Within the file, identifiers are
	 * unique, and are small integers, arranged in monotonically increasing
	 * sequence.
	 * 
	 */
	static final String KEY_ID = "id";
	
	/**
	 * A string used to refer to the title of a document referenced within
	 * the target xml file. The title will be displayed in the List View.
	 * 
	 */
	static final String KEY_DOC_TITLE = "doc_title";
	
	/**
	 * A string used to refer to the description of a document referenced
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
	 * A string used to refer to the source pathname of a given document, referenced
	 * within the target xml file. If this row of the List View is selected by the
	 * user, this pathname will be passed to the Chapter Activity, so that the chapter
	 * can be viewed.
	 * 
	 */
	static final String KEY_SRC_DOC = "src_doc";
	
	/**
	 * A string used to refer to the source pathname of the book cover image of a given document. If this row of 
	 * the List View is selected, this pathname will be passed to the Chapter Activity.
	 * 
	 */
	static final String KEY_BOOKCOVER = "bookcover";
	
	/**
	 * An integer representing the number of items in each node of the target xml document.
	 * 
	 */
	// NOTE: THIS NEEDS TO BE DERIVED SOME OTHER WAY.
	//
	static final private int NUMBER_OF_ITEMS_IN_NODE = 6;
	
	/**
	 * An integer representing the number of nodes in the target xml document.
	 * 
	 */
	// This will be established dynamically, below.
	//
	private int numberOfContentNodes = 0;
	
	/**
	 * An activity that is set to the current activity, for purposes of List View
	 * instantiation.
	 * 
	 */
	// We pass the current activity to the LazyContentAdapter, so that the ListView
	// can be prepared. But we do so in a PostExecution method, after
	// all the network stuff is completed. So, we can't just use the "this" keyword,
	// and must pass an activity variable.
	//
	public Activity activity = this;
	
	/**
	 * A string used for LogCat output from the onCreate method.
	 * 
	 */
	private final static String TAG0 = "CcLV:onCreate";
	
	/**
	 * A string used for LogCat output from the doInBackground method.
	 * 
	 */
	private final static String TAG1 = "CcLV:doInBackground";
	
	/**
	 * A string used for LogCat output from the onPostExecute method.
	 * 
	 */
	private final static String TAG2 = "CcLV:onPostExecute";
	
	/**
	 * A string used for LogCat output from the setOnItemClickListener method.
	 * 
	 */
	private final static String TAG4 = "CcLV:setOnItemClickListener";
	
	/**
	 * A hash map used in the preparation of information for display in the
	 * List View.
	 * 
	 */
	private HashMap<String, String> map;
	
	/**
	 * A dynamically extensible array containing as many hash maps as are needed
	 * to store information on available documents, provided by the target xml
	 * file. Each map will contain string pairings, which are the key and value
	 * for each attribute of a given document (ie, "id" of "0", "title" of "User Guide",
	 * etc).
	 * 
	 */
	private ArrayList<HashMap<String, String>> documentList = new ArrayList<HashMap<String, String>>();
	
	/**
	 * Obtains the URL for thexml file that contains all information on the documents in the suite, 
	 * parses this as a DOM document object, then uses a LazyAdapter object to prepare and display a
	 * corresponding ListView. When a selection is made by the user, the unique
	 * ID for the selection is retrieved and passed to the Book Cover Activity.	
	 * 
	 * @see BookCoverActivity
	 * 
	 * @see LazyAdapter
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// Establish the appearance of the current activity. Note that this is a generic layout-description,
		// and so applies to all platforms.
		//
		setContentView(R.layout.list_view_for_suite);
	
		this.setTitle("Please select a document..."); 
		
		// Grab the intent used to start the current Activity. It contains a string extra, which
		// is the source pathname of the xml file that contains information for the List View.
		// obtain this string, and use it as the argument to the background operation that will
		// do the retrieving of the xml file.
		//
		Intent intent = getIntent();
		String theString = intent.getStringExtra("here");
		Log.w(TAG0, "Got string as " + theString); 
		
		// Establish a progress dialog, which will be shown while the background
		// operation is taking place.
		//
		ProgressDialog progress = new ProgressDialog(this, R.style.CustomDialog);
		progress.setTitle("UltraAPEX Knowledge Point");
      	progress.setMessage("Accessing application contents...");
		
		// The netOp operation is the network activity we do in the background,
		// in order to get the xml file with the data for the chapter, which will
		// be used to create the ListView object.
      	//
		netOp myOp = new netOp(progress); 
		myOp.execute(theString);
		
		// Show the progress dialog while the background operation is taking place.
		//
		progress.show(); 
	}
	
	/**
	 * Handles the network-retrieval of the xml file that contains information on the list 
	 * of documents to be displayed in the list view. This is performed in the background.
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
		 * CustomizedContentListView as the dialog to be shown during retrieval of the
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
			
			// If we have been through this routine before, then the doc
			// object we need to derive from the XML file already exists.
			// So, we can skip the file-access and transformation routine.
			// Otherwise, we do indeed go out to the network.
			//
			if (MetaDataForSuite.all_docs_doc_object == null)
			{
				// Prepare an xml parser and a string in which to contain the xml file's pathname.
				//
				XMLParser parser = new XMLParser();
				String xml = "";
				
				// If Connectivity is 0, then we are going over the network, and treat the string
				// as a URL.
				//
				if (MetaDataForSuite.Connectivity == 0)
				{
					xml = parser.getXmlFromUrl(params[0]); 
					
					if (xml == null)
					{
						Log.w(TAG1, "Looks like the string is null...");
					}
					else
					{
						Log.w(TAG1, "String is..... " + xml); 
					}
				} 
				
				// Otherwise, we are dealing with a file pathname.
				//
				else 
				{
					Log.w(TAG1, "Seeking file now." + '\n');
					
					try 
					{
						xml = MyFileReader.readFile(params[0]);
					} 
					catch (IOException e) 
					{
						
						Log.w(TAG1, "Could not access xml file at stated location of " + params[0]);
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
				MetaDataForSuite.all_docs_doc_object = doc;
			}
			
			// If at some earlier stage we already instantiated the all_docs_doc_object, simply
			// re-use it, and so avoid going back out over the network again.
			//
			else
			{
				doc = MetaDataForSuite.all_docs_doc_object;
			}
			
			// Return the doc object that contains the full list of documents, so that it can be
			// employed in post-execution.
			//
			return doc;
		}
		
		/**
		 * Performs the majority of workrequired in the ListViewForSuite class. Once a Document has
		 * been retrieved, containing information on all documents in the suite, onPostExecute
		 * accepts this as an argument, retrieves the information within, and uses it to
		 * display the List View: and, when the user makes a selection, performs an
		 * appropriate routine.
		 * 
		 * @param Document A Document object containing information on available documents.
		 * 
		 */
		protected void onPostExecute(Document thedoc)
		{
			// Get rid of the "Preparing..." notification.
			//
		    progress.dismiss();
		    
			Log.w(TAG0, "doInBackground completed.");
			
			// It may be that as well as an existing doc object, we also have
			// the entire array list that we derive from the doc object. In which
			// can, we can by-pass the arrayList creation routine, and save some
			// time. Otherwise, we do the creation:
			//
			if (MetaDataForSuite.allDocumentsArrayList == null)
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
			
				// Loop through the node list. Each time you loop, create
				// a hash table, which associates the node name with the node
				// value: for example the value of "title" might be "User Guide", 
				// while the value of "id" might be "3". These
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
					map.put(KEY_DOC_TITLE, parser.getValue(e, KEY_DOC_TITLE));
					map.put(KEY_DESCRIPTION, parser.getValue(e, KEY_DESCRIPTION));
					map.put(KEY_THUMBNAIL, parser.getValue(e, KEY_THUMBNAIL));
					map.put(KEY_SRC_DOC, parser.getValue(e, KEY_SRC_DOC));
					map.put(KEY_BOOKCOVER, parser.getValue(e, KEY_BOOKCOVER));

					// Every time you loop, add your hash table of names and
					// values for this particular document to the hash table array list you
					// created.
					//
					documentList.add(map);
				
					Log.w(TAG2, "Number of tiles in List View is " + documentList.size());
				}
				
				// Save the newly created array list as part of the SuiteMetaDataRegistry
				// object, for further use:
				//
				MetaDataForSuite.allDocumentsArrayList = documentList;
			}
			
			// Otherwise, if we already have an array list saved, use that instead.	
			//
			else
			{
				documentList = MetaDataForSuite.allDocumentsArrayList;
			}
			
			// The list view is associated with the "list" element in the
			// listview.xml file. 
			//
			ListView list=(ListView)findViewById(R.id.list);
			
			// Create a Lazy Content Adapter. This associates the values in
			// each hash map contained with a particular graphical region of the list
			// view UI.
			//
	        AdapterForSuite adapter=new AdapterForSuite(activity, documentList);     
	        
	        // Display the List View.
	        //
	        list.setAdapter(adapter);
	        
	        // Convert the document array list, which we have already established as a value
	        // in the Suite Meta Data Registry, to a 2D string array, and save that also in
	        // the same registry. If it already exists there, no need to bother.
	        //
	        if (MetaDataForSuite.allDocuments == null)
	        {
	        	// Set up the BookMetaDataRegistry to contain all the data in the
	        	// XML file, in the form of a 2D java string array.
	        	//
	        	MetaDataForSuite.allDocuments = new String [numberOfContentNodes]
															[NUMBER_OF_ITEMS_IN_NODE];
	        	
	        	// The routine assembleAllContentInfo transforms the document array list into
	        	// a 2D string.
	        	//
	        	MetaDataForSuite.allDocuments = assembleAllContentInfo(documentList);
	        	Log.w(TAG0, "Set SuiteMetaDataRegistry." + '\n');
	        }
	        
	        // If this 2D array is already in place in the Suite Meta Data Registry, no need to
	        // do anything at this point.
	        //
	        else
	        {
	        	Log.w(TAG2, "We already have established values for SuiteMetaDataRegistry.allDocuments." + '\n');
	        }
	        
	        // The SimpleService is currently an empty service, but may
	        // be properly implemented as some stage in the future. Right
	        // now, it exists as a place-holder only.
	        //
	        //startService(new Intent(this, SimpleService.class));
	        
	        // Now determine what happens when a user clicks on an individual
	        // row in the list view UI. 
	        //
	        list.setOnItemClickListener(new OnItemClickListener() 
	        {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
				{ 	
					// Grab the hidden id value for the row that was clicked on. The hidden id is the key
					// to our determining which document has been chosen.
					//
					TextView tv=(TextView)view.findViewById(R.id.hidden_id);
					int idInt = Integer.parseInt(tv.getText().toString());
					
					Log.w(TAG4, "Click received.");
					Log.w(TAG4, "Value of idInt is " + idInt + '\n');
					
					// If the id signifies the penultimate item in the menu, it requires we go back to the
					// welcome page.
					//
					if (idInt == (MetaDataForSuite.allDocumentsArrayList.size() - 2))
					{
						Intent intent = new Intent(ListViewForSuiteActivity.this, WelcomePageActivity.class);
						startActivity(intent); 
					} 
					
					// If the id signifies the last item in the menu, it requires we exit the
					// application.
					//
					else
					{
						if (idInt == (MetaDataForSuite.allDocumentsArrayList.size() - 1))
						{
							new AlertDialog.Builder(ListViewForSuiteActivity.this, R.style.CustomDialog)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle("Exit Application")
							.setMessage("Are you sure you want to exit?")
							.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									Intent intent = new Intent(ListViewForSuiteActivity.this, LoginActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("exit", "true");
									startActivity(intent);
								}
							})
							.setNegativeButton("No", null)
							.show(); 
						}
						
						// Otherwise, we proceed to the selected chapter.
						//
						else
						{					
							Intent intent = new Intent(ListViewForSuiteActivity.this, BookCoverActivity.class);
							intent.putExtra("intVar", idInt);
							Log.w(TAG0, "starting intent with idInt now " + '\n');
							startActivity(intent);
						}
					}	
				}
	        });					    
		}
		
		/**
		 * Transforms an Array List of Hash Maps into a two-dimensional string. 
		 * It is used to allow document locations to be ascertained with
		 * ease when selected by the user.
		 *
		 * @param map An array list of hash maps, each of which features string pairings.
		 * 
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
				
				// Put this "strip" of information into the current slot in the 2D string array.
				//
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
		 *    	
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
					if (key_string == "doc_title") 
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
									if (key_string == "bookcover")
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

	}	
	
	/**
	 * Restores the ListView to its previous state, once the user 
	 * has returned from another activity. It also performs
	 * an explicit garbage collection.
	 * 
	 */
	public void onResume()
	{
	    super.onResume();   	
	    
	    // Remnant of a background service experiment. Left in place because we might want to
	    // use this at some point.
	    //
	    //startService(new Intent(this, SimpleService.class));
	    
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