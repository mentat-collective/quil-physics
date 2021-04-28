(ns mentat.sketch.core
  "Some snippets."
  (:require [mentat.sketch.driven :as driven]
            [mentat.sketch.ellipsoid :as ell]
            [mentat.sketch.particle :as part]
            [mentat.sketch.pendulum :as pend]
            [mentat.sketch.spring :as spring]))

(defn ^:export run-sketch []
  ;; Pendula
  ;; particle falling.
  #_(part/render)

  ;; spring!
  #_(spring/render)

  ;; Driven Pendulum
  #_(driven/render)

  ;; particle on an ellipsoid, 3d!
  (ell/render 10 250 40 40))
