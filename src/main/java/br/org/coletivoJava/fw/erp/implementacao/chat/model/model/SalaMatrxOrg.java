/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.ItemSimples;
import java.util.List;

/**
 *
 * @author salvio
 */
public class SalaMatrxOrg extends ItemSimples implements ItfChatSalaBean {

    private String codigoChat;
    private String apelido;
    private String nome;

    private String urlSala;
    private String urlSalaFull;
    private boolean existe;
    private FabTipoSalaMatrix tipoSala;

    private ItfUsuarioChat usuarioDono;

    private List<ItfUsuarioChat> usuarios;
    private List<ItfUsuarioChat> usuariosDaEmpresa;
    private List<ItfUsuarioChat> usuariosExternos;

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
    public List<ItfUsuarioChat> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<ItfUsuarioChat> usuarios) {
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

    public ItfUsuarioChat getUsuarioDono() {
        return usuarioDono;
    }

    public void setUsuarioDono(ItfUsuarioChat usuarioDono) {
        this.usuarioDono = usuarioDono;
    }

    public List<ItfUsuarioChat> getUsuariosDaEmpresa() {
        return usuariosDaEmpresa;
    }

    public void setUsuariosDaEmpresa(List<ItfUsuarioChat> usuariosDaEmpresa) {
        this.usuariosDaEmpresa = usuariosDaEmpresa;
    }

    public List<ItfUsuarioChat> getUsuariosExternos() {
        return usuariosExternos;
    }

    public void setUsuariosExternos(List<ItfUsuarioChat> usuariosExternos) {
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
