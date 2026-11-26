# Changelog

Format na wzór [Keep a Changelog](https://keepachangelog.com/pl/1.1.0/). Każdy kamień milowy ze [SPEC.md](SPEC.md) ma swój tag w gicie.

## [Unreleased]
### Dodane (M5, krok 4)
- Walidacja żądań (`@Valid`, `@NotNull`, `@Positive`) z polskimi komunikatami.
- `ApiExceptionHandler` (`@RestControllerAdvice`): wszystkie błędy jako `application/problem+json` (RFC 9457); 400 / 404 / 409 / 422 / 500.
- Wyjątek domenowy `ActionNotAllowedException` (gra zakończona, ustawa już wybrana) z polskim komunikatem.

### Dodane (M5, krok 3)
- Endpointy pełnej tury: `GET /bills`, `PUT /selected-bill`, `GET /deputies` (filtr `faction`, sortowanie `sort`), `POST /bribes`, `POST /votes`, `GET /history`.
- Synchronizacja dostępu do gry w `GameService` (jeden wątek naraz na grę).
- Parametry-enumy w URL bez rozróżniania wielkości liter (`?faction=royal`).
- Test pełnej gry przez API (`GameFlowTest`).

### Dodane (M5, krok 2)
- Moduł `api`: Spring Boot 4.2, `POST /api/games` (201 + Location) i `GET /api/games/{id}`, warstwy controller → service → repository, repozytorium w pamięci, testy `MockMvcTester`.
- Pliki `names.txt` i `coats-of-arms.txt` przeniesione do modułu `domain`, bo korzystają z nich konsola i API.
- JUnit 5 → 6.1 (zgodnie z Spring Boot 4.2), flaga kompilatora `-parameters`.
- Endpointy API w SPEC po angielsku; wybór ustawy jako osobny krok (`PUT /selected-bill`).

### Zmienione (M5, krok 1)
- Projekt wielomodułowy: `liberum-veto-parent` → `domain` (bez zależności runtime, reguła JaCoCo 80%) i `console` (UI, zapis JSON, fat jar w `console/target/liberum-veto.jar`).
- `SaveGameException` przeniesiony z domeny do `console.save`, bo dotyczy plików, a domena nie robi I/O.

### Zmienione
- Kod przetłumaczony na angielski: pakiety (`player`, `game`, `exception`, `console.save`), klasy, metody, zmienne, komentarze, nazwy testów, klucze JSON zapisu i nazwy plików zasobów (`names.txt`, `coats-of-arms.txt`). Interfejs gracza pozostaje po polsku.
- Zapisy gry z wcześniejszych wersji (polskie klucze JSON) nie są kompatybilne.

## [v0.4-statystyki-zapis] (M4)
### Dodane
- Statystyki sejmu na Stream API: średnia lojalność per frakcja, top 3 najbardziej przekupnych, posłowie zagrożeni vetem, średnie poparcie (komenda `statystyki`).
- Zapis i odczyt stanu gry w `save.json` (Jackson, rekordy DTO oddzielone od domeny), autozapis po każdym sejmie, komenda `zapisz`.
- Wybór na starcie: wczytanie zapisu albo nowa gra.
- Fat jar (`target/liberum-veto.jar`) z maven-shade-plugin.
- JaCoCo: raport pokrycia i reguła minimum 80% linii dla pakietów `domain`.

## [v0.3-konsola] (M3)
### Dodane
- Konsolowa pętla gry (`KonsolaUI`), komendy jako `sealed interface` + rekordy, `ParserKomend` akceptujący polskie znaki.
- Generator posłów z imionami i herbami z `resources/imiona.txt` i `herby.txt`.
- Katalog ustaw, `StanGry`, `Gra` z warunkami końca i budżetami zależnymi od wyników.
- AI magnatów: przekupują posłów, których najtaniej zepchnąć w strefę veta.

## [v0.2-glosowanie] (M2)
### Dodane
- Algorytm poparcia i decyzji posła (ZA / PRZECIW / VETO) z wstrzykiwanym `Random`.
- `Sejm.obraduj()`, `WynikGlosowania`, checked `SejmZerwanyException`.
- Gracze: abstrakcyjny `Gracz` (metoda szablonowa `przekup`), `Krol`, `Magnat`; wyjątek `NiewystarczajacyBudzetException`.
- Testy parametryzowane dla progów 49/50 i 14/15.

## [v0.1-model] (M1)
### Dodane
- Projekt Maven z wrapperem, Java 21.
- Enumy `Frakcja`, `Glos`, `TypUstawy` i niemutowalny rekord `Ustawa`.
