/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringTelefone;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

/**
 *
 * @author salvio
 */
public class NormalizarMembrosThread extends Thread {

    private final List<ItfUsuarioChat> membrosIdeais;
    private final List<ItfUsuarioChat> membrosReais;
    private boolean exluirUsuariosNaoIdealizados = false;
    private final ChatMatrixOrgimpl chatService;
    private final String codigoSala;

    public NormalizarMembrosThread(ChatMatrixOrgimpl pChatService, String pCodigoSala, List<ItfUsuarioChat> pMembroIdeal, List<ItfUsuarioChat> pMembroReal) {
        this(pChatService, false, pCodigoSala, pMembroIdeal, pMembroReal);
    }

    protected NormalizarMembrosThread(ChatMatrixOrgimpl pChatService, boolean pExluirUsuariosNaoIdealizados, String pCodigoSala, List<ItfUsuarioChat> pUsuariosIdeais, List<ItfUsuarioChat> pUsuariosReal) {
        this.membrosIdeais = pUsuariosIdeais;
        this.membrosReais = pUsuariosReal;
        this.chatService = pChatService;
        this.codigoSala = pCodigoSala;
        this.exluirUsuariosNaoIdealizados = pExluirUsuariosNaoIdealizados;
    }

    @Override
    public void run() {

        if (membrosIdeais == null || membrosReais == null || codigoSala == null) {
            return;
        }
        List<String> emailsREaisEncontrados = new ArrayList<>();
        List<String> telefonesReaisEncontrados = new ArrayList<>();

        List<ItfUsuarioChat> usuariosIdealizadosNaoEncontrados = new ArrayList<>();
        List<ItfUsuarioChat> usuariosIdealizadosSobrando = new ArrayList<>();

        membrosReais.stream().filter(usr -> usr.getEmail() != null && !usr.getEmail().isEmpty()).map(usr -> usr.getEmail()).forEach(emailsREaisEncontrados::add);
        membrosReais.stream().filter(usr -> usr.getTelefone() != null && !usr.getTelefone().isEmpty()).map(usr -> UtilSBCoreStringTelefone.gerarCeluarInternacional(usr.getTelefone()))
                .forEach(telefonesReaisEncontrados::add);

        membrosIdeais.stream().filter(usr -> usr.getEmail() != null && !usr.getEmail().isEmpty()).filter(usr -> !emailsREaisEncontrados.contains(usr.getEmail())).forEach(usuariosIdealizadosNaoEncontrados::add);
        membrosIdeais.stream().filter(usr -> usr.getTelefone() != null && !usr.getTelefone().isEmpty()).filter(usr -> !telefonesReaisEncontrados.contains(usr.getTelefone())).forEach(usuariosIdealizadosNaoEncontrados::add);

        List<ItfUsuarioChat> listaSemDuplicatas = usuariosIdealizadosNaoEncontrados.stream()
                .distinct()
                .collect(Collectors.toList());

        for (ItfUsuarioChat usuario : listaSemDuplicatas) {
            try {
                chatService.salaAdicionarMembro(chatService.getSalaByCodigo(codigoSala), usuario.getCodigoUsuario());
            } catch (ErroConexaoServicoChat ex) {
                SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "falha acrescentando usuário a sala" + ex.getMessage(), ex);
            }
        }
        if (exluirUsuariosNaoIdealizados == true) {
            List<String> emailsIdeaisEncontrados = new ArrayList<>();
            List<String> telefonesIdeaisEncontrados = new ArrayList<>();

            membrosIdeais.stream().filter(usr -> usr.getEmail() != null && !usr.getEmail().isEmpty()).map(usr -> usr.getEmail()).forEach(emailsIdeaisEncontrados::add);
            membrosIdeais.stream().filter(usr -> usr.getTelefone() != null && !usr.getTelefone().isEmpty()).map(usr -> UtilSBCoreStringTelefone.gerarCeluarInternacional(usr.getTelefone()))
                    .forEach(telefonesIdeaisEncontrados::add);

            for (ItfUsuarioChat membro : membrosReais) {
                if (membro.getCodigoUsuario().equals(chatService.getUsuarioAdmin().getCodigoUsuario())) {
                    continue;
                }
                if (membro.getEmail() != null && membro.getTelefone() == null) {
                    try {
                        chatService.salaRemoverMembro(chatService.getSalaByCodigo(codigoSala), membro.getCodigoUsuario());
                    } catch (ErroConexaoServicoChat ex) {
                        SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha removendo membro da sala " + codigoSala, ex);
                    }
                }
                if (membro.getEmail() != null && !membro.getEmail().isEmpty()) {
                    if (emailsIdeaisEncontrados.contains(membro.getEmail())) {
                        continue;
                    }
                }
                if (membro.getEmail() != null && !membro.getEmail().isEmpty()) {
                    if (telefonesIdeaisEncontrados.contains(UtilSBCoreStringTelefone.gerarCeluarInternacional(membro.getTelefone()))) {
                        continue;
                    }
                }
                try {
                    chatService.salaRemoverMembro(chatService.getSalaByCodigo(codigoSala), membro.getCodigoUsuario());
                } catch (ErroConexaoServicoChat ex) {
                    SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha removendo membro da sala " + codigoSala, ex);
                }

            }
        }

//.
    }

}
