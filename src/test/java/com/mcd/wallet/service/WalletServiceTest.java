package com.mcd.wallet.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @Test
    void shouldGenerateValidMnemonic() {
        String mnemonic = walletService.generateMnemonic();
        assertNotNull(mnemonic);
        assertEquals(12, mnemonic.split(" ").length); // Deve gerar 12 palavras
    }

    @Test
    void shouldDeriveSeedFromMnemonic() {
        String mnemonic = walletService.generateMnemonic();
        byte[] seed = walletService.deriveSeed(mnemonic, "");
        assertNotNull(seed);
        assertEquals(64, seed.length); // Seed gerada com PBKDF2-HMAC-SHA512
    }

    @Test
    void shouldThrowExceptionForInvalidMnemonic() {
        String invalidMnemonic = "this is an invalid mnemonic phrase";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            walletService.deriveSeed(invalidMnemonic, "");
        });
        assertTrue(exception.getMessage().contains("Invalid mnemonic"));
    }
}
