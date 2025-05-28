package com.GoPedidos.GoPedidos.Configurações;

import org.jasypt.util.text.BasicTextEncryptor;

public class Encryptor {
	public static void main(String[] args) {
		BasicTextEncryptor encryptor = new BasicTextEncryptor();
		encryptor.setPassword("MinhaChaveSecreta");

		String encrypted = encryptor.encrypt("Jc1231123");
		System.out.println("Encrypted: " + encrypted);
	}
}