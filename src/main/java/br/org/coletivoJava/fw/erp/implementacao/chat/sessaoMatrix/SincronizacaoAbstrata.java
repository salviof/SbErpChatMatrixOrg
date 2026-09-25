package br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix;

import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoChatSalaBean;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import br.org.coletivoJava.fw.api.erp.chat.model.FabTipoPacoteDeAcaoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.ErroMtxParalizacaoDeProcessamento;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.pacotematrix.PacoteMatrixParsing;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaChatSessaoEscutaAtiva;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfEventoMatix;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ComandoDeAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ErroComandoAtendimentoInvalido;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.pacotematrix.PacoteDeEventosMatrix;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.CarameloCode;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilCRCJson;
import com.super_bits.modulosSB.SBCore.modulos.Mensagens.FabMensagens;
import de.jojii.matrixclientserver.Callbacks.DataCallback;
import de.jojii.matrixclientserver.File.FileHelper;
import de.jojii.matrixclientserver.File.Files;
import de.jojii.matrixclientserver.Networking.HttpHelper;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.coletivojava.fw.api.tratamentoErros.FabErro;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author salvio
 */
public abstract class SincronizacaoAbstrata {

    private final int LONG_POLLING_TIMEOUT;

    private SessaoMatrix sessao;
    private HttpHelper httpHelper;
    private List<ItfListenerEventoMatrix> listenersDeSalas = new ArrayList<>();
    private String filterID = null;
    private final String NOME_REPOSITORIO_NEXT_BATCH = "next_batch_matrix";
    private final JSONObject jsonfilter;
    private final ChatMatrixOrgimpl servicoMatrix;

    public SincronizacaoAbstrata(SessaoMatrix pSessao, HttpHelper httpHelper, int pLONG_POLLING_TIMEOUT, String pJsonFiltroSync) {
        LONG_POLLING_TIMEOUT = pLONG_POLLING_TIMEOUT;
        this.sessao = pSessao;
        this.httpHelper = httpHelper;
        jsonfilter = new JSONObject(pJsonFiltroSync);
        servicoMatrix = pSessao.getSERVICO_MATRIX();
    }

    private static final String TAG_LOG = "[MTX-SYNC]";

    /**
     * Janela de histórico processada no primeiro batch do /sync, quando ele é
     * limitado (sempre fora de produção; em produção, só quando o serviço sobe
     * sem since salvo).
     */
    private static final long JANELA_PRIMEIRO_SYNC_EM_HORAS = 5;
    /**
     * Quantos eventos manter quando o Matrix não envia origin_server_ts e a
     * janela de horas não pode ser medida.
     */
    private static final int LIMITE_EVENTOS_SEM_DATA_HORA = 15;

    /**
     * origin_server_ts do evento em milissegundos, ou 0 quando o Matrix não
     * enviou o campo.
     */
    private static long getDataHoraEvento(ItfEventoMatix pEvento) {
        if (pEvento == null || pEvento.getRaw() == null) {
            return 0;
        }
        return pEvento.getRaw().optLong("origin_server_ts", 0);
    }

    private static <T> List<T> getUltimosItens(List<T> pLista, int pQuantidade) {
        if (pLista.size() <= pQuantidade) {
            return pLista;
        }
        return new ArrayList<>(pLista.subList(pLista.size() - pQuantidade, pLista.size()));
    }

    private String ultimoResumoDeCiclo = null;
    private int repeticoesDoResumoDeCiclo = 0;

    /**
     * Registra um evento de log. A instrumentação nunca pode derrubar o loop de
     * sincronização, por isso o serviço de log é chamado dentro de um try.
     */
    private void log(FabMensagens pTipo, String pMensagem) {
        try {
            CarameloCode.getServicoLogEventos().registrarLogDeEvento(pTipo, TAG_LOG + " " + pMensagem);
        } catch (Throwable t) {
            System.out.println(TAG_LOG + " " + pTipo + " " + pMensagem);
        }
    }

