package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix.SessaoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.ChatMatrixOrg;
import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroRegraDeNEgocioChat;
import br.org.coletivoJava.fw.api.erp.chat.ItfErpChatService;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfNotificacaoUsuarioChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import br.org.coletivoJava.fw.api.erp.chat.notificacoes.ItfRetornoDeChamadaDeNotificacao;
import br.org.coletivoJava.fw.api.erp.chat.notificacoes.SincronizacaoNotificacoes;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.FabTipoSalaMatrix;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.RespostaSalaApiEscuta;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaChatSessaoEscutaAtiva;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.UsuarioChatMatrixOrg;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ComandoDeAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ErroComandoAtendimentoInvalido;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoComandoAtendimento;
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
import com.super_bits.modulosSB.SBCore.modulos.Controller.Interfaces.permissoes.ErroDadosDeContatoUsuarioNaoEncontrado;
import com.super_bits.modulosSB.SBCore.modulos.erp.ErroJsonInterpredador;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ComoUsuario;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.contato.ComoContatoHumano;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.io.InputStream;
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

    private static Map<String, ComoUsuarioChat> mapaUsuarioChatByEmail = new HashMap<>();
    private static Map<String, ComoUsuarioChat> mapaUsuarioChatByTelefone = new HashMap<>();
    private static Map<String, ComoUsuarioChat> mapaUsuarioChatByCodigo = new HashMap<>();
    private static Map<String, SalaChatSessaoEscutaAtiva> mapasalaSessaoAtiva = new HashMap<>();
    private static Class classeEscutaSalas;
    private static Class classeEscutaNotificacao;

    private static SessaoMatrix monitor;
    private final static RespostaSalaApiEscuta respSalaApi = new RespostaSalaApiEscuta();
    private static ConfigModulo configuracao;
    private static SincronizacaoNotificacoes sinc;
    private static String codigoUsuarioAdmin;

    @Override
    public String gerarSenhaPadrao(ComoUsuario pUsuario, String pCodigoUsuario) throws ErroRegraDeNEgocioChat {
        if (!pCodigoUsuario.contains(".ct:")) {
            throw new ErroRegraDeNEgocioChat("Apenas usuários do tipo contato tem senhas autogerenciadas");
        }
        ComoContatoHumano contato = null;
        try {
            contato = SBCore.getServicoPermissao().getContatoDoUsuario(pUsuario);
            if (contato == null) {
                throw new ErroRegraDeNEgocioChat("Os dados de contato do usuário " + pUsuario.getNome() + " não foram encontrados");
            }
        } catch (ErroDadosDeContatoUsuarioNaoEncontrado ex) {
            throw new ErroRegraDeNEgocioChat("Os dados de contato do usuário " + pUsuario.getNome() + " não foram encontrados");
        }

        if (contato.getEmail() != null) {

            if (pUsuario.getEmail().split("@")[1].endsWith(FabConfigApiMatrixChat.DOMINIO_FEDERADO.getValorParametroSistema())) {
                throw new ErroRegraDeNEgocioChat("Usuário possui e-mail corporativo, senhas autogerenciadas, são apenas para usuários que acessam exclusivamente pelo whasapp");
            }

        }

        StringBuilder senha = new StringBuilder();
        String telefone = null;
        try {
            telefone = SBCore.getServicoPermissao().getContatoDoUsuario(pUsuario).getCelular();
        } catch (ErroDadosDeContatoUsuarioNaoEncontrado ex) {
            Logger.getLogger(ChatMatrixOrgimpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (pUsuario.getTelefone() == null) {
            throw new ErroRegraDeNEgocioChat("Usuário sem telefone, senhas autogerenciadas, são apenas para usuários que acessam exclusivamente pelo whasapp");
        }

        senha.append(UtilSBCoreStringTelefone.gerarNumeroTelefoneInternacional(telefone));
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
    public ComoChatSalaBean getSalaByAlias(String pAlias) throws ErroConexaoServicoChat {
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
    public ComoChatSalaBean getSalaByNome(String pNomeSala) throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatSalas.SALA_ENCONTRAR_POR_NOME.getAcao(pNomeSala).getResposta();

        JsonObject respJson = resposta.getRespostaComoObjetoJson();
        if (!respJson.getJsonArray("rooms").isEmpty()) {
            JsonObject jsonSala = respJson.getJsonArray("rooms").get(0).asJsonObject();
            if (respJson.getInt("total_rooms") > 0) {
                System.out.println(UtilSBCoreJson.getTextoByJsonObjeect(jsonSala));
                try {
                    ComoChatSalaBean sala = ERPChat.MATRIX_ORG.getDTO(UtilSBCoreJson.getTextoByJsonObjeect(jsonSala), ComoChatSalaBean.class);

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
    public ComoChatSalaBean getSalaByCodigo(String pCodigoSala) throws ErroConexaoServicoChat {
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
                    ComoChatSalaBean sala = ERPChat.MATRIX_ORG.getDTO(UtilSBCoreJson.getTextoByJsonObjeect(jsonSala), ComoChatSalaBean.class);

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
    public ComoChatSalaBean espacoCriar(String pNomeVisivel, String pAliasUnico) throws ErroConexaoServicoChat {
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixSpaces.ESPACO_CRIAR.getAcao(pNomeVisivel, pAliasUnico).getResposta();
        if (resposta.isSucesso()) {
            System.out.println("RESPONDEU COM SUCESSO SEM ROMID: pegaí:");
            System.out.println(resposta.getRespostaTexto());
            JsonObject json = resposta.getRespostaComoObjetoJson();
            String codigo = json.getString("room_id");
            ComoChatSalaBean sala = getSalaByCodigo(codigo);
            return sala;
        } else {
            return getSalaByAlias(pAliasUnico);
        }
    }

    @Override
    public boolean salaLerUltimoEvento(String pCodigoSala, ComoUsuarioChat pUsuarioLeitura) throws ErroConexaoServicoChat {
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

    private boolean isUmUsuarioAtendimento(String pCodigo) {
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
    public boolean isUmUsuarioAtendimento(ComoUsuarioChat pUsuarioAtendimento) {
        if (pUsuarioAtendimento == null) {
            return false;
        }
        if (pUsuarioAtendimento.getEmail() != null && pUsuarioAtendimento.getEmail().contains("@")) {

            if (pUsuarioAtendimento.getEmail().split("@")[1].endsWith(FabConfigApiMatrixChat.DOMINIO_FEDERADO.getValorParametroSistema())) {
                return true;
            }
        }
        return isUmUsuarioAtendimento(pUsuarioAtendimento.getCodigoUsuario());
    }

    @Override
    public boolean isUmUsuarioContato(ComoUsuarioChat pUsuarioContato) {
        if (pUsuarioContato == null) {
            return false;
        }
        if (pUsuarioContato.getCodigoUsuario().equals(getUsuarioAdmin().getCodigoUsuario())) {
            return false;
        }
        if (pUsuarioContato.getEmail() != null && pUsuarioContato.getEmail().contains("@")) {

            if (pUsuarioContato.getEmail().split("@")[1].endsWith(FabConfigApiMatrixChat.DOMINIO_FEDERADO.getValorParametroSistema())) {
                return false;
            }
        }

        return isUmUsuarioContato(pUsuarioContato.getCodigoUsuario());
    }

    @Override
    public boolean salaTornarMembroAdmin(ComoChatSalaBean pSala, String pCodigoMembro) throws ErroConexaoServicoChat {
        try {
            ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatSalas.SALA_PERMICOES_VISUALIZAR.getAcao(pSala.getCodigoChat()).getResposta();
            JsonObject jsonPowerLevels = resposta.getRespostaComoObjetoJson();
            // --- editar a chave "users" adicionando um novo usuário ---
            JsonObjectBuilder usersBuilder = Json.createObjectBuilder(
                    jsonPowerLevels.getJsonObject("users")
            );
            usersBuilder.add(pCodigoMembro, 100);

            // reconstruir o objeto final, copiando os campos antigos e substituindo "users"
            JsonObjectBuilder finalBuilder = Json.createObjectBuilder(jsonPowerLevels);
            finalBuilder.add("users", usersBuilder);

            return FabApiRestIntMatrixChatSalas.SALA_PERMICOES_ATUALIZAR.getAcao(pSala.getCodigoChat(), UtilSBCoreJson.getTextoByJsonObjeect(finalBuilder.build())).getResposta().isSucesso();
        } catch (Throwable t) {
            return false;
        }
    }

    enum TIPO_INDENTIFICACAO_SALA {
        NOME,
        APELIDO,
        CODIGO
    }

    @Override
    public ComoChatSalaBean getSalaCriandoSeNaoExistir(final ComoChatSalaBean pSalaDados, String pIdentificador) throws ErroConexaoServicoChat {
        if (!isTemChaveValida()) {
            return null;
        }
        TIPO_INDENTIFICACAO_SALA tipoID = null;
        ComoChatSalaBean sala = null;

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
        Optional<ComoChatSalaBean> pesquisaSala = null;
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

            for (ComoUsuarioChat usuario : pSalaDados.getUsuarios()) {
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
                ComoChatSalaBean espaco = getSalaByAlias(tipoSala.getApelidoNomeUnicoSpace());
                System.out.println("Codigo espaço:");
                System.out.println(espaco.getCodigoChat());
                ItfRespostaWebServiceSimples resp2 = FabApiRestIntMatrixSpaces.ESPACO_ADICIONAR_FILHO_DO_ESPACO.getAcao(espaco.getCodigoChat(), sala.getCodigoChat()).getResposta();
            }

        }
        return sala;
    }

    @Override
    public ComoChatSalaBean getSalaCriandoSeNaoExistir(final ComoChatSalaBean pSala) throws ErroConexaoServicoChat {
        if (pSala.getCodigoChat() != null) {
            return getSalaCriandoSeNaoExistir(pSala, pSala.getCodigoChat());
        }
        if (pSala.getApelido() != null) {
            return getSalaCriandoSeNaoExistir(pSala, pSala.getApelido());
        }
        if (pSala.getNome() != null) {
            return getSalaCriandoSeNaoExistir(pSala, pSala.getNome());
        }
        ComoChatSalaBean sala = getSalaCriandoSeNaoExistir(pSala, pSala.getCodigoChat());

        return sala;

    }

    @Override
    public boolean salaExcluir(ComoChatSalaBean pCodigoSalsa) throws ErroConexaoServicoChat {
        if (!isTemChaveValida()) {
            return false;
        }
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatSalas.SALA_EXLUIR.getAcao(pCodigoSalsa.getCodigoChat()).getResposta();
        return resposta.isSucesso();
    }

    @Override
    public List<ComoUsuarioChat> atualizarListaDeUsuarios() throws ErroConexaoServicoChat {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ComoUsuarioChat getUsuarioByTelefone(String pTelefone) throws ErroConexaoServicoChat {
        pTelefone = UtilSBCoreStringTelefone.gerarNumeroTelefoneInternacional(pTelefone);
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
            ComoUsuarioChat usuario = ERPChat.MATRIX_ORG.getDTO(respostaUser.getRespostaTexto(), ComoUsuarioChat.class);
            System.out.println("DTO gerado com sucesso");
            return registraUsuarioPorTelefone(usuario);

        } catch (ErroJsonInterpredador ex) {
            System.out.println("Falha processando usuário" + ex.getMessage());
            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha processando usuário" + ex.getMessage(), ex);
            return null;
        }
    }

    private ComoUsuarioChat registraUsuarioPorEmail(ComoUsuarioChat pUsuario) {
        mapaUsuarioChatByEmail.put(pUsuario.getEmail(), pUsuario);
        mapaUsuarioChatByCodigo.put(pUsuario.getCodigoUsuario(), pUsuario);
        return pUsuario;
    }

    private ComoUsuarioChat registraUsuarioPorCodigo(ComoUsuarioChat pUsuario) {

        mapaUsuarioChatByCodigo.put(pUsuario.getCodigoUsuario(), pUsuario);
        return pUsuario;
    }

    private ComoUsuarioChat registraUsuarioPorTelefone(ComoUsuarioChat pUsuario) {
        String telefone = UtilSBCoreStringTelefone.gerarNumeroTelefoneInternacional(pUsuario.getTelefone());
        mapaUsuarioChatByEmail.put(telefone, pUsuario);
        mapaUsuarioChatByCodigo.put(pUsuario.getCodigoUsuario(), pUsuario);
        return pUsuario;
    }

    @Override
    public ComoUsuarioChat getUsuarioByEmail(String pEmail) throws ErroConexaoServicoChat {

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
            if (!gestaoToken.isTemTokemAtivo()) {
                throw new ErroConexaoServicoChat(resposta.getRespostaTexto());

            }
        }
        String userId = resposta.getRespostaComoObjetoJson().getString("user_id");
        ItfRespostaWebServiceSimples respostaUser = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getAcao(userId).getResposta();
        System.out.println("processando:");
        System.out.println(respostaUser.getRespostaTexto());
        try {
            ComoUsuarioChat usuario = ERPChat.MATRIX_ORG.getDTO(respostaUser.getRespostaTexto(), ComoUsuarioChat.class);
            System.out.println("DTO gerado com sucesso");
            return registraUsuarioPorEmail(usuario);

        } catch (ErroJsonInterpredador ex) {
            System.out.println("Falha processando usuário" + ex.getMessage());
            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha processando usuário" + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public ComoUsuarioChat getUsuarioByCodigo(String pCodigo) throws ErroConexaoServicoChat {
        if (mapaUsuarioChatByCodigo.containsKey(pCodigo)) {
            return mapaUsuarioChatByCodigo.get(pCodigo);
        }
        if (!isTemChaveValida()) {
            return null;
        }
        ItfRespostaWebServiceSimples respostaUser = FabApiRestIntMatrixChatUsuarios.USUARIO_OBTER_DADOS.getAcao(pCodigo).getResposta();
        if (respostaUser.isSucesso()) {

            ComoUsuarioChat usuario;
            System.out.println(respostaUser.getRespostaTexto());
            try {
                usuario = ERPChat.MATRIX_ORG.getDTO(respostaUser.getRespostaTexto(), ComoUsuarioChat.class);
            } catch (ErroJsonInterpredador ex) {
                return null;
            }
            return registraUsuarioPorCodigo(usuario);
        } else {
            return null;
        }

    }

    @Override
    public List<ComoUsuarioChat> getUsuarios() throws ErroConexaoServicoChat {
        //FabApiRestIntMatrixChatUsuarios.USUARIOS_LISTAGEM;
        throw new UnsupportedOperationException("Falha criando usuaário");
    }

    @Override
    public ComoUsuarioChat usuarioCriar(ComoUsuarioChat pUsuario) throws ErroConexaoServicoChat {
        String senha = UtilMatrixERP.gerarSenha(pUsuario);
        return usuarioCriar(pUsuario, senha);
    }

    @Override
    public boolean salaAtualizarMembros(ComoChatSalaBean pSalaRecemAtualizada, List<ComoUsuarioChat> pLista) throws ErroConexaoServicoChat {
        List<ComoUsuarioChat> usuariosAdicionar = new ArrayList<>();
        List<ComoUsuarioChat> usuariosRemover = new ArrayList<>();
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
        for (ComoUsuarioChat usuarioAdd : usuariosAdicionar) {
            salaAdicionarMembro(pSalaRecemAtualizada, usuarioAdd.getCodigoUsuario());
        }

        for (ComoUsuarioChat usuarioRM : usuariosRemover) {
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
    public boolean salaRemoverMembro(ComoChatSalaBean pSala, String pCodigoMembro) throws ErroConexaoServicoChat {
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
    public boolean salaAdicionarMembro(ComoChatSalaBean pSala, String pCodigoUsuario) throws ErroConexaoServicoChat {
        if (!isTemChaveValida()) {
            return false;
        }
        ItfRespostaWebServiceSimples resp = FabApiRestIntMatrixChatSalas.SALA_ADICIONAR_USUARIO.getAcao(pSala.getCodigoChat(), pCodigoUsuario).getResposta();
        return resp.isSucesso();
    }

    @Override
    public ComoUsuarioChat getUsuarioChatByLoginSessaoAtual() {
        ComoUsuario usuariologado = SBCore.getUsuarioLogado();
        ComoUsuarioChat usuario;
        try {
            usuario = getUsuarioByEmail(usuariologado.getEmail());
        } catch (ErroConexaoServicoChat ex) {
            return null;
        }
        return usuario;
    }

    @Override
    public ComoChatSalaBean getSalaAtualizada(ComoChatSalaBean pSala) throws ErroConexaoServicoChat {
        return getSalaByCodigo(pSala.getCodigoChat());
    }

    @Override
    public boolean usuarioAtualizarSenha(String pCodigoUsuario, String pNovaSenha) throws ErroConexaoServicoChat, ErroRegraDeNEgocioChat {
        validarTokenSistema();

        // Verificação inválida, a ideia éra garantir que o usuário aplique a senha de acordo com o email,
        // porém uma vez que o telefone tenha sido alterado, o update do telefone ficaria travado  pois para alterar o telefone precisa primeiro altera a senha, caso seja um usuário desativado
        // if (isUmUsuarioContato(pCodigoUsuario)) {
        //     if (!pNovaSenha.equals(gerarSenhaPadrao(getUsuarioByCodigo(pCodigoUsuario), pCodigoUsuario))) {
        //          throw new ErroRegraDeNEgocioChat("a senha dos usuários de contato são auto gerenciadas" + ", utilise: servico.gerarSenhaPadrao(usuario) para atualizar a senha");
        //     }
        //   }
        ItfRespostaWebServiceSimples resposta = FabApiRestIntMatrixChatUsuarios.USUARIO_ATUALIZAR_SENHA.getAcao(pCodigoUsuario, pNovaSenha).getResposta();
        if (!resposta.isSucesso()) {
            System.out.println(resposta.getRespostaTexto());
        }
        return resposta.isSucesso();
    }

    @Override
    public boolean tokenGestaoEfetuarLogin(ComoUsuarioChat pUsuario, String pSenha) {
        ComoUsuarioChat usuarioChat;
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
    public boolean tokenGestaoEfetuarLogin(ComoUsuario pUsuario) {
        ComoUsuarioChat usuarioChat;
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

    public void registrarClasseEscutaNotificacoes(Class<? extends ItfListenerEventoComandoAtendimento> pClasseEscuta) throws ErroConexaoServicoChat {
        classeEscutaNotificacao = pClasseEscuta;
        if (pClasseEscuta == null) {
            throw new ErroConexaoServicoChat("A classe de escuta é obrigatória");
        }

    }

    private static ItfListenerEventoComandoAtendimento escutaComandos;

    public synchronized void escutaNotificacoes(ComandoDeAtendimento pComando) throws ErroComandoAtendimentoInvalido {
        if (classeEscutaNotificacao == null) {
            throw new ErroComandoAtendimentoInvalido("A classe de escuta das notificações não foi definida");
        }
        if (escutaComandos == null) {
            try {
                escutaComandos = (ItfListenerEventoComandoAtendimento) classeEscutaNotificacao.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ChatMatrixOrgimpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (pComando.getEvento().getRoom_id() != null && !pComando.getEvento().getRoom_id().isEmpty()) {
            escutaComandos.processarComando(pComando);
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
                    Constructor construtor = classeEscutaSalas.getConstructor(ComoChatSalaBean.class);
                    verificadorAutoMonitoramento = (ItfListenerEventoMatrix) construtor.newInstance((ComoChatSalaBean) null);

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

    public synchronized SalaChatSessaoEscutaAtiva salaAbrirSessao(ComoChatSalaBean pSala) throws ErroConexaoServicoChat {

        iniciarConexaoDeEscuta();
        if (pSala == null || UtilSBCoreStringValidador.isNuloOuEmbranco(pSala.getCodigoChat())) {
            throw new UnsupportedOperationException("Codigo da sala não enviado");
        }
        if (mapasalaSessaoAtiva.containsKey(pSala.getCodigoChat())) {
            System.out.println("A sala " + pSala.getCodigoChat() + " Já se encontrava na escuta");
            return mapasalaSessaoAtiva.get(pSala.getCodigoChat());
        }

        try {

            Constructor construtor = classeEscutaSalas.getConstructor(ComoChatSalaBean.class);

            SalaChatSessaoEscutaAtiva novaSala = new SalaChatSessaoEscutaAtiva(pSala, (ItfListenerEventoMatrix) construtor.newInstance(pSala));
            System.out.println("Novo Listener adicionado para sala " + novaSala.getSala().getCodigoChat());
            monitor.adicionarLister(novaSala);
            mapasalaSessaoAtiva.put(pSala.getCodigoChat(), novaSala);
            return mapasalaSessaoAtiva.get(pSala.getCodigoChat());
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException("A classe de escuta não possui um método construtor com " + ComoChatSalaBean.class.getSimpleName());
        } catch (SecurityException ex) {
            throw new UnsupportedOperationException("A classe de escuta não possui um método público de construtor com " + ComoChatSalaBean.class.getSimpleName());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new UnsupportedOperationException("Falha instanciando" + classeEscutaSalas.getSimpleName());
        }

    }

    public String salaEnviarMesagem(ComoChatSalaBean pSala, String pMensagem) throws ErroConexaoServicoChat {
        return salaEnviarMesagem(pSala, null, null, pMensagem);
    }

    @Override
    public String salaEnviarMesagem(ComoChatSalaBean pSala, ComoUsuarioChat pUsuario, String pcodigoMensagem, String pMensagem) throws ErroConexaoServicoChat {
        ComoChatSalaBean sala = null;

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
            if (pUsuario == null) {
                temTokenvalido = validarTokenSistema();
            } else {
                temTokenvalido = validarTokenOuGerarNovo(pUsuario, pUsuario.getCodigoUsuario(), null);
            }
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
            final ComoUsuarioChat usuarioMensagem = pUsuario;
            if (!pSala.getUsuarios().stream().filter(usr -> usr.getCodigoUsuario().equals(usuarioMensagem.getCodigoUsuario())).findFirst().isPresent()) {
                salaAdicionarMembro(pSala, pUsuario.getCodigoUsuario());
            }

            GestaoTokenRestIntmatrixchat gestao = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatSalas.SALA_ENVIAR_MENSAGEM_TEXTO_SIMPLES.getGestaoToken(pUsuario);

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
    public String salaEnviarImagem(ComoChatSalaBean pSala, ComoUsuarioChat pUsuario, String pCodigoMensagem, String pNomeArquivo, InputStream pInput) throws ErroConexaoServicoChat {
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
    public String salaEnviarDocumento(ComoChatSalaBean pSala, ComoUsuarioChat pUsuario, String pCodigoMensagem, String pNomeArquivo, InputStream pInput) throws ErroConexaoServicoChat {
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
    public String salaEnviarVideo(ComoChatSalaBean pSala, ComoUsuarioChat pUsuario, String codigoMensagem, String pNomeArquivo, InputStream pInput) throws ErroConexaoServicoChat {
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
    public String salaEnviarAudio(ComoChatSalaBean pSala, ComoUsuarioChat pUsuario, String pcodigoMensagem, String pNomeArquivo, InputStream pInput) throws ErroConexaoServicoChat {
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

        ComoChatSalaBean saladirect = getSalaByCodigo(roomId);

        boolean temUsuario = saladirect.getUsuarios().stream().filter(usr -> usr.getCodigoUsuario().equals(pCodigoUsuario)).findFirst().isPresent();
        if (!temUsuario) {
            salaAdicionarMembro(saladirect, pCodigoUsuario);
        }

        return salaEnviarMesagem(saladirect, pMensagem);

    }

    @Override
    public boolean usuarioLogadoObterChaveAcesso(String pUserName, String pSenha) {
        GestaoTokenRestIntmatrixchat gestao = (GestaoTokenRestIntmatrixchat) FabApiRestIntMatrixChatUsuarios.USUARIOS_STATUS.getGestaoToken(SBCore.getUsuarioLogado());
        if (gestao.validarToken()) {
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
    public boolean validarTokenOuGerarNovo(ComoUsuario pUsuario, String pCodigo, String pSenha) throws ErroRegraDeNEgocioChat, ErroConexaoServicoChat {
        String senha = pSenha;
        String codUsuario = pCodigo;
        if (pCodigo == null && pUsuario != null && pUsuario instanceof ComoUsuarioChat) {
            codUsuario = ((ComoUsuarioChat) pUsuario).getCodigoUsuario();
        }

        if (pUsuario != null) {
            if (getUsuarioAdmin().getCodigoUsuario().equals(pCodigo)) {
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
                    //codUsuario = pUsuario.getCodigoUsuario();
                    // codUsuario = codUsuario.substring(1, codUsuario.indexOf(":"));
                    ComoUsuarioChat usuariochat = getUsuarioByCodigo(pCodigo);

                    if (isUmUsuarioContato(usuariochat)) {
                        senha = gerarSenhaPadrao(pUsuario, codUsuario);
                        usuarioAtualizarSenha(codUsuario, senha);
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
    public ComoUsuarioChat usuarioAtualizar(String pCodigo, String pNome, String pEmail, String pTelefone) throws ErroConexaoServicoChat {
        String telefone = UtilSBCoreStringTelefone.gerarNumeroTelefoneInternacional(pTelefone);
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
            ComoUsuarioChat usaurio = ERPChat.MATRIX_ORG.getDTO(resposta.getRespostaTexto(), ComoUsuarioChat.class);

            return registraUsuarioPorCodigo(usaurio);
        } catch (ErroJsonInterpredador ex) {
            System.out.println("FALHA Criando DTO");
            Logger.getLogger(ChatMatrixOrgimpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public ComoUsuarioChat usuarioAtualizar(ComoUsuarioChat pUsuario) throws ErroConexaoServicoChat {
        validarTokenSistema();
        return usuarioAtualizar(pUsuario.getCodigoUsuario(), pUsuario.getNome(), pUsuario.getEmailPrincipal(), pUsuario.getTelefone());
    }

    @Override
    public ComoUsuarioChat usuarioCriar(ComoUsuarioChat pUsuario, String pSenha) throws ErroConexaoServicoChat {
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
            ComoUsuarioChat usaurio = ERPChat.MATRIX_ORG.getDTO(resposta.getRespostaTexto(), ComoUsuarioChat.class);

            return registraUsuarioPorCodigo(usaurio);
        } catch (ErroJsonInterpredador ex) {
            System.out.println("FALHA Criando DTO");
            Logger.getLogger(ChatMatrixOrgimpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean salaNotificarLeitura(String pCodigoSala, ComoUsuarioChat pUsuario, String pCodigoReciboMatix) {

        try {
            validarTokenOuGerarNovo(pUsuario, pUsuario.getCodigoUsuario(), gerarSenhaPadrao(pUsuario, pUsuario.getCodigoUsuario()));
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
    public ComoUsuarioChat getUsuarioAdmin() {
        try {
            return getUsuarioByCodigo(getCodigoUsuarioAdmin());
        } catch (ErroConexaoServicoChat ex) {
            return null;
        }
    }

    @Override
    public String gerarCodigoUsuarioContato(String pWhatsappTelefone) throws ErroRegraDeNEgocioChat, ErroConexaoServicoChat {

        String telefone = UtilSBCoreStringTelefone.gerarNumeroTelefoneInternacional(pWhatsappTelefone);
        if (telefone == null) {
            throw new ErroRegraDeNEgocioChat("O Telefone não é válido");
        }

        ComoUsuarioChat usuarioPorTelefone = getUsuarioByTelefone(telefone);
        String codigo = gerarCodigoUsuario("contato", telefone, "ct");
        if (usuarioPorTelefone != null) {

            if (!usuarioPorTelefone.getCodigoUsuario().equals(codigo)) {
                /// outro usuário está usando este telefone, caso seja um usuário do contato ele precisa ser exluido, e um novo criado
                if (isUmUsuarioAtendimento(usuarioPorTelefone)) {
                    throw new ErroRegraDeNEgocioChat("O usuário de atendimento " + usuarioPorTelefone.getNome() + " " + usuarioPorTelefone.getEmail() + " está usando seu telefone, entre em contato, re remova esse numero de telefone do atendimento");
                } else {
                    FabApiRestIntMatrixChatUsuarios.USUARIO_REMOVER.getAcao(codigo);
                }
            }

        }

        // usr = getUsuarioByTelefone(pWhatsappTelefone);
        // if (usr == null) {
        //      return codigo;
        //  }
        return codigo;

    }

    @Override
    public ComoUsuarioChat gerarUsuarioContato(String pNome, String pTelefoneInternacional) throws ErroConexaoServicoChat, ErroRegraDeNEgocioChat {
        String telefoneInternacional = UtilSBCoreStringTelefone.gerarNumeroTelefoneInternacional(pTelefoneInternacional);

        //telefoneInternacional = telefoneInternacional.replace("+", "");
        if (telefoneInternacional == null) {
            throw new ErroRegraDeNEgocioChat("Telefone inválido" + pTelefoneInternacional);
        }
        String codigoUsuario = gerarCodigoUsuarioContato(telefoneInternacional);
        ComoUsuarioChat usuario = getUsuarioByCodigo(codigoUsuario);

        if (usuario == null) {

            UsuarioChatMatrixOrg usuariochat = new UsuarioChatMatrixOrg();
            usuariochat.setNome(pNome);
            usuariochat.setTelefone(telefoneInternacional);
            usuariochat.setCodigoUsuario(codigoUsuario);
            ComoUsuarioChat novoUsuario = usuarioCriar(usuariochat);
            if (novoUsuario == null) {
                throw new ErroConexaoServicoChat("Falha criando usuário tipo contato");
            }

            usuarioAtualizarSenha(codigoUsuario, gerarSenhaPadrao(usuario, codigoUsuario));

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
    public ComoUsuarioChat gerarUsuarioAtendimento(String pNome, String pEmail) throws ErroConexaoServicoChat, ErroRegraDeNEgocioChat {
        ComoUsuarioChat usuario = getUsuarioByEmail(pEmail);
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
            ComoUsuarioChat novoUsuario = usuarioCriar(usuariochat);
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
        ComoUsuarioChat usr = getUsuarioByEmail(pEmail);
        String codigoEstadoDaArte = gerarCodigoUsuario(pNome, pEmail, "at");
        if (usr != null) {
            if (isUmUsuarioAtendimento(usr)) {
                return usr.getCodigoUsuario();
            } else {
                return codigoEstadoDaArte;
            }

        } else {

            return codigoEstadoDaArte;
        }

    }

}
