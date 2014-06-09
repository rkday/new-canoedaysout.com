(ns canoedaysout.trip
  (:require [canoedaysout.utils :refer :all]
            [somnium.congomongo :as m]
            [clj-refresh-cache.core :refer [memoize-refreshing]])
  (:import org.bson.types.ObjectId java.text.SimpleDateFormat))

(def conn
  (m/make-connection "canoedaysout"
                     :host "127.0.0.1"
                     :port 27017))

(m/set-connection! conn)

(defn get-trip [id]
  "Fetches a single trip from the database and formats it for display"
  (if-let [result (m/fetch-by-id :trips (ObjectId. id))]
          (-> result
              (assoc :has-photos (not (empty? (:photos result))))
              (assoc :has-links (not (empty? (:links result))))
              (update-in [:description] auto-paragraph)
              (update-in [:directions] auto-paragraph)
              (update-in [:launch] auto-paragraph)
              (update-in [:submitter] #(when % (clojure.string/trim %)))
              (assoc :has-submitter (pos? (count (:submitter result))))
              (assoc :datestr (.format (SimpleDateFormat. "MMMM d, yyyy") (:date result)))
              (assoc :route (pr-str (un-geojson (:geojson-route result))))
              (assoc :id (or (:legacy-id result) (.toString (:_id result)))))
          {}))


(defn- latest-trip* []
  (-> (m/fetch :trips :only [:start :finish :waterway :county :submitter :date] :sort {:date -1} :limit 1)
      (first)
      (update-in [:_id] #(.toString %))))

(def latest-trip
  (memoize-refreshing latest-trip* (* 600 1000)))


(defn trip-titles [keyfn]
  "Retrieves all the CanoeDaysOut trips, ordered by the result of applying keyfn"
  (let [trips (m/fetch :trips :only [:start :finish :waterway :county :submitter])]
    (->> trips
         (sort-by keyfn)
         (mapv (fn [m]
                 (-> m
                     (update-in [:submitter] #(when % (clojure.string/trim %)))
                     (assoc :has-submitter (pos? (count (:submitter m))))
                     (update-in [:_id] #(.toString %))))))))

(comment )

(def trip-titles-by-waterway
  (memoize-refreshing (fn [] (trip-titles (fn [trip]
                                           "Ignore the word 'River' when sorting"
                                           (when trip
                                             (clojure.string/replace-first
                                              (:waterway trip)
                                              "River "
                                              "")))))
                      (* 600 1000)))

(def trip-titles-by-county
  (memoize-refreshing (fn [] (trip-titles :county)) (* 600 1000))
  )
