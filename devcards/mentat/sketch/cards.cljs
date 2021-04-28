(ns ^:figwheel-hooks mentat.sketch.cards
  (:require [devcards.core :as dc
             :refer [defcard defcard-doc defcard-rg]
             :include-macros true]
            [mentat.sketch.driven :as driven]
            [mentat.sketch.ellipsoid :as ell]
            [mentat.sketch.particle :as part]
            [mentat.sketch.pendulum :as pend]
            [mentat.sketch.spring :as spring]
            [sablono.core :as sab :include-macros true]
            [reagent.core :as r :include-macros true]
            [reagent.dom :as d]
            [sicmutils.env :as e :refer [up down pi /]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(enable-console-print!)

(defn canvas []
  (r/create-class
   {:component-did-mount
    (fn [component]
      (let [node (d/dom-node component)
            width (/ (.-innerWidth js/window) 2)
            height (/ (.-innerHeight js/window) 2)]
        (driven/render node width height)))
    :render
    (fn [] [:div])}))

(defcard-rg driven-pendulum
  "*Code taken from the Reagent readme.*"
  (fn [data-atom _]
    (let [running? (:running? @data-atom)]
      [:div
       [:h3 "Driven"]
       [:div
        [:button {:on-click #(swap! data-atom update :running? not)}
         (if running? "stop" "start")]]
       (when running?
         [canvas])]))
  (r/atom {:running? false})
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

;; code from the reagent page adapted to plain reagent
(defn calc-bmi [bmi-data]
  (let [{:keys [height weight bmi] :as data} bmi-data
        h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))

(defn slider [bmi-data param value min max]
  (sab/html
   [:input {:type "range" :value value :min min :max max
            :style {:width "100%"}
            :on-change (fn [e]
                         (swap! bmi-data assoc param (int (.-target.value e)))
                         (when (not= param :bmi)
                           (swap! bmi-data assoc :bmi nil)))}]))

(defn bmi-component [bmi-data]
  (let [{:keys [weight height bmi]} (calc-bmi @bmi-data)
        [color diagnose] (cond
                           (< bmi 18.5) ["orange" "underweight"]
                           (< bmi 25) ["inherit" "normal"]
                           (< bmi 30) ["orange" "overweight"]
                           :else ["red" "obese"])]
    (sab/html
     [:div
      [:h3 "BMI calculator"]
      [:div {:id "cake"}
       [:span (str "Height: " (int height) "cm")]
       (slider bmi-data :height (int height) 100 220)]
      [:div
       [:span (str "Weight: " (int weight) "kg")]
       (slider bmi-data :weight weight 30 150)]
      [:div
       [:span (str "BMI: " (int bmi) " ")]
       [:span {:style {:color color}} diagnose]
       (slider bmi-data :bmi bmi 10 50)]])))

(defcard bmi-calculator
  "*Code taken from the Reagent readme.*"
  (fn [data-atom _]
    (bmi-component data-atom))
  {:height 180 :weight 80}
  {:inspect-data true
   :frame true
   :history true})

(defn render []
  (devcards.core/start-devcard-ui!))

(defn ^:after-load render-on-reload []
  (render))

(render)
