package proyectoNetBanking.domain.tarjetasCredito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class TarjetaCreditoService {

    //monto minimo para el limite de credito
    private final BigDecimal MONTO_MINIMO_LIMITE_CREDITO = BigDecimal.valueOf(2000);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Transactional
    public void crearTarjetaCredito(Long usuarioId, DatosTarjetaDTO datosTarjetaDTO){

        if (usuarioId == null || usuarioId <= 0) { //validar que numero no sea negativo ni nulo
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo ni un número negativo");
        }

        //verificar que el usuario exista en la bd
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException());

        //validar que el limite de credito sea mayor o igual 2000
        if (datosTarjetaDTO.limiteCredito().compareTo(MONTO_MINIMO_LIMITE_CREDITO) < 0) {
            throw new RuntimeException("El limite de credito introducido es menor al monto minimo aceptado.");
        }

        asignarTarjetaCredito(usuario, datosTarjetaDTO.limiteCredito());
    }

    private void asignarTarjetaCredito(Usuario usuario,  BigDecimal limiteCredito) {

        TarjetaCredito tarjetaUsuario = new TarjetaCredito();
        tarjetaUsuario.setLimiteCredito(limiteCredito);
        tarjetaUsuario.setCreditoDisponible(limiteCredito);
        tarjetaUsuario.setUsuario(usuario);
        tarjetaRepository.save(tarjetaUsuario);
    }


}
