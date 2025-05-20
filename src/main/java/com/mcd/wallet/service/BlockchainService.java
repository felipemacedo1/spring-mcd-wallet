package com.mcd.wallet.service;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.core.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class BlockchainService {

    private final NetworkParameters params;
    private WalletAppKit kit;
    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);

    @Value("${bitcoin.peer-host:127.0.0.1}")
    private String peerHost;

    @Value("${bitcoin.peer-port:18444}")
    private int peerPort;

    public BlockchainService(NetworkParameters params) {
        this.params = params;
    }

    public WalletAppKit getWalletAppKit() {
        return kit;
    }

    @PostConstruct
    public void start() {
        File walletDir = new File("./wallet-data-" + params.getId());

        kit = new WalletAppKit(params, walletDir, "wallet") {
            @Override
            protected void onSetupCompleted() {
                log.info("Wallet setup complete ({})", params.getId());
            }
        };

        // Conexão manual com o bitcoind local (necessário para RegTest)
        if (params.getId().equals(NetworkParameters.ID_REGTEST)) {
            try {
                log.info("Conectando manualmente ao bitcoind local (RegTest): {}:{}", peerHost, peerPort);
                InetAddress address = InetAddress.getByName(peerHost);
                kit.setPeerNodes(new PeerAddress(params, address, peerPort));
            } catch (Exception e) {
                log.error("Erro ao configurar peer manual para RegTest", e);
            }
        }

        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();

        String networkName = params.getId().contains("test") ? "testnet"
                : params.getId().contains("regtest") ? "regtest"
                : "mainnet";

        log.info("Syncing with Bitcoin {}...", networkName);
        log.info("Wallet data directory: {}", walletDir.getAbsolutePath());

        kit.peerGroup().startBlockChainDownload(new DownloadProgressTracker() {
            @Override
            public void doneDownload() {
                log.info("Blockchain sync complete!");
            }
        });

        kit.peerGroup().addConnectedEventListener((peer, peerCount) -> {
            log.info("Connected to peer: {} | Total peers: {}", peer.getAddress(), peerCount);
        });

        kit.peerGroup().addBlocksDownloadedEventListener((peer, block, filteredBlock, blocksLeft) -> {
            log.info("New block received! Hash: {} | Blocks left: {}", block.getHashAsString(), blocksLeft);
        });

        kit.wallet().addCoinsReceivedEventListener((wallet, tx, prevBalance, newBalance) -> {
            log.info("Coins received! TX: {} | New balance: {}", tx.getHashAsString(), newBalance.toFriendlyString());
        });

        kit.wallet().addCoinsSentEventListener((wallet, tx, prevBalance, newBalance) -> {
            log.info("Coins sent! TX: {} | New balance: {}", tx.getHashAsString(), newBalance.toFriendlyString());
        });
    }

    @PreDestroy
    public void stop() {
        if (kit != null && kit.state() != com.google.common.util.concurrent.Service.State.TERMINATED
                && kit.state() != com.google.common.util.concurrent.Service.State.FAILED) {
            try {
                kit.stopAsync();
                kit.awaitTerminated(30, TimeUnit.SECONDS);
                boolean terminated = kit.state() == com.google.common.util.concurrent.Service.State.TERMINATED;
                if (terminated) {
                    log.info("Serviço finalizado.");
                } else {
                    log.warn("Timeout ao aguardar finalização do serviço.");
                }
            } catch (IllegalStateException | TimeoutException e) {
                log.error("Erro ao finalizar o serviço", e);
            }
        } else {
            log.info("Serviço já estava parado ou em estado inválido.");
        }
    }
}
