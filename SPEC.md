# Liberum Veto: symulator sejmiku szlacheckiego

> Projekt portfolio pokazujący naukę Javy od podstaw (core Java), a potem przejście do backendu (Spring Boot) z frontem w Angularze.

---

## 1. Cel projektu

**Cel edukacyjny:** pokazać w jednym, spójnym projekcie:
- programowanie obiektowe (klasy, dziedziczenie, interfejsy, enumy, rekordy),
- kolekcje i Stream API,
- obsługę wyjątków (w tym własnych),
- operacje na plikach (zapis i odczyt stanu gry),
- testy jednostkowe (JUnit 5),
- budowanie projektu Mavenem,
- w etapie 2: REST API, bazę danych, integrację z frontem.

**Cel produktowy:** grywalna, prosta gra turowa w konsoli. Gracz jako **Król** próbuje przeforsować ustawy na kolejnych sejmach. **Magnaci** przekupują posłów i grożą zerwaniem obrad przez *liberum veto*.

---

## 2. Stack technologiczny

| Obszar | Etap 1 | Etap 2 |
|---|---|---|
| Język | Java 21 (LTS) | Java 21 |
| Build | Maven | Maven |
| Testy | JUnit 5, AssertJ | + Spring Boot Test, MockMvc |
| Serializacja | Jackson (JSON) | Jackson |
| Persystencja | plik JSON | Spring Data JPA, H2 (dev) / PostgreSQL (opcjonalnie) |
| UI | konsola (`Scanner`) | Angular (osobny projekt) |
| Jakość | Checkstyle lub Spotless (opcjonalnie) | + GitHub Actions CI |

---

## 3. Zasady gry

### 3.1. Przebieg
1. Gra trwa **5 sejmów**. Każdy sejm to jedna tura.
2. Na początku tury Król dostaje **budżet dukatów** (np. 100 + bonus za poprzednie sukcesy).
3. Król **wybiera ustawę** z listy dostępnych projektów.
4. **Magnaci** (sterowani przez komputer) rozdzielają swój budżet na przekupstwo posłów przeciw ustawie.
5. Król może **przekupić wybranych posłów** (podnieść ich poparcie).
6. Odbywa się **głosowanie**: każdy poseł głosuje ZA, PRZECIW albo zgłasza **VETO**.
7. Jeśli padnie choć jedno veto, **sejm zostaje zerwany** (`SejmZerwanyException`), a ustawa przepada.
8. Bez veta ustawa przechodzi zwykłą większością głosów.

### 3.2. Warunki końca gry
- **Wygrana Króla:** przyjęte co najmniej 3 ustawy z 5.
- **Wygrana magnatów:** zerwane co najmniej 3 sejmy.
- W pozostałych przypadkach remis, czyli „Rzeczpospolita trwa”.

### 3.3. Decyzja posła (algorytm)
Dla każdego posła liczymy **poparcie** (0–100):

```
poparcie = lojalnoscWobecKrola
         + wplywUstawyNaFrakcje(frakcjaPosla)
         + lapowkiOdKrola * wspolczynnikPrzekupnosci
         - lapowkiOdMagnatow * wspolczynnikPrzekupnosci
```

Wynik obcinamy do zakresu 0–100.

- `poparcie >= 50` → **ZA**
- `15 <= poparcie < 50` → **PRZECIW**
- `poparcie < 15` → **VETO** z prawdopodobieństwem `(15 - poparcie) / 15`, w przeciwnym razie **PRZECIW**

Losowość idzie przez wstrzykiwany `java.util.Random` (z ziarnem), dzięki czemu wyniki są **deterministyczne w testach**.

---

## 4. Model domeny

### 4.1. Enumy
```java
enum Frakcja { KROLEWSKA, MAGNACKA, NIEZALEZNA }
enum Glos { ZA, PRZECIW, VETO }
enum TypUstawy { PODATEK, POBOR_WOJSKA, PRZYWILEJ_SZLACHECKI, REFORMA_SKARBU }
```

### 4.2. Klasy i rekordy

| Typ | Rodzaj | Pola / odpowiedzialność |
|---|---|---|
| `Posel` | klasa | `id`, `imie`, `herb`, `frakcja`, `lojalnoscWobecKrola` (0–100), `przekupnosc` (0.0–1.0), `lapowkiKrola`, `lapowkiMagnatow` |
| `Ustawa` | record | `id`, `tytul`, `typ`, `Map<Frakcja, Integer> wplyw` (np. podatek: KROLEWSKA +20, MAGNACKA −30) |
| `Glosujacy` | interfejs | `Glos zaglosuj(Ustawa ustawa, Random random)` |
| `Posel implements Glosujacy` | | implementuje algorytm z 3.3 |
| `Gracz` | klasa abstrakcyjna | `nazwa`, `budzet`, `przekup(Posel, int kwota)` |
| `Krol extends Gracz` | klasa | sterowany przez użytkownika |
| `Magnat extends Gracz` | klasa | strategia AI: `rozdzielLapowki(List<Posel>, Ustawa)` |
| `WynikGlosowania` | record | `Map<Glos, Long> liczbaGlosow`, `boolean przyjeta`, `Optional<Posel> zrywajacy` |
| `Sejm` | klasa | przeprowadza jedną turę: `WynikGlosowania obraduj(Ustawa)` |
| `StanGry` | klasa | numer sejmu, posłowie, budżety, historia wyników |
| `Gra` | klasa | pętla gry, warunki końca |

