package br.org.coletivoJava.fw.erp.implementacao.chat.atendente;

import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestIntMatrixChatUsuarios;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.token.ItfTokenGestao;
import com.super_bits.modulosSB.SBCore.modulos.Mensagens.FabMensagens;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * /sync próprio do usuário admin para a escuta de atendentes.
 *
 * Independente de SincronizacaoAbstrata (integração com o Whatsapp): não abre
 * sessões de sala, não processa comandos e não persiste o since. O primeiro
 * /sync só carrega o estado das salas (membros, nome e alias), sem notificar,
 * então um restart não gera notificações de mensagens antigas.
 *
 * @author salvio
 */
class SincronizacaoAtendentesMatrix extends Thread {

    private static final int TIMEOUT_LONG_POLLING = 30000;
    private static final int TIMEOUT_CONEXAO = 15000;
    /**
     * A carga inicial traz o estado de todas as salas do admin e pode demorar.
     */
    private static final int TIMEOUT_LEITURA_CARGA_INICIAL = 300000;
    private static final int TIMEOUT_LEITURA = TIMEOUT_LONG_POLLING + 30000;
    private static final long ESPERA_MAXIMA_APOS_FALHA = 60000;
    private static final int TAMANHO_MAXIMO_TEXTO = 500;

    private static final String FILTRO_CARGA_INICIAL = gerarFiltro(1);
    private static final String FILTRO = gerarFiltro(50);

    private final String host;
    private volatile boolean ativo = true;
    private String since;
    private String token;
    private int falhasConsecutivas;

    SincronizacaoAtendentesMatrix() {
        super("matrix-sync-atendentes");
        setDaemon(true);
        String url = SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getPropriedade(FabConfigApiMatrixChat.URL_MATRIX_SERVER);
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        host = url;
    }

    private static String gerarFiltro(int pLimiteTimeline) {
        JSONObject semTipos = new JSONObject().put("types", new JSONArray());
        JSONArray camposEvento = new JSONArray()
                .put("type").put("content").put("sender").put("state_key")
                .put("event_id").put("origin_server_ts");
        JSONObject sala = new JSONObject()
                .put("account_data", semTipos)
                .put("state", new JSONObject()
                        .put("types", new JSONArray().put("m.room.member").put("m.room.name").put("m.room.canonical_alias")))
                .put("timeline", new JSONObject()
                        .put("limit", pLimiteTimeline)
                        .put("types", new JSONArray().put("m.room.message").put("m.reaction")
                                .put("m.room.member").put("m.room.name").put("m.room.canonical_alias")))
                .put("ephemeral", new JSONObject()
                        .put("types", new JSONArray().put("m.receipt")));
        return new JSONObject()
                .put("event_fields", camposEvento)
                .put("presence", semTipos)
                .put("account_data", semTipos)
                .put("room", sala)
                .toString();
    }

    void encerrar() {
        ativo = false;
        interrupt();
    }

