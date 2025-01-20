package proyectoNetBanking.domain.usuarios;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.infra.errors.DuplicatedItemsException;
import proyectoNetBanking.infra.errors.TypeUserNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)//Permite usar Mockito en las pruebas y asegura que las dependencias simuladas funcionen correctamente
class UsuarioServiceTest {

    //Un Mock es un objeto simulado
    //con @Mock se simulan las dependencias del servicio que queremos testar
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private CuentaAhorroRepository cuentaRepository;

    @Mock
    private GeneradorId generadorId;

    @Mock
    private EstadoProductoRepository estadoProductoRepository;

    @Captor //es una anotación de Mockito que simplifica la creación de un ArgumentCaptor
    // se utiliza para capturar y analizar los valores reales que se pasan a un mock en tiempo de ejecución
    ArgumentCaptor<CuentaAhorro> cuentaCaptor;

    @InjectMocks //Inyecta las dependencias simuladas en el objeto de prueba (el servicio)
    private UsuarioService usuarioService;

    @Test //Marca un metodo como un caso de prueba.
    @DisplayName("Debería crear un cliente exitosamente y su cuenta ahorro principal, cuando los datos son válidos y no existen duplicados.")
    void crearClienteYCuentaDeAhorroPrincipal() {
        // Arrange: Preparar datos de entrada
        DatosUsuarioDTO datosUsuario = new DatosUsuarioDTO(
                "Juan",
                "Pérez",
                "123456789",
                "juan.perez@example.com",
                "password123",
                2L,
                BigDecimal.valueOf(1000)
        );

        TipoUsuario tipoUsuario = new TipoUsuario(
                2L,
                "Cliente",
                "Usuario que puede acceder a las funcionalidades de un cliente"
        );

        EstadoProducto estadoActivo = new EstadoProducto("Activo");

        Usuario usuarioCreado = new Usuario();
        usuarioCreado.setId(1L);
        usuarioCreado.setNombre(datosUsuario.nombre());
        usuarioCreado.setApellido(datosUsuario.apellido());
        usuarioCreado.setCorreo(datosUsuario.correo());
        usuarioCreado.setPassword(datosUsuario.password());
        usuarioCreado.setCedula(datosUsuario.cedula());
        usuarioCreado.setMontoInicial(datosUsuario.montoInicial());
        usuarioCreado.setTipoUsuario(tipoUsuario);
        usuarioCreado.setActivo(true);

        // Mocks de comportamiento
        Mockito.when(usuarioRepository.existsByCedula(usuarioCreado.getCedula())).thenReturn(false);//deberia retorna false, ya que la cedula no se encuentra duplicada
        Mockito.when(usuarioRepository.existsByCorreo(usuarioCreado.getCorreo())).thenReturn(false);
        Mockito.when(tipoUsuarioRepository.findById(tipoUsuario.getId())).thenReturn(Optional.of(tipoUsuario));
        Mockito.when(usuarioRepository.save(Mockito.any(Usuario.class))).thenReturn(usuarioCreado);
        Mockito.when(generadorId.generarIdUnicoProducto(Mockito.any())).thenReturn("ABC123456");
        Mockito.when(estadoProductoRepository.findByNombreEstadoIgnoreCase("ACTIVO"))
                .thenReturn(Optional.of(estadoActivo));

        // Act: Ejecutar el Metodo
        usuarioService.crearCliente(datosUsuario);

        // Assert: Capturar y validar la cuenta guardada
        Mockito.verify(cuentaRepository).save(cuentaCaptor.capture());
        CuentaAhorro cuentaGuardada = cuentaCaptor.getValue();

        // verifica que los atributos específicos coincidan.
        assertEquals(usuarioCreado.getId(), cuentaGuardada.getUsuario().getId());
        assertEquals(usuarioCreado.getNombre(), cuentaGuardada.getUsuario().getNombre());
        assertEquals(usuarioCreado.getApellido(), cuentaGuardada.getUsuario().getApellido());
        assertEquals(usuarioCreado.getCorreo(), cuentaGuardada.getUsuario().getCorreo());
        assertEquals(usuarioCreado.getPassword(), cuentaGuardada.getUsuario().getPassword());
        assertEquals(usuarioCreado.getCedula(), cuentaGuardada.getUsuario().getCedula());
        assertTrue(cuentaGuardada.isEsPrincipal());
        assertEquals(BigDecimal.valueOf(1000), cuentaGuardada.getSaldoDisponible());
        assertEquals("Fondo de emergencia", cuentaGuardada.getProposito());
        assertEquals("ABC123456", cuentaGuardada.getIdProducto());
    }

