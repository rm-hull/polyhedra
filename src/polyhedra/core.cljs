(ns polyhedra.core
  (:require
    [clojure.string :as str]
    [dataview.protocols :refer [read-utf8-string find! rewind!]]
    [wireframes.transform :refer [point]]))

(defn parse-float [s]
  (js/parseFloat s))

(defn value-spec
  "Parses a netlib value: a value consists of a floating point number
   optionally followed by an expression enclosed by '[]'.  The expression
   is the exact value represented in bc(1) code. Note that this
   implementation currently ignores the expression."
  [reader]
  (let [value (read-utf8-string reader #{\space \newline \[})
        delimiter-pos (dec (count value))
        delimiter (get value delimiter-pos)]
    (when (= delimiter \[) ; read (and ignore) upto next closing square bracket
      (read-utf8-string reader #{\]})
      (read-utf8-string reader #{\space \newline}))
    (parse-float (subs value 0 delimiter-pos))))

(defn keyword-spec
  [reader]
  (let [value (read-utf8-string reader #{\newline})
        offset (if (= (get value 0) \:) 1 0)]
    (keyword
      (subs value offset (dec (count value))))))

(defn point-spec
  [reader]
  (apply point (doall (repeatedly 3 #(value-spec reader)))))

(defn number-list-spec
  [reader]
  (map js/parseInt (str/split (read-utf8-string reader #{\newline}) #" ")))

(defn count-spec
  [reader]
  (first (number-list-spec reader)))

(defn vertices-spec
  [reader]
  (let [kw (keyword-spec reader)]
    (assert
      (= kw :vertices)
      (str "Expected :vertices, got " kw))
    {:points (doall
	       (repeatedly
		 (count-spec reader) ; <== vertex-count
		 #(point-spec reader)))}))

(defn face-spec
  [reader]
  (let [[n & faces] (number-list-spec reader)]
    (assert (= n (count faces)) "Face count does not match actual")
    (vec faces)))

(defn polygons-spec
  [reader]
  (let [kw (keyword-spec reader)]
  (assert
    (= kw :solid)
    (str "Expected :solid, got " kw))
    (let [num-faces (count-spec reader)]
      {:polygons (vec
                   (doall
                     (repeatedly
                       num-faces
                       #(hash-map :vertices (face-spec reader)))))})))

(defn shape-spec
  [reader]
  (let [spec {":name" (fn [reader]
                        {(keyword-spec reader) (str/trim (read-utf8-string reader #{\newline}))} )
              ":solid" polygons-spec
              ":vertices" vertices-spec}]
    (apply merge
      (doseq [[k f] spec
              :when (find! reader k)
              :let  [v (f reader)]]
        (rewind! reader)
        (println "--->" k " produces " v)
        v))))
