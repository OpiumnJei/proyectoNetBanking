package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) //Un cliente intenta realizar una operación que viola una regla de negocio.
public class CreditoNoDisponibleException extends RuntimeException {
    public CreditoNoDisponibleException(String message) {
        super(message);
    }
}
