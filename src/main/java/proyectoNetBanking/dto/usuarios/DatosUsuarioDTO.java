package proyectoNetBanking.dto.usuarios;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Valid
public record DatosUsuarioDTO(
        @NotBlank(message = "El nombre del usuario es un campo requerido.")
        String nombre,
        @NotBlank(message = "El apellido del usuario es un campo requerido.")
        String apellido,
        @NotBlank(message = "La cedula del usuario es un campo requerido.")
        @Pattern(regexp = "\\d{11}", message = "La cédula debe contener 11 dígitos numéricos.") //usamos una expresion regular para controlar la cantidad de digitos de la cedula
        String cedula,
        @NotBlank
        @Email(message = "El correo electronico es obligatorio")
        String correo,
        @NotBlank(message = "La contrasenia es un campo obligatorio")
        @Size(min = 5, message = "La contraseña debe tener al menos 5 caracteres.")
        String password,
        @NotBlank(message = "Introduzca un tipo de usuario valido.")
        String tipoUsuario,
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto inicial no puede ser menor a cero")//se usa inclusive para asegurarnos se acepte el valor inicial como un cero
        BigDecimal montoInicial)
{
    //constructor para gestionar los datos proporcionados por el usuario
    public DatosUsuarioDTO(String nombre, String apellido, String cedula, String correo, String password, String tipoUsuario, BigDecimal montoInicial) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.correo = correo;
        this.password = password;
        this.tipoUsuario = tipoUsuario.toUpperCase();
        this.montoInicial = montoInicial != null ? montoInicial : BigDecimal.ZERO;//ZERO = 0
    }
}
