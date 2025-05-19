(defproject vanjakom/clj-analyzer
  "0.1.0"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :deploy-repositories [
                        ["clojars" {
                                    :url "https://clojars.org/repo"
                                    :sign-releases false}]]    
  :dependencies [
                 [com.mungolab/clj-common "0.3.2"]
                 
                 [org.clojure/tools.reader "1.5.0"]])
