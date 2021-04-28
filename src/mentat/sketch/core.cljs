(ns mentat.sketch.core
  "Some snippets."
  (:require [mentat.sketch.driven :as driven]
            [mentat.sketch.ellipsoid :as ell]
            [mentat.sketch.particle :as part]
            [mentat.sketch.pendulum :as pend]
            [mentat.sketch.spring :as spring]))

(defn ^:export run-sketch []
  ;; Pendula
  #_(pend/triple-sketch)
  #_(pend/double-sketch)

  ;; particle falling.
  #_(part/sketch)

  ;; spring!
  #_(spring/sketch)

  ;; Driven Pendulum
  #_(driven/render)

  ;; particle on an ellipsoid, 3d!
  (ell/sketch 10 250 40 40))
