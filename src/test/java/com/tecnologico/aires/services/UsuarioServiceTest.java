package com.tecnologico.aires.services;

import com.tecnologico.aires.models.Usuario;
import com.tecnologico.aires.models.enums.Rol;
import com.tecnologico.aires.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarNuevoCodificaLaContrasenaAntesDeGuardar() {
        Usuario usuario = new Usuario();
        usuario.setPassword("secreta");
        when(passwordEncoder.encode("secreta")).thenReturn("hash");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = usuarioService.registrarNuevo(usuario);

        assertSame(usuario, resultado);
        org.junit.jupiter.api.Assertions.assertEquals("hash", usuario.getPassword());
        verify(passwordEncoder).encode("secreta");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizarConservaLaContrasenaCuandoLosDatosLleganVacios() {
        Usuario existente = new Usuario();
        existente.setPassword("hash-anterior");
        Usuario nuevos = new Usuario();
        nuevos.setNombreCompleto("Nombre actualizado");
        nuevos.setRol(Rol.TECNICO);
        nuevos.setPassword("   ");
        when(usuarioRepository.save(existente)).thenReturn(existente);

        Usuario resultado = usuarioService.actualizar(existente, nuevos);

        assertSame(existente, resultado);
        org.junit.jupiter.api.Assertions.assertEquals("hash-anterior", existente.getPassword());
        org.junit.jupiter.api.Assertions.assertEquals("Nombre actualizado", existente.getNombreCompleto());
        org.junit.jupiter.api.Assertions.assertEquals(Rol.TECNICO, existente.getRol());
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository).save(existente);
    }

    @Test
    void actualizarCodificaUnaNuevaContrasena() {
        Usuario existente = new Usuario();
        existente.setPassword("hash-anterior");
        Usuario nuevos = new Usuario();
        nuevos.setPassword("nueva");
        when(passwordEncoder.encode("nueva")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(existente)).thenReturn(existente);

        usuarioService.actualizar(existente, nuevos);

        org.junit.jupiter.api.Assertions.assertEquals("hash-nuevo", existente.getPassword());
        verify(passwordEncoder).encode("nueva");
        verify(usuarioRepository).save(existente);
    }
}
