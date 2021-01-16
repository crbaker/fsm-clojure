(ns core.fsm
  (:require [clojure.string :as string]))

(def fsm [{:when :open
          :transitions [{:permit :start
                        ;;  :run method accepts the map and must return the same or updated map
                         :run (fn [m] (assoc m "surname" "baker"))
                         :then :busy}
                        {:permit :close
                         :if (fn [m] (not (= "chris" (m "name"))))
                         :then :open}
                        {:permit :close
                        ;;  :if method accepts the map and must return boolean
                         :if (fn [m] (= "chris" (m "name")))
                         :then :closed}]}
          {:when :busy
           :transitions [{:permit :close
                          :run (fn [m] (assoc m :outcome :success))
                          :then :closed}]}])

(defn sanitise [kw converter]
  (converter (name kw)))

(defn dump-transition [s tr]
  (str (sanitise s string/upper-case) " --> " (sanitise (tr :then) string/upper-case)
       " : " (sanitise (tr :permit) string/lower-case )))

(defn dump-rule [r]
  (map (fn [tr] (dump-transition (r :when) tr)) (r :transitions))
)

(defn dump [fsm]
  (list "``` mermaid" "\n"
        "stateDiagram-v2" "\n"
        (clojure.string/join "\n" (flatten (map dump-rule fsm)))
        "\n" "```"))

(defn rule-for-state
  "find the configured rule for a state"
  [fsm s]
  (nth (filter (fn [r] (= s (r :when))) fsm)0))

(defn add-runner
  "adds a dummy runner if runner not already present"
  [tr]
  (if (contains? tr :run) tr (assoc tr :run (fn [map] map))))

(defn add-predicate
  "adds a dummy predicate if a predicate not already present"
  [tr]
  (if (contains? tr :if) tr (assoc tr :if (fn [_] true))))

(defn transition-matches
  "find the transition that match the supplied trigger"
  [tr t map]
  (and (= t (tr :permit)) (((add-predicate tr) :if) map)))

(defn find-transition
  "find the transition for a trigger"
  [r t map]
  (nth
   (filter
    (fn [tr] (transition-matches tr t map) )
    (r :transitions)) 0))

(defn trigger
  "advance the map to the next state"
  [fsm t map]
  (let [transition (find-transition
                    (rule-for-state fsm (map :state)) t map)]
    (assoc (((add-runner transition) :run) map)
           :state (transition :then))))

(trigger fsm
         :close
         {:state :busy "name" "chris"})
