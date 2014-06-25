/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.fusepool.deduplication.extractor;

import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.RdfGeneratingExtractor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.SIOC;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.commons.io.IOUtils;

import eu.fusepool.silk.Silk;


public class SimpleExtractor extends RdfGeneratingExtractor {

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
        final String text = IOUtils.toString(entity.getData(), "UTF-8");
        final TripleCollection result = new SimpleMGraph();
        /*
        final GraphNode node = new GraphNode(new BNode(), result);
        node.addProperty(RDF.type, new UriRef("http://example.org/ontology#TextDescription"));
        node.addPropertyValue(SIOC.content, text);
        node.addPropertyValue(new UriRef("http://example.org/ontology#textLength"), text.length());
        */
        File configFile = new File("silk-config.xml");
        
        // save the input rdf data in a file (same path as in silk config file) 
        
        Silk.executeFile(configFile, null, 1, true);
        
        // read the silk output file and send the owl:sameas statement as a result
        
        return result;
    }

    @Override
    public boolean isLongRunning() {
        return false;
    }


    
}
