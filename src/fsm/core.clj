(ns fsm.core
  (:require [clojure.string :as string]
            [fsm.spec :as fsm-spec]))

(defn first-match
  "finds the first element in a collection that matches the predicate"
  [f coll]
  (first (filter f coll)))

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
  (clojure.string/join "\n" (list "stateDiagram-v2"
                                  (clojure.string/join "\n" (flatten (map dump-rule fsm))))))

;; FSM
(defn rule-for-state
  "find the configured rule for a state"
  [rules s]
  (let [rule? (filter (fn [r] (= s (r :when))) rules)]
    (if (= 1 (count rule?))
      (nth rule? 0)
      (throw (ex-info (str "no (or multiple) rules for state " s)
                      {:desired-state s})))))

(defn allowed-triggers
  "gets the list of configured triggers that are permissable for the supplied state"
  [fsm s]
  (into [] (set (map (fn [t] (t :permit)) ((rule-for-state (fsm :rules) s) :transitions)))))

(defn add-runner
  "adds a dummy unit runner if runner not already present"
  [tr]
  (if (contains? tr :run) tr (assoc tr :run (fn [m] m))))

(defn add-predicate
  "adds a dummy predicate if a predicate not already present"
  [tr]
  (if (contains? tr :if) tr (assoc tr :if (fn [_] true))))

(defn transition-matches
  "find the transition that match the supplied trigger"
  [tr t entity]
  (and (= t (tr :permit)) (((add-predicate tr) :if) entity)))

(defn find-transition
  "find the transition for a trigger"
  [rules trigger entity]
  (let [transition? (filter
                     (fn [tr] (transition-matches tr trigger entity))
                     (rules :transitions))]
    (if (= 1 (count transition?))
      (nth transition? 0)
      (throw
       (ex-info
        (str "no matching transition in rule " (rules :when) " for trigger " trigger)
        {:rule (rules :when) :trigger trigger})))))

(defn trigger
  "advance the map to the next state"
  [fsm trigger entity]
  (if (fsm-spec/valid? fsm)
    (let [transition (find-transition
                      (rule-for-state (fsm :rules) (entity :state)) trigger entity)]
      (assoc (((add-runner transition) :run) entity)
             :state (transition :then)))
    (fsm-spec/explain fsm)))
