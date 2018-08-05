package com.ueas.kpallv1g6;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

//	CHAPTER ACTIVITY: NOTE ON TRANSITION PROFILES 
//	---------------------------------------------
//
//	This activity, the Chapter Activity, is used in a number of different ways.
//	It provides the standard text and images for individual chapters; it also
//	provides the chapter table-of-contents displays, whereby individual subsections
// 	of the current chapter can be located. It provides diagram magnifications, which
// 	are accessed by the user's left-clicking on icons, adjacent to the standard
//	diagram displays in the chapter text. It also provides the Preface, About, and
// 	Help sections, available from the Welcome Page.
//
//	The activity-type determines certain aspects of behaviour; including decoration,
//	automated subsection access, y-coordinate search, and intent-establishment for
//	subsequent activities. Type and behaviour-management are ascertained through
//	the contents of the calling intent. The combination of the previous activity
//	and the current activity form the "transition profile", which is inspected so
//	that appropriate behaviour can be determined.
//
//	Transition Profiles are organised into four categories. Category A involves an intent and
//	no post-processing; B, an intent and subsequent scrolling; C, an intent and
//	subsequent reload; D, intent via intercept of user-click, and then subsequent reload.
//
//	Origin		O-no	Destination		D-no	T-Profile	Scroll?		Reload?
//	---------------------------------------------------------------------------
//	---------------------------------------------------------------------------
//	A:
//
//	Welcome		1		Prior			6		100			No			No
//
//	ListView	2		Chapter			3		200			No			No
//
//	Chapter		3		Chapter			3		300			No			No
//
//	Chapter		3		TOC				4		400			No			No
//
//	---------------------------------------------------------------------------
//	B:
//
//	TOC (back)	4		Chapter			3		500			Yes			No
//
//	Diagram		5		Chapter			3		600			Yes			No
//
//	---------------------------------------------------------------------------
//	C:
//
//	Audio		7		Chapter			3		700			No			Yes
//
//	Animation	8		Chapter			3		800			No			Yes
//
//	--------------------------------------------------------------------------------
//	D:
//
//	Chapter		3		Diagram			5		900			No			No
//
//	TOC	(href)	4		Chapter			3		1000		No			Yes
//
//	----------------------------------------------------------------------------
//	----------------------------------------------------------------------------
//
//	When starting, for information on how to proceed, each Chapter Activity must:
//
//	A category: Look at the calling intent, which was created either in the chapter activity itself,
//	            via the forward, backward, or toc button, or in either a Welcome Page or List View. No 
//				scrolling or reloading required.
//
//	B category: Look at the calling intent, which was created in the chapter activity itself, due
//	            to a user-click on the back button. Scrolling required, via onPageFinished, since
//				we are returning to standard chapter-text that we left temporarily, in order to check
//				out a toc or diagram.
//
//	C category: Look at the calling intent, which was created in the audio or animation activity, as
// 	            the result of a user-click on a spinner-entry. Reload required, via onPageFinished, since
//				the url for page-source contains a hashed href.
//
//	D category: Look at the calling intent, which was created through interception of a user click
//	            on an href, in shouldOverrideUrlLoading, within the chapter activity itself. Reload required, 
//				via onPageFinished.
//
//	Note that transitions in the D profile are afforded numbers on a pro forma basis only:
//	these can never be inspected, since the transitions are href-driven, and only recognized
//	through interception and inspection of the URL.
//
//	Only transitions that *conclude* with a Chapter Activity are afforded numbering.
//
/**
 * Presents text and images, to be read by the user from a vertically scrollable surface. 
 * Standard chapter-text is presented this way, as is image-magnification and
 * chapter table-of-contents information. Preface-type information, readable from the 
 * Welcome Page, is also provided by the Chapter Activity. The activity provides options for
 * direct magnification of text, by user-intervention. Access to the current activity-type is
 * tracked, so that individual sub-sections can be scolled to on start-up, and specific
 * y-coordinates similarly sought.
 * 
 * @author tony.hillman@ultra-as.com
 *
 */
public class ChapterActivity extends Activity 
{
	/**
	 * The number of chapters in the current document. This is always equal to the number of
	 * nodes in the source XML file, minus 3: since the last three nodes represent only the
	 * navigation elements that appear at the end of the List View.
	 * 
	 */
	private static int HIGHESTCHAPTERNUMBER = 0; 
	
	/**
	 * The vertical ("Y") coordinate that is being departed from, when a transition occurs from 
	 * a Chapter Activity containing one form of content to one containing another. In cases where
	 * the user returns to a chapter from a toc by hitting the "return" button at the bottom, or when
	 * the user returns to a chapter from a diagram, we need to scroll to the exact place of departure,
	 * and so keep a copy of the Y-value for future use.
	 * 
	 */
	private static int YvalueLastDepartedFrom = 0;
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "onCreate" method.
	 * 
	 */
	private final static String TAG0 = "CA:onCreate";
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "showTOCforCurrentChapter" method.
	 * 
	 */
	private final static String TAG1 = "CA:showTOCforCurrentChapter";
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "goToPreviousSection" method.
	 * 
	 */
	private final static String TAG2 = "goToPreviousSection";
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "establishChapterActivityTypeInfrastructure" method.
	 * 
	 */
	private final static String TAG3 = "establishChapterActivityTypeInfrastructure";
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "setInfrastructureForDiagram" method.
	 * 
	 */
	private final static String TAG4 = "setInfrastructureForDiagram";
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "examineTransitionProfile" method.
	 * 
	 */
	private final static String TAG5 = "examineTransitionProfile";
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "establishWebViewSettings" method.
	 * 
	 */
	private final static String TAG6 = "establishWebViewSettings";
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "setTheWebViewClient" method.
	 * 
	 */
	private final static String TAG7 = "setTheWebViewClient";
	
	/**
	 * Tag string for writing to the LogCat from the Chapter Activity "onPageFinished" method.
	 * 
	 */
	private final static String TAG8 = "onPageFinished";
	
	/**
	 * String to represent the url of a page that needs to be loaded.
	 * 
	 */
	String URLstring = null;
	
	/**
	 * Counter for determining to which chapter-number we traverse when we go forward or backward,
	 * by means of the controls at the foot of the page.
	 * 
	 */
	int nextchapter = 0;  
	
	/**
	 * Counter for determining to which chapter-number we traverse when we go forward or backward,
	 * by means of the controls at the foot of the page.
	 * 
	 */
	int currentchapter = 0;

	/**
	 * The current horizontal coordinate of the chapter.
	 * 
	 */
	static int x; 
	
	/**
	 * The current vertical coordinate of the chapter.
	 * 
	 */
	static int y;
	
	/**
	 * Flag to indicate whether, on arrival in the current activity, we need to scroll to a specified y-coordinate.
	 * This may be required when we re-access chapter text from a diagram, or from the "back" button of the 
	 * chapter-toc.
	 *
	 */
	static int needToScroll = 0; 
	
	/**
	 * Flag to indicate whether, on arrival in the current activity, we need to reload the web page, in order to
	 * get Android to respect the hashed href that has been used to call the current activity, from a link in a 
	 * chapter-toc.
	 * 
	 */
	static int needToReload = 0;
	
	/**
	 * A webview that will contain the chapter information displayed to the user.
	 * 
	 */
	WebView wv = null;
	
	/**
	 * Variable used in the ascertaining and maintenance of the identity of the "prior activity" that
	 * is to be displayed by the Chapter Activity, when this is indeed so. A value of 1 indicates "Preface",
	 * 2 indicates "About", and 3 "Help".
	 * 
	 */
	static int prior_activity_number = 0;

	/**
	 * A string used in the determining of chapter numbers.
	 * 
	 */
	static String chapterNumberString = "";
	
	/**
	 * An integer used in the determining of chapter numbers.
	 * 
	 */
	static int chapterNumberInt = 0;
	
	/**
	 * Variable to express the type of the current Chapter Activity. This
	 * is the value retrieved from "whetherTOC", in the intent that started the current activity. FIX THIS
	 * NAME.
	 * 
	 */
	static int currentChapterActivityType = 0;
	
	/**
	 * Variable to express the point of origin constituted by the current Chapter Activity. This
	 * is loaded into the intent with which we recall the Chapter Activity with different
	 * content.
	 * 
	 */
	static int placeOfFutureDeparture = 0;
	
	/**
	 * Variable to express the point of origin whence the current Chapter Activity was
	 * invoked.
	 * 
	 */
	static int placeOfOrigin = 0;
	
	/**
	 * Variable to express the combination of starting and ending points for a
	 * transition from one type of Chapter Activity to another. For example, from
	 * chapter toc back to chapter; from one chapter to the next; from chapter
	 * to diagram.
	 * 
	 */
	static int transitionProfile = 0;
	
	/**
	 * Holds the initial scale of the webview, when the activity starts. This may be used
	 * in tracking y-coordinates that need to be reacquired when documents are returned to,
	 * such that they need to appear partially scrolled. Since the scale of the document may
	 * have been modified by user-magnification at the time of departure, the y-coordinate to
	 * be reacquired needs to be scaled down, so as to be accurate according to the initial,
	 * default scale for the activity.
	 * 
	 */
	static float initialScale = 0;
	
	// Variables required for the text-search facility.
	//
	/**
	 * Instantiated so as to house the facilities for text-search: Find, Repeat, and Close.
	 * 
	 */
	private LinearLayout container; 
	
	/**
	 * Allows a previously initiated text-search to be repeated.
	 * 
	 */
	private Button repeatButton; 
	
	/**
	 * Closes the text-search facility, dismisses all associated graphical utilities, and
	 * restores the title-bar to visibility.
	 * 
	 */
	private Button closeButton;
	
	/**
	 * Accepts textual input for which a search can be conducted on the contents of the current
	 * chapter.
	 * 
	 */
	private EditText findBox; 

	/**
     * A handler that initiates running of a method every so many milliseconds,
     * to update the position of the seek bar and the clock. 
     * 
     */
    private final Handler handler = new Handler();

