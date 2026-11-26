package pl.hubert.liberumveto.api.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.hubert.liberumveto.api.dto.BillDto;
import pl.hubert.liberumveto.api.dto.BillSelectionDto;
import pl.hubert.liberumveto.api.dto.BribeRequest;
import pl.hubert.liberumveto.api.dto.DeputyDto;
import pl.hubert.liberumveto.api.dto.DeputySort;
import pl.hubert.liberumveto.api.dto.GameDto;
import pl.hubert.liberumveto.api.dto.SejmResultDto;
import pl.hubert.liberumveto.api.dto.SelectBillRequest;
import pl.hubert.liberumveto.api.dto.SupportChangeDto;
import pl.hubert.liberumveto.api.dto.VoteResponseDto;
import pl.hubert.liberumveto.api.service.GameService;
import pl.hubert.liberumveto.domain.model.Faction;

/**
 * The HTTP layer: maps URLs to service calls. No game logic here.
 *
 * <p>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}: whatever a method returns
 * is written to the response body as JSON (by Jackson).
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    /** POST /api/games → 201 Created, a Location header pointing to the new game, and the game itself. */
    @PostMapping
    public ResponseEntity<GameDto> create() {
        GameDto game = service.create();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(game.id())
                .toUri();
        return ResponseEntity.created(location).body(game);
    }

    @GetMapping("/{id}")
    public GameDto get(@PathVariable UUID id) {
        return service.get(id);
    }

    /** GET /api/games/{id}/deputies?faction=ROYAL&amp;sort=support (both parameters optional). */
    @GetMapping("/{id}/deputies")
    public List<DeputyDto> deputies(@PathVariable UUID id,
                                    @RequestParam(required = false) Faction faction,
                                    @RequestParam(defaultValue = "ID") DeputySort sort) {
        return service.deputies(id, faction, sort);
    }

    @GetMapping("/{id}/bills")
    public List<BillDto> bills(@PathVariable UUID id) {
        return service.availableBills(id);
    }

    /** PUT, because it sets the value of a single "slot" of the game: the selected bill. */
    @PutMapping("/{id}/selected-bill")
    public BillSelectionDto selectBill(@PathVariable UUID id, @RequestBody SelectBillRequest request) {
        return service.selectBill(id, request.billId());
    }

    /** {@code @RequestBody}: Jackson turns the JSON body into the record. */
    @PostMapping("/{id}/bribes")
    public SupportChangeDto bribe(@PathVariable UUID id, @RequestBody BribeRequest request) {
        return service.bribe(id, request.deputyId(), request.amount());
    }

    @PostMapping("/{id}/votes")
    public VoteResponseDto vote(@PathVariable UUID id) {
        return service.vote(id);
    }

    @GetMapping("/{id}/history")
    public List<SejmResultDto> history(@PathVariable UUID id) {
        return service.history(id);
    }
}
