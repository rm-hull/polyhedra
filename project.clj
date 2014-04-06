(defproject rm-hull/polyhedra "0.0.1-SNAPSHOT"
  :clojurescript? true
  :description "A Clojure/Clojurescript library for reading netlib polyhedra data files"
  :source-paths ["src"]
  :url "https://github.com/rm-hull/polyhedra"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :scm {:url "git@github.com:rm-hull/polyhedra"}
  :min-lein-version "2.3.4"
  :global-vars {*warn-on-reflection* true}
  :plugins [[lein-cljsbuild "1.0.3"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [rm-hull/dommy "0.1.3-SNAPSHOT"]
                 [rm-hull/cljs-test "0.0.8-SNAPSHOT"]
                 [rm-hull/big-bang "0.0.1-SNAPSHOT"]
                 [rm-hull/cljs-dataview "0.0.1-SNAPSHOT"]
                 [rm-hull/wireframes "0.0.1-SNAPSHOT"]]
  :cljsbuild {
    :test-commands {"phantomjs" ["phantomjs" "target/unit-test.js"]}
    :builds {
      :test {
        :source-paths ["src" "test"]
        :incremental? true
        :compiler {
          :output-to "target/unit-test.js"
          :source-map "target/unit-test.map"
          :static-fns true
          :optimizations :whitespace
          :pretty-print true }}
      :examples {
        :source-paths ["src" "examples"]
        :incremental? true
        :compiler {
          :output-to "target/example.js"
          :source-map "target/example.map"
          :static-fns true
          ;:optimizations :advanced
          :pretty-print true }}}})
