(ns ner-eval.samples
  (:require [clojure.string :as str]))

(defn extract [text & args]
  (into {:text text} (map vector [:start :end :entity] args)))

(defn add-positions [{:keys [abstract extractions] :as entity-data}]
  (assoc entity-data
    :extractions (map (fn [{:keys [text start] :as extract}]
                        (if start
                          extract
                          (let [idx (.indexOf abstract text)]
                            (if (pos? idx)
                              (assoc extract
                                :start idx
                                :end (+ idx (count text)))))))
                      extractions)))

(def leipzig
  {:name "Leipzig"
   :abstract "Leipzig with more than 540,000 inhabitants. , is the largest city by population (the second largest being Dresden) in the federal state of Saxony, Germany. Leipzig is situated about 150 km south of Berlin at the confluence of the Weisse Elster, Pleiße, and Parthe rivers at the southerly end of the North German Plain. Leipzig has always been a trade city, situated during the time of the Holy Roman Empire at the intersection of the Via Regia and Via Imperii, two important trade routes. At one time, Leipzig was one of the major European centres of learning and culture in fields such as music and publishing. After World War II, Leipzig became a major urban centre within the German Democratic Republic (East Germany) but its cultural and economic importance declined, despite East Germany being the richest economy in the Soviet Bloc. Leipzig later played a significant role in instigating the fall of communism in Eastern Europe, through events which took place in and around St. Nicholas Church. Since the reunification of Germany, Leipzig has undergone significant change with the restoration of some historical buildings, the demolition of others, and the development of a modern transport infrastructure. Nowadays Leipzig is an important economic centre in Germany and has many institutions and opportunities for culture and recreation including a prominent opera house and one of the most modern zoos in Europe. In 2010 Leipzig was ranked among the top 70 world's most livable cities by consulting firm Mercer in their quality of life survey. Also in 2010, Leipzig was included in the top 10 of cities to visit by the New York Times, and ranked 39th globally out of 289 cities for innovation in the 4th Innovation Cities Index published by Australian agency 2thinknow."
   :extractions [
     (extract "Leipzig" 0 6 "http://dbpedia.org/resource/Leipzig")
     (extract "Dresden")
     (extract "Saxony")
     (extract "Germany")
     (extract "Berlin")
     (extract "Weisse Elster")
     (extract "Pleiße")
     (extract "Parthe")
     (extract "North German Plain")
     (extract "Holy Roman Empire")
     (extract "Via Regia")
     (extract "Via Imperii")
     (extract "World War II")
     (extract "German Democratic Repuplic")
     (extract "East Germany")
     (extract "Soviet Bloc")
     (extract "Eastern Europe")
     (extract "St. Nicholas Church")
     (extract "Europe")
     (extract "Mercer")
     (extract "New York Times")
     (extract "2thinknow")
   ]})
