### Popis

Transformuje vstupné dáta pomocou konštrukčného SPARQL dotazu.

Podporuje [RDF validáciu](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).


### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|**Púšťanie dotazov po grafoch** | Ak zaškrtnuté, dotazy sú púšťané po grafoch |
|**SPARQL konštrukčný dotaz** | Textové pole určené pre SPARQL konštrukčný dotaz |

### Vstupy a výstupy

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input  |vstup| RDFDataUnit | RDF vstupné dáta |áno|
|output |výstup| RDFDataUnit | RDF výstupné (transformované) dáta |áno|
