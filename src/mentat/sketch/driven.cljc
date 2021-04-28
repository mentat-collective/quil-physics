(ns mentat.sketch.driven
  (:refer-clojure :exclude [+ *]
                  :rename {/ core:div - core:-})
  (:require [mentat.sketch.mechanics :as mm]
            [mentat.sketch.particle :as p]
            [sicmutils.env :as e :refer [+ - * / up down cos sin pi]]
            [quil.core :as q #?@(:cljs [:include-macros true])]
            [quil.middleware :as m]))

;; # Driven Pendulum

(defn driven-pendulum->rect
  "Convert to rectangular coordinates from a single angle."
  [l yfn]
  (fn [[t [theta]]]
    (up (* l (sin theta))
        (- (yfn t)
           (* l (cos theta))))))

(defn draw-driven [convert support-fn]
  (fn [{:keys [state color time tick] :as m}]
    (q/background 100)
    (q/fill color 255 255)

    (let [[x y] (convert state)
          [x_s y_s] (support-fn (e/state->t state))]

      (q/with-translation [(core:div (q/width) 2)
                           (core:div (q/height) 2)]
        (q/line x_s (core:- y_s) x (core:- y))
        (q/ellipse x (core:- y) 8 8)))))

(defn render [div-id width height]
  (let [m 1
        l 80
        g 9.8
        yfn (fn [t]
              (* 40 (cos (* t 5))))
        L (-> (p/L-particle m g)
              (mm/transform
               (driven-pendulum->rect l yfn)))
        initial-state (up 0
                          (up (/ pi 4))
                          (up 0))
        built (mm/build L initial-state)]

    (q/sketch
     :host div-id
     :size [width height]
     :setup (:setup built)
     :update (:update built)
     :draw (draw-driven (:xform built) (fn [t] [0 (yfn t)]))
     :middleware [m/fun-mode])))
