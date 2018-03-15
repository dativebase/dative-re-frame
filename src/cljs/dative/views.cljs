(ns dative.views
  (:require [re-frame.core :as re-frame]
            [dative.subs :as subs]
            ))

(defn key-press-login-input
  [e]
  (when (= "Enter" (.-key e))
    (re-frame/dispatch [:user-pressed-enter])))

(defn username-input
  []
  [:li "Username" [:br]
   [:input
    {:auto-complete "on"
     :key "username"
     :value @(re-frame/subscribe [:login-username])
     :disabled @(re-frame/subscribe [:login-inputs-disabled?])
     :on-key-press key-press-login-input
     :on-change #(re-frame/dispatch
                   [:user-changed-username (-> % .-target .-value)])}]])

(defn password-input
  []
  [:li "Password" [:br]
   [:input
    {:auto-complete "off"
     :key "password"
     :value @(re-frame/subscribe [:login-password])
     :disabled @(re-frame/subscribe [:login-inputs-disabled?])
     :on-key-press key-press-login-input
     :on-change #(re-frame/dispatch
                   [:user-changed-password (-> % .-target .-value)])
     :type "password"}]])

(defn login-button
  []
  [:input {:type "button"
           :value "Login"
           :disabled @(re-frame/subscribe [:login-disabled?])
           :on-click (fn [e] (re-frame/dispatch [:user-clicked-login]))}])

(defn logout-button
  []
  [:input {:type "button"
           :value "Logout"
           :disabled @(re-frame/subscribe [:logout-disabled?])
           :on-click (fn [e] (re-frame/dispatch [:user-clicked-logout]))}])

(defn failure-notification
  []
  (when-let [failure @(re-frame/subscribe [:login-failure])]
    [:div {:style {:color "red"}} failure]))

(defn login-widget
  []
  [:div
   {:class 'login-widget}
   [failure-notification]
   [:form
    [:ul
     [username-input]
     [password-input]
     [login-button]
     [logout-button]]]])

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    [login-widget]
    ))
