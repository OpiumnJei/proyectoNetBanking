package proyectoNetBanking.service.usuarios;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.domain.usuarios.TipoUsuario;
import proyectoNetBanking.domain.usuarios.TipoUsuarioEnum;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.dto.productos.ProductoUsuarioDTO;
import proyectoNetBanking.dto.usuarios.*;
import proyectoNetBanking.infra.errors.*;
import proyectoNetBanking.repository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service //marcamos la clase como un servicio/componente de spring
public class UsuarioService {

    // con autowired entablamos una relacion con el repositorio de usuarios, lo inyectamos
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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


    /**
     * @clienteGuardado contiene el id generado por JPA,
     * Los frameworks como JPA generan automáticamente el ID para las entidades persistidas,
     * y este ID estará presente en el objeto retornado por save.
     */
    //crear un cliente
    public void crearUsuario(DatosUsuarioDTO datosUsuarioDTO) {

        validarCedula(datosUsuarioDTO.cedula());
        validarCorreo(datosUsuarioDTO.correo());

        //Si el tipo de usuario introducido por admin es un cliente
        if (TipoUsuarioEnum.CLIENTE.name().equalsIgnoreCase(datosUsuarioDTO.tipoUsuario())) {
            var cliente = crearCliente(datosUsuarioDTO);

            Usuario clienteGuardado = usuarioRepository.save(cliente);
            asignarCuentaPrincipal(clienteGuardado);//colocarle cuenta principal por motivos de logica de negocio
        }

        //Si el tipo de usuario introducido por admin es un administrador
        if (TipoUsuarioEnum.ADMINISTRADOR.name().equalsIgnoreCase(datosUsuarioDTO.tipoUsuario())) {
            var admin = crearAdministrador(datosUsuarioDTO);
            Usuario adminGuardado = usuarioRepository.save(admin);
        }

    }

    private Usuario crearCliente(DatosUsuarioDTO datosUsuarioDTO) {

        Usuario usuario = new Usuario();
        usuario.setNombre(datosUsuarioDTO.nombre());
        usuario.setApellido(datosUsuarioDTO.apellido());
        usuario.setCedula(datosUsuarioDTO.cedula());
        usuario.setCorreo(datosUsuarioDTO.correo());
        usuario.setPassword(passwordEncoder.encode(datosUsuarioDTO.password()));//se hashea la contrasenia
        usuario.setTipoUsuario(colocarTipoUsuario(TipoUsuarioEnum.CLIENTE.name()));
        usuario.setMontoInicial(datosUsuarioDTO.montoInicial());
        usuario.setActivo(true);
        return usuario;
    }

