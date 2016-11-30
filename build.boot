(set-env!
 :dependencies  '[[org.clojure/clojure                 "1.8.0" :scope "provided"]
                  [adzerk/bootlaces                    "0.1.13" :scope "test"]
                  [boot/core                           "2.6.0"]
                  [degree9/boot-semver                 "1.2.0" :scope "test"]
                  [degree9/boot-exec                   "0.5.0-SNAPSHOT"]
                  [me.raynes/conch                     "0.8.0"]]
 :resource-paths   #{"src"})

(require
  '[adzerk.bootlaces :refer :all]
  '[boot-semver.core :refer :all])

(task-options!
  pom {:project 'degree9/boot-nodejs
       :version (get-version)
       :description "Compile cljs app to node.js."
       :url         "https://github.com/degree9/boot-nodejs"
       :scm         {:url "https://github.com/degree9/boot-nodejs"}})

(deftask develop
  "Build boot-nodejs for development."
  []
  (comp
   (watch)
   (version :no-update true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (target  :dir #{"target"})
   (build-jar)))

(deftask deploy
  "Build boot-nodejs and deploy to clojars."
  []
  (comp
   (version)
   (target  :dir #{"target"})
   (build-jar)
   (push-release)))
