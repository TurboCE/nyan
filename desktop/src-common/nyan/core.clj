(ns nyan.core
  (:require [play-clj.core :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [nyan.entities :as e]))

; create an asset manager
(defonce manager (asset-manager))
; set it to be used by play-clj
(set-asset-manager! manager)


(defscreen background-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage) :camera (orthographic))
    [
      (assoc (texture "background.jpg" :flip true false)
             :type :cloud
             :x 0 :y 0 :width 1920 :height 1080
             :angle 0 :origin-x 0 :origin-y 0)
     ]
    )
  
  :on-resize
  (fn [screen entities]
    (height! screen 1080))
  
  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities)
  ))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (let [screen (update! screen
                          :renderer (stage)
                          :camera (orthographic)
                          :world (box-2d 0 0))]
      [
       (e/obj-create! screen "nyan.gif" :user 50 250 125 125 50 500 100 0)
       (e/obj-create! screen "nyan.gif" :enemy 550 250 125 125 50 10 -20 0)]))

  :on-render
  (fn [screen entities]
    (e/update-player (e/find-player-entity entities))    
    (->> entities
      (step! screen)
      (render! screen)))
  :on-resize
  (fn [screen entities]
    (height! screen 1080))  
  :on-begin-contact
  (fn [screen entities]
    (let [e1 (first-entity screen entities)
          e2 (second-entity screen entities)]
      ; do something with the entities that made contact
      (case [(:type e1) (:type e2)]
        [:user :enemy] (remove #(= e2 %) entities)
        nil))))

(defscreen text-screen
  :on-show
  (fn [screen entities]
    (update! screen :camera (orthographic) :renderer (stage))
    [
     (assoc (label "0" (color :white))
            :id :fps
            :x 5)
     (assoc (label "This is the first test program" (color :white)) 
            :id :output-text            
            :x 10 :y 200)
     (assoc (label "0" (color :white))
            :id :object-cnt
            :x 120)
     ]
    )
  
  :on-render
  (fn [screen entities]
    (->> (for [entity entities]
           (case (:id entity)
             :fps (doto entity (label! :set-text (str (game :fps))))
             :object-cnt (doto entity (label! :set-text (str (count entities))))
             entity))
         (render! screen)))
  
  :on-resize
  (fn [screen entities]
    (height! screen 1080))
 )

(defgame nyan-game
  :on-create
  (fn [this]
    (set-screen! this background-screen main-screen text-screen)))
