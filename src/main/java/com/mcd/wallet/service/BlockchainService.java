package com.mcd.wallet.service;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.kits.WalletAppKit;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class BlockchainService {

    private final NetworkParameters params;
    private WalletAppKit kit;
    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);

    public BlockchainService(NetworkParameters params) {
        this.params = params;
    }

    public WalletAppKit getWalletAppKit() {
        return kit;
    }

    @PostConstruct
    public void start() {
        kit = new WalletAppKit(params, new File("./wallet-data"), "wallet") {
            @Override
            protected void onSetupCompleted() {
                System.out.println("Wallet setup complete (" + params.getId() + ").");
            }
        };

        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();

        System.out.println("Sincronizando na " + (params.getId().contains("test") ? "testnet" : "mainnet") + "... cuidado!");

        // Listener de progresso de sincronização
        kit.peerGroup().startBlockChainDownload(new DownloadProgressTracker() {
            @Override
            public void doneDownload() {
                System.out.println("Sincronização completa!");
            }
        });

        // Log de peers conectados
        kit.peerGroup().addConnectedEventListener((peer, peerCount) -> {
            System.out.println("Connected to peer: " + peer.getAddress() + " | Total peers: " + peerCount);
        });

        // Listener para novos blocos recebidos
        kit.peerGroup().addBlocksDownloadedEventListener((peer, block, filteredBlock, blocksLeft) -> {
            System.out.println("Novo bloco recebido! Hash: " + block.getHashAsString() + " | Blocos restantes: " + blocksLeft);
        });

        // Listener para transações recebidas
        kit.wallet().addCoinsReceivedEventListener((wallet, tx, prevBalance, newBalance) -> {
            System.out.println("Nova transação recebida! TX: " + tx.getHashAsString() + " | Novo saldo: " + newBalance.toFriendlyString());
        });

        // Listener para transações enviadas
        kit.wallet().addCoinsSentEventListener((wallet, tx, prevBalance, newBalance) -> {
            System.out.println("Transação enviada! TX: " + tx.getHashAsString() + " | Novo saldo: " + newBalance.toFriendlyString());
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