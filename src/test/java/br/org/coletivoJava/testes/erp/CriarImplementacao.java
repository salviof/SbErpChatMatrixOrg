/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.coletivoJava.testes.erp;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.codigoPostal.br.ERPCodigoPostalBR;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import org.apache.logging.log4j.LogManager;
import org.coletivojava.fw.api.objetoNativo.log.LogPadraoSB;
import org.junit.Test;
import testes.geradorCodigo.erp.GeradorERPImplementacaoContexto;
import testes.geradorCodigo.erp.dto.GeradorDTOInterface;
import testes.geradorCodigo.erp.dto.GeradorDTOPojo;
import testes.geradorCodigo.erp.dto.GeradorDTOProcessador;
import testesFW.ConfigCoreJunitPadraoDesenvolvedor;

/**
 *
 * @author desenvolvedor
 */
public class CriarImplementacao {

    @Test
    public void criarImplementaca() {

        try {
            SBCore.configurar(new ConfigCoreJunitPadraoDesenvolvedor(), SBCore.ESTADO_APP.DESENVOLVIMENTO);
            new GeradorERPImplementacaoContexto(ERPChat.MATRIX_ORG).salvarEmDiretorioPadraCASO_NAO_EXISTA();

            for (Class entidade : ERPChat.MATRIX_ORG.getInterfacesDeEntidade()) {
                GeradorDTOInterface geradorInterface = new GeradorDTOInterface(ERPChat.MATRIX_ORG, entidade);
                geradorInterface.salvarEmDiretorioPadraoSubstituindoAnterior();
                GeradorDTOPojo geradorPojo = new GeradorDTOPojo(ERPChat.MATRIX_ORG, entidade);
                geradorPojo.salvarEmDiretorioPadraCASO_NAO_EXISTA();
                GeradorDTOProcessador geradorProcessador = new GeradorDTOProcessador(ERPChat.MATRIX_ORG, entidade);
                geradorProcessador.salvarEmDiretorioPadraCASO_NAO_EXISTA();

            }
        } catch (Throwable t) {
            LogManager.getLogger(LogPadraoSB.class).error("Erro Criando anotações", t);
        }
    }

}
