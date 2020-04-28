(ns degree9.boot-nodejs
  {:boot/export-tasks true}
  (:require [boot.core :as boot]
            [degree9.boot-nodejs.impl :as impl]))

(boot/deftask serve
  "Start a Node.js server."
  [s script VAL str  "Node.js main script file. (nodejs)"
   m modules VAL str "Location of node_modules folder in project."]
  (impl/serve-impl *opts*))
