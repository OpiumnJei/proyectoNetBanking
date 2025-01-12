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
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaRepository;
import proyectoNetBanking.infra.errors.DuplicatedItemsException;

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

    //inyectamos el generador de ids
    @Autowired
    private GeneradorId generadorId;

    @Autowired
    private CuentaAhorroRepository cuentaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

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

    //inactivar un usuario logica 1
//    public void inactivarUsuario(Long idUsuario) {
//
//        Usuario usuario = usuarioRepository.findById(idUsuario)
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//        //comprobar si el usuario esta activo o no
//        if (!usuario.isActivo()) {
//            throw new IllegalStateException("El usuario ya se encuentra inactivo.");
//        }
//
//        usuario.setActivo(false); //se inactiva el usuario
//        usuarioRepository.save(usuario);//se guarda el usuario inactivo
//
//        inactivarProductosUsuario(usuario);
//    }
//
//    private void inactivarProductosUsuario(Usuario usuario) {
//        // Obtener las cuentas de ahorro del usuario
//        List<CuentaAhorro> cuentasAhorro = cuentaRepository.findByUsuarioId(usuario.getId());
//
//        // Cambiar el estado de las cuentas a "Inactivo"
//        EstadoProducto estadoInactivo = estadoProductoRepository.findByNombreEstado("Inactivo");
//
//        double saldoCuentaNoPrincipal = 0.0; //variable que almacena el saldo de las cuentas NO principales antes de eliminarlas
//        for (CuentaAhorro cuenta : cuentasAhorro) {
//            for (CuentaAhorro cuentasNoPrincipal : cuentasAhorro) {
//                if (!cuentasNoPrincipal.isEsPrincipal() && cuenta.getSaldoDisponible() != 0.0) { //si el saldo el diferente de 0 y ademas no es principal
//                    saldoCuentaNoPrincipal += cuentasNoPrincipal.getSaldoDisponible(); //suma iterativa
//                }
//                cuentaRepository.delete(cuentasNoPrincipal); //una vez determinadas las cuentas se eliminan
//            }
//            if (cuenta.isEsPrincipal()) {
//                cuenta.setSaldoDisponible(cuenta.getSaldoDisponible() + saldoCuentaNoPrincipal);// se le suma el saldo a la cuenta principal
//            }
//            cuentaRepository.save(cuenta); //se guarda la cuenta
//        }
//
//        // Inactivar tarjetas de crédito
//        List<TarjetaCredito> tarjetas = tarjetaRepository.findByUsuarioId(usuario.getId());//se busca en la bd la lista de tarjetas donde los registros que coincidan con el id del usuario
//        for (TarjetaCredito tarjeta : tarjetas) {
//            if (tarjeta.getSaldoPorPagar() == 0) { //si la tarjeta no tiene saldo que pagar
//                tarjeta.setEstadoProducto(estadoInactivo);
//            } else {
//                throw new RuntimeException("El usuario aun tiene alguna(s) tarjeta(s) por saldar");
//            }
//            tarjetaRepository.save(tarjeta); //se guarda tarjeta
//        }
//
//        // Inactivar préstamos
//        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuario.getId()); //se busca en la bd la lista de préstamos que tiene asociado un usuario
//        for (Prestamo prestamo : prestamos) {
//            if (prestamo.getMontoApagar() == 0) { //si el prestamo no tiene un monto activo que pagar
//                prestamo.setEstadoProducto(estadoInactivo);
//            } else {
//                throw new RuntimeException("El usuario aun tiene algún(os) préstamo(os) por pagar");
//            }
//            prestamoRepository.save(prestamo);
//        }
//    }

    @Transactional // se ejecuta el codigo dentro de una transaccion
    public void inactivarUsuario(Long idUsuario) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si el usuario ya está inactivo
        if (!usuario.isActivo()) {
            throw new IllegalStateException("El usuario ya se encuentra inactivo.");
        }

        // Inactivar al usuario
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        // Inactivar productos asociados
        inactivarProductosUsuario(usuario);
    }

    private void inactivarProductosUsuario(Usuario usuario) {
         // se verifica que el estado exista en la bd
        EstadoProducto estadoInactivo = estadoProductoRepository.findByNombreEstado("Inactivo")
                .orElseThrow(() -> new RuntimeException("Estado 'Inactivo' no encontrado"));

        manejarCuentasDeAhorro(usuario, estadoInactivo); // Manejar cuentas de ahorro
        manejarTarjetasCredito(usuario, estadoInactivo);  // Manejar tarjetas de crédito
        manejarPrestamos(usuario, estadoInactivo);  // Manejar préstamos
    }

    //metodo encargado de manejar la eliminacion y movimiento de fondos entre cuentas de ahorro
    private void manejarCuentasDeAhorro(Usuario usuario, EstadoProducto estadoInactivo) {
        // Obtener las cuentas de ahorro del usuario
        List<CuentaAhorro> cuentasAhorro = cuentaRepository.findByUsuarioId(usuario.getId());

        //almacenar la sumatoria de las cuentas de ahorro que no sean principales antes de su eliminacion
        double saldoCuentasNoPrincipales = cuentasAhorro
                .stream() // Se convierte la lista en un Stream para procesarla de manera funcional
                .filter(cuenta -> !cuenta.isEsPrincipal()) // Filtra las cuentas que NO son principales.
                .mapToDouble(CuentaAhorro::getSaldoDisponible) // Obtiene el saldo disponible de cada cuenta.
                .sum(); // Suma todos los saldos obtenidos

        for (CuentaAhorro cuenta : cuentasAhorro) { //Para cada cuenta de ahorro
            if (!cuenta.isEsPrincipal()) {
                if (cuenta.getSaldoDisponible() > 0) {
                    throw new RuntimeException("Una cuenta no principal aún tiene saldo activo.");
                }
                cuentaRepository.delete(cuenta); // Eliminar cuenta no principal
            } else {
                // Transferir saldo acumulado a la cuenta principal
                cuenta.setSaldoDisponible(cuenta.getSaldoDisponible() + saldoCuentasNoPrincipales);
                cuenta.setEstadoProducto(estadoInactivo); // Inactivar cuenta principal
                cuentaRepository.save(cuenta);
            }
        }
    }

    //metodo encargado de manejar la inactividad de tarjetas de credito
    private void manejarTarjetasCredito(Usuario usuario, EstadoProducto estadoInactivo) {
        // Obtener las tarjetas de credito  del usuario
        List<TarjetaCredito> tarjetas = tarjetaRepository.findByUsuarioId(usuario.getId());

        for (TarjetaCredito tarjeta : tarjetas) {
            if (tarjeta.getSaldoPorPagar() > 0) {//si el usuario aun debe saldar
                throw new RuntimeException("El usuario tiene tarjetas de crédito con saldo pendiente.");
            }
            tarjeta.setEstadoProducto(estadoInactivo);
            tarjetaRepository.save(tarjeta);
        }
    }

    // metodo encargado de manejar la inactividad de los prestamos
    private void manejarPrestamos(Usuario usuario, EstadoProducto estadoInactivo) {
        // obtener los prestamos asociados al usuario
        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuario.getId());

        for (Prestamo prestamo : prestamos) {
            if (prestamo.getMontoApagar() > 0) { //si el usuario aun debe dinero del prestamo
                throw new RuntimeException("El usuario tiene préstamos con saldo pendiente.");
            }
            prestamo.setEstadoProducto(estadoInactivo);
            prestamoRepository.save(prestamo);
        }
    }

}
