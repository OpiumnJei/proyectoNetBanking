package proyectoNetBanking.domain.usuarios;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.domain.prestamos.PrestamoRepository;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaRepository;
import proyectoNetBanking.infra.errors.DuplicatedItemsException;
import proyectoNetBanking.infra.errors.TypeUserNotFoundException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)//Permite usar Mockito en las pruebas y asegura que las dependencias simuladas funcionen correctamente
class UsuarioServiceTest {
    //Stubbing es la modificacion del comportamiento de un metodo
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

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private TarjetaRepository tarjetaRepository;


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
        Mockito.verify(cuentaRepository).save(cuentaCaptor.capture()); //captura los datos de la cuenta recien creada
        CuentaAhorro cuentaGuardada = cuentaCaptor.getValue();//almacena los datos extraidos por el cuentaCaptor

        // verifica que los valores tantos de usuario y la cuenta de ahorro creada sean iguales
        assertEquals(usuarioCreado.getId(), cuentaGuardada.getUsuario().getId());
        System.out.println(usuarioCreado.getId() + " y " + cuentaGuardada.getUsuario().getId()); //
        assertEquals(usuarioCreado.getNombre(), cuentaGuardada.getUsuario().getNombre());
        assertEquals(usuarioCreado.getApellido(), cuentaGuardada.getUsuario().getApellido());
        assertEquals(usuarioCreado.getCorreo(), cuentaGuardada.getUsuario().getCorreo());
        assertEquals(usuarioCreado.getPassword(), cuentaGuardada.getUsuario().getPassword());
        assertEquals(usuarioCreado.getCedula(), cuentaGuardada.getUsuario().getCedula());
        assertTrue(cuentaGuardada.isEsPrincipal());
        System.out.println(cuentaGuardada.isEsPrincipal());
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
    @DisplayName("Cuando se intenta registrar un usuario con un nuevoCorreo ya existente en el sistema, se lanza una exception del tipo DuplicatedItemsException.")
    void deberiaLanzarExcepcioCuandoCorreoYaExiste() {

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

        //emular que el nuevoCorreo ya esta registrado
        Mockito.when(usuarioRepository.existsByCorreo(datosUsuarioDTO.correo())).thenReturn(true);

        //verificacion y asercion
        DuplicatedItemsException exception = Assertions.assertThrows(
                DuplicatedItemsException.class, //excepcion a ser capturada
                () -> usuarioService.crearCliente(datosUsuarioDTO) //dentro de usuario service y el metodo crearCliente
        );

        Assertions.assertEquals("El nuevoCorreo ya se encuentra registrado en el sistema.", exception.getMessage());

        System.out.print(exception.getMessage());
        //comprobar que no se hayan guardado los datos del usuario si se lanza y captura la excepcionn
        Mockito.verify(usuarioRepository, Mockito.never()).save(Mockito.any(Usuario.class)); //mockito nunca guardo ningun objeto de tipo usuario
    }

    @Test
    @DisplayName("Debe retornar una excepcion si el tipo de usuario introducido no existe")
    void deberiaRetornarUnaExcepcionSiTipoUsuarioNoExiste() {

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

        Assertions.assertEquals("Tipo de usuario no encontrado", exception.getMessage());
        System.out.println(exception.getMessage());

        Mockito.verify(usuarioRepository, Mockito.never()).save(Mockito.any(Usuario.class));
    }