    @Override
    public void run() {
        CentralAtendentesMatrix.log(FabMensagens.AVISO, "Sincronização dos atendentes iniciada em " + host);
        while (ativo) {
            try {
                if (token == null) {
                    token = obterToken(false);
                    CentralAtendentesMatrix.setCodigoAdmin(ChatMatrixOrgimpl.getCodigoUsuarioAdmin());
                }
                boolean cargaInicial = since == null;
                StringBuilder url = new StringBuilder(host).append("/_matrix/client/v3/sync?filter=")
                        .append(URLEncoder.encode(cargaInicial ? FILTRO_CARGA_INICIAL : FILTRO, "UTF-8"));
                if (!cargaInicial) {
                    url.append("&timeout=").append(TIMEOUT_LONG_POLLING)
                            .append("&since=").append(URLEncoder.encode(since, "UTF-8"));
                }
                String[] resposta = get(url.toString(), cargaInicial ? TIMEOUT_LEITURA_CARGA_INICIAL : TIMEOUT_LEITURA);
                int codigoHttp = Integer.parseInt(resposta[0]);
                String corpo = resposta[1];

                if (codigoHttp == 401 || corpo.contains("M_UNKNOWN_TOKEN")) {
                    CentralAtendentesMatrix.log(FabMensagens.ALERTA, "Token do admin recusado, gerando novo token");
                    token = obterToken(true);
                    continue;
                }
                if (codigoHttp != 200) {
                    throw new IOException("Matrix respondeu http=" + codigoHttp + " " + resumo(corpo));
                }

                JSONObject json = new JSONObject(corpo);
                processar(json, cargaInicial);
                since = json.getString("next_batch");
                if (cargaInicial) {
                    CentralAtendentesMatrix.log(FabMensagens.AVISO, "Carga inicial concluída, salas com atendentes="
                            + CentralAtendentesMatrix.getTotalSalasMonitoradas());
                }
                falhasConsecutivas = 0;
                CentralAtendentesMatrix.limparEscutasInativas();
            } catch (InterruptedException e) {
                break;
            } catch (Throwable t) {
                if (!ativo) {
                    break;
                }
                falhasConsecutivas++;
                long espera = Math.min(ESPERA_MAXIMA_APOS_FALHA, 2000L << Math.min(falhasConsecutivas, 5));
                CentralAtendentesMatrix.log(FabMensagens.ERRO, "Falha na sincronização dos atendentes ("
                        + falhasConsecutivas + "x), nova tentativa em " + espera + "ms: "
                        + t.getClass().getName() + ": " + t.getMessage());
                try {
                    Thread.sleep(espera);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        CentralAtendentesMatrix.log(FabMensagens.AVISO, "Sincronização dos atendentes encerrada");
    }

    private String obterToken(boolean pForcarNovo) {
        ItfTokenGestao gestaoToken = FabApiRestIntMatrixChatUsuarios.USUARIOS_DA_SALA.getGestaoToken();
        if (pForcarNovo || !gestaoToken.validarToken()) {
            gestaoToken.excluirToken();
            gestaoToken.gerarNovoToken();
        }
        if (!gestaoToken.isTemTokemAtivo()) {
            throw new IllegalStateException("Não foi possível obter o token do usuário admin do Matrix");
        }
        return gestaoToken.getToken();
    }

    private String[] get(String pUrl, int pTimeoutLeitura) throws IOException, InterruptedException {
        HttpURLConnection conexao = (HttpURLConnection) new URL(pUrl).openConnection();
        try {
            conexao.setConnectTimeout(TIMEOUT_CONEXAO);
            conexao.setReadTimeout(pTimeoutLeitura);
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setRequestProperty("Accept", "application/json");
            int codigo = conexao.getResponseCode();
            InputStream entrada = codigo >= 400 ? conexao.getErrorStream() : conexao.getInputStream();
            String corpo = "";
            if (entrada != null) {
                try (InputStream in = entrada) {
                    ByteArrayOutputStream saida = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int lidos;
                    while ((lidos = in.read(buffer)) != -1) {
                        saida.write(buffer, 0, lidos);
                    }
                    corpo = new String(saida.toByteArray(), StandardCharsets.UTF_8);
                }
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            return new String[]{String.valueOf(codigo), corpo};
        } finally {
            conexao.disconnect();
        }
    }

    private void processar(JSONObject pJson, boolean pCargaInicial) {
        JSONObject salas = pJson.optJSONObject("rooms");
        if (salas == null) {
            return;
        }
        JSONObject salasAtivas = salas.optJSONObject("join");
        if (salasAtivas != null) {
            Iterator<String> codigos = salasAtivas.keys();
            while (codigos.hasNext()) {
                String codigoSala = codigos.next();
                JSONObject sala = salasAtivas.getJSONObject(codigoSala);
                for (JSONObject evento : getEventos(sala, "state")) {
                    processarEstado(codigoSala, evento);
                }
                for (JSONObject evento : getEventos(sala, "timeline")) {
                    if (evento.has("state_key")) {
                        processarEstado(codigoSala, evento);
                    } else if (!pCargaInicial) {
                        processarTimeline(codigoSala, evento);
                    }
                }
                if (!pCargaInicial) {
                    for (JSONObject evento : getEventos(sala, "ephemeral")) {
                        if ("m.receipt".equals(evento.optString("type"))) {
                            processarLeitura(codigoSala, evento.optJSONObject("content"));
                        }
                    }
                }
            }
        }
        JSONObject salasDeixadas = salas.optJSONObject("leave");
        if (salasDeixadas != null) {
            Iterator<String> codigos = salasDeixadas.keys();
            while (codigos.hasNext()) {
                CentralAtendentesMatrix.removerSala(codigos.next());
            }
        }
    }

    private static Iterable<JSONObject> getEventos(JSONObject pSala, String pSecao) {
        JSONObject secao = pSala.optJSONObject(pSecao);
        JSONArray eventos = secao == null ? null : secao.optJSONArray("events");
        List<JSONObject> lista = new ArrayList<>();
        if (eventos != null) {
            for (int i = 0; i < eventos.length(); i++) {
                JSONObject evento = eventos.optJSONObject(i);
                if (evento != null) {
                    lista.add(evento);
                }
            }
        }
        return lista;
    }

    private void processarEstado(String pSala, JSONObject pEvento) {
        JSONObject conteudo = pEvento.optJSONObject("content");
        if (conteudo == null) {
            conteudo = new JSONObject();
        }
        switch (pEvento.optString("type")) {
            case "m.room.member":
                CentralAtendentesMatrix.atualizarMembro(pSala, pEvento.optString("state_key", null),
                        conteudo.optString("membership", null), conteudo.optString("displayname", null));
                break;
            case "m.room.name":
                CentralAtendentesMatrix.atualizarNomeSala(pSala, conteudo.optString("name", null));
                break;
            case "m.room.canonical_alias":
                CentralAtendentesMatrix.atualizarAliasSala(pSala, conteudo.optString("alias", null));
                break;
            default:
                break;
        }
    }

    private void processarTimeline(String pSala, JSONObject pEvento) {
        String remetente = pEvento.optString("sender", null);
        switch (pEvento.optString("type")) {
            case "m.room.message":
                JSONObject conteudo = pEvento.optJSONObject("content");
                if (conteudo == null || isEdicao(conteudo)) {
                    return;
                }
                CentralAtendentesMatrix.mensagemRecebida(pSala, remetente, pEvento.optString("event_id", null),
                        extrairTexto(conteudo), pEvento.optLong("origin_server_ts", System.currentTimeMillis()));
                break;
            case "m.reaction":
                CentralAtendentesMatrix.reacaoRecebida(pSala, remetente);
                break;
            default:
                break;
        }
    }

    /**
     * content: { "$eventId": { "m.read": { "@usuario:dominio": { "ts": 123 } } } }
     */
    private void processarLeitura(String pSala, JSONObject pConteudo) {
        if (pConteudo == null) {
            return;
        }
        Iterator<String> eventos = pConteudo.keys();
        while (eventos.hasNext()) {
            JSONObject recibos = pConteudo.optJSONObject(eventos.next());
            JSONObject leituras = recibos == null ? null : recibos.optJSONObject("m.read");
            if (leituras == null) {
                continue;
            }
            Iterator<String> usuarios = leituras.keys();
            while (usuarios.hasNext()) {
                String usuario = usuarios.next();
                JSONObject leitura = leituras.optJSONObject(usuario);
                CentralAtendentesMatrix.leituraRecebida(pSala, usuario, leitura == null ? 0 : leitura.optLong("ts", 0));
            }
        }
    }

    private static boolean isEdicao(JSONObject pConteudo) {
        if (pConteudo.has("m.new_content")) {
            return true;
        }
        JSONObject relacao = pConteudo.optJSONObject("m.relates_to");
        return relacao != null && "m.replace".equals(relacao.optString("rel_type"));
    }

    private static String extrairTexto(JSONObject pConteudo) {
        String texto;
        switch (pConteudo.optString("msgtype")) {
            case "m.image":
                texto = "[imagem]";
                break;
            case "m.video":
                texto = "[vídeo]";
                break;
            case "m.audio":
                texto = "[áudio]";
                break;
            case "m.file":
                texto = "[arquivo] " + pConteudo.optString("body", "");
                break;
            case "m.location":
                texto = "[localização]";
                break;
            default:
                texto = pConteudo.optString("body", "");
        }
        texto = texto.trim();
        return texto.length() <= TAMANHO_MAXIMO_TEXTO ? texto : texto.substring(0, TAMANHO_MAXIMO_TEXTO) + "...";
    }

    private static String resumo(String pTexto) {
        return pTexto == null ? "" : (pTexto.length() <= 200 ? pTexto : pTexto.substring(0, 200) + "...");
    }

}
