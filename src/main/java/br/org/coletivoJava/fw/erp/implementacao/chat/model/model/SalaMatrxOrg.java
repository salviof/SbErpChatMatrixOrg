/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ComoChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import com.super_bits.modulosSB.SBCore.modulos.objetos.InfoCampos.anotacoes.InfoObjetoSB;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.ItemSimples;
import java.util.List;

/**
 *
 * @author salvio
 */
@InfoObjetoSB(plural = "Salas MAtrix", tags = "Sala MAtrix")
public class SalaMatrxOrg extends ItemSimples implements ComoChatSalaBean {

    private String codigoChat;
    private String apelido;
    private String nome;

    private String urlSala;
    private String urlSalaFull;
    private boolean existe;
    private FabTipoSalaMatrix tipoSala;

    private ComoUsuarioChat usuarioDono;

    private List<ComoUsuarioChat> usuarios;
    private List<ComoUsuarioChat> usuariosDaEmpresa;
    private List<ComoUsuarioChat> usuariosExternos;

    @Override
    public String getUrlSala() {
        return urlSala;
    }

    public void setUrlSala(String urlSala) {
        this.urlSala = urlSala;
    }

    @Override
    public String getUrlSalaFull() {
        return urlSalaFull;
    }

    public void setUrlSalaFull(String urlSalaFull) {
        this.urlSalaFull = urlSalaFull;
    }

    @Override
    public boolean isExiste() {
        if (codigoChat == null) {
            existe = false;
        }
        return existe;
    }

    public void setExiste(boolean existe) {
        this.existe = existe;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public List<ComoUsuarioChat> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<ComoUsuarioChat> usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public String getCodigoChat() {
        return codigoChat;
    }

    public void setCodigoChat(String codigoChat) {
        this.codigoChat = codigoChat;
    }

    public FabTipoSalaMatrix getTipoSala() {
        return tipoSala;
    }

    public void setTipoSala(FabTipoSalaMatrix tipoSala) {
        this.tipoSala = tipoSala;
    }

    public ComoUsuarioChat getUsuarioDono() {
        return usuarioDono;
    }

    public void setUsuarioDono(ComoUsuarioChat usuarioDono) {
        this.usuarioDono = usuarioDono;
    }

    public List<ComoUsuarioChat> getUsuariosDaEmpresa() {
        return usuariosDaEmpresa;
    }

    public void setUsuariosDaEmpresa(List<ComoUsuarioChat> usuariosDaEmpresa) {
        this.usuariosDaEmpresa = usuariosDaEmpresa;
    }

    public List<ComoUsuarioChat> getUsuariosExternos() {
        return usuariosExternos;
    }

    public void setUsuariosExternos(List<ComoUsuarioChat> usuariosExternos) {
        this.usuariosExternos = usuariosExternos;
    }

    @Override
    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

}