    @Test
    @DisplayName("Debe inactivar un usuario sin productos pendientes correctamente")
    void deberiaInactivarUsuarioSinProductosPendientes() {
        // Datos de prueba
        Long usuarioId = 1L;
        Usuario usuario = crearUsuarioConProductos(1l, true);

        // Mockear comportamiento de repositorios
        Mockito.when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        //El mock de save devuelva el mismo objeto Usuario que se pasó como argumento en el metodo de arriba, lo que asegura que el objeto mockeado se haya guardado correctamente
        Mockito.when(usuarioRepository.save(Mockito.any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Save devuelve el mismo usuario
        Mockito.when(cuentaRepository.findByUsuarioId(usuarioId)).thenReturn(Collections.emptyList());
        Mockito.when(tarjetaRepository.findByUsuarioId(usuarioId)).thenReturn(Collections.emptyList());
        Mockito.when(prestamoRepository.findByUsuarioId(usuarioId)).thenReturn(Collections.emptyList());

        // Ejecutar el metodo a probar
        usuarioService.inactivarUsuario(usuarioId);

        /*
        codigo que verifica si un usuario se encuentra inactivo
            IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> usuarioService.inactivarUsuario(usuario.getId())
        );

        Assertions.assertEquals("El usuario ya se encuentra inactivo.", exception.getMessage());
        System.out.println(exception.getMessage());
        */

        // Verificar que el usuario este inactivo
        Assertions.assertFalse(usuario.isActivo(), "El usuario debería estar inactivo."); //mensaje a mostrar en caso de que la asercion no se cumpla
        System.out.println(usuario.isActivo()); //verificar el estado del usuario

        // Verificar interacciones con los repositorios
        Mockito.verify(usuarioRepository).save(usuario);
        Mockito.verify(cuentaRepository).findByUsuarioId(usuarioId);
        Mockito.verify(tarjetaRepository).findByUsuarioId(usuarioId);
        Mockito.verify(prestamoRepository).findByUsuarioId(usuarioId);
    }

    @Test
    @DisplayName("Al intentar inactivar una tarjeta de credito con un monto por pagar deberia retornar una excepcion.")
    void deberiaLanzarUnaExcepcionAlInactivarUsuarioConTarjetaPorPagar() {
        // Datos de prueba
        Long usuarioId = 1L;
        Usuario usuario = crearUsuarioConProductos(1l, true);

        EstadoProducto estadoActivo = new EstadoProducto("Activo");

        // Mockear comportamiento de repositorios
        Mockito.when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        // Mockear cuentas de ahorro sin saldo pendiente
        Mockito.when(cuentaRepository.findByUsuarioId(usuarioId)).thenReturn(Collections.emptyList());

        // Mockear tarjetas de crédito con saldo pendiente
        List<TarjetaCredito> tarjetasConSaldo = List.of(
                new TarjetaCredito("abc102002", BigDecimal.valueOf(5000), BigDecimal.valueOf(2500), BigDecimal.valueOf(2500), estadoActivo, usuario), // Tarjeta con  2500 de sald opendiente
                new TarjetaCredito("abc102320", BigDecimal.valueOf(4000), BigDecimal.valueOf(4000), BigDecimal.ZERO, estadoActivo, usuario)          // Tarjeta sin saldo pendiente
        );

        Mockito.when(tarjetaRepository.findByUsuarioId(usuarioId)).thenReturn(tarjetasConSaldo);

        //capturar la excepcion generada por el mock
        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> usuarioService.inactivarUsuario(usuario.getId())
        );

        //verificar que el mensaje esperado, sea el mismo retornado en la excepcion
        Assertions.assertEquals("El usuario tiene tarjetas de crédito con saldo pendiente.", exception.getMessage());
        System.out.println("Excepcion lanzada: " + exception.getMessage());

        // Verificar interacciones
        Mockito.verify(tarjetaRepository).findByUsuarioId(usuarioId);
        Mockito.verify(usuarioRepository, Mockito.never()).save(Mockito.any(Usuario.class));
    }

    @Test
    @DisplayName("Al intentar inactivar un prestamo con un monto por pagar deberia retornar una excepcion.")
    void deberiaLanzarExcepcionAlInacitvarUsuarioPrestamoPorPagar(){

        Long usuarioId = 1L;
        Usuario usuario = crearUsuarioConProductos(usuarioId, true);

        EstadoProducto estadoActivo = new EstadoProducto("Activo");


        // Mockear comportamiento de repositorios
        Mockito.when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        // Mockear cuentas de ahorro sin saldo pendiente
        Mockito.when(cuentaRepository.findByUsuarioId(usuario.getId())).thenReturn(Collections.emptyList());

        // Mockear prestamos con saldo pendiente
        List<Prestamo> prestamoConSaldo = List.of(
                new Prestamo("abc102020", BigDecimal.valueOf(20000), BigDecimal.valueOf(15000), BigDecimal.valueOf(5000), estadoActivo, usuario)// Préstamo 15000 de saldo pendiente
//                new Prestamo("abc102021", BigDecimal.valueOf(20000), BigDecimal.ZERO, BigDecimal.valueOf(20000), estadoActivo, usuario) // Préstamo sin saldo pendiente
        );

        Mockito.when(prestamoRepository.findByUsuarioId(usuario.getId())).thenReturn(prestamoConSaldo); //retornar los prestamos que tenga el usario

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> usuarioService.inactivarUsuario(usuario.getId())
        );

        //verificar que el mensaje esperado, sea el mismo retornado en la excepcion
        Assertions.assertEquals("El usuario tiene préstamos con saldo pendiente.", exception.getMessage());
        System.out.println("Excepcion lanzada: " + exception.getMessage());


        //comprobar que el usuario aun permanece activo
        Assertions.assertTrue(usuario.isActivo());
        System.out.println("Estado del usuario: " + usuario.isActivo());

        // Verificar interacciones
        Mockito.verify(prestamoRepository).findByUsuarioId(usuarioId);
        Mockito.verify(usuarioRepository, Mockito.never()).save(Mockito.any(Usuario.class));
    }

    //metodo auxiliar para inicializar un usuario con productos
    private Usuario crearUsuarioConProductos(Long usuarioId, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setActivo(activo);
        return usuario;
    }
}
