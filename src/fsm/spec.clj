(ns fsm.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::permit keyword?)
(s/def ::then keyword?)
(s/def ::run fn?)
(s/def ::if fn?)
(s/def ::transition (s/keys :req-un [::permit ::then]
                            :opt-un [::run ::if]))

(s/def ::when keyword?)
(s/def ::transitions (s/and (s/* ::transition)
                            #(> (count %) 0)))

(s/def ::rule (s/keys :req-un [::when ::transitions]))
(s/def ::rules (s/and (s/* ::rule)
                      #(> (count %) 0)
                      #(true? (= (count (filter (fn [kv] (> (count (last kv)) 1))
                                                (group-by (fn [r] (r :when)) %))) 0))))

(s/def ::fsm (s/keys :req-un [::rules]))

(defn valid? [fsm]
  (s/valid? ::fsm fsm))

(defn explain [fsm]
  (s/explain ::fsm fsm))