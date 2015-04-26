(ns testgame.core
  (:require 
    [testgame.entities :as e]
    [play-clj.core :refer :all]
    [play-clj.g2d :refer :all]
    [play-clj.g2d-physics :refer :all]
    [play-clj.math :refer :all]
    [play-clj.ui :refer :all]))

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
    (width! screen 1920)
    (height! screen 1080))
  
  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities)
  ))

;(defn create-sphere-body!
;  [screen radius]
;  (let [shape (sphere-shape radius)
;        local-inertia (vector-3 0 0 0)]
;    (sphere-shape! shape :calculate-local-inertia mass local-inertia)
;    (->> (rigid-body-info mass nil shape local-inertia)
;         rigid-body
;         (add-body! screen))))

;(defn create-sphere!
;  [screen w h]
;  (-> (model-builder)
;      (model-builder! :create-sphere w h 4 24 24 (get-material) (get-attrs))
;      model
;      (assoc :body (create-sphere-body! screen (/ w 2)))))

(defn create-object-body!
  [screen]
    (->> (body-def :dynamic)
      (add-body! screen)))

(defn create-object!
  [screen tex-name type x y width height radius speed vx vy]
  (let [
        inst (-> (texture tex-name)
               (assoc :body (create-object-body! screen) :type type :speed speed))
        cshape (circle-shape)
        ]
    (body! inst :set-transform x y 0)
    (body! inst :set-bullet true)
    (body! inst :set-linear-velocity vx vy)
    (circle-shape! cshape :set-position (vector-2 (/ width 2) (/ height 2)))
    (circle-shape! cshape :set-radius radius) 
    (body! inst :create-fixture 
           (fixture-def :density 12 :friction 0.9 :shape cshape))
    
    inst)
  )



(defscreen main-screen
  :on-show
  (fn [screen entities]
    (let [screen (update! screen
                          :renderer (stage)
                          :camera (orthographic)
                          :world (box-2d 0 -10)
                          )]
      ;(box-2d! screen :set-contact-listener {})
;      (add-timer! screen :timer 2 2)
;      (add-timer! screen :missile 0.4 0.4)
;      (add-timer! screen :enemy 1 1)
;      (add-timer! screen :gc 2 2)
      [
(create-object! screen "nyan.gif" :user 50 250 125 125 50 10 100 0)
(create-object! screen "nyan.gif" :enemy 550 250 125 125 50 10 -20 0)
       ]
      )
    )
  :on-timer
  (fn [screen entities]
    (case (:id screen)
      :gc (let [] (println "gc do free" (. (Runtime/getRuntime) freeMemory) "byte" (count entities) (System/gc)))
      :missile (e/fire-missile entities)
      :enemy (conj entities (assoc (texture "donut_cat.gif" :flip true false)
                                  :type :enemy
                                  :speed (+ (rand-int 10) 3)
                                  :x 1810 :y (rand-int 1080) :width 323 :height 249
                                  :coll-center [170,145]
                                  :coll-size 100
                                  :angle 0 :origin-x 0 :origin-y 0))
       nil))

  :on-resize
  (fn [screen entities]
    (width! screen 1920)
    (height! screen 1080))
  
  :on-render
  (fn [screen entities]
    (e/update-player (e/find-player-entity entities) entities)    
    (->> entities
      (step! screen)
      (render! screen))    
    )
  :on-begin-contact
  (fn [screen entities]
    (let [e1 (first-entity screen entities)
          e2 (second-entity screen entities)]
      ; do something with the entities that made contact
        (remove #(or (= e1 %) (= e2 %)) entities)))
  )

(defscreen cloud-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage) :camera (orthographic))
    (add-timer! screen :cloud 1 1)
    []
    )
  :on-timer
  (fn [screen entities]
    (case (:id screen)
      :cloud (conj entities (assoc (texture "cloud.png" )
                                   :type :cloud
                                   :speed (+ (rand-int 10) 3)
                                   :x 1910 :y (rand-int 1080) :width 298 :height 213
                                   :angle 0 :origin-x 0 :origin-y 0))
                   
      nil))

  :on-resize
  (fn [screen entities]
    (width! screen 1920)
    (height! screen 1080))
  
  :on-render
  (fn [screen entities]
    (render! screen (remove #(or 
                               (= % nil)
                               (e/check-outofrange %))
                            (e/update-objects entities))
             )
    )
  )


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
    (width! screen 1920)
    (height! screen 1080))
 )

(defgame testgame-game
  :on-create
  (fn [this]
    (set-screen! this  background-screen cloud-screen main-screen text-screen)))

