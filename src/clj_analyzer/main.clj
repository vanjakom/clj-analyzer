(ns clj-analyzer.main
  (:require
   [clojure.edn :as edn]
   clojure.core
   [clojure.tools.reader :as tools]
   [hiccup.core :as hiccup]
   [clj-common.http-server :as server]
   [clj-common.io :as io]
   [clj-common.localfs :as fs]
   [clj-common.path :as path]
   [clj-analyzer.core :as core]))

(def handler
  (compojure.core/routes
    (compojure.core/GET
     "/"
     []
     {
      :status 200
      :headers {
                "Content-Type" "text/html; charset=utf-8"}
      :body (hiccup/html
             [:head
              [:meta {:charset "UTF-8"}]]
             [:body {:style "font-family:arial; max-width:100%; overflow-x:hidden;"}
              (map
               (fn [object]
                 (list
                  (cond
                    (= (:type object) :variable)
                    "[V]"
                    (= (:type object) :fn)
                    "[F]"
                    :else
                    "[X]")
                  (:full-name object)
                  [:br]))
               (vals (sort-by first (deref core/world))))
              "hello world"
              [:br]])})))

(defn -main [& args]
  (server/create-server 7055 handler))

#_(-main)

(do
  (run!
   (fn [nsinfo]
     (swap! core/world (fn [current-world] (core/update-world current-world nsinfo))))
   (core/analyze-project ["Users" "vanja" "projects" "clj-analyzer"]))
  (run!
   (fn [nsinfo]
     (swap! core/world (fn [current-world] (core/update-world current-world nsinfo))))
   (core/analyze-project ["Users" "vanja" "projects" "clj-common"]))
  (run!
   (fn [nsinfo]
     (swap! core/world (fn [current-world] (core/update-world current-world nsinfo))))
   (core/analyze-project ["Users" "vanja" "projects" "clj-geo"]))
 (run!
   (fn [nsinfo]
     (swap! core/world (fn [current-world] (core/update-world current-world nsinfo))))
   (core/analyze-project ["Users" "vanja" "projects" "clj-scheduler"]))
 (run!
   (fn [nsinfo]
     (swap! core/world (fn [current-world] (core/update-world current-world nsinfo))))
   (core/analyze-project ["Users" "vanja" "projects" "moja-geografija"]))
 (run!
   (fn [nsinfo]
     (swap! core/world (fn [current-world] (core/update-world current-world nsinfo))))
   (core/analyze-project ["Users" "vanja" "projects" "notes-viewer"]))
 (run!
   (fn [nsinfo]
     (swap! core/world (fn [current-world] (core/update-world current-world nsinfo))))
   (core/analyze-project ["Users" "vanja" "projects" "trek-mate"]))
 (run!
   (fn [nsinfo]
     (swap! core/world (fn [current-world] (core/update-world current-world nsinfo))))
   (core/analyze-project ["Users" "vanja" "projects" "zanimljiva-geografija"]))

 (println "done"))
