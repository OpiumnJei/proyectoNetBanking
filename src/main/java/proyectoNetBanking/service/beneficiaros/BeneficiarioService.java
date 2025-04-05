package proyectoNetBanking.service.beneficiaros;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.beneficiarios.Beneficiario;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.dto.beneficiarios.DatosBeneficiarioDTO;
import proyectoNetBanking.dto.beneficiarios.ResponseDatosBeneficiarioDTO;
import proyectoNetBanking.infra.errors.*;
import proyectoNetBanking.repository.BeneficiarioRepository;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.repository.UsuarioRepository;


@Service
public class BeneficiarioService {

    @Autowired
    private CuentaAhorroRepository cuentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    // crear un beneficiario
    public ResponseDatosBeneficiarioDTO agregarBeneficiaro (Long usuarioId, DatosBeneficiarioDTO datosBeneficiarioDTO) {

        //verificar que el id del usuario exista
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNotFoundException::new);

        String numeroCuenta = datosBeneficiarioDTO.numeroCuenta();

        CuentaAhorro cuentaAhorro = cuentaRepository.findByIdProducto(numeroCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("El número de cuenta: " + numeroCuenta + " no coincide con ninguna cuenta."));

        //verificar si el usuario ya tiene al beneficiario agregado
        var beneficiarioExistente = beneficiarioRepository.existsByNumCuentaBeneficiarioAndUsuarioId(numeroCuenta, usuarioId);

        if (beneficiarioExistente) {
            throw new BeneficiarioAlreadyExistsException("El beneficiario con número de cuenta " + numeroCuenta + " ya está registrado.");
        }

      Beneficiario beneficiarioGuardado  = guardarBeneficiario(usuario, datosBeneficiarioDTO);

        return new ResponseDatosBeneficiarioDTO(
                beneficiarioGuardado.getUsuario().getId(),
                beneficiarioGuardado.getNumCuentaBeneficiario(),
                beneficiarioGuardado.getNombreBeneficiario()
        );

    }

    private Beneficiario guardarBeneficiario(Usuario usuario, DatosBeneficiarioDTO datosBeneficiarioDTO) {

        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setUsuario(usuario);
        beneficiario.setNumCuentaBeneficiario(datosBeneficiarioDTO.numeroCuenta());
        beneficiario.setNombreBeneficiario(datosBeneficiarioDTO.nombreBeneficiario());
        return beneficiarioRepository.save(beneficiario);
    }

    //listar beneficiaros usando la paginacion
    //pageable permite espeficicar que cantidad de datos deben ser devueltos, tanto la forma en como los datos deben ordenarse, etc
    public Page<DatosBeneficiarioDTO> listarBeneficiarios(Long usuarioId, Pageable pageable){

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));

        if(!usuario.isActivo()){
            throw new UsuarioInactivoException("El usuario introducido se encuentra inactivo");
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
