package proyectoNetBanking.infra.auditConfig;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

//clase utilizada para la auditoria del sistema
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Retorna el usuario actual o un valor predeterminado (e.g., "Sistema").
        // Aquí puedes integrar con Spring Security si lo usas.
        return Optional.of("Jerlinson"); // Cambiar según contexto
    }
}
