## Clojure Finite State Machine

Usage:

```clojure
(def fsm [{:when :open
          :transitions [{:permit :start
                         :run (fn [m] (assoc m "some" "property"))
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
```

The Finite State Machine is defined in a map. To transition from one state to another there should be a rule with n (n>0) number of transitions:

The `:run` keyword referes to a function that is executed just before the new state is applied
The `:if` keywords refers to a predicate that determines if that transition is applicable.

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