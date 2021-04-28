(ns mentat.sketch.particle
  (:refer-clojure :exclude [+ - * /])
  (:require [mentat.sketch.mechanics :as mm]
            [sicmutils.env :as e :refer [+ - * / up square]]
            [sicmutils.structure :as s]
            [quil.core :as q #?@(:cljs [:include-macros true])]
            [quil.middleware :as m]))

(defn L-particle
  "Single particle under some potential."
  [m g]
  (fn [[_ [_ y] qdot]]
    (- (* (/ 1 2) m (square qdot))
       (* m g y))))

(defn draw [state->coords]
  (fn [{:keys [state color]}]
    (q/background 100)
    (q/fill color 255 255)
    (let [[x y] (state->coords state)]
      (q/with-translation [(/ (q/width) 2)
                           (/ (q/height) 2)]
        (q/ellipse x (- y) 5 5)))))

(defn render [node width height]
  (let [m 1
        g 9.8
        L (L-particle m g)
        initial-state (up 0 (up 5 5) (up 4 10))
        built (mm/build L initial-state)]
    (q/sketch
     :host node
     :size [width height]
     :setup (:setup built)
     :update (:update built)
     :draw (draw (:xform built))
     :features [:keep-on-top]
     :middleware [m/fun-mode m/navigation-2d])))
