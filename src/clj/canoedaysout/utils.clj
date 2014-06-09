(ns canoedaysout.utils)

(defn only-n [n coll]
  (let [c (count coll)
        most-points (for [x (range (dec n))] (* c (/ x (dec n))))
        points (concat most-points [(dec c)])]
    (map #(nth coll %) points)))

(defn dbg [x] (println x) x)


(defn auto-paragraph [text]
  (when text
    (reduce (fn [text addition] (str text "<p>" addition "</p>"))
            "" (clojure.string/split text #"\n"))))

(defn geojson [coll]
  (if (== 1 (count coll))
    (let [point (first coll)]
      {:type "Point"
       :coordinates [(:lng point) (:lat point)]})
    {:type "LineString"
     :coordinates (mapv (fn [point] [(:lng point) (:lat point)]) coll)}))

(defn un-geojson [geojson]
  (if (= "Point" (:type geojson))
    [{:lat (second (:coordinates geojson))
      :lng (first (:coordinates geojson))}]
    (mapv (fn [point] {:lng (first point) :lat (second point)}) (:coordinates geojson))))


