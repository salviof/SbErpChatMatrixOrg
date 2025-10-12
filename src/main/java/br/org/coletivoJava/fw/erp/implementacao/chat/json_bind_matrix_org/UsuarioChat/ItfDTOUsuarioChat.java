package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.UsuarioChat;

import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.ItfDTOSBJSON;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.UsuarioChat.JsonBindDTOUsuarioChat;
import java.lang.String;
import java.util.List;
import java.util.Date;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ItfGrupoUsuario;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.cep.ItfLocalPostagem;

@JsonDeserialize(using = JsonBindDTOUsuarioChat.class)
public interface ItfDTOUsuarioChat extends ItfDTOSBJSON, ItfUsuarioChat {

    @Override
    public default String getCodigoUsuario() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default List getEmailsSecundarios() {
        return (List) getValorPorReflexao();
    }

    @Override
    public default String getEmailPrincipal() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getNome() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getSenha() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default Date getDataCadastro() {
        return (Date) getValorPorReflexao();
    }

    @Override
    public default String getApelido() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default boolean isAtivo() {
        return (boolean) getValorPorReflexao();
    }

    @Override
    public default String getTipoUsuario() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default ItfGrupoUsuario getGrupo() {
        return (ItfGrupoUsuario) getValorPorReflexao();
    }

    @Override
    public default List getGruposAdicionais() {
        return (List) getValorPorReflexao();
    }

    @Override
    public default String getEmail() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default ItfLocalPostagem getLocalizacao() {
        return (ItfLocalPostagem) getValorPorReflexao();
    }

    @Override
    public default String getTelefone() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getNomeLongo() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getDescritivo() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default List getGaleria() {
        return (List) getValorPorReflexao();
    }

    @Override
    public default String getNomeUnicoSlug() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default boolean isTemImagemPequenaAdicionada() {
        return (boolean) getValorPorReflexao();
    }

    @Override
    public default String getSlugIdentificador() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getIconeDaClasse() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getNomeCurto() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default Long getId() {
        return (long) getValorPorReflexao();
    }

    @Override
    public default String getCodigoCRMUniversal() {
        return (String) getValorPorReflexao();
    }

}
