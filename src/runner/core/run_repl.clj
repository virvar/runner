(ns runner.core.run-repl
  (:require [play-clj.core :refer :all]
            [runner.core :refer :all]
            [runner.core.desktop-launcher :refer [-main]]))

(defscreen blank-screen
  :on-render
  (fn [screen entities]
    (clear!)))

(set-screen-wrapper! (fn [screen screen-fn]
                       (try (screen-fn)
                         (catch Exception e
                           (.printStackTrace e)
                           (set-screen! runner-game blank-screen)))))

(-main)

(on-gl (set-screen! runner-game main-screen))