### 4.3. Wyjątki
| Wyjątek | Typ | Kiedy |
|---|---|---|
| `SejmZerwanyException` | checked | poseł zgłosił veto; zawiera referencję do posła |
| `NiewystarczajacyBudzetException` | unchecked | próba przekupstwa ponad budżet |
| `NieprawidlowyWyborException` | unchecked | błędne dane z konsoli |
| `BladZapisuGryException` | checked | problem z odczytem lub zapisem pliku |

---

## 5. Wymagania funkcjonalne: etap 1 (konsola)

| ID | Wymaganie |
|---|---|
| F1 | Start gry: nowa gra albo wczytanie zapisu z pliku `save.json` |
| F2 | Generowanie 20 posłów z losowymi atrybutami (imiona i herby z plików `resources/imiona.txt`, `herby.txt`) |
| F3 | Wyświetlenie listy ustaw do wyboru w danej turze (3 losowe z puli) |
| F4 | Przekupstwo: Król wybiera posła po ID i podaje kwotę; walidacja budżetu |
| F5 | Ruch magnatów (AI): przekupują posłów najbliższych progu veta |
| F6 | Głosowanie i czytelny raport: liczba głosów, kto zerwał sejm |
| F7 | Statystyki (Stream API): średnia lojalność per frakcja, top 3 najbardziej przekupnych posłów, posłowie „zagrożeni vetem” |
| F8 | Zapis stanu gry do JSON po każdej turze i na żądanie |
| F9 | Ekran końca gry z wynikiem i historią sejmów |
| F10 | Komenda `pomoc` z listą dostępnych komend |

### Przykładowa sesja
```
=== SEJM 2/5 | Budżet króla: 120 dukatów ===
Wybierz ustawę:
 1) Podatek na wojsko kwarciane
 2) Reforma skarbu koronnego
 3) Potwierdzenie przywilejów szlacheckich
> 1
Magnaci rozdają łapówki...
Komenda (przekup <id> <kwota> | posłowie | statystyki | glosuj | zapisz):
> przekup 7 30
Jan Zamoyski h. Jelita: poparcie 12 → 34
> glosuj
ZA: 11 | PRZECIW: 8 | VETO: 1
!!! Sejm zerwany przez: Stanisław Radziwiłł h. Trąby
```

---

## 6. Wymagania funkcjonalne: etap 2 (Spring Boot + Angular)

### 6.1. REST API
| Metoda | Endpoint | Opis |
|---|---|---|
| `POST` | `/api/games` | nowa gra → `GameDto` |
| `GET` | `/api/games/{id}` | stan gry |
| `GET` | `/api/games/{id}/deputies` | lista posłów (filtry: `?faction=`, `?sort=support`) |
| `GET` | `/api/games/{id}/bills` | ustawy dostępne w turze |
| `PUT` | `/api/games/{id}/selected-bill` | `{ billId }`: wybór ustawy, po którym magnaci od razu rozdają łapówki |
| `POST` | `/api/games/{id}/bribes` | `{ deputyId, amount }` |
| `POST` | `/api/games/{id}/votes` | głosowanie nad wybraną ustawą → `VoteResultDto` |
| `GET` | `/api/games/{id}/history` | wyniki poprzednich sejmów |

Uwaga: URL-e i pola JSON są po angielsku, bo API jest kontraktem dla programistów, a nie komunikacją z graczem. Wybór ustawy to osobny krok przed przekupstwami, zgodnie z przebiegiem tury z punktu 3.1 (magnaci przekupują przed Królem).

### 6.2. Wymagania techniczne
- Warstwy: `controller` → `service` → `repository`, z DTO oddzielonymi od encji.
- Logika gry z etapu 1 **przenoszona bez zmian** do modułu `domain`, bez zależności od Springa.
- Globalna obsługa błędów (`@RestControllerAdvice`) i spójny format błędu.
- Walidacja wejścia (`@Valid`, `@Positive` itd.).
- Dokumentacja API: springdoc-openapi (Swagger UI).
- CORS skonfigurowany dla `http://localhost:4200`.

### 6.3. Front (Angular)
- Widok sali sejmowej: kafelki posłów pokolorowane wg poparcia.
- Panel przekupstwa i przycisk „Głosuj”.
- Animacja lub komunikat przy zerwaniu sejmu.
- Standalone components, signals, `HttpClient` z typowanymi DTO.

