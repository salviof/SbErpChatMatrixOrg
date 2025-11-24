package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ComoUsuarioChat;

import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.ItfDTOSBJSON;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ComoGrupoUsuario;
import java.lang.String;
import java.util.List;
import java.util.Date;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.cep.ComoLocalPostagem;

@JsonDeserialize(using = JsonBindDTOUsuarioChat.class)
public interface ItfDTOUsuarioChat extends ItfDTOSBJSON, ComoUsuarioChat {

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
    public default ComoGrupoUsuario getGrupo() {
        return (ComoGrupoUsuario) getValorPorReflexao();
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
    public default ComoLocalPostagem getLocalizacao() {
        return (ComoLocalPostagem) getValorPorReflexao();
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
