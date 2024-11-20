/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.RespostaLogout;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaChatSessaoEscutaAtiva;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestIntMatrixChatUsuarios;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringFiltros;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.token.ItfTokenGestao;
import de.jojii.matrixclientserver.Bot.Client;
import java.io.IOException;
import static java.lang.Thread.sleep;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

/**
 *
 * @author salvio
 */
public class MonitorConexao extends Thread {

    private final Client clienteConexao;
    private boolean conectadoParaEscuta;
    private boolean tentouConexaoInicial;
    private final String usuarioLogin;
    private final String senhaLogin;

    public synchronized void adicionarLister(SalaChatSessaoEscutaAtiva pSala) {
        clienteConexao.registerRoomEventListener(pSala.getEscuta());
    }

    public MonitorConexao(String pUsuarioLogin, String pSenhaLogin) {
        usuarioLogin = pUsuarioLogin;
        senhaLogin = pSenhaLogin;
        String url = SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getPropriedade(FabConfigApiMatrixChat.URL_MATRIX_SERVER);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        clienteConexao = new Client(url);
        System.out.println("Iniciando monitor de conexao para" + pUsuarioLogin);
        System.out.println("Em: " + url);
    }

    public boolean isTentouConexaoInicial() {
        return tentouConexaoInicial;
    }

    public boolean logout() {
        try {
            clienteConexao.logout(new RespostaLogout());
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public void run() {
        while (!conectadoParaEscuta) {
            try {
                try {
                    if (clienteConexao != null && !clienteConexao.isLoggedIn()) {

                        clienteConexao.login(usuarioLogin, senhaLogin,
                                loginData -> {

                                    if (loginData.isSuccess()) {
                                        conectadoParaEscuta = true;
                                        System.out.println("Conectado com sucesso " + loginData.getUser_id());

                                    } else {
                                        conectadoParaEscuta = false;

                                        System.out.println("Conexaão falhou para o usuário " + usuarioLogin);
                                        System.out.println("Pass:" + UtilSBCoreStringFiltros.getPrimeirasXLetrasDaString(senhaLogin + "....", 4));
                                        System.out.println("Host:" + clienteConexao.getHost());
                                        System.out.println(loginData.getUser_id());
                                        System.out.println(loginData.getHome_server());
                                        System.out.println(loginData.getAccess_token());
                                    }
                                });
                        if (conectadoParaEscuta == false) {
                            ItfTokenGestao gestao = FabApiRestIntMatrixChatUsuarios.USUARIOS_DA_SALA.getGestaoToken();
                            if (!gestao.validarToken()) {
                                gestao.excluirToken();
                                gestao.gerarNovoToken();
                            }
                            if (gestao.validarToken()) {
                                clienteConexao.login(gestao.getToken(), loginData -> {
                                    conectadoParaEscuta = loginData.isSuccess();
                                });
                            }
                        }

                    } else {

                        if (clienteConexao == null) {
                            System.out.println("Conexão é nula");
                        } else {
                            conectadoParaEscuta = true;
                            System.out.println("conexão ok");
                        }
                    }
                    tentouConexaoInicial = true;
                } catch (IOException ex) {
                    SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Erro conectando com chat para sala", ex);
                    tentouConexaoInicial = true;
                }
                sleep(10000);
            } catch (InterruptedException ex) {
                tentouConexaoInicial = true;
                logout();
            }
        }

    }

}
