(ns polyhedra.core-test
  (:use-macros
    [cljs-test.macros :only [deftest is= is is-thrown?]])
  (:require
    [cljs-test.core :as test]
    [wireframes.transform :as wireframes]
    [dataview.ops :as ops]
    [dataview.protocols :as proto]
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
  (is= (proto/eod? reader) true "Reader should be at end")))

(deftest keyword-spec
  (let [keyword-data ":svertices\nnon-prefixed\nsome random String_eh?"
        dataview (create-dataview (count keyword-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 keyword-data)

  (is= (poly/keyword-spec reader) :svertices "Prefixed keyword")
  (is= (poly/keyword-spec reader) :non-prefixed "non-prefixed keyword")
  ;(is= (poly/keyword-spec reader) :some-random-string-eh? "invalid chars") NYI
  ;(is= (proto/eod? reader) true "Reader should be at end")
   ))

(deftest point-spec
  (let [points-data (str "3.52072594216369[(3/2+(7/6)*sqrt(3))] -2.36602540378444[(-3/2+(-1/2)*sqrt(3))] 0[0]\n"
                         "3.52072594216369[(3/2+(7/6)*sqrt(3))] -1.36602540378444[(-1/2+(-1/2)*sqrt(3))] 0[0]\n")
        dataview (create-dataview (count points-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 points-data)

  (is= (poly/point-spec reader) (wireframes/point 3.52072594216369 -2.36602540378444 0) "First point")
  (is= (poly/point-spec reader) (wireframes/point 3.52072594216369 -1.36602540378444 0) "Second point")
  (is= (proto/eod? reader) true "Reader should be at end")))

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
  (is= (proto/eod? reader) true "Reader should be at end")))

(deftest vertices-assert-check
  (let [vertices-data (str ":solid\n"
                           "1 5\n"
                           "3 1 2 3\n")
        dataview (create-dataview (count vertices-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 vertices-data)

    (is-thrown? (poly/vertices-spec reader) "Correctly handled incorrect parse")))

(deftest face-spec
  (let [faces-data (str "3 8 7 12\n"
                        "4 0 2 6 3\n"
                        "5 3 9 12\n"   ; <-- 2 missing points
                        "4 7 6 10 11\n")
        dataview (create-dataview (count faces-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 faces-data)

    (is= (poly/face-spec reader) [8 7 12] "Three-element face")
    (is= (poly/face-spec reader) [0 2 6 3] "Four element face")
    (is-thrown? (poly/face-spec reader) "Mismatch face count & actual")))


(deftest polygons-spec
  (let [polygons-data (str
                        ":solid\n"
                        "12 5\n"
                        "5 10 16 23 24 17\n"
                        "5 37 32 24 23 31\n"
                        "5 25 18 17 24 30\n"
                        "5 4 3 10 17 11\n"
                        "5 1 9 16 10 2\n"
                        "5 22 29 23 16 15\n"
                        "5 15 8 14 21 22\n"
                        "5 0 5 13 14 6\n"
                        "5 12 19 20 13 7\n"
                        "5 33 34 27 20 26\n"
                        "5 36 28 21 27 35\n"
                        "5 27 21 14 13 20\n")
        dataview (create-dataview (count polygons-data))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 polygons-data)

    (is= (poly/polygons-spec reader)
         {:polygons
          [{:vertices [10 16 23 24 17]}
           {:vertices [37 32 24 23 31]}
           {:vertices [25 18 17 24 30]}
           {:vertices [ 4  3 10 17 11]}
           {:vertices [ 1  9 16 10  2]}
           {:vertices [22 29 23 16 15]}
           {:vertices [15  8 14 21 22]}
           {:vertices [ 0  5 13 14  6]}
           {:vertices [12 19 20 13  7]}
           {:vertices [33 34 27 20 26]}
           {:vertices [36 28 21 27 35]}
           {:vertices [27 21 14 13 20]}]}
         "Polygons data produces expected number of faces")))

(deftest shape-spec
  (let [dataview (create-dataview (count test-data/gyroelongated-square-pyramid))
        reader (ops/create-reader dataview)]
    (set-binary-data! dataview 0 test-data/gyroelongated-square-pyramid)
  (is= (poly/shape-spec reader)
        {:name "gyroelongated square pyramid (J10)",
         :number "54",
         :polygons
           [{:vertices [20 23 24]}
            {:vertices [24 23 22]}
            {:vertices [24 22 19]}
            {:vertices [19 22 17]}
            {:vertices [19 17 16]}
            {:vertices [16 17 18]}
            {:vertices [16 18 20]}
            {:vertices [20 18 23]}
            {:vertices [20 24 21]}
            {:vertices [24 19 21]}
            {:vertices [19 16 21]}
            {:vertices [16 20 21]}
            {:vertices [17 22 23 18]}],
         :points
           [[-0.5 0.288675134594813 0 1]
            [0 -0.577350269189626 0 1]
            [0 1.15470053837925 0 1]
            [0.5 0.288675134594813 0 1]
            [1 -1.57735026918963 0 1]
            [1 -0.577350269189626 0 1]
            [1 1.15470053837925 0 1]
            [1.5 0.288675134594813 0 1]
            [2 -1.57735026918963 0 1]
            [2 -0.577350269189626 0 1]
            [2 1.15470053837925 0 1]
            [2.5 0.288675134594813 0 1]
            [3 -0.577350269189626 0 1]
            [3 1.15470053837925 0 1]
            [3.5 0.288675134594813 0 1]
            [4 -0.577350269189626 0 1]
            [3.0404081631282285 -0.18623475304533063 -0.11054892283655414 1]
            [3.1217092883753237 -0.10329876729789654 -1.1037819011964445 1]
            [3.151237686478149 -1.0054078405724787 -0.6732853348673297 1]
            [3.720336928368865 0.43077295950095407 -0.5067791044945713 1]
            [3.7620963894410324 -0.8450019266637827 0.10203497816311095 1]
            [3.8330397992341245 0.09729255774742634 0.4292170945068096 1]
            [4.11280216772905 -0.1328271654007219 -1.2336394315896533 1]
            [4.142330565831875 -1.034936238675304 -0.8031428652605384 1]
            [4.4420251546816685 -0.22799421411749818 -0.29419520349490624 1]]}
       "Test a complex shape"
       )))
