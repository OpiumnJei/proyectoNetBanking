package proyectoNetBanking.infra.AuditConfig;

import org.springframework.data.domain.AuditorAware;
import java.util.Optional;

//clase utilizada para que Spring Boot pueda registrar al usuario que efectuo la transaccion
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Devuelve el nombre del usuario autenticado (puedes integrarlo con Spring Security)
        return Optional.of("admin");
    }
}
