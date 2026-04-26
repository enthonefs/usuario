package com.javanauta.usuario.business;

import com.javanauta.usuario.business.converter.UsuarioConverter;
import com.javanauta.usuario.business.dtos.EnderecoDTO;
import com.javanauta.usuario.business.dtos.TelefoneDTO;
import com.javanauta.usuario.business.dtos.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Endereco;
import com.javanauta.usuario.infrastructure.entity.Telefone;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.exceptions.ConflictException;
import com.javanauta.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.javanauta.usuario.infrastructure.repository.EnderecoRepository;
import com.javanauta.usuario.infrastructure.repository.TelefoneRepository;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import com.javanauta.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

    public UsuarioDTO salvarUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email){
        boolean existe = verificaEmailExistente(email);
        try {
            if (existe){
                throw new ConflictException("Email já existe! " + email);
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email já existe!" + e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTO buscarPorEmail(String email){
        try {
            Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                    () -> new ResourceNotFoundException("Usuário não encontrado! " + email));
            return usuarioConverter.paraUsuarioDTO(usuario);

        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Email inválido! " + email);
        }
    }

    public void deletarPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não localizado!"));

        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);
        usuario.setSenha(passwordEncoder.encode(usuario.getPassword()));

        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));

    }

    public EnderecoDTO atualizaDadosEndereco(Long idEndereco, EnderecoDTO dto){
        try {
            Endereco enderecoExistente = enderecoRepository.findById(idEndereco).orElseThrow(
                    () -> new ResourceNotFoundException("Id de Endereço indisponível! " + idEndereco));

            Endereco endereco = usuarioConverter.updateEndereco(dto, enderecoExistente);

            return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));

        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Id de Endereço indisponível! " + idEndereco);
        }
    }

    public TelefoneDTO atualizaDadosTelefone(Long idTelefone, TelefoneDTO dto){
        try {
            Telefone telefoneExistente = telefoneRepository.findById(idTelefone).orElseThrow(
                    () -> new ResourceNotFoundException("Id de Telefone indisponível! " + idTelefone));

            Telefone telefone = usuarioConverter.updateTelefone(dto, telefoneExistente);

            return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));

        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Id de Telefone indisponível! " + idTelefone);
        }
    }

}
