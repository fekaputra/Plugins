### Popis

Stiahne súbory podľa zoznamu definovaného v konfigurácii. Je možné stiahnuť samostatné súbory alebo aj celé adresáre.

### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|**Zoznam súborov a adresárov na stiahnutie** | Keď je uvedený adresár, stiahnu sa všetky súbory v adresári a jeho podadresároch.|
Ak je zadaný názov každého sťahovaného vstupu, tento názov sa použije na vnútornú identifikáciu daného súboru v ďalšom pokračovaní procesu a tiež ako názov
virtuálnej cesty (cieľové umiestnenie súboru pri nahrávaní mimo UnifiedViews na konci procesu). Ak vás nezaujíma vnútorné pomenovanie súboru alebo názov virtuálnej cesty, napr.
v prípadoch keď potrebujete iba prechádzať stiahnuté súbory v pokračovaní procesu tým istým spôsobom, nie je potrebné špecifikovať meno súboru.
|**Mäkké zlyhanie** | Ak je zaškrtnuté a nastane nejaká chyba v spracovaní VFS položky alebo súboru, tak sa zobrazí varovanie a extrakcia v DPU pokračuje ďalej. Ak zaškrtnuté nie je (default), tak v prípade chyby pri spracovaní ktorejkoľvek VFS položky alebo súboru je extrakcia prerušená a končí chybou.  |
|**Vynechať duplicitné vstupné súborové položky** | Ak je zaškrtnuté, DPU skontroluje či sa niektoré súborové URI nespracúvajú viacnásobne (môže sa stať keď je DPU konfigurované dynamicky). A ak áno, duplicitné spracovanie je vynechané a do logu je pridaná informácia o tomto vynechaní.  |
|**Čakať medzi volaniami (ms)**| Čas v milisekundách, ktorý má DPU stráviť čakaním medzi dvoma HTTP volaniami (default je 0, čo znamená medzi volaniami nečakať) |

### Vstupy a výstupy

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|config |vstup| RdfDataUnit | Dynamická RDF konfigurácia, pozri Pokročilá konfigurácia | |
|output |výstup| FilesDataUnit | Stiahnuté súbory |áno|


### Pokročilá konfigurácia

Krok je možné nakonfigurovať aj dynamicky cez vstup `config` pomocou RDF dát.

Vzor konfigurácie:

```turtle
<http://localhost/resource/config> 
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/filesDownload/Config>;
    <http://unifiedviews.eu/ontology/dpu/filesDownload/hasFile> <http://localhost/resource/file/0>.
```

```turtle
<http://localhost/resource/file/0>
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/filesDownload/File>;
    <http://unifiedviews.eu/ontology/dpu/filesDownload/file/uri> "http://www.zmluvy.gov.sk/data/att/117597_dokument.pdf";
    <http://unifiedviews.eu/ontology/dpu/filesDownload/file/fileName> "zmluva.pdf".`
```
