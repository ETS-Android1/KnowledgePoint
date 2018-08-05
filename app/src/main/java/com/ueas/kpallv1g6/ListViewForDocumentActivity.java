package com.ueas.kpallv1g6;

// Significant input came from Ravi Tamada, on the architecture o fthis class.
//

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Presents a List View that the user employs to select from a range 
 * of chapters within a document. Selection duly takes the user to
 * the text of the specified chapter. 
 * 
 * @author tony.hillman@ultra-as.com
 * 
 */
public class ListViewForDocumentActivity extends Activity 
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
	// NOTE: THIS NEEDS TO BE DERIVED SOME OTHER WAY.
	//
	private int NUMBER_OF_ITEMS_IN_NODE = 6; 
	
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
	// We pass the current activity to the Adapter, so that the ListView
	// can be prepared. But we do so in a PostExecution method, after
	// all the network stuff is completed. So, we can't just use the "this" keyword,
	// and must pass an activity variable.
	//
	public Activity activity = this;
	
	/**
	 * A string that will hold the location of the xml file containing chapter information for
	 * the current document.
	 * 
	 */
	public String xml = "";

	/**
	 * A string used for LogCat output from the onCreate method.
	 * 
	 */
	private final static String TAG0 = "CcLVfB:onCreate";
	
	/**
	 * A string used for LogCat output from the doInBackground method.
	 * 
	 */
	private final static String TAG1 = "CcLVfB:doInBackground";
	
	/**
	 * A string used for LogCat output from the onPostExecute method.
	 * 
	 */
	private final static String TAG2 = "CcLVfB:onPostExecute";
	
	/**
	 * A hash map used in the preparation of information for display in the
	 * List View.
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
	 * Obtains the URL for the chapter information, parses this as a DOM document
	 * object, then uses an AdapterForDocument object to prepare and display a
	 * corresponding ListView. When a selection is made by the user, the unique
	 * ID for the selection is retrieved and passed to the Chapter Activity,
	 * which duly displays the text for the selected chapter.   
	 * 
	 * @see ChapterActivity   
	 * 
	 * @see AdapterForDocument
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// Establish the layout for the List View that will display a book's chapters. Note that
		// this layout file is generic, and so applies to all aspect ratios and screen sizes.
		//
		setContentView(R.layout.list_view_for_book);
		
		// Get the intent whereby the current activity was started. This contains a pathname
		// to an xml file containing data on all of the chapters we need to display as options
		// within the List View.
		//
		Intent intent = getIntent();
		String theString = intent.getStringExtra("here");
		Log.w(TAG0, "Got string as " + theString);
		
		// Set the current document. We will keep this around for reference until such
		// a time as we return to the CustomizedContentListView for the suite, at which
		// point it will be zeroed out. This setting helps us to return to the title page
		// of the book from the current activity.
		//
		MetaDataForDocument.currentDocument = intent.getIntExtra("intVar", 0);
		
		// In preparation for the fetch of the xml file, prepare a progress notification
		// that we will show to the user while the required background activity
		// occurs.
		//
		ProgressDialog progress = new ProgressDialog(this, R.style.CustomDialog);
		progress.setTitle("UltraAPEX Knowledge Point");
      	progress.setMessage("Assembling document contents...");
      	
		// The netOp operation is the network activity we do in the background,
		// in order to get the xml file with the data for the chapters, which will
		// be used to create the ListView object.
      	//
		netOp myOp = new netOp(progress);
		myOp.execute(theString);
		
		this.setTitle("Please select a chapter..."); 
		
		// Display the progress notification.
		//
		progress.show();
	}
	
	/**
	 * Handles network-retrieval of the xml file that contains information on the list of chapters to be
	 * displayed in the list view. This operation is performed in the background, with the Document
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
		 * ListViewForDocument as the dialog to be shown during retrieval of the
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
		 * Performs retrieval of the xml file required by the ListView.
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
			if (MetaDataForDocument.all_chapters_doc_object == null)
			{
				// Prepare an xml parser and a string in which to contain the xml file's pathname.
				// See XMLParser.java.
				//
				XMLParser parser = new XMLParser();
				
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
					
				if (xml == null)
				{
					Log.w(TAG0, "Looks like the string is null...");
				}
				else
				{
					Log.w(TAG0, "String is..... " + xml); 
				}
				
				// Now instantiate the doc object with an appropriate parsing of the xml file.
				//
				doc = parser.getDomElement(xml); 
				
				// Now that we have a doc object, save it for further
				// use, so that we can avoid going back to the network
				// more than once.
				//
				MetaDataForDocument.all_chapters_doc_object = doc;
			}
			
			// If at some earlier stage we already instantiated the all_chapters_doc_object, simply
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
		 * Performs the majority of work required by the ListViewForDocument class. Once a Document has
		 * been retrieved, containing information on all chapters in the document, onPostExecute
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
			if (MetaDataForDocument.allChaptersArrayList == null)
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
			}
			
			// Otherwise, if we already have an array list saved, use that instead.	
			//
			else
			{
				chaptersList = MetaDataForDocument.allChaptersArrayList;
				
				// If we are dealing with the User Guide, keep a copy, to assist our navigation
				// to it from the animations and voice sections.
				//
				if (MetaDataForDocument.currentDocument == 0)
				{
					MetaDataForDocument.userGuideChaptersArrayList = MetaDataForDocument.allChaptersArrayList;
				}
			}
			
			// The list view is associated with the main element in the
			// listview_for_book.xml layout file. 
			//
			ListView list=(ListView)findViewById(R.id.list2);
			
			// Create a Lazy Content Adapter For Book object. This associates the values in
			// each hash map contained with a particular graphical region of the list
			// view UI.
			//
	        AdapterForDocument adapter=new AdapterForDocument(activity, chaptersList);     

	        // Display the List View.
	        //
	        list.setAdapter(adapter);
	        
	        // Convert the chapter array list, which we have already established as a value
	        // in the Book Meta Data Registry, to a 2D string array, and save that also in
	        // the same registry. If it already exists there, no need to bother.
	        //
	        if (MetaDataForDocument.allChapters == null)
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
	        	
	        	// If this is the user guide that we are looking at, keep a copy.
	        	//
	        	if (MetaDataForDocument.currentDocument == 0)
				{
	        		MetaDataForDocument.userGuideChapters = MetaDataForDocument.allChapters;
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
	        		Log.w(TAG2, "Chapter Title: " + MetaDataForDocument.allChapters[k][1] + '\n');
	        		
	        		// The sixth position, represented by the integer 5, is always the specification of the toc
	        		// file for the current chapter.
	        		//
	        		MetaDataForDocument.chapterTOCfiles[k] = MetaDataForDocument.allChapters[k][5];
	        	}
	        	
	        	// If this is the user guide that we are looking at, keep a copy of the TOC file location for it.
	        	//
	        	if (MetaDataForDocument.currentDocument == 0)
				{
	        		MetaDataForDocument.userGuideChapterTOCfiles = MetaDataForDocument.chapterTOCfiles;
				}
	        }
	        else
	        {
	        	Log.w(TAG2, "We already have established values for BookMetaDataRegistry.allChapters." + '\n');
	        } 	        
	               
	        // Now determine what happens when a user clicks on an individual
	        // row in the list view UI. 
	        //
	        list.setOnItemClickListener(new OnItemClickListener() 
	        {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
				{ 	
					// Grab the hidden id value for the row that was clicked on. The hidden id is the key
					// to our determining which chapter has been chosen.
					//
					TextView tv=(TextView)view.findViewById(R.id.hidden_id);
					int idInt = Integer.parseInt(tv.getText().toString());
					
					// If the id signifies the last item in the menu, it requires we exit the
					// application.
					//
					if (idInt == (chaptersList.size() - 1))
					{	
						new AlertDialog.Builder(ListViewForDocumentActivity.this, R.style.CustomDialog)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("Exit Application")
						.setMessage("Are you sure you want to exit?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								Intent intent = new Intent(ListViewForDocumentActivity.this, LoginActivity.class);
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
						// If the id signifies the penultimate item in the menu, it requires we go back to the
						// welcome page.
						//
						if (idInt == (chaptersList.size() - 2))
						{
							
							Intent intent = new Intent(ListViewForDocumentActivity.this, 
															WelcomePageActivity.class);
							
							intent.putExtra("here", xml);
							
							startActivity(intent);
						}
						
						// If this is the last item but 2, we must go back to the contents
						// listing for the current document.
						//
						else
						{	
							if (idInt == (chaptersList.size() - 3))
							{
								Intent intent = new Intent(ListViewForDocumentActivity.this, 
																ListViewForSuiteActivity.class);
								
								intent.putExtra("here", xml);
								startActivity(intent);
							}
							
							// If this is the last item but 3, we must go back to the titlepage
							// for the current document.
							//
							else	
							{
								if (idInt == (chaptersList.size() - 4))
								{
									Intent intent = new Intent(ListViewForDocumentActivity.this, 
											BookCoverActivity.class);
									
									// Go back to the cover page for the current book.
									//
									intent.putExtra("intVar", MetaDataForDocument.currentDocument);
									startActivity(intent);
								}
								
								// Otherwise, figure out the destination chapter, based on the idInt
								// returned from clicking on the list. 
								//
								else
								{	
									String urlOfSelectedChapter = MetaDataForDocument.allChapters[idInt][4];
									Log.w(TAG0, "URL of selected chapter is " + urlOfSelectedChapter + '\n');
									
									Log.w(TAG0, "click received.");
									
							    	Intent intent = new Intent(ListViewForDocumentActivity.this, ChapterActivity.class);

							    	// Specify our selection, by including the idInt.
							    	//
							    	intent.putExtra("intVar", idInt);
							    	
							    	// Include the corresponding chapter-file url.
							    	//
							    	intent.putExtra("urlOfChapter", urlOfSelectedChapter); 
							    	
							    	// Signify that we are departing from a list view (a number "2" origin,
							    	// so far as the Chapter Activity is concerned).
							    	//
							    	intent.putExtra("placeOfDeparture", 2);
							    	
							    	// Signify that we are going to a plain chapter-type activity (a number "3"
							    	// origin, in the terminology of the Chapter Activity).
							    	//
							    	intent.putExtra("targetChapterActivityType", 3);
							    	
							    	// Signify the transition profile, which is the code assigned to
							    	// the switch from the placeOfDeparture to the targetChapterActivityType.
							    	//
							    	intent.putExtra("transitionProfile", 200);
							    	
							    	startActivity(intent);
								}
							}
						}
					}
				}
	        });					    
		}			
	
		/**
		 * Transforms an Array List of Hash Maps into a two-dimensional string. 
		 * It is used to allow document locations to be ascertained with ease when 
		 * selected by the user.
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
	}
	
	/**
	 * Restores the ListView to its previous state, once 
	 * the user has returned from another activity. It also performs an explicit 
	 * garbage collection.
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