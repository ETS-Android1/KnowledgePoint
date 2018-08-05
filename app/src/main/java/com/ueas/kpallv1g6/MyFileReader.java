package com.ueas.kpallv1g6;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads in an html file. 
 *    
 * @author tony.hillman@ultra-as.com
 *
 */
public class MyFileReader
{

	public static String readFile(String pathname) throws IOException 
	{		
		BufferedReader br = new BufferedReader(new FileReader(pathname));
		
		try 
		{
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			while (line != null)
			{
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			
			return sb.toString();			
		} 
		
		finally 
		{
			br.close();
		}	
	}
}
