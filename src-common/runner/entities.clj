(ns runner.entities
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]))

(def tile-size 50)
(def block-top 5)
(def block-velocity 600)

(defn create-player
  [x y]
  (assoc (texture "blue-ball.png")
    :x (* x tile-size)
    :y (* y tile-size)
    :width tile-size
    :height tile-size
    :player? true))

(defn- create-simple-block
  [x y width height]
  (assoc (texture "green-block.png")
    :x (* x tile-size)
    :y (* y tile-size)
    :width (or width tile-size)
    :height (or height tile-size)))

(defn create-block
  [x y & {:keys [width height]}]
  (let [block (create-simple-block x y width height)]
    (assoc block
      :block? true
      :velocity block-velocity)))

(defn create-ground
  [x y & {:keys [width height]}]
  (let [block (create-simple-block x y width height)]
    (assoc block :ground? true)))

(defn create-score-panel
  []
  (assoc (label "Score" (color :white))
    :x 3
    :score? true))

(defn create-best-score-panel
  []
  (assoc (label "Best" (color :white))
    :x 3
    :y 20
    :best-score? true))

(defn create-background
  [x]
  (assoc (texture "background.png")
    :x x
    :width (game :width)
    :height (game :height)
    :background? true))
