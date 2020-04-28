(ns degree9.boot-nodejs.pod
  (:require [clojure.java.io :as io]
            [boot.util :as util]
            [clojure.java.io :as io]
            [me.raynes.conch.low-level :as sh]))

;; This namespace is for use within nodejs-pod context only ;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *proc* (atom nil))

(defn- server-msg [action]
  (util/info "Node.js %s...\n" action))

(defn- server-err [action & [error]]
  (util/fail "Node.js %s...\n" action)
  (when error
    (util/fail "%s \n" error)))

(defn exit-code! []
  (future (sh/exit-code @*proc*)))

(defn start-proc! [& args]
  (when-not @*proc*
    (server-msg "Starting")
    (reset! *proc* (apply sh/proc args))
    (when-not (= 0 @(exit-code!))
      (server-err "Error" (sh/stream-to-string @*proc* :err)))))

(defn stop-proc! []
  (when @*proc*
    (server-msg "Stopping")
    (sh/destroy @*proc*)
    (reset! *proc* nil)))
