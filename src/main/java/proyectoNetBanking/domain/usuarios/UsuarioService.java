package proyectoNetBanking.domain.usuarios;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.infra.errors.DuplicatedItemsException;

@Service //marcamos la clase como un servicio/componente de spring
public class UsuarioService {

    // con autowired entablamos una relacion con el repositorio de usuarios, lo inyectamos
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    //crear un cliente
    public void crearCliente(DatosUsuariosDTO datosUsuariosDTO) {


        //se valida que tanto la cedula y el correo no esten previamente registrados en la bd
        if (usuarioRepository.existsByCedulaUsuario(datosUsuariosDTO.cedula())) {
            throw new DuplicatedItemsException("La cédula ya se encuentra registrada en el sistema.");
        }
        if (usuarioRepository.existsByCorreoUsuario(datosUsuariosDTO.correo())) {
            throw new DuplicatedItemsException("El correo ya se encuentra registrado en el sistema.");
        }

        // Recuperar el TipoUsuario desde la base de datos
        TipoUsuario tipoUsuarioEntity = tipoUsuarioRepository.findById(datosUsuariosDTO.tipoUsuarioId())
                .orElseThrow(() -> new RuntimeException("Tipo de usuario no encontrado"));


        //crear instancia de usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(datosUsuariosDTO.nombre());
        usuario.setApellido(datosUsuariosDTO.apellido());
        usuario.setCedula(datosUsuariosDTO.cedula());
        usuario.setCorreo(datosUsuariosDTO.correo());
        usuario.setPassword(passwordEncoder.encode(datosUsuariosDTO.password()));//se hashea la contrasenia
        usuario.setTipoUsuario(tipoUsuarioEntity);
        usuario.setMontoInicial(datosUsuariosDTO.montoInicial());

        usuarioRepository.save(usuario);

        //asignarle cuenta de ahorro al usuario, que se marcara como la principal
        CuentaAhorro cuentaUsuarioPrincipal = new CuentaAhorro();
    }


}
