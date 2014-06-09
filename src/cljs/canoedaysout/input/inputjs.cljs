(ns canoedaysout.inputjs
  (:require
    [dommy.utils :as utils]
    [dommy.core :as dommy])
  (:use-macros
    [dommy.macros :only [node sel sel1]]))

(enable-console-print!)

(def files-count (atom 1))

(defn add-file [& args]
  "Add an additional file/caption pair to the form"
  (swap! files-count inc)
  (dommy/append! (sel1 :#fileuploads) [:div.row
                                       [:div.col-xs-6 [:input.form-control {:type "file" :id (str "inputFile" @files-count)}]]
                                       [:div.col-xs-6 [:input.form-control {:type "text" :id (str "inputCaption" @files-count)}]]]))

(def links-count (atom 1))
(defn add-link [& args]
  "Add an additional file/caption pair to the form"
  (swap! links-count inc)
  (dommy/append! (sel1 :#links) [:div.row
                                       [:div.col-xs-6 [:input.form-control {:type "text" :id (str "linkName" @files-count)}]]
                                       [:div.col-xs-6 [:input.form-control {:type "text" :id (str "linkURL" @files-count)}]]]))

(dommy/listen! (sel1 :#add-file) :click add-file)
(dommy/listen! (sel1 :#add-link) :click add-link)

(defn coords [line]
  (map #(identity {:lat (.-lat %) :lng (.-lng %)}) (.getLatLngs line)))


;; Set up the map and allow users to draw their own lines on it
(let [current-line (atom nil)
      mappy (L/map "map")
      center (L/latLng. 51.505 -0.09 )
      layer (L/tileLayer "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png" (js-obj "attribution" "rkd"))
      options (clj->js {:draw
                         {:polyline
                          {:shapeOptions {:color "black"
                                          :weight 5}}
                          :polygon false,
                          :circle false
                          :rectangle false,
                          :marker false}
                         :edit false})]
  (.setView mappy center 6)
  (.addTo layer mappy)
  (.on mappy "draw:created" (fn lineDrawn [e]
                              (when @current-line
                                (.removeLayer mappy @current-line))
                              (let [line (.-layer e)]
                                (.addLayer mappy line)
                                (reset! current-line line)
                                (println (coords line))
                                (dommy/set-attr! (sel1 :#coords) "value" (pr-str (coords line))))))
  (.addControl mappy (L.Control.Draw. options)))
