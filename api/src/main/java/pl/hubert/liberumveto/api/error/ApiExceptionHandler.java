package pl.hubert.liberumveto.api.error;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.hubert.liberumveto.api.service.GameNotFoundException;
import pl.hubert.liberumveto.domain.exception.ActionNotAllowedException;
import pl.hubert.liberumveto.domain.exception.InsufficientBudgetException;
import pl.hubert.liberumveto.domain.exception.InvalidChoiceException;

/**
 * One place that turns exceptions into HTTP responses, for every controller
 * ({@code @RestControllerAdvice} = advice applied to all {@code @RestController}s).
 *
 * <p>Every error uses the same format, {@link ProblemDetail} (RFC 9457, {@code application/problem+json}):
 * {@code type}, {@code title} (stable, in English: for client code), {@code status},
 * {@code detail} (in Polish: shown to the player) and {@code instance} (the request path).
 *
 * <p>Extending {@link ResponseEntityExceptionHandler} gives ProblemDetail responses for Spring MVC's
 * own errors too (malformed JSON, wrong parameter types, unsupported HTTP methods, ...).
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(GameNotFoundException.class)
    ProblemDetail handleNotFound(GameNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Game not found", e.getMessage());
    }

    /** Invalid input from the player: unknown deputy, a bill not on the list, no bill selected yet. */
    @ExceptionHandler(InvalidChoiceException.class)
    ProblemDetail handleInvalidChoice(InvalidChoiceException e) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid choice", e.getMessage());
    }

    /** 422: the request is well-formed, but the game rules do not allow it. */
    @ExceptionHandler(InsufficientBudgetException.class)
    ProblemDetail handleInsufficientBudget(InsufficientBudgetException e) {
        ProblemDetail problem = problem(HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient budget", e.getMessage());
        problem.setProperty("budget", e.budget());
        problem.setProperty("amount", e.amount());
        return problem;
    }

    /** 409: the action does not fit the current state of the game (already over, bill already chosen). */
    @ExceptionHandler(ActionNotAllowedException.class)
    ProblemDetail handleActionNotAllowed(ActionNotAllowedException e) {
        return problem(HttpStatus.CONFLICT, "Action not allowed now", e.getMessage());
    }

    /** Catch-all: log the details for us, but do not leak a stack trace to the client. */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "Coś poszło nie tak po stronie serwera");
    }

    /**
     * {@code @Valid} failed. We override the inherited handler to add a field → message map,
     * e.g. {@code "errors": {"amount": "Kwota łapówki musi być dodatnia"}}.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "Popraw dane w żądaniu");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
