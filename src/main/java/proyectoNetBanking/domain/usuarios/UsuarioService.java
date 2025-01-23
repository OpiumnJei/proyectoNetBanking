package proyectoNetBanking.domain.usuarios;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.domain.prestamos.PrestamoRepository;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.DuplicatedItemsException;
import proyectoNetBanking.infra.errors.TypeUserNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;
import java.util.List;

@Service //marcamos la clase como un servicio/componente de spring
public class UsuarioService {

    // con autowired entablamos una relacion con el repositorio de usuarios, lo inyectamos
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private GeneradorId generadorId; //inyectamos el generador de ids

    @Autowired
    private CuentaAhorroRepository cuentaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;


    //crear un cliente
    public void crearCliente(DatosUsuarioDTO datosUsuariosDTO) {

        //se valida que tanto la cedula y el nuevoCorreo no esten previamente registrados en la bd
        if (usuarioRepository.existsByCedula(datosUsuariosDTO.cedula())) {
            throw new DuplicatedItemsException("La cédula ya se encuentra registrada en el sistema.");
        }
        if (usuarioRepository.existsByCorreo(datosUsuariosDTO.correo())) {
            throw new DuplicatedItemsException("El nuevoCorreo ya se encuentra registrado en el sistema.");
        }

        // Recuperar el TipoUsuario desde la base de datos
        TipoUsuario tipoUsuarioEntity = tipoUsuarioRepository.findById(datosUsuariosDTO.tipoUsuarioId())
                .orElseThrow(() -> new TypeUserNotFoundException("Tipo de usuario no encontrado"));

        //crear instancia de usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(datosUsuariosDTO.nombre());
        usuario.setApellido(datosUsuariosDTO.apellido());
        usuario.setCedula(datosUsuariosDTO.cedula());
        usuario.setCorreo(datosUsuariosDTO.correo());
        usuario.setPassword(passwordEncoder.encode(datosUsuariosDTO.password()));//se hashea la contrasenia
        usuario.setTipoUsuario(tipoUsuarioEntity);
        usuario.setMontoInicial(datosUsuariosDTO.montoInicial());

        /* usuarioGuardado contiene el id generado por JPA
        * Los frameworks como JPA generan automáticamente el ID para las entidades persistidas,
        *  y este ID estará presente en el objeto retornado por save.
        * */
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        asignarCuentaPrincipal(usuarioGuardado);//colocarle cuenta principal por motivos de logica de negocio
    }

