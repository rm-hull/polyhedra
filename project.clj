(defproject rm-hull/cljs-dataview "0.0.1-SNAPSHOT"
  :clojurescript? true
  :description "A Clojure/Clojurescript library for reading netlib polyhedra data files"
  :source-paths ["src/cljs"]
  :url "https://github.com/rm-hull/polyhedra"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :scm {:url "git@github.com:rm-hull/polyhedra"}
  :min-lein-version "2.3.4"
  :global-vars {*warn-on-reflection* true}
  :plugins [[lein-cljsbuild "1.0.0"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [rm-hull/cljs-test "0.0.7"]
                 [rm-hull/cljs-dataview "0.0.1-SNAPSHOT"]
                 [org.clojure/clojurescript "0.0-2138"]]
  :cljsbuild {
    :test-commands {"phantomjs" ["phantomjs" "target/unit-test.js"]}
    :builds {
      :test {:source-paths ["src" "test"]
             :incremental? true
             :compiler {:output-to "target/unit-test.js"
                        :optimizations :whitespace
                        :pretty-print true }}}
              })
