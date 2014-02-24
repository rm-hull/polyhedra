(ns polyhedra.helpers)

(defn create-dataview [size]
  (->
    (js/ArrayBuffer. size)
    (js/DataView.)))

(defn set-binary-data! [data-view offset data]
  (let [offsets (iterate inc offset)
        pairs (partition 2 (interleave offsets data))]
    (doseq [[i n] pairs]
      (.setUint8 data-view i (if (string? n) (.charCodeAt n 0) n)))))
