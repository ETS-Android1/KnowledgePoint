package com.ueas.kpallv1g6;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

/**
 * Contains static definitions of meta data related to the
 * videos. Some elements re-represent data as
 * arrays for quick-access, once the first network-based access
 * has occurred.
 * 
 * @author tony.hillman@ultra-as.com
 *
 */
public class MetaDataForVideo 
{
	/**
	 * The URL of the xml file containing the list of chapters.
	 * 
	 */
	static String allVideoURL;
	
	/**
	* Array list that can be used as a shortcut, for example when
	* going to previous and next video from the current. This value is
	* set within the ListViewForVideo. It requires the current hash-strip
	* to be retrieved by means of the current id, and getSongInfo then used
	* to pull out string-values for a string array to be bundled into the
	* next Video activity.
	* 
	*/
	static ArrayList<HashMap<String, String>> allVideosArrayList;
	
	/**
	* The shows, as a 2D string array. The first element in the array is the
	* show itself, represented by an ID'd node in the xml file. The second
	* element consists of each of the sub-nodes in the node (eg, name, url of
	* mp, url or image xml file, etc).
	* 
	*/
	static String[][] allVideos;
	
	
	/**
	 * The latest doc object in use for videos, maintained as a convenience, so that returning to
	 * the network can be avoided.
	 */
	static Document all_videos_doc_object;
	
	/**
	 * The current animation.
	 * 
	 */
	static int currentVideo;
	
	/**
	 * The number of nodes in the xml that corresponds to the current animation.
	 * 
	 */
	static int nodesInCurrentXMLsourcefile;

}
