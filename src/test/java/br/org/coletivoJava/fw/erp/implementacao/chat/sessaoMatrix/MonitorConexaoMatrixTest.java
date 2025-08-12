/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import br.org.coletivoJava.fw.erp.implementacao.chat.ListenerDaSalaExemplo;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaChatSessaoEscutaAtiva;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import de.jojii.matrixclientserver.File.Files;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import testesFW.ConfigCoreJunitPadraoDevLib;

/**
 *
 * @author salvio
 */
public class MonitorConexaoMatrixTest {

    public MonitorConexaoMatrixTest() {
    }

    @Test
    public void testAdicionarLister() {
        SBCore.configurar(new ConfigCoreJunitPadraoDevLib(), SBCore.ESTADO_APP.DESENVOLVIMENTO);
        SessaoMatrix monitor = new SessaoMatrix((ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto());
        monitor.start();

        File file = new File(Files.sync_next_batch);
        System.out.println("Arquivo será salvo em: " + file.getAbsolutePath());
        ChatMatrixOrgimpl chat = (ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto();
        ItfChatSalaBean sala;
        try {
            sala = chat.getSalaByNome("Casanova digital");
            SalaChatSessaoEscutaAtiva salaEscuta = new SalaChatSessaoEscutaAtiva(sala, new ListenerDaSalaExemplo(sala));
            chat.salaRemoverMembro(sala, "@camila_bissiguini020:casanovadigital.com.br");
            chat.salaAdicionarMembro(sala, chat.getUsuarioAdmin().getCodigoUsuario());
            monitor.adicionarLister(salaEscuta);
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(MonitorConexaoMatrixTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MonitorConexaoMatrixTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
