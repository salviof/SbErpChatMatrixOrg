/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.api.erp.chat.notificacoes;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfNotificacaoUsuarioChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestMatrixNotificacoes;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.WS.conexaoWebServiceClient.ItfRespostaWebServiceSimples;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

/**
 *
 * @author salvio
 */
public class SincronizacaoNotificacoes {

    private List<ComoUsuarioChat> usuariosNaEscuta;
    private List<ItfRetornoDeChamadaDeNotificacao> processadoresDeNotificacoes = new ArrayList<>();
    private List<ItfNotificacaoUsuarioChat> ultimasNotificacoes = new ArrayList<>();

    private static Thread eventListenerNotificacoesThread;

    public SincronizacaoNotificacoes() {

    }

    public void addNotificadorEventListener(ItfRetornoDeChamadaDeNotificacao callback) {
        processadoresDeNotificacoes.add(callback);
    }

    public void startSyncee() {

        executarListener();

    }

    public void stopSyncee() {
        if (eventListenerNotificacoesThread != null) {
            eventListenerNotificacoesThread.stop();
        }
    }

    private void executarListener() {
        if (eventListenerNotificacoesThread == null) {
            eventListenerNotificacoesThread = new Thread(() -> {

                while (true) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        ItfRespostaWebServiceSimples resp = FabApiRestMatrixNotificacoes.MINHAS_NOTIFICACOES_LISTAR.getAcao().getResposta();
                        if (!resp.isSucesso()) {
                            return;
                        }
                        JsonObject respJson = resp.getRespostaComoObjetoJson();

                        if (respJson != null) {
                            if (respJson.containsKey("notifications")) {
                                JsonArray jsonArray = respJson.getJsonArray("notifications");
                                int idxNotificacao = 1;
                                for (JsonValue notificacaoJson : jsonArray) {
                                    ItfNotificacaoUsuarioChat notificacao = ERPChat.MATRIX_ORG.getDTO(notificacaoJson.toString(),
                                            ItfNotificacaoUsuarioChat.class);

                                    if (ultimasNotificacoes.stream().filter(nt -> nt.getCodigoNotificacao().equals(notificacao.getCodigoNotificacao()))
                                            .findFirst().isPresent()) {
                                        break;
                                    }

                                    if (ultimasNotificacoes.size() - 1 >= idxNotificacao) {
                                        ultimasNotificacoes.add(idxNotificacao, notificacao);
                                    } else {
                                        ultimasNotificacoes.add(notificacao);
                                    }

                                    for (ItfRetornoDeChamadaDeNotificacao processador : processadoresDeNotificacoes) {
                                        try {
                                            System.out.println("TODO Listeners salas do admin");
                                            processador.onEventReceived(notificacao);
                                        } catch (Throwable t) {

                                        }
                                    }
                                    idxNotificacao++;
                                }

                            }

                        }

                    } catch (Throwable ea) {
                        SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha processando notificações do administrador", ea);
                        continue;
                    }
                }

            });
        }

        eventListenerNotificacoesThread.start();
    }
}
