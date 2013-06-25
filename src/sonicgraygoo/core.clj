  (ns sonicgraygoo.core
  (:use [quil.core]
        [quil.applet :only [current-applet]])
  (:import (fisica Fisica FWorld FCircle FBlob)))

(def world)
(def population)
(def munchies)

(defn new-goo
  "Creates some new goo, adds it to the world and returns it"
  [x y f]
  (let [goo (FCircle. 10)]
    (.setPosition goo x y)
    (.add world goo)
    (def population (assoc population goo {:object goo :freq f}))))

(defn new-munch
  "Creates a new crunchy munch and adds it to the world"
  [x volume]
  {:pre (pos? volume)}
  (let [blob (FCircle. 4);(FBlob.)
        munch {:object blob :volume volume}]
;    (.setAsCircle blob x 200.0 (* 6.0 volume) 4)
    (.setPosition blob x (.height (current-applet)))
    (.setDamping blob 0.0)
    (def munchies (conj munchies munch))
    (.add world blob)
    (.addForce blob 0.0 -100.0)))

(defn feed
  "Feed the goo"
  [goo]
  (println "Feeding" goo))

(defn eat-some
  "Have the munch get eaten"
  [munch bites]
  (let [x (.getX (:object munch))
        y (.getY (:object munch))
        new-volume (- (:volume munch) bites)]
    (println munch "is being munched" bites "times. Mmmmmh.")
    (.removeFromWorld (:object munch))
    (if (pos? new-volume)
      (println "add new"))))

(defn do-eating
  "Let all the goos do the munchy eating they deserve"
  []
  (doseq [munch munchies]
    ; what are we rubbing up against, and is it a goo?
    (let [eaters (keep identity (map #(get population %) (.getTouching (:object munch))))
          ; just how much of this munch to eat?
          bites (count eaters)]
                  ; give some food to the goo and make sure it actually eats it
                  (map feed eaters)
                  (when (pos? bites)
                    (eat-some munch bites)))))

(defn setup []
  (.size (current-applet) (.width (.screen (current-applet))) (.height (.screen (current-applet))))
  (smooth)
  (frame-rate 20)
  (Fisica/init (current-applet))
;  (Fisica/setScale 40)
  (def world (FWorld.)); -200.0 -100.0 200.0 100.0))
  (def munchies [])
  (.setEdges world)
  (.setEdgesFriction world 0.0)
  (.setEdgesRestitution world 1.0)
  (.setGravity world 0.0 0.0)
  (new-munch 200.0 1)
  (new-goo 100.0 50.0 100.0))

(defn draw []
  (background 200)
  (do-eating)
  (.step world)
  (.draw world)
  ; stochastic munchies!
  (when (< (rand) 0.02)
    (new-munch (rand-int (.width (current-applet))) 1)))

(defn clone-all []
  (doseq [goo (vals population)]
    (new-goo (.getX (:object goo)) (.getY (:object goo)) (:freq goo))))

(defn reset []
  (.clear world)
  (setup))

(defn keypress []
  (case (raw-key)
    (\C \c) (println (count population))
    (\D \d) (clone-all)
    (\R \r) (reset)
    \+ (frame-rate (inc (current-frame-rate)))
    \- (frame-rate (dec (current-frame-rate)))
    (println (raw-key))))

(defsketch sonicgraygoo
  :title "sonic gray goo"
  :setup setup
  :draw draw
  :key-typed keypress
  :size [800 600])
