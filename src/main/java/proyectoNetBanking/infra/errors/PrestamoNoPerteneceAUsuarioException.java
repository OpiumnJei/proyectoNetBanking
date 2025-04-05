package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // cuando un usuario intente acceder a un prestamo que no le pertenece
public class PrestamoNoPerteneceAUsuarioException extends RuntimeException {
    public PrestamoNoPerteneceAUsuarioException(String message) {
        super(message);
    }
}
