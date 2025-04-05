package proyectoNetBanking.infra.errors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
//AccessDeniedHandler: Esta interfaz se usa cuando el usuario sí está autenticado, pero no tiene permisos suficientes.
//tratar 403, cuando un usuario si esta autenticado pero no tiene acceso a ciertos metodos
public class Tratar403 implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
        response.getWriter().write("Acceso denegado: no tienes los permisos suficientes.");
    }
}
