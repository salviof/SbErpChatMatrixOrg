package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringTelefone;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

/**
 * Thread responsável por normalizar os membros de uma sala de chat, adicionando
 * usuários idealizados que não estão presentes e, opcionalmente, removendo
 * usuários reais que não estão na lista ideal. Usuários de atendimento têm
 * email obrigatório; contatos têm telefone obrigatório.
 */
public class NormalizarMembrosThread extends Thread {

    private final List<ItfUsuarioChat> membrosIdeais;
    private final List<ItfUsuarioChat> membrosReais;
    private final boolean excluirUsuariosNaoIdealizados;
    private final ChatMatrixOrgimpl chatService;
    private final String codigoSala;

    /**
     * Construtor principal com exclusão de usuários não idealizados opcional.
     *
     * @param pChatService Serviço de chat Matrix.
     * @param pExcluirUsuariosNaoIdealizados Define se usuários reais não
     * idealizados devem ser removidos.
     * @param pCodigoSala Código da sala a ser normalizada.
     * @param pUsuariosIdeais Lista de usuários idealizados.
     * @param pUsuariosReais Lista de usuários reais na sala.
     */
    public NormalizarMembrosThread(ChatMatrixOrgimpl pChatService, boolean pExcluirUsuariosNaoIdealizados,
            String pCodigoSala, List<ItfUsuarioChat> pUsuariosIdeais,
            List<ItfUsuarioChat> pUsuariosReais) {
        this.membrosIdeais = pUsuariosIdeais != null ? new ArrayList<>(pUsuariosIdeais) : new ArrayList<>();
        this.membrosReais = pUsuariosReais != null ? new ArrayList<>(pUsuariosReais) : new ArrayList<>();
        this.excluirUsuariosNaoIdealizados = pExcluirUsuariosNaoIdealizados;
        this.chatService = pChatService;
        this.codigoSala = pCodigoSala;
    }

    /**
     * Construtor simplificado sem exclusão de usuários não idealizados.
     *
     * @param pChatService Serviço de chat Matrix.
     * @param pCodigoSala Código da sala a ser normalizada.
     * @param pMembrosIdeais Lista de usuários idealizados.
     * @param pMembrosReais Lista de usuários reais na sala.
     */
    public NormalizarMembrosThread(ChatMatrixOrgimpl pChatService, String pCodigoSala,
            List<ItfUsuarioChat> pMembrosIdeais, List<ItfUsuarioChat> pMembrosReais) {
        this(pChatService, false, pCodigoSala, pMembrosIdeais, pMembrosReais);
    }

    public NormalizarMembrosThread(ChatMatrixOrgimpl pChatService, String pCodigoSala,
            List<ItfUsuarioChat> pMembrosIdeais, List<ItfUsuarioChat> pMembrosReais, boolean removerUsuarios) {
        this(pChatService, removerUsuarios, pCodigoSala, pMembrosIdeais, pMembrosReais);
    }

