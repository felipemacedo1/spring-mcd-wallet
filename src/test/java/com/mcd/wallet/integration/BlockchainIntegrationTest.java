package com.mcd.wallet.integration;

import com.mcd.wallet.service.BlockchainService;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
class BlockchainIntegrationTest {

    @Autowired
    private BlockchainService blockchainService;

    @Test
    void shouldConnectToPeersAndStartSync() throws InterruptedException {
        // Aguarda alguns segundos para conectar com peers (configurável)
        Thread.sleep(10_000); // 10 segundos pra dar tempo de conectar

        WalletAppKit kit = blockchainService.getWalletAppKit();
        PeerGroup peerGroup = kit.peerGroup();
        Wallet wallet = kit.wallet();

        // Verifica se tem peers conectados
        int connectedPeers = peerGroup.numConnectedPeers();
        System.out.println("Peers conectados: " + connectedPeers);

        assertThat(connectedPeers).isGreaterThan(0);

        // Verifica se a wallet foi inicializada corretamente
        assertThat(wallet).isNotNull();
    }
}
