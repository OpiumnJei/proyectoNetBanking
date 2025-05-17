package proyectoNetBanking.service.correos;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {


    @Value("${resend.api.key}") //ojo se debe crear una variable de entorno como buena practica
    private String apiKey;

    private Resend resend;

    // se debe ejecutar automáticamente después de que la apiKey ha sido creada e inyectada por Spring
    @PostConstruct
    public void init() {
        resend = new Resend(apiKey);
    }

    public void enviarCorreoBienvenida(String correoUsuario, String nombreUsuario) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Netbanking Api <onboarding@resend.dev>")
                .to(correoUsuario) //se usa el correo registrado en resend, ya que si no se tiene un dominio comprado, debe usar ese correo para pruebas.
                .subject("¡Bienvenido a NetBanking!")
                .html("<h1>Hola " + nombreUsuario + " 👋</h1><p>¡Gracias por registrarte en nuestro sistema NetBanking !</p>")
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println(data.getId());
        } catch (ResendException e) {
            e.printStackTrace();
        }
    }
}


