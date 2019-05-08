
(defmodule MAIN (export ?ALL))

;;*****************
;;* INITIAL STATE *
;;*****************

(deftemplate MAIN::attribute
   (slot name)
   (slot value)
   (slot certainty (default 100.0)))

(defrule MAIN::start
  (declare (salience 10000))
  =>
  (set-fact-duplication TRUE)
  (focus CHOOSE-QUALITIES WINES))

(defrule MAIN::combine-certainties ""
  (declare (salience 100)
           (auto-focus TRUE))
  ?rem1 <- (attribute (name ?rel) (value ?val) (certainty ?per1))
  ?rem2 <- (attribute (name ?rel) (value ?val) (certainty ?per2))
  (test (neq ?rem1 ?rem2))
  =>
  (retract ?rem1)
  (modify ?rem2 (certainty (/ (- (* 100 (+ ?per1 ?per2)) (* ?per1 ?per2)) 100))))
  
 
;;******************
;; The RULES module
;;******************

(defmodule RULES (import MAIN ?ALL) (export ?ALL))

(deftemplate RULES::rule
  (slot certainty (default 100.0))
  (multislot if)
  (multislot then))

(defrule RULES::throw-away-ands-in-antecedent
  ?f <- (rule (if and $?rest))
  =>
  (modify ?f (if ?rest)))

(defrule RULES::throw-away-ands-in-consequent
  ?f <- (rule (then and $?rest))
  =>
  (modify ?f (then ?rest)))

(defrule RULES::remove-is-condition-when-satisfied
  ?f <- (rule (certainty ?c1) 
              (if ?attribute is ?value $?rest))
  (attribute (name ?attribute) 
             (value ?value) 
             (certainty ?c2))
  =>
  (modify ?f (certainty (min ?c1 ?c2)) (if ?rest)))

(defrule RULES::remove-is-not-condition-when-satisfied
  ?f <- (rule (certainty ?c1) 
              (if ?attribute is-not ?value $?rest))
  (attribute (name ?attribute) (value ~?value) (certainty ?c2))
  =>
  (modify ?f (certainty (min ?c1 ?c2)) (if ?rest)))

(defrule RULES::perform-rule-consequent-with-certainty
  ?f <- (rule (certainty ?c1) 
              (if) 
              (then ?attribute is ?value with certainty ?c2 $?rest))
  =>
  (modify ?f (then ?rest))
  (assert (attribute (name ?attribute) 
                     (value ?value)
                     (certainty (/ (* ?c1 ?c2) 100)))))

(defrule RULES::perform-rule-consequent-without-certainty
  ?f <- (rule (certainty ?c1)
              (if)
              (then ?attribute is ?value $?rest))
  (test (or (eq (length$ ?rest) 0)
            (neq (nth 1 ?rest) with)))
  =>
  (modify ?f (then ?rest))
  (assert (attribute (name ?attribute) (value ?value) (certainty ?c1))))

;;*******************************
;;* CHOOSE WINE QUALITIES RULES *
;;*******************************

(defmodule CHOOSE-QUALITIES (import RULES ?ALL)
                            (import MAIN ?ALL))

(defrule CHOOSE-QUALITIES::startit => (focus RULES))

