(ns dative.views
  (:require [re-frame.core :as re-frame]
            [dative.subs :as subs]
            [re-com.core :as re-com]
            ))

(defn key-up-login-input
  [e]
  (when (= "Enter" (.-key e))
    (re-frame/dispatch [:user-pressed-enter])))

(defn username-input
  []
  (let [status-meta @(re-frame/subscribe [:login-username-status])
        status (first status-meta)
        status-icon? (boolean status)
        status-tooltip (or (second status-meta) "")]
    [re-com/box
     :child
     [re-com/input-text
      :attr {:on-key-up key-up-login-input}
      :status status
      :status-icon? status-icon?
      :status-tooltip status-tooltip
      :change-on-blur? false
      :placeholder "username"
      :model @(re-frame/subscribe [:login-username])
      :disabled? @(re-frame/subscribe [:login-inputs-disabled?])
      :on-change #(re-frame/dispatch [:user-changed-username %])]]))

(defn password-input
  []
  (let [status-meta @(re-frame/subscribe [:login-password-status])
        status (first status-meta)
        status-icon? (boolean status)
        status-tooltip (or (second status-meta) "")]
    [re-com/box
     :child
     [re-com/input-password
      :attr {:on-key-up key-up-login-input}
      :status status
      :status-icon? status-icon?
      :status-tooltip status-tooltip
      :placeholder "password"
      :change-on-blur? false
      :model @(re-frame/subscribe [:login-password])
      :disabled? @(re-frame/subscribe [:login-inputs-disabled?])
      :on-change #(re-frame/dispatch [:user-changed-password %])]]))

(defn login-button
  []
  [re-com/box
   :child
   [re-com/button
    :label "Login"
    :disabled? @(re-frame/subscribe [:login-disabled?])
    :on-click (fn [e] (re-frame/dispatch [:user-clicked-login]))]])

(defn logout-button
  []
  [re-com/box
   :child
   [re-com/button
    :label "Logout"
    :disabled? @(re-frame/subscribe [:logout-disabled?])
    :on-click (fn [e] (re-frame/dispatch [:user-clicked-logout]))]])

(defn login-widget
  []
  [re-com/v-box
   :margin "10px"
   :gap "5px"
   :children [
    [username-input]
    [password-input]
    [re-com/h-box
     :children [[login-button]
                [logout-button]]
     :gap "5px"]]])

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    [login-widget]
    ))
