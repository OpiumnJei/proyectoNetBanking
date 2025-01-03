package proyectoNetBanking.infra.AuditConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration
public class AuditorConfig {

    @Bean //este metodo pasa a ser gestionado por spring
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}
