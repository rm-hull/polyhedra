(ns polyhedra.examples.demo
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [dommy.macros :refer [sel1 node]])
  (:require
    [clojure.string :refer [split-lines]]
    [cljs.core.async :refer [chan <! >!]]
    [dommy.core :refer [insert-after!]]
    ;[enchilada :refer [ctx canvas canvas-size]]
    [wireframes.renderer.canvas :refer [draw-solid ->canvas]]
    [wireframes.renderer.color :refer [wireframe solid]]
    [wireframes.transform :refer [combine rotate scale translate degrees->radians]]
    [wireframes.shapes.primitives :refer [transform-shape center-at-origin]]
    [big-bang.core :refer [big-bang]]
    [dataview.loader :refer [fetch-blob fetch-text]]
    [dataview.ops :refer [create-reader]]
    [polyhedra.core :refer [shape-spec]]
    [polyhedra.examples.dropdown :refer [make-dropdown]]))


(enable-console-print!)

(def canvas (.getElementById js/document "canvas-area"))
(def ctx (when canvas (.getContext canvas "2d")))
(def width 800) ; 28 + newline
(def height 600)
(def initial-k 0.45)
(def initial-persistence 95)
(def canvas { :x (quot width -2) :y (quot height -2) :w width :h height})

(def url-prefix "https://raw.github.com/rm-hull/polyhedra/master/resources/netlib/")

(defn proxy-request [url]
  (str "http://programming-enchiladas.destructuring-bind.org/proxy?url=" (js/encodeURI url)))

(defn shapes> [dest-chan]
  (let [src-chan (chan 1)]
    (go
      (loop []
        (when-let [msg (<! src-chan)]
          (->>
            (:polyhedra msg)
            (str url-prefix)
            proxy-request
            fetch-blob
            <!
            create-reader
            shape-spec
            center-at-origin
            (hash-map :shape)
            (>! dest-chan))
          (recur))))
    src-chan))

(defn to-keyword> [dest-chan]
  (let [src-chan (chan 1)]
    (go
      (loop []
        (when-let [msg (<! src-chan)]
          (>! dest-chan (update-in msg [:style] keyword))
          (recur))))
    src-chan))

(defn color-fn [style color]
  (condp = style
    :transparent (wireframe color style)
    :translucent (wireframe color style)
    :opaque (wireframe color style)
    :shaded (solid color)))

(def transform
  (memoize
    (fn [x y z depth]
      (combine
        (rotate :x (degrees->radians x))
        (rotate :y (degrees->radians y))
        (rotate :z (degrees->radians z))
        (translate 0 0 depth)))))

(def color 0xEAF5FC)
(def styles (map name [:transparent :translucent :opaque :shaded]))

(def initial-state
  {:depth 16
   :focal-length 3
   :style (first styles)
   :color color
   :x 0
   :y 0
   :z 0 })

(defn render-shape
  "Draws the shape at the given state of the world (the x,y,z rotation angles)"
  [{:keys [shape style color focal-length depth x y z]}]
  (when shape

    (set! (.-fillStyle ctx) "rgba(255,255,255,0.75")
    (.fillRect ctx  0 0 width height)

    ((->canvas ctx)
      (partial draw-solid
        {:style style
         :focal-length focal-length
         :color-fn (color-fn style color)
         :shape shape
         :transform (transform x y z depth)})
      [width height])))

(defn update-state
  "Increment/decrement the rotation angles around the x,y and z axes"
  [event {:keys [x y z] :as world-state}]
  (->
    world-state
    (assoc :x (+ x 0.3))
    (assoc :y (- y 0.7))
    (assoc :z (+ z 0.5))))

(defn handle-incoming-msg [event world-state]
  (println "handle-incoming-msg: " event)
  (merge world-state event))

(defn start []
  (let [updates-chan (chan 1)

        ]
    (go
      (let [catalog-url (proxy-request (str url-prefix "names.txt"))
            names (vec (split-lines (<! (fetch-text catalog-url))))]
        (->>
          (sel1 :#canvas-area)
          (insert-after! (node
                           [:div
                             (make-dropdown
                               :id :polyhedra
                               :label-text "Polyhedra:"
                               :initial-value (rand-int (count names))
                               :options (zipmap (iterate inc 0) names)
                               :send-channel (shapes> updates-chan))
                             (make-dropdown
                               :id :style
                               :label-text "Style:"
                               :initial-value (second styles)
                               :options (zipmap styles styles)
                               :send-channel (to-keyword> updates-chan))
                            ])))))

    (big-bang
      :initial-state initial-state
      :on-tick update-state
      :to-draw render-shape
      :receive-channel updates-chan
      :on-receive handle-incoming-msg)))


