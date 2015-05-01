(ns nyan.core.desktop-launcher
  (:require [nyan.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. nyan-game "nyan" 800 600)
  (Keyboard/enableRepeatEvents true))
