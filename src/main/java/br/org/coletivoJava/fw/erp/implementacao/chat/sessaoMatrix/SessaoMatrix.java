package br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix;

import br.org.coletivoJava.fw.api.erp.chat.ErroRegraDeNEgocioChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.RespostaLogout;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaChatSessaoEscutaAtiva;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestIntMatrixChatUsuarios;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import br.org.coletivoJava.integracoes.restIntmatrixchat.implementacao.GestaoTokenRestIntmatrixchat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.token.ItfTokenGestao;
import java.io.IOException;
import static java.lang.Thread.sleep;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

/**
 *
 * @author salvio
 */
public class SessaoMatrix extends Thread {

    private final ClientMatrix clienteConexao;
    private final ChatMatrixOrgimpl SERVICO_MATRIX;
    private final ItfTokenGestao gestaoToken;

    private boolean conectadoParaEscuta;
    private boolean tentouConexaoInicial;
    private final TIPO_MONITOR tipo;

    enum TIPO_MONITOR {
        ADMIN,
        CONTATO,
        ATENDIMENTO
    }

    public ItfTokenGestao getGestaoToken() {
        return gestaoToken;
    }

    public ChatMatrixOrgimpl getSERVICO_MATRIX() {
        return SERVICO_MATRIX;
    }

    public SessaoMatrix(ChatMatrixOrgimpl pServicoMatrix) {
        tipo = TIPO_MONITOR.ADMIN;
        String url = SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getPropriedade(FabConfigApiMatrixChat.URL_MATRIX_SERVER);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        SERVICO_MATRIX = pServicoMatrix;
        gestaoToken = FabApiRestIntMatrixChatUsuarios.USUARIOS_DA_SALA.getGestaoToken();
        clienteConexao = new ClientMatrix(this);
    }

    public ItfTokenGestao getGestaoToken(ComoUsuarioChat pUsuario, String senha) throws ErroRegraDeNEgocioChat {
        switch (tipo) {

            case ADMIN:
                return FabApiRestIntMatrixChatUsuarios.USUARIOS_DA_SALA.getGestaoToken();

            case CONTATO:
                GestaoTokenRestIntmatrixchat gestao = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIOS_DA_SALA.getGestaoToken(pUsuario);
                gestao.setLoginNomeUsuario(pUsuario.getEmailPrincipal());
                gestao.setLoginSenhaUsuario(SERVICO_MATRIX.gerarSenhaPadrao(pUsuario, pUsuario.getCodigoUsuario()));
                return gestao;

            case ATENDIMENTO:
                GestaoTokenRestIntmatrixchat gestaoAtendimento = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIOS_DA_SALA.getGestaoToken(pUsuario);
                gestaoAtendimento.setLoginNomeUsuario(pUsuario.getEmailPrincipal());
                gestaoAtendimento.setLoginSenhaUsuario(senha);
                return gestaoAtendimento;

            default:
                throw new AssertionError();
        }
    }

    public SessaoMatrix(ChatMatrixOrgimpl pServicoMatrix, ComoUsuarioChat pUsuarioContato) throws ErroRegraDeNEgocioChat {
        SERVICO_MATRIX = pServicoMatrix;
        if (!SERVICO_MATRIX.isUmUsuarioContato(pUsuarioContato.getCodigoUsuario())) {
            throw new ErroRegraDeNEgocioChat("Para monitorar um umsuario de contato envie um usuario contato no parametro");
        }
        String url = SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getPropriedade(FabConfigApiMatrixChat.URL_MATRIX_SERVER);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        tipo = TIPO_MONITOR.CONTATO;
        gestaoToken = getGestaoToken(pUsuarioContato, SERVICO_MATRIX.gerarSenhaPadrao(pUsuarioContato, pUsuarioContato.getCodigoUsuario()));
        clienteConexao = new ClientMatrix(this);

    }

    public SessaoMatrix(ChatMatrixOrgimpl pServicoMatrix, ComoUsuarioChat pUsuarioAtividade, String pSenha) throws ErroRegraDeNEgocioChat {
        SERVICO_MATRIX = pServicoMatrix;
        if (!SERVICO_MATRIX.isUmUsuarioAtendimento(pUsuarioAtividade)) {
            throw new ErroRegraDeNEgocioChat("Para monitorar um umsuario de contato envie um usuario contato no parametro");
        }
        String url = SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getPropriedade(FabConfigApiMatrixChat.URL_MATRIX_SERVER);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        tipo = TIPO_MONITOR.CONTATO;
        gestaoToken = getGestaoToken(pUsuarioAtividade, pSenha);
        clienteConexao = new ClientMatrix(this);

    }

    public synchronized void adicionarLister(SalaChatSessaoEscutaAtiva pSala) {
        clienteConexao.registerRoomEventListener(pSala.getEscuta());

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

                        if (conectadoParaEscuta == false) {
                            if (!gestaoToken.validarToken()) {
                                gestaoToken.excluirToken();
                                gestaoToken.gerarNovoToken();
                            }
                            if (gestaoToken.validarToken()) {
                                clienteConexao.login(gestaoToken.getToken(), loginData -> {
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
            } catch (Throwable t) {
                System.out.println("Problema efetuando login" + t.getMessage());
            }
        }

    }

    public ClientMatrix getClienteConexao() {
        return clienteConexao;
    }

}
