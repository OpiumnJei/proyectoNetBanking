package proyectoNetBanking.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import proyectoNetBanking.infra.authentication.AuthenticationService;
import proyectoNetBanking.infra.authentication.TokenService;
import proyectoNetBanking.repository.UsuarioRepository;

import java.io.IOException;
import java.util.Collection;

@Component
public class SecurityFilterJWT extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Este metodo se ejecuta cada vez que llega una solicitud HTTP y se encarga de verificar si hay un JWT válido,
     * y si lo hay, autentica al usuario dentro del contexto de Spring Security.
     *
     * @param request: Representa la solicitud HTTP del cliente.
     * @param response: Representa la respuesta HTTP que se devolverá al cliente.
     * @param filterChain: Es la cadena de filtros de Spring. Sirve para continuar con la ejecución del siguiente filtro si todo esta bien.
     * */
    @Override // sobrescribe el metodo doFilterInternal de la clase padre OncePerRequestFilter.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = recuperarToken(request);

        if (token != null) {
            try {
                // obtener datos del usuario a partir del token
                String cedula = tokenService.getSubject(token);

                // para obtener la información del usuario (UserDetails) a partir de la cédula (o username).
                UserDetails userDetails = authenticationService.loadUserByUsername(cedula);

                // Crear autenticación
                var authentication = new UsernamePasswordAuthenticationToken( //UsernamePassword.. objeto de autenticación estándar en Spring Security.
                        userDetails, // usuario
                        null, //null como contraseña, ya que no se esta usando una autenticacion stateful
                        userDetails.getAuthorities() // se obtiene el rol/es del usuario
                );

                // Para depuración
                Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                System.out.println("DEBUG JWT: Autoridades para " + userDetails.getUsername() + ":");
                for (GrantedAuthority auth : authorities) {
                    System.out.println("  - " + auth.getAuthority());
                }

                /*
                 * Se registra la autenticación en el contexto de seguridad de Spring,
                 * lo que permite que más adelante en el flujo (por ejemplo, en un controlador)
                 * se pueda acceder al usuario autenticado con @AuthenticationPrincipal.
                 * */
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (RuntimeException ex) {
                //Si ocurre algún error (por ejemplo, el token es inválido o ha expirado), se devuelve un error HTTP 401 UNAUTHORIZED.
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
            }
        }

        //pasar al siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
/**
 * @param request
 * Representa la solicitud HTTP que contiene encabezados y otros datos de la petición que el cliente
 * hace al servidor.
 *
 * */
    private String recuperarToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization"); // se obtiene el header enviado por el usuario
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7); // retorna el token sin el prefijo "Bearer "
    }
}
