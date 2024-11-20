package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.UsuarioChat;

import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.DTO_SB_JSON_PROCESSADOR_GENERICO;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreJson;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.io.IOException;

public class JsonBindDTOUsuarioChat
        extends
        DTO_SB_JSON_PROCESSADOR_GENERICO<DTOUsuarioChat> {

    public JsonBindDTOUsuarioChat() {
        super(DTOUsuarioChat.class);
    }

    @Override
    public DTOUsuarioChat deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JacksonException {

        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);

        DTOUsuarioChat dto = new DTOUsuarioChat();

        adicionarPropriedadeString("imgPequena", node, "avatar_url");
        adicionarPropriedadeInteiro("id", node, "name");
        adicionarPropriedadeString("codigoUsuario", node, "name");
        adicionarPropriedadeString("nome", node, "displayname");
        adicionarPropriedadeString("apelido", node, "username");
        JsonObject json = UtilSBCoreJson.getJsonObjectByTexto(node.toString());
        if (json.containsKey("threepids")) {
            if (!json.getJsonArray("threepids").isEmpty()) {

                JsonArray identificacoesTerceiros = json.getJsonArray("threepids");

                for (Object id : identificacoesTerceiros) {
                    JsonObject idJsonTrp = (JsonObject) id;
                    System.out.println(id.getClass().getSimpleName());
                    if (idJsonTrp.getString("medium").equals("email")) {
                        String email = idJsonTrp.getString("address");
                        getObjectBuilder().add("email", email);
                        getObjectBuilder().add("emailprincipal", email);
                    }
                    if (idJsonTrp.getString("medium").equals("msisdn")) {
                        String telefone = idJsonTrp.getString("address");
                        getObjectBuilder().add("telefone", telefone);
                        getObjectBuilder().add("telefonePrincipal", telefone);
                    }

                }

            }
        }
        selarProcesamento(dto);
        return dto;
    }

}
