(ns dative.db)


;; TODO: these should be received from the Dative server and/or stored in a
;; JSON/EDN file.
(def demo-old-1-uuid (random-uuid))
(def demo-old-2-uuid (random-uuid))
(def new-old-instance-uuid (random-uuid))
(def old-instances
  {demo-old-1-uuid {:id demo-old-1-uuid
                    :label "Demo OLD 1"
                    :url "http://127.0.0.1:61001/old/"
                    :state :ready}
   demo-old-2-uuid {:id demo-old-2-uuid
                    :label "Demo OLD 2"
                    :url "http://127.0.0.1:61001/xyzold/"
                    :state :ready}
   new-old-instance-uuid {:id new-old-instance-uuid
                          :label ""
                          :url ""
                          :state :ready}})

(def default-db
  {:name "Dative"
   :username ""
   :password ""

   :login-state :login-is-ready
   :login-invalid-reason nil

   :old-instances old-instances
   :current-old-instance demo-old-1-uuid
   :new-old-instance new-old-instance-uuid
   :old-instance-to-be-deleted nil

   :current-tab :forms-tab
   })
