package br.org.coletivoJava.fw.api.erp.chat;

import javax.inject.Qualifier;
import br.org.coletivoJava.fw.api.erp.chat.ItfErpChatService;
import com.super_bits.modulosSB.SBCore.modulos.erp.InfoReferenciaApiErp;
import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Qualifier
@InfoReferenciaApiErp(tipoObjeto = ERPChat.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ChatMatrixOrg {
}
