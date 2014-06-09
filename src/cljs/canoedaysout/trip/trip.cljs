(ns canoedaysout.tripjs
  (:require
    [dommy.utils :as utils]
    [dommy.core :as dommy]
    [cljs.reader])
  (:use-macros
    [dommy.macros :only [node sel sel1]]))

(enable-console-print!)

;; Set up the photo gallery

(blueimp/Gallery (-> js/document
                     (.getElementById "links")
                     (.getElementsByTagName "a"))
                 (clj->js {:container "#blueimp-gallery"
                           :carousel true}))

;; Read the route for this trip out of a HTML data div, and display it
;; in a map

(def route (cljs.reader/read-string (dommy/attr (sel1 :#hidden-route) "data-coords")))

(defn convert-coords [coords]
  (let [coords (map (fn [{lat :lat lng :lng}] (L/latLng lat lng)) coords)]
    (if (> (count coords) 1)
      (L/polyline (apply array coords))
      (L/marker (first coords)))))

(defn first-coords [coords]
  ((fn [{lat :lat lng :lng}] (L/latLng lat lng)) (first coords)))

(let [mappy (L/map "trip-map")
      center (first-coords route)
      layer (L/tileLayer "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png" (js-obj "attribution" "rkd"))
      route-layer (convert-coords route)]
  (.setView mappy center 12)
  (.addTo layer mappy)
  (.addTo route-layer mappy))

