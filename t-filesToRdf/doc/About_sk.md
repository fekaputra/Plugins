### Popis

Získa RDF dáta zo súborov ľubovoľného RDF formátu. Výstupná dátová jednotka RDF obsahuje jeden záznam pre každý vstupný súbor (každý vstupný súbor vyprodukuje jeden graf RDF dát).

Pri predvolenom nastavení je RDF formát detekovaný automaticky na základe prípony vstupného súboru. V prípade, že detekcia bude nesprávna, špecifikujte prosím RDF formát vstupných súborov manuálne.

Podporuje [RDF validáciu](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).

### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|**RDF formát vstupných súborov** | RDF formát dát vo vstupných súboroch. AUTO = automatická detekcia RDF formátu vstupných súborov (predvolené) |
|**Čo robiť keď zlyhá transformácia jedného súboru** | Zastaviť vykonávanie alebo pokračovať a preskočiť na ďaľší súbor |
|**Ako vyberať symbolické meno výstupu** | Použiť symbolické mená vstupných súborov alebo použiť jedno pevné symbolické meno |
|**Použi názov súboru ako meno virtuálneho grafu** | Ak je zaškrtnuté, DPU automaticky vygeneruje metaúdaje o virtuálnom grafe, ktoré budú rovnaké ako meno súboru (takže sa prepokladá, že názov súboru je napr. HTTP URL). |

### Vstupy a výstupy ###

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|filesInput |vstup| FilesDataUnit | Vstupné súbory s dátami |áno|
|rdfOutput  |výstup| RDFDataUnit | Získané RDF dáta |áno|
