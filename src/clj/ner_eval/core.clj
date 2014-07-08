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
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.edn :as edn]

            [ner-eval.nerd :as nerd]
            [ner-eval.fox :as fox]
            [ner-eval.dbpedia-spotlight :as spotlight]

            [ner-eval.metrics :as m]))

(defn simple-tsv [str]
  (mapv (fn [line]
          (mapv edn/read-string (str/split line #"\t")))
        (rest (str/split str #"\n"))))

(defn dbpedia-query
  "send a sparql query to the dbpedia endpoint returning a vector of result tuples."
  [sparql-str]
  (-> (http/get "http://dbpedia.org/sparql"
                {:query-params {:query sparql-str
                                :format "text/tab-separated-values"}})
      :body
      simple-tsv))

(defn query-cities [n]
  (str "
select ?city ?abstract
where {
  ?city rdf:type dbpedia-owl:Town ;
        dbpedia-owl:country dbpedia:Germany ;
        dbpedia-owl:abstract ?abstract ;
        dbpedia-owl:populationTotal ?population .
  filter (lang(?abstract) = \"en\")
  optional { ?city dbpedia-owl:capital ?capital }
  filter (!bound(?capital))
}
order by desc(?population)
limit " n))

(defn query-entities-of-class [class n]
  (let [class (if (keyword? class)
                (str (namespace class) ":" (name class))
                class)]
    (str "
select ?entity ?abstract ?country
where {
  ?entity rdf:type " class " ;
          dbpedia-owl:country ?country ;
          dbpedia-owl:abstract ?abstract .
  filter (lang(?abstract) = \"en\")
}
limit " n)))

(defn just-text [text]
  (cond
    (map? text) (:text text)
    (vector? text) (nth text 1)
    :else text))

(defn annotate-text
  "Annotate the entities in the text using external NER tools.

  - extractor: `:fox`, `:nerd` or `:spotlight`
  - text: the text to annotate
  - args: options specific to the NER tool used
    * `:fox`: ner library used: `:opennlp`, `:illinois`, `:stanford` or `:balie`
    * `:nerd`: any of the strings combined, alchemyapi, datatxt, dbspotlight, lupedia, opencalais, saplo, semitags, textrazor, thd, wikimeta, yahoo or zemanta
    * `:spotlight`: a map of parameters to pass to dbpedia spotlight, e.g. `{:lang \"de\", :confidence 0.5}`"
  [extractor text & args]
  (let [text (just-text text)
        ann-fn (case extractor
                 :nerd nerd/annotate-text*
                 :fox fox/annotate-text*
                 :spotlight spotlight/annotate-text*)]
    (apply ann-fn text args)))

(defn annotate-texts [n extractor texts & args]
  (let [anns (atom {})
        total (count texts)
        processed (atom 0)
        stats (atom {:total total, :success 0, :error 0})
        semaphore (java.util.concurrent.Semaphore. n)]
    (doseq [text texts]
      (future
        (let [ann (try
                    (apply annotate-text extractor text args)
                    (catch Throwable c
                      nil))]
          (swap! stats update-in [(if ann :success :error)] inc)
          (swap! anns assoc (just-text text) ann))))
    [anns stats]))

(defn annotation-finished? [ann-stats]
   (= (:total ann-stats) (+ (:success ann-stats) (:error ann-stats))))

(def annotation-stats (atom nil))

(defn annotate-texts-blocking [& args]
  (let [[anns stats] (apply annotate-texts args)]
    (while (not (annotation-finished? @stats))
      (Thread/sleep 1000))
    (swap! annotation-stats (constantly @stats))
    @anns))

(def default-configs
  {:nerd-combined [:nerd "combined"]
   :spotlight [:spotlight]
   :fox-opennlp [:fox :opennlp]})

(defn run-annotations
  ([name texts]
   (run-annotations 10 name texts))
  ([n id texts]
   (run-annotations n id texts default-configs))
  ([n id texts configs]
   (doseq [[config-name [extractor & args]] configs]
     (let [anns (apply annotate-texts-blocking n extractor texts args)]
       (spit (str id "-anns-" (name config-name) ".edn") anns)
       (println "annotated using" config-name @annotation-stats)))))

(defn run-stats [prefix & [ref-name dir]]
  (let [files (filter #(.startsWith (.getName %) prefix) (.listFiles (java.io.File. (or dir "."))))
        ref-anns (edn/read-string (slurp (or ref-name (first files))))
        csv-name (str prefix ".csv")]
    (spit csv-name (str "dataset, " m/stats-csv-header "\n"))
    (doseq [file files]
      (let [anns (edn/read-string (slurp file))
            file-name (.getName file)
            stats (m/annotation-stats anns (if (not= file-name ref-name)
                                             ref-anns
                                             nil))
            csv-stats (cons (.substring file-name (count prefix)) (m/stats-to-csv stats))]
        (spit csv-name (str (apply str (interleave csv-stats (repeat ", "))) "\n") :append true)
        (println (str  file-name ": " stats))))))
