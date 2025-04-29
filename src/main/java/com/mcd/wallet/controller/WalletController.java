package com.mcd.wallet.controller;

import com.mcd.wallet.controller.dto.ErrorResponse;
import com.mcd.wallet.controller.dto.SendBitcoinRequest;
import com.mcd.wallet.controller.dto.TransactionResponse;
import com.mcd.wallet.service.BlockchainService;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

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

    @PostMapping("/send")
    public ResponseEntity<?> sendTransaction(@Valid @RequestBody SendBitcoinRequest request) {
        try {
            // Input validation
            if (request.address() == null || request.address().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("ADDRESS_INVALID", "Bitcoin address cannot be empty"));
            }

            if (request.amount() == null || request.amount().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("AMOUNT_INVALID", "Amount cannot be empty"));
            }

            Wallet wallet = blockchainService.getWalletAppKit().wallet();

            // Parse and validate amount
            Coin value;
            try {
                value = Coin.parseCoin(request.amount());
                if (!value.isGreaterThan(Coin.ZERO)) {
                    return ResponseEntity.badRequest()
                            .body(new ErrorResponse("AMOUNT_INVALID", "Amount must be greater than zero"));
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("AMOUNT_INVALID", "Invalid amount format"));
            }

            // Parse and validate address
            Address toAddress;
            try {
                toAddress = Address.fromString(wallet.getNetworkParameters(), request.address());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("ADDRESS_INVALID", "Invalid bitcoin address"));
            }

            // Check balance
            if (wallet.getBalance().isLessThan(value)) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("INSUFFICIENT_FUNDS", "Insufficient wallet balance"));
            }

            // Send with timeout
            CompletableFuture<Wallet.SendResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return wallet.sendCoins(blockchainService.getWalletAppKit().peerGroup(), toAddress, value);
                } catch (InsufficientMoneyException e) {
                    throw new CompletionException(e);
                }
            });

            Wallet.SendResult result = future.get(30, TimeUnit.SECONDS);

            return ResponseEntity.ok(new TransactionResponse(
                    result.tx.getTxId().toString(),
                    value.toFriendlyString()
            ));

        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(new ErrorResponse("TIMEOUT", "Transaction timed out"));
        } catch (Exception e) {
            logger.error("Error processing transaction", e);
            if (e instanceof CompletionException ce && ce.getCause() instanceof InsufficientMoneyException) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("INSUFFICIENT_FUNDS", "Insufficient wallet balance"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"));
        }

    }
    }
