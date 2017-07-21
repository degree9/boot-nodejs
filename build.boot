(set-env!
 :dependencies  '[[org.clojure/clojure                 "1.8.0"]
                  [degree9/boot-semver                 "1.6.0" :scope "test"]
                  [me.raynes/conch                     "0.8.0"]]
 :resource-paths   #{"src"})

(require '[degree9.boot-semver :refer :all]
         '[degree9.boot-semver.impl :refer [get-version]])

(def +version+ (get-version))

(task-options!
 target {:dir #{"target"}}
 pom {:project 'degree9/boot-nodejs
      :version +version+
      :description "Compile cljs app to node.js."
      :url         "https://github.com/degree9/boot-nodejs"
      :scm         {:url "https://github.com/degree9/boot-nodejs"}})

(deftask develop
  "Build boot-nodejs for development."
  []
  (comp
   (watch)
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (target)
   (build-jar)))

(deftask deploy
  "Build boot-nodejs and deploy to clojars."
  []
  (comp
   (version)
   (target)
   (build-jar)
   (push-release)))
