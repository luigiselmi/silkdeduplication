Interlinking of Spatial Data
============================

An example application for the interlinking of spatial data. Implements the requirements in [FP-181](https://fusepool.atlassian.net/browse/FP-181).

The purpose of the application is to enable a user to post an RDF data set about locations in the Province of Trento
and get the result of the interlinking process against a local data set through a SPARQL endpoint. Both data sets,
the source that must be imported in Jena, and the target that must be sent via http POST to the server can be downloaded 
from the Open Data Portal of the Province of Trento (PAT).
The target RDF data file, that represents the local knowledge base against which the interlinking will be performed, can be downloaded from the url

http://dati.trentino.it/dataset/localita-geografiche-1991-671556

It contains information about 1800 locations in the province of Trento, from the municipalities, to small villages and farms like area, perimeter, and a polygon in UTM coordinates. The same file is available in extractor/src/test/resources/pat-locgeo.rdf. In order to test the interlinking of spatial entities (locations) the following file from the same portal can be used

http://dati.trentino.it/dataset/centri-abitati-istat-ed-1991-980076

This RDF data set contains the same information as for the first data set about 1208 locations in the province provided by ISTAT, the Italian Institute of Statistics. Locations in the two data sets are represented as members of two different classes. The names of the locations are given only for the target data set and the code identifiers are different for the same location so that the only way to check when two URIs represent the same location is by its area, perimeter and distance from the other one.

In order to compile the software you need to download from its repository on github the transformer library that makes it possible to post a request asynchronously to the server

https://github.com/fusepoolP3/p3-transformer-library

The server will compare the locations' descriptions retrieving the target data from a SPARQL endpoint. It can easily be set up using Fuseki from the Apache Jena project. Fuseki (version jena-fuseki-1.0.1 or later) can be download from 

https://jena.apache.org/download/index.cgi

Before starting Fuseki the target data set must be imported in the embedded Jena TDB triple store. An assembler file, jena-spatial-assembler.ttl, is provided to make it easy to import the data into Jena TDB and to run Fuseki. Be sure to update the path to the data set folder and to the Lucene index in the assembler file. The Lucene index enables the basic spatial searches supported by Jena Spatial like search for a location within a radius from a given point or within a box.
By default Jena and SILK, the interlinking engine used by the application, can use only WGS84 coordinates (e.g. lat, long) for points while the vertices in the polygons given in both data set are in UTM coordinates (x, y). To make the comparison based on the distance of the locations as easy as possible the target data must be enriched with the latitude and longitude of one point taken from the vertices of the polygon of each location transforming its UTM coordinates in WGS84. You can convert the target data set with the following command

    java -cp &lt;path of fuseki-server.jar&gt;:&lt;path of extractor-0.1-SNAPSHOT.jar&gt; eu.fusepool.deduplication.utm2wgs84.RdfCoordinatesConverter &lt;path of pat-locgeo.rdf&gt;

The converted file is also available in src/test/resources/wgs84-pat-locgeo.rdf. The converted target file can be imported in the Jena TDB triple store using the command

    java -cp fuseki-server.jar tdb.tdbloader --graph=urn:x-localinstance:/enrich-pat-locgeo --desc jena-spatial-assembler.ttl wgs84-pat-locgeo.rdf

where the paths are modified accordingly. The graph parameter is the named graph where the triples will be stored. That is the graph from where SILK will retrieve the data for the comparison with the incoming data. If the name of the graph is changed or not used it must be changed also in the SILK configuration file extractor/src/main/resources/silk-config-spatial.xml. After the target data has been imported Fuseki can be run with the command

    java -Xmx4g -jar fuseki-server.jar --conf jena-spatial-assembler.ttl

When Fuseki is started the application can be run as well from extractor/ folder with the command

    mvn exec:java

The application is waiting on the port 7100. To post the RDF data to be interlinked with the data from the SPARQL endpoint move to the folder extractor/src/test/resources/ and run the curl command 

    curl -X POST -T pat-centri-abitati.rdf http://localhost:7100

The result of the interlinking process will be sent back to the client.

The SILK version used is 2.6.0 that is not available from the Maven repository. In order to use it in Maven it must be dowloaded from the project webste

http://wifo5-03.informatik.uni-mannheim.de/bizer/silk/

then unzip the file and import the file silk.jar into your local Maven repository using the command

    mvn install:install-file -Dfile=&lt; path to silk.jar &gt; -DgroupId=de.fuberlin.wiwiss.silk -DartifactId=silk -Dversion=2.6.0 -Dpackaging=jar

The silk.jar file can be used as a dependency in the project using the coordinates

    <dependency>  
      <groupId>de.fuberlin.wiwiss.silk</groupId>  
      <artifactId>silk</artifactId>   
      <version>2.6.0</version>  
    </dependency>   





