(ns testgame.entities
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]))

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

(defn get-object-entities [type entities]
  (remove #(case (:type %)
             type false
             true) entities))

(defn fire-missile [entities]
  (let [player (find-player-entity entities)]
    (conj entities (assoc (texture "nyan_face.png")
                          :type :missile
                          :speed 30
                          :x (+ (:x player) (/ (:width player) 2)) :y (+ (:y player) (/ (:height player) 2)) 
                          :width 40 :height 40
                          :coll-center [20,20]
                          :coll-size 20
                          :angle 0 :origin-x 0 :origin-y 0))))

(defn check-outofrange [entity]
  (if (or (< (:x entity) -400) (> (:x entity) 1920)
          (< (:y entity) -400) (> (:y entity) 1080))
    (let [] (println "true" (:x entity) (:y entity)) true)
    false
    )
  )

(defn distance-vect [first-vect second-vect]
  (Math/sqrt 
    (reduce + (map #(Math/pow % 2) 
                   (map #(Math/abs %) (map - first-vect second-vect))))))
(defn check-collision [first-entity second-entity]
  (let [first-vec (map + [(:x first-entity) (:y first-entity)] 
                       (:coll-center first-entity))
        second-vec (map + [(:x second-entity) (:y second-entity)] 
                        (:coll-center second-entity))]
    (if (< (distance-vect first-vec second-vec) 
           (+ (:coll-size first-entity) 
              (:coll-size second-entity))) 
      true false)))

(defn collision? [entity type entities]
  (->> (filter #(= (:type %) type) entities)
    (some #(check-collision entity %))))

(defn enemy-move [entity]
  (assoc entity :x (- (:x entity) (:speed entity))))

(defn missile-move [entity]
  (assoc entity :x (+ (:x entity) (:speed entity))))

(defn update-object [entity entities]
  (case (:type entity)
    :cloud (assoc entity :x (- (:x entity) (:speed entity)))
    :enemy (let [] (if (collision? entity :missile entities) nil (enemy-move entity)))
    :object (assoc entity :x (+ (:x entity) 12))
    :missile (let [] (if (collision? entity :enemy entities) nil (missile-move entity)))
    entity)
  )
(defn boundary-set 
  [targ lb ub]
  (if (< targ lb) lb (if (> targ ub) ub targ)))

(defn update-player [entity entities]
  (let [
        vx (+ (if (or (touched? :left) (key-pressed? :dpad-left)) 
                (* -1 (:speed entity)) 0) 
              (if (or (touched? :right) 
                      (key-pressed? :dpad-right)) 
                (:speed entity) 0))
        vy (+ (if (or (touched? :up) (key-pressed? :dpad-up)) 
                (:speed entity) 0) 
              (if (or (touched? :down) (key-pressed? :dpad-down)) 
                (* -1 (:speed entity)) 0))
        nx (:x entity)
        ny (:y entity)
        newx (+ nx vx)
        newy (+ ny vy)]   
    (assoc entity
         :x (boundary-set newx 0 1920)
         :y (boundary-set newy 0 1080)
         )
    ))

(defn update-objects [entities]
  (remove #(or 
             (= % nil)
             (check-outofrange %))
          (map (fn [entity]
                  (case (:type entity)
                    :user (update-player entity entities)
                    (update-object entity entities))) entities)))
