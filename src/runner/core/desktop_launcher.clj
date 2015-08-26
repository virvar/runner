(ns runner.core.desktop-launcher
  (:require [runner.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. runner-game "runner" 800 600)
  (Keyboard/enableRepeatEvents true))
