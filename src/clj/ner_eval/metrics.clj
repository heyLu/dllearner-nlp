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

(defn overlap?
  "Overlap if boundaries of one range are within the other."
  [[s1 e1] [s2 e2]]
  (or (< s1 s2 e1) (< s1 e2 e1)
      (< s2 s1 e2) (< s2 e1 e2)))

(defn overlaps
  "Annotations whose ranges overlap"
  [anns1 anns2]
  ())

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

(defn annotation-stats [anns-map]
  (dissoc
   (let [m (assoc {}
             :anns (flatten (vals anns-map))
             :texts (keys anns-map))]
     (->> m
          count-stats
          word-stats))
   :anns :texts))