    private Usuario crearAdministrador(DatosUsuarioDTO datosUsuarioDTO) {

        Usuario usuario = new Usuario();
        usuario.setNombre(datosUsuarioDTO.nombre());
        usuario.setApellido(datosUsuarioDTO.apellido());
        usuario.setCedula(datosUsuarioDTO.cedula());
        usuario.setCorreo(datosUsuarioDTO.correo());
        usuario.setPassword(passwordEncoder.encode(datosUsuarioDTO.password()));//se hashea la contrasenia
        usuario.setTipoUsuario(colocarTipoUsuario(TipoUsuarioEnum.ADMINISTRADOR.name()));
        usuario.setMontoInicial(BigDecimal.ZERO);
        usuario.setActivo(true);

        return usuario;
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


    public Page<ListaUsuariosDTO> listarUsuarios(Pageable pageable){

        // paginas de beneficiarios
        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);

        // se retornan los datos mapeados que contienen la informacion de la pagina actual
        return usuarios.map( usuario -> new ListaUsuariosDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getTipoUsuario().getNombreTipoUsuario(),
                usuario.isActivo()
        ));
    }

    @Transactional // se ejecuta el codigo dentro de una transaccion
    public void inactivarUsuario(Long usuarioId) {

        Usuario usuario = obtenerUsuario(usuarioId);

        // Verificar si el usuario ya está inactivo
        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException("El usuario ya se encuentra inactivo.");
        }

        // verificar e inactivar productos que no tengan monto pendiente por pagar
        inactivarProductosUsuario(usuario.getId());

        //luego de verificarse de que el usuario no tenga productos con deudas, se inactiva
        usuario.setActivo(false);
        Usuario usuarioInactivo = usuarioRepository.save(usuario);
    }

    @Transactional
    public void activarUsuario(Long usuarioId) {

        Usuario usuario = obtenerUsuario(usuarioId);

        // Verificar si el usuario ya está inactivo
        if (usuario.isActivo()) {
            throw new UsuarioInactivoException("El usuario se encuentra activo.");
        }

        //luego de verificarse de que el usuario no tenga productos con deudas, se inactiva
        usuario.setActivo(true);
        Usuario usuarioActivo = usuarioRepository.save(usuario);
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

        var saldoCuentasSecundarias = calcularSaldoCuentasSecundarias(cuentasAhorro); //almacena la sumatoria de los saldos

        transferirFondosACuentaPrincipal(cuentasAhorro, saldoCuentasSecundarias);

    }

    @Transactional
    private void transferirFondosACuentaPrincipal(List<CuentaAhorro> cuentasAhorro, BigDecimal saldoCuentasSecundarias) {
        for (CuentaAhorro cuenta : cuentasAhorro) { //Para cada cuenta de ahorro
            if (!cuenta.isEsPrincipal()) {
                if (BigDecimal.ZERO.compareTo(cuenta.getSaldoDisponible()) != 0) {
                    cuenta.setSaldoDisponible(BigDecimal.ZERO); //reducir a 0 el saldo de las cuentas NO pricipales para que se refleje la transaccion entre cuentas
                }
                cuenta.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name())); // Eliminar logicamente cuentas no principal
            }

            // Transferir saldo acumulado a la cuenta principal
            // como ambos metodos estan referenciados por atributos BigDecimal podemos usar el metodo add
            cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().add(saldoCuentasSecundarias));
            cuenta.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name())); // Inactivar cuenta principal
            cuentaRepository.save(cuenta);
        }
    }

    private BigDecimal calcularSaldoCuentasSecundarias(List<CuentaAhorro> cuentasAhorro) {
        return cuentasAhorro
                .stream() // Se convierte la lista en un Stream para procesarla de manera funcional
                .filter(cuenta -> !cuenta.isEsPrincipal()) // Filtra las cuentas que NO son principales.
                .map(CuentaAhorro::getSaldoDisponible) // Obtiene el saldo disponible de cada cuenta.
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Suma todos los saldos obtenidos
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
    public ClienteResponseDTO actualizarDatosCliente(Long usuarioId, ActualizarDatosUsuarioDTO datosUsuarioDTO) {
        Usuario usuario = obtenerUsuario(usuarioId);

        // Verificar si el usuario está inactivo
        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException("El usuario se encuentra inactivo.");
        }

        var montoAdicional = datosUsuarioDTO.montoAdicional();

        actualizarDatosCliente(usuario, datosUsuarioDTO);
        CuentaAhorro cuentaAhorro = obtenerCuentaPrincipal(usuarioId); //se busca la cuenta principal del usuario
        actualizarSaldoCuentaPrincipal(cuentaAhorro, montoAdicional); //se actualiza el saldo de la cuenta asociada al usuario

        //repuesta al cliente
        return new ClienteResponseDTO(
                datosUsuarioDTO.nuevoNombre(),
                datosUsuarioDTO.nuevoApellido(),
                datosUsuarioDTO.nuevaCedula(),
                datosUsuarioDTO.nuevoCorreo(),
                datosUsuarioDTO.newPassword(),
                datosUsuarioDTO.montoAdicional()
        );
    }

    public AdminResponseDTO actualizarDatosAdmin(Long usuarioId, ActualizarDatosUsuarioDTO datosUsuarioDTO) {
        Usuario usuario = obtenerUsuario(usuarioId);

        // Verificar si el usuario está inactivo
        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException("El usuario se encuentra inactivo.");
        }

        //verificar si el usuario es de tipo administrador
        if("ADMINISTRADOR".equalsIgnoreCase(usuario.getTipoUsuario().getNombreTipoUsuario())){
            throw new BusinessException("El usuario que se quiere actualizar no es un administrador.");
        }

        actualizarDatosAdmin(usuario, datosUsuarioDTO);

        // retorno al admin
        return new AdminResponseDTO(
                datosUsuarioDTO.nuevoNombre(),
                datosUsuarioDTO.nuevoApellido(),
                datosUsuarioDTO.nuevaCedula(),
                datosUsuarioDTO.nuevoCorreo(),
                datosUsuarioDTO.newPassword()
        );
    }

    private void actualizarSaldoCuentaPrincipal(CuentaAhorro cuentaAhorro, BigDecimal montoAdicional) {
        if (montoAdicional == null) {
            throw new DatosInvalidosException("El monto adicional no puede ser null");
        }
        cuentaAhorro.setSaldoDisponible(cuentaAhorro.getSaldoDisponible().add(montoAdicional));
        cuentaRepository.save(cuentaAhorro);
    }

    //metodo para obtener la cuenta principal del usuario
    private CuentaAhorro obtenerCuentaPrincipal(Long usuarioId) {
        return cuentaRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(CuentaAhorro::isEsPrincipal)
                .findFirst()//extrae el primer registro en donde esPrincipal = true
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró una cuenta principal para este usuario"));
    }

    //metodo auxiliar para actualizar datos
    private void actualizarDatosCliente(Usuario cliente, ActualizarDatosUsuarioDTO datosUsuarioDTO) {

        cliente.setNombre(datosUsuarioDTO.nuevoNombre());
        cliente.setApellido(datosUsuarioDTO.nuevoApellido());
        cliente.setPassword(passwordEncoder.encode(datosUsuarioDTO.newPassword()));
        cliente.setCorreo(datosUsuarioDTO.nuevoCorreo());
        cliente.setCedula(datosUsuarioDTO.nuevaCedula());
    }

    //metodo auxiliar para actualizar datos
    private void actualizarDatosAdmin(Usuario admin, ActualizarDatosUsuarioDTO datosUsuarioDTO) {

        admin.setNombre(datosUsuarioDTO.nuevoNombre());
        admin.setApellido(datosUsuarioDTO.nuevoApellido());
        admin.setPassword(passwordEncoder.encode(datosUsuarioDTO.newPassword()));
        admin.setCorreo(datosUsuarioDTO.nuevoCorreo());
        admin.setCedula(datosUsuarioDTO.nuevaCedula());
    }

    //Se retorna una lista de todos los productos activos de un usuario
    public List<ProductoUsuarioDTO> obtenerProductosUsuario(Long usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);

        // Obtener los productos activos del usuario
        List<CuentaAhorro> cuentasAhorro = listarCuentasAhorroActivas(usuario.getId());
        List<TarjetaCredito> tarjetasCredito = listarTarjetasCreditoActivas(usuario.getId());
        List<Prestamo> prestamos = listarPrestamosActivos(usuario.getId());

        // validar si el usuario tiene al menos un producto activo
        if(cuentasAhorro.isEmpty() && tarjetasCredito.isEmpty() && prestamos.isEmpty()){
            throw new ProductosNotFoundException("El usuario no tiene productos activos");
        }
        return listarProductosUsuarios(cuentasAhorro, tarjetasCredito, prestamos);
    }

    private List<CuentaAhorro> listarCuentasAhorroActivas(Long usuarioId) {
        return cuentaRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(c -> esProductoActivo(c.getEstadoProducto()))
                .toList();
    }

    private List<TarjetaCredito> listarTarjetasCreditoActivas(Long usuarioId) {
        return tarjetaRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(t -> esProductoActivo(t.getEstadoProducto()))
                .toList();
    }

    private List<Prestamo> listarPrestamosActivos(Long usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(p -> esProductoActivo(p.getEstadoProducto()))
                .toList();
    }

    private List<ProductoUsuarioDTO> listarProductosUsuarios(List<CuentaAhorro> cuentasAhorro, List<TarjetaCredito> tarjetasCredito, List<Prestamo> prestamos) {
        List<ProductoUsuarioDTO> productos = new ArrayList<>();

        // Agregar cuentas de ahorro al listado
        for (CuentaAhorro cuenta : cuentasAhorro) {
            productos.add(ProductoUsuarioDTO.builder() //usando builder de lombok
                    .tipoProducto("Cuenta de ahorro")
                    .productoId(cuenta.getId())
                    .saldoDisponible(cuenta.getSaldoDisponible())
                    .build()
            );
        }

        // Agregar tarjetas de crédito al listado
        for (TarjetaCredito tarjeta : tarjetasCredito) {
            productos.add(ProductoUsuarioDTO.builder()
                    .tipoProducto("Tarjeta de credito")
                    .productoId(tarjeta.getId())
                    .saldoPorPagar(tarjeta.getSaldoPorPagar())
                    .build()
            );
        }

        // Agregar préstamos al listado
        for (Prestamo prestamo : prestamos) {
            productos.add(ProductoUsuarioDTO.builder()
                    .tipoProducto("Prestamo")
                    .productoId(prestamo.getId())
                    .saldoPorPagar(prestamo.getMontoApagar())
                    .build()
            );
        }

        // Retornar el listado de productos
        return productos;
    }

    //generar id del producto
    public String generarIdUnicoProducto() {
       /*
       Esta linea -> return generadorId.generarIdUnicoProducto(id ->cuentaRepository.existsByIdProducto(id));
       Hace lo mismo que la linea de abajo:
       */
        return generadorId.generarIdUnicoProducto(cuentaRepository::existsByIdProducto); //se traduce del repositorio toma el metodo existsbyIdProducto como una referencia
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));
    }

    private void validarCorreo(String correo) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new DuplicatedItemsException("El nuevoCorreo ya se encuentra registrado en el sistema.");
        }
    }

    private void validarCedula(String cedula) {
        if (usuarioRepository.existsByCedula(cedula)) {
            throw new DuplicatedItemsException("La cédula ya se encuentra registrada en el sistema.");
        }
    }

    private TipoUsuario colocarTipoUsuario(String tipoUsuario) {
        return tipoUsuarioRepository.findByNombreTipoUsuarioIgnoreCase(tipoUsuario)
                .orElseThrow(() -> new TypeUserNotFoundException("Tipo de usuario no encontrado"));
    }

    private EstadoProducto colocarEstadoProductos(String nombreEstado) {
        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }

    //verificar si un producto esta activo
    private boolean esProductoActivo(EstadoProducto estadoProducto) {
        return estadoProducto.getNombreEstado().equalsIgnoreCase(EstadoProductoEnum.ACTIVO.name());
    }
}
