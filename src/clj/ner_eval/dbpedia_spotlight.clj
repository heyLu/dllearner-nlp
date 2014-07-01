(ns ner-eval.dbpedia-spotlight
  (:require [clj-http.client :as http]
            [clojure.edn :as edn]))

(defn api-base-url [lang]
  (case lang
    "en" "http://spotlight.dbpedia.org/rest/"
    ;"de" "http://de.dbpedia.org/spotlight/rest/"
    "de" "http://spotlight.sztaki.hu:2226/rest/"))

(defn spotlight->anns [res]
  (mapv (fn [ann]
          (let [text ((keyword "@surfaceForm") ann)
                entity ((keyword "@URI") ann)
                start (edn/read-string ((keyword "@offset") ann))
                end (+ start (count text))]
            {:text text, :entity entity, :start start, :end end}))
        (:Resources res)))

(defn annotate-text* [text & [{:keys [lang] :as opts}]]
  (->
   (http/post (str (api-base-url (or lang "en")) "annotate")
              {:query-params (into opts {:text text})
               :content-type :x-www-form-urlencoded
               :accept :json
               :as :json
               :throw-entire-message? true})
   :body
   spotlight->anns))
