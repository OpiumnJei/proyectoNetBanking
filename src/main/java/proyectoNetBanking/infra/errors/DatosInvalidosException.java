package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)// Codigo/estado que debe retornar la excepcion
public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String message) {
        super(message);
    }
}
