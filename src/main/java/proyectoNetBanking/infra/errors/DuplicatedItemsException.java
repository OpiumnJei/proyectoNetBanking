package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatedItemsException extends RuntimeException {
    public DuplicatedItemsException(String message) {
        super(message);
    }
}
