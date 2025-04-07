(ns clj-analyzer.core
  (:require
   [clojure.edn :as edn]
   clojure.core
   [clojure.tools.reader :as tools]
   [hiccup.core :as hiccup]
   [clj-common.http-server :as server]
   [clj-common.io :as io]
   [clj-common.localfs :as fs]
   [clj-common.path :as path]))

(defn read-clj-as-edn
  "Reads all objects in file, skipping new lines. Fails on #()"
  [input-stream]
  (let [reader (new
                java.io.PushbackReader
                (new java.io.InputStreamReader input-stream))]
    (loop [buffer []
           next (edn/read {:eof nil} reader)]
      (if (some? next)
        (recur
         (conj buffer next)
         (edn/read {:eof nil} reader))
        buffer))))

(defn read-clj
  "Reads all objects in file, skipping new lines. Fails on macro (`)"
  [input-stream]
  (let [reader (new
                java.io.PushbackReader
                (new java.io.InputStreamReader input-stream))]
    (loop [buffer []
           next (clojure.core/read {:eof nil} reader)]
      (if (some? next)
        (recur
         (conj buffer next)
         (edn/read {:eof nil} reader))
        buffer))))

(defn read-clj-tools
  "Reads all objects in file, skipping new lines."
  [input-stream]
  (let [reader (new
                java.io.PushbackReader
                (new java.io.InputStreamReader input-stream))]
    (loop [buffer []
           next (tools/read {:eof nil} reader)]
      (if (some? next)
        (recur
         (conj buffer next)
         (tools/read {:eof nil} reader))
        buffer))))


(defn analyze-clj [clj-path]
  (println "analyzing" clj-path)
  (let [data (read-clj-tools (fs/input-stream clj-path))]
    (reduce
     (fn [structure element]
       (if (coll? element)
         (let [function (first element)]
           (cond
             (= function 'ns)
             (update-in structure [:ns] (constantly (str (second element))))

             (= function 'defn)
             (update-in
              structure
              [:defns]
              (fn [defns]
                (conj (or defns []) (str (second element)))))

             (= function 'def)
             (update-in
              structure
              [:defs]
              (fn [defs]
                (conj (or defs []) (str (second element)))))
           
             :else
             structure))
         structure))
     {}
     data)))

;; useful for debug
#_(run!
   println
   (read-clj-tools (fs/input-stream ["Users" "vanja" "projects" "trek-mate" "src"
                                     "clj" "trek_mate" "dataset" "timisoara.clj"])))
#_(analyze-clj ["Users" "vanja" "projects" "trek-mate" "src"
                                   "clj" "trek_mate" "dataset" "timisoara.clj"])


(defn find-clj-files
  "Recursively searches the given directory for all .clj files and 
  returns their paths.
  Provided by chatgpt."
  [path]
  (let [dir (path/path->string path)]
    (letfn [(clj-file? [file]
              (and (.isFile file)
                   (or
                    (.endsWith (.getName file) ".clj")
                    (.endsWith (.getName file) ".cljc"))))
            (list-files [f]
              (if (.isDirectory f)
                (mapcat list-files (.listFiles f))
                (when (clj-file? f)
                  [(path/string->path (.getPath f))])))]
      (list-files (new java.io.File dir)))))

(defn analyze-project [project-root-path]
  (let [files (find-clj-files (path/child project-root-path "src"))]
    #_(doseq [file files]
      (println "analyzing" file))
    (doall
     (map
      analyze-clj
      files))))


(run!
     (fn [nsinfo]
       (let [ns (:ns nsinfo)]
         (doseq [defn (:defns nsinfo)]
           (println (str ns "/" defn)))
         (doseq [def (:defs nsinfo)]
           (println (str ns "/" def)))))
     (analyze-project ["Users" "vanja" "projects" "clj-common"])
     )

(defn create-world []
  {})

(defn update-world [world nsinfo]
  (reduce
   (fn [world def]
     (let [full-name (str (:ns nsinfo) "/" def)]
       (assoc world
              full-name
              {:ns (:ns nsinfo)
               :name def
               :full-name full-name
               :type :variable})))
   (reduce
    (fn [world defn]
      (let [full-name (str (:ns nsinfo) "/" defn)]
        (assoc world
               full-name
               {:ns (:ns nsinfo)
                :name defn
                :full-name full-name
                :type :fn})))
    world
    (:defns nsinfo))
   (:defs nsinfo)))

(def world (atom (create-world)))

#_(run!
 (fn [nsinfo]
   (swap! world (fn [current-world] (update-world current-world nsinfo))))
 (analyze-project ["Users" "vanja" "projects" "clj-common"]))
#_(run!
 (fn [nsinfo]
   (swap! world (fn [current-world] (update-world current-world nsinfo))))
 (analyze-project ["Users" "vanja" "projects" "trek-mate"]))

#_(run!
 (fn [[full-name object]]
   (println
    (cond
      (= (:type object) :variable)
      "[V]"
      (= (:type object) :fn)
      "[F]"
      :else
      "[X]")
    (:full-name object)))
 (sort-by
  first
  (deref world)))

#_(println (analyze-clj ["Users" "vanja" "projects" "clj-analyzer" "src"
                       "clj_analyzer" "core.clj"]))


#_(println (analyze-clj ["Users" "vanja" "projects" "clj-analyzer" "project.clj"]))


#_(read-clj (io/string->input-stream "(update-in a [:a] (fn [x] x))"))
#_(read-clj (io/string->input-stream "(update-in a [:a] #(fn [x] x))"))

#_(clojure.edn/read-string "(update-in a [:a] #(fn [x] x))")
#_(clojure.edn/read-string "(update-in a [:a] #(conj %1 x))")




