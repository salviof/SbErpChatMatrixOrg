/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.ErroConexaoServicoChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import testesFW.ConfigCoreJunitPadraoDevAcaoPermissao;

/**
 *
 * @author salvio
 */
public class FabTipoSalaMatrixTest {

    public FabTipoSalaMatrixTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    private static ChatMatrixOrgimpl erpChatService = (ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto();

    @Test
    public void testGetApelidoNomeUnicoSpace() {
        SBCore.configurar(new ConfigCoreJunitPadraoDevAcaoPermissao(), SBCore.ESTADO_APP.DESENVOLVIMENTO);
        try {
            ItfUsuarioChat salvio = erpChatService.getUsuarioByEmail("salvio@casanovadigital.com.br");
            ItfUsuarioChat camila = erpChatService.getUsuarioByEmail("camila@casanovadigital.com.br");

            for (FabTipoSalaMatrix tipoSala : FabTipoSalaMatrix.values()) {

                ItfChatSalaBean sala = erpChatService.espacoCriar(tipoSala.getNomeSpaceDysplay(), tipoSala.getApelidoNomeUnicoSpace());
                erpChatService.salaAdicionarMembro(sala, salvio.getCodigoUsuario());
                erpChatService.salaAdicionarMembro(sala, camila.getCodigoUsuario());

                System.out.println(tipoSala.getApelidoNomeUnicoSpace());
            }
        } catch (ErroConexaoServicoChat ex) {
            Logger.getLogger(FabTipoSalaMatrixTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
