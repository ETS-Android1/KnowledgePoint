package com.ueas.kpallv1g6;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;

/**
 * Contains static definitions of meta data related to the
 * chapter list and the chapters. Values in the class are typically
 * set when a document is first selected by the user,
 * following authentication. Some elements re-represent data as
 * arrays for quick-access, once the first network-based access
 * has occurred.
 * 
 * @author tony.hillman@ultra-as.com
 *
 */
public class MetaDataForDocument 
{
	/**
	 * URL of the xml file containing the list of chapters.
	 */
	static String allChaptersURL;

	/**
	 * Array list to which the information at the allChaptersURL location can be converted and used as a 
	 * shortcut. At times when this has not been set, an application will access the allChaptersURL in the
	 * same way, which is more time consuming. The allChaptersArrayList will typically be set after the 
	 * first time that the allChaptersURL has been used.
	 * 
	 */
	static ArrayList<HashMap<String, String>> allChaptersArrayList;
	
	/**
	 * Array list containing information on the contents of the User Guide, for
	 * quick reference.
	 * 
	 */
	static ArrayList<HashMap<String, String>> userGuideChaptersArrayList;
	
	/**
	 * Record of whether documents other than the User Guide have been visited 
	 * during the current session. This is employed by certain activities that wish to
	 * provide quick-access to the User Guide, without traversal of the standard ListView
	 * mechanism being required.
	 * 
	 */
	static int whetherWidelyEstablished = 0;
	
	/**
	 * The chapters of the current document.
	 * 
	 */
	static String[][] allChapters;
	
	/**
	 * The chapters of the User Guide.
	 * 
	 */
	static String[][] userGuideChapters;
	
	/**
	 * The chapters of the current document, obtained from xml source as a Document object,
	 * and maintained for convenience.
	 * 
	 */
	static Document all_chapters_doc_object;
	
	/**
	 * The chapters of the current User Guide, obtained from xml source as a Document object,
	 * and maintained for convenience.
	 * 
	 */
	static Document user_guide_chapters_doc_object;
	
	/**
	 * The table of contents files for the chapters of the current document.
	 * 
	 */
	static String[] chapterTOCfiles;
	
	/**
	 * The table of contents files for the chapters of the user Guide.
	 * 
	 */
	static String[] userGuideChapterTOCfiles;
	
	/**
	 * The current document, derived from the ListView for the entire documentation suite,
	 * and retained to facilitate re-access of the Book Cover for the document from the
	 * chapters that the book contains.
	 * 
	 */
	static int currentDocument;
	
	/**
	 * A record of nodes in the current xml file, use in support of quick access to the User
	 * Guide.
	 */
	static int nodesInCurrentXMLsourcefile;
	
	/**
	 * A record of nodes in the current xml file, use in support of quick access to the User
	 * Guide.
	 */
	static int nodesInUserGuideXMLsourcefile;
	
	/**
	 * A flag to indicate whether or not, during the current session, the user has
	 * employed the text-search facility. A value of 0 indicates that they have not,
	 * and consequently an explanatory toast will appear. A value of 1 indicates that
	 * this is not the first time they have searched, and therefore the toast will
	 * not appear again.
	 * 
	 */
	static int searchYetEmployed;
}
