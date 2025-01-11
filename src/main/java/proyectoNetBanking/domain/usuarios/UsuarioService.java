package proyectoNetBanking.domain.usuarios;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
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

    //inyectamos el generador de ids
    @Autowired
    private GeneradorId generadorId;

    @Autowired
    private CuentaAhorroRepository cuentaRepository;

    //crear un cliente
    public void crearCliente(DatosUsuariosDTO datosUsuariosDTO) {

        //se valida que tanto la cedula y el correo no esten previamente registrados en la bd
        if (usuarioRepository.existsByCedula(datosUsuariosDTO.cedula())) {
            throw new DuplicatedItemsException("La cédula ya se encuentra registrada en el sistema.");
        }
        if (usuarioRepository.existsByCorreo(datosUsuariosDTO.correo())) {
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
    }


    //asignar cuenta de ahorro principal al usuario
    public void asignarCuentaPrincipal(Usuario usuario) {
        //crear instancia de cuenta de ahorro
        CuentaAhorro cuentaUsuarioPrincipal = new CuentaAhorro();
        cuentaUsuarioPrincipal.setIdProducto(generarIdUnicoProducto()); // se coloca el id
        cuentaUsuarioPrincipal.setUsuario(usuario); //aunque se coloque la entidad usuario completa, hibernate solamente toma el id
        cuentaUsuarioPrincipal.setEsPrincipal(true);
        cuentaUsuarioPrincipal.setSaldoDisponible(usuario.getMontoInicial());

        //guardar la cuenta
        cuentaRepository.save(cuentaUsuarioPrincipal);
    }

    //generar id del producto y verificar que no exista un producto con ese id
    public String generarIdUnicoProducto() {
        String idGenerado;
        do {
            idGenerado = generadorId.generarIdProducto();
        }
        while (cuentaRepository.existsByIdProducto(idGenerado)); //validar que el id del producto  generado no exista en la bd
        return idGenerado;
    }

}
