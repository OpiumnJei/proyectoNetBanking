package proyectoNetBanking.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proyectoNetBanking.dto.cuentasAhorro.DatosTransferenciaCuentaDTO;
import proyectoNetBanking.dto.cuentasAhorro.ResponseTransferenciaDTO;
import proyectoNetBanking.service.cuentasAhorro.TransferenciaCuentaService;

@RestController
@RequestMapping("netbanking/")
public class TransferenciaCuentasController {

    @Autowired
    private TransferenciaCuentaService transferenciaCuentaService;

    @PostMapping("realizar-transferencia")
    public ResponseEntity<ResponseTransferenciaDTO> realizarTransferenciaCuentas(@RequestBody @Valid DatosTransferenciaCuentaDTO datosTransferenciaCuentaDTO){
        ResponseTransferenciaDTO nuevaTransferencia = transferenciaCuentaService.realizarTransferenciaEnCuentas(datosTransferenciaCuentaDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTransferencia);

    }

}
