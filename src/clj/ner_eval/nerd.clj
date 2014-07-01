(ns ner-eval.nerd
  "Tiny binding to the NERD api.

# Notes

there seems to be a cache for documents and annotations. adding a document with the same text multiple times returns the same `idDocument`. the java library also adds `cache` and `force` parameters to annotation requests [1], which may or may not actually do anything.

[1]: https://github.com/giusepperizzo/nerd4java/blob/master/src/fr/eurecom/nerd/client/NERDResult.java#L129-L130"
  (:require [clj-http.client :as http]))

(def api-key "120rj2n2stnmquhgcgim11ucrgd68jh4")
(def api-base-url "http://nerd.eurecom.fr/api/")
(def api-extractors
  #{"combined" "alchemyapi" "datatxt" "dbspotlight" "lupedia" "opencalais" "saplo" "semitags" "textrazor" "thd" "wikimeta" "yahoo" "zemanta"})

(defn api-request [method path params]
  (http/request
   {:method method
    :url (str api-base-url path)
    :content-type :x-www-form-urlencoded
    :query-params (into {:key api-key} params)
    :as :json
    :throw-entire-message? true}))

(defn add-document [text]
  (:body (api-request :post "document" {:text text})))

(defn singleton-map [key val]
  (if (map? val)
    val
    {key val}))

(defn annotate-document [extractor id-or-map]
  (assert api-extractors extractor)
  (let [id-or-map (singleton-map :idDocument id-or-map)
        params (assoc id-or-map :extractor extractor)]
    (:body (api-request :post "annotation" params))))

(defn get-annotation [id-or-map]
  (let [id-or-map (singleton-map :idAnnotation id-or-map)]
    (:body (api-request :get "entity" id-or-map))))

(defn normalize [{:keys [label uri startChar endChar]}]
  {:text label,
   :entity uri,
   :start startChar,
   :end endChar})

(defn annotate-text* [text & [extractor]]
  (let [extractor (or extractor "combined")]
    (->> (add-document text)
         (annotate-document extractor)
         (get-annotation)
         (map normalize))))
