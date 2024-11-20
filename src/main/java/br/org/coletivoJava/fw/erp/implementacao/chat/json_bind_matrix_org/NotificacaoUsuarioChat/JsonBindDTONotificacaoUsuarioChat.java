package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.NotificacaoUsuarioChat;

import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.DTO_SB_JSON_PROCESSADOR_GENERICO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class JsonBindDTONotificacaoUsuarioChat
        extends
        DTO_SB_JSON_PROCESSADOR_GENERICO<DTONotificacaoUsuarioChat> {

    public JsonBindDTONotificacaoUsuarioChat() {
        super(DTONotificacaoUsuarioChat.class);
    }

    @Override
    public DTONotificacaoUsuarioChat deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);
        DTONotificacaoUsuarioChat dto = new DTONotificacaoUsuarioChat();

        JsonNode dadosEvento = node.get("event");
        adicionarPropriedadeString("codigoNotificacao", dadosEvento, "type");
        adicionarPropriedadeString("codigoNotificacao", dadosEvento, "event_id");

        adicionarPropriedadeString("remetente", dadosEvento, "sender");
        JsonNode dadosConteudo = dadosEvento.get("content");
        adicionarPropriedadeString("conteudo", dadosConteudo, "body");
        adicionarPropriedadeString("codigoSalaOrigem", node, "room_id");
        String codigocompleto = dadosEvento.get("event_id").asText();
        getObjectBuilder().add("id", codigocompleto.hashCode());
        selarProcesamento(dto);
        return dto;
    }

}
