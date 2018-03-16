(ns dative.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
  :login-state
  (fn [db _] (get db :login-state)))

(re-frame/reg-sub
  :login-invalid-reason
  (fn [db _] (get db :login-invalid-reason)))

(re-frame/reg-sub
  :login-username-status
  (fn [db _] [(re-frame/subscribe [:login-state])
              (re-frame/subscribe [:login-invalid-reason])])
  (fn [[login-state invalid-reason] _]
    (case login-state
      :login-requires-username [:error "Username required"]
      :login-is-invalid [:error invalid-reason]
      :user-is-authenticated [:success]
      :login-is-authenticating [:validating]
      [nil])))

(re-frame/reg-sub
  :login-password-status
  (fn [db _] [(re-frame/subscribe [:login-state])
              (re-frame/subscribe [:login-invalid-reason])])
  (fn [[login-state invalid-reason] _]
    (case login-state
      :login-requires-password [:error "Password required"]
      :login-is-invalid [:error invalid-reason]
      :user-is-authenticated [:success]
      :login-is-authenticating [:validating]
      [nil])))

(re-frame/reg-sub
  :login-password
  (fn [db _] (get db :password)))

(re-frame/reg-sub
  :login-username
  (fn [db _] (get db :username)))

(re-frame/reg-sub
  :login-disabled?
  (fn [db _] (re-frame/subscribe [:login-state]))
  (fn [login-state _] (not= login-state :login-is-ready)))

(re-frame/reg-sub
  :logout-disabled?
  (fn [db _] (re-frame/subscribe [:login-state]))
  (fn [login-state _] (not= login-state :user-is-authenticated)))

(re-frame/reg-sub
  :login-inputs-disabled?
  (fn [db _] (re-frame/subscribe [:login-state]))
  (fn [login-state _] (= login-state :user-is-authenticated)))