    //asignar cuenta de ahorro principal al usuario
    public void asignarCuentaPrincipal(Usuario usuario) {
        //crear instancia de cuenta de ahorro
        CuentaAhorro cuentaUsuarioPrincipal = new CuentaAhorro();
        cuentaUsuarioPrincipal.setIdProducto(generarIdUnicoProducto()); // se coloca el id
        cuentaUsuarioPrincipal.setUsuario(usuario); //aunque se coloque la entidad usuario completa, hibernate solamente toma el id
        cuentaUsuarioPrincipal.setEsPrincipal(true);
        cuentaUsuarioPrincipal.setProposito("Fondo de emergencia");
        cuentaUsuarioPrincipal.setSaldoDisponible(usuario.getMontoInicial());
        cuentaUsuarioPrincipal.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.ACTIVO.name()));

        //guardar la cuenta
        cuentaRepository.save(cuentaUsuarioPrincipal);
    }

    @Transactional // se ejecuta el codigo dentro de una transaccion
    public void inactivarUsuario(Long idUsuario) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si el usuario ya está inactivo
        if (!usuario.isActivo()) {
            throw new IllegalStateException("El usuario ya se encuentra inactivo.");
        }


        // verificar e inactivar productos que no tengan monto pendiente por pagar
        inactivarProductosUsuario(idUsuario);

        //luego de verificarse de que el usuario no tenga productos con deudas, se inactiva
        usuario.setActivo(false);
       Usuario usuarioInactivo = usuarioRepository.save(usuario);
    }

    //inactivar todos los productos
    private void inactivarProductosUsuario(Long usuario) {

        manejarCuentasDeAhorro(usuario); // Manejar cuentas de ahorro
        manejarTarjetasCredito(usuario);  // Manejar tarjetas de crédito
        manejarPrestamos(usuario);  // Manejar préstamos
    }

    //metodo encargado de manejar la eliminacion y movimiento de fondos entre cuentas de ahorro
    @Transactional
    private void manejarCuentasDeAhorro(Long usuario) {
        // Obtener las cuentas de ahorro del usuario
        List<CuentaAhorro> cuentasAhorro = cuentaRepository.findByUsuarioId(usuario);

        //almacenar la sumatoria de las cuentas de ahorro que no sean principales antes de su eliminacion
        var saldoCuentasNoPrincipales = cuentasAhorro
                .stream() // Se convierte la lista en un Stream para procesarla de manera funcional
                .filter(cuenta -> !cuenta.isEsPrincipal()) // Filtra las cuentas que NO son principales.
                .map(CuentaAhorro::getSaldoDisponible) // Obtiene el saldo disponible de cada cuenta.
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Suma todos los saldos obtenidos

        for (CuentaAhorro cuenta : cuentasAhorro) { //Para cada cuenta de ahorro
            if (!cuenta.isEsPrincipal()) {
                if (BigDecimal.ZERO.compareTo(cuenta.getSaldoDisponible()) != 0) {
                    cuenta.setSaldoDisponible(BigDecimal.ZERO); //reducir a 0 el saldo de las cuentas NO pricipales para que se refleje la transaccion entre cuentas
                }
                cuenta.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name())); // Eliminar logicamente cuentas no principal
            } else {
                // Transferir saldo acumulado a la cuenta principal
                // como ambos metodos estan referenciados por atributos BigDecimal podemos usar el metodo add
                cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().add(saldoCuentasNoPrincipales));
                cuenta.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name())); // Inactivar cuenta principal
                cuentaRepository.save(cuenta);
            }
        }
    }
    //metodo encargado de manejar la inactividad de tarjetas de credito

    @Transactional
    private void manejarTarjetasCredito(Long usuario) {
        // Obtener las tarjetas de credito del usuario
        List<TarjetaCredito> tarjetas = tarjetaRepository.findByUsuarioId(usuario);

        for (TarjetaCredito tarjeta : tarjetas) {
            if (tarjeta.getSaldoPorPagar().compareTo(BigDecimal.ZERO) > 0) {//si saldo por pagar es mayor que cero
                throw new RuntimeException("El usuario tiene tarjetas de crédito con saldo pendiente.");
            }
            tarjeta.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name()));
            tarjetaRepository.save(tarjeta);
        }
    }
    // metodo encargado de manejar la inactividad de los prestamos

    @Transactional
    private void manejarPrestamos(Long usuario) {
        // obtener los prestamos asociados al usuario
        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuario);

        for (Prestamo prestamo : prestamos) {
            if (prestamo.getMontoApagar().compareTo(BigDecimal.ZERO) > 0) { //si monto a pagar es mayor que cero
                throw new RuntimeException("El usuario tiene préstamos con saldo pendiente.");
            }
            prestamo.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name()));
            prestamoRepository.save(prestamo);
        }
    }

    //metodo para actualizar los datos de un cliente
    public void actualizarDatosCliente(Long usuarioId, ActualizarDatosUsuarioDTO datosUsuarioDTO){
        //verificar que el usuario exista
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));

        // Verificar si el usuario está inactivo
        if (!usuario.isActivo()) {
            throw new IllegalStateException("El usuario se encuentra inactivo.");
        }

        usuario.setNombre(datosUsuarioDTO.nuevoNombre());
        usuario.setApellido(datosUsuarioDTO.nuevoApellido());
        usuario.setPassword(passwordEncoder.encode(datosUsuarioDTO.newPassword()));
        usuario.setCorreo(datosUsuarioDTO.nuevoCorreo());
        usuario.setCedula(datosUsuarioDTO.nuevaCedula());

        //sumar monto adicional a la cuenta principal del usuario

        //buscar cuenta principal
        CuentaAhorro cuentaPrincipal = cuentaRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(CuentaAhorro::isEsPrincipal)
                .findFirst()//extrae el primer registro en donde esPrincipal = true
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró una cuenta principal para este usuario"));

        //se suma el monto adicional a esa cuenta
        cuentaPrincipal.setSaldoDisponible(cuentaPrincipal.getSaldoDisponible().add(datosUsuarioDTO.montoAdicinal()));

        cuentaRepository.save(cuentaPrincipal);
    }

    //generar id del producto
    public String generarIdUnicoProducto() {
       /*
       Esta linea -> return generadorId.generarIdUnicoProducto(id ->cuentaRepository.existsByIdProducto(id));
       Hace lo mismo que la linea de abajo:
       *  */
        return generadorId.generarIdUnicoProducto(cuentaRepository::existsByIdProducto); //se traduce del repositorio toma el metodo existsbyIdProducto como una referencia
    }

    public EstadoProducto colocarEstadoProductos(String nombreEstado) {

        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }
}
