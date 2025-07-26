package proyectoNetBanking.domain.transferenciasCuenta;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.dto.cuentasAhorro.ResponseTransferenciaDTO;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.dto.cuentasAhorro.DatosTransferenciaCuentaDTO;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.service.transacciones.TransaccionService;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;
import proyectoNetBanking.service.cuentasAhorro.TransferenciaCuentaService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaCuentaServiceTest {


    @Mock
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Mock
    private TransaccionService transaccionService;

    // --- AÑADE ESTA LÍNEA ---
    @Mock
    private EstadoProductoRepository estadoProductoRepository;

    @InjectMocks
    private TransferenciaCuentaService transferenciaCuentaService;

    @Test
    void testRealizarTransferenciaExitoso() {
        // Datos de prueba
        Long idCuentaOrigen = 1L;
        Long idCuentaDestino = 2L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosTransferenciaCuentaDTO datosTransferenciaDTO = new DatosTransferenciaCuentaDTO(idCuentaOrigen,idCuentaDestino,  montoPago);

        //Simular un objeto EstadoProducto de prueba
        EstadoProducto estadoActivo = new EstadoProducto();
        estadoActivo.setId(1L);
        estadoActivo.setNombreEstado("Activo"); // O el nombre que corresponda

        // Simular cuenta origen
        CuentaAhorro cuentaOrigen = new CuentaAhorro();
        cuentaOrigen.setId(idCuentaOrigen);
        cuentaOrigen.setSaldoDisponible(new BigDecimal("200.00"));
        cuentaOrigen.setEstadoProducto(estadoActivo);

        // Simular cuenta destino
        CuentaAhorro cuentaDestino = new CuentaAhorro();
        cuentaDestino.setId(idCuentaDestino);
        cuentaDestino.setSaldoDisponible(new BigDecimal("50.00"));
        cuentaDestino.setEstadoProducto(estadoActivo);


        // Simular transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setId(1L);
        transaccion.setCuentaOrigen(cuentaOrigen);
        transaccion.setCuentaDestino(cuentaDestino);
        transaccion.setMontoTransaccion(montoPago);

        // Configurar mocks

        // Configurar mocks
        // Le dices que cuando busque "ACTIVO", devuelva el objeto 'estadoActivo' que ya creaste
        when(estadoProductoRepository.findByNombreEstadoIgnoreCase(EstadoProductoEnum.ACTIVO.name()))
                .thenReturn(Optional.of(estadoActivo));
        //Le dices a Mockito que la cuenta SÍ existe con el estado activo.
        when(cuentaAhorroRepository.existsByIdAndEstadoProducto(cuentaOrigen.getId(), estadoActivo))
                .thenReturn(true);
        //Le dices a Mockito que la cuenta SÍ existe con el estado activo.
        when(cuentaAhorroRepository.existsByIdAndEstadoProducto(cuentaDestino.getId(), estadoActivo))
                .thenReturn(true);
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaAhorroRepository.findById(idCuentaDestino)).thenReturn(Optional.of(cuentaDestino));
        when(transaccionService.registrarTransaccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(transaccion);

        // Ejecutar el método
        ResponseTransferenciaDTO resultado = transferenciaCuentaService.realizarTransferenciaEnCuentas(datosTransferenciaDTO);

        // Verificar resultados
        assertNotNull(resultado);
        assertEquals(new BigDecimal("100.00"), cuentaOrigen.getSaldoDisponible()); // 200 - 100 = 100
        assertEquals(new BigDecimal("150.00"), cuentaDestino.getSaldoDisponible()); // 50 + 100 = 150

        // Verificar que se llamaron los métodos simulados
        verify(cuentaAhorroRepository, times(1)).findById(idCuentaOrigen);
        verify(cuentaAhorroRepository, times(1)).findById(idCuentaDestino);
        verify(transaccionService, times(1)).registrarTransaccion(
                TipoTransaccion.TRANSFERENCIA,
                cuentaOrigen,
                cuentaDestino,
                null,
                null,
                montoPago,
                "Se realizo una transferencia entre cuentas"
        );
    }

    @Test
    void testRealizarTransferenciaSaldoInsuficiente() {
        // Datos de prueba
        Long idCuentaOrigen = 1L;
        Long idCuentaDestino = 2L;
        BigDecimal montoPago = new BigDecimal("300.00");

        DatosTransferenciaCuentaDTO datosTransferenciaDTO = new DatosTransferenciaCuentaDTO(idCuentaOrigen, idCuentaDestino, montoPago);

        //Simular un objeto EstadoProducto de prueba
        EstadoProducto estadoActivo = new EstadoProducto();
        estadoActivo.setId(1L);
        estadoActivo.setNombreEstado("Activo"); // O el nombre que corresponda

        // Simular cuenta origen con saldo insuficiente
        CuentaAhorro cuentaOrigen = new CuentaAhorro();
        cuentaOrigen.setId(idCuentaOrigen);
        cuentaOrigen.setSaldoDisponible(new BigDecimal("200.00"));
        cuentaOrigen.setEstadoProducto(estadoActivo);

        // Simular cuenta destino
        CuentaAhorro cuentaDestino = new CuentaAhorro();
        cuentaDestino.setId(idCuentaDestino);
        cuentaDestino.setSaldoDisponible(new BigDecimal("50.00"));
        cuentaDestino.setEstadoProducto(estadoActivo);

        // Configurar mocks
        // Le dices que cuando busque "ACTIVO", devuelva el objeto 'estadoActivo' que ya creaste
        when(estadoProductoRepository.findByNombreEstadoIgnoreCase(EstadoProductoEnum.ACTIVO.name()))
                .thenReturn(Optional.of(estadoActivo));
        //Le dices a Mockito que la cuenta SÍ existe con el estado activo.
        when(cuentaAhorroRepository.existsByIdAndEstadoProducto(cuentaOrigen.getId(), estadoActivo))
                .thenReturn(true);
        //Le dices a Mockito que la cuenta SÍ existe con el estado activo.
        when(cuentaAhorroRepository.existsByIdAndEstadoProducto(cuentaDestino.getId(), estadoActivo))
                .thenReturn(true);
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaAhorroRepository.findById(idCuentaDestino)).thenReturn(Optional.of(cuentaDestino));

        // Ejecutar el metodo y verificar excepción
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> {
                    transferenciaCuentaService.realizarTransferenciaEnCuentas(datosTransferenciaDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("La cuenta no dispone de el saldo suficiente para realizar la transferencia.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    //  (sad path).
    @Test
    void testRealizarTransferenciaEnCuentaOrigenNoEncontrada() {
        // Datos de prueba
        Long idCuentaOrigen = 1L;
        Long idCuentaDestino = 2L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosTransferenciaCuentaDTO datosTransferenciaDTO = new DatosTransferenciaCuentaDTO(idCuentaOrigen, idCuentaDestino, montoPago);

        //Simular un objeto EstadoProducto de prueba
        EstadoProducto estadoActivo = new EstadoProducto();
        estadoActivo.setId(1L);
        estadoActivo.setNombreEstado("Activo"); // O el nombre que corresponda

        // Simular cuenta origen con saldo insuficiente
        CuentaAhorro cuentaDestino = new CuentaAhorro();
        cuentaDestino.setId(idCuentaDestino);
        cuentaDestino.setSaldoDisponible(new BigDecimal("200.00"));
        cuentaDestino.setEstadoProducto(estadoActivo);

        // Configurar mock para lanzar excepción
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.empty());
//        when(cuentaAhorroRepository.findById(cuentaDestino.getId())).thenReturn(Optional.of(cuentaDestino));

        // Ejecutar el método y verificar excepción
        CuentaNotFoundException exception = assertThrows(
                CuentaNotFoundException.class,
                () -> {
                    transferenciaCuentaService.realizarTransferenciaEnCuentas(datosTransferenciaDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("La cuenta no ha sido encontrada.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    // (sad path).
    @Test
    void testRealizarTransferenciaEnCuentaDestinoNoEncontrada() {
        // Datos de prueba
        Long idCuentaOrigen = 1L;
        Long idCuentaDestino = 2L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosTransferenciaCuentaDTO datosTransferenciaDTO = new DatosTransferenciaCuentaDTO(idCuentaOrigen, idCuentaDestino, montoPago);

        // Configurar mocks
        when(cuentaAhorroRepository.findById(idCuentaOrigen)).thenReturn(Optional.empty());
//        when(cuentaAhorroRepository.findById(idCuentaDestino)).thenReturn(Optional.empty());

        // Ejecutar el metodo y verificar excepción
        CuentaNotFoundException exception = assertThrows(
                CuentaNotFoundException.class,
                () -> {
                    transferenciaCuentaService.realizarTransferenciaEnCuentas(datosTransferenciaDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("La cuenta no ha sido encontrada.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }
}