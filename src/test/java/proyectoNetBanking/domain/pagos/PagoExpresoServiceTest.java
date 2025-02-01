package proyectoNetBanking.domain.pagos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.domain.transacciones.TransaccionService;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoExpresoServiceTest {

    @Mock
    private CuentaAhorroRepository cuentaAhorroRepository;

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

        DatosPagoExpresoDTO datosPagoExpresoDTO = new DatosPagoExpresoDTO(numeroCuentaDestino, idCuentaOrigen, montoPago);

        // Simular cuentas de ahorro
        CuentaAhorro cuentaOrigen = new CuentaAhorro();
        cuentaOrigen.setId(idCuentaOrigen);
        cuentaOrigen.setSaldoDisponible(new BigDecimal("200.00"));

        CuentaAhorro cuentaDestino = new CuentaAhorro();
        cuentaDestino.setIdProducto(numeroCuentaDestino);
        cuentaDestino.setSaldoDisponible(new BigDecimal("50.00"));

        // Simular transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setId(1L);

        // Configurar mocks
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaAhorroRepository.findByIdProducto(numeroCuentaDestino)).thenReturn(Optional.of(cuentaDestino));
        when(transaccionService.registrarTransaccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(transaccion);

        // Ejecutar el metodo y toda la logica detras del mismo
        Transaccion resultado = pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO);


        assertNotNull(resultado); //verificar que el retorno de resultado no sea nulo

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

        DatosPagoExpresoDTO datosPagoExpresoDTO = new DatosPagoExpresoDTO(numeroCuentaDestino, idCuentaOrigen,montoPago);

        // Simular cuenta de origen con saldo insuficiente
        CuentaAhorro cuentaOrigen = new CuentaAhorro();
        cuentaOrigen.setId(idCuentaOrigen);
        cuentaOrigen.setSaldoDisponible(new BigDecimal("200.00"));

        // simular cuenta destino
        CuentaAhorro cuentaDestino = new CuentaAhorro();
        cuentaDestino.setIdProducto(numeroCuentaDestino);

        // Configurar mock
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

        DatosPagoExpresoDTO datosPagoExpresoDTO = new DatosPagoExpresoDTO( numeroCuentaDestino,  idCuentaOrigen,montoPago);

        // simular que el id no se encuentra en la bd retornando un empty
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.empty());

        // Ejecutar el metodo y verificar excepción
        CuentaNotFoundException exception = Assertions.assertThrows(
                CuentaNotFoundException.class,
                () -> {pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO);
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

        DatosPagoExpresoDTO datosPagoExpresoDTO = new DatosPagoExpresoDTO( numeroCuentaDestino, idCuentaOrigen, montoPago);

        // Simular cuenta de origen
        CuentaAhorro cuentaOrigen = new CuentaAhorro();
        cuentaOrigen.setId(idCuentaOrigen);
        cuentaOrigen.setSaldoDisponible(new BigDecimal("200.00"));

        // Configurar mocks
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