    @Test
    @DisplayName("Cuando se intenta registrar un usuario con una cédula ya existente en el sistema, se lanza una excepción del tipo DuplicatedItemsException.")
    void deberiaLanzarExceptionCuandoCedulaYaExiste() {
        // Configuración del entorno de prueba
        String cedulaExistente = "12345678901";

        //datos de prueba
        DatosUsuarioDTO datosUsuario = new DatosUsuarioDTO(
                "John",
                "Doe",
                cedulaExistente,
                "johndoe@example.com",
                "password123",
                2L, // tipoUsuarioId
                BigDecimal.valueOf(1000) // montoInicial
        );

        // Simular que la cédula ya está registrada
        Mockito.when(usuarioRepository.existsByCedula(cedulaExistente)).thenReturn(true);

        // Verificación y aserción
        DuplicatedItemsException exception = Assertions.assertThrows(  //assertThrows verifica que se lance una excepción específica durante la ejecución de un bloque de código.
                DuplicatedItemsException.class, //tipo de excepcion que se espera sea lanzada
                () -> usuarioService.crearCliente(datosUsuario) //se espera que dentro de usuarioService se lanze una exception en el metodo crearCliente
        );

        //se comprueba que el string retornado en la excepcion, sea igual al string esperado
        Assertions.assertEquals("La cédula ya se encuentra registrada en el sistema.", exception.getMessage());

        System.out.println(exception.getMessage());
        /*
        Mockito.verify: Sirve para verificar si un metodo en un mock (objeto simulado) fue invocado.
          Parámetros del metodo:
            usuarioRepository: Es el mock que estamos verificando.
            Mockito.never(): Especifica que el metodo no debe haber sido invocado en ningún momento.
            .save(Mockito.any(Usuario.class)): estamos verificando que el metodo save no fue llamado con ningún objeto de tipo Usuario.
        */

        // Verificar que no se intentó guardar el usuario
        Mockito.verify(usuarioRepository, Mockito.never()).save(Mockito.any(Usuario.class));
    }

    @Test
    @DisplayName("Cuando se intenta registrar un usuario con un correo ya existente en el sistema, se lanza una exception del tipo DuplicatedItemsException.")
    void deberiaLanzarExcepcioCuandoCorreoYaExiste(){

        String correoExistente = "jerlinson@gmail.com";

        DatosUsuarioDTO datosUsuarioDTO = new DatosUsuarioDTO(
                "John",
                "Doe",
                "12020202",
                correoExistente,
                "password123",
                2L, // tipoUsuarioId
                BigDecimal.valueOf(1000) // montoInicial
        );

        //emular que el correo ya esta registrado
        Mockito.when(usuarioRepository.existsByCorreo(datosUsuarioDTO.correo())).thenReturn(true);

        //verificacion y asercion
        DuplicatedItemsException exception = Assertions.assertThrows(
                DuplicatedItemsException.class, //excepcion a ser capturada
                () -> usuarioService.crearCliente(datosUsuarioDTO) //dentro de usuario service y el metodo crearCliente
        );

        Assertions.assertEquals("El correo ya se encuentra registrado en el sistema.", exception.getMessage());

        System.out.print(exception.getMessage());
        //comprobar que no se hayan guardado los datos del usuario si se lanza y captura la excepcionn
        Mockito.verify(usuarioRepository, Mockito.never()).save(Mockito.any(Usuario.class)); //mockito nunca guardo ningun objeto de tipo usuario
    }

    @Test
    @DisplayName("Debe retornar una excepcion si el tipo de usuario introducido no existe")
    void deberiaRetornarUnaExcepcionSiTipoUsuarioNoExiste(){

        Long tipoUsuario = 3L;

        DatosUsuarioDTO datosUsuario = new DatosUsuarioDTO(
                "John",
                "Doe",
                "12020202",
                "jerlin@gmail.com",
                "password123",
                tipoUsuario, // tipoUsuarioId
                BigDecimal.valueOf(1000) // montoInicial
        );

        //simular que el tipo de usuario no se encuentra en el repositorio
        Mockito.when(tipoUsuarioRepository.findById(datosUsuario.tipoUsuarioId())).thenReturn(Optional.empty());

        TypeUserNotFoundException exception = Assertions.assertThrows(
                TypeUserNotFoundException.class,
                () -> usuarioService.crearCliente(datosUsuario)
        );

        Assertions.assertEquals("Tipo de usuario no encontrado",exception.getMessage());
        System.out.println(exception.getMessage());

        Mockito.verify(usuarioRepository, Mockito.never()).save(Mockito.any(Usuario.class));
    }
}