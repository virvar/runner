(ns runner.logic
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all]
            [runner.entities :refer :all]))

(def velocity 300)
(def gravity 500)
(def ground-y tile-size)
(def start-time (atom 0))
(def best-score (atom 0))
(def block-creation-rate 0.25)
(def block-creation-interval 300)
(def distance (atom 0))

(defn- correct-player-position
  [player]
  (let [x (:x player)]
    (assoc player :x
      (cond
       (< x 0) 0
       (< (game :width) (+ x (:width player))) (- (game :width) (:width player))
       :default x))))

(defn- move
  [entity direction delta]
  (let [distance (* velocity delta)
        new-entity (cond-> entity
                           (= direction :right) (assoc :x (+ (:x entity) distance))
                           (= direction :left) (assoc :x (- (:x entity) distance)))]
    (correct-player-position new-entity)))

(defn- move-world!
  [entities direction delta-time]
  (let [delta-length (* velocity delta-time)
        delta (cond
               (= direction :right) delta-length
               (= direction :left) (- delta-length))]
    (swap! distance #(+ % delta))
    (map (fn [entity]
           (if (or (:block? entity)
                   (:background? entity))
             (update entity :x #(- % delta))
             entity))
         entities)))

(defn- handle-input!
  [screen entities]
  (let [delta-time (:delta-time screen)]
    (cond-> entities
            (key-pressed? :dpad-left)
            (move-world! :left delta-time)
            (key-pressed? :dpad-right)
            (move-world! :right delta-time))))

(defn- move-block
  [entity delta-time]
  (let [delta-x (* block-velocity delta-time)
        new-entity (update entity :x #(- % delta-x))]
    (if (< (:x new-entity) 0)
      nil
      new-entity)))

(defn- move-block-y
  [entity delta-time]
  (let [delta-y (* (:velocity entity) delta-time)
        entity (update entity :y #(+ % delta-y))]
    (cond
     (and (< (:y entity) ground-y)
          (< (:velocity entity) ground-y))
     (assoc entity :velocity block-velocity)
     (and (> (:y entity) (* block-top tile-size))
          (< ground-y (:velocity entity)))
     (assoc entity :velocity (- block-velocity))
     :default entity)))

(defn- move-background
  [entity delta-time]
  (update entity :x
          (fn [x]
            (let [delta-x (* block-velocity delta-time)
                  new-x (- x delta-x)]
              (if (< new-x (- (game :width)))
                (+ new-x (* (game :width) 2))
                new-x)))))

(defn- handle-move
  [screen entities entity]
  (let [delta-time (:delta-time screen)]
    (cond
     ;;      (:player? entity)
     ;;      (cond-> entity
     ;;              (key-pressed? :dpad-left)
     ;;              (move :left delta-time)
     ;;              (key-pressed? :dpad-right)
     ;;              (move :right delta-time))
     (:block? entity)
     (-> entity
         (move-block-y delta-time)
         (move-block delta-time))
     (:background? entity)
     (move-background entity delta-time)
     :default entity)))

(defn- create-block-rand!
  [screen]
  (let [delta-x (* block-velocity (:delta-time screen))]
    (swap! distance #(+ % delta-x)))
  (if (< block-creation-interval @distance)
    (if (< (rand) block-creation-rate)
      (do
        (reset! distance 0)
        (create-block 15 (+ (rand-int 5) 1)))
      nil)
    nil))

(defn- collide?
  [entity1 entity2]
  (intersector! :intersect-rectangles
                (rectangle (:x entity1) (:y entity1) (:width entity1) (:height entity1))
                (rectangle (:x entity2) (:y entity2) (:width entity2) (:height entity2))
                (rectangle 0 0 0 0)))

(defn- has-collision?
  [entities player]
  (some (fn [entity]
          (and (:block? entity)
               (collide? entity player)))
        entities))

(defn- game-over?
  [screen entities]
  (let [player (find-first :player? entities)]
    (has-collision? entities player)))

(defn- get-best-score!
  [score]
  (if (< @best-score score)
    (reset! best-score score)
    @best-score))

(defn- update-info!
  [screen entities]
  (let [score-panel (find-first :score? entities)
        best-score-panel (find-first :best-score? entities)
        score (int (* (- (:total-time screen) @start-time) 100))
        best-score (get-best-score! score)]
    (label! score-panel :set-text (str "Score: " score))
    (label! best-score-panel :set-text (str "Best: " best-score))))

(defn restart-game
  [screen]
  (reset! start-time (or (:total-time screen) 0))
  (vector (create-background 0)
          (create-background (game :width))
          (create-score-panel)
          (create-best-score-panel)
          (create-player 3 1)))

(defn update-game!
  [screen entities]
  (if (game-over? screen entities)
    (restart-game screen)
    (do
      (update-info! screen entities)
      (let [new-entities (filterv some? (->> entities
                                             (map #(as-> % entity
                                                         (handle-move screen entities entity)))
                                             (handle-input! screen)))
            new-block (create-block-rand! screen)]
        (if new-block
          (conj new-entities new-block)
          new-entities)))))

(defn init-game
  [screen]
  (restart-game screen))
