/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package br.org.coletivoJava.fw.api.erp.chat.notificacoes;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.coletivojava.fw.api.tratamentoErros.FabErro;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import testesFW.ConfigCoreJunitPadraoDesenvolvedor;

/**
 *
 * @author salvio
 */
public class SincronizacaoNotificacoesTest {

    private static ChatMatrixOrgimpl erpChatService = (ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto();

    public SincronizacaoNotificacoesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of addRoomEventListener method, of class SincronizacaoNotificacoes.
     */
    @Test
    public void testAddRoomEventListener() {
        try {
            SBCore.configurar(new ConfigCoreJunitPadraoDesenvolvedor(), SBCore.ESTADO_APP.DESENVOLVIMENTO);
            erpChatService.registrarClasseEscutaNotificacoes(ListenerNotificacaoTestes.class);
        } catch (ErroConexaoServicoChat ex) {
            SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, "Falha registrando listener", ex);
        }
        while (true) {
            System.out.println("Crie uma notificação");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SincronizacaoNotificacoesTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
