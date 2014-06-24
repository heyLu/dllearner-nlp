(ns ner-eval.core
  "Evaluate NER capabilities of nlp tools.

# steps

- retrieve entities from dbpedia (select by class)
    * using the sparql endpoint
    * which format to use? (json, csv, ...)
- get abstracts for those entities
- process abstracts using nlp tools
    * start with nerd, it supports many things (api might be cumbersome, though)
    * result are extracted entities (vector of places in the text with info)
- basic stats
    * number of extracted entities
- more reasonable stats
    * overlap
        - which tools also find this
        - do tools different tools find 'bigger' entities for the same (or similar) text?
        - (maybe there's a library for diffing in clj?)
    * which entities are mapped (different ones for the same text)
- comparison with hand-tagged data
    * how many places also matched
    * to which entities

# data model

- everything in vectors
- from sparql: just the names
- with abstracts: {:name \"...\", :abstract \"...\"} (maybe also comment?)
- entity extraction: vector of maps, name, abstract, entities:
    * entities: `[{:text \"...\", :start 0, :end 10, :entity \"<url>\"}]`"
  (:require [clj-http.client :as http]))

(defn dbpedia-query
  "send a sparql query to the dbpedia endpoint returning a vector of result tuples."
  [sparql-str]
  nil)

(defn query-cities [n]
  (str "
SELECT ?city
WHERE { ?city rdf:type dbp:City . }
LIMIT " n))
