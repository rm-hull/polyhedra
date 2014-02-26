(ns polyhedra.examples.catalogue-list
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [cljs-test.macros :refer [deftest is= is is-thrown?]])
  (:require
    [clojure.string :refer [split-lines]]
    [cljs-test.core :as test]
    [cljs.core.async :refer [<!]]
    [dataview.loader :refer [fetch-blob fetch-text]]
    [dataview.ops :refer [create-reader]]
    [polyhedra.core :refer [shape-spec]]))

(def url-prefix "https://raw.github.com/rm-hull/polyhedra/master/resources/netlib/")


(defn proxy-request [url]
  (str "http://programming-enchiladas.destructuring-bind.org/proxy?url=" (js/encodeURI url)))

(go
  (let [catalog-url (proxy-request (str url-prefix "names.txt"))
        names (vec (split-lines (<! (fetch-text catalog-url))))]

    (println "\n\nLoading netlib shapes\n")
    (doseq [n (range (count names))
            :let [url (proxy-request (str url-prefix n))]]
      (go
        (let [shape (-> (<! (fetch-blob url)) create-reader shape-spec)]
          (println
            (if (= (nth names n) (:name shape)) "[OK]" "[NAME MISMATCH]")
            "Processed shape" (:number shape) "as:" (:name shape)))))))
