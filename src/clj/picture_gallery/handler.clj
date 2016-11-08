(ns picture-gallery.handler
  (:require [compojure
             [core :refer [routes wrap-routes]]
             [route :as route]]
            [mount.core :as mount]
            [picture-gallery
             [env :refer [defaults]]
             [layout :refer [error-page]]
             [middleware :as middleware]]
            [picture-gallery.routes
             [home :refer [home-routes]]
             [services :refer [restricted-service-routes service-routes]]]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
   #'service-routes
   (wrap-routes #'restricted-service-routes middleware/wrap-auth)
   (wrap-routes #'home-routes middleware/wrap-csrf)
   (route/not-found
    (:body
     (error-page {:status 404
                  :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
