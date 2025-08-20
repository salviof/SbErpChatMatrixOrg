/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.UtilMatrixERP;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.google.common.collect.Lists;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringSlugs;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringValidador;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringsCammelCase;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringsExtrator;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ItfBeanSimplesSomenteLeitura;
import java.util.ArrayList;
import java.util.List;
import org.coletivojava.fw.api.tratamentoErros.ErroPreparandoObjeto;

/**
 *
 * @author salvio
 */
public enum FabTipoSalaMatrix implements ItfFabricaSalaChat {

    WTZAP_ATENDIMENTO,
    WTZAP_VENDAS,
    WTZAP_ATENDIMENTO_GRUPO_CLIENTE,
    MATRIX_CHAT_VENDAS,
    MATRIX_CHAT_ATENDIMENTO,
    MATRIX_CHAT_ATENDIMENTO_CHAMADO,
    MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE,
    CHAT_DINAMICO_DE_ENTIDADE;

    public static FabTipoSalaMatrix getTipoByAlias(String pAlias) {
        for (FabTipoSalaMatrix tipo : FabTipoSalaMatrix.values()) {
            if (pAlias.contains(tipo.getSlug() + ":")) {
                return tipo;
            }
        }
        return null;
    }

    boolean isChatAtendimentoDeContato() {
        switch (this) {

            case WTZAP_ATENDIMENTO:

            case WTZAP_VENDAS:

            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:

            case MATRIX_CHAT_VENDAS:

            case MATRIX_CHAT_ATENDIMENTO:

            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:
                return true;
            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:

            case CHAT_DINAMICO_DE_ENTIDADE:
                return false;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public String getSlug() {
        switch (this) {

            case WTZAP_ATENDIMENTO:
                return "wc";
            case WTZAP_VENDAS:
                return "wv";
            case MATRIX_CHAT_ATENDIMENTO:
                return "ca";
            case MATRIX_CHAT_VENDAS:
                return "cv";
            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:
                return "cd";
            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:
                return "ct";

            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:
                return "gc";
            case CHAT_DINAMICO_DE_ENTIDADE:
                return "chat";

            default:
                throw new AssertionError();
        }

    }

    @Override
    public String getDescricao() {
        switch (this) {
            case WTZAP_ATENDIMENTO:
                return "Sala particular de atendimento ao cliente via whatsapp ";

            case WTZAP_VENDAS:
                return "Sala particular de soluções via whatsapp";

            case MATRIX_CHAT_ATENDIMENTO:
                return "Sala particular de atendimento ao cliente";

            case MATRIX_CHAT_VENDAS:

                return "Sala de soluções via internet";

            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:
                return "Sala de debate";

            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:

                return "Abertura de Chamado do cliente";
            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:
                return "Grupo de atendimento";

            case CHAT_DINAMICO_DE_ENTIDADE:
                return "Chat dinâmico";

            default:
                throw new AssertionError();
        }

    }

    @Override
    public String getAliasSala(ItfUsuarioChat pUSuarioLead) {
        String slug = getSlug();

        return UtilMatrixERP.gerarAliasSalaIDCanonicoUsuarioWhatsapp(pUSuarioLead, slug);

    }

    @Override
    public String getAliasSala(ItfBeanSimplesSomenteLeitura pBeanVinculado) {
        String slug = getSlug();
        return UtilMatrixERP.gerarAliasSalaIDCanonicoObjetoRelacionado(pBeanVinculado, slug);

    }

    @Override
    public SalaMatrxOrg getSalaMatrix(ItfBeanSimplesSomenteLeitura pBeanVinculado,
            ItfUsuarioChat pUsuarioDono,
            List<ItfUsuarioChat> pUsuariosAtendimento,
            List<ItfUsuarioChat> pUsuarioContatos
    ) throws ErroPreparandoObjeto {
        if (pUsuarioContatos == null) {
            pUsuarioContatos = new ArrayList<>();
        }

        SalaMatrxOrg novaSala = new SalaMatrxOrg();
        ItfUsuarioChat usuarioContatoPrincipal = null;
        try {
            if (isChatAtendimentoDeContato()) {

                if (pUsuarioContatos.isEmpty()) {
                    throw new UnsupportedOperationException("Pelomenos um usuário de contato é obrigatorio");
                }
                novaSala.setUsuariosExternos(pUsuarioContatos);
                if (pUsuarioContatos != null && !pUsuarioContatos.isEmpty()) {
                    usuarioContatoPrincipal = pUsuarioContatos.get(0);
                }
                if (pUsuarioContatos.get(0).getTelefone() == null || pUsuarioContatos.get(0).getTelefone().isEmpty()) {
                    throw new ErroPreparandoObjeto(pBeanVinculado, "O telefone do usuário externo não pode ser nulo");
                }
            }

            if (pUsuariosAtendimento.isEmpty()) {
                throw new UnsupportedOperationException("Pelomenos um usuário de contato é obrigatorio");
            }
        } catch (Throwable t) {
            throw new ErroPreparandoObjeto(pBeanVinculado, t);
        }

        if (pUsuariosAtendimento.get(0).getEmail() == null || pUsuariosAtendimento.get(0).getEmail().isEmpty()) {
            throw new ErroPreparandoObjeto(pBeanVinculado, "O email do usuário interno não pode ser nulo");
        }

        novaSala.setUsuariosDaEmpresa(pUsuariosAtendimento);

        novaSala.setUsuarios(new ArrayList<>());
        String slug = getSlug();

        if (novaSala.getUsuarios() == null) {
            novaSala.setUsuarios(new ArrayList());
        }
        ItfUsuarioChat usuarioAtendimentoPrincipal = null;
        if (pUsuariosAtendimento != null && !pUsuariosAtendimento.isEmpty()) {
            usuarioAtendimentoPrincipal = pUsuariosAtendimento.get(0);
        }

        StringBuilder nomeSala = new StringBuilder();
        switch (this) {
            case WTZAP_ATENDIMENTO:

                if (UtilSBCoreStringValidador.isNuloOuEmbranco(usuarioContatoPrincipal.getTelefone())) {
                    throw new ErroPreparandoObjeto(novaSala, "O telefone do destinatario é obrigatório");
                }

                String nomeClienteReduzido = UtilSBCoreStringsExtrator.getNomeReduzido(usuarioContatoPrincipal.getNome());
                nomeClienteReduzido = UtilSBCoreStringsCammelCase.getCamelByTextoPrimeiraLetraMaiuscula(nomeClienteReduzido);
                nomeSala.append(nomeClienteReduzido);
                nomeSala.append(usuarioContatoPrincipal.getTelefone());
                nomeSala.append("_");
                nomeSala.append(slug);

                novaSala.setApelido(UtilMatrixERP.gerarAliasSalaIDCanonicoUsuarioWhatsapp(usuarioContatoPrincipal, slug));
                novaSala.setNome(nomeSala.toString());

                novaSala.getUsuarios().add(usuarioContatoPrincipal);
                novaSala.getUsuarios().add(usuarioAtendimentoPrincipal);
                break;
            case WTZAP_VENDAS:

                if (UtilSBCoreStringValidador.isNuloOuEmbranco(usuarioContatoPrincipal.getTelefone())) {
                    throw new ErroPreparandoObjeto(novaSala, "O telefone do destinatario é obrigatório");
                }

                String nomeLeadReduzido = UtilSBCoreStringsExtrator.getNomeReduzido(usuarioContatoPrincipal.getNome());
                nomeLeadReduzido = UtilSBCoreStringsCammelCase.getCamelByTextoPrimeiraLetraMaiuscula(nomeLeadReduzido);
                nomeSala.append(nomeLeadReduzido);
                nomeSala.append(usuarioContatoPrincipal.getTelefone());
                nomeSala.append("_");
                nomeSala.append(slug);

                novaSala.setApelido(UtilMatrixERP.gerarAliasSalaIDCanonicoUsuarioWhatsapp(usuarioContatoPrincipal, slug));
                novaSala.setNome(nomeSala.toString());

                novaSala.getUsuarios().add(usuarioContatoPrincipal);
                novaSala.getUsuarios().add(usuarioAtendimentoPrincipal);
                break;

            case MATRIX_CHAT_VENDAS:
                if (pUsuariosAtendimento == null || pUsuariosAtendimento.isEmpty() || pUsuariosAtendimento.size() > 1) {
                    throw new UnsupportedOperationException("o usuário externo é obrigatório pois é utilizado na formação do nome");
                }
                ItfUsuarioChat usuarioNovoLead = pUsuariosAtendimento.get(0);
                novaSala.setApelido(slug + UtilSBCoreStringSlugs.gerarSlugSimples(usuarioNovoLead.getEmail()));
                novaSala.setNome(getSlug() + usuarioNovoLead.getEmail());
                System.out.println("Nome da SALA primeiro contato:");
                System.out.println(novaSala.getNome());
                if (!novaSala.getUsuarios().contains(usuarioNovoLead)) {
                    novaSala.getUsuarios().add((ItfUsuarioChat) pUsuariosAtendimento.get(0));
                    novaSala.getUsuarios().add((ItfUsuarioChat) pUsuarioDono);
                }
                return novaSala;

            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:
            case MATRIX_CHAT_ATENDIMENTO:
                String apelido = UtilMatrixERP.gerarAliasSalaIDCanonicoObjetoRelacionado(pBeanVinculado, slug);

                novaSala.setApelido(apelido);
                novaSala.setNome(pBeanVinculado.getNome());
                for (ItfUsuarioChat usr : pUsuarioContatos) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }
                for (ItfUsuarioChat usr : pUsuarioContatos) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }

                return novaSala;

            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:
                String apelidoChat = UtilMatrixERP.gerarAliasSalaIDCanonicoObjetoRelacionado(pBeanVinculado, slug);
                novaSala.setApelido(apelidoChat);
                novaSala.setNome(pBeanVinculado.getNome());
                for (ItfUsuarioChat usratendimento : pUsuariosAtendimento) {
                    if (!novaSala.getUsuarios().contains(usratendimento)) {
                        novaSala.getUsuarios().add(usratendimento);
                    }
                }
                for (ItfUsuarioChat usr : pUsuarioContatos) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }

                return novaSala;
            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:
                break;
            case CHAT_DINAMICO_DE_ENTIDADE:
                String apelidoChatDinamico = UtilMatrixERP.gerarAliasSalaIDCanonicoObjetoRelacionado(pBeanVinculado, slug);
                novaSala.setApelido(apelidoChatDinamico);
                novaSala.setNome(pBeanVinculado.getNome());
                for (ItfUsuarioChat usr : pUsuariosAtendimento) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }
                for (ItfUsuarioChat usr : pUsuarioContatos) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }
                return novaSala;

            default:
                throw new AssertionError();
        }

        return novaSala;

    }

    @Override
    public SalaMatrxOrg getSalaMatrix(ItfUsuarioChat pUsuarioDono, ItfUsuarioChat pUsuarioAtendimento, ItfUsuarioChat pUsuarioContato) throws ErroPreparandoObjeto {
        switch (this) {

            case WTZAP_ATENDIMENTO:
            case WTZAP_VENDAS:
            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:
                return getSalaMatrix(null, pUsuarioDono, Lists.newArrayList(pUsuarioAtendimento), Lists.newArrayList(pUsuarioContato));

            case CHAT_DINAMICO_DE_ENTIDADE:

            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:

            case MATRIX_CHAT_VENDAS:

            case MATRIX_CHAT_ATENDIMENTO:

            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:

            default:
                throw new ErroPreparandoObjeto(new SalaMatrxOrg(), "tipo de sala não é compatível com estes parametros, envie a entidade relacionada ao " + this.toString());
        }

    }

    @Override
    public SalaMatrxOrg getSalaMatrixPadrao(ItfUsuarioChat pUsuarioAtendimento, ItfUsuarioChat pUsuariosContato) throws ErroPreparandoObjeto {
        return getSalaMatrix(null, pUsuarioAtendimento, Lists.newArrayList(pUsuarioAtendimento), Lists.newArrayList(pUsuariosContato));
    }

    @Override
    public String
            getApelidoNomeUnicoSpace() {
        String dominioFederado = SBCore.getConfigModulo(FabConfigApiMatrixChat.class
        ).getPropriedade(FabConfigApiMatrixChat.DOMINIO_FEDERADO);
        return "#" + UtilSBCoreStringsCammelCase.getCamelByTextoPrimeiraLetraMaiusculaSemCaracterEspecial(toString()).toLowerCase()
                + ":" + dominioFederado;

    }

    @Override
    public String getNomeSpaceDysplay() {
        switch (this) {

            case WTZAP_ATENDIMENTO:
                return "Atd via whatsapp";

            case WTZAP_VENDAS:

                return "Vendas via whatsapp";

            case MATRIX_CHAT_ATENDIMENTO:
                return "Atd via chat ";

            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:

                return "Atd grupo Whatasapp";

            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:

                return "Atd chamado Cliente";

            case MATRIX_CHAT_VENDAS:
                return "Vendas via chat";

            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:
                return "Debates sobre contratos";
            case CHAT_DINAMICO_DE_ENTIDADE:
                return "Debates Internos";

            default:
                throw new AssertionError();
        }

    }

}
