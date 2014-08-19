(defproject ner-eval "0.1.0"
  :description "Evaluating NER capabilities of nlp tools."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "0.9.2"]]
  :source-paths ["src/clj"]
  :profiles {:uberjar {:aot [ner-eval.core]}}
  :main ner-eval.core)
