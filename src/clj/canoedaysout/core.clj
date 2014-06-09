(ns canoedaysout.core
  (:require [canoedaysout.trip :refer [get-trip latest-trip trip-titles-by-county trip-titles-by-waterway]]
            [canoedaysout.utils :refer :all]
            [ring.adapter.jetty]
            [ring.util.response :refer [redirect]]
            [clostache.parser :refer [render-resource]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]))

(defn make-title [trip]
  (str (:county trip) " - " (:waterway trip) " - " (:start trip) " to " (:finish trip) " - CanoeDaysOut"))

(defroutes app
  (GET "/" [] (render-resource "base.tmpl" {:latest-trip (latest-trip)
                                            :content (auto-paragraph (render-resource "maintext.tmpl" {}))
                                            :title "Canoe Days Out"}))
  (GET "/input" [] (render-resource "base.tmpl" {:latest-trip (latest-trip)
                                                 :content (render-resource "input.tmpl" {})
                                                 :title "Input a trip"}))
  (GET "/trips/by-county" [] (render-resource "base.tmpl"
                                              {:latest-trip (latest-trip)
                                               :content (render-resource "triplist.tmpl"
                                                                         {:trips (trip-titles-by-county)})
                                               :title "Trips by county"}))
  (GET "/trips/by-waterway" [] (render-resource "base.tmpl"
                                                {:latest-trip (latest-trip)
                                                 :content (render-resource "triplist-waterway.tmpl"
                                                                           {:trips (trip-titles-by-waterway)})
                                                 :title "Trips by waterway"}))
  (GET "/trip/:id" [id]
       (let [trip (get-trip id)]
         (render-resource "base.tmpl" {:latest-trip (latest-trip)
                                       :content (render-resource "trip.tmpl" trip)
                                       :title (make-title trip)}))))

(defroutes test-app
  app
  ;; Refer to the main site for images so I don't need to store them
  ;; all locally
  (GET "/static/images/*" {:keys [uri] :as request} (redirect (str "http://new.canoedaysout.com" uri)))
  (route/resources "/static/"))

(defn run-test-server []
  (ring.adapter.jetty/run-jetty
   #'test-app
   {:port 8080 :join? false}))

