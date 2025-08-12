package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix.SessaoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.ChatMatrixOrg;
import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroRegraDeNEgocioChat;
import br.org.coletivoJava.fw.api.erp.chat.ItfErpChatService;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfNotificacaoUsuarioChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import br.org.coletivoJava.fw.api.erp.chat.notificacoes.ItfRetornoDeChamadaDeNotificacao;
import br.org.coletivoJava.fw.api.erp.chat.notificacoes.SincronizacaoNotificacoes;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.FabTipoSalaMatrix;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.RespostaSalaApiEscuta;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaChatSessaoEscutaAtiva;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.UsuarioChatMatrixOrg;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestIntMatrixChatSalas;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestIntMatrixChatUsuarios;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestIntMatrixSpaces;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestInteMatrixChatDirect;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestMatrixMedia;
import br.org.coletivoJava.integracoes.matrixChat.FabApiRestMatrixNotificacoes;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import br.org.coletivoJava.integracoes.restIntmatrixchat.UtilsbApiMatrixChat;
import br.org.coletivoJava.integracoes.restIntmatrixchat.implementacao.GestaoTokenRestIntmatrixchat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.ConfigGeral.arquivosConfiguracao.ConfigModulo;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreCriptrografia;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreJson;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringFiltros;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringSlugs;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringTelefone;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringValidador;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringsCammelCase;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.WS.conexaoWebServiceClient.ItfRespostaWebServiceSimples;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.token.ItfTokenGestao;
import com.super_bits.modulosSB.SBCore.modulos.erp.ErroJsonInterpredador;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ItfUsuario;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.InputStream;
import static java.lang.Thread.sleep;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

/**
 *
 * @author salvio
 */