	/**
	 * Examines the calling intent to determine
	 * from what kind of activity it has been invoked, and what "type" of activity it is
	 * supposed to effect. A Chapter Activity can be of the following kinds: standard, toc,
	 * diagram, prior. The method hereby determines whether scrolling or sub-section acquisition
	 * is required in the initial display it renders, and make the appropriate arrangements.
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		Log.w(TAG0, "Doing the onCreate for ChapterActivity.");

		// Grab the intent used to call the current Chapter Activity.
		//
		Intent intent = getIntent();
		
		// Determine what kind of activity we got here from, by consulting information provided in the
		// intent.
		//
		placeOfOrigin = intent.getIntExtra("placeOfDeparture", 0);

		// Find out what type of chapter activity we are, again by consulting information in the intent.
		//
		currentChapterActivityType = intent.getIntExtra("targetChapterActivityType", 0);
		
		// Determine the transition profile number. We'll use this in ensuring we make the
		// right adjustments for viewing. Note that once the transitionProfile has been
		// recognized and adjustments made, it must be reset to 0. This is to ensure that
		// if any href is subsequently used to transition to another type of Chapter Activity,
		// the zero can be recognized as indeed signifying this kind of transition.
		//
		transitionProfile = intent.getIntExtra("transitionProfile", 0);
		
		// Examine the transition profile, and make settings based on it. Principally, these
		// involve the "needToReload" and "needToScroll" flags, which may be used to locate
		// a subsection (based on an href in the calling intent's url) or a specific
		// y-coordinate.
		//
		examineTransitionProfile(transitionProfile, intent);
		
		establishChapterActivityTypeInfrastructure(currentChapterActivityType, intent);
		
		wv = establishWebViewSettings(wv);

		// Grab the webview settings, and specify a default font size. NOTE: THIS
		// PROBABLY SHOULD BE REMOVED. IT IS LIKELY OBSOLETE, BUT ANYWAY, SHOULD BE
		// HANDLED IN A RESOURCE FILE, NOT IN CODE. FIX.
		//
		WebSettings webSettings = wv.getSettings();
		webSettings.setDefaultFontSize(10);

		// Load the string passed with the intent into the webview for display.
		//
		wv.loadUrl(URLstring);
		
	} // End of onCreate.
	
	/**
	 * Returns the user to the section-menu for the current chapter, destroying
	 * the current activity as it does so.
	 * 
	 * @param view
	 *            The current view.
	 */
	public void returnToMenu(View view) 
	{
		// Put all coordinates back to zero.
		//
		YvalueLastDepartedFrom = 0;
		x = 0;
		y = 0;

		// Establish an intent for returning us to the chapter-menu. Provide the location
		// of the file for the current chapter and the corresponding ID integer, both of
		// which are expected.
		//
		Intent intent = new Intent(this, ListViewForDocumentActivity.class);
		String XMLfileLocation = MetaDataForSuite.allDocuments[currentchapter][4];
		intent.putExtra("here", XMLfileLocation);
		intent.putExtra("intVar", MetaDataForDocument.currentDocument);
		
		// Start the ListView activity.
		//
		startActivity(intent);
	
		// End the chapter activity entirely.
		//
		finish();
		
		// Free webview memory.
		//
		wv = null;
	}

	/**
	 * Takes the user to the next sequential section in the current chapter,
	 * starting the appropriate activity, and destroying the current as it does
	 * so.
	 * 
	 * @param view
	 *            The current view.
	 */
	public void goToNextSection(View view) 
	{
		// We are visiting the next chapter, and so will go to the top of the chapter-page.
		// There no need for any coordinates other than zero.
		//
		YvalueLastDepartedFrom = 0;
		x = 0;
		y = 0;

		// If we are currently in the last chapter, the next chapter becomes the
		// first chapter.
		//
		if (currentchapter >= HIGHESTCHAPTERNUMBER) 
		{
			nextchapter = 0;
		} 
		
		// Otherwise, just add one to the current chapter-number, and go there.
		//
		else
		{
			nextchapter = currentchapter;			
		}

		// Set up the intent.
		//
		Intent intent = null;
		intent = new Intent(ChapterActivity.this, ChapterActivity.class);
		
		// Specify the next chapter as our target.
		//
		String urlOfSelectedChapter = MetaDataForDocument.allChapters[nextchapter][4];

		// Add the chapter-text url and the ID, to be sent along with the intent.
		//
    	intent.putExtra("urlOfChapter", urlOfSelectedChapter);
		intent.putExtra("intVar", nextchapter);
		
		// Also specify that our departure and our target are both chapter-activities, which is
		// to say "3" in the terminology of the Chapter Activity. Then specify the corresponding
		// transition profile; which for chapter to chapter, is 300.
		//
		intent.putExtra("placeOfDeparture", 3);
    	intent.putExtra("targetChapterActivityType", 3);
    	intent.putExtra("transitionProfile", 300);
    	
    	// Start the activity.
    	//
		startActivity(intent);
		
		// Free the webview memory, then destroy the activity we are leaving.
		//
		wv = null;
		finish();
	}
	
	/**
	 * Replaces the display of the main chapter
	 * content with a display of the table of contents for that chapter. 
	 * 
	 * @param view The current view.
	 * 
	 */
	public void showTOCforCurrentChapter(View view)
	{
		// Establish the webview for the toc.
		//
		wv = (WebView) findViewById(R.id.my_webview);

		// Determine the current y-coordinate, so that we can optionally
		// return to it, if we do return by means of the back button.
		//
		YvalueLastDepartedFrom = wv.getScrollY();

		// The y-coordinate we just obtained might be affected by rescaling.
		// So we must translate it into a coordinate that applies when the
		// initial, default scaling is used (this is the scaling we'll return to).
		//
		@SuppressWarnings("deprecation")
		float currentScale = wv.getScale();
		float newY = (float) YvalueLastDepartedFrom * ( (initialScale) / currentScale);
		Log.w(TAG1, "newY is " + newY);
		x = 0;
		
		// Re-establish the y-coordinate based on the initial, default scale.
		//
		YvalueLastDepartedFrom = (int) newY;
		
		// Establish an intent, for starting the toc-type Chapter Activity.
		//
		Intent intent = null;
		intent = new Intent(ChapterActivity.this, ChapterActivity.class);
		
		// Specify that our departure is 3, and our target 4, meaning chapter and
		// chapter toc, respectively. This means a transition profile of 400. See
		// the Chapter Activity for a full description of the numbering system.
		//
		intent.putExtra("placeOfDeparture", 3);
    	intent.putExtra("targetChapterActivityType", 4);
    	intent.putExtra("transitionProfile", 400);
		
		String urlOfTOCforCurrentChapter = "";
		
		// Get the appropriate toc location. 
		//
		urlOfTOCforCurrentChapter = MetaDataForDocument.chapterTOCfiles[currentchapter - 1];

		// Add the url for the chapter-toc to which we are going.
		//
		intent.putExtra("urlOfChapter", urlOfTOCforCurrentChapter);

		// Specify that it's the same chapter-number as the one we currently
		// have.
		//
		intent.putExtra("intVar", currentchapter - 1);
    	
		// Start the activity.
		//
		startActivity(intent);
		
		// Nullify the current webview object. We'll re-instantiate.
		//
		wv = null;
		
		// Destroy the current activity, to save space.
		//
		finish();
	}

	/**
	 * Takes the user to the last sequential section 
	 * in the current chapter, starting the appropriate activity, and destroying the 
	 * current as it does so; or, takes the user back to the chapter web page, if 
	 * a diagram is currently being displayed.
	 * 
	 * @param view
	 *            The current view.
	 */
	public void goToPreviousSection(View view) 
	{	
		Log.w(TAG0, "goToPreviousSection starts....\n");
		
		// Establish an intent with which we transition to the next
		// activity.
		//
		Intent intent = null;
		
		// Determine the nature of the current activity, and determine the kind
		// of transition the back-button entails purely on that basis. There
		// is only one possibility for any given chapter activity-type.
		//
		switch(currentChapterActivityType)
		{
			// If we are in a standard chapter activity, we must be going
			// back to another standard chapter activity.
			//
			case 3:
				
				// We are visiting the next chapter, and so will go to the top of the chapter-page.
				// There no need for any coordinates other than zero.
				//
				YvalueLastDepartedFrom = 0;
				x = 0;
				y = 0;
				
				// If we are at the fist chapter...
				//
				if (currentchapter <= 1) 
				{
					// Then the chapter prior to this is the last chapter, which is the number
					// of the last chapter minus one, in the array in which its url is accessed.
					//
					nextchapter = HIGHESTCHAPTERNUMBER - 1;
				} 
				else 
				{
					// The visible number of the current chapter is one less, in the array in
					// which its url is accessed. Lessen this by 1 more, to get to the 
					// previous chapter: thus, we go back by one chapter.
					//
					nextchapter = currentchapter - 2;
				}

				// Set up the intent, providing the basic information needed to
				// display the chapter to which we are going.
				//
				intent = new Intent(ChapterActivity.this, ChapterActivity.class);		
				String urlOfSelectedChapter = MetaDataForDocument.allChapters[nextchapter][4];
				intent.putExtra("urlOfChapter", urlOfSelectedChapter);
				intent.putExtra("intVar", nextchapter);
				
				// Also specify that our departure and our target are both chapter-activities, which is
				// to say "3" in the terminology of the Chapter Activity. Then specify the corresponding
				// transition profile; which for chapter to chapter, is 300.
				//
				intent.putExtra("placeOfDeparture", 3);
		    	intent.putExtra("targetChapterActivityType", 3);
		    	intent.putExtra("transitionProfile", 300);

		    	// Start the activity.
		    	//
				startActivity(intent);
				
				// Free the webview memory, and destroy the activity we are leaving.
				//
				wv = null;
				finish();
				
				break;
			
			// If we are in a toc, then we are going back to the main chapter
			// activity.
			case 4:	
				
				// Set up the intent for the chapter we are returning to.
				//
				intent = new Intent(ChapterActivity.this, ChapterActivity.class);
				
				// The chapter number is the same number to which the current toc
				// corresponds. So, go back to the current chapter-number minus one,
				// to get its correct array-position.
				//
				nextchapter = currentchapter - 1;

				// Add information appropriate for the chapter we are returning
				// to.
				//
				urlOfSelectedChapter = MetaDataForDocument.allChapters[nextchapter][4];
				intent.putExtra("urlOfChapter", urlOfSelectedChapter);
				intent.putExtra("intVar", nextchapter);
				
				// Also specify that our departure and our target are both chapter-activities, which is
				// to say "3" in the terminology of the Chapter Activity. Then specify the corresponding
				// transition profile; which for chapter to chapter, is 300.
				//
				intent.putExtra("placeOfDeparture", 4);
		    	intent.putExtra("targetChapterActivityType", 3);
		    	intent.putExtra("transitionProfile", 500);
		    	
		    	// Start the activity.
		    	//
				startActivity(intent);
				
				// Free the webview memory, and destroy the activity we are leaving.
				//
				wv = null;
				finish();
				
				break;
				
			// If we are in a diagram activity, then we must be going back to the
			// main chapter activity.
			//
			case 5:
				
				// Set up the intent for the chapter we are returning to.
				//
				intent = new Intent(ChapterActivity.this, ChapterActivity.class);
				
				// The chapter number will be the same as the one the current diagram
				// corresponds to.
				//
				nextchapter = currentchapter - 1;

				// Add appropriate information for the chapter we are returning to.
				//
				urlOfSelectedChapter = MetaDataForDocument.allChapters[nextchapter][4];
				Log.w(TAG2, "url of chapter returned to is ... " + urlOfSelectedChapter);
				intent.putExtra("urlOfChapter", urlOfSelectedChapter);
				intent.putExtra("intVar", nextchapter);
				
				// Also specify that our departure and our target are diagram and chapter-activities, respectively,
				// which is represented as 5 and 3. Then specify the corresponding
				// transition profile; which for chapter to chapter, is 600.
				//
				intent.putExtra("placeOfDeparture", 5);
		    	intent.putExtra("targetChapterActivityType", 3);
		    	intent.putExtra("transitionProfile", 600);
		    	
		    	// Start the activity.
		    	//
				startActivity(intent);
				
				// Free the webview memory, and destroy the activity we are leaving.
				//
				wv = null;
				finish();
				
				break;
			
			// If we are in a prior activity, then we must be going back to the
			// Welcome Page.
			//
			case 6:
				
				// Establish an intent to take us to the Welcome Page Activity.
				//
				intent = new Intent(ChapterActivity.this, WelcomePageActivity.class);
				
				// Start the activity.
		    	//
				startActivity(intent);
				
				// Free the webview memory, and destroy the activity we are leaving.
				//
				wv = null;
				finish();
				
				break;

			// If something is wrong, and we haven't accounted for the attempted
			// transition, go back to the Welcome Page.
			//
			default:
				
				Log.w(TAG2, "Couldn't perform the backwards transition. Going to Welcome Page.\n");
				
				// Establish an intent to take us to the Welcome Page Activity.
				//
				intent = new Intent(ChapterActivity.this, WelcomePageActivity.class);	
				
				// Start the activity.
		    	//
				startActivity(intent);
				
				// Free the webview memory, and destroy the activity we are leaving.
				//
				wv = null;
				finish();
				
				break;
		}
	}
	
