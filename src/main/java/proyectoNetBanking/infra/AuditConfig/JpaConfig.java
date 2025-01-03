package proyectoNetBanking.infra.AuditConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//clase principal encargada de manejar la auditoria con jpa
@Configuration
@EnableJpaAuditing //habilitar la auditoria para jpa
public class JpaConfig {
}
