package com.mcd.wallet.integration;

import com.mcd.wallet.service.BlockchainService;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Tag("integration")
// TODO: Fix or improve peer connection reliability in test environment
// @SpringBootTest
class BlockchainIntegrationTest {
//
//    @Autowired
//    private BlockchainService blockchainService;
//
//    @Test
//    void shouldConnectToPeersAndStartSync() {
//        WalletAppKit kit = blockchainService.getWalletAppKit();
//
//        await()
//                .atMost(Duration.ofSeconds(30))
//                .pollInterval(Duration.ofSeconds(1))
//                .until(() -> kit.peerGroup().numConnectedPeers() > 0);
//
//        PeerGroup peerGroup = kit.peerGroup();
//        Wallet wallet = kit.wallet();
//
//        int connectedPeers = peerGroup.numConnectedPeers();
//        System.out.println("Peers conectados: " + connectedPeers);
//        assertThat(connectedPeers).isGreaterThan(0);
//        assertThat(wallet).isNotNull();
//    }
//
//    @AfterEach
//    void cleanup() {
//        WalletAppKit kit = blockchainService.getWalletAppKit();
//        if (kit != null && kit.isRunning()) {
//            kit.peerGroup().stop();
//        }
//    }
}