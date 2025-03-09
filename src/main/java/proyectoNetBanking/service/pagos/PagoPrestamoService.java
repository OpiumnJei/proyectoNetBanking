package proyectoNetBanking.service.pagos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.dto.pagos.DatosPagoPrestamoDTO;
import proyectoNetBanking.dto.pagos.ResponsePagoPrestamoDTO;
import proyectoNetBanking.infra.errors.*;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.repository.PrestamoRepository;
import proyectoNetBanking.repository.UsuarioRepository;
import proyectoNetBanking.service.transacciones.TransaccionService;

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
    private EstadoProductoRepository estadoProductoRepository;

    //validar datos enviados por el admin
    @Transactional
    public ResponsePagoPrestamoDTO realizarPagoPrestamo(Long prestamoId, DatosPagoPrestamoDTO datosPagoPrestamoDTO) {

        //verificar que el prestamo exista
        Prestamo prestamo = obtenerPrestamo(prestamoId);

        //verificar que el usuario exista en la bd
        Usuario usuario = obtenerUsuario(datosPagoPrestamoDTO.usuarioId());

        if (prestamo.getMontoApagar().compareTo(BigDecimal.ZERO) == 0) {
            throw new PrestamoYaSaldadoException("El monto del prestamo ya ha sido saldado.");
        }

        validarPrestamoUsuario(prestamo, usuario);  //validar que el prestamo pertenezca al usuario

        Long cuentaId = datosPagoPrestamoDTO.cuentaUsuarioId();

        //verificar que la cuenta exista y que pernezca al usuario
        CuentaAhorro cuentaAhorro = validarCuentaAhorro(cuentaId, usuario);

        BigDecimal montoPago = datosPagoPrestamoDTO.montoPago();

        validarSaldoDisponible(cuentaAhorro, montoPago);

        //igualar el monto a pagar con el monto de la deuda
        BigDecimal montoPagoAjustado = ajustarMontoPago(prestamo, montoPago);

        //realizar el pago
        registrarPagoPrestamo(cuentaAhorro, prestamo, montoPagoAjustado);

        Transaccion transaccion = transaccionService.registrarTransaccion(
                TipoTransaccion.PAGO_PRESTAMO,
                cuentaAhorro,
                null,
                null,
                prestamo,
                montoPago,
                "Se realizo un pago del prestamo"
        );

        return new ResponsePagoPrestamoDTO(
                transaccion.getId(),
                transaccion.getTipoTransaccion(),
                transaccion.getFecha(),
                transaccion.getCuentaOrigen().getId(),
                transaccion.getPrestamo().getId(),
                transaccion.getMontoTransaccion(),
                prestamo.getMontoApagar(),
                "El pago del prestamo se realizó correctamente"
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

    //metodo gestiona la igualdad entre el monto pagado y el monto a pagar
    private BigDecimal ajustarMontoPago(Prestamo prestamo, BigDecimal montoPago) {

        BigDecimal saldoPorPagar = prestamo.getMontoApagar(); //saldo a pagar

        if (montoPago.compareTo(saldoPorPagar) > 0) {//si el monto a pagar es mayor que el saldo a pagar
            //se paga lo que se debe lo demás no se usa
            montoPago = saldoPorPagar; //la cantidad a pagar sera igual al saldo a paga
        }

        return montoPago;
    }

    private void validarPrestamoUsuario(Prestamo prestamo, Usuario usuario) {
        if (!prestamo.getUsuario().getId().equals(usuario.getId())) {
            throw new PrestamoNoPerteneceAUsuarioException("El prestamo especificado no pertenece al usuario.");
        }
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
            throw new CuentaNoPerteneceAUsuarioException("La cuenta especificada no pertenece al usuario");
        }
        return cuentaAhorro;
    }

    private Usuario obtenerUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));
    }

    private Prestamo obtenerPrestamo(Long prestamoId) {
        return prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new PrestamoNotFoundException("El prestamo no existe."));
    }
    //metodo encargado de la gestion de estados
    private EstadoProducto colocarEstadoProductos(String nombreEstado) {

        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }
}