	/**
	 * Stops and destroys the media player, forces a garbage collection, and specifically
	 * reclaims memory from the current activity's webview.
	 * 
	 */
	@Override
	public void onDestroy() 
	{
		super.onDestroy();

		System.gc();
		
		wv = null;
	}
	
	/**
	 * Exits the current application, due to an error. Presents a dialog to the
	 * user, so that the user can confirm the exit.
	 * 
	 */
	public void exitTheApplication()
	{
		// Establish the alert dialog with which to exit the application.
		//
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		
		// Set the basic characteristics of the dialog, including the
		// error messages.
		//
        builder
            .setMessage( "There was an error: Exiting..." )
            .setCancelable( false )
            .setNeutralButton( "Ok.", new DialogInterface.OnClickListener()
            {
                public void onClick ( DialogInterface dialog, int which )
                {
                    ChapterActivity.this.finish();
                }
            } );
        
        // Show the dialog, as an error-handling mechanism.
        //
        AlertDialog error = builder.create();   		
        error.show();
        
        return;
	}
	
	/**
	 * Based on the type of the current Chapter Activity, and additional information within the
	 * calling intent, establishes infrastructure for the activity-type. This includes strings for
	 * the decoration, plus additional variables for navigation purposes.
	 * 
	 * @param chapterActivityType An integer that specifies the type of the current Chapter Activity.
	 * 
	 * @param intent The intent used to call the current activity.
	 * 
	 */
	private void establishChapterActivityTypeInfrastructure(int chapterActivityType, Intent intent)
	{
		// Examine the chapterActivityType, and based on its value, set up the infrastructure
		// for the current activity appropriately.
		//
		switch (chapterActivityType)
		{
			// If we have a standard chapter-activity...
			//
			case 3:
				
				// Duly set up the infrastructure for the standard activity.
				//
				setInfrastructureForChapterOrTOC(intent);
				
				break;
			
			// If we have a chapter-toc...
			//
			case 4:
				
				// Duly set up the infrastructure for the toc-activity.
				//
				setInfrastructureForChapterOrTOC(intent);
				
				break;
			
			// If we have a diagram-type activity...
			//
			case 5:
				
				// Duly set up the infrastructure for the diagram-type activity.
				//
				setInfrastructureForDiagram(intent);
				
				break;
				
			// If we have a prior activity...
			//
			case 6:
				
				// Duly set up the infrastructure for the prior activity.
				//
				setInfrastructureForPrior(intent);
				
				break;
			
			default:
				
				//Log.w(TAG3, "Problem in set up.\n");
				
				exitTheApplication();
				
				break;
		}
	}
	
	/**
	 * Sets up the infrastructure for a Chapter Activity whose content is of standard or
	 * TOC-type. The infrastructure includes the strings for the decoration, and additional
	 * variables for navigation purposes.
	 * 
	 * @param intent The intent used to call the current activity.
	 * 
	 */
	private void setInfrastructureForChapterOrTOC(Intent intent)
	{
		// Get the integer sent with the intent, and based on its value,
		// choose a chapter to display.
		//
		currentchapter = intent.getIntExtra("intVar", 0); 

		// Set the title.
		//
		this.setTitle(getString(R.string.app_name));
	
		// Establish the textview for title-display.
		//
		TextView tv = (TextView)findViewById(R.id.my_textview);
	
		// Establish the string for the book-title.
		//
		String bookTitleString = ""; 

		// Determine whether we are looking at the user guide or an administrator guide, and
		// set title-elements accordingly.
		//
		if (MetaDataForSuite.allDocuments[MetaDataForDocument.currentDocument][1].equals("User Guide"))
		{
			bookTitleString = "UltraAPEX User Guide";
		}
		else
		{
			// The Galaxy S6 is short on space. So, when we find admin guide titles that are long,
			// we cut them down, so that they fit acceptably, without wrap-around.
			//
			if (MetaDataForSuite.currentAspectRatio == 110)
			{
				String titleTestString = MetaDataForSuite.allDocuments[MetaDataForDocument.currentDocument][1];

				if (titleTestString.equals("Pre-Departure Sequencer"))
				{
					bookTitleString = "UltraAPEX Administrator Guide: "
							+ "Sequencer";
				}
				else
				{
					if (titleTestString.equals("Strategic Dashboards"))
					{
						bookTitleString = "UltraAPEX Administrator Guide: "
								+ "Dashboards";
					}
					else
					{
						if (titleTestString.equals("Delay Code Management"))
						{
							bookTitleString = "UltraAPEX Administrator Guide: "
									+ "Delay Codes";
						}
						else
						{
							// Otherwise, just print the whole book-title.
							//
							bookTitleString = "UltraAPEX Administrator Guide: "
									+ MetaDataForSuite.allDocuments[MetaDataForDocument.currentDocument][1];
						}
					}
				}
			}
			else
			{
				// If the device is not an S6, just print the whole title.
				//
				bookTitleString = "UltraAPEX Administrator Guide: "
						+ MetaDataForSuite.allDocuments[MetaDataForDocument.currentDocument][1];
			}
		}
		
		// Set the title-bar text.
		//
		tv.setText("\u0009" + bookTitleString + "\n\u0009" + "Chapter " + MetaDataForDocument.allChapters[currentchapter][1]);

		// Which chapter are we looking at? Chapter IDs are counted from 0, but real chapters from 1. So, we base
		// our computation of the chapter number on "ID + 1".
		//
		currentchapter = currentchapter + 1;
	
		// Get the url of the current chapter from the calling intent.
		//
		URLstring = intent.getStringExtra("urlOfChapter");

		// Establish the number of the last chapter. This will always be 4 less than the number of nodes, since
		// the last four are used solely for navigational purposes.
		//
		HIGHESTCHAPTERNUMBER = (MetaDataForDocument.nodesInCurrentXMLsourcefile - 4); 	
	}
	
	/**
	 * Sets up the infrastructure for a Chapter Activity whose content is of the diagram-type.
	 * The infrastructure includes the strings for the decoration, and additional
	 * variables for navigation purposes.
	 * 
	 * @param intent The intent used to call the current activity.
	 */
	private void setInfrastructureForDiagram(Intent intent)
	{
		Log.w(TAG4, "We've arrived in a diagram activity.\n");
		
		// Get the chapter ID from the calling intent.
		//
		currentchapter = intent.getIntExtra("intVar", 0); 
		
		// Which chapter are we looking at? Chapter IDs are counted from 0, but real chapters from 1. So, we base
		// our computation of the chapter number on "ID + 1".
		//
		currentchapter = currentchapter + 1;
		
		// Get the url of the chapter.
		//
		URLstring = intent.getStringExtra("urlOfChapter");
		
		Log.w(TAG4, "URLstring is " + URLstring);
	}
	
	/**
	 * Sets up the infrastructure for a Chapter Activity whose content is of the prior-type.
	 * The infrastructure includes the strings for the decoration, and additional
	 * variables for navigation purposes.
	 * 
	 * @param intent The intent used to call the current activity.
	 */
	private void setInfrastructureForPrior(Intent intent)
	{
		TextView tv = (TextView)findViewById(R.id.my_textview);
		
		// Set the titlebar for the prior activity ("Preface", "About", "Feedback", etc). 
		//
		String priorActivityTitle = "";
	
		switch (prior_activity_number)
		{
			case 0:
				priorActivityTitle = priorActivityTitle.concat("Preface");
				break;
				
			case 1:
				priorActivityTitle = priorActivityTitle.concat("About");
				break;
				
			case 2:
				
				// Currently not used, but might be one day.
				//
				priorActivityTitle = priorActivityTitle.concat("Feedback");
				break;
				
			case 3:
				priorActivityTitle = priorActivityTitle.concat("Help");
				break;	
				
			default:
				priorActivityTitle = priorActivityTitle.concat("");
				break;
		}
	
		URLstring = MetaDataForSuite.prior_activity_files[prior_activity_number][0];

		tv.setText("\u0009" + (getString(R.string.app_name)) + "\n\u0009" + priorActivityTitle);
	}
	
