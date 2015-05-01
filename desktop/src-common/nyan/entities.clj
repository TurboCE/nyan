(ns nyan.entities
  (:require 
    [play-clj.core :refer :all]
    [play-clj.g2d :refer :all]
    [play-clj.g2d-physics :refer :all]
    [play-clj.math :refer :all]
    [play-clj.ui :refer :all]))

(defn create-object-body!
  [screen]
    (->> (body-def :dynamic)
      (add-body! screen)))

(defn obj-create!
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
           (fixture-def :density 0.1 :friction 0.1 :shape cshape))
    inst))

(defn obj-set-vector [inst vx vy]
  (body! inst :set-linear-velocity vx vy))

(defn touched?
  [key]
  (and (game :touched?)
       (case key
         :down (< (game :y) (/ (game :height) 3))
         :up (> (game :y) (* (game :height) (/ 2 3)))
         :left (< (game :x) (/ (game :width) 3))
         :right (> (game :x) (* (game :width) (/ 2 3)))
         :center (and (< (/ (game :width) 3) (game :x) (* (game :width) (/ 2 3)))
                      (< (/ (game :height) 3) (game :y) (* (game :height) (/ 2 3))))
         false)))

(defn find-player-entity [entities]
  (find-first #(case (:type %)
                 :user true
                 false) entities))

(defn update-player [entity]
  (let [
        vx (+ (if (or (touched? :left) (key-pressed? :dpad-left)) 
                (* -1 (:speed entity)) 0) 
              (if (or (touched? :right) 
                      (key-pressed? :dpad-right)) 
                (:speed entity) 0))
        vy (+ (if (or (touched? :up) (key-pressed? :dpad-up)) 
                (:speed entity) 0) 
              (if (or (touched? :down) (key-pressed? :dpad-down)) 
                (* -1 (:speed entity)) 0))]   
    (obj-set-vector entity vx vy)))
