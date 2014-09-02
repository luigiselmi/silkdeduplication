silkdeduplication
=================

An example application for interlinking of spatial data

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

Before starting Fuseki the target data set must be imported in the embedded Jena TDB triple store. An assembler file is provided to make it easy to import the data into Jena TDB and to run Fuseki. Be sure to update the path to the data set folder and to the Lucene index in the assembler file. The Luce index enables the basic spatial searches supported by Jena Spatial like search for a location within a radius from a given point or within a box.





