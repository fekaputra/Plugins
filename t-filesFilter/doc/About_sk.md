### Popis

Filtruje súbory na základe filtrovacieho vzoru definovaného v konfigurácii. Podporované sú aj regulárne výrazy.

### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|**Aktuálna šablóna** | Filtrovací vzor, napr. '.*csv' |
|**Použiť regulárny výraz (checkbox)** | Ak je checkbox aktívny, na vstupe sú akceptované [regulárne výrazy](http://www.w3.org/TR/xpath-functions/#regex-syntax). Regulárne výrazy sa NEAPLIKUJÚ na názvy súborov, ale na tzv. symbolicName, ktoré je nastavené na základe tzv. virtualPath (ohľadom významu symbolicName a virtualPath prosím konzultujte [dokumentáciu](https://grips.semantic-web.at/display/UDDOC/Basic+Concepts+for+DPU+developers)). |

### Vstupy a výstupy

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input  |vstup| FilesDataUnit | Zoznam súborov pre filtrovanie |áno|
|output |výstup| FilesDataUnit | Zoznam súborov vyhovujúcich filtru |áno|
