(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//localhost:5432/depot_development"
              ;; Not needed for a non-secure local database...
              :user "ericky"
              :password "ericky"
              })

(require '[clojure.java.jdbc :as jdbc]
         '[java-jdbc.ddl :as ddl])

(jdbc/db-do-commands db-spec false
  (ddl/create-table
     :tags
     [:id :serial "PRIMARY KEY"]
     [:name :varchar "NOT NULL"]))

(require '[java-jdbc.sql :as sql])

(jdbc/insert! db-spec :tags
                      {:name "Clojure"}
                      {:name "Java"})
;; -> ({:name "Clojure", :id 1} {:name "Java", :id 2})

(jdbc/query db-spec (sql/select * :tags (sql/where {:name "Clojure"})))
;; -> ({:name "Clojure", :id 1})
(jdbc/query db-spec (sql/select * :tags (sql/where {:id 2})))

(jdbc/query db-spec (sql/select * :tags))
;; -> ({:name "Clojure", :id 1}
;      {:name "Java", :id 2})

(filter #(not (.endsWith (:name %) "ure"))
        (jdbc/query db-spec (sql/select * :tags)))
;; -> ({:name "Java", :id 2})

(jdbc/query db-spec (sql/select * :klasses))

(filter #(.startsWith (:name %) "COLOR")
        (jdbc/query db-spec (sql/select * :klasses)))

(require '[clojure.java.jdbc :as jdbc]
         '[java-jdbc.ddl :as ddl])

(jdbc/db-do-commands db-spec
  (ddl/create-table :fruit
    [:name "varchar(16)" "PRIMARY KEY"]
    [:appearance "varchar(32)"]
    [:cost :int "NOT NULL"]
    [:unit "varchar(16)"]
    [:grade :real]))
;; -> (0)

(jdbc/insert! db-spec :fruit
  nil ; column names omitted
  ["Red Delicious" "dark red" 20 "bushel" 8.2]
  ["Plantain" "mild spotting" 48 "stalk" 7.4]
  ["Kiwifruit" "fresh"  35 "crate" 9.1]
  ["Plum" "ripe" 12 "carton" 8.4])
;; -> (1 1 1 1)

(jdbc/db-do-commands db-spec
  (ddl/create-table :delete_me
    [:name "varchar(16)" "PRIMARY KEY"]))

(jdbc/db-do-commands db-spec (ddl/drop-table :delete_me))
;; -> (0)

(jdbc/insert! db-spec :fruit
  {:name "Banana" :appearance "spotting" :cost 35}
  {:name "Tomato" :appearance "rotten" :cost 10 :grade 1.4}
  {:name "Peach" :appearance "fresh" :cost 37 :unit "pallet"})
;; -> ({:grade nil, :unit nil, :cost 35, :appearance "spotting", :name "Banana"}
;;     {:grade 1.4, :unit nil, :cost 10, :appearance "rotten", :name "Tomato"}
;;     {:grade nil, :unit "pallet", :cost 37, :appearance "fresh",
;;      :name "Peach"})

(jdbc/insert! db-spec :fruit
  [:name :cost]
  ["Mango" 84]
  ["Kumquat" 77])
;; -> (1 1)

(jdbc/update! db-spec :fruit
  {:grade 7.0 :appearance "spotting" :cost 75}
  (sql/where {:name "Mango"}))
;; -> (1)

(->> (jdbc/query db-spec (sql/select "name, grade" :fruit))
     ;; Filter all fruits by fruits with grade < 3.0
     (filter (fn [{:keys [grade]}] (and grade (< grade 3.0))))
     (map (juxt :name :grade)))
;; -> (["Tomato" 1.4])
