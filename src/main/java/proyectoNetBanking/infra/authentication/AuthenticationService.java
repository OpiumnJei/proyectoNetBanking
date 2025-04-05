package proyectoNetBanking.infra.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import proyectoNetBanking.repository.UsuarioRepository;

import java.util.Collections;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //Este metodo carga los detalles del usuario desde la base de datos.
    @Override
    public UserDetails loadUserByUsername(String cedula) throws UsernameNotFoundException {

        return usuarioRepository.findByCedula(cedula) //se busca al usuario por su cedula en la bd
                .map(usuario -> new User( //se mapean los datos del usuario encontrado a un objeto User de Spring Security, que implementa UserDetails.
                        usuario.getCedula(), //cedula del usuario
                        usuario.getPassword(), // password encriptada del usuario
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().getNombreTipoUsuario().toUpperCase()) // se obtiene el rol del usuario y se convierte a mayusculas
                        )

                ))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    }

}
