package proyectoNetBanking.domain.pagos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.domain.prestamos.PrestamoRepository;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.domain.transacciones.TransaccionService;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class PagoPrestamoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private  EstadoProductoRepository estadoProductoRepository;

    //validar datos enviados por el admin
    @Transactional
    public Transaccion realizarPagoPrestamo(Long prestamoId, DatosPagoPrestamoDTO datosPagoPrestamoDTO) {

        //verificar que el prestamo exista
        Prestamo prestamo = obtenerPrestamo(prestamoId);

        //verificar que el usuario exista en la bd
        Usuario usuario = obtenerUsuario(datosPagoPrestamoDTO.idUsuario());

        if (prestamo.getMontoApagar().compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("El monto del prestamo ya ha sido saldado.");
        }

        validarPrestamoUsuario(prestamo, usuario);  //validar que el prestamo pertenezca al usuario

        Long cuentaId = datosPagoPrestamoDTO.idCuentaUsuario();

        //verificar que la cuenta exista y que pernezca al usuario
        CuentaAhorro cuentaAhorro = validarCuentaAhorro(cuentaId, usuario);

        BigDecimal montoPago = datosPagoPrestamoDTO.montoPago();

        validarSaldoDisponible(cuentaAhorro, montoPago);

        //realizar el pago
        registrarPagoPrestamo(cuentaAhorro, prestamo, montoPago);

        return transaccionService.registrarTransaccion(
                TipoTransaccion.PAGO_PRESTAMO,
                cuentaAhorro,
                null,
                null,
                prestamo,
                montoPago,
                "Se realizo un pago del prestamo"
        );
    }

    //pagar un prestamo y cambiar su estado
    private void registrarPagoPrestamo(CuentaAhorro cuentaAhorro, Prestamo prestamo, BigDecimal montoPago) {
        //restar el monto pagado a la cuenta de ahorro
        cuentaAhorro.setSaldoDisponible(cuentaAhorro.getSaldoDisponible().subtract(montoPago));
        cuentaAhorroRepository.save(cuentaAhorro);

        //sumar el monto pagado por el usuario al campo correspondiente en la entidad prestamo
        prestamo.setMontoPagado(prestamo.getMontoPagado().add(montoPago));

        //restar la cantidad pagada por el usuario al monto original del prestamo
        prestamo.setMontoApagar(prestamo.getMontoApagar().subtract(montoPago));

        if (prestamo.getMontoApagar().compareTo(BigDecimal.ZERO) == 0) { //si la cantidad a pagar es 0, quiere decir que ya esta pagp
            prestamo.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name()));
        }

        prestamoRepository.save(prestamo); //guardar en la bd
    }


    private Prestamo validarPrestamoUsuario(Prestamo prestamo, Usuario usuario) {
        if (!prestamo.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("El prestamo especificado no pertenece al usuario");
        }

        return prestamo;
    }

    //verificar que Si el saldo disponible en la cuenta es menor al monto enviado por usuario
    private void validarSaldoDisponible(CuentaAhorro cuentaAhorro, BigDecimal montoPago) {
        if (cuentaAhorro.getSaldoDisponible().compareTo(montoPago) < 0) {
            throw new SaldoInsuficienteException("La cuenta no dispone de el saldo suficiente para realizar el pago.");
        }
    }

    private CuentaAhorro validarCuentaAhorro(Long Idcuenta, Usuario usuario) {
        CuentaAhorro cuentaAhorro = cuentaAhorroRepository.findById(Idcuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta de ahorro no existe."));

        if (!cuentaAhorro.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("La cuenta especificada no pertenece al usuario");
        }
        return cuentaAhorro;
    }

    private Usuario obtenerUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));
    }

    private Prestamo obtenerPrestamo(Long prestamoId) {
        return prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("El prestamo no existe."));
    }
    //metodo encargado de la gestion de estados
    private EstadoProducto colocarEstadoProductos(String nombreEstado) {

        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }
}
