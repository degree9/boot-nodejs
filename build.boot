(set-env!
 :dependencies  '[[boot/core           "2.8.2"]
                  [degree9/boot-io     "1.4.0"]
                  [degree9/boot-semver "1.8.0" :scope "test"]]
 :resource-paths   #{"src"})

(require '[degree9.boot-semver :refer :all])

(task-options!
  target {:dir #{"target"}}
  pom {:project 'degree9/boot-nodejs
       :description "Compile cljs app to node.js."
       :url         "https://github.com/degree9/boot-nodejs"
       :scm         {:url "https://github.com/degree9/boot-nodejs"}})

(deftask develop
  "Build boot-nodejs for development."
  []
  (comp
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (watch)
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
