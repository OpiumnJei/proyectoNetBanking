package proyectoNetBanking.domain.pagos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.dto.pagos.ResponsePagoPrestamoDTO;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.repository.PrestamoRepository;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.dto.pagos.DatosPagoPrestamoDTO;
import proyectoNetBanking.service.pagos.PagoPrestamoService;
import proyectoNetBanking.service.transacciones.TransaccionService;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.repository.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)//para poder utiliar mockito en las pruebas
class PagoPrestamoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private TransaccionService transaccionService;

    @Mock
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Mock
    private EstadoProductoRepository estadoProductoRepository;

    @InjectMocks
    private PagoPrestamoService pagoPrestamoService;

    @Test
    void testRealizarPagoPrestamoExitoso() {
        // Datos de prueba
        Long prestamoId = 1L;
        Long usuarioId = 2L;
        Long cuentaId = 3L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoPrestamoDTO datosPagoPrestamoDTO = new DatosPagoPrestamoDTO(usuarioId, cuentaId, montoPago);

        // Simular usuario
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        // Simular préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setId(prestamoId);
        prestamo.setUsuario(usuario);
        prestamo.setMontoPrestamo(new BigDecimal("500.00"));
        prestamo.setMontoApagar(new BigDecimal("300.00"));
        prestamo.setMontoPagado(new BigDecimal("200.00"));

        // Simular cuenta de ahorro
        CuentaAhorro cuentaAhorro = new CuentaAhorro();
        cuentaAhorro.setId(cuentaId);
        cuentaAhorro.setUsuario(usuario);
        cuentaAhorro.setSaldoDisponible(new BigDecimal("200.00"));

        // Simular transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setId(1L);

        // Configurar mocks
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(prestamoRepository.findById(prestamoId)).thenReturn(Optional.of(prestamo));
        when(cuentaAhorroRepository.findById(cuentaId)).thenReturn(Optional.of(cuentaAhorro));
        when(transaccionService.registrarTransaccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(transaccion);

        // Ejecutar el método
        ResponsePagoPrestamoDTO resultado = pagoPrestamoService.realizarPagoPrestamo(prestamoId, datosPagoPrestamoDTO);

        // Verificar resultados
        assertNotNull(resultado);
        assertEquals(new BigDecimal("100.00"), cuentaAhorro.getSaldoDisponible()); // 200 - 100 = 100
        assertEquals(new BigDecimal("300.00"), prestamo.getMontoPagado()); // 200 + 100 = 300
        assertEquals(new BigDecimal("200.00"), prestamo.getMontoApagar()); // 300 - 100 = 200

        // Verificar que se llamaron los métodos simulados
        verify(usuarioRepository, times(1)).findById(usuarioId);
        verify(prestamoRepository, times(1)).findById(prestamoId);
        verify(cuentaAhorroRepository, times(1)).findById(cuentaId);
        verify(transaccionService, times(1)).registrarTransaccion(
                TipoTransaccion.PAGO_PRESTAMO,
                cuentaAhorro,
                null,
                null,
                prestamo,
                montoPago,
                "Se realizo un pago del prestamo"
        );
    }

    @Test
    void testRealizarPagoPrestamoSaldoInsuficiente() {
        // Datos de prueba
        Long prestamoId = 1L;
        Long usuarioId = 2L;
        Long cuentaId = 3L;
        BigDecimal montoPago = new BigDecimal("300.00");

        DatosPagoPrestamoDTO datosPagoPrestamoDTO = new DatosPagoPrestamoDTO(usuarioId, cuentaId, montoPago);

        // Simular usuario
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        // Simular préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setId(prestamoId);
        prestamo.setUsuario(usuario);
        prestamo.setMontoApagar(new BigDecimal("300.00"));

        // Simular cuenta de ahorro con saldo insuficiente
        CuentaAhorro cuentaAhorro = new CuentaAhorro();
        cuentaAhorro.setId(cuentaId);
        cuentaAhorro.setUsuario(usuario);
        cuentaAhorro.setSaldoDisponible(new BigDecimal("200.00"));

        // Configurar mocks
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(prestamoRepository.findById(prestamoId)).thenReturn(Optional.of(prestamo));
        when(cuentaAhorroRepository.findById(cuentaId)).thenReturn(Optional.of(cuentaAhorro));

        // Ejecutar el metodo y verificar excepción
        SaldoInsuficienteException exception = Assertions.assertThrows(
                SaldoInsuficienteException.class, () -> {
                    pagoPrestamoService.realizarPagoPrestamo(prestamoId, datosPagoPrestamoDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("La cuenta no dispone de el saldo suficiente para realizar el pago.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarPagoPrestamoYaSaldado() {
        // Datos de prueba
        Long prestamoId = 1L;
        Long usuarioId = 2L;
        Long cuentaId = 3L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoPrestamoDTO datosPagoPrestamoDTO = new DatosPagoPrestamoDTO(usuarioId, cuentaId, montoPago);

        // simular creacion de usuario

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        // Simular préstamo ya saldado
        Prestamo prestamo = new Prestamo();
        prestamo.setId(prestamoId);
        prestamo.setMontoApagar(BigDecimal.ZERO);

        // Configurar mocks
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(prestamoRepository.findById(prestamoId)).thenReturn(Optional.of(prestamo));

        // Ejecutar el método y verificar excepción
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    pagoPrestamoService.realizarPagoPrestamo(prestamoId, datosPagoPrestamoDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("El monto del prestamo ya ha sido saldado.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarPagoPrestamoNoPerteneceAlUsuario() {
        // Datos de prueba
        Long prestamoId = 1L;
        Long usuarioId = 2L;
        Long cuentaId = 3L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoPrestamoDTO datosPagoPrestamoDTO = new DatosPagoPrestamoDTO(usuarioId, cuentaId, montoPago);

        // Simular usuario 1
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        // simular usuario 2
        Usuario usuario2 = new Usuario();
        usuario2.setId(4L);

        // Simular préstamo que no pertenece al usuario
        Prestamo prestamo = new Prestamo();
        prestamo.setId(prestamoId);
        prestamo.setMontoApagar(BigDecimal.valueOf(100.00));
        prestamo.setUsuario(usuario2); // Usuario diferente

        // Configurar mocks
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(prestamoRepository.findById(prestamoId)).thenReturn(Optional.of(prestamo));

        // Ejecutar el metodo y verificar excepción
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    pagoPrestamoService.realizarPagoPrestamo(prestamoId, datosPagoPrestamoDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("El prestamo especificado no pertenece al usuario", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarPagoPrestamoCuentaNoEncontrada() {
        // Datos de prueba
        Long prestamoId = 1L;
        Long usuarioId = 2L;
        Long cuentaId = 3L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoPrestamoDTO datosPagoPrestamoDTO = new DatosPagoPrestamoDTO(usuarioId, cuentaId, montoPago);

        // Simular usuario
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        // Simular préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setId(prestamoId);
        prestamo.setMontoApagar(BigDecimal.valueOf(500.00));
        prestamo.setUsuario(usuario);

        // Configurar mocks
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(prestamoRepository.findById(prestamoId)).thenReturn(Optional.of(prestamo));
        when(cuentaAhorroRepository.findById(cuentaId)).thenReturn(Optional.empty());

        // Ejecutar el método y verificar excepción
        CuentaNotFoundException exception = Assertions.assertThrows(
                CuentaNotFoundException.class,
                () -> {
                    pagoPrestamoService.realizarPagoPrestamo(prestamoId, datosPagoPrestamoDTO);
                });

        // Verificar mensaje de excepción
        Assertions.assertEquals("La cuenta de ahorro no existe.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarPagoPrestamoUsuarioNoEncontrado() {
        // Datos de prueba
        Long prestamoId = 1L;
        Long usuarioId = 2L;
        Long cuentaId = 3L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoPrestamoDTO datosPagoPrestamoDTO = new DatosPagoPrestamoDTO(usuarioId, cuentaId, montoPago);

        Prestamo prestamo = new Prestamo();
        prestamo.setId(prestamoId);

        // Configurar mock para lanzar excepción
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());
        when(prestamoRepository.findById(prestamoId)).thenReturn(Optional.of(prestamo));


        // Ejecutar el metodo y verificar excepción
        UsuarioNotFoundException exception = assertThrows(
                UsuarioNotFoundException.class,
                () -> {
                    pagoPrestamoService.realizarPagoPrestamo(prestamoId, datosPagoPrestamoDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("Usuario no encontrado", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarPagoPrestamoNoEncontrado() {
        // Datos de prueba
        Long prestamoId = 1L;
        Long usuarioId = 2L;
        Long cuentaId = 3L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoPrestamoDTO datosPagoPrestamoDTO = new DatosPagoPrestamoDTO(usuarioId, cuentaId, montoPago);

        // Configurar mock para lanzar excepción
        when(prestamoRepository.findById(prestamoId)).thenReturn(Optional.empty());

        // Ejecutar el método y verificar excepción
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pagoPrestamoService.realizarPagoPrestamo(prestamoId, datosPagoPrestamoDTO);
        });

        // Verificar mensaje de excepción
        assertEquals("El prestamo no existe.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }
}