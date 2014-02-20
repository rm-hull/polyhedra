(ns polyhedra.core-test
  (:use-macros [cljs-test.macros :only [deftest is= is]])
  (:require [cljs-test.core :as test]
            [polyhedra.core :as poly]
            [polyhedra.test-data :as test-data]
            [polyhedra.helpers :refer [create-dataview set-binary-data!]]
            [dataview.ops :as ops]))

;test-data/cuboctahedron

(deftest parse-float
  (is= (poly/parse-float "3.7") 3.7 "Simple float")
  (is= (poly/parse-float " 0.7") 0.7 "Leading spaces")
  (is= (poly/parse-float "5.62  ") 5.62 "Trailing spaces")
  (is= (poly/parse-float "-5.4") -5.4 "Simple negative")
  (is= (poly/parse-float ".4") 0.4 "Missing leading zero on x < 1.0")
  (is= (poly/parse-float "-.44354") -0.44354 "Missing leading zero on -1.0 < x < 0.0")
  (is= (poly/parse-float "-.577350269189626") -0.577350269189626 "High precision neg number")
  (is  (js/isNaN (poly/parse-float "str")) "Non-number"))

(deftest value-spec
  (let [points-data "3.7 0[0] -.577350269189626[(-1/3)*sqrt(3)]\n"
        dataview (create-dataview (count points-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 points-data)

  (is= (poly/value-spec reader) 3.7 "Simple point-spec float")
  (is= (poly/value-spec reader) 0.0 "Simple point-spec composite")
  (is= (poly/value-spec reader) -0.577350269189626 "Complex point-spec composite")
  (is= (ops/eod? reader) true "Reader should be at end")))

(deftest keyword-spec
  (let [keyword-data ":svertices\nnon-prefixed\nsome random String_eh?"
        dataview (create-dataview (count keyword-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 keyword-data)

  (is= (poly/keyword-spec reader) :svertices "Prefixed keyword")
  (is= (poly/keyword-spec reader) :non-prefixed "non-prefixed keyword")
  ;(is= (poly/keyword-spec reader) :some-random-string-eh? "invalid chars") NYI
  ;(is= (ops/eod? reader) true "Reader should be at end")
   ))
