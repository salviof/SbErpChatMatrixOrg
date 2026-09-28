package br.org.coletivoJava.fw.erp.implementacao.chat.atendente;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import com.super_bits.modulosSB.SBCore.ConfigGeral.CarameloCode;
import com.super_bits.modulosSB.SBCore.modulos.Mensagens.FabMensagens;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Estado compartilhado das escutas de atendentes, um por JVM.
 *
 * Guarda apenas Strings: os atendentes de cada sala, nomes e aliases, e as
 * notificações pendentes dos atendentes com sessão aberta. Atendentes sem
 * sessão não acumulam pendências.
 *
 * Regras:
 * <ul>
 * <li>Mensagem de contato: NOVA_INTERACAO para cada atendente da sala com
 * sessão aberta que ainda não tenha notificação pendente na sala.</li>
 * <li>Mensagem ou reação de atendente: RESPONDIDA para todos os atendentes da
 * sala com notificação pendente.</li>
 * <li>Mensagem de contato com mais de 6 horas não notifica.</li>
 * <li>Recibo de leitura do atendente: LIDA apenas para ele.</li>
 * <li>Mensagens do admin são ignoradas.</li>
 * </ul>
 *
 * @author salvio
 */
public final class CentralAtendentesMatrix {

    private static final String TAG_LOG = "[MTX-ATENDENTE]";

    private static final Object LOCK = new Object();

    /**
     * Mensagens de contato mais antigas que isso (origin_server_ts) não geram
     * notificação, ex: histórico entregue com atraso pelo Matrix.
     */
    private static final long IDADE_MAXIMA_MENSAGEM_NOTIFICADA = 6 * 60 * 60 * 1000L;

    /**
     * sala -> atendentes presentes (contatos e admin não entram)
     */
    private static final Map<String, Set<String>> ATENDENTES_DA_SALA = new HashMap<>();
    private static final Map<String, String> NOME_SALA = new HashMap<>();
    private static final Map<String, String> ALIAS_SALA = new HashMap<>();
    /**
     * usuário -> displayname, de contatos e atendentes
     */
    private static final Map<String, String> NOME_USUARIO = new HashMap<>();
    /**
     * atendente -> escutas das sessões abertas
     */
    private static final Map<String, List<WeakReference<EscutaAtendenteMatrixAbst>>> ASSINANTES = new HashMap<>();
    /**
     * atendente -> (sala -> última mensagem notificada)
     */
    private static final Map<String, Map<String, AtualizacaoSalaAtendente>> PENDENTES = new HashMap<>();

    /**
     * usuário -> e-mail; "" quando o usuário não tem e-mail
     */
    private static final Map<String, String> EMAIL_USUARIO = new ConcurrentHashMap<>();

    private static ExecutorService despacho;
    private static SincronizacaoAtendentesMatrix sincronizacao;
    private static volatile String codigoAdmin;

    private CentralAtendentesMatrix() {
    }

    static void log(FabMensagens pTipo, String pMensagem) {
        try {
            CarameloCode.getServicoLogEventos().registrarLogDeEvento(pTipo, TAG_LOG + " " + pMensagem);
        } catch (Throwable t) {
            System.out.println(TAG_LOG + " " + pTipo + " " + pMensagem);
        }
    }

