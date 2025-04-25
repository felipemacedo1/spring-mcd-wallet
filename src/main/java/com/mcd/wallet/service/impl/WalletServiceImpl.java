package com.mcd.wallet.service.impl;

import com.mcd.wallet.service.WalletService;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    private static final long SEED_CREATION_TIME = 0L; // For deterministic behavior

    @Override
    public String generateMnemonic() {
        try {
            byte[] entropy = new byte[16]; // 128 bits → 12 words
            new SecureRandom().nextBytes(entropy);

            List<String> mnemonicWords = MnemonicCode.INSTANCE.toMnemonic(entropy);
            String mnemonic = String.join(" ", mnemonicWords);
            log.debug("Generated mnemonic: {}", mnemonic); // Optional: log only if safe

            return mnemonic;
        } catch (Exception e) {
            throw new WalletException("Failed to generate mnemonic", e);
        }
    }

    @Override
    public byte[] deriveSeed(String mnemonic, String passphrase) {
        try {
            SeedGenerationParameters params = new SeedGenerationParameters(mnemonic, passphrase);
            byte[] seed = createDeterministicSeed(params);
            log.debug("Derived seed with length: {}", seed.length); // Log seed length (not the seed itself)
            return seed;
        } catch (MnemonicException | UnreadableWalletException e) {
            throw new WalletException("Invalid mnemonic provided", e);
        }
    }

    private byte[] createDeterministicSeed(@NotNull SeedGenerationParameters params) throws MnemonicException, UnreadableWalletException {
        // Validate mnemonic format
        MnemonicCode.INSTANCE.check(List.of(params.mnemonic().split(" ")));

        DeterministicSeed seed = new DeterministicSeed(
                params.mnemonic(),
                null,
                params.passphrase(),
                SEED_CREATION_TIME
        );
        return seed.getSeedBytes();
    }

    // record instead of class for brevity
    public record SeedGenerationParameters(String mnemonic, String passphrase) {
        public SeedGenerationParameters {
            Objects.requireNonNull(mnemonic, "Mnemonic cannot be null");
            Objects.requireNonNull(passphrase, "Passphrase cannot be null");
        }
    }

    // Custom exception
    public static class WalletException extends RuntimeException {
        public WalletException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}