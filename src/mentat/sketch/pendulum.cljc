(ns mentat.sketch.pendulum
  (:refer-clojure :exclude [ + - * /])
  (:require [mentat.sketch.mechanics :as mm]
            [sicmutils.env :as e
             :refer [+ - * / up down cos sin square pi]]
            [quil.core :as q #?@(:cljs [:include-macros true])]
            [quil.middleware :as m]))

(defn L-rectangular
  "Accepts:

  - a down tuple of the masses of each particle
  - a potential function of all coordinates

  Returns a function of local tuple -> the Lagrangian for N cartesian
  coordinates."
  [masses U]
  (fn [[_ q qdot]]
    ;; because `masses` is a down tuple, and `(mapv square qdot)` is an up, this
    ;; triggers a contraction!
    (- (* (/ 1 2) masses (mapv square qdot))
       (U q))))

(defn U-uniform-gravity
  "Accepts:

  - a down tuple of each particle's mass
  - g, the gravitational constant
  - optionally, the cartesian coordinate index of the vertical component. You'd
    need to update this if you went into 3d.

  Returns a function of the generalized coordinates to a uniform vertical
  gravitational potential."
  ([masses g]
   (U-uniform-gravity masses g 1))
  ([masses g vertical-coordinate]
   (let [vertical #(get % vertical-coordinate)]
     (fn [q]
       (* g masses (mapv vertical q))))))


;; Think of this next function like a coordinate transform that's targeted at a
;; specific spot in the `q` structure. I want to specify a sequence of all of
;; the angles, and I need to provide a transform from that angle to `x, y`
;; coordinates.
;;
;; but what is the angle? It's the angle of the new pendulum off of vertical,
;; but with its zero point offset to the XY coordinate of the pendulum bob it's
;; getting attached to!
;;
;; So you query the `attachment`'s XY, then use that to transform your index.

(defn attach-pendulum
  "Replaces an angle at the ith index with a pendulum. The supplied attachment
  can be either a:

  - map of the form {:coordinate idx}, in which case the pendulum will attach
    there
  - A 2-tuple, representing a 2d attachment point

  If the attachment index doesn't exist, attaches to the origin."
  [l idx attachment]
  (fn [[_ q]]
    (let [[x y] (if (vector? attachment)
                  attachment
                  (get q (:coordinate attachment) [0 0]))]
      (update
       q idx (fn [angle]
               (up (+ x (* l (sin angle)))
                   (- y (* l (cos angle)))))))))

;; `reduce` means that each angle is transformed to XY before the next angle
;; needs it!

(defn L-chain
  "Lagrangian for a chain of pendulums under the influence of a uniform
  gravitational pull in the -y direction."
  [m lengths g]
  (let [U (U-uniform-gravity m g)]
    (reduce mm/transform
            (L-rectangular m U)
            (map-indexed
             (fn [i l]
               (attach-pendulum
                l i (if (= 0 i)
                      [0 0]
                      {:coordinate (dec i)})))
             lengths))))


;; Drawing API

(defn attach!
  "Attach two points with a line."
  [[x1 y1] [x2 y2]]
  (q/line x1 (- y1) x2 (- y2)))

(defn bob!
  "Generate a bob at that point."
  [[x y]]
  (q/ellipse x (- y) 2 2))

(defn draw-chain [convert]
  (fn [{:keys [state color]}]
    (q/background 100)
    (q/fill color 255 255)

    (let [nodes (convert state)
          pairs (partition 2 1 (cons [0 0] nodes))]
      (q/with-translation [(/ (q/width) 2)
                           (/ (q/height) 2)]
        (doseq [[base end] pairs]
          (attach! base end))

        (doseq [end nodes]
          (bob! end))))))

(defn double-sketch []
  (let [g 98
        masses (down 1 1)
        lengths [4 10]
        L (L-chain masses lengths g)
        initial-state (up 0
                          (up pi (/ pi 2))
                          (up 0 0))
        built (mm/build L initial-state)]

    (q/defsketch double-pendulum
      :title "Double pendulum!"
      :host "app"
      :size [1200 800]
      :setup (:setup built)
      :update (:update built)
      :draw (draw-chain
             (:xform built))
      :features [:keep-on-top :present]
      :middleware [m/fun-mode m/navigation-2d])))

(defn triple-sketch []
  (let [g 98
        masses (down 1 1 1)
        lengths [4 10 12]
        L (L-chain masses lengths g)
        initial-state (up 0
                          (up pi (/ pi 2) (/ pi 2))
                          (up 0 0 0))
        built (mm/build L initial-state)]
    (q/defsketch triple-pendulum
      :title "Triple pendulum!"
      :host "app"
      :size [1200 800]
      :setup (:setup built)
      :update (:update built)
      :draw (draw-chain
             (:xform built))
      :features [:keep-on-top :present]
      :middleware [m/fun-mode m/navigation-2d])))
