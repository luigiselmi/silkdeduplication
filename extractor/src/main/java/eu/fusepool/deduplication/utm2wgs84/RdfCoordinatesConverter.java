package eu.fusepool.deduplication.utm2wgs84;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;

public class RdfCoordinatesConverter {
	
	
	public void utm2wgs84(InputStream in, OutputStream out) throws FileNotFoundException {
		
		// load the data in a model
		Model model = ModelFactory.createDefaultModel();
		model.read(in, null);
		
		RdfCoordinatesConverter converter = new RdfCoordinatesConverter();
		// Gets the first vertice of each polygon
		HashMap<String,String> firstPointMap = converter.getFirstPointList(model);
		// Transforms vertices from UTM to WGS84
		HashMap<String,WGS84Point> wgs84Map = converter.convertToWGS84(firstPointMap);
		// Enrich the model with wgs84:lat and wgs84:long properties
		Model enrichedModel = converter.enrichModel(model, wgs84Map);
		
		//PrintWriter writer = new PrintWriter(outputRdfFileName);
		enrichedModel.write(out);
		
	}
	
	/**
	 * Returns a list of location URIs with their UTM coordinates. Each location is the first
	 * vertice of a polygon. 
	 * @param model
	 * @return
	 */
	protected HashMap<String,String> getFirstPointList(Model model) {
		Property polygon = model.createProperty("http://www.territorio.provincia.tn.it/geodati/ontology/polygon");		
		HashMap<String,String> pointMap = new HashMap<String, String>();
		Resource subject = null;
		RDFNode object = null;
        SimpleSelector selector = new SimpleSelector(subject, polygon, object);
		for (StmtIterator i = model.listStatements( selector ); i.hasNext(); ) {
            Statement stmt = i.nextStatement();
            String polyString = stmt.getObject().toString();       
            String [] point = polyString.split("\\s+");
            String firstPoint = point[0];
            int comaIndex = firstPoint.indexOf(",");
            String x = firstPoint.substring(0, comaIndex);
            String y = firstPoint.substring(comaIndex + 1);
            String subjUri = stmt.getSubject().getURI();            
            pointMap.put(subjUri, "32 T " + x + " " + y);
            //System.out.println( PrintUtil.print("32 T " + x + " " + y) );
        }
		return pointMap;
    }
	
	
	/**
	 * Transforms the coordinates of locations from UTM to WGS84
	 * @param utmPointList
	 * @return
	 */
	protected HashMap<String,WGS84Point> convertToWGS84(HashMap<String,String> utmPointMap){
		HashMap<String,WGS84Point> wgs84pointMap = new HashMap<String,WGS84Point>();
		CoordinateConversion converter = new CoordinateConversion();
		Set subjectUriSet = utmPointMap.keySet();
		Iterator<String> iutmPoint = subjectUriSet.iterator();
		while(iutmPoint.hasNext()) {	
			String subjectUri = iutmPoint.next();
			String utm = utmPointMap.get(subjectUri);
			double latlng [] = converter.utm2LatLon( utm );
			WGS84Point wgs84 = new WGS84Point();
			wgs84.setLat(latlng[0]);
			wgs84.setLong(latlng[1]);
			wgs84pointMap.put(subjectUri, wgs84);			
			//System.out.println( PrintUtil.print(subjectUri + ": " +  latlng[0] + ", " + latlng[1] ) );
		}		
		return wgs84pointMap;
	}
	/**
	 * Enrich a model with geo:lat and geo:lang and geo:geometry properties
	 * The value of geo:geometry has the format POINT(longitude latitude) 
	 * @param model
	 * @param wgs84Map
	 */
	protected Model enrichModel(Model model, HashMap<String,WGS84Point> wgs84Map) {
		Property wgs84lat = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
		Property wgs84long = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
		Property wgs84geometry = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#geometry");
		Set subjectUriSet = wgs84Map.keySet();
		Iterator<String> wgs84Point = subjectUriSet.iterator();
		while(wgs84Point.hasNext()) {	
			String subjectUri = wgs84Point.next();
			double latitude = wgs84Map.get(subjectUri).getLat();
			double longitude = wgs84Map.get(subjectUri).getLong();
			model.getResource(subjectUri).addProperty(wgs84lat, String.valueOf(latitude));
			model.getResource(subjectUri).addProperty(wgs84long, String.valueOf(longitude));
			String geometry = "POINT(" + String.valueOf(longitude) + " " + String.valueOf(latitude) + ")";
			model.getResource(subjectUri).addProperty(wgs84geometry, geometry);
		}
		
		return model;
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		// Input RDF file		
		//String rdfDataFile = "src/test/resources/pat-locgeo.rdf";
		String rdfDataFile = "";
		if( args.length == 0 ){
			System.out.println("An argument for the RDF file to be converted is expected.");
			System.exit(0);
		}
		else {
			rdfDataFile = args[0];
		}
		// Output RDF file
		File outputFile = new File("wgs84-" + rdfDataFile);
		
		// load the data in a model
		Model model = FileManager.get().loadModel("file:" + rdfDataFile);
		
		RdfCoordinatesConverter converter = new RdfCoordinatesConverter();
		// Gets the first vertice of each polygon
		HashMap<String,String> firstPointMap = converter.getFirstPointList(model);
		// Transforms vertices from UTM to WGS84
		HashMap<String,WGS84Point> wgs84Map = converter.convertToWGS84(firstPointMap);
		// Enrich the model with wgs84:lat and wgs84:long properties
		Model enrichedModel = converter.enrichModel(model, wgs84Map);
		
		PrintWriter writer = new PrintWriter(outputFile);
		enrichedModel.write(writer);
		
	    
	}

}
