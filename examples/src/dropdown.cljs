(ns polyhedra.examples.dropdown
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [dommy.macros :refer [node]])
  (:require
    [cljs.core.async :refer [>!]]
    [dommy.core :refer [set-html!]]
    [big-bang.core :refer [big-bang]]
    [big-bang.events.browser :refer [target]]
    [big-bang.package :refer [make-package]]))

(defn make-dropdown
  "Returns a labelled HTML select bound to a channel"
  [& {:keys [id label-text options initial-value send-channel]}]
  (let [id-str (name id)
        element (node
                  [:select {:id id-str :name id-str}
                    (for [[k v] (sort-by second options)]
                      [:option
                       (if (= k initial-value)
                                 {:value k :selected "selected"}
                                 {:value k })
                       v])])]
    (big-bang
      :initial-state {id initial-value}
      :send-channel send-channel
      :event-target element

      :on-change (fn [event world-state]
                   (let [value (-> event target .-value)]
                     (make-package value {id value}))))

    (go (>! send-channel {id initial-value}))

    (node
     [:span.dropdown
       [:label {:for id-str} label-text]
       element])))
