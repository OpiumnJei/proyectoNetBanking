package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class BeneficiarioAlreadyExistsException extends RuntimeException {
    public BeneficiarioAlreadyExistsException(String message) {
        super(message);
    }
}