---

## 7. Struktura projektu

```
liberum-veto/
├── pom.xml                     (parent, multi-module od etapu 2)
├── domain/                     (czysta Java: model, logika, wyjątki)
│   └── src/main/java/pl/hubert/liberumveto/domain/
│       ├── model/              Posel, Ustawa, Frakcja, Glos...
│       ├── gracz/              Gracz, Krol, Magnat
│       ├── sejm/               Sejm, WynikGlosowania
│       ├── gra/                Gra, StanGry
│       └── wyjatki/
├── console/                    (etap 1: UI konsolowe, zapis do pliku)
│   └── .../console/            Main, KonsolaUI, ParserKomend, ZapisGry
├── api/                        (etap 2: Spring Boot)
│   └── .../api/                controller, service, repository, dto, config
└── README.md
```

W etapie 1 wystarczy jeden moduł z pakietami `domain` i `console`. Podział na moduły Mavena robimy przy przejściu do etapu 2.

---

## 8. Testy

**Wymagane minimum (etap 1):**
- `PoselTest`: decyzja ZA, PRZECIW i VETO dla granicznych wartości poparcia (49/50, 14/15).
- `SejmTest`: ustawa przechodzi przy większości; jedno veto zrywa sejm (`assertThrows`).
- `KrolTest`: przekupstwo zmniejsza budżet; przekroczenie budżetu rzuca wyjątek.
- `GraTest`: warunki wygranej i przegranej.
- `ZapisGryTest`: zapis i odczyt dają identyczny stan (round-trip).
- Testy parametryzowane (`@ParameterizedTest`) dla algorytmu poparcia.

**Etap 2:** testy kontrolerów (MockMvc) i test integracyjny pełnej tury przez API.

**Cel pokrycia:** co najmniej 80% dla modułu `domain` (JaCoCo).

---

## 9. Wymagania niefunkcjonalne

- Kod po angielsku: pakiety, klasy, metody, zmienne, komentarze, testy, klucze JSON. Po polsku (UTF-8) tylko to, co widzi gracz: komunikaty konsoli, komendy, tytuły ustaw, imiona i herby. Nazwy klas w tabelach powyżej to pierwotne polskie robocze nazwy; odpowiedniki są w słowniczku w README.
- Brak `static` stanu globalnego; zależności (`Random`, `Scanner`) przekazywane przez konstruktor.
- Niemutowalność tam, gdzie się da (`record`, `List.copyOf`).
- Każda klasa ma jedną odpowiedzialność; logika gry nie wie nic o konsoli.
- `mvn clean verify` przechodzi bez błędów.

---

## 10. Kamienie milowe

| # | Zakres | Co pokazuje | Szac. czas |
|---|---|---|---|
| M1 | Setup Maven, model domeny, enumy, rekordy | podstawy składni, OOP | 2–3 dni |
| M2 | Algorytm głosowania i `Sejm` + testy | logika, wyjątki, JUnit | 3–4 dni |
| M3 | Konsolowa pętla gry, przekupstwa, AI magnatów | I/O, kolekcje | 3–4 dni |
| M4 | Statystyki na Streamach, zapis i odczyt JSON | Stream API, pliki, Jackson | 2–3 dni |
| M5 | Refaktor na multi-module, Spring Boot API | Spring, REST, JPA | 1–2 tyg. |
| M6 | Front w Angularze, CI na GitHub Actions | full stack, DevOps | 1 tydz. |

Każdy kamień milowy kończy się tagiem w gicie (`v0.1-model`, `v0.2-glosowanie`…) i wpisem w `CHANGELOG.md`.

---

## 11. Kryteria akceptacji (Definition of Done)

- [x] Można rozegrać pełną grę (5 sejmów) w konsoli od startu do ekranu końcowego.
- [x] Gra zapisuje się i wczytuje bez utraty stanu.
- [x] Wszystkie testy przechodzą, pokrycie `domain` co najmniej 80%.
- [x] README zawiera: opis gry, zrzut lub GIF z rozgrywki, instrukcję uruchomienia, listę użytych konceptów Javy.
- [x] (Etap 2) API udokumentowane w Swaggerze, front komunikuje się z backendem.

---

## 12. Pomysły na rozszerzenia (poza zakresem)

- Kilku magnatów z różnymi strategiami (wzorzec **Strategy**).
- Zdarzenia losowe między sejmami: wojna, zaraza, elekcja (wzorzec **Observer** lub event bus).
- Konfederacja: zawiązanie jej wyłącza veto, ale kosztuje lojalność.
- Ranking graczy w bazie danych.
- Wielowątkowa symulacja 1000 gier do balansowania parametrów (`ExecutorService`), czyli wstęp do współbieżności.
