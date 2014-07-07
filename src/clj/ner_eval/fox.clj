(ns ner-eval.fox
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.edn :as edn]))

(def api-base-url "http://139.18.2.164:4444/api")

(defn fox-ld->anns [ld]
  (vec (mapcat (fn [ann]
                 (let [{:strs [beginIndex endIndex means ann:body]} ann]
                   (if (vector? beginIndex)
                     (map #(hash-map :text ann:body :entity means :start %1 :end %2)
                          (sort (map edn/read-string beginIndex))
                          (sort (map edn/read-string endIndex)))
                     [{:text ann:body
                       :entity means
                       :start (edn/read-string beginIndex)
                       :end (edn/read-string  endIndex)}])))
               (get ld "@graph"))))

(defn foxlight->str [foxlight]
  (let [base "org.aksw.fox.nertools.NER"]
    (case foxlight
      :opennlp (str base "OpenNLP")
      :illinois (str base "IllinoisExtended")
      :stanford (str base "Stanford")
      :balie (str base "Balie")
      "OFF")))

(defn annotate-text* [text & [foxlight]]
  (->
   (http/post api-base-url
              {:query-params {:input text
                              :type "text"
                              :task "NER"
                              :output "JSON-LD"
                              :foxlight (foxlight->str foxlight)}
               :content-type :x-www-form-urlencoded
               :throw-entire-message? true
               :as :json})
   :body :output
   java.net.URLDecoder/decode
   json/parse-string
   fox-ld->anns))
