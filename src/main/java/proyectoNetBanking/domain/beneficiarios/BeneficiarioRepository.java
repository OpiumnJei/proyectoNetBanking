package proyectoNetBanking.domain.beneficiarios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//Beneficiario es la entidad que representa beneficiarios en el sistema, Long es el tipo de dato usado para su identificar unico Id.
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {
    //verificar si el usuario ya tiene un campo asociado con el mismo numero de cuenta, para evitar la duplicidad de beneficiarios
    boolean existsByNumCuentaBeneficiarioAndUsuarioId(String numeroCuenta, Long usuarioId);

    //retorna una lista de paginas de resultados del tipo Beneficiario
    //Objeto de tipo pageable en el metodo filtra automáticamente la página, el tamaño y la ordenación
    Page<Beneficiario> findByUsuarioId(Long usuarioId, Pageable pageable);
}
