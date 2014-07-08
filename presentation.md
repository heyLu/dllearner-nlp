% DL-Learner + NLP
% Lucas Stadler

# Idee

- Idee: viele Informationen im Semantic Web nur als Text enthalten,
    Extrahierung möglich
- Beispiel: `rdfs:comment`, `dbpedia-owl:abstract`

        dbpedia:Leipzig
            rdf:type dbpedia-owl:Town ;
            rdfs:comment "..." ;
            dbpedia-owl:abstract
              "Leipzig with more than 540,000 inhabitants. ,
               is the largest city by population (the second
               largest being Dresden) in the federal state of
               Saxony, Germany [...]" ; # shortened
            dbpedia-owl:populationTotal 500000 .
    
    - aus Wikipedia extrahiert (DBPedia)
    - schon an "der richtigen Stelle" (`dbpedia:Leipzig ...`)

# Extrahierbare Informationen

- Fakten
    * "more than 540,000 inhabitants"
    * "largest city by population"
    * "in [...] Saxony, Germany"
- Schemawissen
    * "_Leipzig_ with more than 540,000 **inhabitants**"

        "_<city>_ **inhabitants** <number>"
- Klassen und Properties
    * in Beziehung mit Bundesstaaten: "Saxony, Germany"

# NLP

- einfachste Möglichkeit: reguläre Ausdrücke

        [A-Z][a-z]+ (with more than|has) [0-9,\.]+ inhabitants

        [A-Z][a-z]+ is a [A-Za-z]+
    * Nachteile: nur bekannte Muster, sprachabhängig
- Named Entity Recognition (NER)
    * Erkennen von Begriffen im Text
    * Nachteile: Verlust von Strukturinformationen
- Part-of-Speech Tagging
    * Satzstruktur
    * darauf wiederum Erkennen von Mustern

<!--# DL-Learner

- DL-Learner: Lernen von Ontologien anhand von Beispielen

        x gehört zu Klasse y. z gehört nicht zu Klasse y.
-->

# Implementierung

- Extraktion aus DBPedia
- Instanzen zu Klassen finden

        SELECT ?entity ?abstract
        WHERE {
            ?entity rdf:type dbpedia-owl:Town ;
                    dbpedia-owl:abstract ?abstract .
            FILTER (lang(?abstract) = "en")
        }
- Annotationen mittels NER erhalten

# Implementierung

- Bibliothek um verschiedene Tools abzufragen
    (NERD, FOX, DBPedia Spotlight)
    
    * Normalisierung der Ergebnisse notwendig
- Metriken für die erhaltenen Annotationen
    * Anzahl der Annotationen (gesamt, eindeutig)
    * Länge der extrahierten Terme
    * erhaltene gemappte URLs (DBPedia, eigene, ...)
    * "overlaps" mit Referenzdaten

# Experimente

- Eignung von NER Frameworks (NER, FOX, DBPedia Spotlight)
    * Interpretation der Metriken
- Welche Bibliotheken unterstützen Extraktion von Fakten?
    * FRED
    * AlchemyAPI
    * OpenCalais

# Ergebnisse

- einheitliche API für NER Frameworks/APIs (Clojure)
    * Erweiterung zu REST Schnittstelle möglich/einfach
- Überblick über Features von diversen NLP Frameworks
- weitere Experimente notwendig (mehr Terme oder besseres Mapping?)
