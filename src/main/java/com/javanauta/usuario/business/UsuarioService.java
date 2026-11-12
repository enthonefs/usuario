package com.javanauta.usuario.business;

import com.javanauta.usuario.business.converter.UsuarioConverter;
import com.javanauta.usuario.business.dto.EnderecoDTO;
import com.javanauta.usuario.business.dto.TelefoneDTO;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Endereco;
import com.javanauta.usuario.infrastructure.entity.Telefone;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.exceptions.ConflictException;
import com.javanauta.usuario.infrastructure.exceptions.InvalidDataException;
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
        validarDados(usuarioDTO);
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    private void validarDados(UsuarioDTO dto) {
        validarTamanho("nome", dto.getNome(), 100);
        validarTamanho("email", dto.getEmail(), 100);
        validarTamanho("senha", dto.getSenha(), 100);

        if (dto.getEnderecos() != null) {
            dto.getEnderecos().forEach(endereco -> {
                validarTamanho("endereco.complemento", endereco.getComplemento(), 10);
                validarTamanho("endereco.cidade", endereco.getCidade(), 150);
                validarTamanho("endereco.estado", endereco.getEstado(), 5);
                validarTamanho("endereco.cep", endereco.getCep(), 10);
            });
        }
        if (dto.getTelefones() != null) {
            dto.getTelefones().forEach(telefone -> {
                validarTamanho("telefone.ddd", telefone.getDdd(), 5);
                validarTamanho("telefone.numero", telefone.getNumero(), 20);
            });
        }
    }

    private void validarTamanho(String campo, String valor, int limite) {
        if (valor != null && valor.length() > limite) {
            throw new InvalidDataException(campo + " deve possuir no máximo " + limite + " caracteres.");
        }
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

        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));

    }

    public EnderecoDTO atualizaDadosEndereco(Long idEndereco, EnderecoDTO dto){
        try {
            validarEndereco(dto);
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
            validarTelefone(dto);
            Telefone telefoneExistente = telefoneRepository.findById(idTelefone).orElseThrow(
                    () -> new ResourceNotFoundException("Id de Telefone indisponível! " + idTelefone));

            Telefone telefone = usuarioConverter.updateTelefone(dto, telefoneExistente);

            return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));

        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Id de Telefone indisponível! " + idTelefone);
        }
    }

    public EnderecoDTO cadastraEndereco(String token, EnderecoDTO dto){
        validarEndereco(dto);
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email inválido! " + email));

        Endereco endereco = usuarioConverter.paraEnderecoEntity(dto, usuario.getId());
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO cadastraTelefone(String token, TelefoneDTO dto){
        validarTelefone(dto);
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email inválido! " + email));

        Telefone telefone = usuarioConverter.paraTelefoneEntity(dto, usuario.getId());
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));

    }

    private void validarEndereco(EnderecoDTO endereco) {
        if (endereco == null) throw new InvalidDataException("endereco é obrigatório.");
        validarTamanho("endereco.complemento", endereco.getComplemento(), 10);
        validarTamanho("endereco.cidade", endereco.getCidade(), 150);
        validarTamanho("endereco.estado", endereco.getEstado(), 5);
        validarTamanho("endereco.cep", endereco.getCep(), 10);
    }

    private void validarTelefone(TelefoneDTO telefone) {
        if (telefone == null) throw new InvalidDataException("telefone é obrigatório.");
        validarTamanho("telefone.ddd", telefone.getDdd(), 5);
        validarTamanho("telefone.numero", telefone.getNumero(), 20);
    }
}
