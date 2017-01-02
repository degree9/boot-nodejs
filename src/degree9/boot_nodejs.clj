(ns degree9.boot-nodejs
  {:boot/export-tasks true}
  (:require [boot.core :as boot]
            [boot.util :as util]
            [boot.pod :as pod]
            [clojure.java.io :as io]
            [cheshire.core :refer :all]
            [me.raynes.conch.low-level :as sh]))

(boot/deftask cljs-edn
  "Generate a .cljs.edn file."
  [e edn      VAL str   "EDN file name."
   r require  VAL [sym] "Vector of namespaces to require."
   f init-fns VAL [sym] "Vector of fuctions to wrap in do block."
   t target   VAL kw    "Target platform."
   d closure-defines  VAL {} "A map of closure defines options."
   o compiler-options VAL {} "A map of compiler options."]
   (assert (:edn *opts*) "Must provide an edn file name.")
   (assert (:init-fns *opts*) "Must provide init-fns.")
   (let [edn    (:edn *opts*)
         init   (:init-fn *opts*)
         ednstr {:require (:require *opts* [])
                 :init-fns (:init-fns *opts* [])
                 :compiler-options (:compiler-options *opts*
                                     {:target (:target *opts*)
                                      :closure-defines (:closure-defines *opts*)})}
         tmp    (boot/tmp-dir!)
         fname  (str edn ".cljs.edn")
         fedn   (io/file tmp fname)]
     (boot/with-pre-wrap fileset
       (util/info "Generating EDN file...\n")
       (util/info "• %s\n" fname)
       (doto fedn (spit ednstr))
       (-> fileset (boot/add-resource tmp) boot/commit!))))

(defn- stop-nodejs [server]
  (when @server
    (util/info (str "Stopping Node.js...\n"))
    (sh/destroy @server)
    (reset! server nil)))

(defn- start-nodejs [server & [{:keys [script dir]}]]
  (when-not @server
    (util/info (str "Starting Node.js...\n"))
    (reset! server (sh/proc "node" script :dir dir))
    (sh/stream-to-out @server :out)
    (when-not (= 0 @(future (sh/exit-code @server)))
      (util/fail (str "Node.js Error...\n"))
      (util/fail (str (sh/stream-to-string @server :err) "\n")))))

(boot/deftask serve
  "Start a Node.js server."
  [s script VAL str  "Node.js main script file."]
  (let [script (:script *opts* "nodejs")
        server (atom nil)
        tmp (boot/tmp-dir!)
        tmp-dir (.getAbsolutePath tmp)
        sync! #(apply boot/sync! tmp (boot/output-dirs %))
        stop #(stop-nodejs server)
        start #(start-nodejs server :script script :dir tmp-dir)]
       (boot/cleanup (stop))
       (boot/with-pass-thru fileset
         (sync! fileset)
         (stop)
         (future (start)))))

(boot/deftask nodejs
    "Generate a Node.js edn."
    [e edn       VAL str  "Node.js main edn name."
     i init-fn   VAL sym  "Node.js init function."
     d dev           bool "Sets nodejs-cljs dev flag."]
     (assert (:init-fn *opts*) "Must provide an init-fn.")
     (cljs-edn
       :edn (:edn *opts* "nodejs")
       :target :nodejs
       :init-fns [(:init-fn *opts*)]
       :closure-defines {'nodejs-cljs.core/dev? (:dev *opts* false)}))
