/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.fusepool.deduplication.extractor;

import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.RdfGeneratingExtractor;
import eu.fusepool.silk.Silk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.ontologies.SIOC;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.commons.io.IOUtils;


public class LongRunningExtractor extends RdfGeneratingExtractor {
	final String SILK_CONFIG_FILE = "src/main/resources/silk-config-file.xml";
	final String INPUT_RDF_FILE = "src/main/resources/inputdata.ttl";
	final String RESULT_FILE = "src/main/resources/accepted_links.nt";
	final String BASE_URI = "http://example.org/";

    @Override
    public Set<MimeType> getSupportedInputFormats() {
        try {
            MimeType mimeType = new MimeType("text/plain;charset=UTF-8");
            return Collections.singleton(mimeType);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected TripleCollection generateRdf(Entity entity) throws IOException {
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        final InputStream inputRdfData = entity.getData();
    	TripleCollection duplicates = findDuplicates(inputRdfData);
		return duplicates;
		
        
		/*
        final String text = IOUtils.toString(entity.getData(), "UTF-8");
        final TripleCollection result = new SimpleMGraph();
        final GraphNode node = new GraphNode(new BNode(), result);
        node.addProperty(RDF.type, new UriRef("http://example.org/ontology#TextDescription"));
        node.addPropertyValue(RDFS.comment, "This took a long while");
        node.addPropertyValue(SIOC.content, text);
        node.addPropertyValue(new UriRef("http://example.org/ontology#textLength"), text.length());
        return result;
        */
        
    }
    
    protected TripleCollection findDuplicates(InputStream inputRdf) throws IOException {
        File configFile = new File(SILK_CONFIG_FILE);
        File rdfFile = new File(INPUT_RDF_FILE);
        FileOutputStream out = new FileOutputStream(rdfFile); 
        IOUtils.copy(inputRdf, out);
        out.close();
        Silk.executeFile(configFile, null, 1, true);
        
        return parseResult(RESULT_FILE);
    }
    
    /**
     * Reads the silk output (n-triples) and returns the owl:sameas statements as a result
     * @param fileName
     * @return
     * @throws IOException
     */
    protected TripleCollection parseResult(String fileName) throws IOException {
    	final TripleCollection links = new SimpleMGraph();
    	BufferedReader in = new BufferedReader(new FileReader(fileName));
    	String statement;
    	while((statement = in.readLine())!= null){
    		Triple link = new TripleImpl(getSubject(statement),OWL.sameAs,getObject(statement));
    		links.add(link);
    	}
    	in.close();
    	return links;
    }
    
    protected UriRef getSubject(String statement){
    	int endOfSubjectIndex = statement.indexOf('>');
    	String subjectName = statement.substring(1, endOfSubjectIndex);
    	UriRef subjectRef = new UriRef(subjectName);
    	return subjectRef;
    }
    
    protected UriRef getObject(String statement) {
    	int startOfObjectIndex = statement.lastIndexOf('<');
    	String objectName = statement.substring(startOfObjectIndex + 1, statement.length() - 1);
    	UriRef objectRef = new UriRef(objectName);
    	return objectRef;
    }

    @Override
    public boolean isLongRunning() {
        return true;
    }


    
}
