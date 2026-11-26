package pl.hubert.liberumveto.api.controller;

import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.hubert.liberumveto.api.dto.GameDto;
import pl.hubert.liberumveto.api.service.GameService;

/**
 * The HTTP layer: maps URLs to service calls and domain objects to DTOs. No game logic here.
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
        UUID id = service.create();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(GameDto.from(id, service.get(id)));
    }

    /** GET /api/games/{id} → 200 with the game state, or 404. */
    @GetMapping("/{id}")
    public GameDto get(@PathVariable UUID id) {
        return GameDto.from(id, service.get(id));
    }
}
