package com.mintgestao.Application.Service.Autenticacao;

import com.mintgestao.Application.Service.Token.TokenService;
import com.mintgestao.Domain.DTO.Login.LoginRequestDTO;
import com.mintgestao.Domain.DTO.Login.LoginResponseDTO;
import com.mintgestao.Domain.Entity.Usuario;
import com.mintgestao.Infrastructure.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements IAutenticacaoService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public LoginResponseDTO entrar(LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
        var auth = authenticationManager.authenticate(usernamePassword);
        var token = tokenService.gerarToken((Usuario) auth.getPrincipal());
        var refreshToken = tokenService.gerarRefreshToken((Usuario) auth.getPrincipal());

        Usuario usuario = (Usuario) repository.findByEmail(data.email());
        return new LoginResponseDTO(usuario, token, refreshToken);
    }

    @Override
    public Boolean registrar(Usuario usuario) {
        if (this.repository.findByEmail(usuario.getEmail()) != null) return false;
        String senhaCriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
        repository.save(usuario);
        return true;
    }

    @Override
    public String atualizarToken(String refreshToken) {
        Usuario usuario = tokenService.validarRefreshToken(refreshToken);
        UserDetails user = repository.findByEmail(usuario.getEmail());
        return tokenService.gerarToken((Usuario) user);
    }
}