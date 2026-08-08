package com.javanauta.usuario.business;

import com.javanauta.usuario.business.converter.UsuarioConverter;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.repository.EnderecoRepository;
import com.javanauta.usuario.infrastructure.repository.TelefoneRepository;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import com.javanauta.usuario.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioConverter usuarioConverter;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EnderecoRepository enderecoRepository;
    @Mock
    private TelefoneRepository telefoneRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveSalvarUsuarioComSenhaCriptografada() {
        UsuarioDTO dto = UsuarioDTO.builder()
                .nome("Ana")
                .email("ana@email.com")
                .senha("123")
                .build();

        Usuario usuario = Usuario.builder()
                .nome("Ana")
                .email("ana@email.com")
                .senha("encoded")
                .build();

        when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(usuarioConverter.paraUsuario(any())).thenReturn(usuario);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioConverter.paraUsuarioDTO(usuario)).thenReturn(dto);

        UsuarioDTO resultado = usuarioService.salvarUsuario(dto);

        assertEquals(dto, resultado);
        verify(passwordEncoder).encode("123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Ana")
                .email("ana@email.com")
                .senha("encoded")
                .build();

        when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(usuario));
        when(usuarioConverter.paraUsuarioDTO(usuario)).thenReturn(UsuarioDTO.builder().email("ana@email.com").build());

        UsuarioDTO resultado = usuarioService.buscarPorEmail("ana@email.com");

        assertEquals("ana@email.com", resultado.getEmail());
    }

    @Test
    void deveAtualizarUsuarioSemRecriptografarSenhaEmDuplicidade() {
        UsuarioDTO dto = UsuarioDTO.builder()
                .nome("Ana Silva")
                .senha("nova-senha")
                .build();

        Usuario entidade = Usuario.builder()
                .id(1L)
                .nome("Ana")
                .email("ana@email.com")
                .senha("senha-antiga")
                .build();

        Usuario atualizado = Usuario.builder()
                .id(1L)
                .nome("Ana Silva")
                .email("ana@email.com")
                .senha("encoded-nova-senha")
                .build();

        when(jwtUtil.extrairEmailToken("token")).thenReturn("ana@email.com");
        when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(entidade));
        when(passwordEncoder.encode("nova-senha")).thenReturn("encoded-nova-senha");
        when(usuarioConverter.updateUsuario(any(), any())).thenReturn(atualizado);
        when(usuarioRepository.save(atualizado)).thenReturn(atualizado);
        when(usuarioConverter.paraUsuarioDTO(atualizado)).thenReturn(dto);

        UsuarioDTO resultado = usuarioService.atualizaDadosUsuario("Bearer token", dto);

        assertEquals("Ana Silva", resultado.getNome());
        assertEquals("encoded-nova-senha", dto.getSenha());
        verify(passwordEncoder).encode("nova-senha");
        verify(usuarioRepository).save(atualizado);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.buscarPorEmail("inexistente@email.com"));

        assertNotNull(exception.getMessage());
    }
}
