(ns dative.events
  (:require [re-frame.core :refer [after reg-event-db reg-event-fx]]
            [clojure.string :as string]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [dative.db :as dative-db]
            [dative.utils :refer [get-current-old-instance-url]]))


(def debug (after (fn [db event]
                             (.log js/console "=======")
                             (.log js/console "state: " (str (:new-old-state db)))
                             (.log js/console "event: " (str event)))))


;(def interceptors [debug])
(def interceptors [])


(defn next-state
  [fsm current-state transition]
  (get-in fsm [current-state transition]))


;; ============================================================================
;; Miscellaneous Events
;; ============================================================================

(defn handle-app-was-initialized
  [db [event _]]
  dative-db/default-db)


(defn handle-user-clicked-tab
  [db [_ new-tab-id]]
  (assoc db :current-tab new-tab-id))

(reg-event-db :app-was-initialized interceptors
                       handle-app-was-initialized)
(reg-event-db :user-clicked-tab interceptors handle-user-clicked-tab)


;; ============================================================================
;; Login Form Events
;; ============================================================================

(def login-state-machine
  {nil                        {:app-was-initialized               :login-is-ready}
   :login-is-ready            {:password-invalidated-login        :login-requires-password
                               :username-invalidated-login        :login-requires-username
                               :login-initiated-authentication    :login-is-authenticating}
   :login-requires-username   {:user-changed-username             :login-is-ready}
   :login-requires-password   {:user-changed-password             :login-is-ready}
   :login-is-invalid          {:user-changed-username             :login-is-ready
                               :user-changed-password             :login-is-ready
                               :user-changed-current-old-instance :login-is-ready}
   :user-is-authenticated     {:login-initiated-deauthentication  :login-is-deauthenticating}
   :login-is-authenticating   {:server-not-authenticated          :login-is-invalid
                               :server-authenticated              :user-is-authenticated}
   :login-is-deauthenticating {:server-deauthenticated            :login-is-ready
                               :server-not-deauthenticated        :login-is-ready}})


(defn update-next-login-state
  [db event]
  (if-let [new-state (next-state login-state-machine (:login-state db) event)]
    (assoc db :login-state new-state)
    db))


(defn handle-next-login-state
  [db [event _]]
  (update-next-login-state db event))


