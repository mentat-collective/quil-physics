(ns mentat.sketch.spring
  (:refer-clojure :exclude [partial zero? + - * /])
  (:require [mentat.sketch.mechanics :as mm]
            [mentat.sketch.particle :as mp]
            [sicmutils.env :as e
             :refer [+ - * / up down cos sin square pi]]
            [sicmutils.numerical.ode :as ode]
            [sicmutils.structure :as s]
            [quil.core :as q #?@(:cljs [:include-macros true])]
            [quil.middleware :as m]))

;; # Harmonic Oscillator

(defn L-harmonic
  "Lagrangian for a harmonic oscillator."
  [m k]
  (fn [[_ q qdot]]
    (- (* (/ 1 2) m (square qdot))
       (* (/ 1 2) k (square q)))))

(defn render [node width height]
  (let [m 1
        k 9.8
        L (L-harmonic m k)
        initial-state (up 0 (up 5 5) (up 4 10))
        built (mm/build L initial-state)]
    (q/sketch
     :host node
     :size [width height]
     :setup (:setup built)
     :update (:update built)
     :draw (mp/draw (:xform built))
     :features [:keep-on-top]
     :middleware [m/fun-mode m/navigation-2d])))
