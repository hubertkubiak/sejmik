# Liberum Veto 🏛️

> *„Nie pozwalam!”* Turowa gra konsolowa w Javie 21: jako Król próbujesz przeforsować ustawy na sejmach Rzeczypospolitej, a magnaci przekupują posłów, żeby jednym vetem zerwać obrady.

**EN:** A turn-based console strategy game about the 17th-century Polish-Lithuanian parliament, written in modern Java 21. A portfolio project for learning core Java (OOP, records, sealed types, pattern matching, Stream API, exceptions, file I/O, JUnit 5), built as the foundation for a Spring Boot + Angular stage 2.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9-blue?logo=apachemaven)
![JUnit](https://img.shields.io/badge/JUnit-5-green?logo=junit5)
![Coverage](https://img.shields.io/badge/coverage%20domain-%E2%89%A580%25-brightgreen)

---

## Zasady gry

- Gra trwa **5 sejmów**. W każdym Król dostaje budżet: **100 dukatów + 20 za każdą przyjętą ustawę**.
- Król **wybiera ustawę** z 3 wylosowanych. Każda ustawa inaczej wpływa na frakcje: królewską, magnacką i niezależną.
- **Magnaci** (komputer) od razu rozdają łapówki przeciw ustawie.
- Król **przekupuje posłów**, żeby podnieść ich poparcie.
- W **głosowaniu** każdy poseł liczy poparcie:

  ```
  poparcie = lojalność + wpływ ustawy na frakcję + (łapówki króla − łapówki magnatów) × przekupność
  ```

  | Poparcie | Głos |
  |---|---|
  | ≥ 50 | **ZA** |
  | 15–49 | **PRZECIW** |
  | < 15 | **VETO** z prawdopodobieństwem `(15 − poparcie) / 15` |

- **Jedno veto zrywa sejm.** Bez veta ustawa przechodzi zwykłą większością.
- **Wygrana Króla:** co najmniej 3 przyjęte ustawy. **Wygrana magnatów:** co najmniej 3 zerwane sejmy. Inaczej remis: *Rzeczpospolita trwa*.

## Przykładowa rozgrywka

```
=== SEJM 1/5 | Budżet króla: 100 dukatów ===
Wybierz ustawę:
 1) Zniesienie ceł na spław zboża              [królewska +0, magnacka +15, niezależna +10]
 2) Pobór łanowy na wojnę z Moskwą             [królewska +20, magnacka -20, niezależna +0]
 3) Potwierdzenie przywilejów szlacheckich     [królewska -10, magnacka +20, niezależna +15]
> 3
Magnaci rozdają łapówki... (przekupionych posłów: 3)
Obrady nad ustawą: Potwierdzenie przywilejów szlacheckich
Komenda (przekup <id> <kwota> | posłowie | statystyki | głosuj | zapisz | pomoc):
> statystyki
Średnia lojalność wobec Króla:
  królewska    69,7  (posłów: 6)
  magnacka     31,7  (posłów: 7)
  niezależna   52,0  (posłów: 7)
Top 3 najbardziej przekupnych:
  [5] Bogusław Leszczyński h. Trąby (1,00)
  ...
Zagrożeni vetem (3):
  [5] Bogusław Leszczyński h. Trąby: poparcie 0
  ...
```

Inna partia, czyli jak *nie* grać: łapówki dla posłów, którzy i tak byli ZA, to wyrzucone dukaty.

```
Obrady nad ustawą: Podatek na wojsko kwarciane
> przekup 5 35
Władysław Siciński h. Pobóg: poparcie 95 → 100 (zostało 65 dukatów)
> przekup 15 25
Paweł Sapieha h. Ostoja: poparcie 97 → 100 (zostało 40 dukatów)
> przekup 16 40
Michał Pac h. Pilawa: poparcie 29 → 60 (zostało 0 dukatów)
> głosuj
ZA: 11 | PRZECIW: 2 | VETO: 7
!!! Sejm zerwany przez: Krzysztof Zbaraski h. Ostoja
(gra zapisana w save.json)
```

Komendy: `przekup <id> <kwota>`, `posłowie` (`p`), `statystyki` (`s`), `głosuj` (`g`), `zapisz`, `pomoc` (`?`), `wyjdź`. Polskie znaki są opcjonalne (`poslowie` też działa).

## Uruchomienie

Wymagany jest **JDK 21**. Mavena nie trzeba instalować, bo projekt ma Maven Wrapper.

```bash
# build + testy + raport pokrycia + fat jar
./mvnw clean verify          # Windows: mvnw.cmd clean verify

# gra
java -jar target/liberum-veto.jar
```

- Stan gry zapisuje się do `save.json` w bieżącym katalogu. Przy następnym uruchomieniu gra zaproponuje wczytanie.
- Raport pokrycia testami znajdziesz w `target/site/jacoco/index.html`.

## Technologie

| Obszar | Technologia |
|---|---|
| Język | **Java 21 (LTS)** |
| Build | **Maven 3.9** + Maven Wrapper, maven-shade-plugin (fat jar) |
| Testy | **JUnit 5** (Jupiter, testy parametryzowane, `@Nested`, `@TempDir`), **AssertJ** |
| Pokrycie | **JaCoCo** z regułą ≥ 80% linii dla pakietów `domain` (build pada poniżej progu) |
| Serializacja | **Jackson Databind** (rekordy bez adnotacji) |
| UI | konsola (`Scanner` / `PrintStream`) |

## Koncepty Javy w projekcie

| Koncept | Gdzie |
|---|---|
| Klasy, hermetyzacja, `equals`/`hashCode` encji | [`Deputy`](src/main/java/pl/hubert/liberumveto/domain/model/Deputy.java) |
| Interfejs i polimorfizm | [`Voter`](src/main/java/pl/hubert/liberumveto/domain/model/Voter.java) |
| Klasa abstrakcyjna, dziedziczenie, wzorzec *template method* | [`Player`](src/main/java/pl/hubert/liberumveto/domain/player/Player.java), [`King`](src/main/java/pl/hubert/liberumveto/domain/player/King.java), [`Magnate`](src/main/java/pl/hubert/liberumveto/domain/player/Magnate.java) |
| Enumy z polami i metodami | [`Faction`](src/main/java/pl/hubert/liberumveto/domain/model/Faction.java), [`GameResult`](src/main/java/pl/hubert/liberumveto/domain/game/GameResult.java) |
| Rekordy, kompaktowy konstruktor, kopie obronne (`Map.copyOf`, `List.copyOf`) | [`Bill`](src/main/java/pl/hubert/liberumveto/domain/model/Bill.java), [`VoteResult`](src/main/java/pl/hubert/liberumveto/domain/sejm/VoteResult.java) |
| `sealed interface` + record patterns + wyczerpujący `switch` | [`Command`](src/main/java/pl/hubert/liberumveto/console/Command.java), [`ConsoleUI`](src/main/java/pl/hubert/liberumveto/console/ConsoleUI.java) |
| `switch` jako wyrażenie, text blocks | [`DeputyGenerator`](src/main/java/pl/hubert/liberumveto/domain/game/DeputyGenerator.java), [`ConsoleUI`](src/main/java/pl/hubert/liberumveto/console/ConsoleUI.java) |
| Wyjątki checked i unchecked, własne wyjątki z danymi, multi-catch, opakowywanie (`cause`) | [`exception/`](src/main/java/pl/hubert/liberumveto/domain/exception/), [`Game.vote`](src/main/java/pl/hubert/liberumveto/domain/game/Game.java), [`CommandParser`](src/main/java/pl/hubert/liberumveto/console/CommandParser.java) |
| Kolekcje: `EnumMap`, `LinkedHashMap`, niemutowalne listy | [`Sejm`](src/main/java/pl/hubert/liberumveto/domain/sejm/Sejm.java), [`Statistics`](src/main/java/pl/hubert/liberumveto/domain/game/Statistics.java) |
| Stream API: `groupingBy`, `counting`, `averagingInt`, `toMap`, komparatory, `IntStream`, `Optional` | [`Statistics`](src/main/java/pl/hubert/liberumveto/domain/game/Statistics.java), [`Sejm`](src/main/java/pl/hubert/liberumveto/domain/sejm/Sejm.java) |
| Lambdy, referencje do metod, `Supplier` / `Function` jako zależności | [`Main`](src/main/java/pl/hubert/liberumveto/console/Main.java), [`ConsoleUI.run`](src/main/java/pl/hubert/liberumveto/console/ConsoleUI.java) |
| Wstrzykiwanie zależności przez konstruktor (`Random`, `Scanner`), composition root | [`Main`](src/main/java/pl/hubert/liberumveto/console/Main.java) |
| Pliki: NIO.2 (`Path`, `Files`), zapis atomowy, try-with-resources, zasoby z classpath | [`SaveFile`](src/main/java/pl/hubert/liberumveto/console/save/SaveFile.java), [`TextResources`](src/main/java/pl/hubert/liberumveto/console/TextResources.java) |
| DTO + mapper (domena niezależna od Jacksona) | [`save/`](src/main/java/pl/hubert/liberumveto/console/save/) |
| Testy: parametryzowane, test double (`Random` z ustaloną wartością), round-trip z `usingRecursiveComparison` | [`src/test`](src/test/java/pl/hubert/liberumveto/) |

## Architektura

```
pl.hubert.liberumveto
├── domain                czysta Java, bez I/O i bibliotek
│   ├── model             Deputy, Bill, Faction, Vote, BillType, Voter
│   ├── player            Player, King, Magnate (AI)
│   ├── sejm              Sejm, VoteResult
│   ├── game              Game, GameState, Statistics, DeputyGenerator, BillCatalog
│   └── exception         SejmDissolved, InsufficientBudget, InvalidChoice, SaveGame
└── console               UI i infrastruktura
    ├── Main, ConsoleUI, CommandParser, Command, TextResources
    └── save              SaveFile, SavedGame (DTO), SaveMapper (Jackson)
```

Logika gry nie wie nic o konsoli: `Game` udostępnia operacje `selectBill` → `bribe` → `vote`, które w etapie 2 wywoła kontroler REST zamiast konsoli.

Kod (nazwy, komentarze, testy, klucze JSON) jest po angielsku. Po polsku jest tylko to, co widzi gracz: komunikaty, komendy, tytuły ustaw, imiona i herby.

### Słowniczek

| Polski | W kodzie |
|---|---|
| poseł | `Deputy` |
| ustawa | `Bill` |
| frakcja (królewska / magnacka / niezależna) | `Faction` (`ROYAL` / `MAGNATE` / `INDEPENDENT`) |
| głos (ZA / PRZECIW / VETO) | `Vote` (`FOR` / `AGAINST` / `VETO`) |
| poparcie, lojalność, przekupność | support, `loyaltyToKing`, `corruptibility` |
| łapówka, przekupić | bribe |
| zerwanie sejmu | `SejmDissolvedException`, `VoteResult.dissolved()` |
| herb | `coatOfArms` |

## Roadmapa

- [x] M1: model domeny (`v0.1-model`)
- [x] M2: algorytm głosowania i `Sejm` (`v0.2-glosowanie`)
- [x] M3: konsolowa pętla gry, przekupstwa, AI magnatów (`v0.3-konsola`)
- [x] M4: statystyki na Streamach, zapis i odczyt JSON (`v0.4-statystyki-zapis`)
- [ ] M5: multi-module Maven + Spring Boot REST API (JPA, H2, Swagger)
- [ ] M6: front w Angularze, CI na GitHub Actions

Pełna specyfikacja: [SPEC.md](SPEC.md). Historia zmian: [CHANGELOG.md](CHANGELOG.md).
