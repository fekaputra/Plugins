### Popis

Premenuje súbory

### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|**Vzor** | Regulárny výraz použitý na získanie reťazca, ktorý má byť nahradený v názve súboru. Táto hodnota je použitá ako časť náhrady (druhý argument) v SPARQL REPLACE |
|**Náhrada** | Hodnota, ktorou sa bude nahrádzať, môže odkazovať na skupiny vyhovujúce parametru 'Vzor'. Táto hodnota sa používa ako časť náhrady (tretí argument) v SPARQL REPLACE |

### Vstupy a výstupy ###

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|inFilesData  |vstup| FilesDataUnit | Súbory, ktoré majú byť premenované |áno|
|outFilesData |výstup| FilesDataUnit | Premenované súbory |áno|