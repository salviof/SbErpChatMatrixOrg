/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.FabTipoSalaMatrix;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import java.util.logging.Level;
import java.util.logging.Logger;
import testesFW.ConfigCoreJunitPadraoDesenvolvedor;

import com.super_bits.modulosSB.SBCore.ConfigGeral.arquivosConfiguracao.ConfigModulo;
import org.coletivojava.fw.api.tratamentoErros.ErroPreparandoObjeto;
import org.junit.Test;

/**
 *
 * @author salvio
 */
public class TesteMonitoramentoSessaoChat {

    private static ChatMatrixOrgimpl erpChatService = (ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto();
    private ConfigModulo configuracao;
    private String nomeSala = "salaTestes";

    @Test
    public void testeMonitoramentoChat() throws ErroConexaoServicoChat {

        SBCore.configurar(new ConfigCoreJunitPadraoDesenvolvedor(), SBCore.ESTADO_APP.DESENVOLVIMENTO);
        configuracao = SBCore.getConfigModulo(FabConfigApiMatrixChat.class);
        try {
            ItfUsuarioChat usuarioChat1 = erpChatService.getUsuarioByEmail("salvio@casanovadigital.com.br");
            ItfUsuarioChat usuarioChat2 = erpChatService.getUsuarioByEmail("renata.mota@casanovadigital.com.br");
            ItfChatSalaBean salaRegistrada = FabTipoSalaMatrix.WTZAP_VENDAS.getSalaMatrix(usuarioChat1, usuarioChat2);
            salaRegistrada = erpChatService.getSalaByNome(salaRegistrada.getApelido());
            if (salaRegistrada == null) {
                salaRegistrada = FabTipoSalaMatrix.WTZAP_VENDAS.getSalaMatrix(usuarioChat1, usuarioChat2);
                salaRegistrada = erpChatService.getSalaCriandoSeNaoExistir(salaRegistrada);
            }

            System.out.println(salaRegistrada.getCodigoChat());
            System.out.println(salaRegistrada.getNome());
            System.out.println(salaRegistrada.getApelido());

            for (ItfUsuarioChat usr : salaRegistrada.getUsuarios()) {
                System.out.println(usr.getCodigoUsuario());
            }
            erpChatService.salaAdicionarMembro(salaRegistrada, usuarioChat1.getCodigoUsuario());
            erpChatService.registrarClasseDeEscutaSalas(SalaListenerTeste.class);
            erpChatService.salaAbrirSessao(salaRegistrada);
            boolean ativo = true;
            while (ativo) {
                Thread.sleep(1000);
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(TesteMonitoramentoSessaoChat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ErroPreparandoObjeto ex) {
            Logger.getLogger(TesteMonitoramentoSessaoChat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
