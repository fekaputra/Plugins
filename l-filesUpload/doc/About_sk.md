### Popis

Nahrá zoznam súborov na definované miesto.

### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|**Úplná cesta k cieľovému súboru** | Cieľová cesta k súborom, kam sa budú nahrávať|
|**Používateľské meno** | Používateľské meno na cieľovom hoste|
|**Heslo** | Heslo príslušné k používateľskému menu|

### Vstupy a výstupy

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input  |i| FilesDataUnit | Súbory na nahratie do cieľového adresáru |x|
|output |o| FilesDataUnit | Rovnaké ako vstup, len s aktualizáciou času poslednej modifikácie (Resource.last_modified time) ||