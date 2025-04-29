package com.mcd.wallet.controller;

import org.springframework.web.bind.annotation.*;
import com.mcd.wallet.service.BlockchainService;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final BlockchainService blockchainService;

    @Autowired
    public WalletController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping("/balance")
    public String getBalance() {
        Wallet wallet = blockchainService.getWalletAppKit().wallet();
        return wallet.getBalance().toFriendlyString();
    }

    @GetMapping("/transactions")
    public List<String> getTransactions() {
        Wallet wallet = blockchainService.getWalletAppKit().wallet();
        return wallet.getTransactions(false).stream()
                .map(tx -> "TXID: " + tx.getTxId() + " | Valor: " + tx.getValue(wallet).toFriendlyString())
                .collect(Collectors.toList());
    }
}
