package proyectoNetBanking.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//CONFIGURACION PARA LA ENCRIPTACION DE LAS CONTRASENIAS
//hay que crear esta configuracion manualmente ya que spring no la genera automaticamente
@Configuration
public class BcryptBeanConfig {

    @Bean // un bean representa una pieza fundamental de tu lógica de negocio o configuración.
    public PasswordEncoder passwordEncoder(){
       return new BCryptPasswordEncoder();
    }
}