    /**
     * Registra o resultado de um ciclo do /sync agrupando repetições idênticas.
     *
     * O "since" faz parte do resumo, então um travamento aparece no log como a
     * mesma linha repetindo com contador crescente, enquanto um ciclo saudável
     * gera uma linha nova a cada avanço do batch.
     */
    private void logCiclo(FabMensagens pTipo, String pResumo) {
        if (pResumo.equals(ultimoResumoDeCiclo)) {
            repeticoesDoResumoDeCiclo++;
            if (repeticoesDoResumoDeCiclo % 50 == 0) {
                log(pTipo, pResumo + " [MESMO RESULTADO REPETIDO " + repeticoesDoResumoDeCiclo + "x CONSECUTIVAS]");
            }
            return;
        }
        if (repeticoesDoResumoDeCiclo > 0) {
            log(FabMensagens.AVISO, "O ciclo anterior repetiu " + repeticoesDoResumoDeCiclo
                    + "x antes de mudar de estado: " + ultimoResumoDeCiclo);
        }
        ultimoResumoDeCiclo = pResumo;
        repeticoesDoResumoDeCiclo = 0;
        log(pTipo, pResumo);
    }

    private static String resumoBatch(String pBatch) {
        if (pBatch == null || pBatch.trim().isEmpty()) {
            return "<vazio>";
        }
        return pBatch.length() <= 28 ? pBatch : pBatch.substring(0, 28) + "...";
    }

    public void addRoomEventListener(ItfListenerEventoMatrix callback) {
        if (!listenersDeSalas.contains(callback)) {
            listenersDeSalas.add(callback);
        }

    }

    public void removeRoomEventListener(ItfListenerEventoMatrix callback) {
        listenersDeSalas.remove(callback);
    }

    void startSyncee() {
        if (filterID == null) {
            requestFilterID(data -> {
                this.filterID = (String) data;
                runEventListener(filterID);
            });
        } else {
            runEventListener(filterID);
        }
    }

