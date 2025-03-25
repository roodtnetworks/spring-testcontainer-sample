package net.roodt.springtestcontainers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountRepository accountRepository;

    private static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> account(@PathVariable("id") UUID accountId) {
        // This is a straight forward use of optional to inline an if/else type block

        return Optional.ofNullable(accountRepository.findByAccountId(accountId))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<Account> getAccountImperative(UUID accountId) {
        Account account = accountRepository.findByAccountId(accountId);
        if(account == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(account);
        }
    }

    private ResponseEntity<Account> getAccountDeclarative(UUID accountId) {
        return Optional.ofNullable(accountRepository.findByAccountId(accountId))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Account>> accounts() {
        // trying to be less verbose, potentially at the expense of readability
        // could achieve the same with an if/else based on whether the list has elements or not.
        return Optional.ofNullable(accountRepository.findAll())
                .filter(not(List::isEmpty))
//                .filter((list) -> list.size() > 0) // arguably better - no need to create the not predicate.
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody Account account) {
        accountRepository.create(new Account(account.accountId(), account.name()));
        // Do some HATEOAS here. The repository is throwing a ResponseStatus aware exception if a conflict exists
        // Conflict exception handled by springtestcontainers.roodt.net.AccountController.handleAlreadyExists
        return ResponseEntity.created(URI.create("/accounts/" + account.accountId().toString())).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateAccount(@RequestBody Account account) {
//        return accountRepository.update(new Account(account.accountId(), account.name())) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        return Optional.of(accountRepository.update(new Account(account.accountId(), account.name())))
                .filter(success -> success)
                .map(success -> ResponseEntity.noContent().<Void>build())
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") UUID accountId) {
        return Optional.ofNullable(accountRepository.delete(accountId))
                .filter(success -> success)
                .map(success -> ResponseEntity.noContent().<Void>build())
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({IllegalArgumentException.class, EmptyResultDataAccessException.class, InvalidFormatException.class})
    public void handleNotFound(Exception ex) {
        logger.error("Oh dear, we tried but couldn't find your data", ex);
        // return empty 404
    }

    /**
     * Maps DataIntegrityViolationException to a 409 Conflict HTTP status code.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ DataIntegrityViolationException.class })
    public void handleAlreadyExists(Exception ex) {
        logger.error("Oh dear, the data already exists", ex);
        // return empty 409
    }
}
