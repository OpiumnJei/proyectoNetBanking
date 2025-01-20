package proyectoNetBanking.infra.auditConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//su propósito principal es habilitar la auditoría mediante la anotación @EnableJpaAuditing.
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl") // Refiere al bean registrado manualmente
public class JpaConfig {

    public AuditorAware<String> auditorAwareImpl() {
        return new AuditorAwareImpl();
    }
}