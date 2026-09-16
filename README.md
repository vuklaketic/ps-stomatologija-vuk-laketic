# PS Stomatologija

Java klijent-server aplikacija za zakazivanje stomatoloških termina, razvijena u okviru predmeta **Projektovanje softvera**.

## Opis

Aplikacija omogućava zakazivanje i upravljanje terminima u stomatološkoj ordinaciji kroz klijent-server arhitekturu. Server obrađuje poslovnu logiku i komunikaciju sa bazom podataka, dok klijent predstavlja korisnički interfejs za pacijente i/ili osoblje ordinacije.

## Struktura projekta

- **`Klijent/`** – klijentska aplikacija (korisnički interfejs)
- **`Server/`** – serverska aplikacija (poslovna logika, komunikacija sa bazom)
- **`Zajednicki/`** – deljene klase i resursi koje koriste i klijent i server (npr. modeli, DTO objekti)
- **`lib/`** – eksterne biblioteke potrebne za rad projekta

## Tehnologije

- Java
- Klijent-server arhitektura (socket komunikacija)
- Relaciona baza podataka

## Pokretanje projekta

1. Klonirati repozitorijum:
   ```bash
   git clone https://github.com/vuklaketic/ps-stomatologija-vuk-laketic.git
   ```
2. Podesiti konekciju ka bazi podataka u `Server/baza.properties` (fajl nije uključen u repozitorijum iz bezbednosnih razloga – potrebno ga je kreirati lokalno).
3. Pokrenuti server aplikaciju iz `Server/` foldera.
4. Pokrenuti klijent aplikaciju iz `Klijent/` foldera.

## Napomena

Projekat je izrađen u edukativne svrhe, u okviru fakultetskog kursa Projektovanje softvera.
