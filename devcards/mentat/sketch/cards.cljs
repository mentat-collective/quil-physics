(ns ^:figwheel-hooks mentat.sketch.cards
  (:require [devcards.core :as dc
             :refer [defcard defcard-doc defcard-rg]
             :include-macros true]
            [mentat.sketch.driven :as driven]
            [mentat.sketch.ellipsoid :as ell]
            [mentat.sketch.particle :as part]
            [mentat.sketch.pendulum :as pend]
            [mentat.sketch.spring :as spring]
            [reagent.core :as r :include-macros true]
            [reagent.dom :as d]))

(enable-console-print!)

(defn canvas [ctor]
  (r/create-class
   {:component-did-mount
    (fn [component]
      (let [node  (d/dom-node component)
            width (/ (.-innerWidth js/window) 2)
            height (/ (.-innerHeight js/window) 2)]
        (ctor node width height)))
    :render (fn [] [:div])}))

(def sketches
  {"driven" driven/render
   "double" pend/render-double
   "triple" pend/render-triple
   "freefall" part/render
   "spring" spring/render
   "ellipsoid" (ell/render 10 250 40 40)})

(defcard-rg driven-pendulum
  "*Code taken from the Reagent readme.*"
  (fn [data-atom _]
    (let [running? (:running? @data-atom)]
      [:div
       [:h3 "Physics Demos"]
       [:div
        [:select {:on-change
                  (fn [node]
                    (let [v (.. node -target -value)]
                      (swap! data-atom assoc :selected v)))}
         [:option {:value "driven"} "Driven Pendulum"]
         [:option {:value "double"} "Double Pendulum"]
         [:option {:value "triple"} "Triple Pendulum"]
         [:option {:value "freefall"} "Particle under Gravity"]
         [:option {:value "spring"} "Spring"]
         [:option {:value "ellipsoid"} "Ellipsoid Particle"]]
        [:button {:on-click #(swap! data-atom update :running? not)}
         (if running? "stop" "start")]]
       (when running?
         (let [ctor (sketches (:selected @data-atom))]
           [(canvas ctor)]))]))
  (r/atom {:running? false
           :selected "driven"})
  {:inspect-data true
   :frame true})

(defcard-doc
  "
## Rendering Reagent components
Note: The following examples assume a namespace that looks like this:
```clojure
(ns xxx
    (:require [devcards.core]
              [reagent.core :as reagent])
    (:require-macros [devcards.core :as dc
                                    :refer [defcard defcard-rg]]))
```
")

(defn render []
  (devcards.core/start-devcard-ui!))

(defn ^:after-load render-on-reload []
  (render))

(render)
