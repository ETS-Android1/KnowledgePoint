package com.ueas.kpallv1g6;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import android.util.Log;

/**
 * Retrieves XML data from a specified URL, transforms it into a DOM document,
 * and obtains its values.
 * 
 * @author tony.hillman@ultra-as.com
 * 
 */
public class XMLParser 
{
	/**
	 * A string for writing to the LogCat.
	 * 
	 */
	private final static String TAG0 = "XMLParser:";

	/**
	 * Constructor for the XMLParser object.
	 * 
	 */
	public XMLParser() 
	{
		// Nothing at this point.
	}

	/**
	 * Retrieves XML data from a specified location, and returns this as a
	 * string.
	 * 
	 * @param url
	 *            	A string that is the URL of the XML file to be retrieved.
	 *            
	 * @return 
	 * 				A string that is the XML data at the specified URL.
	 * 
	 */
	public String getXmlFromUrl(String url) 
	{
		String xml = null;

		try 
		{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			//HttpPost httpPost = new HttpPost(url);
			HttpGet httpGet = new HttpGet(url);

			// HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			
			xml = EntityUtils.toString(httpEntity);

		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} 
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		return xml;
	}
	
	/**
	 * Creates a DOM document based on XML data passed as a string.
	 * 
	 * @param xml
	 *            	A string that is the XML data from which the DOM document is
	 *            	to be created.
	 * @return 
	 * 				The DOM document based on the specified string.
	 * 
	 */
	public Document getDomElement(String xml)
	{
		Document doc = null;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try 
		{
			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
		    is.setCharacterStream(new StringReader(xml));
		    doc = db.parse(is); 

		} 
		catch (ParserConfigurationException e) 
		{
			Log.e("Error: ", e.getMessage());
			return null;
		} 
		catch (SAXException e) 
		{
			Log.e("Error: ", e.getMessage());
	        return null;
		} 
		catch (IOException e) 
		{
			Log.e("Error: ", e.getMessage());
			return null;
		}

	    return doc;
	}
	
	/**
	 * Returns a string that represents the value of a DOM document node.
	 * 
	 * @param elem
	 *            The DOM document node whose value is to be returned.
	 *            
	 * @returns 
	 * 			  The string that is the value of the specified node.
	 * 
	 */
	 public final String getElementValue( Node elem ) 
	 {
	     Node child;
	     
	     if( elem != null)
	     {
	         if (elem.hasChildNodes())
	         {
	             for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() )
	             {
	                 if( child.getNodeType() == Node.TEXT_NODE  )
	                 {
	                     return child.getNodeValue();
	                 }
	             }
	         }
	     }
	     
	     return "";
	 }
	 
	/**
	 * Returns a string that is the URL value of a "thumb_url" node within a DOM
	 * document.
	 * 
	 * @param elem
	 *            The DOM document node whose value is to be returned.
	 * @return 
	 * 			  The string that is the URL value of the specified node.
	 * 
	 */
	 public final String getURLValue( Node elem )
	 {
		String theURL = null;
		
		Node parent = elem.getParentNode();
		NodeList nl = ((Document)parent).getElementsByTagName("thumb_url");
		
		for (int i = 0; i < nl.getLength(); i++) 
		{
			theURL = (nl.item(i)).getNodeValue();
		}
	
	     return theURL;
	 }
	 
	/**
	 * Returns a string that is the value of a node within a DOM document.
	 * 
	 * @param item
	 *            The node whose value is to be returned.
	 * @param str
	 *            A string that is the key according to which an associated node
	 *            value is to be retrieved.
	 *            
	 * */
	 public String getValue(Element item, String str) 
	 {		
			NodeList n = item.getElementsByTagName(str);	
			
			return this.getElementValue(n.item(0));
	 }
}
