package proyectoNetBanking.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyectoNetBanking.dto.pagos.*;
import proyectoNetBanking.service.pagos.PagoBeneficiarioService;
import proyectoNetBanking.service.pagos.PagoExpresoService;
import proyectoNetBanking.service.pagos.PagoPrestamoService;
import proyectoNetBanking.service.pagos.PagoTarjetaService;

@RestController
@RequestMapping("netbanking/cliente/pagos")
public class PagosController {

    @Autowired
    private PagoExpresoService pagoExpresoService;

    @Autowired
    private PagoTarjetaService pagoTarjetaService;

    @Autowired
    private PagoPrestamoService pagoPrestamoService;

    @Autowired
    private PagoBeneficiarioService pagoBeneficiarioService;

    @PostMapping("/realizar-pago-expreso")
    public ResponseEntity<ResponsePagoExpresoDTO> realizarPagoExpreso(@RequestBody @Valid DatosPagoExpresoDTO datosPagoExpresoDTO) {
        ResponsePagoExpresoDTO nuevoPagoPrestamo = pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPagoPrestamo);
    }

    @PostMapping("/realizar-pago-tarjeta/{tarjetaId}")
    public ResponseEntity<ResponsePagoTarjetaDTO> realizarPagoTarjeta(@PathVariable Long tarjetaId, @RequestBody @Valid DatosPagoTarjetaDTO datosPagoTarjetaDTO) {
        ResponsePagoTarjetaDTO nuevoPagoTarjeta = pagoTarjetaService.realizarPagoTarjetaCredito(tarjetaId,datosPagoTarjetaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPagoTarjeta);
    }

    @PostMapping("/realizar-pago-prestamo/{prestamoId}")
    public ResponseEntity<ResponsePagoPrestamoDTO> realizarPagoPrestamo(@PathVariable Long prestamoId, @RequestBody @Valid DatosPagoPrestamoDTO DatosPagoPrestamoDTO) {
        ResponsePagoPrestamoDTO nuevoPagoPrestamo = pagoPrestamoService.realizarPagoPrestamo(prestamoId,DatosPagoPrestamoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPagoPrestamo);
    }

    @PostMapping("/realizar-pago-beneficiario/{beneficiarioId}")
    public ResponseEntity<ResponsePagoBeneficiarioDTO> realizarPago(@PathVariable Long beneficiarioId, @RequestBody @Valid DatosPagoBeneficiarioDTO datosPagoBeneficiarioDTO) {
        ResponsePagoBeneficiarioDTO nuevoPagoBeneficiario = pagoBeneficiarioService.realizarPagoBeneficiario(beneficiarioId,datosPagoBeneficiarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPagoBeneficiario);
    }

}
