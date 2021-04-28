(ns mentat.sketch.ellipsoid
  (:refer-clojure :exclude [ + - * /])
  (:require [mentat.sketch.mechanics :as mm]
            [sicmutils.env :refer [+ - * / up down cos sin square]]
            [quil.core :as q #?@(:cljs [:include-macros true])]
            [quil.middleware :as m]))

;; # Particle on an Ellipse

(defn L-free-3d [m]
  (fn [[_ _ qdot]]
    (* (/ 1 2) m (square qdot))))

(defn elliptical->rect [a b c]
  (fn [[_ [θ φ] _]]
    (up (* a (sin θ) (cos φ))
        (* b (sin θ) (sin φ))
        (* c (cos θ)))))

(defn draw-ellipse [ball-radius a b c convert]
  (fn [{:keys [state color]}]
    (q/background 100)

    (let [[x y z] (convert state)]
      ;; Move origin point to the center of the sketch.
      (q/with-translation [(/ (q/width) 2)
                           (/ (q/height) 2)]

        (q/fill 0 255 255 50)
        #?(:clj
           (q/sphere a)
           :cljs
           (q/ellipsoid a b c))


        ;; Draw the circle.
        (q/fill color 255 255)
        (q/with-translation [x y z]
          (q/sphere ball-radius))))))

(defn render [ball-radius a b c]
  (fn [node width height]
    (let [m 1
          a-path (+ ball-radius a)
          b-path (+ ball-radius b)
          c-path (+ ball-radius c)
          initial-state (up 0 (up 1 1) (up 1.5 1.2))
          L (mm/transform (L-free-3d m)
                          (elliptical->rect a-path b-path c-path))
          built (mm/build L initial-state)]
      (q/sketch
       :host node
       :size [width height]
       :setup (:setup built)
       :update (:update built)
       :draw (draw-ellipse ball-radius a b c (:xform built))
       :features [:keep-on-top]
       :renderer :p3d
       :middleware [m/fun-mode m/navigation-3d]))))