	/**
	 * Examines the value of the current transition profile, and establishes values based
	 * on it. Chiefly, these are used to ascertain whether there is a need to seek a
	 * particular subsection within chapter-text, or to scroll to a specific y-coordinate.
	 * 
	 * @param transitionProfile The current transition profile.
	 * 
	 * @param intent The intent used to start the current activity.
	 * 
	 */
	private void examineTransitionProfile(int transitionProfile, Intent intent)
	{
		// Figure out what the transition profile is. Once we've done so, make appropriate adjustments.
		// If for some reason there is a discrepancy, exit the application.
		//
		switch (transitionProfile)
		{
			case 0:
				// If we don't have a positive setting for the transitionProfile, something
				// has gone badly wrong. So we need to exit.
				//
				Log.w(TAG0, "Transition problem! Profile at value 0: something wasn't set...");
				
				exitTheApplication();
				
				break;
				
			case 100:
				// The last place of departure was the Welcome Page, the destination a prior
				// activity. No need to reload or scroll.
				//	
				// Make sure we are in a prior activity, and indeed got here from
				// the Welcome Page:
				//
				if ((currentChapterActivityType != 6) || (placeOfOrigin != 1))
				{
					Log.w(TAG5, "Transition problem! Not \"from 1 to 6\"...");
					
					exitTheApplication();
				}
				else
				{	
					// Establish the content view for the prior activity. Do this conditionally, based on the 
					// screen-size/aspect-ratio combination we have already determined.
					//
					switch (MetaDataForSuite.currentAspectRatio)
					{
						// This is a 480x800 4.3" device.
						//
						case 0:
							setContentView(R.layout.prior_activity_for_zero___four_point_three);
							Log.w(TAG0, "Set Prior content for 0.\n");
							break;
							
						// This is a 540x960 4.0" to 4.8" device.
					    //
						case 10:
							setContentView(R.layout.prior_activity_for_one_zero___four_point_zero_etc);
							Log.w(TAG0, "Set Prior content for 10.\n");
							break;
									
						// This is a 720x1280 4.3" to 5.0" device.
					    //
						case 20:
							setContentView(R.layout.prior_activity_for_two_zero___four_point_three_etc);
							Log.w(TAG0, "Set Prior content for 20.\n");
							break;
							
						// This is a 768x1280 4.7" device.
					    //
						case 30:
							setContentView(R.layout.prior_activity_for_three_zero___four_point_seven);
							Log.w(TAG0, "Set Prior content for 30.\n");
							break;
							
						// This is a 800x1280 7" device.
					    //
						case 40:
							setContentView(R.layout.prior_activity_for_four_zero___seven);
							Log.w(TAG0, "Set Prior content for 40.\n");
							break;
							
						// This is a 800x1280 10.1" device.
					    //
						case 50:
							setContentView(R.layout.prior_activity_for_five_zero___ten);
							Log.w(TAG0, "Set Prior content for 50.\n");
							break;
							
					    // This is a 1080x1920 generic-size device.
						//
						case 60:
							setContentView(R.layout.prior_activity_for_six_zero___gen);
							Log.w(TAG0, "Set Prior content for 60.\n");
							break;
						
						// This is a 1080x1920 5.7 inch device.
						//
						case 70:
							setContentView(R.layout.prior_activity_for_seven_zero___five_point_seven);
							Log.w(TAG0, "Set Prior content for 70.\n");
							break;
						
						// This is a 1200x1920 7 inch device.
						//
						case 80:
							setContentView(R.layout.prior_activity_for_eight_zero___seven);
							Log.w(TAG0, "Set Prior content for 80.\n");
							break;
						
						// This is a 1200x1920 10 inch device.
						//
						case 90:
							setContentView(R.layout.prior_activity_for_nine_zero___ten);
							Log.w(TAG0, "Set Prior content for 90.\n");
							break;
							
						// This is a 1440x900 7 inch device.
						//
						case 100:
							setContentView(R.layout.prior_activity_for_one_zero_zero___seven);
							Log.w(TAG0, "Set Prior content for 100.\n");
							break;

						// This is a 1440x2560 5.1 inch device.
						//
						case 110:
							setContentView(R.layout.prior_activity_for_one_one_zero___five_point_one);
							Log.w(TAG0, "Set Prior content for 110.\n");
							break;
					
						// If we are not sure, we use the following default.
						//
						default:
							setContentView(R.layout.prior_activity_for_six_zero___gen);
							Log.w(TAG0, "Set Prior content for default.\n");
							break;			
					}
					
					// We do not need to scroll, since we are entering a prior for the first time.
					//
					needToScroll = 0;
					
					// We do not need to reload, since no href has been used to access the current page.
					//
					needToReload = 0;
					
					// Now find out specifically which prior activity we are going to be looking at, by 
					// checking information provided in the intent.
					// 
					prior_activity_number = intent.getIntExtra("prior_activity_number", 0);	
				}
				
				break;
				
			case 200:
				// The last place of departure was a listview, and we have come to a chapter. 
				//	
				// Make sure our current location and presumed origin are correct.
				//
				if ((currentChapterActivityType != 3) || (placeOfOrigin != 2))
				{
					Log.w(TAG5, "Transition problem! Not \"from 2 to 3\"...");
					
					exitTheApplication();
				}
				else
				{
					// We are in a chapteractivity, so set the layout accordingly. For the chapter, this
					// is abstracted into a method, to avoid repeating the lengthy code segment multiple
					// times throughout the switch statement.
					//
					setChapterContentView(MetaDataForSuite.currentAspectRatio);
					
					// We don't need to scroll, since we are going to the top of a chapter,
					// as initial point of entry.
					//
					needToScroll = 0;
					
					// We don't need to reload, since no href has been used to access the current page.
					//
					needToReload = 0;	
				}

				break;
				
			case 300:
				// The last place of departure was a chapter, and we have accessed another chapter,
				// by means of the forward or backward button.
				//
				// Make sure destination and origin are as presumed.
				//
				if ((currentChapterActivityType != 3) || (placeOfOrigin != 3))
				{
					Log.w(TAG5, "Transition problem! Not \"from 3 to 3\"...");
					
					exitTheApplication();
				}
				else
				{
					// We are in a chapteractivity, so set the layout accordingly. For the chapter, this
					// is abstracted into a method, to avoid repeating the lengthy code segment multiple
					// times throughout the switch statement.
					//
					setChapterContentView(MetaDataForSuite.currentAspectRatio);
					
					// We don't need to scroll, since we are just going to the top.
					//
					needToScroll = 0;
				
					// We don't need to reload, since no href has been used to access the current page.
					//
					needToReload = 0;
				}
				
				break;	
				
			case 400:
				// The last place of departure was a Chapter, and we have arrived in a TOC, by
				// left-clicking on the icon at the top-right of the chapter-display.
				//
				// Make sure origin and destination are what we expect.
				//
				if ((currentChapterActivityType != 4) || (placeOfOrigin != 3))
				{
					Log.w(TAG5, "Transition problem! Not \"from 3 to 4\"...");
					
					exitTheApplication();
				}
				else
				{
					// We are in a toc.
					//
					// Establish the content view for the chapter toc activity. Do this conditionally, based on the 
					// screen-size/aspect-ratio combination we have already determined.
					//
					switch (MetaDataForSuite.currentAspectRatio)
					{
						// This is a 480x800 4.3" device.
						//
						case 0:
							setContentView(R.layout.chapter_toc_activity_for_zero___four_point_three);
							Log.w(TAG0, "Set Chapter toc content for 0.\n");
							break;
							
						// This is a 540x960 4.0" to 4.8" device.
					    //
						case 10:
							setContentView(R.layout.chapter_toc_activity_for_one_zero___four_point_zero_etc);
							Log.w(TAG0, "Set Chapter toc content for 10.\n");
							break;
							
						// This is a 720x1280 4.3" to 5.0" device. 
					    //
						case 20:
							setContentView(R.layout.chapter_toc_activity_for_two_zero___four_point_three_etc);
							Log.w(TAG0, "Set Chapter toc content for 20.\n");
							break;
							
						// This is a 768x1280 4.7" device.
					    //
						case 30:
							setContentView(R.layout.chapter_toc_activity_for_three_zero___four_point_seven);
							Log.w(TAG0, "Set Chapter toc content for 30.\n");
							break;
							
						// This is a 800x1280 7" device.
					    //
						case 40:
							setContentView(R.layout.chapter_toc_activity_for_four_zero___seven);
							Log.w(TAG0, "Set Chapter content for 40.\n");
							break;
							
						// This is a 800x1280 10.1" device.
					    //
						case 50:
							setContentView(R.layout.chapter_toc_activity_for_five_zero___ten);
							Log.w(TAG0, "Set Chapter content for 50.\n");
							break;
						
					    // This is a 1080x1920 generic-size device.
						//
						case 60:
							setContentView(R.layout.chapter_toc_activity_for_six_zero___gen);
							Log.w(TAG0, "Set Chapter toc content for 60.\n");
							break;
						
						// This is a 1080x1920 5.7 inch device.
						//
						case 70:
							setContentView(R.layout.chapter_toc_activity_for_seven_zero___five_point_seven);
							Log.w(TAG0, "Set Chapter toc content for 70.\n");
							break;
						
						// This is a 1200x1920 7 inch device.
						//
						case 80:
							setContentView(R.layout.chapter_toc_activity_for_eight_zero___seven);
							Log.w(TAG0, "Set Chapter toc content for 80.\n");
							break;
						
						// This is a 1200x1920 10 inch device.
						//
						case 90:
							setContentView(R.layout.chapter_toc_activity_for_nine_zero___ten);
							Log.w(TAG0, "Set Chapter toc content for 90.\n");
							break;
							
						// This is a 1440x900 7 inch device.
						//
						case 100:
							setContentView(R.layout.chapter_toc_activity_for_one_zero_zero___seven);
							Log.w(TAG0, "Set Chapter toc content for 100.\n");
							break;

						// This is a 1440x2560 5.1 inch device.
						//
						case 110:
							setContentView(R.layout.chapter_toc_activity_for_one_one_zero___five_point_one);
							Log.w(TAG0, "Set Chapter toc content for 110.\n");
							break;
						
						// If we are not sure, we use the following default.
						//
						default:
							setContentView(R.layout.chapter_toc_activity_for_six_zero___gen);
							Log.w(TAG0, "Set Chapter toc content for default.\n");
							break;			
					}
					
					Log.w(TAG5, "TOCtest: Have set chapter_toc layout.\n");
				
					// We don't need to scroll, since we are arriving at the top of the toc.
					//
					needToScroll = 0;
				
					// We don't need to reload, since no href has been used to access the current page.
					//
					needToReload = 0;
				}
				
				break;	
				
			case 500:
				// We have transitioned from a chapter-toc back to a chapter, via the back-button.
				//
				// Make sure the points of origin and destination are as assumed.
				//
				if ((currentChapterActivityType != 3) || (placeOfOrigin != 4))
				{
					Log.w(TAG5, "Transition problem! Not \"from 3 to 4\"...");
					
					exitTheApplication();
				}
				else
				{
					// We are in a chapteractivity, so set the layout accordingly. For the chapter, this
					// is abstracted into a method, to avoid repeating the lengthy code segment multiple
					// times throughout the switch statement.
					//
					setChapterContentView(MetaDataForSuite.currentAspectRatio);
				
					// We don't need to scroll.
					//
					needToScroll = 0;
				
					// We do need to reload, since an href has been used to access the current page,
					// and we are looking for a specific sub-section within the chapter.
					//
					needToReload = 1;
				}

				break;
				
			case 600:
				// The last place of departure was a diagram, and we have returned to the chapter
				// activity via the back-button of the diagram activity.
				//
				// Make sure the points of origin and destination are as assumed.
				//
				if ((currentChapterActivityType != 3) || (placeOfOrigin != 5))
				{
					Log.w(TAG5, "Transition problem! Not \"from 5 to 3\"...");
					
					exitTheApplication();
				}
				else
				{
					// We are in a chapteractivity, so set the layout accordingly. For the chapter, this
					// is abstracted into a method, to avoid repeating the lengthy code segment multiple
					// times throughout the switch statement.
					//
					setChapterContentView(MetaDataForSuite.currentAspectRatio);
				
					// We do need to scroll, since we wish to reacquire the exact y-coordinate
					// from which we previously departed this chapter.
					//
					needToScroll = 1;
				
					// We don't need to reload, since no href has been used to access the current 
					// page.
					//
					needToReload = 0;
				}

				break;
			
			case 700:
				// The last place of departure was an audio activity, and we have returned to the chapter
				// activity via the back-button of the diagram activity.
				//
				// Make sure the points of origin and destination are as assumed.
				//
				if ((currentChapterActivityType != 3) || (placeOfOrigin != 7))
				{
					Log.w(TAG5, "Transition problem! Not \"from 7 to 3\"...");
					
					exitTheApplication();
				}
				else
				{
					// We are in a chapteractivity, so set the layout accordingly. For the chapter, this
					// is abstracted into a method, to avoid repeating the lengthy code segment multiple
					// times throughout the switch statement.
					//
					setChapterContentView(MetaDataForSuite.currentAspectRatio);
				
					// We don't need to scroll.
					//
					needToScroll = 0;
				
					// We do need to reload, since a hashed href has been used to access the current 
					// page.
					//
					needToReload = 1;
				}

				break;
				
			case 800:
				// The last place of departure was an animation activity, and we have returned to the chapter
				// activity via the back-button of the diagram activity.
				//
				// Make sure the points of origin and destination are as assumed.
				//
				if ((currentChapterActivityType != 3) || (placeOfOrigin != 8))
				{
					Log.w(TAG5, "Transition problem! Not \"from 8 to 3\"...");
					
					exitTheApplication();
				}
				else
				{
					// We are in a chapteractivity, so set the layout accordingly. For the chapter, this
					// is abstracted into a method, to avoid repeating the lengthy code segment multiple
					// times throughout the switch statement.
					//
					setChapterContentView(MetaDataForSuite.currentAspectRatio);
				
					// We don't need to scroll.
					//
					needToScroll = 0;
				
					// We do need to reload, since a hashed href has been used to access the current 
					// page.
					//
					needToReload = 1;
				}

				break;
				
			case 900:
				// The last place of departure was a chapter, and we have started a diagram activity by
				// means of clicking on an href-icon, for image-magnification purposes.
				//
				// Make sure the points of origin and destination are as assumed.
				//
				if ((currentChapterActivityType != 5) || (placeOfOrigin != 3))
				{
					Log.w(TAG5, "Transition problem! Not \"from 3 to 5\"...");
					
					exitTheApplication();
				}
				else
				{
					// We are in a diagram activity.
					//
					// Establish the content view for the diagram activity. Do this conditionally, based on the 
					// screen-size/aspect-ratio combination we have already determined.
					//
					switch (MetaDataForSuite.currentAspectRatio)
					{
						// This is a 480x800 4.3" device.
						//
						case 0:
							setContentView(R.layout.diagram_activity_for_zero___four_point_three);
							Log.w(TAG0, "Set Diagram content for 0.\n");
							break;
							
						// This is a 540x960 4.0" to 4.8" device.
					    //
						case 10:
							setContentView(R.layout.diagram_activity_for_one_zero___four_point_zero_etc);
							Log.w(TAG0, "Set Diagram content for 10.\n");
							break;
							
						// This is a 720x1280 4.3" to 5.0" device.
					    //
						case 20:
							setContentView(R.layout.diagram_activity_for_two_zero___four_point_three_etc);
							Log.w(TAG0, "Set Diagram content for 20.\n");
							break;
							
						// This is a 768x1280 4.7" device.
					    //
						case 30:
							setContentView(R.layout.diagram_activity_for_three_zero___four_point_seven);
							Log.w(TAG0, "Set Chapter toc content for 30.\n");
							break;
							
						// This is a 800x1280 7" device.
					    //
						case 40:
							setContentView(R.layout.diagram_activity_for_four_zero___seven);
							Log.w(TAG0, "Set Diagram content for 40.\n");
							break;
							
						// This is a 800x1280 10.1" device.
					    //
						case 50:
							setContentView(R.layout.diagram_activity_for_five_zero___ten);
							Log.w(TAG0, "Set Diagram content for 50.\n");
							break;
					
					    // This is a 1080x1920 generic-size device.
						//
						case 60:
							setContentView(R.layout.diagram_activity_for_six_zero___gen);
							Log.w(TAG0, "Set Diagram content for 60.\n");
							break;
						
						// This is a 1080x1920 5.7 inch device.
						//
						case 70:
							setContentView(R.layout.diagram_activity_for_seven_zero___five_point_seven);
							Log.w(TAG0, "Set Diagramc content for 70.\n");
							break;
						
						// This is a 1200x1920 7 inch device.
						//
						case 80:
							setContentView(R.layout.diagram_activity_for_eight_zero___seven);
							Log.w(TAG0, "Set Diagram content for 80.\n");
							break;
						
						// This is a 1200x1920 10 inch device.
						//
						case 90:
							setContentView(R.layout.diagram_activity_for_nine_zero___ten);
							Log.w(TAG0, "Set Diagram content for 90.\n");
							break;
							
						// This is a 1440x900 7 inch device.
						//
						case 100:
							setContentView(R.layout.diagram_activity_for_one_zero_zero___seven);
							Log.w(TAG0, "Set Diagram content for 100.\n");
							break;

						// This is a 1440x2560 5.1 inch device.
						//
						case 110:
							setContentView(R.layout.diagram_activity_for_one_one_zero___five_point_one);
							Log.w(TAG0, "Set Diagram content for 110.\n");
							break;
						
						// If we are not sure, we use the following default.
						//
						default:
							setContentView(R.layout.diagram_activity_for_six_zero___gen);
							Log.w(TAG0, "Set Diagram content for default.\n");
							break;			
					}
				
					// We don't need to scroll, since we just go to the top of the diagram.
					//
					needToScroll = 0;
				
					// We don't need to reload, since we just go to the top of the diagram.
					//
					needToReload = 0;
				}

				break;
				
			default:
				
				Log.w(TAG5, "There was a problem with establishing the transition profile. Exiting...\n");
				
				exitTheApplication();
				
				break;
		}
	}
	
