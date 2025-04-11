package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ChatSalaBean;

import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.ItfDTOSBJSON;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ChatSalaBean.JsonBindDTOChatSalaBean;
import java.lang.String;
import java.util.List;

@JsonDeserialize(using = JsonBindDTOChatSalaBean.class)
public interface ItfDTOChatSalaBean extends ItfDTOSBJSON, ItfChatSalaBean {

    @Override
    public default String getApelido() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default List getUsuarios() {
        return (List) getValorPorReflexao();
    }

    @Override
    public default String getUrlSalaFull() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getUrlSala() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default String getCodigoChat() {
        return (String) getValorPorReflexao();
    }

    @Override
    public default boolean isExiste() {
        return (boolean) getValorPorReflexao();
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
    public default String getNome() {
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
}
