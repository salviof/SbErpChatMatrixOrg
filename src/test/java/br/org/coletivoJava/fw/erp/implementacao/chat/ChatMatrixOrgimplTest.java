/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroRegraDeNEgocioChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ComandoDeAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoComandoAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfNotificacaoUsuarioChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.FabTipoSalaMatrix;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaChatSessaoEscutaAtiva;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.UsuarioChatMatrixOrg;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.ConfigGeral.arquivosConfiguracao.ConfigModulo;
import com.super_bits.modulosSB.SBCore.modulos.objetos.InfoCampos.ItensGenericos.basico.UsuarioAnonimo;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ItfUsuario;
import jakarta.json.JsonArray;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.coletivojava.fw.api.tratamentoErros.ErroPreparandoObjeto;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import testesFW.ConfigCoreJunitPadraoDevLib;
import testesFW.TesteJunit;

/**
 *
 * @author salvio
 */
public class ChatMatrixOrgimplTest extends TesteJunit {

    private static ChatMatrixOrgimpl erpChatService = (ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto();

    private static final String usuarioEmail = "usuarioTeste@casanovadigital.com.br";
    private static final String usuarioSenha = "7879879654654!@#%@#$";
    private static final String usuarioTelefonr = "31989999999";
    private static final String salaTesteNome = "salaTeste";
    private static String salaTesteID = "";

    /**
     * Test of getSalaByCodigo method, of class ChatMatrixOrgimpl.
     */
    public void testeIntegracao() {
        try {

            if (testarObterUsuario()) {
                testarRemoverUsuario();
                testarCriarUsuario();
            } else {
                testarCriarUsuario();
                testarRemoverUsuario();
                testarCriarUsuario();
            }

            testarCriarSala();

            ItfChatSalaBean salaChat = erpChatService.getSalaByCodigo("!dehxkVYYxJDsNAORfM:casanovadigital.com.br");
            assertEquals("O id não é o id esperado", salaChat.getId(), Long.valueOf("!dehxkVYYxJDsNAORfM:casanovadigital.com.br".hashCode()));
            System.out.println(salaChat.getNome());
            System.out.println(salaChat.getUsuarios());

            for (ItfUsuarioChat usuario : salaChat.getUsuarios()) {
                System.out.println(usuario.getNome());
                System.out.println(usuario.getCodigoUsuario());
            }
        } catch (Throwable t) {
            lancarErroJUnit(t);
        }
    }

    public void testarRemoverSala() {
        //UtilMatrixERP.
        //erpChatService.salaExcluir(salaTesteNome);
    }

    @Override
    protected void configAmbienteDesevolvimento() {
        SBCore.configurar(new ConfigCoreJunitPadraoDevLib(), SBCore.ESTADO_APP.DESENVOLVIMENTO);
    }

    public boolean testarObterUsuario() throws ErroConexaoServicoChat {
        ItfUsuarioChat usuario = erpChatService.getUsuarioByEmail("renata.mota@casanovadigital.com.br");

        if (usuario == null) {
            return false;
        }

        System.out.println(usuario.getEmail());
        assertNotNull("O usuário não foi encontrado", usuario);

        System.out.println(usuario.getEmailPrincipal());
        System.out.println(usuario.getEmail());
        System.out.println(usuario.getCodigoUsuario());
        System.out.println(usuario.getNome());
        System.out.println(usuario.getId());
        System.out.println(usuario.getCodigoUsuario().hashCode());
        return true;
    }

    public boolean testarObterDadosSala() {
//        ItfUsuarioChat usuario = erpChatService.getUsuarioByEmail("renata.mota@casanovadigital.com.br");
        return true;
    }

    public void testarRemoverUsuario() {

    }

    public void criarUsuariosCasanova() {
        UsuarioChatMatrixOrg salvio = UtilMatrixERP.gerarUsuarioUnicoByEmail("Salvio Furbino", "salvio@casanovadigital.com.br", "31984178550");
        UsuarioChatMatrixOrg camila = UtilMatrixERP.gerarUsuarioUnicoByEmail("Camila Bissiguini", "camila@casanovadigital.com.br", "31995171605");
        UsuarioChatMatrixOrg beatriz = UtilMatrixERP.gerarUsuarioUnicoByEmail("Beatriz Mascena", "beatriz@casanovadigital.com.br", "81996654025");
        UsuarioChatMatrixOrg patricia = UtilMatrixERP.gerarUsuarioUnicoByEmail("Patrícia Paiva", "patricia@casanovadigital.com.br", "31991105847");
        UsuarioChatMatrixOrg renata = UtilMatrixERP.gerarUsuarioUnicoByEmail("Renata Mota", "renata.mota@casanovadigital.com.br", "31998535825");
        try {
            ItfUsuarioChat usuarioSAlvio = erpChatService.usuarioCriar(salvio, "semSenha");
            ItfUsuarioChat usuarioCamila = erpChatService.usuarioCriar(camila, "comunicacaoPIX");
            ItfUsuarioChat usuarioBeatriz = erpChatService.usuarioCriar(beatriz, "casaLover@Bia");
            ItfUsuarioChat usuarioPatricia = erpChatService.usuarioCriar(patricia, "pattyPaiva@casaLover");
            ItfUsuarioChat usuarioREnata = erpChatService.usuarioCriar(renata, "Renata@CasaLover");
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(ChatMatrixOrgimplTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testarCriarUsuario() throws Throwable {
        UsuarioChatMatrixOrg usuarioTeste = UtilMatrixERP.gerarUsuarioUnicoByEmail("Salvio Furbino", "salvio@casanovadigital.com.br", "31984178550");

        try {
            ItfUsuarioChat usuario = erpChatService.usuarioCriar(usuarioTeste, "semSenha");
            if (usuario != null) {
                System.out.println(usuario.getEmailPrincipal());
                System.out.println(usuario.getEmail());
                System.out.println(usuario.getCodigoUsuario());
                System.out.println(usuario.getNome());
                System.out.println(usuario.getId());
                System.out.println(usuario.getCodigoUsuario().hashCode());
            }
        } catch (ErroConexaoServicoChat ex) {
            throw new Throwable("Erro criando usuário" + ex.getMessage());
        }
        ItfUsuarioChat usuarioPesquisa = erpChatService.getUsuarioByEmail("salvio@casanovadigital.com.br");
        assertNotNull("O usuário não foi encontrado", usuarioPesquisa);

        System.out.println(usuarioPesquisa.getEmailPrincipal());
        System.out.println(usuarioPesquisa.getEmail());
        System.out.println(usuarioPesquisa.getCodigoUsuario());
        System.out.println(usuarioPesquisa.getNome());
        System.out.println(usuarioPesquisa.getId());
        System.out.println(usuarioPesquisa.getCodigoUsuario().hashCode());
    }

    public void testarCriarSala() {
        ItfUsuarioChat usuarioComprador = UtilMatrixERP.gerarUsuarioUnicoByEmail("Salvio Furbino Contratação", "salviof@hotmail.com", "31984178550");

        try {
            usuarioComprador = erpChatService.usuarioCriar(usuarioComprador);
            ItfUsuarioChat usuarioRepresentante = UtilMatrixERP.gerarUsuarioUnicoByEmail("Salvio Furbino Vendas", "salvio@casanovadigital.com.br", "31984178550");
            usuarioRepresentante = erpChatService.usuarioCriar(usuarioRepresentante);
            ItfChatSalaBean novaSala;
            try {
                novaSala = UtilMatrixERP.gerarSala(FabTipoSalaMatrix.WTZAP_VENDAS, usuarioRepresentante, usuarioRepresentante, usuarioComprador);

                novaSala = erpChatService.getSalaCriandoSeNaoExistir(novaSala);

                System.out.println(novaSala.getCodigoChat());
            } catch (ErroPreparandoObjeto ex) {
                Logger.getLogger(ChatMatrixOrgimplTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(ChatMatrixOrgimplTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Test of getConfiguracao method, of class ChatMatrixOrgimpl.
     */
    public void testGetConfiguracao() {
        System.out.println("getConfiguracao");
        ConfigModulo expResult = null;
        ConfigModulo result = ChatMatrixOrgimpl.getConfiguracao();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public void directEnviarNotificacao() {
        ChatMatrixOrgimpl instance = new ChatMatrixOrgimpl();
        try {
            ItfUsuarioChat usuario = instance.getUsuarioByEmail("salvio@casanovadigital.com.br");
            boolean sucesso = instance.enviarDirect(usuario.getCodigoUsuario(), "apenas teste") != null;
            assertTrue("falha enviando mensagem", sucesso);
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(ChatMatrixOrgimplTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getSAlaByCodigo() {
        ChatMatrixOrgimpl instance = new ChatMatrixOrgimpl();
        try {
            ItfChatSalaBean sala = instance.getSalaByCodigo("!xnlMIZLTTrCHdHUAxI:casanovadigital.com.br");
            System.out.println(sala.getCodigoChat());
            System.out.println(sala.getNome());
            System.out.println(sala.getApelido());
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(ChatMatrixOrgimplTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testGetNotificacoesUsuarioAdmin() throws Exception {
        System.out.println("getNotificacoesUsuarioAdmin");
        ChatMatrixOrgimpl instance = new ChatMatrixOrgimpl();

        List<ItfNotificacaoUsuarioChat> notificacoes;
        try {
            notificacoes = instance.getUltimasNotificacoesUsuarioAdmin();
            for (ItfNotificacaoUsuarioChat notificacao : notificacoes) {
                System.out.println(notificacao.getConteudo());
                System.out.println(notificacao.getRemetente());
                System.out.println(notificacao.getCodigoSalaOrigem());
            }

            while (!notificacoes.isEmpty()) {
                notificacoes = instance.getUltimasNotificacoesUsuarioAdmin();
                for (ItfNotificacaoUsuarioChat notificacao : notificacoes) {
                    System.out.println(notificacao.getConteudo());
                    System.out.println(notificacao.getRemetente());
                    System.out.println(notificacao.getCodigoSalaOrigem());
                }

            }
            assertTrue("Notificações repetidas foram obtidas", notificacoes.isEmpty());

            notificacoes = instance.getUltimasNotificacoesUsuarioAdmin();
            assertFalse("Notificações repetidas foram obtidas", notificacoes.isEmpty());
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(ChatMatrixOrgimplTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        // TODO review the generated test code and remove the default call to fail.
    }

    public void testeGeraacaoAliasCanonico() {
        ChatMatrixOrgimpl instance = new ChatMatrixOrgimpl();

        ItfUsuarioChat usuario;
        try {
            usuario = instance.getUsuarioByTelefone("5531986831481");
            System.out.println(usuario.getTelefone());
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(ChatMatrixOrgimplTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        String retorno1 = instance.gerarAliasIdentificadorCanonico("Salvio furbino");
        System.out.println(retorno1);

        String retorno2 = instance.gerarAliasIdentificadorCanonico("+(31)5531987187893_sv");
        System.out.println(retorno2);
        UsuarioAnonimo item = new UsuarioAnonimo();

        String retorno3 = instance.gerarAliasIdentificadorCanonico((item.getClassePontoIdentificador().replace(".", "_")).toLowerCase() + "_" + "at");
        System.out.println(retorno3);
    }

    @Test
    public void teste() {
        ChatMatrixOrgimpl chatMatrixService = new ChatMatrixOrgimpl();
        String codigoContato;
        try {
            codigoContato = chatMatrixService.gerarCodigoUsuarioContato("31984178110");
            System.out.println(codigoContato);
        } catch (ErroRegraDeNEgocioChat | ErroConexaoServicoChat ex) {
            Logger.getLogger(ChatMatrixOrgimplTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