	/**
	 * Sets up the WebView for use with the Chapter Activity. This includes establishing use
	 * of the appropriate file-access prototol, selection of the webview object within the
	 * layout file, and viewport settings. Conditions for reloading (to permit automated
	 * seeking of specific subsections, in accordance with href specifications) and scrolling
	 * (to seek specific, mid-page y-coordinates on returns from diagrams and chapter tables-of-contents. 
	 * 
	 * @param wv The unconfigured WebView object for the current activity.
	 * 
	 * @return The appropriately configured WebView object for the current activity.
	 * 
	 */
	@SuppressWarnings("deprecation")
	private WebView establishWebViewSettings(WebView wv)
	{
		// Now, for chapter activities of all kinds, determine the right protocol
		// for accessing source documents.
		//
		if (MetaDataForSuite.Connectivity == 1)
		{
			if(URLstring == null)
			{
				Log.w(TAG6, "ERROR: URLSTRING IS NULL...");
			}
			
			// Make sure that, in the event of the contents being on the SD card, the file
			// access protocol will be followed.
			//
			if (URLstring.contains("file://"))
			{
				// Do nothing
			}
			else
			{
				URLstring = "file://" + URLstring;
			}
		}

		// Establish the webview object, defined within the layout file.
		//
		if (currentChapterActivityType == 5)
		{
			// For the diagram activity.
			//
			wv = (WebView) findViewById(R.id.my_webviewd);
		}
		else
		{
			// For all other activity-types.
			//
			wv = (WebView) findViewById(R.id.my_webview);
		}
	
		// Establish viewPort settings for the webview display.
		//
		wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setUseWideViewPort(true);
		wv.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

		// Make a record of the initial scale we'll use. Note that scale maximum and
		// minimum are set within the html file to be displayed.
		//
		initialScale = wv.getScale();
		
		wv = setTheWebViewClient(wv);
		
		return wv;
	}
	
