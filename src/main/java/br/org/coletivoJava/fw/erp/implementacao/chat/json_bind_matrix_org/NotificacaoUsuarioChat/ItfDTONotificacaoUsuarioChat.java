package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.NotificacaoUsuarioChat;

import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.ItfDTOSBJSON;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfNotificacaoUsuarioChat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.NotificacaoUsuarioChat.JsonBindDTONotificacaoUsuarioChat;
import java.lang.String;
import java.lang.Long;

@JsonDeserialize(using = JsonBindDTONotificacaoUsuarioChat.class)
public interface ItfDTONotificacaoUsuarioChat
        extends
        ItfDTOSBJSON,
        ItfNotificacaoUsuarioChat {

    @Override
    public default String getCodigoSalaOrigem() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getCodigoNotificacao() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getConteudo() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getRemetente() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getTipoEvento() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getEventoJson() {
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
    public default String getNomeUnicoSlug() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default Long getId() {
        return (Long) getValorPorReflexao();
    }

    @Override
    public default String getNomeDoObjetoPlural() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getNomeDoObjeto() {
        return (String) getValorPorReflexao();
    }

}
