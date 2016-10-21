### Popis

Transformuje vstupné dáta pomocou aktualizačného SPARQL dotazu.

Podporuje [RDF validáciu](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).

### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|**Púšťanie dotazov po grafoch** | ak zaškrtnuté, dotazy sú púšťané po grafoch |
|**SPARQL aktualizačný dotaz** | Textové pole určené pre SPARQL aktualizačný dotaz |

### Vstupy a výstupy

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input  |vstup| RDFDataUnit | RDF vstupné dáta |áno|
|output |výstup| RDFDataUnit | RDF výstupné (transformované) dáta |áno|
