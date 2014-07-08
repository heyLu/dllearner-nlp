(ns ner-eval.metrics
  "metrics for comparing ner tools.

- number of terms extracted
    * counting doesn't seem to make sense either:
        something matching all worlds would \"win\", no knowledge about good matches
    * but maybe also not necessary because the dl-learner would filter that out for ontologies (kind of)
- maps to dbpedia
- overlap
    * # of overlaps, percentage (of what?), ...?
    * for number of terms, mapping to dbpedia we only need the
        terms from all abstracts, but if we want to compare overlaps
        we need data to compare with
    * possible base data: dbpedia-spotlight, hand-tagged
"
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(defn intersect [coll1 coll2]
  (set/intersection (into #{} coll1) (into #{} coll2)))

(defn same-annotations [anns1 anns2]
  (intersect anns1 anns2))

(defn same-terms [anns1 anns2]
  (= (map :text anns1) (map :text anns2)))

(defn same-termset [anns1 anns2]
  (intersect (map :text anns1) (map :text anns2)))

(defn compare-termsets [anns1 anns2]
  (let [ts1 (into #{} (map :text anns1))
        ts2 (into #{} (map :text anns2))
        c {:intersection (set/intersection ts1 ts2)
           :difference (set/difference ts1 ts2)}]
    (assoc {} :intersectionCount (count (:intersection c))
             :differenceCount (count (:difference c)))))

(def overlap-example
  "Peter Pan is a dear friend of the Great Doodadle of neverland who likes cake very much.")

(def overlap-example-ranges
  [[0 8]
   [20 25]
   [35 47] [39 47] [52 59] [35 59]])

(defn overlap?
  "Overlap if boundaries of one range are within the other."
  [[s1 e1] [s2 e2]]
  (or (<= s1 s2 e1) (<= s1 e2 e1)
      (<= s2 s1 e2) (<= s2 e1 e2)))

(defn overlap [ann1 ann2]
  (let [{s1 :start, e1 :end} ann1
        {s2 :start, e2 :end} ann2]
    (if (overlap? [s1 e1] [s2 e2])
      {:text (:text ann1)
       :ref (:text ann2)
       :length (- (- e1 s1) (- e2 s2))}
      nil)))

(defn overlaps
  "Annotations whose ranges overlap"
  [anns ref-anns]
  (filter (comp not nil?)
    (map #(overlap %1 %2) anns ref-anns)))

(defn result-overlaps [anns-for-texts refs-for-texts]
  (let [texts (keys refs-for-texts)]
    (seq
     (flatten
      (filter (comp not nil? seq)
              (map (fn [text]
                     (if (contains? anns-for-texts text)
                       (let [anns (get anns-for-texts text)
                             ref-anns (get refs-for-texts text)]
                         (overlaps anns ref-anns))
                       nil))
                   texts))))))

(defn collect-anns [text-anns]
  (into #{} (flatten (vals text-anns))))

(defn count-stats [{:keys [anns] :as m}]
  (assoc m
    :total (count anns)
    :unique (count (into #{} (map :text anns)))))

(defn coll-stats [numbers]
  (let [count (count numbers)]
    {:total count
     :avg (if (> count 0)
            (float (/ (reduce + numbers) count))
            0)
     :min (reduce min Integer/MAX_VALUE numbers)
     :max (reduce max Integer/MIN_VALUE numbers)}))

(defn word-stats [{:keys [texts anns] :as m}]
  (assoc m
    :ann-length (coll-stats (map (comp count :text) anns))
    :word-length (coll-stats (mapcat #(map count (str/split % #"\s+")) texts))))

(defn mapping-stats [{:keys [anns] :as m}]
  (assoc m
    :mappings (reduce (fn [mappings ann]
                        (let [entity (:entity ann)
                              mapped-ns (try
                                          (.getHost (java.net.URL. entity))
                                          (catch Throwable t
                                            entity))]
                          (update-in mappings [mapped-ns]
                                     (fn [old]
                                       (inc (or old 0))))))
                      {}
                      anns)))

(defn overlap-stats [{:keys [anns-for-texts ref-anns] :as m}]
  (if ref-anns
    (let [overlaps (result-overlaps anns-for-texts ref-anns)]
      (assoc m
        :overlaps (coll-stats (map :length overlaps))))
    m))

(defn annotation-stats [anns-map & [ref-anns]]
  (dissoc
   (let [m (assoc {}
             :anns-for-texts anns-map
             :ref-anns ref-anns
             :anns (flatten (vals anns-map))
             :texts (keys anns-map))]
     (->> m
          count-stats
          word-stats
          mapping-stats
          overlap-stats))
   :anns :texts :anns-for-texts :ref-anns))

(def stats-csv-header
  "total, unique, ann-length, overlap-length, most-mappings, %-most-mappings")

(defn stats-to-csv [stats]
  (let [{:keys [total unique ann-length overlaps mappings]} stats
        [mtotal most-mappings mv] (reduce-kv (fn [[total mk mv] k v]
                                               (if (> v mv)
                                                 [(+ total v) k v]
                                                 [(+ total v) mk mv]))
                                             [0 nil 0]
                                             mappings)]
    [total unique (:avg ann-length) (:avg overlaps) most-mappings (float (* 100 (/ mv mtotal)))]))