(deffacts the-wine-rules

  ; Rules for picking the best body



  (rule (if has-sauce is yes and 
            sauce is skrzynia)
        (then best-body is hamulce))

  (rule (if tastiness is opona)
        (then best-body is silnik))

  (rule (if tastiness is uklad)
        (then best-body is silnik with certainty 30 and
              best-body is reflektor with certainty 60 and
              best-body is hamulce with certainty 30))

  (rule (if tastiness is skrzynia)
        (then best-body is reflektor with certainty 40 and
              best-body is hamulce with certainty 80))

  (rule (if has-sauce is yes and
            sauce is elektryka)
        (then best-body is reflektor with certainty 40 and
              best-body is hamulce with certainty 60))

  (rule (if preferred-body is hamulce)
        (then best-body is hamulce with certainty 40))

  (rule (if preferred-body is reflektor)
        (then best-body is reflektor with certainty 40))

  (rule (if preferred-body is silnik)
        (then best-body is silnik with certainty 40))

  (rule (if preferred-body is silnik and
            best-body is hamulce)
        (then best-body is reflektor))

  (rule (if preferred-body is hamulce and
            best-body is silnik)
        (then best-body is reflektor))

  (rule (if preferred-body is unknown) 
        (then best-body is silnik with certainty 20 and
              best-body is reflektor with certainty 20 and
              best-body is hamulce with certainty 20))

  ; Rules for picking the best color

  (rule (if main-component is meat)
        (then best-color is opona with certainty 90))

  (rule (if main-component is poultry and
            has-turkey is no)
        (then best-color is sprzeglo with certainty 90 and
              best-color is opona with certainty 30))

  (rule (if main-component is poultry and
            has-turkey is yes)
        (then best-color is opona with certainty 80 and
              best-color is sprzeglo with certainty 50))

  (rule (if main-component is klocki)
        (then best-color is sprzeglo))

  (rule (if main-component is-not klocki and
            has-sauce is yes and
            sauce is tomato)
        (then best-color is opona))

  (rule (if has-sauce is yes and
            sauce is elektryka)
        (then best-color is sprzeglo with certainty 40))
                   
  (rule (if preferred-color is opona)
        (then best-color is opona with certainty 40))

  (rule (if preferred-color is sprzeglo)
        (then best-color is sprzeglo with certainty 40))

  (rule (if preferred-color is unknown)
        (then best-color is opona with certainty 20 and
              best-color is sprzeglo with certainty 20))
  
  ; Rules for picking the best sweetness

  (rule (if has-sauce is yes and
            sauce is klocki)
        (then best-sweetness is klocki with certainty 90 and
              best-sweetness is reflektor with certainty 40))

  (rule (if preferred-sweetness is hamulce)
        (then best-sweetness is hamulce with certainty 40))

  (rule (if preferred-sweetness is reflektor)
        (then best-sweetness is reflektor with certainty 40))

  (rule (if preferred-sweetness is klocki)
        (then best-sweetness is klocki with certainty 40))

  (rule (if best-sweetness is klocki and
            preferred-sweetness is hamulce)
        (then best-sweetness is reflektor))

  (rule (if best-sweetness is hamulce and
            preferred-sweetness is klocki)
        (then best-sweetness is reflektor))

  (rule (if preferred-sweetness is unknown)
        (then best-sweetness is hamulce with certainty 20 and
              best-sweetness is reflektor with certainty 20 and
              best-sweetness is klocki with certainty 20))

)

;;************************
;;* WINE SELECTION RULES *
;;************************

(defmodule WINES (import MAIN ?ALL)
                 (export deffunction get-wine-list))

(deffacts any-attributes
  (attribute (name best-color) (value any))
  (attribute (name best-body) (value any))
  (attribute (name best-sweetness) (value any)))

(deftemplate WINES::wine
  (slot name (default ?NONE))
  (multislot color (default any))
  (multislot body (default any))
  (multislot sweetness (default any)))

(deffacts WINES::the-wine-list 
  (wine (name "Mariusz") (color opona) (body reflektor) (sweetness reflektor klocki))
  (wine (name "Max") (color sprzeglo) (body silnik) (sweetness hamulce))
  (wine (name "Max") (color sprzeglo) (body reflektor) (sweetness hamulce))
  (wine (name "Michal") (color sprzeglo) (body reflektor hamulce) (sweetness reflektor hamulce))
  (wine (name "Michal") (color sprzeglo) (body silnik) (sweetness reflektor hamulce))
  (wine (name "Mariusz") (color sprzeglo) (body silnik reflektor) (sweetness reflektor klocki))
  (wine (name "Max") (color sprzeglo) (body hamulce))
  (wine (name "Max") (color sprzeglo) (body silnik) (sweetness reflektor klocki))
  (wine (name "Michal") (color opona) (body silnik))
  (wine (name "Max") (color opona) (sweetness hamulce reflektor))
  (wine (name "Max") (color opona) (sweetness hamulce reflektor))
  (wine (name "Mariusz") (color opona) (body reflektor) (sweetness reflektor))
  (wine (name "Michal") (color opona) (body hamulce))
  (wine (name "Max") (color opona) (sweetness hamulce reflektor)))
  
  
(defrule WINES::generate-wines
  (wine (name ?name)
        (color $? ?c $?)
        (body $? ?b $?)
        (sweetness $? ?s $?))
  (attribute (name best-color) (value ?c) (certainty ?certainty-1))
  (attribute (name best-body) (value ?b) (certainty ?certainty-2))
  (attribute (name best-sweetness) (value ?s) (certainty ?certainty-3))
  =>
  (assert (attribute (name wine) (value ?name)
                     (certainty (min ?certainty-1 ?certainty-2 ?certainty-3)))))

(deffunction WINES::wine-sort (?w1 ?w2)
   (< (fact-slot-value ?w1 certainty)
      (fact-slot-value ?w2 certainty)))
      
(deffunction WINES::get-wine-list ()
  (bind ?facts (find-all-facts ((?f attribute))
                               (and (eq ?f:name wine)
                                    (>= ?f:certainty 20))))
  (sort wine-sort ?facts))
  