(defn handle-user-changed-current-old-instance
  [db [event new-current-old-instance]]
  (-> db
      (assoc :current-old-instance new-current-old-instance)
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

(defn handle-user-pressed-enter-in-login
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
  (let [{:keys [username password current-old-instance old-instances]} db
        current-old-instance-url
        (get-current-old-instance-url old-instances current-old-instance)]
    {:db (update-next-login-state db event)
     :http-xhrio {:method          :post
                  :uri             (str current-old-instance-url "login/authenticate")
                  :params          {:username username :password password}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:server-authenticated]
                  :on-failure      [:server-not-authenticated]}}))

(defn handle-login-initiated-deauthentication
  [{:keys [db]} [event _]]
  (let [{:keys [username password current-old-instance old-instances]} db
        current-old-instance-url
        (get-current-old-instance-url old-instances current-old-instance)]
    {:db (update-next-login-state db event)
     :http-xhrio {:method          :get
                  :uri             (str current-old-instance-url "login/logout")
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
  db)


(reg-event-db :non-event interceptors
              handle-next-login-state)
(reg-event-db :username-invalidated-login interceptors
              handle-next-login-state)
(reg-event-db :password-invalidated-login interceptors
              handle-next-login-state)
(reg-event-db :server-authenticated interceptors
              handle-next-login-state)
(reg-event-db :server-deauthenticated interceptors
              handle-next-login-state)

(reg-event-db
  :user-changed-username
  interceptors
  (fn [db [event username]]
    (-> db
        (assoc :username username)
        (update-next-login-state event))))

(reg-event-db :user-changed-password interceptors handle-user-changed-password)
(reg-event-db :user-changed-current-old-instance interceptors
                       handle-user-changed-current-old-instance)
(reg-event-fx :user-clicked-logout interceptors handle-user-clicked-logout)
(reg-event-fx :user-clicked-login interceptors handle-user-clicked-login)
(reg-event-fx :user-pressed-enter-in-login interceptors
              handle-user-pressed-enter-in-login)
(reg-event-fx :login-initiated-authentication interceptors
                       handle-login-initiated-authentication)
(reg-event-fx :login-initiated-deauthentication interceptors
                       handle-login-initiated-deauthentication)
(reg-event-db :server-not-authenticated interceptors
                       handle-server-not-authenticated)
(reg-event-db :server-not-deauthenticated interceptors
                       handle-logout-failure)


;; ============================================================================
;; OLD Instance Form Events
;; ============================================================================

(def edit-old-state-machine
  {:ready          {:label-invalidated               :requires-label
                    :url-invalidated                 :requires-url
                    :create-new-old                  :ready}
   :requires-url   {:user-changed-old-instance-url   :ready}
   :requires-label {:user-changed-old-instance-label :ready}})


(defn update-next-edit-old-state
  [db event old-id]
  (let [current-state (get-in db [:old-instances old-id :state])]
    (if-let [new-state (next-state edit-old-state-machine current-state event)]
      (assoc-in db [:old-instances old-id :state] new-state)
      db)))

(defn handle-next-new-old-state
  [db [event old-id]]
  (update-next-edit-old-state db event old-id))

(defn handle-user-clicked-create-old
  [{:keys [db]} _]
  (let [{:keys [new-old-instance old-instances]} db
        new-old-instance (get old-instances new-old-instance)
        {:keys [label url]} new-old-instance]
    {:db db
     :dispatch (cond (string/blank? label)
                     [:label-invalidated (:new-old-instance db)]
                     (string/blank? url)
                     [:url-invalidated (:new-old-instance db)]
                     :else
                     [:create-new-old])}))

(defn handle-user-pressed-enter-in-old-input
  [{:keys [db]} _]
  (let [{:keys [new-old-instance old-instances]} db
        new-old-instance (get old-instances new-old-instance)
        {:keys [label url state]} new-old-instance]
    {:db db
     :dispatch (cond (string/blank? label)
                     [:label-invalidated (:new-old-instance db)]
                     (string/blank? url)
                     [:url-invalidated (:new-old-instance db)]
                     (= state :ready)
                     [:create-new-old]
                     :else
                     nil)}))

(defn handle-user-changed-old-instance-url
  [db [event old-instance-id new-old-instance-url]]
  (-> db
      (update-next-edit-old-state event old-instance-id)
      (assoc-in [:old-instances old-instance-id :url] new-old-instance-url)))

(defn handle-user-changed-old-instance-name
  [db [event old-instance-id new-old-instance-name]]
  (-> db
      (update-next-edit-old-state event old-instance-id)
      (assoc-in [:old-instances old-instance-id :label] new-old-instance-name)))

(defn handle-create-new-old
  [db [event]]
  (let [new-new-old-instance-uuid (random-uuid)
        new-new-old-instance {:id new-new-old-instance-uuid
                              :label ""
                              :url ""}]
    (-> db (update-next-edit-old-state event (:new-old-instance db))
        (assoc-in [:old-instances new-new-old-instance-uuid]
                  new-new-old-instance)
        (assoc :new-old-instance new-new-old-instance-uuid))))

(defn handle-set-old-instance-to-be-deleted
  [db [_ old-instance-id]]
  (assoc db :old-instance-to-be-deleted old-instance-id))

(defn handle-delete-old-instance
  [db [_ old-instance-id]]
  (-> db
    (assoc :old-instance-to-be-deleted nil)
    (update-in [:old-instances] dissoc old-instance-id)))


(reg-event-db :create-new-old interceptors handle-create-new-old)
(reg-event-db :user-changed-old-instance-url interceptors
                       handle-user-changed-old-instance-url)
(reg-event-db :user-changed-old-instance-label interceptors
                       handle-user-changed-old-instance-name)
(reg-event-fx :user-clicked-create-new-old-instance-button interceptors
              handle-user-clicked-create-old)
(reg-event-fx :user-pressed-enter-in-old-input interceptors
              handle-user-pressed-enter-in-old-input)

(reg-event-db :set-old-instance-to-be-deleted
                       interceptors handle-set-old-instance-to-be-deleted)
(reg-event-db :delete-old-instance interceptors
                       handle-delete-old-instance)
(reg-event-db :url-invalidated interceptors
                       handle-next-new-old-state)
(reg-event-db :label-invalidated interceptors
                       handle-next-new-old-state)
