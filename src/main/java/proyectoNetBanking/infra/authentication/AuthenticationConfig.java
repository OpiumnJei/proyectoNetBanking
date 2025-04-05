package proyectoNetBanking.infra.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

/**
 * Esta clase forma parte de la configuración de seguridad en Spring Boot y se encarga de proporcionar un AuthenticationManager,
 * que es el componente responsable de autenticar a los usuarios.
 */

@Configuration
//Spring escaneará esta clase para registrar beans que estarán disponibles en el contexto de la aplicación.
public class AuthenticationConfig {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * @param configuration es un componente de Spring Security que contiene la configuración de autenticación.
     *
     *                      El metodo recibe una instancia de AuthenticationConfiguration
     *                      que es un componente de Spring Security que contiene la configuración de autenticación.
     */
    @Bean //es un Bean, por lo tanto, Spring lo ejecutará y registrará el resultado como un bean en el contexto.
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        //Devuelve el AuthenticationManager preconfigurado por Spring Boot basado en la implementación de
        //UserDetailsService en AuthenticationService
        return configuration.getAuthenticationManager();
    }

}
