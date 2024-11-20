/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import de.jojii.matrixclientserver.Callbacks.EmptyCallback;

/**
 *
 * @author salvio
 */
public class RespostaLogout implements EmptyCallback {

    @Override
    public void onRun() {
        System.out.println("Logout");
    }

}
