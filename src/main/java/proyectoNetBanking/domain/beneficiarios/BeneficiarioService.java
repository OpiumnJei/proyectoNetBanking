package proyectoNetBanking.domain.beneficiarios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.BeneficiarioAlreadyExistsException;
import proyectoNetBanking.infra.errors.BeneficiarioNotFoundException;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class BeneficiarioService {

    @Autowired
    private CuentaAhorroRepository cuentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    // crear un beneficiario
    public void obtenerDatosBeneficiaro(DatosBeneficiarioDTO datosBeneficiarioDTO, Long usuarioId) {

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

    //listar beneficiaros
    public List<DatosBeneficiarioDTO> listarBeneficiarios(Long usuarioId){

        // Verificar que el usuario exista
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new UsuarioNotFoundException("Usuario no encontrado");
        }

        // Obtener beneficiarios y mapearlos a DTOs
        return beneficiarioRepository.findByUsuarioId(usuarioId)// se los ids en la tabla de beneficiarios
                .stream() //se convierte la lista a un stream
                .map(beneficiario -> new DatosBeneficiarioDTO( //por cada objeto del tipo beneficiario se crea un nuevo objeto del tipo DatosBeneficiarioDTO
                        beneficiario.getNumCuentaBeneficiario(), //se mapea el numero de cuenta
                        beneficiario.getNombreBeneficiario()
                ))
                .toList(); // Convierte el stream en una lista
    }

    //eliminar un beneficiario
    public void elimininarBeneficiario(Long beneficiarioId){
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new BeneficiarioNotFoundException("Beneficiario no encontrado"));

//        if (!beneficiario.getUsuarioId().getId().equals(usuarioAutenticado.getId())) {
//            throw new UnauthorizedActionException("No tiene permiso para eliminar este beneficiario");
//        }
        beneficiarioRepository.delete(beneficiario);
    }
}
