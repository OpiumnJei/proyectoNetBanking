package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // el usuario intenta usar una cuenta que no es suya
public class CuentaNoPerteneceAUsuarioException extends RuntimeException {
    public CuentaNoPerteneceAUsuarioException(String message) {
        super(message);
    }
}
