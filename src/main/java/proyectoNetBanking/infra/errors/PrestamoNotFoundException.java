package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PrestamoNotFoundException extends RuntimeException {
    public PrestamoNotFoundException(String message) {
        super(message);
    }
}
