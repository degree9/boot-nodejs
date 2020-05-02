(ns degree9.boot-nodejs.impl
  (:require [clojure.java.io :as io]
            [boot.core :as boot]
            [boot.pod  :as pod]
            [boot.util :as util]
            [degree9.boot-io.filesystem :as fs]))

;; Helper Macros ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmacro require-in [pod & body]
  `(pod/with-eval-in ~pod
    (require ~@body)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Helper Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- fs-sync! [tmp]
  (let [prev (atom nil)]
    (boot/with-pass-thru fileset
      (let [diff (boot/fileset-diff fileset @prev)]
        (reset! prev fileset)
        (apply boot/sync! tmp diff)))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; NodeJS Pod ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- pod-deps []
  (-> "degree9/boot_nodejs/pod_deps.edn" io/resource slurp read-string))

(def nodejs-pod
  (delay
    (doto
      (pod/make-pod
        (update-in (boot/get-env) [:dependencies] into (pod-deps)))
      (require-in '[degree9.boot-nodejs.pod]))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- stop! []
  (pod/with-eval-in @nodejs-pod
    (degree9.boot-nodejs.pod/stop-proc!)))

(defn- start! [& args]
  (future
    (pod/with-eval-in @nodejs-pod
      (degree9.boot-nodejs.pod/start-proc! ~@args))))

(defn serve-impl [*opts*]
  (let [script  (:script *opts* "nodejs")
        modules (:modules *opts* "./node_modules")
        tmp     (boot/tmp-dir!)
        source  (io/file modules)
        target  (io/file tmp modules)]
    (boot/cleanup (stop!))
    (comp
      (boot/with-pre-wrap fileset
        (when-not (fs/exists? target)
          (util/info "Copying %s directory... \n" modules)
          (fs/copy-dir source target))
        (-> fileset (boot/add-resource tmp) boot/commit!))
      (fs-sync! tmp)
      (boot/with-pass-thru fileset
        (stop!)
        (start! "node" script :dir (.getAbsolutePath tmp))))))
