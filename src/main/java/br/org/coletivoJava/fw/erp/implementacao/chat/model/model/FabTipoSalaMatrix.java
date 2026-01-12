/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.UtilMatrixERP;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.google.common.collect.Lists;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilCRCStringSlugs;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilCRCStringValidador;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilCRCStringsCammelCase;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilCRCStringsExtrator;
import com.super_bits.modulosSB.SBCore.modulos.objetos.entidade.basico.ComoEntidadeSimplesSomenteLeitura;
import java.util.ArrayList;
import java.util.List;
import org.coletivojava.fw.api.tratamentoErros.ErroPreparandoObjeto;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

/**
 *
 * @author salvio
 */
public enum FabTipoSalaMatrix implements ComoFabricaSalaChat {

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
    public String getAliasSalaParaUsuario(ComoUsuarioChat pUSuarioLead) {
        String slug = getSlug();
        switch (this) {
            case WTZAP_ATENDIMENTO:
            case WTZAP_VENDAS:

                return UtilMatrixERP.gerarAliasSalaIDCanonicoUsuarioWhatsapp(pUSuarioLead, slug);
            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:
                throw new UnsupportedOperationException("Atendimento de grupo ainda não foi implmentado");
            case MATRIX_CHAT_ATENDIMENTO:
            case MATRIX_CHAT_VENDAS:
            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:
            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:
            case CHAT_DINAMICO_DE_ENTIDADE:

            default:
                throw new UnsupportedOperationException("Utilize uma entidade que não seja ComoUsuario chat para gerar o Apelido");

        }

    }

    @Override
    public String getAliasSalaParaEnttidade(ComoEntidadeSimplesSomenteLeitura pBeanVinculado) {
        String slug = getSlug();

        switch (this) {
            case WTZAP_ATENDIMENTO:
            case WTZAP_VENDAS:
                throw new UnsupportedOperationException("Utilize uma entidade que não seja ComoUsuario chat para gerar o Apelido");

            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:
                throw new UnsupportedOperationException("Atendimento de grupo ainda não foi implmentado");
            case MATRIX_CHAT_ATENDIMENTO:
            case MATRIX_CHAT_VENDAS:
            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:
            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:
            case CHAT_DINAMICO_DE_ENTIDADE:
                return UtilMatrixERP.gerarAliasSalaIDCanonicoObjetoRelacionado(pBeanVinculado, slug);
            default:
                throw new UnsupportedOperationException("Utilize uma entidade ComoUsuarioChat");

        }

    }

