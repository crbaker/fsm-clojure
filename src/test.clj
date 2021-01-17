(require '[clojure.spec.alpha :as s]
          '[fsm.core :as fsm])

(def fsm-example {:rules [{:when :open
                           :transitions [{:permit :start
                                          :run (fn [m] (assoc m :surname "baker"))
                                          :then :busy}
                                         {:permit :close
                                          :if (fn [m] (not (= "chris" (m :name))))
                                          :then :open}
                                         {:permit :close
                                          :if (fn [m] (= "chris" (m :name)))
                                          :then :closed}]}
                          {:when :busy
                           :transitions [{:permit :close
                                          :run (fn [m] (assoc m :outcome :success))
                                          :then :closed}]}]})

;; test execute the :start trigger on the map. The current state of the map is :open
;; therefore the new state will change to :busy and the resulting map
;; will have a new key added becuase of the :run method
(s/def ::state (s/and keyword? #(= :busy %)))
(s/def ::name (s/and string? #(= "chris" %)))
(s/def ::expected (s/keys :req-un [::state  ::name ::surname]))

(s/explain ::expected
           (fsm/trigger fsm-example
                    :start
                    {:state :open :name "chris"}))

;; test execute the :close trigger on the map. The current state of the map is :open
;; therefore the new state will be :closed becuase the name is chris
(s/def ::state (s/and keyword? #(= :closed %)))
(s/def ::name (s/and string? #(= "chris" %)))
(s/def ::expected (s/keys :req-un [::state  ::name]))

(s/explain ::expected
           (fsm/trigger fsm-example
                    :close
                    {:state :open :name "chris"}))

;; test execute the :close trigger on the map. The current state of the map is :open
;; therefore the new state will remain :open becuase the name is not chris
(s/def ::state (s/and keyword? #(= :open %)))
(s/def ::name (s/and string? #(= "emma" %)))
(s/def ::expected (s/keys :req-un [::state  ::name]))

(s/explain ::expected
           (fsm/trigger fsm-example
                    :close
                    {:state :open :name "emma"}))