    static ChatMatrixOrgimpl getServico() {
        return (ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto();
    }

    // ------------------------------------------------------------------ sessões
    static String getCodigoUsuarioPorEmail(String pEmail) {
        if (pEmail == null || pEmail.trim().isEmpty()) {
            return null;
        }
        try {
            ComoUsuarioChat usuario = getServico().getUsuarioByEmail(pEmail.trim());
            return usuario == null ? null : usuario.getCodigoUsuario();
        } catch (Throwable t) {
            log(FabMensagens.ERRO, "Falha localizando o usuário do Matrix pelo e-mail " + pEmail + ": " + t.getMessage());
            return null;
        }
    }

    static String getEmailUsuario(String pCodigoUsuario) {
        if (pCodigoUsuario == null) {
            return null;
        }
        String email = EMAIL_USUARIO.get(pCodigoUsuario);
        if (email == null) {
            try {
                ComoUsuarioChat usuario = getServico().getUsuarioByCodigo(pCodigoUsuario);
                if (usuario == null) {
                    // sem chave válida ou usuário inexistente: tenta novamente na próxima
                    return null;
                }
                email = usuario.getEmailPrincipal();
                if (email == null || email.trim().isEmpty()) {
                    email = usuario.getEmail();
                }
                email = email == null ? "" : email.trim();
                EMAIL_USUARIO.put(pCodigoUsuario, email);
            } catch (Throwable t) {
                log(FabMensagens.AVISO, "Falha obtendo o e-mail de " + pCodigoUsuario + ": " + t.getMessage());
                return null;
            }
        }
        return email.isEmpty() ? null : email;
    }

    static void registrar(EscutaAtendenteMatrixAbst pEscuta) {
        synchronized (LOCK) {
            List<WeakReference<EscutaAtendenteMatrixAbst>> escutas = ASSINANTES.get(pEscuta.getCodigoAtendente());
            if (escutas == null) {
                escutas = new ArrayList<>(1);
                ASSINANTES.put(pEscuta.getCodigoAtendente(), escutas);
            }
            for (WeakReference<EscutaAtendenteMatrixAbst> ref : escutas) {
                if (ref.get() == pEscuta) {
                    return;
                }
            }
            escutas.add(new WeakReference<>(pEscuta));
            if (despacho == null) {
                despacho = Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "matrix-despacho-atendentes");
                    t.setDaemon(true);
                    return t;
                });
            }
            if (sincronizacao == null) {
                sincronizacao = new SincronizacaoAtendentesMatrix();
                sincronizacao.start();
            }
        }
    }

    static void remover(EscutaAtendenteMatrixAbst pEscuta) {
        synchronized (LOCK) {
            String atendente = pEscuta.getCodigoAtendente();
            List<WeakReference<EscutaAtendenteMatrixAbst>> escutas = ASSINANTES.get(atendente);
            if (escutas == null) {
                return;
            }
            escutas.removeIf(ref -> ref.get() == null || ref.get() == pEscuta);
            if (escutas.isEmpty()) {
                ASSINANTES.remove(atendente);
                PENDENTES.remove(atendente);
            }
        }
    }

    /**
     * Remove as escutas recolhidas pelo GC (sessões que expiraram sem
     * encerrar) e as pendências dos atendentes que ficaram sem sessão.
     */
    static void limparEscutasInativas() {
        synchronized (LOCK) {
            Iterator<Map.Entry<String, List<WeakReference<EscutaAtendenteMatrixAbst>>>> it = ASSINANTES.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, List<WeakReference<EscutaAtendenteMatrixAbst>>> entrada = it.next();
                entrada.getValue().removeIf(ref -> ref.get() == null);
                if (entrada.getValue().isEmpty()) {
                    PENDENTES.remove(entrada.getKey());
                    it.remove();
                }
            }
        }
    }

    static boolean isNotificadoNaSala(String pCodigoAtendente, String pCodigoSala) {
        if (pCodigoAtendente == null || pCodigoSala == null) {
            return false;
        }
        synchronized (LOCK) {
            Map<String, AtualizacaoSalaAtendente> pendentes = PENDENTES.get(pCodigoAtendente);
            return pendentes != null && pendentes.containsKey(pCodigoSala);
        }
    }

    static List<AtualizacaoSalaAtendente> getNotificacoesPendentes(String pCodigoAtendente) {
        synchronized (LOCK) {
            Map<String, AtualizacaoSalaAtendente> pendentes = PENDENTES.get(pCodigoAtendente);
            return pendentes == null ? new ArrayList<>() : new ArrayList<>(pendentes.values());
        }
    }

    /**
     * Encerra a sincronização, para o desligamento da aplicação. As escutas
     * continuam registradas, e a sincronização volta no próximo registro.
     */
    public static void encerrarMonitoramento() {
        synchronized (LOCK) {
            if (sincronizacao != null) {
                sincronizacao.encerrar();
                sincronizacao = null;
            }
            if (despacho != null) {
                despacho.shutdownNow();
                despacho = null;
            }
            ATENDENTES_DA_SALA.clear();
            NOME_SALA.clear();
            ALIAS_SALA.clear();
            PENDENTES.clear();
        }
    }

    // ------------------------------------------------------ eventos do /sync
    static void setCodigoAdmin(String pCodigoAdmin) {
        codigoAdmin = pCodigoAdmin;
    }

    private static boolean isAdmin(String pCodigoUsuario) {
        return pCodigoUsuario != null && pCodigoUsuario.equals(codigoAdmin);
    }

    private static boolean isContato(String pCodigoUsuario) {
        return pCodigoUsuario != null && pCodigoUsuario.contains(".ct:");
    }

    static int getTotalSalasMonitoradas() {
        synchronized (LOCK) {
            return ATENDENTES_DA_SALA.size();
        }
    }

    static void atualizarMembro(String pSala, String pUsuario, String pMembership, String pNome) {
        if (pSala == null || pUsuario == null) {
            return;
        }
        synchronized (LOCK) {
            if (pNome != null && !pNome.isEmpty()) {
                NOME_USUARIO.put(pUsuario, pNome);
            }
            if (isAdmin(pUsuario) || isContato(pUsuario)) {
                return;
            }
            if ("join".equals(pMembership)) {
                Set<String> atendentes = ATENDENTES_DA_SALA.get(pSala);
                if (atendentes == null) {
                    atendentes = new HashSet<>(4);
                    ATENDENTES_DA_SALA.put(pSala, atendentes);
                }
                atendentes.add(pUsuario);
            } else if ("leave".equals(pMembership) || "ban".equals(pMembership)) {
                Set<String> atendentes = ATENDENTES_DA_SALA.get(pSala);
                if (atendentes != null) {
                    atendentes.remove(pUsuario);
                    if (atendentes.isEmpty()) {
                        ATENDENTES_DA_SALA.remove(pSala);
                    }
                }
                retirarPendencia(pUsuario, pSala, FabTipoAtualizacaoSalaAtendente.SAIU_DA_SALA);
            }
        }
    }

    static void atualizarNomeSala(String pSala, String pNome) {
        synchronized (LOCK) {
            if (pNome == null || pNome.isEmpty()) {
                NOME_SALA.remove(pSala);
            } else {
                NOME_SALA.put(pSala, pNome);
            }
        }
    }

    static void atualizarAliasSala(String pSala, String pAlias) {
        synchronized (LOCK) {
            if (pAlias == null || pAlias.isEmpty()) {
                ALIAS_SALA.remove(pSala);
            } else {
                ALIAS_SALA.put(pSala, pAlias);
            }
        }
    }

    /**
     * O admin saiu da sala: ela deixa de ser monitorada.
     */
    static void removerSala(String pSala) {
        synchronized (LOCK) {
            Set<String> atendentes = ATENDENTES_DA_SALA.remove(pSala);
            if (atendentes != null) {
                for (String atendente : atendentes) {
                    retirarPendencia(atendente, pSala, FabTipoAtualizacaoSalaAtendente.SAIU_DA_SALA);
                }
            }
            NOME_SALA.remove(pSala);
            ALIAS_SALA.remove(pSala);
        }
    }

    static void mensagemRecebida(String pSala, String pRemetente, String pEventId, String pTexto, long pDataHora) {
        if (pSala == null || pRemetente == null || isAdmin(pRemetente)) {
            return;
        }
        synchronized (LOCK) {
            if (!isContato(pRemetente)) {
                retirarPendenciasDaSala(pSala);
                return;
            }
            if (pDataHora < System.currentTimeMillis() - IDADE_MAXIMA_MENSAGEM_NOTIFICADA) {
                return;
            }
            Set<String> atendentes = ATENDENTES_DA_SALA.get(pSala);
            if (atendentes == null) {
                return;
            }
            for (String atendente : atendentes) {
                if (!ASSINANTES.containsKey(atendente)) {
                    continue;
                }
                Map<String, AtualizacaoSalaAtendente> pendentes = PENDENTES.get(atendente);
                if (pendentes == null) {
                    pendentes = new HashMap<>(4);
                    PENDENTES.put(atendente, pendentes);
                }
                AtualizacaoSalaAtendente atualizacao = new AtualizacaoSalaAtendente(
                        FabTipoAtualizacaoSalaAtendente.NOVA_INTERACAO, atendente,
                        pSala, ALIAS_SALA.get(pSala), NOME_SALA.get(pSala),
                        pRemetente, getNomeUsuario(pRemetente), pTexto, pEventId, pDataHora);
                // a pendência sempre guarda a última mensagem, mas só a primeira é despachada
                if (pendentes.put(pSala, atualizacao) == null) {
                    despachar(atendente, atualizacao);
                }
            }
        }
    }

    static void reacaoRecebida(String pSala, String pRemetente) {
        if (pSala == null || pRemetente == null || isAdmin(pRemetente) || isContato(pRemetente)) {
            return;
        }
        synchronized (LOCK) {
            retirarPendenciasDaSala(pSala);
        }
    }

    static void leituraRecebida(String pSala, String pUsuario, long pDataHoraLeitura) {
        if (pSala == null || pUsuario == null) {
            return;
        }
        synchronized (LOCK) {
            Map<String, AtualizacaoSalaAtendente> pendentes = PENDENTES.get(pUsuario);
            if (pendentes == null) {
                return;
            }
            AtualizacaoSalaAtendente pendente = pendentes.get(pSala);
            // recibo atrasado, de antes da última mensagem, não retira a notificação
            if (pendente == null || (pDataHoraLeitura > 0 && pDataHoraLeitura < pendente.getDataHora())) {
                return;
            }
            retirarPendencia(pUsuario, pSala, FabTipoAtualizacaoSalaAtendente.LIDA);
        }
    }

    // ------------------------------------------------ chamados dentro do LOCK
    private static String getNomeUsuario(String pCodigoUsuario) {
        String nome = NOME_USUARIO.get(pCodigoUsuario);
        if (nome != null) {
            return nome;
        }
        // @joao.ct:dominio -> joao.ct
        String localpart = pCodigoUsuario.startsWith("@") ? pCodigoUsuario.substring(1) : pCodigoUsuario;
        int idxDominio = localpart.indexOf(':');
        return idxDominio > 0 ? localpart.substring(0, idxDominio) : localpart;
    }

    private static void retirarPendenciasDaSala(String pSala) {
        Set<String> atendentes = ATENDENTES_DA_SALA.get(pSala);
        if (atendentes == null) {
            return;
        }
        for (String atendente : atendentes) {
            retirarPendencia(atendente, pSala, FabTipoAtualizacaoSalaAtendente.RESPONDIDA);
        }
    }

    private static void retirarPendencia(String pAtendente, String pSala, FabTipoAtualizacaoSalaAtendente pTipo) {
        Map<String, AtualizacaoSalaAtendente> pendentes = PENDENTES.get(pAtendente);
        if (pendentes == null) {
            return;
        }
        AtualizacaoSalaAtendente pendente = pendentes.remove(pSala);
        if (pendentes.isEmpty()) {
            PENDENTES.remove(pAtendente);
        }
        if (pendente != null) {
            despachar(pAtendente, pendente.comTipo(pTipo));
        }
    }

    private static void despachar(String pAtendente, AtualizacaoSalaAtendente pAtualizacao) {
        List<WeakReference<EscutaAtendenteMatrixAbst>> refs = ASSINANTES.get(pAtendente);
        if (refs == null || despacho == null) {
            return;
        }
        List<EscutaAtendenteMatrixAbst> escutas = new ArrayList<>(refs.size());
        for (WeakReference<EscutaAtendenteMatrixAbst> ref : refs) {
            EscutaAtendenteMatrixAbst escuta = ref.get();
            if (escuta != null && escuta.isAtivo()) {
                escutas.add(escuta);
            }
        }
        if (escutas.isEmpty()) {
            return;
        }
        despacho.execute(() -> {
            for (EscutaAtendenteMatrixAbst escuta : escutas) {
                try {
                    escuta.atualizacaoSala(pAtualizacao);
                } catch (Throwable t) {
                    log(FabMensagens.ERRO, "Falha na escuta do atendente ao processar " + pAtualizacao
                            + ": " + t.getClass().getName() + ": " + t.getMessage());
                }
            }
        });
    }

}