    @Override
    public SalaMatrxOrg getSalaMatrix(ComoEntidadeSimplesSomenteLeitura pBeanVinculado,
            ComoUsuarioChat pUsuarioDono,
            List<ComoUsuarioChat> pUsuariosAtendimento,
            List<ComoUsuarioChat> pUsuarioContatos
    ) throws ErroPreparandoObjeto {
        if (pUsuarioContatos == null) {
            pUsuarioContatos = new ArrayList<>();
        }

        SalaMatrxOrg novaSala = new SalaMatrxOrg();
        ComoUsuarioChat usuarioContatoPrincipal = null;
        try {
            if (isChatAtendimentoDeContato()) {

                if (pUsuarioContatos == null || pUsuarioContatos.isEmpty()) {
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
                throw new UnsupportedOperationException("Pelomenos um usuário de atendimento é obrigatorio");
            }
        } catch (Throwable t) {
            SBCore.RelatarErroAoUsuario(FabErro.SOLICITAR_REPARO, "Falha criando sala matrix ideal" + t.getMessage(), t);
            if (pBeanVinculado != null) {
                throw new ErroPreparandoObjeto(pBeanVinculado, t);
            } else {
                throw new ErroPreparandoObjeto(novaSala, t);
            }
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
        ComoUsuarioChat usuarioAtendimentoPrincipal = null;
        if (pUsuariosAtendimento != null && !pUsuariosAtendimento.isEmpty()) {
            usuarioAtendimentoPrincipal = pUsuariosAtendimento.get(0);
        }

        StringBuilder nomeSala = new StringBuilder();
        switch (this) {
            case WTZAP_ATENDIMENTO:

                if (UtilCRCStringValidador.isNuloOuEmbranco(usuarioContatoPrincipal.getTelefone())) {
                    throw new ErroPreparandoObjeto(novaSala, "O telefone do destinatario é obrigatório");
                }

                String nomeClienteReduzido = UtilCRCStringsExtrator.getNomeReduzido(usuarioContatoPrincipal.getNome());
                nomeClienteReduzido = UtilCRCStringsCammelCase.getCamelByTextoPrimeiraLetraMaiuscula(nomeClienteReduzido);
                nomeSala.append(nomeClienteReduzido);
                nomeSala.append(usuarioContatoPrincipal.getTelefone());
                nomeSala.append("_");
                nomeSala.append(slug);

                novaSala.setApelido(getAliasSalaParaUsuario(usuarioContatoPrincipal));
                novaSala.setNome(nomeSala.toString());

                novaSala.getUsuarios().add(usuarioContatoPrincipal);
                novaSala.getUsuarios().add(usuarioAtendimentoPrincipal);
                break;
            case WTZAP_VENDAS:

                if (UtilCRCStringValidador.isNuloOuEmbranco(usuarioContatoPrincipal.getTelefone())) {
                    throw new ErroPreparandoObjeto(novaSala, "O telefone do destinatario é obrigatório");
                }

                String nomeLeadReduzido = UtilCRCStringsExtrator.getNomeReduzido(usuarioContatoPrincipal.getNome());
                nomeLeadReduzido = UtilCRCStringsCammelCase.getCamelByTextoPrimeiraLetraMaiuscula(nomeLeadReduzido);
                nomeSala.append(nomeLeadReduzido);
                nomeSala.append(usuarioContatoPrincipal.getTelefone());
                nomeSala.append("_");
                nomeSala.append(slug);

                novaSala.setApelido(getAliasSalaParaUsuario(usuarioContatoPrincipal));
                novaSala.setNome(nomeSala.toString());

                novaSala.getUsuarios().add(usuarioContatoPrincipal);
                novaSala.getUsuarios().add(usuarioAtendimentoPrincipal);
                break;

            case MATRIX_CHAT_VENDAS:
                if (pUsuariosAtendimento == null || pUsuariosAtendimento.isEmpty() || pUsuariosAtendimento.size() > 1) {
                    throw new UnsupportedOperationException("o usuário externo é obrigatório pois é utilizado na formação do nome");
                }
                ComoUsuarioChat usuarioNovoLead = pUsuariosAtendimento.get(0);
                novaSala.setApelido(slug + UtilCRCStringSlugs.gerarSlugSimples(usuarioNovoLead.getEmail()));
                novaSala.setNome(getSlug() + usuarioNovoLead.getEmail());
                System.out.println("Nome da SALA primeiro contato:");
                System.out.println(novaSala.getNome());
                if (!novaSala.getUsuarios().contains(usuarioNovoLead)) {
                    novaSala.getUsuarios().add((ComoUsuarioChat) pUsuariosAtendimento.get(0));
                    novaSala.getUsuarios().add((ComoUsuarioChat) pUsuarioDono);
                }
                return novaSala;

            case MATRIX_CHAT_DEBATE_INTERNO_LEAD_CLIENTE:
            case MATRIX_CHAT_ATENDIMENTO:
                String apelido = getAliasSalaParaEnttidade(pBeanVinculado);

                novaSala.setApelido(apelido);
                novaSala.setNome(pBeanVinculado.getNome());
                for (ComoUsuarioChat usr : pUsuarioContatos) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }
                for (ComoUsuarioChat usr : pUsuarioContatos) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }

                return novaSala;

            case MATRIX_CHAT_ATENDIMENTO_CHAMADO:
                String apelidoChat = getAliasSalaParaEnttidade(pBeanVinculado);
                novaSala.setApelido(apelidoChat);
                novaSala.setNome(pBeanVinculado.getNome());
                for (ComoUsuarioChat usratendimento : pUsuariosAtendimento) {
                    if (!novaSala.getUsuarios().contains(usratendimento)) {
                        novaSala.getUsuarios().add(usratendimento);
                    }
                }
                for (ComoUsuarioChat usr : pUsuarioContatos) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }

                return novaSala;
            case WTZAP_ATENDIMENTO_GRUPO_CLIENTE:
                break;
            case CHAT_DINAMICO_DE_ENTIDADE:
                String apelidoChatDinamico = getAliasSalaParaEnttidade(pBeanVinculado);
                novaSala.setApelido(apelidoChatDinamico);
                novaSala.setNome(pBeanVinculado.getNome());
                for (ComoUsuarioChat usr : pUsuariosAtendimento) {
                    if (!novaSala.getUsuarios().contains(usr)) {
                        novaSala.getUsuarios().add(usr);
                    }
                }
                for (ComoUsuarioChat usr : pUsuarioContatos) {
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
    public SalaMatrxOrg getSalaMatrix(ComoUsuarioChat pUsuarioDono, ComoUsuarioChat pUsuarioAtendimento, ComoUsuarioChat pUsuarioContato) throws ErroPreparandoObjeto {
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
    public SalaMatrxOrg getSalaMatrixPadrao(ComoUsuarioChat pUsuarioAtendimento, ComoUsuarioChat pUsuariosContato) throws ErroPreparandoObjeto {
        return getSalaMatrix(null, pUsuarioAtendimento, Lists.newArrayList(pUsuarioAtendimento), Lists.newArrayList(pUsuariosContato));
    }

    @Override
    public String
            getApelidoNomeUnicoSpace() {
        String dominioFederado = SBCore.getConfigModulo(FabConfigApiMatrixChat.class
        ).getPropriedade(FabConfigApiMatrixChat.DOMINIO_FEDERADO);
        return "#" + UtilCRCStringsCammelCase.getCamelByTextoPrimeiraLetraMaiusculaSemCaracterEspecial(toString()).toLowerCase()
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
