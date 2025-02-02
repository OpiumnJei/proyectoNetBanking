package proyectoNetBanking.domain.usuarios.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.prestamos.PrestamoRepository;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.TransaccionRepository;
import proyectoNetBanking.domain.usuarios.TipoUsuario;
import proyectoNetBanking.domain.usuarios.TipoUsuarioRepository;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.EstadoProductoNotFoundException;
import proyectoNetBanking.infra.errors.TypeUserNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class IndicadoresService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private CuentaAhorroRepository cuentaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

    /**
     * Listar la cantidad de transacciones que se han realizado en el sistema total (Desde el
     * inicio de los tiempos) y las transacciones de ese día.
     */

    public Map<String, Long> ListarTotalTransacciones() {
        Map<String, Long> indicadoresTransacciones = new HashMap<>();

        // Total de transacciones
        Long totalTransacciones = transaccionRepository.count();
        indicadoresTransacciones.put("totalTransacciones", totalTransacciones);

        // Transacciones del día
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay(); //inicio del dia

        //al inicio del dia se le suma un dia, y a ese dia se le quita un nano segundo
        LocalDateTime finDia = inicioDia.plusDays(1).minusNanos(1); // fin del dia

        Long transaccionesHoy = transaccionRepository.countByFechaBetween(inicioDia, finDia);
        indicadoresTransacciones.put("transaccionesHoy", transaccionesHoy);

        return indicadoresTransacciones;
    }

    /**
     * Las cantidades de pagos realizados ese día y en total(Desde el inicio de los tiempos).
     */

    public Map<String, Long> listarTotalPagos() {
        Map<String, Long> indicadoresPagos = new HashMap<>();

        // total pagos expresos
        Long totalPagosExpresos = contarPagosExpresos();
        indicadoresPagos.put("totalPagosExpresos", totalPagosExpresos);

        // total pagos tarjetas
        Long totalPagosTarjetas = contarPagosTarjetas();
        indicadoresPagos.put("totalPagosTarjetas", totalPagosTarjetas);

        Long totalPagosPrestamos = contarPagosPrestamos();
        indicadoresPagos.put("totalPagosPrestamos", totalPagosPrestamos);

        Long totalPagosBeneficiarios = contarPagosBeneficiarios();
        indicadoresPagos.put("totalPagosBeneficiarios", totalPagosBeneficiarios);

        return indicadoresPagos;
    }

    /**
     * La cantidad de clientes activos y los inactivos
     *
     * @return Un Map con las cantidades de clientes activos e inactivos.
     * @throws TypeUserNotFoundException Si el tipo de usuario "Cliente" no se encuentra.
     */

    public Map<String, Long> listarClientes() {
        Map<String, Long> indicadoresClientes = new HashMap<>();

        // Recuperar el TipoUsuario "Cliente" desde la base de datos
        TipoUsuario usuarioCliente = tipoUsuarioRepository.findById(2L)
                .orElseThrow(() -> new TypeUserNotFoundException("Tipo de usuario no encontrado"));

        // Contar clientes activos
        Long totalClientesActivos = contarClientesActivos(usuarioCliente);
        indicadoresClientes.put("clientesActivos", totalClientesActivos);

        // Contar clientes inactivos
        Long totalClientesInactivos = contarClientesInactivos(usuarioCliente);
        indicadoresClientes.put("clientesInactivos", totalClientesInactivos);

        return indicadoresClientes;
    }


    /**
     * Las cantidades de productos activos asignados a los clientes.
     *
     * @return Un Map con las cantidades de productos asignados a los clientes.
     * @throws EstadoProductoNotFoundException Si el tipo de estado ACTIVO no se encuentra.
     */
    public Map<String, Long> listarProductosAsignados() {
        Map<String, Long> indicadoresProductos = new HashMap<>();

        String activo = "Activo";
        EstadoProducto estadoActivo = estadoProductoRepository.findByNombreEstadoIgnoreCase(activo)
                .orElseThrow(() -> new EstadoProductoNotFoundException("Estado no encontrado"));

        Long totalCuentasAhorroActivas = contarCuentasActivas(estadoActivo);
        indicadoresProductos.put("totalCuentasAhorroActivas", totalCuentasAhorroActivas);

        Long totalPrestamosActivos = contarPrestamosActivos(estadoActivo);
        indicadoresProductos.put("totalPrestamosActivos", totalPrestamosActivos);

        Long totalTarjetasActivas = contarTarjetasActivas(estadoActivo);
        indicadoresProductos.put("totalTarjetasActivas", totalTarjetasActivas);

        return indicadoresProductos;
    }

    private Long contarCuentasActivas(EstadoProducto estadoActivo) {
        return cuentaRepository.countByEstadoProductoId(estadoActivo);
    }

    private Long contarPrestamosActivos(EstadoProducto estadoActivo) {
        return prestamoRepository.countByEstadoProductoId(estadoActivo);
    }

    private Long contarTarjetasActivas(EstadoProducto estadoActivo) {
        return tarjetaRepository.countByEstadoProductoId(estadoActivo);
    }

    private Long contarClientesActivos(TipoUsuario usuarioCliente) {
        return usuarioRepository.countByTipoUsuarioAndActivo(usuarioCliente, true);
    }

    private Long contarClientesInactivos(TipoUsuario usuarioCliente) {
        return usuarioRepository.countByTipoUsuarioAndActivo(usuarioCliente, false);
    }

    private Long contarPagosExpresos() {
        return transaccionRepository.countByTipoTransaccion(TipoTransaccion.PAGO_EXPRESO);
    }

    private Long contarPagosTarjetas() {
        return transaccionRepository.countByTipoTransaccion(TipoTransaccion.PAGO_TARJETA);
    }

    private Long contarPagosPrestamos() {
        return transaccionRepository.countByTipoTransaccion(TipoTransaccion.PAGO_PRESTAMO);
    }

    private Long contarPagosBeneficiarios() {
        return transaccionRepository.countByTipoTransaccion(TipoTransaccion.PAGO_BENEFICIARIO);
    }

}
