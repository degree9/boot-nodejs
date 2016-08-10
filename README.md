# boot-nodejs
[![Clojars Project](https://img.shields.io/clojars/v/degree9/boot-nodejs.svg)](https://clojars.org/degree9/boot-nodejs)

Node.js wrapper task for [boot-clj][1].

* Provides `nodejs` task for executing node.

> The following outlines basic usage of the task, extensive testing has not been done.
> Please submit issues and pull requests!

## Usage

Add `boot-nodejs` to your `build.boot` dependencies and `require` the namespace:

```clj
(set-env! :dependencies '[[degree9/boot-nodejs "X.Y.Z"]])
(require '[degree9.boot-nodejs :refer :all])
```

Start a node script.

```bash
boot node -s app.js
```

Use in a wrapper task:

```clojure
(boot/deftask mytask
  ""
  [...]
  (let [...]
    (comp
      (nodejs :script "app.js"))))
```

##Task Options

{options-description}

```clojure
{options}
```

{options-notes}

[1]: https://github.com/boot-clj/boot
