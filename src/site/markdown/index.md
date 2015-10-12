<div class="well sidebar" id="well-home">
    <h1>
        <img src='images/pikes-big.png' alt='PIKES' title='PIKES' />
    </h1>
    
    <p class='title2'>
        Pikes is a Knowledge Extraction Suite
    </p>
                
    <p class='title2'>
        <!--<a class="btn btn-primary btn-large" href="install.html">Download</a>-->
        <a class="btn btn-primary btn-large" href="https://knowledgestore2.fbk.eu/pikes-demo/">Online demo</a>
        <a class="btn btn-primary btn-large" href="https://www.youtube.com/watch?v=D0mcnUKc3sg">Video tour</a>
    </p>
</div>

---------------------------------------

### About

**PIKES** is a Java-based suite that extracts knowledge from textual resources.
The tool implements a rule-based strategy that reinterprets the output of semantic role labelling (SRL) tools in light
of other linguistic analyses, such as dependency parsing or co-reference resolution, thus properly capturing and
formalizing in RDF important linguistic aspects such as argument nominalization, frame-frame relations, and group
entities.

### Features

- Argument nominalization using SRL
- Frame-frame relations extractions
- Entity grouping exploiting linking and co-reference
- Extensible and replaceable NLP pipeline
- Interlinked three-layer representation model exposed as RDF
- Instance RDF triples annotated with detailed information of the mentions (via named graph)
- REST API service included, built on top of [Grizzly](https://grizzly.java.net/)
- Based on [Java 8](http://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html) and [RDFpro](http://rdfpro.fbk.eu/)

### News

- 2015-06-08 Version 0.1 has been released.