package eu.fusepool.deduplication.utm2wgs84;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


public class CoordinatesConverter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String utmFilePath = args[0];
		String latlngFilePath = args[1];
		if(utmFilePath == null || latlngFilePath == null){
			System.out.println("Usage: >CoordinatesConverter utm_file.txt latlng_file.txt");
			System.exit(0);
		}
		
		File latlngFile = new File(latlngFilePath);
		
		
		try {
			CoordinateConversion conversion = new CoordinateConversion();
			int count = 0;
			BufferedReader in = new BufferedReader(new FileReader(utmFilePath));
			StringBuffer latlngBuffer = new StringBuffer();

			String utm_line;
			while((utm_line = in.readLine())!= null){
				count++;
				double latlng [] = conversion.utm2LatLon(utm_line);
				double lat = latlng[0];
				double lng = latlng[1];
				
				latlngBuffer.append(lat + "," + lng + "\n");
			
			}
			
			PrintWriter writer = new PrintWriter(latlngFile);
			writer.print(latlngBuffer);
			writer.close();
			
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}

	}
	
}
