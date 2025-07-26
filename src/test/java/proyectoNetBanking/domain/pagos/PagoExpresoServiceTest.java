package proyectoNetBanking.domain.pagos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.dto.pagos.DatosPagoExpresoDTO;
import proyectoNetBanking.dto.pagos.ResponsePagoExpresoDTO;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.service.pagos.PagoExpresoService;
import proyectoNetBanking.service.transacciones.TransaccionService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoExpresoServiceTest {

    @Mock
    private CuentaAhorroRepository cuentaAhorroRepository;

    // --- AÑADE ESTA LÍNEA ---
    @Mock
    private EstadoProductoRepository estadoProductoRepository;

    @Mock
    private TransaccionService transaccionService;

    @InjectMocks
    private PagoExpresoService pagoExpresoService;

    @Test
    void testRealizarPagoExpresoExitoso() {
        // Datos de prueba
        Long idCuentaOrigen = 1L;
        String numeroCuentaDestino = "12345679";
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoExpresoDTO datosPagoExpresoDTO = new DatosPagoExpresoDTO(idCuentaOrigen, numeroCuentaDestino, montoPago);

        //Simular un objeto EstadoProducto de prueba
        EstadoProducto estadoActivo = new EstadoProducto();
        estadoActivo.setId(1L);
        estadoActivo.setNombreEstado("Activo"); // O el nombre que corresponda

        // Simular cuentas de ahorro
        CuentaAhorro cuentaOrigen = new CuentaAhorro();
        cuentaOrigen.setId(idCuentaOrigen);
        cuentaOrigen.setSaldoDisponible(new BigDecimal("200.00"));
        cuentaOrigen.setEstadoProducto(estadoActivo);

        CuentaAhorro cuentaDestino = new CuentaAhorro();
        cuentaDestino.setIdProducto(numeroCuentaDestino);
        cuentaDestino.setSaldoDisponible(new BigDecimal("50.00"));
        cuentaDestino.setEstadoProducto(estadoActivo);

        // Simular transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(cuentaOrigen);
        transaccion.setCuentaDestino(cuentaDestino);
        transaccion.setMontoTransaccion(montoPago);
        transaccion.setId(1L);

        // Configurar mocks
        // Le dices que cuando busque "ACTIVO", devuelva el objeto 'estadoActivo' que ya creaste
        when(estadoProductoRepository.findByNombreEstadoIgnoreCase(EstadoProductoEnum.ACTIVO.name()))
                .thenReturn(Optional.of(estadoActivo));
        //Le dices a Mockito que la cuenta SÍ existe con el estado activo.
        when(cuentaAhorroRepository.existsByIdAndEstadoProducto(cuentaOrigen.getId(), estadoActivo))
                .thenReturn(true);
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaAhorroRepository.findByIdProducto(numeroCuentaDestino)).thenReturn(Optional.of(cuentaDestino));
        when(transaccionService.registrarTransaccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(transaccion);

        // Ejecutar el metodo y toda la logica detras del mismo
        ResponsePagoExpresoDTO resultado = pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO);

        assertNotNull(resultado); //verificar que el retorno de resultado no sea nulo
        assertNotNull(resultado.fechaTransaccion()); // La fecha no debe ser nula

        // Verificar que el resultado tiene los valores correctos
        assertEquals(1L, resultado.transaccionId());
        assertEquals(idCuentaOrigen, resultado.cuentaOrigenId());
        assertEquals(numeroCuentaDestino, resultado.cuentaDestino());
        assertEquals(montoPago, resultado.montoPago());
        assertEquals("El pago expreso se realizó correctamente", resultado.mensaje());

        //comprobar si se actualizaron correctamente los saldos
        assertEquals(new BigDecimal("100.00"), cuentaOrigen.getSaldoDisponible()); // 200 - 100 = 100
        assertEquals(new BigDecimal("150.00"), cuentaDestino.getSaldoDisponible()); // 50 + 100 = 150

        // Verificar que se llamaron los métodos simulados
        verify(cuentaAhorroRepository, times(1)).findById(idCuentaOrigen);
        verify(cuentaAhorroRepository, times(1)).findByIdProducto(numeroCuentaDestino);
        verify(transaccionService, times(1)).registrarTransaccion(
                TipoTransaccion.PAGO_EXPRESO,
                cuentaOrigen,
                cuentaDestino,
                null,
                null,
                montoPago,
                "Se realizo un pago expreso"
        );

    }

    @Test
    void testRealizarPagoExpresoSaldoInsuficiente() {
        // Datos de prueba
        Long idCuentaOrigen = 1L;
        String numeroCuentaDestino = "12345689";
        BigDecimal montoPago = new BigDecimal("300.00"); //monto de la transferencia

        DatosPagoExpresoDTO datosPagoExpresoDTO = new DatosPagoExpresoDTO(idCuentaOrigen, numeroCuentaDestino, montoPago);

        //Simular un objeto EstadoProducto de prueba
        EstadoProducto estadoActivo = new EstadoProducto();
        estadoActivo.setId(1L);
        estadoActivo.setNombreEstado("Activo"); // O el nombre que corresponda

        // Simular cuenta de origen con saldo insuficiente
        CuentaAhorro cuentaOrigen = new CuentaAhorro();
        cuentaOrigen.setId(idCuentaOrigen);
        cuentaOrigen.setSaldoDisponible(new BigDecimal("200.00"));
        cuentaOrigen.setEstadoProducto(estadoActivo);

        // simular cuenta destino
        CuentaAhorro cuentaDestino = new CuentaAhorro();
        cuentaDestino.setIdProducto(numeroCuentaDestino);
        cuentaDestino.setEstadoProducto(estadoActivo);


        // Configurar mock
        // Configurar mocks
        // Le dices que cuando busque "ACTIVO", devuelva el objeto 'estadoActivo' que ya creaste
        when(estadoProductoRepository.findByNombreEstadoIgnoreCase(EstadoProductoEnum.ACTIVO.name()))
                .thenReturn(Optional.of(estadoActivo));
        //Le dices a Mockito que la cuenta SÍ existe con el estado activo.
        when(cuentaAhorroRepository.existsByIdAndEstadoProducto(cuentaOrigen.getId(), estadoActivo))
                .thenReturn(true);

        when(cuentaAhorroRepository.findById(cuentaOrigen.getId())).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaAhorroRepository.findByIdProducto(cuentaDestino.getIdProducto())).thenReturn(Optional.of(cuentaDestino));

        // Ejecutar el metodo y verificar excepción
        SaldoInsuficienteException exception = Assertions.assertThrows(
                SaldoInsuficienteException.class,
                () -> pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO)
        );

        // Verificar mensaje de excepción
        Assertions.assertEquals("La cuenta no dispone de el saldo suficiente para realizar el pago.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarPagoExpresoCuentaOrigenNoEncontrada() {
        // Datos de prueba
        Long idCuentaOrigen = 1L;
        String numeroCuentaDestino = "123456";
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoExpresoDTO datosPagoExpresoDTO = new DatosPagoExpresoDTO(idCuentaOrigen, numeroCuentaDestino, montoPago);

        // simular que el id no se encuentra en la bd retornando un empty
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.empty());

        // Ejecutar el metodo y verificar excepción
        CuentaNotFoundException exception = Assertions.assertThrows(
                CuentaNotFoundException.class,
                () -> {
                    pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO);
                });

        // Verificar mensaje de excepción
        Assertions.assertEquals("La cuenta no ha sido encontrada.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarPagoExpresoCuentaDestinoNoEncontrada() {
        // Datos de prueba
        Long idCuentaOrigen = 1L;
        String numeroCuentaDestino = "123456";
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoExpresoDTO datosPagoExpresoDTO = new DatosPagoExpresoDTO(idCuentaOrigen, numeroCuentaDestino, montoPago);

        //Simular un objeto EstadoProducto de prueba
        EstadoProducto estadoActivo = new EstadoProducto();
        estadoActivo.setId(1L);
        estadoActivo.setNombreEstado("Activo"); // O el nombre que corresponda

        // Simular cuenta de origen
        CuentaAhorro cuentaOrigen = new CuentaAhorro();
        cuentaOrigen.setId(idCuentaOrigen);
        cuentaOrigen.setSaldoDisponible(new BigDecimal("200.00"));
        cuentaOrigen.setEstadoProducto(estadoActivo);

        // Configurar mocks
        // Le dices que cuando busque "ACTIVO", devuelva el objeto 'estadoActivo' que ya creaste
        when(estadoProductoRepository.findByNombreEstadoIgnoreCase(EstadoProductoEnum.ACTIVO.name()))
                .thenReturn(Optional.of(estadoActivo));
        //Le dices a Mockito que la cuenta SÍ existe con el estado activo.
        when(cuentaAhorroRepository.existsByIdAndEstadoProducto(cuentaOrigen.getId(), estadoActivo))
                .thenReturn(true);
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaAhorroRepository.findByIdProducto(numeroCuentaDestino)).thenReturn(Optional.empty());

        // Ejecutar el método y verificar excepción
        CuentaNotFoundException exception = Assertions.assertThrows(
                CuentaNotFoundException.class, () -> {
                    pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO);
                });

        // Verificar mensaje de excepción
        Assertions.assertEquals("La cuenta no ha sido encontrada.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }
}