package br.org.coletivoJava.fw.erp.implementacao.chat.atendente;

import java.util.List;

/**
 * Escuta das salas de um atendente, identificado pelo e-mail.
 *
 * Feita para ficar em um bean de sessão: guarda apenas o e-mail e o código do
 * usuário no Matrix. A sincronização com o Matrix (usuário admin) e o estado
 * das salas ficam em {@link CentralAtendentesMatrix}, compartilhados por todas
 * as sessões da JVM.
 *
 * A central guarda apenas uma referência fraca desta escuta, portanto quem
 * criar a escuta precisa manter uma referência a ela enquanto a sessão
 * existir. Se a sessão expirar sem {@link #encerrar()}, o GC recolhe a escuta
 * e a central remove o registro.
 *
 * Todas as chamadas de {@link #atualizacaoSala} são feitas em uma única thread
 * de despacho, nunca na thread da sessão.
 *
 * @author salvio
 */
public abstract class EscutaAtendenteMatrixAbst {

    private final String emailAtendente;
    private volatile String codigoAtendente;
    private volatile boolean ativo;

    public EscutaAtendenteMatrixAbst(String pEmailAtendente) {
        emailAtendente = pEmailAtendente;
    }

    /**
     * Localiza o usuário do Matrix pelo e-mail e registra a escuta. Na
     * primeira escuta da JVM, também inicia a sincronização com o Matrix.
     *
     * @return false quando o e-mail não corresponde a um usuário do Matrix
     */
    public final boolean iniciar() {
        if (ativo) {
            return true;
        }
        if (codigoAtendente == null) {
            codigoAtendente = CentralAtendentesMatrix.getCodigoUsuarioPorEmail(emailAtendente);
        }
        if (codigoAtendente == null) {
            return false;
        }
        ativo = true;
        CentralAtendentesMatrix.registrar(this);
        return true;
    }

    public final void encerrar() {
        if (!ativo) {
            return;
        }
        ativo = false;
        CentralAtendentesMatrix.remover(this);
    }

    public boolean isAtivo() {
        return ativo;
    }

    public String getEmailAtendente() {
        return emailAtendente;
    }

    /**
     * @return código do usuário no Matrix, ex: @joao.at:dominio.com.br, ou
     * null antes de {@link #iniciar()}
     */
    public String getCodigoAtendente() {
        return codigoAtendente;
    }

    /**
     * @return true se o atendente já foi notificado da sala e ainda não leu
     * nem respondeu
     */
    public boolean isNotificadoNaSala(String pCodigoSala) {
        return CentralAtendentesMatrix.isNotificadoNaSala(codigoAtendente, pCodigoSala);
    }

    /**
     * Notificações pendentes do atendente, com os dados da última mensagem de
     * cada sala. Útil para uma sessão nova do mesmo usuário, que não recebe
     * novamente as notificações já enviadas.
     */
    public List<AtualizacaoSalaAtendente> getNotificacoesPendentes() {
        return CentralAtendentesMatrix.getNotificacoesPendentes(codigoAtendente);
    }

    /**
     * Chamado sempre que uma sala do atendente muda de estado.
     *
     * NOVA_INTERACAO: criar a notificação. Enquanto ela estiver pendente,
     * novas mensagens da mesma sala não geram outra chamada.
     *
     * {@link AtualizacaoSalaAtendente#isRemoverNotificacao()}: retirar a
     * notificação da sala.
     */
    public abstract void atualizacaoSala(AtualizacaoSalaAtendente pAtualizacao);

}
