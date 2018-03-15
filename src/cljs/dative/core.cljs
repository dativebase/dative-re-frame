(ns dative.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [dative.events :as events]
            [dative.views :as views]
            [dative.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:app-was-initialized])
  (dev-setup)
  (mount-root))
