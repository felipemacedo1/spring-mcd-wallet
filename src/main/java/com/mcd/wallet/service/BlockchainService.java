package com.mcd.wallet.service;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;

@Service
public class BlockchainService {

    private final WalletAppKit kit;
    private final NetworkParameters params;

    public BlockchainService() {
        params = MainNetParams.get();
        kit = new WalletAppKit(params, new File("./wallet-data-mainnet"), "wallet") {
            @Override
            protected void onSetupCompleted() {
                System.out.println("Wallet setup complete (Mainnet).");
            }
        };
    }
    public WalletAppKit getWalletAppKit() {
        return kit;
    }


    @PostConstruct
    public void start() {
        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();

        System.out.println("Sincronizando na mainnet... cuidado! 😂");

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
        kit.stopAsync();
        kit.awaitTerminated();
        System.out.println("Serviço finalizado.");
    }
}
