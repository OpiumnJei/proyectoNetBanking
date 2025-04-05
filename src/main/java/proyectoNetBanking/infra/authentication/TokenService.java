package proyectoNetBanking.infra.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.usuarios.Usuario;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// servicio encargado de la generacion de tokens
@Service
public class TokenService {

    @Value("${api.security-token.secret}")
    private String apiSecret;

    public String generarToken(Usuario usuario) {
        String tokenJWT;
        try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            tokenJWT = JWT.create()
                    .withIssuer("netbanking-api")  //quien emite el token
                    .withSubject(usuario.getCedula()) //a quien va dirido el token
                    .withClaim("id", usuario.getId()) //retorna el id del usuario
                    .withClaim("role", usuario.getTipoUsuario().getNombreTipoUsuario()) //retorna el rol del usuario
                    .withExpiresAt(generarTiempoExpiracion())
                    .sign(algorithm); // se firma el token generado
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error al generar token JWT", exception);
        }

        return tokenJWT;
    }

    /**
     * @param token enviado desde SecurityFilterJWT.
     * este metodo se usa para validar el mismo.
     *
     * */
    public String getSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            return JWT.require(algorithm)
                    .withIssuer("netbanking-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido o expirado");
        }
    }

    //genera el tiempo de expiracion del token
    private Instant generarTiempoExpiracion() {
        //añade 2 horas al tiempo actual y retorna el resultado como un objeto de tipo Instant
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-04:00"));
    }
}