    private void requestFilterID(DataCallback filterIDResponse) {

        try {
            httpHelper.sendRequestAsync(sessao.getClienteConexao().getHost(), HttpHelper.URLs.user + sessao.getClienteConexao().getLoginData().getUser_id() + "/filter", jsonfilter, data -> {
                try {
                    JSONObject object1data = new JSONObject((String) data);
                    if (object1data.has("filter_id")) {
                        if (filterIDResponse != null) {
                            filterIDResponse.onData(object1data.getString("filter_id"));
                        }
                    } else {
                        System.err.println("Error getting filter!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            },
                    true, "POST");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopSyncee() {
        if (eventListenerThread != null) {
            eventListenerThread.interrupt();
        }
    }

    private Thread eventListenerThread;

    private List<String> salaNaoMonitorada = new ArrayList<>();

    private void runEventListener(String filterID) {

        if (eventListenerThread == null) {
            eventListenerThread = new Thread(() -> {
                String baseurl = HttpHelper.URLs.sync + "?access_token=" + sessao.getClienteConexao().getLoginData().getAccess_token() + "&filter=" + filterID + "&timeout=" + LONG_POLLING_TIMEOUT;
                String nextURL = "";
                try {
                    if (SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getRepositorioDeArquivosExternos().getTexto(NOME_REPOSITORIO_NEXT_BATCH) == null) {

                        SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getRepositorioDeArquivosExternos().putConteudoRecursoExterno(NOME_REPOSITORIO_NEXT_BATCH, "");
                    }
                    String file_next_batch = SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getRepositorioDeArquivosExternos().getTexto(NOME_REPOSITORIO_NEXT_BATCH);
                    //FileHelper.readFile(Files.sync_next_batch);
                    if (file_next_batch.length() > 0) {

                        nextURL = baseurl + "&since=" + file_next_batch;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (nextURL.trim().length() == 0) {
                    nextURL = baseurl;
                }
                boolean pausarProcessamento = false;
                boolean primeiroPricessamento = true;
                // Sem since salvo, o primeiro /sync do Matrix devolve o estado inteiro das
                // salas, e não o movimento recente. Vale até em produção, mas apenas nesse
                // primeiro batch: a partir do since salvo o processamento é sempre integral.
                boolean semSinceSalvo = !nextURL.contains("&since=");
                String batchEmUso = nextURL.contains("&since=")
                        ? nextURL.substring(nextURL.indexOf("&since=") + 7) : "<inicial>";
                long totalDeEventosProcessados = 0;
                log(FabMensagens.AVISO, "Loop de sincronização iniciado."
                        + " since=" + resumoBatch(batchEmUso)
                        + " longPollTimeout=" + LONG_POLLING_TIMEOUT + "ms"
                        + " producao=" + SBCore.isEmModoProducao());
                while (true) {

                    try {
                        // iss não significa que a requisição será feita a cada 200 milisegundos
                        // pois o /sync do matrix é um long polling com consistência garantida. ou seja se o request /sync for enviado com timeout de 40 segundos, ele vai aguardar por 40 segundos
                        //até que algum evento seja transmintido.
                        if (!pausarProcessamento) {
                            Thread.sleep(200);
                        } else {

                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String data;
                    pausarProcessamento = false;
                    long inicioRequisicao = System.currentTimeMillis();
                    try {
                        data = httpHelper.sendRequest(sessao.getClienteConexao().getHost(), nextURL, null, false, "GET");

                    } catch (IOException e) {
                        logCiclo(FabMensagens.ERRO, "CICLO=FALHA_IO_NO_SYNC"
                                + " since=" + resumoBatch(batchEmUso)
                                + " duracao=" + (System.currentTimeMillis() - inicioRequisicao) + "ms"
                                + " erro=" + e.getClass().getSimpleName() + ": " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                    long duracaoRequisicao = System.currentTimeMillis() - inicioRequisicao;

                    if (data != null && data.length() > 0) {
                        JsonObject dadosJson = UtilCRCJson.getJsonObjectByTexto(data);
                        if (primeiroPricessamento && SBCore.isEmModoDesenvolvimento()) {
                            //Quando em desenvolvimento, não lê os enventos antigos
                            String nexBatchPrimeiraLeitura = dadosJson.getString("next_batch");
                            SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getRepositorioDeArquivosExternos().putConteudoRecursoExterno(NOME_REPOSITORIO_NEXT_BATCH, nexBatchPrimeiraLeitura);
                            primeiroPricessamento = false;
                            continue;
                        }

                        //{
                        // "errcode": "M_UNKNOWN_TOKEN",
                        // "error": "Unrecognized access token."
                        //}
                        if (dadosJson.containsKey("errcode")) {
                            if (dadosJson.getString("errcode").equals("M_UNKNOWN_TOKEN")) {
                                logCiclo(FabMensagens.ALERTA, "CICLO=TOKEN_INVALIDO"
                                        + " since=" + resumoBatch(batchEmUso)
                                        + " (renovando token de acesso do Matrix)");
                                if (!sessao.getGestaoToken().validarToken()) {
                                    sessao.getGestaoToken().excluirToken();
                                    sessao.getGestaoToken().gerarNovoToken();

                                }
                                httpHelper.setAccess_token(sessao.getGestaoToken().getToken());
                                continue;
                            }
                            logCiclo(FabMensagens.ERRO, "CICLO=ERRO_DO_MATRIX"
                                    + " since=" + resumoBatch(batchEmUso)
                                    + " errcode=" + dadosJson.getString("errcode"));
                        }
                        // Só o next_batch é obrigatório: sem ele não há como avançar o since.
                        // A ausência de "rooms" é normal (delta só com device_lists, presence,
                        // to_device ou account_data) e não pode barrar o avanço, senão o mesmo
                        // /sync é repetido em loop, sem long polling, até o serviço reiniciar.
                        if (dadosJson == null || !dadosJson.containsKey("next_batch")) {
                            logCiclo(FabMensagens.AVISO, "CICLO=RESPOSTA_SEM_NEXT_BATCH"
                                    + " since=" + resumoBatch(batchEmUso)
                                    + " duracaoRequisicao=" + duracaoRequisicao + "ms"
                                    + " (o since NÃO avança neste ciclo)");
                            continue;
                        }
                        try {
                            JSONObject syncData = new JSONObject(data);

                            PacoteDeEventosMatrix pacote = PacoteMatrixParsing.parseEventoSalas(syncData, servicoMatrix);

                            List<ItfEventoMatix> eventosDeSala = pacote.getEventos();

                            List<ComandoDeAtendimento> comandos = pacote.getComandos();

                            // Fora de produção o primeiro batch é sempre limitado ao movimento
                            // recente. Em produção processa tudo, exceto quando não havia since
                            // salvo: aí este primeiro batch é o estado inteiro das salas, e não o
                            // movimento recente. Nos dois casos, do segundo batch em diante o
                            // processamento é integral e em tempo real.
                            if (primeiroPricessamento
                                    && (!CarameloCode.isEmModoProducao() || semSinceSalvo)) {
                                int eventosRecebidos = eventosDeSala.size();
                                int comandosRecebidos = comandos.size();
                                long inicioJanela = System.currentTimeMillis()
                                        - (JANELA_PRIMEIRO_SYNC_EM_HORAS * 60 * 60 * 1000L);
                                int totalComDataHora = 0;
                                List<ItfEventoMatix> eventosRecentes = new ArrayList<>();
                                for (ItfEventoMatix evento : eventosDeSala) {
                                    long dataHoraEvento = getDataHoraEvento(evento);
                                    if (dataHoraEvento > 0) {
                                        totalComDataHora++;
                                    }
                                    if (dataHoraEvento >= inicioJanela) {
                                        eventosRecentes.add(evento);
                                    }
                                }
                                List<ComandoDeAtendimento> comandosRecentes = new ArrayList<>();
                                for (ComandoDeAtendimento comando : comandos) {
                                    long dataHoraEvento = getDataHoraEvento(comando.getEvento());
                                    if (dataHoraEvento > 0) {
                                        totalComDataHora++;
                                    }
                                    if (dataHoraEvento >= inicioJanela) {
                                        comandosRecentes.add(comando);
                                    }
                                }
                                String criterio;
                                if (totalComDataHora > 0) {
                                    eventosDeSala = eventosRecentes;
                                    comandos = comandosRecentes;
                                    criterio = "apenas o que chegou nas últimas "
                                            + JANELA_PRIMEIRO_SYNC_EM_HORAS + "h";
                                } else {
                                    // Sem origin_server_ts não há como medir a janela de horas.
                                    eventosDeSala = getUltimosItens(eventosDeSala, LIMITE_EVENTOS_SEM_DATA_HORA);
                                    comandos = getUltimosItens(comandos, LIMITE_EVENTOS_SEM_DATA_HORA);
                                    criterio = "apenas os " + LIMITE_EVENTOS_SEM_DATA_HORA
                                            + " últimos de cada lista, porque nenhum evento veio com origin_server_ts"
                                            + " e a janela de " + JANELA_PRIMEIRO_SYNC_EM_HORAS + "h não pôde ser medida";
                                }
                                int descartados = (eventosRecebidos - eventosDeSala.size())
                                        + (comandosRecebidos - comandos.size());
                                if (descartados > 0) {
                                    log(FabMensagens.ALERTA, "Primeiro batch limitado ("
                                            + (CarameloCode.isEmModoProducao()
                                                    ? "produção iniciada sem since salvo"
                                                    : "execução fora de produção") + "):"
                                            + " recebidos eventos=" + eventosRecebidos
                                            + " comandos=" + comandosRecebidos + ","
                                            + " processando " + criterio
                                            + " (eventos=" + eventosDeSala.size()
                                            + " comandos=" + comandos.size() + ")."
                                            + " " + descartados + " item(ns) de histórico descartado(s)."
                                            + " O since é salvo ao final deste ciclo e do próximo batch"
                                            + " em diante tudo passa a ser processado.");
                                }
                            }

                            if (!eventosDeSala.isEmpty() || !comandos.isEmpty()) {
                                log(FabMensagens.AVISO, "Batch com conteúdo recebido em " + duracaoRequisicao + "ms."
                                        + " since=" + resumoBatch(batchEmUso)
                                        + " eventos=" + eventosDeSala.size()
                                        + " comandos=" + comandos.size()
                                        + " listenersAtivos=" + listenersDeSalas.size()
                                        + " salasIgnoradas=" + salaNaoMonitorada.size());
                            }

                            for (ComandoDeAtendimento comando : comandos) {

                                try {
                                    servicoMatrix.escutaNotificacoes(comando);
                                } catch (ErroComandoAtendimentoInvalido ex) {
                                    log(FabMensagens.AVISO, "Comando de atendimento inválido ignorado: " + ex.getMessage());
                                    continue;
                                }

                            }

                            for (ItfEventoMatix evento : eventosDeSala) {

                                long inicioEvento = System.currentTimeMillis();
                                log(FabMensagens.AVISO, "Evento recebido"
                                        + " tipo=" + evento.getType()
                                        + " sala=" + evento.getRoom_id()
                                        + " remetente=" + evento.getSender()
                                        + " id=" + evento.getEvent_id());

                                Optional<ItfListenerEventoMatrix> pesquisaListener = listenersDeSalas.stream().filter(listener -> isEventoCompativelListener(listener, evento)).findFirst();
                                if (pesquisaListener.isPresent()) {
                                    ItfListenerEventoMatrix listener = pesquisaListener.get();
                                    if (listener.isElegivel(evento)) {
                                        try {
                                            listener.processarEvento(evento);
                                            log(FabMensagens.AVISO, "Evento id=" + evento.getEvent_id()
                                                    + " tipo=" + evento.getType()
                                                    + " processado por listener existente em "
                                                    + (System.currentTimeMillis() - inicioEvento) + "ms");
                                        } catch (ErroMtxParalizacaoDeProcessamento ex) {
                                            log(FabMensagens.ALERTA, "PARALISAÇÃO pedida pelo listener no evento id="
                                                    + evento.getEvent_id() + " após "
                                                    + (System.currentTimeMillis() - inicioEvento) + "ms."
                                                    + " Motivo: " + ex.getMessage()
                                                    + ". O batch será interrompido e o since NÃO avançará.");
                                            pausarProcessamento = true;
                                            break;
                                        } catch (Throwable t) {
                                            log(FabMensagens.ERRO, "Falha processando evento id=" + evento.getEvent_id()
                                                    + " tipo=" + evento.getType()
                                                    + " após " + (System.currentTimeMillis() - inicioEvento) + "ms."
                                                    + " O evento será DESCARTADO: "
                                                    + t.getClass().getName() + ": " + t.getMessage());
                                            System.out.println("Falha processando evento " + evento.getRaw().toString(4));
                                            continue;
                                        }
                                    } else {
                                        log(FabMensagens.AVISO, "Evento id=" + evento.getEvent_id()
                                                + " tipo=" + evento.getType()
                                                + " não elegível para o listener da sala; ignorado em "
                                                + (System.currentTimeMillis() - inicioEvento) + "ms");
                                    }
                                } else {
                                    if (evento.getRoom_id() == null || evento.getRoom_id().isEmpty() || salaNaoMonitorada.contains(evento.getRoom_id())) {
                                        log(FabMensagens.AVISO, "Evento id=" + evento.getEvent_id()
                                                + " ignorado (sala sem id ou já marcada como não monitorada)"
                                                + " sala=" + evento.getRoom_id());
                                        continue;
                                    }
                                    // Mudança de nome/avatar do contato (ou do admin) gera um m.room.member em
                                    // cada sala dele. O listener recusa eventos desses remetentes, então abrir a
                                    // sala (getSalaByCodigo, ~4s cada) só atrasa o batch sem efeito nenhum.
                                    if (FabTipoPacoteDeAcaoMatrix.ATUALIZACAO_MEMBROS.equals(evento.getTipoEvento())
                                            && (servicoMatrix.isUmUsuarioContato(evento.getSender())
                                            || ChatMatrixOrgimpl.getCodigoUsuarioAdmin().equals(evento.getSender()))) {
                                        log(FabMensagens.AVISO, "Evento id=" + evento.getEvent_id()
                                                + " tipo=" + evento.getType()
                                                + " de " + evento.getSender()
                                                + " em sala sem listener; ignorado sem abrir a sala");
                                        continue;
                                    }
                                    try {
                                        long inicioBuscaSala = System.currentTimeMillis();
                                        ComoChatSalaBean sala = servicoMatrix.getSalaByCodigo(evento.getRoom_id());
                                        log(FabMensagens.AVISO, "Sala " + evento.getRoom_id()
                                                + " sem listener; getSalaByCodigo respondeu em "
                                                + (System.currentTimeMillis() - inicioBuscaSala) + "ms"
                                                + " resultado=" + (sala == null ? "NULL" : sala.getApelido()));
                                        if (sala == null) {
                                            log(FabMensagens.ALERTA, "getSalaByCodigo retornou NULL para sala="
                                                    + evento.getRoom_id() + " (evento id=" + evento.getEvent_id() + ")."
                                                    + " A sala foi excluída ou não é mais visível para o usuário admin."
                                                    + " Ela será marcada como NÃO monitorada e todos os eventos dela"
                                                    + " serão ignorados até o serviço reiniciar, para que o since avance"
                                                    + " e o batch não seja reprocessado indefinidamente.");
                                            salaNaoMonitorada.add(evento.getRoom_id());
                                            continue;
                                        }
                                        if (servicoMatrix.isSalaMonitoramentoAutomatica(sala.getApelido())) {
                                            SalaChatSessaoEscutaAtiva escuta = servicoMatrix.salaAbrirSessao(sala);
                                            addRoomEventListener(escuta.getEscuta());
                                            log(FabMensagens.AVISO, "Listener aberto sob demanda para a sala "
                                                    + sala.getApelido() + " (" + evento.getRoom_id() + ")");
                                            try {
                                                if (escuta.getEscuta().isElegivel(evento)) {
                                                    escuta.getEscuta().processarEvento(evento);
                                                    log(FabMensagens.AVISO, "Evento id=" + evento.getEvent_id()
                                                            + " tipo=" + evento.getType()
                                                            + " processado por listener novo em "
                                                            + (System.currentTimeMillis() - inicioEvento) + "ms");
                                                } else {
                                                    log(FabMensagens.AVISO, "Evento id=" + evento.getEvent_id()
                                                            + " tipo=" + evento.getType()
                                                            + " não elegível para o listener novo; ignorado");
                                                }
                                            } catch (ErroMtxParalizacaoDeProcessamento ex) {
                                                log(FabMensagens.ALERTA, "PARALISAÇÃO pedida pelo listener novo no evento id="
                                                        + evento.getEvent_id() + " após "
                                                        + (System.currentTimeMillis() - inicioEvento) + "ms."
                                                        + " Motivo: " + ex.getMessage()
                                                        + ". O batch será interrompido e o since NÃO avançará.");
                                                pausarProcessamento = true;
                                                break;
                                            } catch (Throwable t) {
                                                log(FabMensagens.ERRO, "Falha processando evento id=" + evento.getEvent_id()
                                                        + " tipo=" + evento.getType()
                                                        + " após " + (System.currentTimeMillis() - inicioEvento) + "ms."
                                                        + " O evento será DESCARTADO: "
                                                        + t.getClass().getName() + ": " + t.getMessage());
                                                System.out.println("Falha processando evento " + evento.getRaw().toString(4));
                                                continue;
                                            }
                                        } else {
                                            log(FabMensagens.ALERTA, "Sala " + evento.getRoom_id()
                                                    + " (" + sala.getApelido() + ") marcada como NÃO monitorada."
                                                    + " Todos os eventos dela serão ignorados até o serviço reiniciar.");
                                            salaNaoMonitorada.add(evento.getRoom_id());
                                            //servicoMatrix.salaEnviarMesagem(sala, "Sala não monitorada, essa mensagem não foi processada");
                                        }
                                    } catch (ErroConexaoServicoChat ex) {
                                        log(FabMensagens.ERRO, "Falha de conexão com o Matrix ao resolver a sala "
                                                + evento.getRoom_id() + " do evento id=" + evento.getEvent_id()
                                                + ". O evento será DESCARTADO: " + ex.getMessage());
                                        Logger.getLogger(SincronizacaoAbstrata.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (Throwable t) {
                                        // Sem este catch a exceção sobe para o batch e o since não avança.
                                        log(FabMensagens.ERRO, "Falha não prevista ao resolver a sala "
                                                + evento.getRoom_id() + " do evento id=" + evento.getEvent_id()
                                                + ". O evento será DESCARTADO: "
                                                + t.getClass().getName() + ": " + t.getMessage());
                                        Logger.getLogger(SincronizacaoAbstrata.class.getName()).log(Level.SEVERE, null, t);
                                    }

                                }
                            }
                            if (pausarProcessamento) {
                                logCiclo(FabMensagens.ALERTA, "CICLO=PAUSADO_SEM_AVANCAR_SINCE"
                                        + " since=" + resumoBatch(batchEmUso)
                                        + " eventosNoBatch=" + eventosDeSala.size()
                                        + " (este mesmo batch será rebuscado; nada novo é entregue ao Whatsapp enquanto isso durar)");
                                continue;
                            }
                            String nextBatch = dadosJson.getString("next_batch");
                            totalDeEventosProcessados += eventosDeSala.size();
                            logCiclo(FabMensagens.AVISO, "CICLO=SINCE_AVANCADO"
                                    + " de=" + resumoBatch(batchEmUso)
                                    + " para=" + resumoBatch(nextBatch)
                                    + " eventosNoBatch=" + eventosDeSala.size()
                                    + " duracaoRequisicao=" + duracaoRequisicao + "ms"
                                    + " duracaoCiclo=" + (System.currentTimeMillis() - inicioRequisicao) + "ms"
                                    + " totalDeEventosDesdeOInicio=" + totalDeEventosProcessados);
                            batchEmUso = nextBatch;
                            nextURL = baseurl + "&since=" + nextBatch;
                            SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getRepositorioDeArquivosExternos().putConteudoRecursoExterno(NOME_REPOSITORIO_NEXT_BATCH, nextBatch);
                            FileHelper.writeFile(Files.sync_next_batch, nextBatch);
                            // Só deixa de ser o primeiro processamento quando um batch é concluído e o
                            // since avança. Se o ciclo falhar e o mesmo batch voltar, a regra de janela
                            // fora de produção continua valendo.
                            primeiroPricessamento = false;
                        } catch (JSONException ea) {
                            logCiclo(FabMensagens.ERRO, "CICLO=JSON_INVALIDO_DO_MATRIX"
                                    + " since=" + resumoBatch(batchEmUso)
                                    + " erro=" + ea.getMessage()
                                    + " (o since NÃO avança neste ciclo)");
                            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Matrix enviou um JSON INVÁLIDO !!! :O " + data, ea);
                            ea.printStackTrace();
                            continue;
                        } catch (Throwable t) {
                            logCiclo(FabMensagens.ERRO_FATAL, "CICLO=EXCECAO_NAO_TRATADA_NO_BATCH"
                                    + " since=" + resumoBatch(batchEmUso)
                                    + " erro=" + t.getClass().getName() + ": " + t.getMessage()
                                    + " (o since NÃO avança; o MESMO batch será reprocessado indefinidamente"
                                    + " e nenhuma mensagem nova do Matrix chegará ao Whatsapp até reiniciar)");
                            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha processando  " + data, t);
                            pausarProcessamento = true;
                        }

                    }
                }
            });

            eventListenerThread.start();
        }
    }

    public abstract boolean isEventoCompativelListener(ItfListenerEventoMatrix pListener, ItfEventoMatix pEvento);
}