@ChatMatrixOrg
@Singleton
public class ChatMatrixOrgimpl
        implements ItfErpChatService {

    private static Map<String, ItfUsuarioChat> mapaUsuarioChatByEmail = new HashMap<>();
    private static Map<String, ItfUsuarioChat> mapaUsuarioChatByTelefone = new HashMap<>();
    private static Map<String, ItfUsuarioChat> mapaUsuarioChatByCodigo = new HashMap<>();
    private static Map<String, SalaChatSessaoEscutaAtiva> mapasalaSessaoAtiva = new HashMap<>();
    private static Class classeEscutaSalas;
    private static Class classeEscutaNotificacao;
    private static SincronizacaoNotificacoes sincronizadorNotificacoes;
    private static SessaoMatrix monitor;
    private final static RespostaSalaApiEscuta respSalaApi = new RespostaSalaApiEscuta();
    private static ConfigModulo configuracao;
    private static SincronizacaoNotificacoes sinc;
    private static String codigoUsuarioAdmin;

    public String gerarSenhaPadrao(ItfUsuarioChat pUsuario) throws ErroRegraDeNEgocioChat {
        if (!pUsuario.getCodigoUsuario().contains(".ct:")) {
            throw new ErroRegraDeNEgocioChat("Apenas usuários do tipo contato tem senhas autogerenciadas");
        }
        if (pUsuario.getTelefone() == null) {
            throw new ErroRegraDeNEgocioChat("Usuário sem telefone, senhas autogerenciadas, são apenas para usuários que acessam exclusivamente pelo whasapp");
        }
        if (pUsuario.getEmail() != null) {
            throw new ErroRegraDeNEgocioChat("Usuário possui e-mail, senhas autogerenciadas, são apenas para usuários que acessam exclusivamente pelo whasapp");
        }
        StringBuilder senha = new StringBuilder();
        senha.append(pUsuario.getTelefone());
        senha.append(SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getPropriedade(FabConfigApiMatrixChat.SEGREDO));
        String hashSenha = UtilSBCoreCriptrografia.getHash128HexaMD5AsString(senha.toString());
        return hashSenha;
    }

    public static String getCodigoUsuarioAdmin() {
        if (codigoUsuarioAdmin == null) {
            GestaoTokenRestIntmatrixchat gtoke = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIOS_STATUS.getGestaoToken();

            if (!gtoke.isTemTokemAtivo()) {
                gtoke.gerarNovoToken();
            }
            codigoUsuarioAdmin = gtoke.getUserID();
        }
        return codigoUsuarioAdmin;
    }

    public static ConfigModulo getConfiguracao() {
        if (configuracao == null) {
            configuracao = SBCore.getConfigModulo(FabConfigApiMatrixChat.class);
        }
        return configuracao;
    }

    public ChatMatrixOrgimpl() {

    }

    public ChatMatrixOrgimpl(ConfigModulo pConfig) {
        configuracao = pConfig;
    }

    @Override
    public ItfChatSalaBean getSalaByAlias(String pAlias) throws ErroConexaoServicoChat {
        ItfTokenGestao tokenEcontrarById = FabApiRestIntMatrixChatSalas.SALA_ENCONTRAR_POR_ALIAS.getGestaoToken();
        if (!tokenEcontrarById.isTemTokemAtivo()) {
            tokenEcontrarById.gerarNovoToken();
        }
        ItfRespostaWebServiceSimples respostaBuscaPeloAlias = FabApiRestIntMatrixChatSalas.SALA_ENCONTRAR_POR_ALIAS.getAcao(pAlias).getResposta();
        //{"room_id":"!sysqUVrbhFXRcPuBcH:casanovadigital.com.br","servers":["casanovadigital.com.br"]}
        //{"errcode":"M_NOT_FOUND","error":"Room alias #APELIDO_APENAS_TEST:casanovadigital.com.br not found"}
        if (respostaBuscaPeloAlias.getRespostaComoObjetoJson().containsKey("errcode")) {
            return null;
        }
        String roomId = respostaBuscaPeloAlias.getRespostaComoObjetoJson().getString("room_id");
        return getSalaByCodigo(roomId);
    }

    @Override
    public ItfChatSalaBean getSalaByNome(String pNomeSala) throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatSalas.SALA_ENCONTRAR_POR_NOME.getAcao(pNomeSala).getResposta();

        JsonObject respJson = resposta.getRespostaComoObjetoJson();
        if (!respJson.getJsonArray("rooms").isEmpty()) {
            JsonObject jsonSala = respJson.getJsonArray("rooms").get(0).asJsonObject();
            if (respJson.getInt("total_rooms") > 0) {
                System.out.println(UtilSBCoreJson.getTextoByJsonObjeect(jsonSala));
                try {
                    ItfChatSalaBean sala = ERPChat.MATRIX_ORG.getDTO(UtilSBCoreJson.getTextoByJsonObjeect(jsonSala), ItfChatSalaBean.class);

                    return sala;
                } catch (ErroJsonInterpredador ex) {
                    SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha interpretando Json", ex);
                }
            }
        }
        return null;

    }

    private boolean isTemChaveValida() throws ErroConexaoServicoChat {
        GestaoTokenRestIntmatrixchat gestaoToken = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getGestaoToken();

        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS_BY_EMAIL.getAcao(getConfiguracao().getPropriedade(FabConfigApiMatrixChat.USUARIO_ADMIN)).getResposta();

        if (resposta == null || !resposta.isSucesso()) {
            System.out.println("Falha localizando usuário pelo e-mail:");

            gestaoToken = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getGestaoToken();
            if (!gestaoToken.validarToken()) {
                gestaoToken.renovarToken();
            }
            if (!gestaoToken.isTemTokemAtivo()) {
                throw new ErroConexaoServicoChat("Falha conectando com serviço de chat");
            }

        }

        return gestaoToken.isTemTokemAtivo();
    }

    @Override
    public ItfChatSalaBean getSalaByCodigo(String pCodigoSala) throws ErroConexaoServicoChat {
        if (!isTemChaveValida()) {
            return null;
        }
        if (pCodigoSala == null) {
            return null;
        }
        if (!pCodigoSala.startsWith("!")) {
            return null;
        }

        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatSalas.SALA_ENCONTRAR_POR_ID.getAcao(pCodigoSala).getResposta();

        JsonObject respJson = resposta.getRespostaComoObjetoJson();
        if (!respJson.getJsonArray("rooms").isEmpty()) {
            JsonObject jsonSala = respJson.getJsonArray("rooms").get(0).asJsonObject();
            if (respJson.getInt("total_rooms") > 0) {
                System.out.println(UtilSBCoreJson.getTextoByJsonObjeect(jsonSala));
                try {
                    ItfChatSalaBean sala = ERPChat.MATRIX_ORG.getDTO(UtilSBCoreJson.getTextoByJsonObjeect(jsonSala), ItfChatSalaBean.class);

                    return sala;
                } catch (ErroJsonInterpredador ex) {
                    SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha interpretando Json", ex);
                }
            }
        } else {
            System.out.println("Sala não encontrada com código " + pCodigoSala);
        }
        return null;
    }

    @Override
    public boolean espacoAdicionarSala(String pCodigoSpace, String pCodigoSala) throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples resp_vincular = FabApiRestIntMatrixSpaces.ESPACO_ADICIONAR_FILHO_DO_ESPACO.getAcao(pCodigoSpace, pCodigoSala, pCodigoSala).getResposta();
        return resp_vincular.isSucesso();
    }

    @Override
    public ItfChatSalaBean espacoCriar(String pNomeVisivel, String pAliasUnico) throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixSpaces.ESPACO_CRIAR.getAcao(pNomeVisivel, pAliasUnico).getResposta();
        if (resposta.isSucesso()) {
            System.out.println("RESPONDEU COM SUCESSO SEM ROMID: pegaí:");
            System.out.println(resposta.getRespostaTexto());
            JsonObject json = resposta.getRespostaComoObjetoJson();
            String codigo = json.getString("room_id");
            ItfChatSalaBean sala = getSalaByCodigo(codigo);
            return sala;
        } else {
            return getSalaByAlias(pAliasUnico);
        }
    }

    @Override
    public boolean salaLerUltimoEvento(String pCodigoSala, ItfUsuarioChat pUsuarioLeitura) throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples respUltimoEvento = FabApiRestIntMatrixChatSalas.SALA_OBTER_ULTIMO_EVENTO
                .getAcao(pCodigoSala).getResposta();
        String ultimoEvento
                = UtilSBCoreJson.getValorApartirDoCaminho("chunk[0].event_id", respUltimoEvento.getRespostaComoObjetoJson());
        ItfRespostaWebServiceSimples resp = FabApiRestIntMatrixChatSalas.SALA_MARCAR_COMO_LIDO.getAcao(pUsuarioLeitura, pCodigoSala, ultimoEvento).getResposta();

        return resp.isSucesso();
    }

    @Override
    public JsonArray salaLerUltimasMensagens(String pCodigoSala) throws ErroConexaoServicoChat {

        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatSalas.SALA_OBTER_ULTIMAS_10_MENSAGENS.getAcao(pCodigoSala).getResposta();
        return resposta.getRespostaComoObjetoJson().getJsonArray("chunk");

    }

    public boolean isUmUsuarioAtendimento(String pCodigo) {
        if (pCodigo == null) {
            return false;
        }
        return pCodigo.contains(".at:");
    }

    public boolean isUmUsuarioContato(String pCodigo) {
        if (pCodigo == null) {
            return false;
        }
        return pCodigo.contains(".ct:");
    }

    @Override
    public boolean isUmUsuarioAtendimento(ItfUsuarioChat pUsuarioAtendimento) {
        if (pUsuarioAtendimento == null) {
            return false;
        }
        return isUmUsuarioAtendimento(pUsuarioAtendimento.getCodigoUsuario());
    }

    @Override
    public boolean isUmUsuarioContato(ItfUsuarioChat pUsuarioContato) {
        if (pUsuarioContato == null) {
            return false;
        }
        return isUmUsuarioContato(pUsuarioContato.getCodigoUsuario());
    }

    enum TIPO_INDENTIFICACAO_SALA {
        NOME,
        APELIDO,
        CODIGO
    }

    @Override
    public ItfChatSalaBean getSalaCriandoSeNaoExistir(final ItfChatSalaBean pSalaDados, String pIdentificador) throws ErroConexaoServicoChat {
        if (!isTemChaveValida()) {
            return null;
        }
        TIPO_INDENTIFICACAO_SALA tipoID = null;
        ItfChatSalaBean sala = null;

        if (!UtilSBCoreStringValidador.isNuloOuEmbranco(pIdentificador)) {
            if (pIdentificador.startsWith("!")) {
                tipoID = TIPO_INDENTIFICACAO_SALA.CODIGO;
            } else if (pIdentificador.startsWith("#")) {
                tipoID = TIPO_INDENTIFICACAO_SALA.APELIDO;
            } else {
                tipoID = TIPO_INDENTIFICACAO_SALA.NOME;
            }
        } else {
            tipoID = TIPO_INDENTIFICACAO_SALA.NOME;
        }
        Optional<ItfChatSalaBean> pesquisaSala = null;
        System.out.println("PROCURANDO MAPA DE SALAS ATIVAS VIA" + tipoID + " com " + pIdentificador);
        switch (tipoID) {

            case NOME:
                if (pSalaDados.getNome() == null || pSalaDados.getNome().isEmpty()) {
                    pesquisaSala = mapasalaSessaoAtiva.values().stream()
                            .filter(sl -> sl.getSala().getNome().equals(pIdentificador))
                            .map(slativa -> slativa.getSala()).findFirst();
                } else {
                    pesquisaSala = mapasalaSessaoAtiva.values().stream()
                            .filter(sl -> sl.getSala().getNome().equals(pSalaDados.getNome()))
                            .map(slativa -> slativa.getSala()).findFirst();
                }

                break;
            case APELIDO:

                pesquisaSala = mapasalaSessaoAtiva.values().stream()
                        .filter(sl -> sl.getSala().getApelido().equals(pIdentificador))
                        .map(slativa -> slativa.getSala()).findFirst();
                break;
            case CODIGO:
                pesquisaSala = mapasalaSessaoAtiva.values().stream()
                        .filter(sl -> sl.getSala().getCodigoChat().equals(pIdentificador))
                        .map(slativa -> slativa.getSala()).findFirst();
                break;
            default:
                throw new AssertionError();
        }

        if (pesquisaSala.isPresent()) {
            return pesquisaSala.get();
        }

        if (UtilSBCoreStringValidador.isNuloOuEmbranco(pIdentificador)) {
            System.out.println("Pesquisando sala por nome" + pSalaDados.getNome());
            sala = getSalaByNome(pSalaDados.getNome());
        } else {
            if (pIdentificador.startsWith("!")) {
                System.out.println("Pesquisando sala por codigo" + pSalaDados.getNome());
                sala = getSalaByCodigo(pIdentificador);
            } else if (pIdentificador.startsWith("#")) {
                sala = getSalaByAlias(pIdentificador);

            } else {
                sala = getSalaByNome(pSalaDados.getApelido());

            }
        }

        if (sala != null) {
            return sala;
        }
        System.out.println("Criando sala com apelido" + pSalaDados.getApelido());
        System.out.println("e nome" + pSalaDados.getNome());
        if (sala == null) {
            ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatSalas.SALA_CRIAR
                    .getAcao(pSalaDados.getNome(), pSalaDados.getApelido()).getResposta();
            if (resposta.isSucesso()) {
                System.out.println("RESPONDEU COM SUCESSO SEM ROMID: pegaí:");
                System.out.println(resposta.getRespostaTexto());
                JsonObject json = resposta.getRespostaComoObjetoJson();
                String codigo = json.getString("room_id");
                sala = getSalaByCodigo(codigo);
            } else {
                System.out.println("Erro criando sala");
                System.out.println(resposta.getRespostaTexto());
                return null;
            }
        }

        if (sala != null) {

            for (ItfUsuarioChat usuario : pSalaDados.getUsuarios()) {
                if (!sala.getUsuarios().contains(usuario)) {
                    ItfRespostaWebServiceSimples resp = FabApiRestIntMatrixChatSalas.SALA_ADICIONAR_USUARIO.getAcao(sala.getCodigoChat(), usuario.getCodigoUsuario()).getResposta();
                    if (resp.isSucesso()) {
                        sala.getUsuarios().add(usuario);
                    }
                }
            }
            switch (tipoID) {

                case NOME:
                    break;
                case APELIDO:
                    break;
                case CODIGO:
                    break;
                default:
                    throw new AssertionError();
            }

            FabTipoSalaMatrix tipoSala = FabTipoSalaMatrix.getTipoByAlias(sala.getApelido());
            if (tipoSala != null) {
                ItfChatSalaBean espaco = getSalaByAlias(tipoSala.getApelidoNomeUnicoSpace());
                System.out.println("Codigo espaço:");
                System.out.println(espaco.getCodigoChat());
                ItfRespostaWebServiceSimples resp2 = FabApiRestIntMatrixSpaces.ESPACO_ADICIONAR_FILHO_DO_ESPACO.getAcao(espaco.getCodigoChat(), sala.getCodigoChat()).getResposta();
            }

        }
        return sala;
    }

    @Override
    public ItfChatSalaBean getSalaCriandoSeNaoExistir(final ItfChatSalaBean pSala) throws ErroConexaoServicoChat {
        if (pSala.getCodigoChat() != null) {
            return getSalaCriandoSeNaoExistir(pSala, pSala.getCodigoChat());
        }
        if (pSala.getApelido() != null) {
            return getSalaCriandoSeNaoExistir(pSala, pSala.getApelido());
        }
        if (pSala.getNome() != null) {
            return getSalaCriandoSeNaoExistir(pSala, pSala.getNome());
        }
        ItfChatSalaBean sala = getSalaCriandoSeNaoExistir(pSala, pSala.getCodigoChat());

        return sala;

    }

    @Override
    public boolean salaExcluir(ItfChatSalaBean pCodigoSalsa) throws ErroConexaoServicoChat {
        if (!isTemChaveValida()) {
            return false;
        }
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatSalas.SALA_EXLUIR.getAcao(pCodigoSalsa.getCodigoChat()).getResposta();
        return resposta.isSucesso();
    }

    @Override
    public List<ItfUsuarioChat> atualizarListaDeUsuarios() throws ErroConexaoServicoChat {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ItfUsuarioChat getUsuarioByTelefone(String pTelefone) throws ErroConexaoServicoChat {
        pTelefone = UtilSBCoreStringTelefone.gerarCeluarInternacional(pTelefone);
        if (mapaUsuarioChatByTelefone.containsKey(pTelefone)) {
            return mapaUsuarioChatByTelefone.get(pTelefone);
        }

        if (!isTemChaveValida()) {

            return null;
        }

        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS_BY_TELEFONE.getAcao(pTelefone).getResposta();
        if (!resposta.isSucesso()) {
            System.out.println("Falha localizando usuário pelo telefone:" + pTelefone);
            System.out.println(resposta.getRetorno());
            return null;
        }
        String userId = resposta.getRespostaComoObjetoJson().getString("user_id");
        ItfRespostaWebServiceSimples respostaUser = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getAcao(userId).getResposta();
        System.out.println("processando:");
        System.out.println(respostaUser.getRespostaTexto());
        try {
            ItfUsuarioChat usuario = ERPChat.MATRIX_ORG.getDTO(respostaUser.getRespostaTexto(), ItfUsuarioChat.class);
            System.out.println("DTO gerado com sucesso");
            return registraUsuarioPorTelefone(usuario);

        } catch (ErroJsonInterpredador ex) {
            System.out.println("Falha processando usuário" + ex.getMessage());
            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha processando usuário" + ex.getMessage(), ex);
            return null;
        }
    }

    private ItfUsuarioChat registraUsuarioPorEmail(ItfUsuarioChat pUsuario) {
        mapaUsuarioChatByEmail.put(pUsuario.getEmail(), pUsuario);
        mapaUsuarioChatByCodigo.put(pUsuario.getCodigoUsuario(), pUsuario);
        return pUsuario;
    }

    private ItfUsuarioChat registraUsuarioPorCodigo(ItfUsuarioChat pUsuario) {

        mapaUsuarioChatByCodigo.put(pUsuario.getCodigoUsuario(), pUsuario);
        return pUsuario;
    }

    private ItfUsuarioChat registraUsuarioPorTelefone(ItfUsuarioChat pUsuario) {
        mapaUsuarioChatByEmail.put(pUsuario.getTelefone(), pUsuario);
        mapaUsuarioChatByCodigo.put(pUsuario.getCodigoUsuario(), pUsuario);
        return pUsuario;
    }

    @Override
    public ItfUsuarioChat getUsuarioByEmail(String pEmail) throws ErroConexaoServicoChat {

        if (mapaUsuarioChatByEmail.containsKey(pEmail)) {
            return mapaUsuarioChatByEmail.get(pEmail);
        }

        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS_BY_EMAIL.getAcao(pEmail).getResposta();

        if (!resposta.isSucesso()) {
            System.out.println("Falha localizando usuário pelo e-mail:" + pEmail);

            GestaoTokenRestIntmatrixchat gestaoToken = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getGestaoToken();
            if (!gestaoToken.validarToken()) {
                gestaoToken.renovarToken();
            }
            if (gestaoToken.isTemTokemAtivo()) {
                return null;
            }
        }
        String userId = resposta.getRespostaComoObjetoJson().getString("user_id");
        ItfRespostaWebServiceSimples respostaUser = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getAcao(userId).getResposta();
        System.out.println("processando:");
        System.out.println(respostaUser.getRespostaTexto());
        try {
            ItfUsuarioChat usuario = ERPChat.MATRIX_ORG.getDTO(respostaUser.getRespostaTexto(), ItfUsuarioChat.class);
            System.out.println("DTO gerado com sucesso");
            return registraUsuarioPorEmail(usuario);

        } catch (ErroJsonInterpredador ex) {
            System.out.println("Falha processando usuário" + ex.getMessage());
            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha processando usuário" + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public ItfUsuarioChat getUsuarioByCodigo(String pCodigo) throws ErroConexaoServicoChat {
        if (mapaUsuarioChatByCodigo.containsKey(pCodigo)) {
            return mapaUsuarioChatByCodigo.get(pCodigo);
        }
        if (!isTemChaveValida()) {
            return null;
        }
        ItfRespostaWebServiceSimples respostaUser = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getAcao(pCodigo).getResposta();
        if (respostaUser.isSucesso()) {

            ItfUsuarioChat usuario;
            System.out.println(respostaUser.getRespostaTexto());
            try {
                usuario = ERPChat.MATRIX_ORG.getDTO(respostaUser.getRespostaTexto(), ItfUsuarioChat.class);
            } catch (ErroJsonInterpredador ex) {
                return null;
            }
            return registraUsuarioPorCodigo(usuario);
        } else {
            return null;
        }

    }

    @Override
    public List<ItfUsuarioChat> getUsuarios() throws ErroConexaoServicoChat {
        //FabApiRestIntMatrixChatUsuarios.USUARIOS_LISTAGEM;
        throw new UnsupportedOperationException("Falha criando usuaário");
    }

    @Override
    public ItfUsuarioChat usuarioCriar(ItfUsuarioChat pUsuario) throws ErroConexaoServicoChat {
        String senha = UtilMatrixERP.gerarSenha(pUsuario);
        return usuarioCriar(pUsuario, senha);
    }

    @Override
    public boolean salaAtualizarMembros(ItfChatSalaBean pSalaRecemAtualizada, List<ItfUsuarioChat> pLista) throws ErroConexaoServicoChat {
        List<ItfUsuarioChat> usuariosAdicionar = new ArrayList<>();
        List<ItfUsuarioChat> usuariosRemover = new ArrayList<>();
        String codigoAdmin = getCodigoUsuarioAdmin();
        List<String> codigosUsuariosDesejado = new ArrayList<>();
        List<String> codigosUsuariosEstadoAtual = new ArrayList<>();
        pLista.stream()
                .filter(usr -> !UtilSBCoreStringValidador.isNuloOuEmbranco(usr.getCodigoUsuario()))
                .map(usr -> usr.getCodigoUsuario())
                .forEach(codigosUsuariosDesejado::add);
        if (codigosUsuariosDesejado.isEmpty()) {
            return false;
        }

        pSalaRecemAtualizada.getUsuarios().stream()
                .filter(usr -> !UtilSBCoreStringValidador.isNuloOuEmbranco(usr.getCodigoUsuario()))
                .map(usr -> usr.getCodigoUsuario())
                .forEach(codigosUsuariosEstadoAtual::add);

        pSalaRecemAtualizada.getUsuarios().stream()
                .filter(usr -> !usr.getCodigoUsuario().equals(codigoAdmin) && !codigosUsuariosDesejado.contains(usr.getCodigoUsuario()))
                .forEach(usuariosRemover::add);

        pLista.stream()
                .filter(usr -> !usr.getCodigoUsuario().equals(codigoAdmin) && !codigosUsuariosEstadoAtual.contains(usr.getCodigoUsuario()))
                .forEach(usuariosAdicionar::add);
        if (usuariosAdicionar.isEmpty() && usuariosRemover.isEmpty()) {
            return false;
        }
        for (ItfUsuarioChat usuarioAdd : usuariosAdicionar) {
            salaAdicionarMembro(pSalaRecemAtualizada, usuarioAdd.getCodigoUsuario());
        }

        for (ItfUsuarioChat usuarioRM : usuariosRemover) {
            salaRemoverMembro(pSalaRecemAtualizada, usuarioRM.getCodigoUsuario());
        }

        return true;
    }

    /**
     *
     * Remove um usuário de uma sala.
     *
     *
     * @param pSala
     * @param pCodigoMembro
     * @return Erro em caso de falha de conexão, e false, caso o usuário não
     * tenha sido removido por não estar na sala, ou outra regra de negócio
     * @throws ErroConexaoServicoChat
     */
    @Override
    public boolean salaRemoverMembro(ItfChatSalaBean pSala, String pCodigoMembro) throws ErroConexaoServicoChat {
        try {

            validarTokenSistema();
            if (pCodigoMembro.equals(getCodigoUsuarioAdmin())) {
                return false;
            }
            String codigoAdmin = getCodigoUsuarioAdmin();
            if (!pSala.getUsuarios().stream().filter(usr -> usr.getCodigoUsuario().equals(codigoAdmin)).findFirst().isPresent()) {
                salaAdicionarMembro(pSala, getCodigoUsuarioAdmin());
            }
            ItfRespostaWebServiceSimples resp = FabApiRestIntMatrixChatSalas.SALA_REMOVER_USUARIO.getAcao(pSala.getCodigoChat(), pCodigoMembro).getResposta();

            return resp.isSucesso();
        } catch (Throwable t) {
            throw new ErroConexaoServicoChat("Falha removendo membro da sala " + pSala + " Codigo membro " + pCodigoMembro);
        }
    }

    @Override
    public boolean salaAdicionarMembro(ItfChatSalaBean pSala, String pCodigoUsuario) throws ErroConexaoServicoChat {
        if (!isTemChaveValida()) {
            return false;
        }
        ItfRespostaWebServiceSimples resp = FabApiRestIntMatrixChatSalas.SALA_ADICIONAR_USUARIO.getAcao(pSala.getCodigoChat(), pCodigoUsuario).getResposta();
        return resp.isSucesso();
    }

    @Override
    public ItfUsuarioChat getUsuarioChatByLoginSessaoAtual() {
        ItfUsuario usuariologado = SBCore.getUsuarioLogado();
        ItfUsuarioChat usuario;
        try {
            usuario = getUsuarioByEmail(usuariologado.getEmail());
        } catch (ErroConexaoServicoChat ex) {
            return null;
        }
        return usuario;
    }

    @Override
    public ItfChatSalaBean getSalaAtualizada(ItfChatSalaBean pSala) throws ErroConexaoServicoChat {
        return getSalaByCodigo(pSala.getCodigoChat());
    }

    @Override
    public boolean usuarioAtualizarSenha(String pCodigoUsuario, String pNovaSenha) throws ErroConexaoServicoChat, ErroRegraDeNEgocioChat {
        validarTokenSistema();
        if (pCodigoUsuario.contains(".ct:")) {
            if (!pNovaSenha.equals(gerarSenhaPadrao(getUsuarioByCodigo(pCodigoUsuario)))) {
                throw new ErroRegraDeNEgocioChat("a senha dos usuários de contato são auto gerenciadas" + ", utilise: servico.gerarSenhaPadrao(usuario) para atualizar a senha");
            }

        }
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatUsuarios.USUARIO_ATUALIZAR_SENHA.getAcao(pCodigoUsuario, pNovaSenha).getResposta();
        if (!resposta.isSucesso()) {
            System.out.println(resposta.getRespostaTexto());
        }
        return resposta.isSucesso();
    }

    @Override
    public boolean tokenGestaoEfetuarLogin(ItfUsuarioChat pUsuario, String pSenha) {
        ItfUsuarioChat usuarioChat;
        try {
            usuarioChat = getUsuarioByEmail(pUsuario.getEmail());
        } catch (ErroConexaoServicoChat ex) {
            return false;
        }
        if (usuarioChat == null) {
            return false;
        }

        GestaoTokenRestIntmatrixchat gestaoToken = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getGestaoToken(pUsuario);
        if (gestaoToken.isTemTokemAtivo()) {
            return gestaoToken.isTemTokemAtivo();
        }
        gestaoToken.setLoginNomeUsuario(pUsuario.getCodigoUsuario());
        gestaoToken.setLoginSenhaUsuario(pSenha);
        gestaoToken.gerarNovoToken();
        return gestaoToken.isTemTokemAtivo();
    }

    @Override
    public boolean tokenGestaoEfetuarLogin(ItfUsuario pUsuario) {
        ItfUsuarioChat usuarioChat;
        try {
            usuarioChat = getUsuarioByEmail(pUsuario.getEmail());
        } catch (ErroConexaoServicoChat ex) {
            return false;
        }
        String senha = UtilMatrixERP.gerarSenha(usuarioChat);
        return tokenGestaoEfetuarLogin(usuarioChat, senha);

    }

    @Override
    public boolean isUsuarioOnlineByCodUser(String pCodigo) throws ErroConexaoServicoChat {
        //"presence":"online"]
        if (isTemChaveValida()) {

            ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatUsuarios.USUARIOS_STATUS.getAcao(pCodigo).getResposta();
            if (resposta.isSucesso()) {
                JsonObject json = UtilSBCoreJson.getJsonObjectByTexto(resposta.getRespostaTexto());
                if (json.getString("presence").equals("online")) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public void registrarClasseDeEscutaSalas(Class<? extends ItfListenerEventoMatrix> pClasseEscuta) throws ErroConexaoServicoChat {
        classeEscutaSalas = pClasseEscuta;
        iniciarConexaoDeEscuta();
    }

    public void registrarClasseEscutaNotificacoes(Class<? extends ItfRetornoDeChamadaDeNotificacao> pClasseEscuta) throws ErroConexaoServicoChat {
        classeEscutaNotificacao = pClasseEscuta;
        if (pClasseEscuta == null) {
            throw new ErroConexaoServicoChat("A classe de escuta é obrigatória");
        }
        iniciarEscutaNotificacoes();
    }

    private synchronized void iniciarEscutaNotificacoes() throws ErroConexaoServicoChat {
        if (classeEscutaNotificacao != null) {
            if (sincronizadorNotificacoes == null) {
                sincronizadorNotificacoes = new SincronizacaoNotificacoes();
                try {
                    sincronizadorNotificacoes.addNotificadorEventListener((ItfRetornoDeChamadaDeNotificacao) classeEscutaNotificacao.newInstance());
                } catch (InstantiationException ex) {
                    throw new ErroConexaoServicoChat("Falha instanciando listener de notificação padrão");
                } catch (IllegalAccessException ex) {
                    throw new ErroConexaoServicoChat("Falha instanciando listener de notificação padrão");
                }
            } else {
                sincronizadorNotificacoes.stopSyncee();
            }
            sincronizadorNotificacoes.startSyncee();

        }
    }

    private synchronized void iniciarConexaoDeEscuta() throws ErroConexaoServicoChat {

        if (monitor == null) {
            if (classeEscutaSalas == null) {
                throw new ErroConexaoServicoChat("a classe de escuta não foi definida");
            }
            monitor = new SessaoMatrix(this);
            monitor.start();

        }
    }
    private ItfListenerEventoMatrix verificadorAutoMonitoramento;

    public synchronized boolean isSalaMonitoramentoAutomatica(String pNomeSala) {
        if (verificadorAutoMonitoramento == null) {
            if (classeEscutaSalas != null) {
                try {
                    Constructor construtor = classeEscutaSalas.getConstructor(ItfChatSalaBean.class);
                    verificadorAutoMonitoramento = (ItfListenerEventoMatrix) construtor.newInstance((ItfChatSalaBean) null);

                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    return false;
                }
            }
        }
        try {
            return verificadorAutoMonitoramento.isSalaComAutoMonitoramento(pNomeSala);
        } catch (Throwable t) {
            SBCore.RelatarErroAoUsuario(FabErro.SOLICITAR_REPARO, "falha verificando monitoramento automatico para " + pNomeSala, t);
            return false;
        }

    }

    public synchronized boolean isTemSalaSessaoAtiva(String pCodigo) {

        return mapasalaSessaoAtiva.containsKey(pCodigo);

    }

    public boolean isSalaEscutaDefinida() {
        return classeEscutaSalas != null;
    }

    public synchronized SalaChatSessaoEscutaAtiva salaAbrirSessao(ItfChatSalaBean pSala) throws ErroConexaoServicoChat {

        iniciarConexaoDeEscuta();
        if (pSala == null || UtilSBCoreStringValidador.isNuloOuEmbranco(pSala.getCodigoChat())) {
            throw new UnsupportedOperationException("Codigo da sala não enviado");
        }
        if (mapasalaSessaoAtiva.containsKey(pSala.getCodigoChat())) {
            System.out.println("A sala " + pSala.getCodigoChat() + " Já se encontrava na escuta");
            return mapasalaSessaoAtiva.get(pSala.getCodigoChat());
        }

        try {

            Constructor construtor = classeEscutaSalas.getConstructor(ItfChatSalaBean.class);

            SalaChatSessaoEscutaAtiva novaSala = new SalaChatSessaoEscutaAtiva(pSala, (ItfListenerEventoMatrix) construtor.newInstance(pSala));
            System.out.println("Novo Listener adicionado para sala " + novaSala.getSala().getCodigoChat());
            monitor.adicionarLister(novaSala);
            mapasalaSessaoAtiva.put(pSala.getCodigoChat(), novaSala);
            return mapasalaSessaoAtiva.get(pSala.getCodigoChat());
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException("A classe de escuta não possui um método construtor com " + ItfChatSalaBean.class.getSimpleName());
        } catch (SecurityException ex) {
            throw new UnsupportedOperationException("A classe de escuta não possui um método público de construtor com " + ItfChatSalaBean.class.getSimpleName());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new UnsupportedOperationException("Falha instanciando" + classeEscutaSalas.getSimpleName());
        }

    }

    public String salaEnviarMesagem(ItfChatSalaBean pSala, String pMensagem) throws ErroConexaoServicoChat {
        return salaEnviarMesagem(pSala, null, null, pMensagem);
    }

    @Override
    public String salaEnviarMesagem(ItfChatSalaBean pSala, ItfUsuarioChat pUsuario, String pcodigoMensagem, String pMensagem) throws ErroConexaoServicoChat {
        ItfChatSalaBean sala = null;

        if (pSala.getCodigoChat() == null) {
            sala = getSalaCriandoSeNaoExistir(pSala);
        } else {
            sala = pSala;
        }
        if (pUsuario != null) {
            if (pUsuario.getCodigoUsuario().equals(getUsuarioAdmin().getCodigoUsuario())) {
                pUsuario = null;
            }
        }

        String codigoMensagem = pcodigoMensagem;
        if (UtilSBCoreStringValidador.isNuloOuEmbranco(codigoMensagem)) {
            codigoMensagem = String.valueOf((new Date().getTime() + pMensagem.hashCode()));
        }
        ItfRespostaWebServiceSimples resp = null;

        boolean temTokenvalido;
        try {
            temTokenvalido = validarTokenOuGerarNovo(pUsuario, null, null);
        } catch (ErroRegraDeNEgocioChat ex) {
            temTokenvalido = false;
        }

        if (!temTokenvalido) {
            return null;
        }
        if (pUsuario == null) {

            resp = FabApiRestIntMatrixChatSalas.SALA_ENVIAR_MENSAGEM_TEXTO_SIMPLES
                    .getAcao(pSala.getCodigoChat(), codigoMensagem, pMensagem).getResposta();
        } else {
            final ItfUsuarioChat usuarioMensagem = pUsuario;
            if (!pSala.getUsuarios().stream().filter(usr -> usr.getCodigoUsuario().equals(usuarioMensagem.getCodigoUsuario())).findFirst().isPresent()) {
                salaAdicionarMembro(pSala, pUsuario.getCodigoUsuario());
            }

            GestaoTokenRestIntmatrixchat gestao = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatSalas.SALA_ENVIAR_MENSAGEM_TEXTO_SIMPLES.getGestaoToken(pUsuario);
            if (!gestao.isTemTokemAtivo()) {
                gestao.gerarNovoToken();
            }
            if (!gestao.isTemTokemAtivo()) {
                throw new ErroConexaoServicoChat("Falha autenticando com o usuário " + pUsuario.getNome());
            }
            resp = FabApiRestIntMatrixChatSalas.SALA_ENVIAR_MENSAGEM_TEXTO_SIMPLES
                    .getAcao(pUsuario, pSala.getCodigoChat(), codigoMensagem, pMensagem).getResposta();
        }
        if (!resp.isSucesso()) {
            throw new ErroConexaoServicoChat("Falha enviando mensagem de texto simples " + resp.getRespostaTexto());
        } else {
            System.out.println("mensagem enviada com sucesso");
            return resp.getRespostaComoObjetoJson().getString("event_id");
        }

    }

    @Override
    public String salaEnviarImagem(ItfChatSalaBean pSala, ItfUsuarioChat pUsuario, String pCodigoMensagem, String pNomeArquivo, InputStream pInput) throws ErroConexaoServicoChat {
        if (pInput == null) {
            throw new ErroConexaoServicoChat("Falha registrando imagem, stream de imagem nulo enviado");
        }
        ItfRespostaWebServiceSimples resp = FabApiRestMatrixMedia.UPLOAD_ARQUIVO.getAcao(pNomeArquivo,
                pInput).getResposta();
        System.out.println(resp.getRespostaTexto());

        if (!resp.isSucesso()) {

            throw new ErroConexaoServicoChat("Falha registrando arquivo no serviço de mensagens" + resp.getRespostaTexto());
        }

        String codigoMensagem = pCodigoMensagem;
        if (UtilSBCoreStringValidador.isNuloOuEmbranco(codigoMensagem)) {
            codigoMensagem = String.valueOf((new Date().getTime() + pInput.hashCode() + pUsuario.hashCode()));
        }
        String uriImagem = resp.getRespostaComoObjetoJson().getString("content_uri");
        System.out.println("Enviando imagem com uri" + uriImagem);
        ItfRespostaWebServiceSimples respEnvioImagem = FabApiRestIntMatrixChatSalas.SALA_ENVIAR_MENSAGEM_IMAGEM.
                getAcao(pSala.getCodigoChat(), codigoMensagem, pNomeArquivo, uriImagem).getResposta();

        return respEnvioImagem.getRespostaComoObjetoJson().getString("event_id");

    }

    @Override
    public String salaEnviarDocumento(ItfChatSalaBean pSala, ItfUsuarioChat pUsuario, String pCodigoMensagem, String pNomeArquivo, InputStream pInput) throws ErroConexaoServicoChat {
        if (pInput == null) {
            throw new ErroConexaoServicoChat("Falha registrando imagem, stream de imagem nulo enviado");
        }
        ItfRespostaWebServiceSimples resp = FabApiRestMatrixMedia.UPLOAD_ARQUIVO.getAcao(pNomeArquivo,
                pInput).getResposta();
        System.out.println(resp.getRespostaTexto());

        if (!resp.isSucesso()) {
            return salaEnviarMesagem(pSala, null, pCodigoMensagem, "Ouve falha enviando um documento para o servidor Matrix");
            //throw new ErroConexaoServicoChat("Falha registrando no serviço de mensagens");
        }

        String codigoMensagem = pCodigoMensagem;
        if (UtilSBCoreStringValidador.isNuloOuEmbranco(codigoMensagem)) {
            codigoMensagem = String.valueOf((new Date().getTime() + pInput.hashCode() + pUsuario.hashCode()));
        }
        String uriImagem = resp.getRespostaComoObjetoJson().getString("content_uri");
        ItfRespostaWebServiceSimples respEnvioImagem = FabApiRestIntMatrixChatSalas.SALA_ENVIAR_MENSAGEM_DOCUMENTO.
                getAcao(pSala.getCodigoChat(), codigoMensagem, pNomeArquivo, uriImagem).getResposta();

        return respEnvioImagem.getRespostaComoObjetoJson().getString("event_id");

    }

    @Override
    public String salaEnviarVideo(ItfChatSalaBean pSala, ItfUsuarioChat pUsuario, String codigoMensagem, String pNomeArquivo, InputStream pInput) throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples resp = FabApiRestMatrixMedia.UPLOAD_ARQUIVO.getAcao(pNomeArquivo,
                pInput).getResposta();
        System.out.println(resp.getRespostaTexto());

        if (!resp.isSucesso()) {
            throw new ErroConexaoServicoChat("Falha registrando arquivo no serviço de mensagens");
        }
        String uriImagem = resp.getRespostaComoObjetoJson().getString("content_uri");
        ItfRespostaWebServiceSimples respEnvioImagem = FabApiRestIntMatrixChatSalas.SALA_ENVIAR_MENSAGEM_VIDEO.
                getAcao(pSala.getCodigoChat(), codigoMensagem, pNomeArquivo, uriImagem).getResposta();
        return respEnvioImagem.getRespostaComoObjetoJson().getString("event_id");
    }

    @Override
    public String salaEnviarAudio(ItfChatSalaBean pSala, ItfUsuarioChat pUsuario, String pcodigoMensagem, String pNomeArquivo, InputStream pInput) throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples resp = FabApiRestMatrixMedia.UPLOAD_ARQUIVO.getAcao(pNomeArquivo,
                pInput).getResposta();
        System.out.println(resp.getRespostaTexto());

        String codigoMensagem = pcodigoMensagem;
        if (UtilSBCoreStringValidador.isNuloOuEmbranco(codigoMensagem)) {
            codigoMensagem = String.valueOf((new Date().getTime() + pInput.hashCode() + pUsuario.hashCode()));
        }

        if (!resp.isSucesso()) {

            throw new ErroConexaoServicoChat("Falha registrando arquivo no serviço de mensagens");
        }
        String uriImagem = resp.getRespostaComoObjetoJson().getString("content_uri");
        ItfRespostaWebServiceSimples respEnvioImagem = FabApiRestIntMatrixChatSalas.SALA_ENVIAR_MENSAGEM_AUDIO.
                getAcao(pSala.getCodigoChat(), codigoMensagem, pNomeArquivo, uriImagem).getResposta();
        if (!respEnvioImagem.isSucesso()) {

            throw new ErroConexaoServicoChat("Falha enviando mensagem");
        }

        return respEnvioImagem.getRespostaComoObjetoJson().getString("event_id");
    }

    @Override
    public List<ItfNotificacaoUsuarioChat> getUltimasNotificacoesUsuarioAdmin() throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples resp = FabApiRestMatrixNotificacoes.MINHAS_NOTIFICACOES_LISTAR.getAcao().getResposta();
        JsonObject respJson = resp.getRespostaComoObjetoJson();
        List<ItfNotificacaoUsuarioChat> notificacoes = new ArrayList<>();
        if (respJson != null) {
            if (respJson.containsKey("notifications")) {
                JsonArray jsonArray = respJson.getJsonArray("notifications");
                for (JsonValue notificacaoJson : jsonArray) {
                    try {

                        ItfNotificacaoUsuarioChat notificacao = ERPChat.MATRIX_ORG.getDTO(notificacaoJson.toString(),
                                ItfNotificacaoUsuarioChat.class);
                        notificacoes.add(notificacao);

                    } catch (ErroJsonInterpredador ex) {
                        Logger.getLogger(ChatMatrixOrgimpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        }
        return notificacoes;
    }

    @Override
    public String enviarDirect(String pCodigoUsuario, String pMensagem) throws ErroConexaoServicoChat {

        if (!isTemChaveValida()) {
            throw new ErroConexaoServicoChat("Falha validando chave de acesso admin");
        }

        if (RepostorioDirectsAdmin.getRoomIDDirectAdminByUsuario(pCodigoUsuario) == null) {
            ItfRespostaWebServiceSimples resp = FabApiRestInteMatrixChatDirect.DIRECT_CRIAR_SALA.getAcao(pCodigoUsuario).getResposta();
            if (resp.isSucesso()) {
                String roomID = resp.getRespostaComoObjetoJson().getString("room_id");

                RepostorioDirectsAdmin.adicionarNovoDirect(pCodigoUsuario, roomID);
            }
        }

        String roomId = RepostorioDirectsAdmin.getRoomIDDirectAdminByUsuario(pCodigoUsuario);

        ItfChatSalaBean saladirect = getSalaByCodigo(roomId);

        boolean temUsuario = saladirect.getUsuarios().stream().filter(usr -> usr.getCodigoUsuario().equals(pCodigoUsuario)).findFirst().isPresent();
        if (!temUsuario) {
            salaAdicionarMembro(saladirect, pCodigoUsuario);
        }

        return salaEnviarMesagem(saladirect, pMensagem);

    }

    @Override
    public boolean usuarioLogadoObterChaveAcesso(String pUserName, String pSenha) {
        GestaoTokenRestIntmatrixchat gestao = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIOS_STATUS.getGestaoToken(SBCore.getUsuarioLogado());
        if (gestao.isTemTokemAtivo()) {
            return true;
        }

        gestao.excluirToken();
        gestao.setLoginNomeUsuario(pUserName);
        gestao.setLoginSenhaUsuario(pSenha);
        gestao.gerarNovoToken();
        return gestao.isTemTokemAtivo();
    }

    @Override
    public boolean usuarioLogadovalidarChaveAcessoRegistrada() {
        GestaoTokenRestIntmatrixchat gestao = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIOS_STATUS.getGestaoToken(SBCore.getUsuarioLogado());
        if (gestao == null) {
            return false;
        }
        return gestao.validarToken();
    }

    @Override
    public boolean validarTokenOuGerarNovo(ItfUsuarioChat pUsuario, String pCodigo, String pSenha) throws ErroRegraDeNEgocioChat, ErroConexaoServicoChat {
        String senha = pSenha;
        String codUsuario = pCodigo;
        if (pUsuario != null) {
            if (getUsuarioAdmin().getCodigoUsuario().equals(pUsuario.getCodigoUsuario())) {
                pUsuario = null;
            }
        }

        GestaoTokenRestIntmatrixchat gestao = null;
        if (pUsuario == null) {
            gestao = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIOS_STATUS.getGestaoToken();
        } else {
            gestao = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIOS_STATUS.getGestaoToken(pUsuario);
        }

        if (gestao == null) {
            return false;
        }

        boolean validacaoToken = gestao.validarToken();
        if (validacaoToken) {
            return true;
        } else {

            gestao.excluirToken();
            if (pUsuario != null) {

                if (pSenha == null) {
                    codUsuario = pUsuario.getCodigoUsuario();
                    // codUsuario = codUsuario.substring(1, codUsuario.indexOf(":"));

                    if (codUsuario.contains(".ct:")) {
                        senha = gerarSenhaPadrao(pUsuario);
                    }

                }

                if (UtilSBCoreStringValidador.isNuloOuEmbranco(codUsuario) || UtilSBCoreStringValidador.isNuloOuEmbranco(senha)) {
                    return false;
                }
                gestao.setLoginNomeUsuario(codUsuario);
                gestao.setLoginSenhaUsuario(senha);
            }
            gestao.gerarNovoToken();
            boolean conexaoUsuarioValidade = gestao.validarToken();
            if (conexaoUsuarioValidade) {
                return true;
            }

            if (senha != null && codUsuario != null) {
                if (pUsuario.getEmail() == null) {
                    usuarioAtualizarSenha(codUsuario, senha);
                }
            }

            gestao.excluirToken();
            gestao.setLoginNomeUsuario(codUsuario);
            gestao.setLoginSenhaUsuario(senha);
            gestao.gerarNovoToken();
            conexaoUsuarioValidade = gestao.validarToken();
            return conexaoUsuarioValidade;
        }

    }

    @Override
    public boolean validarTokenSistema() {
        try {
            return validarTokenOuGerarNovo(null, codigoUsuarioAdmin, codigoUsuarioAdmin);
        } catch (ErroRegraDeNEgocioChat | ErroConexaoServicoChat ex) {
            return false;
        }
    }

    @Override
    public ItfUsuarioChat usuarioAtualizar(String pCodigo, String pNome, String pEmail, String pTelefone) throws ErroConexaoServicoChat {
        String telefone = UtilSBCoreStringTelefone.gerarCeluarInternacional(pTelefone.replace("+", ""));
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatUsuarios.USUARIO_ATUALIZAR.getAcao(
                pCodigo,
                pNome,
                pEmail,
                telefone
        ).getResposta();

        if (!resposta.isSucesso()) {
            System.out.println(resposta.getRespostaTexto());
            resposta.dispararMensagens();
            System.out.println("Falha criando usuário ");
            return null;
        } else {
            System.out.println("Usuário atualizado com sucesso");
        }
        try {
            System.out.println("Criando DTO");
            ItfUsuarioChat usaurio = ERPChat.MATRIX_ORG.getDTO(resposta.getRespostaTexto(), ItfUsuarioChat.class);

            return registraUsuarioPorCodigo(usaurio);
        } catch (ErroJsonInterpredador ex) {
            System.out.println("FALHA Criando DTO");
            Logger.getLogger(ChatMatrixOrgimpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public ItfUsuarioChat usuarioAtualizar(ItfUsuarioChat pUsuario) throws ErroConexaoServicoChat {
        validarTokenSistema();
        return usuarioAtualizar(pUsuario.getCodigoUsuario(), pUsuario.getNome(), pUsuario.getEmailPrincipal(), pUsuario.getTelefone());
    }

    @Override
    public ItfUsuarioChat usuarioCriar(ItfUsuarioChat pUsuario, String pSenha) throws ErroConexaoServicoChat {
        if (!isTemChaveValida()) {
            System.out.println("NÃO TEM CHAVE VÁLIDA IMPOSSÍVEL CRIAR USUÁRIO");
            return null;
        }
        if (pUsuario.getEmail() != null) {

        }
        if (pUsuario.getCodigoUsuario() == null) {
            throw new UnsupportedOperationException("Usuário sem código enviado");
        }

        if (mapaUsuarioChatByEmail.get(pUsuario.getEmail()) != null) {
            return mapaUsuarioChatByEmail.get(pUsuario.getEmail());
        }

        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatUsuarios.USUARIO_CRIAR.getAcao(
                pUsuario.getCodigoUsuario(),
                pUsuario.getNome(),
                pUsuario.getEmail(),
                pUsuario.getTelefone(),
                pSenha).getResposta();
        if (!resposta.isSucesso()) {
            System.out.println(resposta.getRespostaTexto());
            resposta.dispararMensagens();
            System.out.println("Falha criando usuário ");
            return null;
        } else {
            System.out.println("Usuário criado com sucesso");
        }
        try {
            System.out.println("Criando DTO");
            ItfUsuarioChat usaurio = ERPChat.MATRIX_ORG.getDTO(resposta.getRespostaTexto(), ItfUsuarioChat.class);

            return registraUsuarioPorCodigo(usaurio);
        } catch (ErroJsonInterpredador ex) {
            System.out.println("FALHA Criando DTO");
            Logger.getLogger(ChatMatrixOrgimpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean salaNotificarLeitura(String pCodigoSala, ItfUsuarioChat pUsuario, String pCodigoReciboMatix) {

        try {
            validarTokenOuGerarNovo(pUsuario, pUsuario.getCodigoUsuario(), gerarSenhaPadrao(pUsuario));
        } catch (ErroRegraDeNEgocioChat | ErroConexaoServicoChat ex) {
            return false;
        }

        ItfRespostaWebServiceSimples resp = FabApiRestIntMatrixChatSalas.SALA_MARCAR_COMO_LIDO.getAcao(pUsuario, pCodigoSala, pCodigoReciboMatix).getResposta();
        return resp.isSucesso();
    }

    @Override
    public String gerarAliasIdentificadorCanonico(String pNomeCurtoAlias) {
        return UtilMatrixERP.gerarAliasSalaIDCanonico(pNomeCurtoAlias);

    }

    @Override
    public ItfUsuarioChat getUsuarioAdmin() {
        try {
            return getUsuarioByCodigo(getCodigoUsuarioAdmin());
        } catch (ErroConexaoServicoChat ex) {
            return null;
        }
    }

    @Override
    public String gerarCodigoUsuarioContato(String pNome, String pWhatsappTelefone) throws ErroRegraDeNEgocioChat, ErroConexaoServicoChat {
        if (pNome == null || pNome.length() < 2) {
            throw new ErroRegraDeNEgocioChat("O nome não é válido");
        }
        String telefone = UtilSBCoreStringTelefone.gerarCeluarInternacional(pWhatsappTelefone);
        if (telefone == null) {
            throw new ErroRegraDeNEgocioChat("O Telefone não é válido");
        }

        ItfUsuarioChat usr = getUsuarioByTelefone(pWhatsappTelefone);
        if (usr != null && !usr.getCodigoUsuario().contains(".ct:")) {
            if (usr.getCodigoUsuario().contains(".at:")) {
                throw new ErroRegraDeNEgocioChat("Já existe um usuário de atendimento vinculado ao número  " + pWhatsappTelefone);
            } else {
                throw new ErroRegraDeNEgocioChat("Um usuário vinculado ao telefone " + pWhatsappTelefone + " foi encontrado, mas o codigo não é do tipo Contato");
            }
        }
        if (usr != null) {
            return usr.getCodigoUsuario();
        } else {

            String codigo = gerarCodigoUsuario(pNome, telefone, "ct");
            return codigo;
        }

    }

    @Override
    public ItfUsuarioChat gerarUsuarioContato(String pNome, String pTelefoneInternacional) throws ErroConexaoServicoChat, ErroRegraDeNEgocioChat {
        String telefoneInternacional = UtilSBCoreStringTelefone.gerarCeluarInternacional(pTelefoneInternacional);

        //telefoneInternacional = telefoneInternacional.replace("+", "");
        if (telefoneInternacional == null) {
            throw new ErroRegraDeNEgocioChat("Telefone inválido" + pTelefoneInternacional);
        }
        String codigoUsuario = gerarCodigoUsuarioContato(pNome, telefoneInternacional);
        ItfUsuarioChat usuario = getUsuarioByCodigo(codigoUsuario);

        if (usuario == null) {

            UsuarioChatMatrixOrg usuariochat = new UsuarioChatMatrixOrg();
            usuariochat.setNome(pNome);
            usuariochat.setTelefone(telefoneInternacional);
            usuariochat.setCodigoUsuario(codigoUsuario);
            ItfUsuarioChat novoUsuario = usuarioCriar(usuariochat);
            if (novoUsuario == null) {
                throw new ErroConexaoServicoChat("Falha criando usuário tipo contato");
            }

            usuarioAtualizarSenha(codigoUsuario, gerarSenhaPadrao(usuario));

            usuario = novoUsuario;
        } else {
            boolean teveAlteracao = false;
            if (usuario.getTelefone() == null || !usuario.getTelefone().equals(telefoneInternacional)) {
                teveAlteracao = true;
            }
            if (usuario.getNome() == null || !usuario.getNome().equals(pNome)) {
                teveAlteracao = true;
            }
            if (teveAlteracao) {
                UsuarioChatMatrixOrg usuarioAtualizacao = new UsuarioChatMatrixOrg();
                usuarioAtualizacao.setNome(pNome);
                usuarioAtualizacao.setTelefone(telefoneInternacional);
                usuarioAtualizacao.setCodigoUsuario(usuario.getCodigoUsuario());
                usuario = usuarioAtualizar(usuarioAtualizacao);

            }

        }
        return usuario;
    }

    @Override
    public ItfUsuarioChat gerarUsuarioAtendimento(String pNome, String pEmail) throws ErroConexaoServicoChat, ErroRegraDeNEgocioChat {
        ItfUsuarioChat usuario = getUsuarioByEmail(pEmail);
        if (usuario == null) {

            UsuarioChatMatrixOrg usuariochat = new UsuarioChatMatrixOrg();
            usuariochat.setNome(pNome);
            usuariochat.setEmail(pEmail);
            String nomeOriginal = pNome;

            String usuarioPart1 = UtilSBCoreStringsCammelCase.getCamelByTextoPrimeiraLetraMaiuscula(nomeOriginal);
            usuarioPart1 = UtilSBCoreStringSlugs.gerarSlugSimples(usuarioPart1);

            usuariochat.setApelido(usuarioPart1);
            String codigoUsuario = gerarCodigoUsuarioAtendimento(pNome, pEmail);

            usuariochat.setCodigoUsuario(codigoUsuario);
            ItfUsuarioChat novoUsuario = usuarioCriar(usuariochat);
            usuario = novoUsuario;
        }
        return usuario;
    }

    private String gerarCodigoUsuario(String pIdentificadoHumano, String pIdentificadorSistema, String pTipo) {

        String identificadorHumano = UtilSBCoreStringsCammelCase.getCamelCaseTextoSemAcentuacaoECaracterEspecial(pIdentificadoHumano);
        identificadorHumano = UtilSBCoreStringFiltros.getPrimeirasXLetrasDaString(identificadorHumano, 50);
        String identificadorSistema = UtilSBCoreStringFiltros.removeCaracteresEspeciaisAcentoMantendoApenasLetrasNumerosEspaco(pIdentificadorSistema);
        String codigoUsuario = UtilsbApiMatrixChat.gerarCodigoBySlugUser(identificadorHumano + "." + identificadorSistema + "." + pTipo);
        return codigoUsuario;
    }

    @Override
    public String gerarCodigoUsuarioAtendimento(String pNome, String pEmail) throws ErroConexaoServicoChat, ErroRegraDeNEgocioChat {
        if (pNome == null || pNome.length() < 2) {
            throw new ErroRegraDeNEgocioChat("O nome não é válido");
        }
        if (pEmail == null || !pEmail.contains(".") || !pEmail.contains("@")) {
            throw new ErroRegraDeNEgocioChat("O email não é válido");
        }
        ItfUsuarioChat usr = getUsuarioByEmail(pEmail);
        if (usr != null) {
            return usr.getCodigoUsuario();
        } else {

            String codigo = gerarCodigoUsuario(pNome, pEmail, "at");
            return codigo;
        }

    }

}
