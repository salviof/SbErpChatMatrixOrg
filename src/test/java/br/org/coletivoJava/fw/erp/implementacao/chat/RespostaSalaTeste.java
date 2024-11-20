/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import de.jojii.matrixclientserver.Callbacks.DataCallback;
import java.io.IOException;

/**
 *
 * @author salvio
 */
public class RespostaSalaTeste implements DataCallback {

    @Override
    public void onData(Object o) throws IOException {
        System.out.println("OPA!!!");
        System.out.println(o);
    }

}
