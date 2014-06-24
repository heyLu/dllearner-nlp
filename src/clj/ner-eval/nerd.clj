(ns ner-eval.nerd
  "Tiny binding to the NERD api."
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

(defn annotate-document [extractor id-or-map]
  (assert api-extractors extractor)
  (let [id-or-map (if (map? id-or-map)
                    id-or-map
                    {:extractor extractor
                     :idDocument id-or-map})]
    (:body (api-request :post "annotate" id-or-map))))

(defn get-annotation [id-or-map]
  (let [id-or-map (if (map? id-or-map)
                    id-or-map
                    {:idAnnotation id-or-map})]
    (:body (api-request :get "entity" id-or-map))))

(defn normalize [{:keys [label uri startChar endChar]}]
  {:text label,
   :entity uri,
   :start startChar,
   :end endChar})