    @Override
    public void run() {
        // Verifica parâmetros essenciais
        if (membrosIdeais.isEmpty() || membrosReais.isEmpty() || codigoSala == null || chatService == null) {
            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Parâmetros inválidos: membros ou serviço de chat nulos", null);
            return;
        }

        // Valida usuários idealizados e reais (email para atendimento, telefone para contatos)
        validarUsuarios(membrosIdeais, "idealizados");
        validarUsuarios(membrosReais, "reais");

        // Coleta identificadores reais
        List<String> emailsReaisAtendimento = new ArrayList<>();
        List<String> telefonesReaisContatos = new ArrayList<>();
        membrosReais.stream()
                .filter(usr -> chatService.isUmUsuarioAtendimento(usr) && usr.getEmail() != null && !usr.getEmail().isEmpty())
                .map(ItfUsuarioChat::getEmail)
                .forEach(emailsReaisAtendimento::add);
        membrosReais.stream()
                .filter(usr -> chatService.isUmUsuarioContato(usr) && usr.getTelefone() != null && !usr.getTelefone().isEmpty())
                .map(usr -> UtilSBCoreStringTelefone.gerarCeluarInternacional(usr.getTelefone()))
                .forEach(telefonesReaisContatos::add);

        // Identifica usuários idealizados não encontrados
        List<ItfUsuarioChat> usuariosIdealizadosNaoEncontrados = membrosIdeais.stream()
                .filter(usr -> {
                    if (chatService.isUmUsuarioAtendimento(usr)) {
                        return usr.getEmail() != null && !usr.getEmail().isEmpty()
                                && !emailsReaisAtendimento.contains(usr.getEmail());
                    } else if (chatService.isUmUsuarioContato(usr)) {
                        return usr.getTelefone() != null && !usr.getTelefone().isEmpty()
                                && !telefonesReaisContatos.contains(UtilSBCoreStringTelefone.gerarCeluarInternacional(usr.getTelefone()));
                    }
                    return false; // Usuários sem tipo válido são ignorados
                })
                .distinct()
                .collect(Collectors.toList());

        // Adiciona usuários idealizados não encontrados à sala
        for (ItfUsuarioChat usuario : usuariosIdealizadosNaoEncontrados) {
            try {
                chatService.salaAdicionarMembro(chatService.getSalaByCodigo(codigoSala), usuario.getCodigoUsuario());
            } catch (ErroConexaoServicoChat ex) {
                SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha ao adicionar usuário à sala: " + ex.getMessage(), ex);
            }
        }

        // Remove usuários reais não idealizados, se configurado
        if (excluirUsuariosNaoIdealizados) {
            List<String> emailsIdeaisAtendimento = new ArrayList<>();
            List<String> telefonesIdeaisContatos = new ArrayList<>();
            membrosIdeais.stream()
                    .filter(usr -> chatService.isUmUsuarioAtendimento(usr) && usr.getEmail() != null && !usr.getEmail().isEmpty())
                    .map(ItfUsuarioChat::getEmail)
                    .forEach(emailsIdeaisAtendimento::add);
            membrosIdeais.stream()
                    .filter(usr -> chatService.isUmUsuarioContato(usr) && usr.getTelefone() != null && !usr.getTelefone().isEmpty())
                    .map(usr -> UtilSBCoreStringTelefone.gerarCeluarInternacional(usr.getTelefone()))
                    .forEach(telefonesIdeaisContatos::add);

            for (ItfUsuarioChat membro : membrosReais) {
                // Evita remover o administrador
                if (membro.getCodigoUsuario().equals(chatService.getUsuarioAdmin().getCodigoUsuario())) {
                    continue;
                }

                // Verifica se o membro real deve ser removido
                boolean deveRemover = false;
                if (chatService.isUmUsuarioAtendimento(membro)) {
                    if (membro.getEmail() == null || membro.getEmail().isEmpty()
                            || !emailsIdeaisAtendimento.contains(membro.getEmail())) {
                        deveRemover = true;
                    }
                } else if (chatService.isUmUsuarioContato(membro)) {
                    if (membro.getTelefone() == null || membro.getTelefone().isEmpty()
                            || !telefonesIdeaisContatos.contains(UtilSBCoreStringTelefone.gerarCeluarInternacional(membro.getTelefone()))) {
                        deveRemover = true;
                    }
                } else {
                    // Usuários sem tipo válido são removidos, se não idealizados
                    deveRemover = true;
                }

                if (deveRemover) {
                    try {
                        chatService.salaRemoverMembro(chatService.getSalaByCodigo(codigoSala), membro.getCodigoUsuario());
                    } catch (ErroConexaoServicoChat ex) {
                        SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha ao remover membro da sala " + codigoSala, ex);
                    }
                }
            }
        }
    }

    /**
     * Valida se os usuários atendem às obrigatoriedades de email (para
     * atendimento) ou telefone (para contatos). Reporta erros para usuários
     * inválidos.
     *
     * @param usuarios Lista de usuários a validar.
     * @param tipoLista Tipo da lista ("idealizados" ou "reais") para mensagens
     * de erro.
     */
    private void validarUsuarios(List<ItfUsuarioChat> usuarios, String tipoLista) {
        for (ItfUsuarioChat usr : usuarios) {
            if (usr.getCodigoUsuario().equals(chatService.getUsuarioAdmin().getCodigoUsuario())) {
                continue;
            }
            if (chatService.isUmUsuarioAtendimento(usr) && (usr.getEmail() == null || usr.getEmail().isEmpty())) {
                SBCore.RelatarErro(FabErro.SOLICITAR_REPARO,
                        "Usuário de atendimento sem email na lista " + tipoLista + ": " + usr.getCodigoUsuario(), null);
            } else if (chatService.isUmUsuarioContato(usr) && (usr.getTelefone() == null || usr.getTelefone().isEmpty())) {
                SBCore.RelatarErro(FabErro.SOLICITAR_REPARO,
                        "Contato sem telefone na lista " + tipoLista + ": " + usr.getCodigoUsuario(), null);
            }
        }
    }
}