	/**
	 * Implements two critical methods to be employed by the WebView for the current
	 * activity. The first is "shouldOverrideUrlLoading", which allows interception of
	 * user-clicks on hypertext references, so that appropriate settings can be made,
	 * including the instantiation and dispatch of an intent. The second is "onPageFinished",
	 * which is invoked by the Android subsystem whenever a page-load has completed; the
	 * implementation for this activity allows the reload to reposition the page according
	 * to a given y-coordinate, so that we are in the middle of the file.
	 * 
	 * @param wv The WebView object for the current activity.
	 * 
	 * @return The WebView object for the current activity, with methods instantated.
	 * 
	 */
	private WebView setTheWebViewClient(WebView wv)
	{
		// Set up the web view client. This involves implementing two critical methods.
		//
		// The first is "onPageFinished", which is run following the initial load of any
		// url specified. It allows various adjustments to be made. These prominently include
		// (a) reloading the page, which is required in order to get Android to respect href
		// tags, such as take the user to a given subsection within the chapter - if no reload
		// occurs, we remain at the top of the page; and (b) scrolling to a specified y-coordinate,
		// so as to reacquire a position in a chapter we are returning to.
		// 
		// The second is "shouldOverrideUrlLoading". When we implement a button for the user to
		// click on, we are obviously empowered to make anything we want happen, in connection
		// with that click. We do not have that luxury with hrefs, because they carry no code
		// peculiar to them. To compensate, "shouldOverrideUrlLoading" allows us to intercept
		// the user's click on an href, and make some decisions in correspondence with what is
		// going to be loaded. For example, we can look at the url of the reference, decide whether
		// it is a diagram or a chapter, and set things based on that knowledge.
		//
		wv.setWebViewClient(new WebViewClient() 
		{
			// The method shouldOverrideUrlLoading is called when the user left-clicks
			// on a hypertext reference. It examines the url and makes decisions based
			// on what it finds. There are two circumstances in which it is called. First,
			// the user is in a chapter-toc, and clicks on a reference to go back to the
			// main chapter. Secondly, the user is in a main chapter, and clicks on an icon,
			// in order to examine the magnification of an image.
			//
			/**
			 * Determines whether a user-clicked hypertext reference contains either a 
			 * reference to an html file, or to a graphics file. If to an html file, the chapter
			 * number is resolved by inspection of the url, and an intent dispatched, to start
			 * a new Chapter Activity, using the url. If to a graphics file, an intent is
			 * likewise prepared: in addition, a record is made of the y-coordinate occupied by
			 * the user at the time of the click; this position can thus be returned to later.
			 * 
			 */
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) 
			{
				Log.w(TAG7, "SHOULD OVERRIDE STARTING NOW...");
	
				// Since the user's clicking on an href is taking us elsewhere, make a record 
				// of the current Y-coordinate. We may wish to return to it.
				//
				YvalueLastDepartedFrom = view.getScrollY();
				
				// If this is the current chapter, just load the page. NOTE: IS THIS STILL
				// REQUIRED? FIX.
				//
				if (url.equals(URLstring))
				{
					view.loadUrl(url);
				}
				else
				{
					if (url.contains(".htm"))
					{
						// The user has clicked on a toc href that takes us back to the
						// main chapter. So, restart the current activity with that chapter.
						// 
						// In cases where we restart the intent, this value seems to persist. So,
						// make sure it is nulled out each time.
						chapterNumberString = "";
	
						Intent intent = null;
						intent = new Intent(ChapterActivity.this, ChapterActivity.class);
						
						// By examining the URL, figure out the number of the main chapter
						// to which we are going (which is the chapter from which we 
						// previously accessed the current chapter-toc.
						//
						for (int i = 0; i <= (url.length() - 1); i++)
						{
							if (    
									(url.charAt(i) == 'm')     && 
									(url.charAt(i + 1) == 'l') &&
									(url.charAt(i + 2) == '/') &&
									(url.charAt(i + 3) == 'c') &&
									(url.charAt(i + 4) == 'h')
								)
							{
								char firstChar = url.charAt(i + 7);
								char secondChar = url.charAt(i + 8);
								
								chapterNumberString = chapterNumberString + firstChar;
								chapterNumberInt = Integer.parseInt(chapterNumberString);
								
								if (secondChar == '0' ||
										secondChar == '1' ||
										secondChar == '2' ||
										secondChar == '3' ||
										secondChar == '4' ||
										secondChar == '5' ||
										secondChar == '6' ||
										secondChar == '7' ||
										secondChar == '8' ||
										secondChar == '9'
									)
								{
									chapterNumberString = chapterNumberString + secondChar;
									chapterNumberInt = Integer.parseInt(chapterNumberString);
								}
							}	
						}
						
						// Once we have determined the number, put it in the intent we
						// are calling. It will be used to set up the decoration.
						//
						intent.putExtra("intVar", chapterNumberInt - 1);
						
						intent.putExtra("urlOfChapter", url);
						
						// Specify that our departure is 4, and our target 3, meaning Welcome Page and
						// prior activity, respectively. This means a transition profile of 500. See
						// the Chapter Activity for a full description of the numbering system.
						//
						intent.putExtra("placeOfDeparture", 4);
				    	intent.putExtra("targetChapterActivityType", 3);
				    	intent.putExtra("transitionProfile", 500);
						
						Log.w(TAG7, "Starting activity with url of " + url + '\n');
						
						// Start the intent, to take us back from the toc to the main chapter.
						//
						startActivity(intent);
						
						// Destroy the toc activity we are leaving.
						//
						finish();
					}
					
					// Alternatively, we may be going from a main chapter to a diagram.
					// Determine this by looking at the url, and specifically at the
					// suffix of the filename.
					//
					else
					{
						if (url.contains(".png") || url.contains(".jpg") || url.contains(".gif"))
						{	
							// Prepare to make a copy of the y-coordinate we are leaving from, since
							// we want to reacquire it when we return to the standard chapter activity
							// from the diagram. We need to make adjustments for scale, scaling down,
							// if necessary, the current y-coordinate, so it gets saved in terms of
							// the initial, default scale for the page, prior to any use-magnification
							// of the text.
							//
							// Get the current scale.
							//
							@SuppressWarnings("deprecation")
							float currentScale = view.getScale();
							
							// Transform the y-coordinate.
							//
							float newY = (float) YvalueLastDepartedFrom * ( (initialScale) / currentScale);
							
							// Make a copy to be retrieved on our return.
							//
							YvalueLastDepartedFrom = (int) newY;
	
							// Establish the layout and the webview for the diagram activity.
							//
							//setContentView(R.layout.diagramactivity);
							//WebView wv = (WebView) findViewById(R.id.my_webviewd);
							
							Intent intent = null;
							intent = new Intent(ChapterActivity.this, ChapterActivity.class);
							
							// Once we have determined the number, put it in the intent we
							// are calling. It will be used to set up the decoration. Use the
							// original ID number, as came from the ListView.
							//
							intent.putExtra("intVar", currentchapter - 1);
							
							intent.putExtra("urlOfChapter", url);
							
							// Specify that our departure is 3, and our target 5, meaning chapter-text and
							// diagram, respectively. This means a transition profile of 900.
							//
							intent.putExtra("placeOfDeparture", 3);
					    	intent.putExtra("targetChapterActivityType", 5);
					    	intent.putExtra("transitionProfile", 900);
					    	
					    	// Start the diagram-type Chapter Activity.
					    	//
					    	startActivity(intent);
					    	
					    	// Destroy the standard activity we are leaving.
							//
							finish();
						}
					}
				}
				
				return true;
			}
			
			/**
			 * Whenever the current page has completed being loaded into the browser, checks
			 * whether the url contains a hashed hypertext reference; and, if it does, ensures
			 * the page is reloaded, so as to be oriented to the correct reference. If there
			 * is no such reference, checks whether the transition profile requires scrolling,
			 * and if it does, calls the scrolling facility to an appropriate y-coordinate.
			 * 
			 */
			public void onPageFinished(final WebView view, String url) 
			{
				// Flag to the LogCat that we are in the onPageFinished section.
				//
				Log.w(TAG8, "ON PAGE FINISHED starting");
			
				// Check whether we are loading with a has sign and subsection reference. If we
				// are, we need to do a reload, to ensure we don't get stuck at the top of
				// the page.
				//
				if (url.contains("#"))
				{
					// Load the url again, and set the view to visible. This takes us to the subsection
					// contained in the url.
					//
					if (needToReload == 1)
					{
						view.loadUrl(url);
						view.setVisibility(View.VISIBLE);
						
						// Flag that we no longer need to reload, since we indeed just did
						// so. This ensures that we reload to the hashed href just once. Any
						// subsequent calling of "onPageFinished" (by the Android platform)
						// will just take us to whatever is the natural current y-coordinate,
						// and so will essentially be invisible to us.
						//
						needToReload = 0;
					}
					
					// If no need to reload has been flagged, don't reload.
					//
					else
					{
						Log.w(TAG8, "No reload needed, since unflagged.\n");
					}
				}
				
				// If there's no hash in the URL, no need to reload, since there's
				// no subsection to go to.
				//
				else
				{
					Log.w(TAG8, "No reload required, since there's no hash in the URL.\n");
				}	
				
				// However, we do need to determine whether we need to scroll to a y-coordinate 
				// greater than zero.
				//	
				// If we are returning from a diagram or from a toc in which we
				// elected to return to our starting point via the back-button, and
				// the current activity is (as it should be) a chapter activity, and
				// "needToScroll" has duly been set to 1, scroll to the last Y position
				// of this chapter that the user occupied before transitioning to 
				// the diagram or toc.
				//
				if (
						(placeOfOrigin == 5 || placeOfOrigin == 4) && // Have returned from a diagram or toc.
						 currentChapterActivityType == 3 && 		  // Have arrived in a chapter.
						 needToScroll == 1							  // Have already recognized a need to scroll.
				   )		
				{	
					// Make the value of the y-coordinate equal to what it was when the user was
					// last in this chapter, prior to looking at the diagram or toc.
					//
					y = YvalueLastDepartedFrom;
					
					// Perform the corresponding scroll, and flag this to the LogCat.
					//
					// Note that we are never interested in the x coordinate, which will always be
					// 0. NOTE: THIS IS THE ONE TIME WHERE WE SIMPLY REFRESH THE CONTENT WITHIN
					// THE BROWSER, RATHER THAN FIRING UP ANOTHER, VIA AN INTENT. SUGGEST WE
					// BE CONSISTENT, AND SO CHANGE THIS. THE Y COORDINATE COULD THUS BE
					// PASSED WITHIN THE INTENT, WHICH WOULD PROBABLY MAKE THINGS CLEANER,
					// SINCE WE WOULDN'T BE RELYING ON VARIABLES WITHIN THE ACTIVITY: WE'D BE
					// PASSING THEM TO THE NEW INVOCATION. FIX.
					//
					Log.w(TAG0, "Scrolling now...");
					view.scrollTo(x, y);
					
					// Register that there's no longer any need to scroll, because we've indeed just
					// done it.
					//
					needToScroll = 0;
				}
				
				// WORKAROUND (FIX?)
				//
				// If we are not returning from a diagram or toc such that scrolling is required, then we
				// can just go to the top, which is to say, x and y are both 0.
				//
				// However, we face a problem, in that onPageFinished sometimes gets called multiple times
				// by the system, possibly due to the timing and synchronicities of the page-load process.
				// This can result, again, in the loss of a y-coordinate that we've established, and a return
				// to 0 that we don't want. 
				//
				// Therefore, we'll assume that whenever our record of an old Y-value is greater than zero, it
				// is indeed there we wish to be, and we'll insist that every subsequent re-calling of this
				// method respect that. Note that we only need to do this in a regular chapter
				// activity.
				//
				else
				{
					// Only do this if we are in a standard-type Chapter Activity. We don't
					// want to do it for the TOC. 
				    //
					if (currentChapterActivityType == 3)
					{
						Log.w(TAG0, "Checking scroll position now...");
						// Check to see where we now are, first grabbing the current x and y values.
						//
						x = view.getScrollX();
						y = view.getScrollY(); 
					
						// If y is less than an outstanding value of YvalueLastDepartedFrom, fix the problem, and
						// again scroll down the page.
						//
						if ((y == 0) && (y < YvalueLastDepartedFrom))
						{
							y = YvalueLastDepartedFrom;
							view.scrollTo(x, y); 
						}
					}
				}
			}
			
			// onPageFinished returns no value.
		});
		
		return wv;
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
	public boolean showPopUpMenu(View v)    
	{	
		// Establish the control button at the upper right as a view, such
		// that a popup menu can be associated with it.
		//
		View cb = findViewById(R.id.contents_button);
		
		// Create a popup-menu instance.
		//
        PopupMenu popup = new PopupMenu(ChapterActivity.this, cb); 
        
        // Instantiate the popup menu by inflating the appropriate xml
        // description file.
        //
        popup.getMenuInflater().inflate(R.menu.chapter_toc_or_search, popup.getMenu());  

        // Register the popup menu with OnMenuItemClickListener, so that actions
        // can be taken when menu-items are selected by the user.
        //
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() 
        {  
        	// When the user makes a selection from the popup menu...
        	//
        	public boolean onMenuItemClick(MenuItem item) 
        	{  
        		// Either show the toc for the current chapter...
        		//
        		if (item.getTitle().equals("Table of Contents for this chapter"))
        		{
        			showTOCforCurrentChapter(wv);	
        		}
        		
        		// Or initiate text-search.
        		//
        		else
        		{
        			if (item.getTitle().equals("Text-Search for this chapter"))
            		{
        				// Call the search method, to start text-search procedures.
        				//
            			search(wv);
            			
            			// Check the following global variable, to determine whether this is
            			// the first time during the current session that the user has employed
            			// textual search. If it is, bring up an explanatory toast. Otherwise,
            			// don't.
            			//
            			if (MetaDataForDocument.searchYetEmployed == 0)
            			{
            			
	            			MetaDataForDocument.searchYetEmployed = 1;
	            			
	            			Toast toast= Toast.makeText(getApplicationContext(), 
	            										"Enter text, then hit \"Next\", on the soft keyboard. " + 
	            										"Then, for repeat searches, use the \"Repeat\" button, " +
	            										"at the top.", 
	            										Toast.LENGTH_LONG);  
	            			
	            			toast.setGravity(Gravity.CENTER_VERTICAL , 0, 0);
	            			
	            			toast.show();
            			}
            			else
            			{
            				// Do nothing.
            			}
            		}
        		}
        		
        		return true;  
        	}  
        });  

        // Display the popup menu.
        //
        popup.show();
        
