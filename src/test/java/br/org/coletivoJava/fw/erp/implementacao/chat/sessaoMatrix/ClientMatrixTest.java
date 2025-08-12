/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import testesFW.ConfigCoreJunitPadraoDevLib;

/**
 *
 * @author salvio
 */
public class ClientMatrixTest {

    public ClientMatrixTest() {
    }

    /**
     * Test of getSincronizacao method, of class ClientMatrix.
     */
    @Test
    public void testGetSincronizacao() {
        SBCore.configurar(new ConfigCoreJunitPadraoDevLib(), SBCore.ESTADO_APP.DESENVOLVIMENTO);
        SessaoMatrix sessao = new SessaoMatrix((ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto());

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientMatrixTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
