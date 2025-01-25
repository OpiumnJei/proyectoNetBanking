package proyectoNetBanking.domain.beneficiarios;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//Beneficiario es la entidad que representa beneficiarios en el sistema, Long es el tipo de dato usado para su identificar unico Id.
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {
    //verificar si el usuario ya tiene un campo asociado con el mismo numero de cuenta, para evitar la duplicidad de beneficiarios
    boolean existsByNumCuentaBeneficiarioAndUsuarioId(String numeroCuenta, Long usuarioId);

    List<Beneficiario> findByUsuarioId(Long usuarioId);
}
