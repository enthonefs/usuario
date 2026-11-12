package com.javanauta.usuario.business.converter;

import com.javanauta.usuario.business.dto.EnderecoDTO;
import com.javanauta.usuario.business.dto.TelefoneDTO;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Endereco;
import com.javanauta.usuario.infrastructure.entity.Telefone;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UsuarioConverterTest {

    private final UsuarioConverter converter = new UsuarioConverter();

    @Test
    void deveConverterUsuarioDtoEmUsuario() {
        UsuarioDTO dto = UsuarioDTO.builder()
                .nome("Maria")
                .email("maria@email.com")
                .senha("123")
                .enderecos(List.of(EnderecoDTO.builder().rua("Rua A").numero(10L).build()))
                .telefones(List.of(TelefoneDTO.builder().ddd("85").numero("999999999").build()))
                .build();

        Usuario usuario = converter.paraUsuario(dto);

        assertEquals("Maria", usuario.getNome());
        assertEquals("maria@email.com", usuario.getEmail());
        assertEquals("123", usuario.getSenha());
        assertNotNull(usuario.getEnderecos());
        assertNotNull(usuario.getTelefones());
    }

    @Test
    void deveAtualizarCamposSomenteQuandoDtoPossuiValor() {
        Usuario entity = Usuario.builder()
                .id(1L)
                .nome("Maria")
                .email("maria@email.com")
                .senha("senha-antiga")
                .build();

        UsuarioDTO dto = UsuarioDTO.builder()
                .nome("Maria Silva")
                .build();

        Usuario atualizado = converter.updateUsuario(dto, entity);

        assertEquals(1L, atualizado.getId());
        assertEquals("Maria Silva", atualizado.getNome());
        assertEquals("maria@email.com", atualizado.getEmail());
        assertEquals("senha-antiga", atualizado.getSenha());
    }

    @Test
    void deveConverterUsuarioParaUsuarioDto() {
        Usuario usuario = Usuario.builder()
                .id(2L)
                .nome("Joao")
                .email("joao@email.com")
                .senha("encoded")
                .enderecos(List.of(Endereco.builder().id(3L).rua("Rua B").build()))
                .telefones(List.of(Telefone.builder().id(4L).ddd("11").numero("988888888").build()))
                .build();

        UsuarioDTO dto = converter.paraUsuarioDTO(usuario);

        assertEquals(2L, dto.getId());
        assertEquals("Joao", dto.getNome());
        assertEquals("joao@email.com", dto.getEmail());
        assertEquals("encoded", dto.getSenha());
        assertEquals(1, dto.getEnderecos().size());
        assertEquals(1, dto.getTelefones().size());
    }
}
