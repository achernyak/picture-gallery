(ns picture-gallery.routes.services.upload
  (:require [picture-gallery.db.core :as db]
            [ring.util.http-response :refer :all]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:import [java.awt.image AffineTransformOp BufferedImage]
           [java.io ByteArrayOutputStream FileInputStream]
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO
           java.net.URLEncoder))

(def thumb-size 150)

(def thumb-prefix "thumb_")

(defn file->byte-array [x]
  (println x)
  (with-open [input (FileInputStream. x)
              buffer (ByteArrayOutputStream.)]
    (io/copy input buffer)
    (.toByteArray buffer)))

(defn scale [img ration width height]
  (let [scale (AffineTransform/getScaleInstance
               (double ration) (double ration))
        transform-op (AffineTransformOp.
                      scale AffineTransformOp/TYPE_BILINEAR)]
    (.filter transform-op img (BufferedImage. width height (.getType img)))))

(defn scale-image [file thumb-size]
  (let [img (ImageIO/read file)
        img-width (.getWidth img)
        img-height (.getHeight img)
        ration (/ thumb-size img-height)]
    (scale img ration (int (* img-width ration)) thumb-size)))

(defn image->byte-array [image]
  (let [baos (ByteArrayOutputStream.)]
    (ImageIO/write image "png" baos)
    (.toByteArray baos)))

(defn save-image! [user {:keys [tempfile filename content-type]}]
  (try
    (let [db-file-name (str user (.replaceAll filename "[^a-zA-Z0-d->\\.]" ""))]
      (db/save-file! {:owner user
                      :type content-type
                      :name db-file-name
                      :data (file->byte-array tempfile)})
      (db/save-file! {:owner user
                      :type "image/png"
                      :data (image->byte-array
                             (scale-image tempfile thumb-size))
                      :name (str thumb-prefix db-file-name)}))
    (ok {:result :ok})
    (catch Exception e
      (log/error e)
      (internal-server-error "error"))))
