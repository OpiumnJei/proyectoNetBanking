package proyectoNetBanking.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import proyectoNetBanking.infra.errors.Tratar401;
import proyectoNetBanking.infra.errors.Tratar403;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity // para habilitar el funcionamiento de @PreAuthorize en los controllers
public class SecurityConfig {

    @Autowired
    private SecurityFilterJWT securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
         http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "netbanking/usuarios/login").permitAll() //permitir que cualquier usuario sin importar el rol se autentique
                        .requestMatchers(HttpMethod.GET, "netbanking/admin/indicadores/**").hasRole("ADMINISTRADOR") //para acceder a los endpoints con el prefijo indicado se necesita ser admin
                        .requestMatchers(HttpMethod.POST, "netbanking/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "netbanking/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "netbanking/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "netbanking/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "netbanking/cliente/pagos/**").hasRole("CLIENTE")

                        .anyRequest().authenticated()
                )
                 .exceptionHandling(exceptions -> exceptions
                         .authenticationEntryPoint(tratar401()) // Usa el bean tratar401()
                         .accessDeniedHandler(tratar403()) // Usa el bean tratar403()
                 )
                 //agregar el filtro de seguridad externo(securityFilter) antes que la configuracion de seguridad de esta clase
                 .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    //Errores de autenticacion y autorizacion
    @Bean
    public AuthenticationEntryPoint tratar401() {
        return new Tratar401();
    }

    //Errores de autorizacion
    @Bean
    public AccessDeniedHandler tratar403() {
        return new Tratar403();
    }
}

