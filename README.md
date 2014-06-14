# DL-Learner + NLP = ?

"Combination of NLP-algorithms with DL-Learner algorithms"

DL-Learner: infers owl class descriptions from given data

- positive/negative examples: given examples, search for class
    description that includes as many as possible instances

NLP: given a text, extract terms from it

- to be useful, those need to be from ontologies (or otherwise
    have an assigned/agreed upon meaning)

# Goals/Ideas

* parsing the abstracts of instances of classes and then figuring
    out which schema entities (classes & properties) appear most
    often

    - the task is to evaluate nlp tools with respect to how well
        they could be used to do this
* extracting axioms from text, by inferring schema knowledge
    from it (e.g. given "Paul is a human", infer that Paul is
    an instance of the class "human")
* extracting facts from text, similar to extracting axioms, but
    with a different grammar and patterns to look out for

# Resources

- background:
    * RDF/RDFS/OWL ("Semantic Web", Hitzler et. al.)
    * description logics ("The Description Logic Handbook")
    * NLP (Information Extraction, Knowledge Extraction,
        Named Entity Recognition)
    * [DL-Learner](http://dllearner.org) ([manual/introduction](http://dl-learner.org/files/dl-learner-manual.pdf))
- nlp tools
    * [DBpedia Spotlight](https://github.com/dbpedia-spotlight/dbpedia-spotlight/wiki)
        (installable web service & [demo](http://spotlight.dbpedia.org/demo/),
         [demo rest endpoint](http://spotlight.sztaki.hu:2222/rest))
        - annotates text with terms from dbpedia
    * [FRED](http://wit.istc.cnr.it/stlab-tools/fred) (web service,
        [documentation](http://wit.istc.cnr.it/stlab-tools/fred/api))
        - extracts entities and relationships between them from sentences
            (it also infers some generic classes from the linguistic
             interpretation of the sentences. e.g. noun -> class)
    * [FOX](http://aksw.org/Projects/FOX.html) (library & web service)
        - generates rdf triples containing annotations of fragments
            of the input text (e.g. Named Entity Recognition with RDF output)
        - example output only links to dbpedia
    * [NERD](http://nerd.eurecom.fr/documentation) (web service)
        - extracts annotations using various nlp tools (including dbpedia spotlight,
            opencalais, alchemy, ...), but also supports a "combined" mode
    * [alchemy](http://alchemyapi.com) (web service, [demo](http://www.alchemyapi.com/products/demo/))
    * [open calais](http://opencalais.com) (web service)
