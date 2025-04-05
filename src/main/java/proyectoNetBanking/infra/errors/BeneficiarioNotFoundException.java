package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BeneficiarioNotFoundException extends RuntimeException {
    public BeneficiarioNotFoundException(String message) {
        super(message);
    }
}
