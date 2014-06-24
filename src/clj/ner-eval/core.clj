(ns ner-eval
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
