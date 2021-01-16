## Clojure Finite State Machine

Usage:

```clojure
(def fsm-example [{:when :open
           :transitions [{:permit :start
                          :run (fn [m] (assoc m "surname" "baker"))
                          :then :busy}
                         {:permit :close
                          :if (fn [m] (not (= "chris" (m "name"))))
                          :then :open}
                         {:permit :close
                          :if (fn [m] (= "chris" (m "name")))
                          :then :closed}]}
          {:when :busy
           :transitions [{:permit :close
                          :run (fn [m] (assoc m :outcome :success))
                          :then :closed}]}])

;; execute the :start trigger on the map. The current state of the map is :open
;; therefore the new state will change to :busy and the resulting map
;; will have a new key added becuase of the :run method
(trigger fsm-example
         :start
         {:state :open "name" "chris"})

;; execute the :close trigger on the map. The current state of the map is :open
;; therefore the new state will be :closed becuase the name is chris
(trigger fsm-example
         :close
         {:state :open "name" "chris"})

;; execute the :close trigger on the map. The current state of the map is :open
;; therefore the new state will remain :open becuase the name is not chris
(trigger fsm-example
         :close
         {:state :open "name" "emma"})
```

The Finite State Machine is defined in a map. To transition from one state to another there should be a rule with n (n>0) number of transitions:

The `:run` keyword referes to an optional function that is executed just before the new state is applied. It will accept the map and should return the same or updated map
The `:if` keywords refers to an optional predicate that determines if that transition is applicable to the current state of the map.

There can only be one valid transition, 0 or > 1 will result in an exception

Use the Dump method to generate a Mermaid Graph. The above definition will yeild the following graph:

[Online Mermaid Viewer](https://mermaid-js.github.io/mermaid-live-editor/#/edit/eyJjb2RlIjoiIHN0YXRlRGlhZ3JhbS12MiBcbiBPUEVOIC0tPiBCVVNZIDogc3RhcnRcbk9QRU4gLS0+IE9QRU4gOiBjbG9zZVxuT1BFTiAtLT4gQ0xPU0VEIDogY2xvc2VcbkJVU1kgLS0+IENMT1NFRCA6IGNsb3NlICIsIm1lcm1haWQiOnt9LCJ1cGRhdGVFZGl0b3IiOnRydWV9)

``` mermaid 
 stateDiagram-v2 
 OPEN --> BUSY : start
OPEN --> OPEN : close
OPEN --> CLOSED : close
BUSY --> CLOSED : close 
```