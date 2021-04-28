(ns mentat.sketch.mechanics
  "Mechanics helpers for interacting with sicmutils."
  (:refer-clojure :exclude [partial zero? + - * /])
  (:require [sicmutils.env :as e :refer [+ - * / up cos sin]]
            [sicmutils.numerical.ode :as ode]
            [sicmutils.structure :as s]
            [quil.core :as q])
  #?(:clj
     (:import (org.apache.commons.math3.ode.nonstiff
               GraggBulirschStoerIntegrator))))

(defn timestamp []
  (try (/ (q/millis) 1000.0)
       (catch #?(:clj Exception :cljs js/Error) _
         0)))

(defn setup-fn
  "I THINK this can stay the same for any of the Lagrangians."
  [initial-state]
  (fn []
    (q/frame-rate 60)
    ;; Set color mode to HSB (HSV) instead of default RGB.
    (q/color-mode :hsb)
    {:state initial-state
     :time (timestamp)
     :color 0
     :tick 0
     :navigation-2d {:zoom 4}}))

(defn Lagrangian-updater
  "Returns an update function that uses the supplied Lagrangian to map state to
  state."
  [L initial-state]
  #?(:clj
     (let [state-derivative (e/Lagrangian->state-derivative L)
           {:keys [integrator equations dimension] :as m}
           (ode/integration-opts (constantly state-derivative)
                                 []
                                 initial-state
                                 {:epsilon 1e-6
                                  :compile? true})
           buffer (double-array dimension)]
       (fn [{:keys [state time color tick] :as m}]
         (let [s (double-array (flatten state))
               t2 (timestamp)]
           ;; Every update step uses `integrator` to push forward from `time` to
           ;; `t2`, the current timestamp. This is probably NOT the best way! The
           ;; integrator can run much faster than realtime, and it's most efficient
           ;; when we let it do that, and let it internally hit a callback at
           ;; specified intervals. But that is not how the functional API works for
           ;; Quil.
           ;;
           ;; Should we instead let it run forward in a different thread at larger
           ;; steps and feed some data structure? Or just... not worry about it??
           (.integrate ^GraggBulirschStoerIntegrator integrator
                       equations time s t2 buffer)

           ;; Every tick of the integator gives us these new fields.
           (merge m {:color (mod (inc color) 255)
                     :state (s/unflatten buffer state)
                     :time t2
                     :tick (inc tick)}))))

     :cljs
     (let [state-derivative (e/Lagrangian->state-derivative L)
           {:keys [integrator equations dimension] :as m}
           (ode/integration-opts (constantly state-derivative)
                                 []
                                 initial-state
                                 {:epsilon 1e-6
                                  :compile? true})]
       (fn [{:keys [state time color tick] :as m}]
         (let [s  (into-array (flatten state))
               t2 (timestamp)]
           (let [output (.solve integrator equations time s t2)
                 new-state (s/unflatten (.-y output) initial-state)]
             (merge m {:color (mod (inc color) 255)
                       :state new-state
                       :time t2
                       :tick (inc tick)})))))))

;; # API Attempt

(defn init
  "Generates an initial state dictionary."
  [L]
  {:lagrangian L
   :transforms []})

(defn transform
  "Takes a state dictionary and stores the coordinate transformation, AND composes
  it with the Lagrangian."
  [m f]
  (if-not (map? m)
    (transform (init m) f)

    ;; `F->C` turns a function that JUST transforms coordinates into a new
    ;; function that transforms the whole local tuple.
    (let [xform (e/F->C f)]
      (update m :transforms conj xform))))

(defn build
  "Returns the final keys for the sketch."
  [m initial-state]
  (if-not (map? m)
    (build (init m) initial-state)
    (let [{:keys [lagrangian transforms] :as m} m
          xform (apply e/compose (rseq transforms))
          L (e/compose lagrangian xform)]
      {:setup (setup-fn initial-state)
       :update (Lagrangian-updater L initial-state)
       :xform (e/compose e/coordinate xform)})))
