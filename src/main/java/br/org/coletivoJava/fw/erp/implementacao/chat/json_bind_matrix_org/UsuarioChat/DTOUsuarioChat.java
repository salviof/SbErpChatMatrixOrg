package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.UsuarioChat;

import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.DTO_SBGENERICO;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ItfGrupoUsuario;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.cep.ItfLocal;

public class DTOUsuarioChat extends DTO_SBGENERICO<ItfDTOUsuarioChat>
        implements
        ItfDTOUsuarioChat {

    public DTOUsuarioChat(String pJson) {
        super(JsonBindDTOUsuarioChat.class, pJson);
    }

    public DTOUsuarioChat() {
        super(null, null);
    }

    @Override
    public void setGrupo(ItfGrupoUsuario grupo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void instanciarNovoEndereco() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setLocalizacao(ItfLocal pLocal) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setNomeLongo(String pnomeLongo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setDescritivo(String pDescritivo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setAtivo(boolean pAtivo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getImgMedia() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isTemImagemMedioAdicionada() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getImgGrande() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isTemImagemGrandeAdicionada() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
