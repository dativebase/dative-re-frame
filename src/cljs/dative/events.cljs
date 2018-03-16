(ns dative.events
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [dative.db :as dative-db]))

(def login-state-machine
  {nil                        {:app-was-initialized              :login-is-ready}
   :login-is-ready            {:password-invalidated-login       :login-requires-password
                               :username-invalidated-login       :login-requires-username
                               :login-initiated-authentication   :login-is-authenticating}
   :login-requires-username   {:user-changed-username            :login-is-ready}
   :login-requires-password   {:user-changed-password            :login-is-ready}
   :login-is-invalid          {:user-changed-username            :login-is-ready
                               :user-changed-password            :login-is-ready}
   :user-is-authenticated     {:login-initiated-deauthentication :login-is-deauthenticating}
   :login-is-authenticating   {:server-not-authenticated         :login-is-invalid
                               :server-authenticated             :user-is-authenticated}
   :login-is-deauthenticating {:server-deauthenticated           :login-is-ready
                               :server-not-deauthenticated       :login-is-ready}})

(def old-url "http://127.0.0.1:61001/old/")

(defn next-state
  [fsm current-state transition]
  (get-in fsm [current-state transition]))

(defn update-next-login-state
  [db event]
  (if-let [new-state (next-state login-state-machine (:login-state db) event)]
    (assoc db :login-state new-state)
    db))

(defn handle-next-login-state
  [db [event _]]
  (update-next-login-state db event))

(defn handle-user-changed-username
  [db [event username]]
  (println username)
  (-> db
      (assoc :username username)
      (update-next-login-state event)))

(defn handle-user-changed-password
  [db [event password]]
  (-> db
      (assoc :password password)
      (update-next-login-state event)))

(defn handle-user-clicked-login
  [{:keys [db]} _]
  (let [{:keys [username password]} db]
    {:db db
     :dispatch (cond (string/blank? username)
                     [:username-invalidated-login]
                     (string/blank? password)
                     [:password-invalidated-login]
                     :else
                     [:login-initiated-authentication])}))

(defn handle-user-pressed-enter
  [{:keys [db]} _]
  (let [{:keys [username password login-state]} db]
    {:db db
     :dispatch (cond (string/blank? username)
                     [:username-invalidated-login]
                     (string/blank? password)
                     [:password-invalidated-login]
                     (= login-state :login-is-ready)
                     [:login-initiated-authentication]
                     :else
                     nil)}))

(defn handle-user-clicked-logout
  [{:keys [db]} _]
  {:db db
   :dispatch [:login-initiated-deauthentication]})

(defn handle-login-initiated-authentication
  [{:keys [db]} [event _]]
  (let [{:keys [username password]} db]
    {:db (update-next-login-state db event)
     :http-xhrio {:method          :post
                  :uri             (str old-url "login/authenticate")
                  :params          {:username username :password password}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:server-authenticated]
                  :on-failure      [:server-not-authenticated]}}))

(defn handle-login-initiated-deauthentication
  [{:keys [db]} [event _]]
  (let [{:keys [username password]} db]
    {:db (update-next-login-state db event)
     :http-xhrio {:method          :get
                  :uri             (str old-url "login/logout")
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:server-deauthenticated]
                  :on-failure      [:server-not-deauthenticated]}}))

(defn handle-server-not-authenticated
  [db [event {:keys [response]}]]
  (let [error-msg (:error response "Undetermined error.")]
    (-> db
        (assoc :login-invalid-reason error-msg)
        (update-next-login-state event))))


(defn handle-logout-failure
  [db [_ msg]]
  (println "logout message")
  (println (type msg))
  (println msg)
  db)

(def debug (re-frame/after (fn [db event]
                             (.log js/console "=======")
                             (.log js/console "state: " (str (:login-state db)))
                             (.log js/console "event: " (str event)))))

(def interceptors [debug])

(defn handle-app-was-initialized
  [db [event _]]
  (-> db
      (assoc :username ""
             :password "")
      (update-next-login-state event)))

(re-frame/reg-event-db :app-was-initialized interceptors handle-app-was-initialized)
(re-frame/reg-event-db :non-event interceptors handle-next-login-state)
(re-frame/reg-event-db :username-invalidated-login interceptors handle-next-login-state)
(re-frame/reg-event-db :password-invalidated-login interceptors handle-next-login-state)
(re-frame/reg-event-db :user-changed-username interceptors handle-user-changed-username)
(re-frame/reg-event-db :user-changed-password interceptors handle-user-changed-password)
(re-frame/reg-event-fx :user-clicked-logout interceptors handle-user-clicked-logout)
(re-frame/reg-event-fx :user-clicked-login interceptors handle-user-clicked-login)
(re-frame/reg-event-fx :user-pressed-enter interceptors handle-user-pressed-enter)
(re-frame/reg-event-fx :login-initiated-authentication interceptors handle-login-initiated-authentication)
(re-frame/reg-event-fx :login-initiated-deauthentication interceptors handle-login-initiated-deauthentication)
(re-frame/reg-event-db :server-not-authenticated interceptors handle-server-not-authenticated)
(re-frame/reg-event-db :server-authenticated interceptors handle-next-login-state)
(re-frame/reg-event-db :server-deauthenticated interceptors handle-next-login-state)
(re-frame/reg-event-db :server-not-deauthenticated interceptors handle-logout-failure)
