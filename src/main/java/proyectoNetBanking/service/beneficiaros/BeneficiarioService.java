package proyectoNetBanking.service.beneficiaros;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.beneficiarios.Beneficiario;
import proyectoNetBanking.repository.BeneficiarioRepository;
import proyectoNetBanking.dto.beneficiarios.DatosBeneficiarioDTO;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.repository.UsuarioRepository;
import proyectoNetBanking.infra.errors.BeneficiarioAlreadyExistsException;
import proyectoNetBanking.infra.errors.BeneficiarioNotFoundException;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;


@Service
public class BeneficiarioService {

    @Autowired
    private CuentaAhorroRepository cuentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    // crear un beneficiario
    public void validarDatosBeneficiaro(DatosBeneficiarioDTO datosBeneficiarioDTO, Long usuarioId) {

        String numeroCuenta = datosBeneficiarioDTO.numeroCuenta();
        CuentaAhorro cuentaAhorro = cuentaRepository.findByIdProducto(numeroCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("El número de cuenta: " + numeroCuenta + " no coincide con ninguna cuenta."));


        //verificar que el id del usuario exista
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNotFoundException::new);

        //verificar si el usuario ya tiene al beneficiario agregado
        var beneficiarioExistente = beneficiarioRepository.existsByNumCuentaBeneficiarioAndUsuarioId(numeroCuenta, usuarioId);

        if (beneficiarioExistente) {
            throw new BeneficiarioAlreadyExistsException("El beneficiario con número de cuenta " + numeroCuenta + " ya está registrado.");
        }

        guardarBeneficiario(datosBeneficiarioDTO, usuario);

    }

    private void guardarBeneficiario(DatosBeneficiarioDTO datosBeneficiarioDTO, Usuario usuario) {

        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setUsuario(usuario);
        beneficiario.setNumCuentaBeneficiario(datosBeneficiarioDTO.numeroCuenta());
        beneficiario.setNombreBeneficiario(datosBeneficiarioDTO.nombreBeneficiario());
        beneficiarioRepository.save(beneficiario);
    }

    //listar beneficiaros usando la paginacion
    //pageable permite espeficicar que cantidad de datos deben ser devueltos, tanto la forma en como los datos deben ordenarse, etc
    public Page<DatosBeneficiarioDTO> listarBeneficiarios(Long usuarioId, Pageable pageable){

        // Verificar que el usuario exista
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new UsuarioNotFoundException("Usuario no encontrado");
        }

        // paginas de beneficiarios
        Page<Beneficiario> beneficiarios = beneficiarioRepository.findByUsuarioId(usuarioId, pageable);

        // se retornan los datos mapeados que contienen la informacion de la pagina actual
        return beneficiarios.map(beneficiario -> new DatosBeneficiarioDTO(
                beneficiario.getNombreBeneficiario(),
                beneficiario.getNumCuentaBeneficiario()
        ));
    }

    //eliminar un beneficiario
    public void elimininarBeneficiario(Long beneficiarioId){
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new BeneficiarioNotFoundException("Beneficiario no encontrado"));

        beneficiarioRepository.delete(beneficiario);
    }
}
