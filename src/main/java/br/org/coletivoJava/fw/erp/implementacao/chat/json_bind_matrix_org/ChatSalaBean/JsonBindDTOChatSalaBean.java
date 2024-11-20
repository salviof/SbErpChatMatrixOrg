package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ChatSalaBean;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.UtilMatrixERP;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.UsuarioChat.DTOUsuarioChat;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.DTO_SB_JSON_PROCESSADOR_GENERICO;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestIntMatrixChatUsuarios;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreJson;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.WS.conexaoWebServiceClient.ItfRespostaWebServiceSimples;
import com.super_bits.modulosSB.SBCore.modulos.erp.ErroJsonInterpredador;
import jakarta.json.JsonString;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

public class JsonBindDTOChatSalaBean
        extends
        DTO_SB_JSON_PROCESSADOR_GENERICO<DTOChatSalaBean> {

    public JsonBindDTOChatSalaBean() {
        super(DTOChatSalaBean.class);
    }

    @Override
    public DTOChatSalaBean deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JacksonException {
        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);
        DTOChatSalaBean dto = new DTOChatSalaBean();
        adicionarPropriedadeInteiro("id", node, "room_id");
        adicionarPropriedadeString("codigoChat", node, "room_id");
        adicionarPropriedadeString("nome", node, "name");
        adicionarPropriedadeString("urlSala", node, "");
        if (!node.get("canonical_alias").isNull()) {
            String apelido = node.get("canonical_alias").asText();
            getObjectBuilder().add("apelido", apelido);
        } else {
            adicionarPropriedadeString("apelido", node, "name");
        }

        ///_synapse/admin/v1/rooms/<room_id>/members
        String roomId = node.get("room_id").asText();
        String nome = node.get("name").asText();
        ItfRespostaWebServiceSimples respUsuaarioSalaRest = FabApiRestIntMatrixChatUsuarios.USUARIOS_DA_SALA.getAcao(roomId).getResposta();
        List<ItfUsuarioChat> usuarios = new ArrayList<>();

        respUsuaarioSalaRest.getRespostaComoObjetoJson().getJsonArray("members").stream().forEach(us -> {
            try {
                JsonString codigoUsuario = (JsonString) us;
                ItfRespostaWebServiceSimples respDadpsUsuario = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getAcao(codigoUsuario.getString()).getResposta();
                DTOUsuarioChat usuario = (DTOUsuarioChat) ERPChat.MATRIX_ORG.getDTO(UtilSBCoreJson.getTextoByJsonObjeect(respDadpsUsuario.getRespostaComoObjetoJson()), ItfUsuarioChat.class);
                usuarios.add(usuario);
            } catch (ErroJsonInterpredador ex) {
                SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha processando json de usuaŕio", ex);
            }
        });
        adicionarListas("usuarios", usuarios);

        //adicionarPropriedadeListaObjetos(ItfUsuarioChat.class, "", node, "usaurios")
        selarProcesamento(dto);
        return dto;
    }
}
