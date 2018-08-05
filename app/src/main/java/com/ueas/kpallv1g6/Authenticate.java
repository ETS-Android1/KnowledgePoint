package com.ueas.kpallv1g6;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import android.util.Log;

/**
 * Performs authentication with an appropriate
 * authority. On conclusion, creates an intent that returns control
 * to the Login activity, which in turn either sends the user to the
 * DataInitialization activity, so fully starting the application, or presents a 
 * notification of failure.
 * 
 * @author tony.hillman@ultra-as.com 
 *
 */
public class Authenticate 
{
	/**
	 * Tag for writing to Android LogCat.
	 * 
	 */
	private final String TAG1 = "performRemoteAuthentication";
	
	/**
	 * The constructor for the Authenticate class.
	 * 
	 */
	public Authenticate()
	{
		// Currently empty.
	}
	
	/**
	 * Authenticates a user by submitting a login and a password to the appropriate authority. 
	 * 
	 * @param login A string that is the user's submitted login.
	 * 
	 * @param password A string that is the user's submitted password.
	 * 
	 * @return A string that indicates success or failure.
	 * 
	 * @throws IOException
	 * 
	 */
	public String performRemoteAuthentication(String login, String password) throws IOException
	{		
		URL url = null;

		try 
		{
			// NOTES ABOUT AUTHENTICATION. I'M KEEPING THESE FOR REFERENCE FOR
			// WHEN WE (IF WE) RETURN TO NETWORK ACCESS. THE MACHINE USED WHEN I 
			// WROTE THIS WAS "SPARTA".
			//
			// To make a secure connection to the servlet, we need to specify
			// the name of the system as a Subject Name Alternative. For some
			// reason, it won't work as a regular IP. Not sure why, but the
			// answer is to create the certificate for Tomcat with Java 7 keytool, 
			// specifying the SAN with the "-ext san=dns:sparta" construct. Then
			// use InstallCert to get a copy of this certificate into the JDK
			// keystore used by the calling application.
			
			// Currently, the Android app refuses to authenticate securely from
			// within the Android emulator. This is not a problem for regular 
			// Java classes run from within Eclipse.
			
			// NOTE: THE FOLLOWING ARE REMNANTS OF THE VARIOUS EXPERIMENTS I'VE
			// RUN.
			
			//url = new URL("https://sparta:8453/NewTomCat4/MySQLAuthChecker3");
			//url = new URL("http://192.168.5.165:8080/NewTomCat4/MySQLAuthChecker3");
			url = new URL("http://141.196.102.86:8080/NewTomCat4/MySQLAuthChecker3");
			//url = new URL("http://192.168.0.7:8080/NewTomCat4/MySQLAuthChecker3");
		}
		catch (MalformedURLException ex)
		{
			System.out.println("couldn't create url");
		}
		
		// Set up an http connection object.
		//
		HttpURLConnection urlConn = null;

		// Try to open the connection, based on the specified url.
		//
		try 
		{
			urlConn = (HttpURLConnection) url.openConnection();
		}
		catch (IOException ex)
		{
			Log.w(TAG1, "couldn't open url connection");
		}
		
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		
		try
		{
			urlConn.setRequestMethod("POST");
		}
		catch (ProtocolException ex)
		{
			System.out.println("Couldn't set url connection's request method.\n");
		}
		
		// Try to connect.
		//
		try
		{
			urlConn.connect();
		}
		catch (IOException ex)
		{
			Log.w(TAG1, "Couldn't connect");
			Log.i(TAG1, "Could not connect");
			System.out.println("Couldn't connect to url.\n");
		}

		// Establish input and output streams, for communicating across the
		// connection.
		//
		DataOutputStream output = null;
		DataInputStream input = null;	
		
		output = new DataOutputStream(urlConn.getOutputStream());
		
		// Specify a login and a password, which will be written to
		// the output stream for the connection to the URL.
		//
		@SuppressWarnings("deprecation")
		String content =
				"login=" + URLEncoder.encode(login) +
				"&pwd=" + URLEncoder.encode(password);
		
		try
		{	
			// Write the content to the output stream. It should now go
			// to the servlet, and be handled there.
			//
			output.writeBytes(content);
			
			// Flush and close the stream.
			//
			output.flush();
			output.close();
		}
		catch (IOException ex)
		{
			System.out.println("Couldn't write bytes to output.\n");
		}
		
		// Create an input stream and a buffer, and read from the buffer 
		// until it the stream is declared closed. This is the response from
		// the server.
		//
		input = new DataInputStream(urlConn.getInputStream());
        byte[] buffer = new byte[2048];
        
        // The result string will come from the servlet, and will commence
        // either with the letter Y or N, indicating success or failure.
        //
        String result = null;

        while ( input.read( buffer ) != -1 ) 
        {
        	// Grab the string from the servlet, and put it in the result string.
        	//
        	result = new String (buffer);
        }
        
        // Close the data input stream.
        //
        input.close();
        
        System.out.println("Got to end of authentication routine.\n");
        
        return result;	
	}
}
