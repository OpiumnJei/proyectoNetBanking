package proyectoNetBanking.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.dto.authentication.LoginDTO;
import proyectoNetBanking.dto.authentication.ResponseTokenDTO;
import proyectoNetBanking.infra.authentication.TokenService;
import proyectoNetBanking.repository.UsuarioRepository;

//@RestController Combina @Controller y @ResponseBody.
//Indica que esta clase maneja solicitudes HTTP y que sus métodos retornan directamente datos (por ejemplo, JSON).
@RestController
@RequestMapping("netbanking/usuarios") //Define el prefijo de URL para todos los endpoints de esta clase.
public class AuthenticationController { //Gestiona el inicio de sesión en tu sistema netbanking usando JWT.

    @Autowired
    private AuthenticationManager authenticationManager; //encargado de autenticar a los usuarios

    @Autowired
    private TokenService tokenService; //encargado de generar y validar tokens JWT.

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<ResponseTokenDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        // Crear token de autenticación
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginDTO.cedula(), loginDTO.password());

        // Si las credenciales son válidas, Spring devuelve un objeto Authentication con los datos del usuario autenticado.
        Authentication authentication = authenticationManager.authenticate(authToken);

        // Una vez autenticado, busca en la base de datos los datos completos del usuario, para poder incluir más información en el JWT o en la respuesta.
        Usuario usuario = usuarioRepository.findByCedula(loginDTO.cedula())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Generar token JWT
        String token = tokenService.generarToken(usuario);

        //se retorna el token y el tipo del usuario
        return ResponseEntity.ok(new ResponseTokenDTO(token, usuario.getTipoUsuario()
                .getNombreTipoUsuario()));
    }
}
