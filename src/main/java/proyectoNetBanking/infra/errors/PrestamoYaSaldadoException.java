package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // se intenta pagar un registro ya pagado
public class PrestamoYaSaldadoException extends RuntimeException {
    public PrestamoYaSaldadoException(String message) {
        super(message);
    }
}
