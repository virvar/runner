(ns runner.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [runner.logic :as logic]))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage))
    (logic/init-game screen))

  :on-render
  (fn [screen entities]
    (clear!)
    (->> entities
         (logic/update-game! screen)
         (render! screen))))

(defgame runner-game
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
