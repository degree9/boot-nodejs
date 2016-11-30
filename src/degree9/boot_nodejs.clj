(ns degree9.boot-nodejs
  {:boot/export-tasks true}
  (:require [boot.core :as boot]
            [boot.util :as util]
            [boot.pod :as pod]
            [clojure.java.io :as io]
            [cheshire.core :refer :all]
            [degree9.boot-exec :as exec]
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
   (assert (:init-fns *opts*) "Must provide an init-fn.")
   (let [edn    (:edn *opts*)
         init   (:init-fn *opts*)
         main   (symbol (namespace init))
         ednstr {:require (:require *opts* [])
                 :init-fns (:init-fns *opts* [])
                 :compiler-options (:compiler-options *opts*
                                     {:target (:target *opts*)
                                      :closure-defines (:closure-defines *opts*)})}
         tmp    (boot/tmp-dir!)
         fname  (str edn ".cljs.edn")
         fedn   (io/file tmp fname)]
     (boot/with-pre-wrap fileset
       (util/info (str "Generating EDN file: " fname "\n"))
       (doto fedn (spit ednstr))
       (-> fileset (boot/add-resource tmp) boot/commit!))))

(defn copy-fileset! [tmp]
  (boot/with-pre-wrap fileset
    (let [files (->> fileset boot/output-files (map (juxt boot/tmp-path boot/tmp-file)))]
      (util/info "Copying files to temp dir...\n")
      (prn (.getAbsolutePath tmp))
      (doseq [[path in-file] files]
        (let [out-file (doto (io/file tmp path) io/make-parents)]
          (io/copy in-file out-file)))
      fileset)))

(boot/deftask serve
  "Start a Node.js server."
  [e edn VAL str  "Node.js main edn file name."]
  (let [edn (:edn *opts* "nodejs")
        app (str edn ".js")
        tmp (boot/tmp-dir!)
        tmp-dir (.getAbsolutePath tmp)
        pod (atom nil)
        sync! #(apply boot/sync! tmp (boot/output-dirs %))
        start (delay (util/info (str "Starting Node.js...\n"))
                     (reset! pod (pod/make-pod (boot/get-env)))
                     (pod/with-eval-in @pod
                       (require '[me.raynes.conch.low-level :as sh]
                                '[boot.util :as util])
                       (def server (sh/proc "node" ~app :dir ~tmp-dir))
                       (sh/stream-to-out server :out)))
        stop (delay (util/info (str "Stopping Node.js...\n"))
                    (util/guard (pod/with-eval-in @pod (sh/destroy server))))]
       (boot/cleanup @stop)
       (boot/with-pre-wrap [fs]
         (util/with-let [_ fs] (sync! fs) @start))))

(boot/deftask nodejs
    "Generate a Node.js edn."
    [e edn       VAL str  "Node.js main edn name."
     i init-fn   VAL sym  "Node.js init function."
     d dev           bool "Sets nodejs-cljs dev flag."]
     (assert (:init-fn *opts*) "Must provide an init-fn.")
     (cljs-edn
       :edn (:edn *opts* "nodejs")
       :target :nodejs
       :init-fn (:init-fn *opts*)
       :closure-defines {'nodejs-cljs.core/dev? (:dev *opts* false)}))
