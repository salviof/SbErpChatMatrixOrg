/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UTilSBCoreInputs;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilCRCJson;
import com.super_bits.modulosSB.SBCore.modulos.ManipulaArquivo.UtilCRCArquivoTexto;
import groovy.json.JsonBuilder;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author salvio
 */
public class RepostorioDirectsAdmin {

    private static final String NOME_ARQUIVO_LISTAS
            = "directsAdmin.json";
    private static Map<String, String> MAPA_USUARIO_SALA_DIRECTS_ADMIN;

    private static String getArquivoDirects() {
        return SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getRepositorioDeArquivosExternos()
                .getCaminhoArquivosRepositorio() + "/" + NOME_ARQUIVO_LISTAS;
    }

    private static void loadDirects() {
        String arquivo = getArquivoDirects();
        if (!new File(arquivo).exists()) {
            MAPA_USUARIO_SALA_DIRECTS_ADMIN = new HashMap<>();
            return;
        }
        MAPA_USUARIO_SALA_DIRECTS_ADMIN = new HashMap<>();
        JsonObject json = UtilCRCJson.getJsonObjectByTexto(UTilSBCoreInputs.getStringByArquivoLocal(getArquivoDirects()));
        JsonArray listaDirects = json.getJsonArray(ChatMatrixOrgimpl.getCodigoUsuarioAdmin());
        for (JsonValue v : listaDirects) {
            MAPA_USUARIO_SALA_DIRECTS_ADMIN.put(v.asJsonObject().getString("user_id"),
                    v.asJsonObject().getString("room_id"));
        }
    }

    private static synchronized void persistirArquivos() {
        JsonObjectBuilder jsonB = Json.createObjectBuilder();
        JsonArrayBuilder chaveJson = Json.createArrayBuilder();
        String codigoUsuario = ChatMatrixOrgimpl.getCodigoUsuarioAdmin();
        for (String usuario : MAPA_USUARIO_SALA_DIRECTS_ADMIN.keySet()) {
            JsonObjectBuilder dadosUsuario = Json.createObjectBuilder();
            dadosUsuario.add("user_id", usuario);
            dadosUsuario.add("room_id", MAPA_USUARIO_SALA_DIRECTS_ADMIN.get(usuario));
            chaveJson.add(dadosUsuario);
        }
        jsonB.add(codigoUsuario, chaveJson);
        UtilCRCArquivoTexto.escreverEmArquivoSubstituindoArqAnterior(getArquivoDirects(), UtilCRCJson.getTextoByJsonObjeect(jsonB.build()));

    }

    public synchronized static void adicionarNovoDirect(String pUserID, String pCodigoSala) {
        MAPA_USUARIO_SALA_DIRECTS_ADMIN.put(pUserID, pCodigoSala);
        persistirArquivos();
        loadDirects();
    }

    public static String getRoomIDDirectAdminByUsuario(String pUsuario) {
        if (MAPA_USUARIO_SALA_DIRECTS_ADMIN == null || MAPA_USUARIO_SALA_DIRECTS_ADMIN.isEmpty()) {
            loadDirects();
        }
        return MAPA_USUARIO_SALA_DIRECTS_ADMIN.get(pUsuario);
    }

}