        return true;
	}

	/**
	 * Provides graphical facilities for textual search of the current chapter: these are
	 * Find, which is an editable text field, into which the user types a string to be 
	 * searched for; Repeat, which causes a search to be repeated on the current chapter; and
	 * Close, which dismisses all the search facilities. When visible, the facilities occupy
	 * the space typically occupied by the title-bar for the current chapter.
	 * 
	 * @param v The current view.
	 */
    public void search(View v)
    {   
    	// Create a handle on the webview that contains the text on which searches
    	// will be conducted.
    	//
    	wv = (WebView) findViewById(R.id.my_webview);
    	
    	// Create a handle on the linear layout that contains the graphical utilities for
    	// the search facility.
    	//
        container = (LinearLayout)findViewById(R.id.searchbox);   
        
        // Since the search graphical utilities occupy the same space as the title-bar,
        // make the title-bar and its associated control button invisible for the duration
        // of the search interactions.
        //
		View tv = findViewById(R.id.my_textview);
		tv.setVisibility(View.INVISIBLE);
		
		View cb = findViewById(R.id.contents_button);
		cb.setVisibility(View.INVISIBLE);
		
		// Create a button whereby a search can be repeated.
		//
	    repeatButton = new Button(this);    
        repeatButton.setText("Repeat"); 
        LinearLayout.LayoutParams nextParams = null;
        
        // The size of the text, the padding around the text, the minimum height of the
        // button itself, and the margins around the button are all specified as integer
        // resources. So, retrieve these, and establish the settings.
        //   
        // Establish button characteristics according to the aspect ratio and screen size of the
        // underlying platform.
        //
        switch (MetaDataForSuite.currentAspectRatio)
		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_zero___four_point_three));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_zero___four_point_three),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_zero___four_point_three),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_zero___four_point_three),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_zero___four_point_three));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_zero___four_point_three));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_zero___four_point_three));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_zero___four_point_three),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_zero___four_point_three),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_zero___four_point_three),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_zero___four_point_three)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 0.\n");
				break;
		
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_one_zero___four_point_zero_etc));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_one_zero___four_point_zero_etc),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_one_zero___four_point_zero_etc),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_one_zero___four_point_zero_etc),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_one_zero___four_point_zero_etc));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_one_zero___four_point_zero_etc));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_one_zero___four_point_zero_etc));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_one_zero___four_point_zero_etc),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_one_zero___four_point_zero_etc),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_one_zero___four_point_zero_etc),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_one_zero___four_point_zero_etc)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_two_zero___four_point_three_etc));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_two_zero___four_point_three_etc),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_two_zero___four_point_three_etc),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_two_zero___four_point_three_etc),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_two_zero___four_point_three_etc));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_two_zero___four_point_three_etc));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_two_zero___four_point_three_etc));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_two_zero___four_point_three_etc),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_two_zero___four_point_three_etc),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_two_zero___four_point_three_etc),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_two_zero___four_point_three_etc)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 20.\n");
				break;
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_three_zero___four_point_seven));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_three_zero___four_point_seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_three_zero___four_point_seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_three_zero___four_point_seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_three_zero___four_point_seven));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_three_zero___four_point_seven));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_three_zero___four_point_seven));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_three_zero___four_point_seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_three_zero___four_point_seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_three_zero___four_point_seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_three_zero___four_point_seven)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_four_zero___seven));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_four_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_four_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_four_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_four_zero___seven));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_four_zero___seven));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_four_zero___seven));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_four_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_four_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_four_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_four_zero___seven)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_five_zero___ten));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_five_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_five_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_five_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_five_zero___ten));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_five_zero___ten));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_five_zero___ten));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_five_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_five_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_five_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_five_zero___ten)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 50.\n");
				break;
				
		    // This is a 1080x1920 generic-size device.
			//
			case 60:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_six_zero___gen));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_six_zero___gen));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_six_zero___gen));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_six_zero___gen));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_six_zero___gen)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 60.\n");
				break;

			// This is a 1080x1920 5.7 inch device.
			//
			case 70:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_seven_zero___five_point_seven));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_seven_zero___five_point_seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_seven_zero___five_point_seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_seven_zero___five_point_seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_seven_zero___five_point_seven));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_seven_zero___five_point_seven));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_seven_zero___five_point_seven));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_seven_zero___five_point_seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_seven_zero___five_point_seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_seven_zero___five_point_seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_seven_zero___five_point_seven)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 70.\n");
				break;
			
			// This is a 1200x1920 7 inch device.
			//
			case 80:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_eight_zero___seven));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_eight_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_eight_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_eight_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_eight_zero___seven));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_eight_zero___seven));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_eight_zero___seven));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_eight_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_eight_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_eight_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_eight_zero___seven)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 80.\n");
				break;
			
			// This is a 1200x1920 10 inch device.
			//
			case 90:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_nine_zero___ten));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_nine_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_nine_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_nine_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_nine_zero___ten));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_nine_zero___ten));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_nine_zero___ten));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_nine_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_nine_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_nine_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_nine_zero___ten)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 90.\n");
				break;
				
			// This is a 1440x900 7 inch device.
			//
			case 100:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_one_zero_zero___seven));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_one_zero_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_one_zero_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_one_zero_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_one_zero_zero___seven));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_one_zero_zero___seven));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_one_zero_zero___seven));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_one_zero_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_one_zero_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_one_zero_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_one_zero_zero___seven)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for 100.\n");
				break;

			// This is a 1440x2560 5.1 inch device.
			//
			case 110:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_one_one_zero___five_point_one));
				repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_top_padding_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_right_padding_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_bottom_padding_for_one_one_zero___five_point_one));

				repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
				repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_one_one_zero___five_point_one));
				repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_one_one_zero___five_point_one));
				repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));

				nextParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
				);

				nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_top_margin_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_right_margin_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_bottom_margin_for_one_one_zero___five_point_one)
				);

				repeatButton.setLayoutParams(nextParams);

				Log.w(TAG0, "Set Repeat Button specifics for 110.\n");
				break;
			
			// If we are not sure, we use the following default.
			//
			default:
				repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_six_zero___gen));
		        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_six_zero___gen));
		        
		        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_six_zero___gen));
		        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_six_zero___gen));
		        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        nextParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        nextParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_six_zero___gen)
		        );
		        
		        repeatButton.setLayoutParams(nextParams);
		        
				Log.w(TAG0, "Set Repeat Button specifics for default.\n");
				break;			
		}
        
        // Make the Repeat button initially invisible. It must only appear after the first
        // search has concluded; the first search having been initiated by means of the Next
        // button on the virtual keyboard. NOTE: IS THERE A WAY OF KEEPING THE KEYBOARD OUT
        // OF THE SEARCH LOOP, AND JUST USING IT FOR TEXT-ENTRY ONLY? FIX.
        //
        View nb = repeatButton;
        nb.setVisibility(View.INVISIBLE);
        
        // Make the Repeat button unclickable for now: we'll make it clickable later,
        // when it appears.
        //
        repeatButton.setClickable(false);
            
        // When the Repeat button is pressed, search for the specified text string.
        //  
        repeatButton.setOnClickListener(new OnClickListener()
        {   
    		@Override  
    		public void onClick(View v) 
    		{   	
    			wv.findNext(true);   
    		}   
        });   
        
        // Now create a button for Closing the search facility. The procedure is near-identical
        // to that of the Repeat button, above. Note, however, that the Close button, along
        // with the text-input field, is always visible, for as long as the search facility
        // is in the foreground. The Close button, indeed, terminates it and backgrounds
        // the UI resources.
        //
        closeButton = new Button(this);   
        closeButton.setText("Close");  
        LinearLayout.LayoutParams closeParams = null;

        // Establish button characteristics according to the aspect ratio and screen size of the
        // underlying platform.
        //
        switch (MetaDataForSuite.currentAspectRatio)
		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_zero___four_point_three));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_zero___four_point_three),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_zero___four_point_three),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_zero___four_point_three),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_zero___four_point_three));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_zero___four_point_three));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_zero___four_point_three));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_zero___four_point_three),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_zero___four_point_three),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_zero___four_point_three),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_zero___four_point_three)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 0.\n");
				break;
		
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_one_zero___four_point_zero_etc));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_one_zero___four_point_zero_etc),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_one_zero___four_point_zero_etc),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_one_zero___four_point_zero_etc),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_one_zero___four_point_zero_etc));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_one_zero___four_point_zero_etc));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_one_zero___four_point_zero_etc));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_one_zero___four_point_zero_etc),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_one_zero___four_point_zero_etc),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_one_zero___four_point_zero_etc),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_one_zero___four_point_zero_etc)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_two_zero___four_point_three_etc));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_two_zero___four_point_three_etc),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_two_zero___four_point_three_etc),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_two_zero___four_point_three_etc),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_two_zero___four_point_three_etc));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_two_zero___four_point_three_etc));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_two_zero___four_point_three_etc));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_two_zero___four_point_three_etc),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_two_zero___four_point_three_etc),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_two_zero___four_point_three_etc),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_two_zero___four_point_three_etc)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 20.\n");
				break;
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_three_zero___four_point_seven));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_three_zero___four_point_seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_three_zero___four_point_seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_three_zero___four_point_seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_three_zero___four_point_seven));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_three_zero___four_point_seven));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_three_zero___four_point_seven));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_three_zero___four_point_seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_three_zero___four_point_seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_three_zero___four_point_seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_three_zero___four_point_seven)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_four_zero___seven));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_four_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_four_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_four_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_four_zero___seven));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_four_zero___seven));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_four_zero___seven));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_four_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_four_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_four_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_four_zero___seven)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_five_zero___ten));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_five_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_five_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_five_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_five_zero___ten));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_five_zero___ten));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_five_zero___ten));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_five_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_five_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_five_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_five_zero___ten)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 50.\n");
				break;
				
					
		    // This is a 1080x1920 generic-size device.
			//
			case 60:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_six_zero___gen));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_six_zero___gen),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_six_zero___gen));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_six_zero___gen));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_six_zero___gen));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_six_zero___gen),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_six_zero___gen)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 60.\n");
				break;

			// This is a 1080x1920 5.7 inch device.
			//
			case 70:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_seven_zero___five_point_seven));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_seven_zero___five_point_seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_seven_zero___five_point_seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_seven_zero___five_point_seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_seven_zero___five_point_seven));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_seven_zero___five_point_seven));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_seven_zero___five_point_seven));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_seven_zero___five_point_seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_seven_zero___five_point_seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_seven_zero___five_point_seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_seven_zero___five_point_seven)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 70.\n");
				break;
			
			// This is a 1200x1920 7 inch device.
			//
			case 80:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_eight_zero___seven));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_eight_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_eight_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_eight_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_eight_zero___seven));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_eight_zero___seven));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_eight_zero___seven));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_eight_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_eight_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_eight_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_eight_zero___seven)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 80.\n");
				break;
			
			// This is a 1200x1920 10 inch device.
			//
			case 90:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_nine_zero___ten));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_nine_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_nine_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_nine_zero___ten),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_nine_zero___ten));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_nine_zero___ten));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_nine_zero___ten));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_nine_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_nine_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_nine_zero___ten),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_nine_zero___ten)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 90.\n");
				break;
				
			// This is a 1440x900 7 inch device.
			//
			case 100:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_one_zero_zero___seven));
		        closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_one_zero_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_top_padding_for_one_zero_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_right_padding_for_one_zero_zero___seven),
		        					   getResources().getInteger(R.integer.search_button_bottom_padding_for_one_zero_zero___seven));
		        
		        closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
		        closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_one_zero_zero___seven));
		        closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_one_zero_zero___seven));
		        closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));
		        
		        closeParams = new LinearLayout.LayoutParams(
		        		LinearLayout.LayoutParams.WRAP_CONTENT,      
		        		LinearLayout.LayoutParams.WRAP_CONTENT
		        );
		        
		        closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_one_zero_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_top_margin_for_one_zero_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_right_margin_for_one_zero_zero___seven),
						   			   getResources().getInteger(R.integer.search_button_bottom_margin_for_one_zero_zero___seven)
		        );
		        
		        closeButton.setLayoutParams(closeParams);
		        
				Log.w(TAG0, "Set Close Button specifics for 100.\n");
				break;

			// This is a 1440x900 7 inch device.
			//
			case 110:
				closeButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size_for_one_one_zero___five_point_one));
				closeButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_top_padding_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_right_padding_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_bottom_padding_for_one_one_zero___five_point_one));

				closeButton.setTextColor(getResources().getColor(R.color.search_button_text));
				closeButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height_for_one_one_zero___five_point_one));
				closeButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width_for_one_one_zero___five_point_one));
				closeButton.setBackgroundColor(getResources().getColor(R.color.search_button));

				closeParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
				);

				closeParams.setMargins(getResources().getInteger(R.integer.search_button_left_margin_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_top_margin_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_right_margin_for_one_one_zero___five_point_one),
						getResources().getInteger(R.integer.search_button_bottom_margin_for_one_one_zero___five_point_one)
				);

				closeButton.setLayoutParams(closeParams);

				Log.w(TAG0, "Set Close Button specifics for 110.\n");
				break;
			
			// If we are not sure, we use the following default.
			//
			default:
				
				break;			
		}
        
        // Establish a routine for when the Close button is pressed.
        //
        closeButton.setOnClickListener(new OnClickListener()
        {   
        	@SuppressWarnings("deprecation")
			@Override  
        	public void onClick(View v)
        	{   
        		// Make the UI search resources invisible.
        		//
        		container.removeAllViews();   
        		
        		// Remove the highlights from the words we searched
        		// for, so the text all has the original white background. The
        		// best way to do this, seemingly, is to conduct a text search on
        		// nothing! This just returns an unhighlighted webview. IS THERE
        		// A BETTER WAY OF DOING THIS? FIX.
        		//
        		wv.findAll(""); 
        		
        		// Restore the title-bar for the current activity to visibility.
        		//
        		View tv = findViewById(R.id.my_textview);
        		tv.setVisibility(View.VISIBLE);
        		
        		// Restore the control button also.
        		//
        		View cb = findViewById(R.id.contents_button);
        		cb.setVisibility(View.VISIBLE);
        		
        		// Get rid of the virtual keyboard. This often shouldn't be necessary, since we
        		// get rid of it after each initial search on newly inputted data. But it
        		// covers the case where we changed our minds, and didn't search at all.
        		//
        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    			imm.hideSoftInputFromWindow(wv.getWindowToken(), 0);
        	}   
        });   
        
        // Now create the facility for text-input. Searches are conducted on the value
        // retrieved here.
        // 
        //
        findBox = new EditText(this);   
        findBox.setSingleLine(true);   
        findBox.setHint("Search"); 
        findBox.setTextColor(getResources().getColor(R.color.search_box_text_color));
        
        // Establish the length (the Minimum Ems) of the text-input facility in accordance with the aspect ratio and 
        // screensize of the underlying platform.
        //
        // This is a 480x800 4.3" device.
     	//
		switch (MetaDataForSuite.currentAspectRatio)
		{
 			case 0:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_zero___four_point_three));
 				Log.w(TAG0, "Set min ems for 0.\n");
 				break;
 		
 			// This is a 540x960 4.0" to 4.8" device.
 		    //
 			case 10:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_one_zero___four_point_zero_etc));
 				Log.w(TAG0, "Set min ems for 10.\n");
 				break;
 				
			// This is a 720x1280 4.3" to 5.0" device.
 		    //
 			case 20:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_two_zero___four_point_three_etc));
 				Log.w(TAG0, "Set min ems for 20.\n");
 				break;
 				
			// This is a 720x1280 4.3" to 5.0" device.
 		    //
 			case 30:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_three_zero___four_point_seven));
 				Log.w(TAG0, "Set min ems for 30.\n");
 				break;
 				
			// This is a 800x1280 7" device.
		    //
			case 40:
				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_four_zero___seven));
				Log.w(TAG0, "Set min ems for 40.\n");
				break;	
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_five_zero___ten));
				Log.w(TAG0, "Set min ems for 50.\n");
				break;	
				
 		    // This is a 1080x1920 generic-size device.
 			//
 			case 60:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_six_zero___gen));
 				Log.w(TAG0, "Set min ems for 60.\n");
 				break;
 			
 			// This is a 1080x1920 5.7 inch device.
 			//
 			case 70:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_seven_zero___five_point_seven));
 				Log.w(TAG0, "Set min ems for 70.\n");
 				break;
 			
 			// This is a 1200x1920 7 inch device.
 			//
 			case 80:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_eight_zero___seven));
 				Log.w(TAG0, "Set min ems for 80.\n");
 				break;
 			
 			// This is a 1200x1920 10 inch device.
 			//
 			case 90:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_nine_zero___ten));
 				Log.w(TAG0, "Set min ems for 90.\n");
 				break;
 				
 			// This is a 1200x1920 10 inch device.
 			//
 			case 100:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_one_zero_zero___seven));
 				Log.w(TAG0, "Set min ems for 100.\n");
 				break;
 			
 			// If we are not sure, we use the following default.
 			//
 			default:
 				findBox.setMinEms(getResources().getInteger(R.integer.search_box_min_ems_for_six_zero___gen));
 				Log.w(TAG0, "Set min ems for default.\n");
 				break;			
 		}

        // Establish a routine for what happens when this field is accessed.
        //
	    findBox.setOnKeyListener(new OnKeyListener()
	    {   
	    	@SuppressWarnings("deprecation")
			public boolean onKey(View v, int keyCode, KeyEvent event)
	    	{   
	    		// If the user accesses this field and starts to make changes,
	    		// make the Repeat button invisible. This is because new data is
	    		// being entered, and we always use the virtual keyboard to conduct
	    		// the first search. Repeat is literally only for repeat searches.
	    		//
	    		if (findBox.hasFocus() && 
	    						((event.getAction() != KeyEvent.ACTION_DOWN) && 
	    						(keyCode != KeyEvent.KEYCODE_ENTER))
	    			)
	    		{
	    			View nb = repeatButton;
	    	        nb.setVisibility(View.INVISIBLE);
	    		}
	    		else
	    		{
	    			// Otherwise, if "enter" is pressed on the virtual keyboard, we are
	    			// conducting the first search on inputted data.
	    			//
		    		if((event.getAction() == KeyEvent.ACTION_DOWN) && 
		    					((keyCode == KeyEvent.KEYCODE_ENTER)))
		    		{   
		    			// Dismiss the virtual keyboard. We don't want it taking up space
		    			// while we are studying search-results and conducting repeat
		    			// searches.
		    			//
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    			imm.hideSoftInputFromWindow(wv.getWindowToken(), 0);
		    			
		    			// Perform search and associated activities. The decision here has been
		    			// to place these within a runnable, scheduled for initiation somewhat
		    			// after the dismissal of the virtual keyboard. This is because the Android
		    			// repaint of the activity, following the keyboard's dismissal, is visually
		    			// very messy. Sometimes, having the actual search happen instantaneously
		    			// results in more disorientation, so it's possibly good to delay it by
		    			// some number of milliseconds. Not a perfect solution. FIX.
		    			//
		    			Runnable notification = new Runnable()
	    				{
	    					public void run()
	    					{
	    						// Now that an initial search has been kicked off, we put the
	    						// Repeat button into play. Make most of its values identical to those
	    						// of the search button, to which it is adjacent on appearance.
	    						//
	    		    			repeatButton.setClickable(true);
	    		    			
	    		    			// NOTE: The following passage just repeats what was done earlier. I think its inclusion
	    		    			// was probably just a bug. I've commented it out for now, and am testing - but most
	    		    			// likely, it should just be removed. FIX.
	    		    			//
	    		    			/*
	    		    			repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
	    		    			repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
	    		    			
	    		    			repeatButton.setTextSize(getResources().getInteger(R.integer.search_button_text_size));
	    		    	        repeatButton.setPadding(getResources().getInteger(R.integer.search_button_left_padding),
	    		    	        					   getResources().getInteger(R.integer.search_button_top_padding),
	    		    	        					   getResources().getInteger(R.integer.search_button_right_padding),
	    		    	        					   getResources().getInteger(R.integer.search_button_bottom_padding));
	    		    	        
	    		    	        repeatButton.setTextColor(getResources().getColor(R.color.search_button_text));
	    		    	        repeatButton.setMinHeight(getResources().getInteger(R.integer.search_button_min_height));
	    		    	        repeatButton.setMinWidth(getResources().getInteger(R.integer.search_button_min_width));
	    		    	        repeatButton.setBackgroundColor(getResources().getColor(R.color.search_button));
	    		    	        */
	    		    			
	    		    			// Make the button visible.
	    		    			//
	    		    			View nb = repeatButton;
	    		    	        nb.setVisibility(View.VISIBLE);
	    		    	        
	    		    	        // Grab the user-entered string from the search text-field.
	    		    	        //
	    		    			wv.findAll(findBox.getText().toString());   
	    		      
	    		    			// Perform updates to the webview, consequent on search-results.
	    		    			//
	    		    			try
	    		    			{   
	    		    				Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);   
	    		    				m.invoke(wv, true);   
	    		    			}
	    		    			catch(Exception ignored)
	    		    			{
	    		    				// Do nothing
	    		    			}  
	    					}
	    				};
	    				
	    				// Perform this routine a little later than immediately after the dismissal of the
	    				// virtual keyboard, for the reasons given above.
	    				//
	    				long now = SystemClock.uptimeMillis();
	                    long next = now + (100);
	        			handler.postAtTime(notification, next);	
		    		}   
		    	}
	    		
	    		return false;
	    	}
	    });   
      
	    // Add the three UI elements we have created to the container. They will appear
	    // from left to right in the order give here.
	    //
	    container.addView(findBox); 
	    container.addView(repeatButton);
	    container.addView(closeButton); 
	    
	    // Ensure that as soon as the UI elements are visible, the findBox has
	    // focus, and the soft keyboard is visible. This allows the user immediately
	    // to start keying-in the text string to be searched for.
	    //
	    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(findBox.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        findBox.requestFocus();
    }   
    
    /**
     * Selects the layout appropriate for the Chapter Activity, in accordance with the dimensions
     * of the current device-screen.
     * 
     * @param aspectRatio Used to represent an aspect ratio, which is that of the current device-screen.
     * 
     */
    void setChapterContentView(int aspectRatio)
    {
    	// Establish the content view for the chapter activity. Do this conditionally, based on the 
		// screen-size/aspect-ratio combination we have already determined.
		//
		switch (aspectRatio)
		{
			// This is a 480x800 4.3" device.
			//
			case 0:
				setContentView(R.layout.chapter_activity_for_zero___four_point_three);
				Log.w(TAG0, "Set Chapter content for 0.\n");
				break;
			
			// This is a 540x960 4.0" to 4.8" device.
		    //
			case 10:
				setContentView(R.layout.chapter_activity_for_one_zero___four_point_zero_etc);
				Log.w(TAG0, "Set Chapter content for 10.\n");
				break;
				
			// This is a 720x1280 4.3" to 5.0" device.
		    //
			case 20:
				setContentView(R.layout.chapter_activity_for_two_zero___four_point_three_etc);
				Log.w(TAG0, "Set Chapter content for 20.\n");
				break;
				
			// This is a 768x1280 4.7" device.
		    //
			case 30:
				setContentView(R.layout.chapter_activity_for_three_zero___four_point_seven);
				Log.w(TAG0, "Set Chapter content for 30.\n");
				break;
				
			// This is a 800x1280 7" device.
		    //
			case 40:
				setContentView(R.layout.chapter_activity_for_four_zero___seven);
				Log.w(TAG0, "Set Chapter content for 40.\n");
				break;
				
			// This is a 800x1280 10.1" device.
		    //
			case 50:
				setContentView(R.layout.chapter_activity_for_five_zero___ten);
				Log.w(TAG0, "Set Chapter content for 50.\n");
				break;
					
		    // This is a 1080x1920 generic-size device.
			//
			case 60:
				setContentView(R.layout.chapter_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Chapter content for 60.\n");
				break;
			
			// This is a 1080x1920 5.7 inch device.
			//
			case 70:
				setContentView(R.layout.chapter_activity_for_seven_zero___five_point_seven);
				Log.w(TAG0, "Set Chapter content for 70.\n");
				break;
			
			// This is a 1200x1920 7 inch device.
			//
			case 80:
				setContentView(R.layout.chapter_activity_for_eight_zero___seven);
				Log.w(TAG0, "Set Chapter content for 80.\n");
				break;
			
			// This is a 1200x1920 10 inch device.
			//
			case 90:
				setContentView(R.layout.chapter_activity_for_nine_zero___ten);
				Log.w(TAG0, "Set Chapter content for 90.\n");
				break;
				
			// This is a 1440x900 7 inch device.
			//
			case 100:
				setContentView(R.layout.chapter_activity_for_one_zero_zero___seven);
				Log.w(TAG0, "Set Chapter content for 100.\n");
				break;

			// This is a 1440x2560 5.1 inch device.
			//
			case 110:
				setContentView(R.layout.chapter_activity_for_one_one_zero___five_point_one);
				Log.w(TAG0, "Set Chapter content for 110.\n");
				break;
			
			// If we are not sure, we use the following default.
			//
			default:
				setContentView(R.layout.chapter_activity_for_six_zero___gen);
				Log.w(TAG0, "Set Chapter content for default.\n");
				break;			
		}
    }
}
