(ns testgame.core.desktop-launcher
  (:require [testgame.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. testgame-game "testgame" 1920 1080)
  (Keyboard/enableRepeatEvents true))
