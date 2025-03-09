package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TarjetaSinSaldoPendienteException extends RuntimeException {
    public TarjetaSinSaldoPendienteException(String message) {
        super(message);
    }
}
