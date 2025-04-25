package com.mcd.wallet.service;

import org.bitcoinj.core.Address;
import org.bitcoinj.crypto.MnemonicCode;

import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KeyDerivationServiceTest {

    @Autowired
    private KeyDerivationService keyDerivationService;

    @Test
    void shouldDeriveAddressesFromSeed() throws MnemonicException.MnemonicLengthException, UnreadableWalletException {
        // Gerar mnemonic de 12 palavras (128 bits)
        byte[] entropy = new byte[16];
        new SecureRandom().nextBytes(entropy);
        List<String> mnemonicWords = MnemonicCode.INSTANCE.toMnemonic(entropy);
        String mnemonic = String.join(" ", mnemonicWords);

        // Derivar seed
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", 0L);
        byte[] seedBytes = seed.getSeedBytes();

        // Derivar 5 endereços
        List<Address> addresses = keyDerivationService.deriveAddresses(seedBytes, 5);

        // Validar
        assertNotNull(addresses);
        assertEquals(5, addresses.size());
        addresses.forEach(address -> {
            assertNotNull(address);
            System.out.println("Derived Address: " + address);
        });
    }
}
