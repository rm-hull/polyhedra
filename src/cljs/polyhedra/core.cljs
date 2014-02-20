(ns polyhedra.core
  (:require [dataview.ops :refer [read-utf8-string]]))

(defn parse-float [s]
  (js/parseFloat s))

(defn value-spec
  "Parses a netlib value: a value consists of a floating point number
   optionally followed by a	expression enclosed by '[]'.  The expression
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
