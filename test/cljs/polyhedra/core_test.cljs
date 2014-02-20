(ns polyhedra.core-test
  (:use-macros
    [cljs-test.macros :only [deftest is= is]])
  (:require
    [cljs-test.core :as test]
    [wireframes.transform :as wireframes]
    [dataview.ops :as ops]
    [polyhedra.core :as poly]
    [polyhedra.test-data :as test-data]
    [polyhedra.helpers :refer [create-dataview set-binary-data!]]))

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

(deftest point-spec
  (let [points-data (str "3.52072594216369[(3/2+(7/6)*sqrt(3))] -2.36602540378444[(-3/2+(-1/2)*sqrt(3))] 0[0]\n"
                         "3.52072594216369[(3/2+(7/6)*sqrt(3))] -1.36602540378444[(-1/2+(-1/2)*sqrt(3))] 0[0]\n")
        dataview (create-dataview (count points-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 points-data)

  (is= (poly/point-spec reader) (wireframes/point 3.52072594216369 -2.36602540378444 0) "First point")
  (is= (poly/point-spec reader) (wireframes/point 3.52072594216369 -1.36602540378444 0) "Second point")
  (is= (ops/eod? reader) true "Reader should be at end")))

(deftest vertices-spec
  (let [vertices-data (str ":vertices\n"
                           "4\n"
                           "-.577350269189626[(-1/3)*sqrt(3)] -1[-1] 0[0]\n"
                           "-.577350269189626[(-1/3)*sqrt(3)] 0[0] 0[0]\n"
                           "-.0773502691896257[(1/2+(-1/3)*sqrt(3))] -1.86602540378444[(-1+(-1/2)*sqrt(3))] 0[0]\n"
                           ".288675134594813[(1/6)*sqrt(3)] -.5[-1/2] 0[0]\n")
        dataview (create-dataview (count vertices-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 vertices-data)

  (is= (poly/vertices-spec reader) {:points [(wireframes/point -0.577350269189626 -1 0)
                                             (wireframes/point -0.577350269189626 0 0)
                                             (wireframes/point -0.0773502691896257 -1.86602540378444 0)
                                             (wireframes/point 0.288675134594813 -0.5 0)]} "Four vertices")
  (is= (ops/eod? reader) true "Reader should be at end")))

(deftest vertices-assert-check
  (let [vertices-data (str ":solid\n"
                           "1 5\n"
                           "3 1 2 3\n")
        dataview (create-dataview (count vertices-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 vertices-data)

  (try
    (poly/vertices-spec reader)
    (is false "Should've failed")

    (catch js/Error e
      (is true "Correctly handled incorrect parse")))))
