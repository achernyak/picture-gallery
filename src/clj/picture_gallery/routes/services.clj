(ns picture-gallery.routes.services
  (:require [compojure.api.sweet :refer :all]
            [picture-gallery.routes.services
             [auth :as auth]
             [upload :as upload]]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [compojure.api.upload :refer [wrap-multipart-params TempFileUpload]]))

(s/defschema UserRegistration
  {:id String
   :pass String
   :pass-confirm String})

(s/defschema Result
  {:result s/Keyword
   (s/optional-key :message) String})

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Picture Gallery API"
                           :description "Public Services"}}}}
  (POST "/register" req
        :return Result
        :body [user UserRegistration]
        :summary "register a new user"
        (auth/register! req user))
  (POST "/login" req
        :header-params [authorization :- String]
        :summary "log in the user and create a session"
        :return Result
        (auth/login! req authorization))
  (POST "/logout" []
        :summary "remove user session"
        :return Result
        (auth/logout!)))

(defapi restricted-service-routes
  {:swagger {:ui "/swagger-ui-private"
             :spec "/swagger-private.json"
             :data {:info {:version "1.0.0"
                           :title "Picture Gallery API"
                           :description "Private Services"}}}}
  (POST "/upload" req
        :multipart-params [file :- TempFileUpload]
        :middleware [wrap-multipart-params]
        :summary "handles image upload"
        :return Result
        (upload/save-image! (:identity req) file)))
