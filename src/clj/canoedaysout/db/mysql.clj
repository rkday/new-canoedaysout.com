(ns canoedaysout.trip
  (:require [canoedaysout.utils :refer :all]
            [clojure.java.io :refer [file]]
            [yesql.core :refer [defquery]]
            [clj-http.client :as client])
  (:import java.util.Calendar))

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/canoedaysout"
               :user "root"})

(defquery trip "trip.sql")
(defquery get-all-trips "all-trips.sql")

(defn photos-for-id [id]
  "Discovers the list of photos for a trip by parsing the folders on disk"
  (sort-by (fn [s] [(count s) s])
           (filter #(.startsWith % "image")
                   (apply vector (.list (file (str "/Users/robertday/canoedaysout.com/Images/album" id)))))))

(defn to-route [lat lng elat elng]
  "Reformats a start and end point from the database into a format that can be passed to the geojson utility function"
  (if (= elat elng 0.0)
    [{:lat lat, :lng lng}]
    [{:lat lat, :lng lng}
     {:lat elat, :lng elng}]))

(defn coords-from-pedometer [pedometer-id]
  "Some data from te legacy database is held at gmap-pedometer.com. This retrieves the data from there and reformats it."
  (-> (pedometer-post pedometer-id)
      :body
      (clojure.string/split  #"(&|=)")
      (#(partition 2 %))
      (#(reduce
         (fn [coll [k v]] (assoc coll k v))
         {}
         %))
      (get "polyline")
      (clojure.string/split  #"a")
      (#(partition 2 %))
      (#(map (fn [[lat lng]] {:lat (Double/parseDouble lat)
                             :lng (Double/parseDouble lng)}) %))))

(defn pedometer-post [id]
  (client/post "http://www.gmap-pedometer.com/gp/ajaxRoute/get"
               {:body  (str "rId=" id)
                :headers {"Accept-Encoding" "gzip, deflate"
                          "Accept-Language" "en-US,en;q=0.5"
                          "Accept" "*/*"
                          "Cache-Control" "no-cache"
                          "Connection" "keep-alive"
                          "Host" "www.gmap-pedometer.com"
                          "Content-Type" "application/x-www-form-urlencoded; charset=UTF-8"
                          "X-Requested-With" "XMLHttpRequest" }}))

(defn convert [datefield]
  "Dates in the legacy MySQL database are either a year or a Unix timestamp - convert them properly"
  (.getTime
   (if (> 3000 datefield)
     (doto (Calendar/getInstance) (.set 2008 0 1 0 0 0))
     (doto (Calendar/getInstance) (.setTimeInMillis (* 1000 1386370187))))))

(defn transform [db-result]
  "Reformats the result from the MySQL database into a standard form"
  (merge (select-keys db-result [:start :finish :waterway :county :launch :directions :description :email])
         {:legacy-id (:id db-result)
          :active (= "1" (:active db-result))
          :emailok (= "1" (:emailok db-result))
          :date (convert (:date db-result))
          :submitter (:name db-result)
          :links (if (and (:links db-result) (:linkurls db-result))
                   (mapv (fn [[name url]] {:url url :caption name})
                                               (partition 2 (interleave (clojure.string/split (:links db-result) #"\r?\n")
                                                                        (clojure.string/split (:linkurls db-result) #"\r?\n"))))
                   [])
          :photos (mapv (fn [[name url]] {:image url :caption name})
                        (partition 2 (interleave (if (:captions db-result)
                                                   (clojure.string/split (:captions db-result) #"\r?\n")
                                                   [])
                                                (photos-for-id (:id db-result)))))
          :geojson-route  (geojson (if (and (:pedometer db-result) (pos? (:pedometer db-result)))
                            (only-n 20 (coords-from-pedometer (:pedometer db-result)))
                            (to-route (:latitude db-result)
                                      (:longitude db-result)
                                      (:end_latitude db-result)
                                      (:end_longitude db-result))))}))

(defn get-trip [id]
  "Retrieves a trip from the legacy MySQL database"
  (transform (first (trip mysql-db id))))

(comment
  (defn convert-to-mongo [id]
    (m/insert! :trips (get-trip id)))

  (defn convert-all-to-mongo []
    (map #(m/insert! :trips (transform %)) (get-all-trips mysql-db))))

