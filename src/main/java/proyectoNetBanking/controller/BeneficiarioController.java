package proyectoNetBanking.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyectoNetBanking.dto.beneficiarios.DatosBeneficiarioDTO;
import proyectoNetBanking.dto.beneficiarios.ResponseDatosBeneficiarioDTO;
import proyectoNetBanking.service.beneficiaros.BeneficiarioService;

@RestController
@RequestMapping("netbanking/beneficiarios")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @PostMapping("/agregar-beneficiario/{usuarioId}")
    public ResponseEntity<ResponseDatosBeneficiarioDTO> agregarBeneficiario(@PathVariable Long usuarioId,
                                                                            @RequestBody @Valid DatosBeneficiarioDTO datosBeneficiarioDTO) {

        ResponseDatosBeneficiarioDTO nuevoBeneficiario = beneficiarioService.agregarBeneficiaro(usuarioId, datosBeneficiarioDTO);
        return ResponseEntity.ok(nuevoBeneficiario);
    }

    @GetMapping("/listar-beneficiarios/{usuarioId}")
    public ResponseEntity<Page<DatosBeneficiarioDTO>> listarBeneficiarios(@PathVariable Long usuarioId,
                                                                          Pageable pageable) {

        return ResponseEntity.ok(beneficiarioService.listarBeneficiarios(usuarioId, pageable));
    }

    @DeleteMapping("/eliminar-beneficiario/{beneficiarioId}")
    public ResponseEntity<String> eliminarBeneficiario(@PathVariable Long beneficiarioId) {
        beneficiarioService.elimininarBeneficiario(beneficiarioId);
        return ResponseEntity.ok("Beneficiario eliminado exitosamente");
    }
}
