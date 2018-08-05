package com.ueas.kpallv1g6;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

/**
 * Contains static definitions of all meta data to be used
 * by activities within the application, including titles,
 * and locations and durations of bitmap, animation, video,
 * and audio files.
 * 
 * @author tony.hillman@ultra-as.com
 *
 */
public class MetaDataForSuite 
{
	/**
	 * A prefix to be used in networked data-access.
	 * 
	 */
	static String currentServerAddress = "http://141.196.102.86";

	/**
	 * The locations of all documents.
	 * 
	 */
	static String[] allDocumentXMLfileLocations;
	
	/**
	 * The URL of the current chapter.
	 * 
	 */
	static String currentChapterURL;;
	
	/**
	 * All documents in the suite.
	 * 
	 */
	static String[][] allDocuments;
	
	/**
	 * All documents in the suite.
	 */
	static Document all_docs_doc_object;
	
	/**
	 * All documents in the suite.
	 * 
	 */
	static ArrayList<HashMap<String, String>> allDocumentsArrayList;
	
	/**
	 * Indicator of whether data-access is local or networked. A value of 0 indicates
	 * that the data is remotely located.
	 */
	static int Connectivity = 1;
	
	/**
	 * Locations of files for "prior" activities, such as Preface, Help, and About.
	 * 
	 */
	static String[][] prior_activity_files;
	
	/**
	 * Information on a selected animation.
	 * 
	 */
	static HashMap<String, String> mapForSelectedSong;
	
	/**
	 * Flag to indicate whether the goPortrait activity has yet been used.
	 * If 0, the activity has not yet been used, and may be used. If 1, it
	 * has been used, and so does not need to be used again.
	 * 
	 */
	static int goPortraitUsed = 0;
	
	/**
	 * Flag to indicate whether the goLandscape activity has yet been used.
	 * If 0, the activity has not yet been used, and may be used. If 1, it
	 * has been used, and so does not need to be used again.
	 * 
	 */
	static int goLandscapeUsed = 0;
	
	/**
	 * Value allocated to represent the aspect ratio of the
	 * current device. For example, if the value is 60, the associated
	 * aspect ratio is 1080x1920. If the value is 01, the aspect ratio is
	 * 540x960. Associations are arbitrary, and declared in the Login
	 * Activity. The default is 50, which signifies 800x1280.
	 * 
	 */
	static int currentAspectRatio = 50;
	
	/**
	 * Value allocated to represent the layout genus of the current
	 * aspect ratio, which is determined through a combination of the
	 * aspect ratio and the screen size. (Sometimes, a given aspect ratio
	 * is employed with more than one screen size, due to dots-per-inch
	 * implementation differences.) The default is 0.
	 * 
	 */
	static int currentLayoutGenus = 0;
	
	/**
	 * Value allocated to represent the html sizing required
	 * by the current platform. This may or may not be fixed with
	 * reference to the aspect ratio. (Sometimes, a given aspect ratio
	 * embraces more than one html sizing specification, due to variations
	 * in dots-per-inch and resulting screen-dimension.) The default is
	 * 30, which accords with 10.1" 800x1200 devices.
	 * 
	 */
	static int htmlSizingSpecification = 30;
}
