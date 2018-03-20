(ns dative.core
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [secretary.core         :refer [defroute]])
  (:require [goog.events                   :as    events]
            [reagent.core                  :as    reagent]
            [re-frame.core                 :as    re-frame]
            [alandipert.storage-atom       :refer [local-storage]]
            [secretary.core                :as    secretary]
            [re-com.core                   :refer [h-box v-box box gap line scroller border label p title alert-box h-split] :refer-macros [handler-fn]]
            [re-com.util                   :refer [get-element-by-id item-for-id]]
            [dative.config                 :as    config]
            [dative.events                 :as    dative-events]
            [dative.home                   :as    home]
            [dative.login                  :as    login]
            [dative.old-instances          :as    old-instances]
            [dative.subs                   :as    dative-subs]
            [dative.utils                  :refer [dative-title-box]]
            [goog.history.EventType        :as    EventType])
  (:import [goog History]))


(def tabs-definition
  [{:id :home                  :level :major :label "Home"               :panel home/panel}
   {:id :admin                 :level :major :label "Admin"}
   {:id :login                 :level :minor :label "Login"              :panel login/panel}
   {:id :old-instances         :level :minor :label "OLDs"               :panel old-instances/panel}
   ])


(defn scroll-to-top
  [element]
  (set! (.-scrollTop element) 0))


(defn nav-item
  []
  (let [mouse-over? (reagent/atom false)]
    (fn [tab selected-tab-id on-select-tab]
      (let [selected?   (= @selected-tab-id (:id tab))
            is-major?  (= (:level tab) :major)
            has-panel? (some? (:panel tab))]
        [:div
         {:style         {;:width            "150px"
                          :white-space      "nowrap"
                          :line-height      "1.3em"
                          :padding-left     (if is-major? "24px" "32px")
                          :padding-top      (when is-major? "6px")
                          :font-size        (when is-major? "15px")
                          :font-weight      (when is-major? "bold")
                          :border-right     (when selected? "4px #d0d0d0 solid")
                          :cursor           (if has-panel? "pointer" "default")
                          :color            (if has-panel? (when selected? "#111") "#888")
                          :background-color (if (or
                                                  (= @selected-tab-id (:id tab))
                                                  @mouse-over?) "#eaeaea")}
          :on-mouse-over (handler-fn (when has-panel? (reset! mouse-over? true)))
          :on-mouse-out  (handler-fn (reset! mouse-over? false))
          :on-click      (handler-fn (when has-panel?
                                       (on-select-tab (:id tab))
                                       (scroll-to-top (get-element-by-id "right-panel"))))}
         [:span (:label tab)]]))))


(defn left-side-nav-bar
  [selected-tab-id on-select-tab]
    [v-box
     :class    "noselect"
     :style    {:background-color "#fcfcfc"}
     ;:size    "1"
     :children (for [tab tabs-definition]
                 [nav-item tab selected-tab-id on-select-tab])])



(defn browser-alert
  []
  [box
   :padding "10px 10px 0px 0px"
   :child   [alert-box
             :alert-type :danger
             :heading    "Only Tested On Chrome"
             :body       "Dative should work on all modern browsers, but there might be dragons!"]])


;; -- Routes, Local Storage and History ------------------------------------------------------

(def id-store        (local-storage (atom nil) ::id-store))
(def selected-tab-id (reagent/atom (if (or (nil? @id-store) (nil? (item-for-id @id-store tabs-definition)))
                                     (:id (first tabs-definition))
                                     @id-store)))  ;; id of the selected tab from local storage


(defroute dative-page "/:tab" [tab] (let [id (keyword tab)]
                                    (reset! selected-tab-id id)
                                    (reset! id-store id)))


(def history (History.))
(events/listen history EventType/NAVIGATE (fn [event] (secretary/dispatch! (.-token event))))
(.setEnabled history true)


(defn main
  []
  (let [on-select-tab #(.setToken history (dative-page {:tab (name %1)}))] ;; or can use (str "/" (name %1))
    (fn
      []
      [h-split
       ;; Outer-most box height must be 100% to fill the entrie client height.
       ;; This assumes that height of <body> is itself also set to 100%.
       ;; width does not need to be set.
       :height   "100%"
       ;:gap      "60px"
       :initial-split 9
       :margin "0px"
       :panel-1 [scroller
                 ;:size  "none"
                 :v-scroll :auto
                 :h-scroll :off
                 :child [v-box
                         :size "1"
                         :children [[dative-title-box]
                                    [left-side-nav-bar selected-tab-id on-select-tab]]]]
       :panel-2 [scroller
                 :attr  {:id "right-panel"}
                 :child [v-box
                         :size  "1"
                         :children [(when-not (-> js/goog .-labs .-userAgent .-browser .isChrome) [browser-alert])
                                    [box
                                     :padding "0px 0px 0px 50px"
                                     :child [(:panel (item-for-id @selected-tab-id tabs-definition))]]]]]])))    ;; the tab panel to show, for the selected tab


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))


(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main] (get-element-by-id "app")))


(defn ^:export init []
  (re-frame/dispatch-sync [:app-was-initialized])
  (dev-setup)
  (mount-root))
