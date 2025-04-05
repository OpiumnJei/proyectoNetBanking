package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // cuando se intenta pagar un registro ya pagado
public class TarjetaYaSaldadaException extends RuntimeException {
    public TarjetaYaSaldadaException(String message) {
        super(message);
    }
}
