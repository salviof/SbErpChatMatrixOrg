/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.FabTipoSalaMatrix;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.super_bits.modulosSB.SBCore.ConfigGeral.arquivosConfiguracao.ConfigModulo;
import org.coletivojava.fw.api.tratamentoErros.ErroPreparandoObjeto;
import org.junit.Test;
import testesFW.ConfigCoreJunitPadraoDevLib;

/**
 *
 * @author salvio
 */
public class TesteSessaoChatMatrixSalaListenerTest {

    private static ChatMatrixOrgimpl erpChatService = (ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto();
    private ConfigModulo configuracao;
    private String nomeSala = "salaTestes";

    public ComoChatSalaBean getSalaRegistradaVendas() throws ErroConexaoServicoChat, ErroPreparandoObjeto {
        ComoUsuarioChat usuarioChat1 = erpChatService.getUsuarioByEmail("salvio@casanovadigital.com.br");
        ComoUsuarioChat usuarioChat2 = erpChatService.getUsuarioByEmail("renata.mota@casanovadigital.com.br");
        ComoChatSalaBean salaRegistrada = FabTipoSalaMatrix.WTZAP_VENDAS.getSalaMatrixPadrao(usuarioChat1, usuarioChat2);
        salaRegistrada = erpChatService.getSalaByNome(salaRegistrada.getApelido());
        if (salaRegistrada == null) {
            salaRegistrada = FabTipoSalaMatrix.WTZAP_VENDAS.getSalaMatrixPadrao(usuarioChat1, usuarioChat2);
            salaRegistrada = erpChatService.getSalaCriandoSeNaoExistir(salaRegistrada);
        }
        return salaRegistrada;
    }

    public ComoChatSalaBean getSalaRegistradadaByNome(String id) {
        ComoChatSalaBean salaRegistrada;
        try {
            salaRegistrada = erpChatService.getSalaByNome(nomeSala);
            return salaRegistrada;
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(TesteSessaoChatMatrixSalaListenerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Test
    public void testeMonitoramentoChat() throws ErroConexaoServicoChat {

        SBCore.configurar(new ConfigCoreJunitPadraoDevLib(), SBCore.ESTADO_APP.DESENVOLVIMENTO);

        configuracao = SBCore.getConfigModulo(FabConfigApiMatrixChat.class);
        try {
            System.out.println("Ativando monitor de sala do domínio:");
            System.out.println(configuracao.getPropriedade(FabConfigApiMatrixChat.URL_MATRIX_SERVER));
            nomeSala = "Casanova digital";
            // System.out.println(salaRegistrada.getCodigoChat());
            //  System.out.println(salaRegistrada.getNome());
            //  System.out.println(salaRegistrada.getApelido());

            ComoChatSalaBean salaRegistrada = getSalaRegistradadaByNome(nomeSala);
            for (ComoUsuarioChat usr : salaRegistrada.getUsuarios()) {
                System.out.println(usr.getCodigoUsuario());
            }
            //erpChatService.salaAdicionarMembro(salaRegistrada, usuarioChat1.getCodigoUsuario());
            erpChatService.registrarClasseDeEscutaSalas(SalaListenerTeste.class);
            erpChatService.salaAbrirSessao(salaRegistrada);
            boolean ativo = true;
            while (ativo) {
                Thread.sleep(1000);
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(TesteSessaoChatMatrixSalaListenerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
