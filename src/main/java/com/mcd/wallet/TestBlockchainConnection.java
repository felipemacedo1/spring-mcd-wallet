package com.mcd.wallet;

import com.mcd.wallet.service.BlockchainService;

public class TestBlockchainConnection {
    public static void main(String[] args) throws InterruptedException {
        BlockchainService service = new BlockchainService();
        service.start();

        // Deixa rodar 5 minutos pra conectar e sincronizar blocos
        Thread.sleep(300_000);

        service.stop();
    }
}
