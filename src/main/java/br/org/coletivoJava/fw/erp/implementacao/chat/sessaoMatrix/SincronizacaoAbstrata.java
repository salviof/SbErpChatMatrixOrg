package br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix;

import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import br.org.coletivoJava.fw.api.erp.chat.ErroMtxParalizacaoDeProcessamento;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.pacotematrix.PacoteMatrixParsing;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaChatSessaoEscutaAtiva;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfEventoMatix;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ComandoDeAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ErroComandoAtendimentoInvalido;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.pacotematrix.PacoteDeEventosMatrix;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreJson;
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
                    try {
                        data = httpHelper.sendRequest(sessao.getClienteConexao().getHost(), nextURL, null, false, "GET");

                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (data != null && data.length() > 0) {
                        JsonObject dadosJson = UtilSBCoreJson.getJsonObjectByTexto(data);
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
                                if (!sessao.getGestaoToken().validarToken()) {
                                    sessao.getGestaoToken().excluirToken();
                                    sessao.getGestaoToken().gerarNovoToken();

                                }
                                httpHelper.setAccess_token(sessao.getGestaoToken().getToken());
                                continue;
                            }
                        }
                        if (dadosJson == null || !dadosJson.containsKey("next_batch") || !dadosJson.containsKey("rooms")) {
                            continue;
                        }
                        try {
                            JSONObject syncData = new JSONObject(data);

                            PacoteDeEventosMatrix pacote = PacoteMatrixParsing.parseEventoSalas(syncData, servicoMatrix);

                            List<ItfEventoMatix> eventosDeSala = pacote.getEventos();

                            List<ComandoDeAtendimento> comandos = pacote.getComandos();

                            for (ComandoDeAtendimento comando : comandos) {

                                try {
                                    servicoMatrix.escutaNotificacoes(comando);
                                } catch (ErroComandoAtendimentoInvalido ex) {
                                    continue;
                                }

                            }

                            for (ItfEventoMatix evento : eventosDeSala) {

                                Optional<ItfListenerEventoMatrix> pesquisaListener = listenersDeSalas.stream().filter(listener -> isEventoCompativelListener(listener, evento)).findFirst();
                                if (pesquisaListener.isPresent()) {
                                    ItfListenerEventoMatrix listener = pesquisaListener.get();
                                    if (listener.isElegivel(evento)) {
                                        try {
                                            listener.processarEvento(evento);
                                        } catch (ErroMtxParalizacaoDeProcessamento ex) {
                                            pausarProcessamento = true;
                                            break;
                                        } catch (Throwable t) {
                                            System.out.println("Falha processando evento " + evento.getRaw().toString(4));
                                            continue;
                                        }
                                    }
                                } else {
                                    if (evento.getRoom_id() == null || evento.getRoom_id().isEmpty() || salaNaoMonitorada.contains(evento.getRoom_id())) {
                                        continue;
                                    }
                                    try {
                                        ItfChatSalaBean sala = servicoMatrix.getSalaByCodigo(evento.getRoom_id());
                                        if (servicoMatrix.isSalaMonitoramentoAutomatica(sala.getApelido())) {
                                            SalaChatSessaoEscutaAtiva escuta = servicoMatrix.salaAbrirSessao(sala);
                                            addRoomEventListener(escuta.getEscuta());
                                            try {
                                                if (escuta.getEscuta().isElegivel(evento)) {
                                                    escuta.getEscuta().processarEvento(evento);
                                                }
                                            } catch (ErroMtxParalizacaoDeProcessamento ex) {
                                                pausarProcessamento = true;
                                                break;
                                            } catch (Throwable t) {
                                                System.out.println("Falha processando evento " + evento.getRaw().toString(4));
                                                continue;
                                            }
                                        } else {
                                            salaNaoMonitorada.add(evento.getRoom_id());
                                            //servicoMatrix.salaEnviarMesagem(sala, "Sala não monitorada, essa mensagem não foi processada");
                                        }
                                    } catch (ErroConexaoServicoChat ex) {
                                        Logger.getLogger(SincronizacaoAbstrata.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                }
                            }
                            if (pausarProcessamento) {
                                continue;
                            }
                            String nextBatch = dadosJson.getString("next_batch");
                            nextURL = baseurl + "&since=" + nextBatch;
                            SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getRepositorioDeArquivosExternos().putConteudoRecursoExterno(NOME_REPOSITORIO_NEXT_BATCH, nextBatch);
                            FileHelper.writeFile(Files.sync_next_batch, nextBatch);
                        } catch (JSONException ea) {
                            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Matrix enviou um JSON INVÁLIDO !!! :O " + data, ea);
                            ea.printStackTrace();
                            continue;
                        } catch (Throwable t) {
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
