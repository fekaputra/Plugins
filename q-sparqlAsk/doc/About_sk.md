### Description

Vykoná zadaný SPARQL ASK dopyt ktorý ak vráti "NO", tak sa spracúvané údaje interpretujú ako nevyhovujúce určitým obmedzeniam.

V prípade keď SPARQL ASK dopyt vráti "NO", skončí spracovanie s chybou alebo sa pridá do logu varovanie (default správanie je ukončenie s chybou, používateľ to však môže zmeniť).

DPU kopíruje vstupné dáta na výstup, takže je vhodné ho umiestniť priamo do stredu toku údajov.

### Configuration parameters

| Meno | Popis |
|:----|:----|
|**Typ správy** | Typ správy použitý v prípade, že SPARQL ASK dopyt vráti "NO" (chyba alebo varovanie) |
|**SPARQL ASK dopyt** | SPARQL ASK dopyt ktorý bude vykonávaný. |

### Inputs and outputs

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input |i| RdfDataUnit | Údaje ktoré budú kontrolované |x|
|output |o| RdfDataUnit | Skontrolované údaje |x|

