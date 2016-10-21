### Popis

Konvertuje tabuľkové dáta (SQL databázové tabuľky) na RDF dáta.

Podporuje [RDF validáciu](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).

### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|Základ URI zdroja | Táto hodnota sa použije pri automatickom generovaní vlastností stĺpcov a tiež na vytvorenie absolútnej URI ak je v stĺpci 'URI vlastnosti' poskytnutá relatívna URI |
|Stĺpec kľúča | Názov stĺpca, ktorý bude pridaný k 'Základ URI vlastnosti' a použitý ako predmet pre riadky. |
|Trieda pre entitu riadku | Táto hodnota sa použije ako trieda pre všetky entity riadkov |
|Mapovať všetky stĺpce | Východzie mapovanie je automaticky vygenerované pre každý stĺpec |
|Generovať stĺpec pre riadok | Stĺpec s číslom riadku je generovaný pre každý riadok |
|Generovať triedu pre tabuľku/riadok | Pre výrazy tabuľkových entít s typom sú generované triedy  |
|Generovať predmet pre tabuľku | Vytvorí sa predmet pre každú tabuľku, ktorý odkazuje na všetky riadky v danej tabuľke |
|Generovať označenia | Označenia (rdfs:labels) sú generované do URI stĺpcov |
|Rozšírený stĺpec kľúča | 'Stĺpec kľúča' je interpretovaný ako šablóna. Experimentálna funkcionalita! |
|Automatický typ ako reťazec | Všetky automatické typy sú považované za reťazce |
|Jednoduché mapovanie | Vlastné mapovanie stĺpcov na základe názvu stĺpca v zdrojovej tabuľke |


### Vstupy a výstupy

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|tablesInput |vstup| RelationalDataUnit| Vstupné databázové tabuľky |áno|
|rdfOutput |výstup| RDFDataUnit  | RDF dáta na výstupe |áno|
