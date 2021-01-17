(ns fsm.core
  (:require [clojure.string :as string]
            [fsm.spec :as fsm-spec]))

;; To dump to mermaid syntax
;; 
(defn sanitise [kw converter]
  (converter (name kw)))

(defn dump-transition [s tr]
  (str (sanitise s string/upper-case) " --> " (sanitise (tr :then) string/upper-case)
       " : " (sanitise (tr :permit) string/lower-case)))

(defn dump-rule [r]
  (map (fn [tr] (dump-transition (r :when) tr)) (r :transitions)))

(defn dump [fsm]
  (list "``` mermaid" "\n"
        "stateDiagram-v2" "\n"
        (clojure.string/join "\n" (flatten (map dump-rule fsm)))
        "\n" "```"))

;; FSM
(defn rule-for-state
  "find the configured rule for a state"
  [fsm s]
  (let [rule? (filter (fn [r] (= s (r :when))) fsm)]
    (if (= 1 (count rule?))
      (nth rule? 0)
      (throw (ex-info (str "no (or multiple) rules for state " s)
                      {:desired-state s})))))

(defn add-runner
  "adds a dummy unit runner if runner not already present"
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
  (let [transition? (filter
                     (fn [tr] (transition-matches tr t map))
                     (r :transitions))]
    (if (= 1 (count transition?))
      (nth transition? 0)
      (throw
       (ex-info
        (str "no matching transition in rule " (r :when) " for trigger " t)
        {:rule (r :when) :trigger t})))))

(defn trigger
  "advance the map to the next state"
  [fsm t map]
  (if (fsm-spec/valid? fsm)
    (let [transition (find-transition
                      (rule-for-state (fsm :rules) (map :state)) t map)]
      (assoc (((add-runner transition) :run) map)
             :state (transition :then)))
    (fsm-spec/explain fsm)))
