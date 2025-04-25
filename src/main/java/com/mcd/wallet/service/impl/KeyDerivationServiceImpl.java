package com.mcd.wallet.service.impl;

import com.mcd.wallet.service.KeyDerivationService;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.script.Script;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyDerivationServiceImpl implements KeyDerivationService {

    private final NetworkParameters networkParameters;

    public KeyDerivationServiceImpl(NetworkParameters networkParameters) {
        this.networkParameters = networkParameters;
    }

    @Override
    public List<Address> deriveAddresses(byte[] seed, int count) {
        // 1. Criar chave-mestre (BIP-32)
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy hierarchy = new DeterministicHierarchy(masterKey);

        // 2. Definir caminho BIP-44: m/44'/coin_type'/0'/0
        int coinType = networkParameters.getId().equals("org.bitcoin.production") ? 0 : 1; // 0: mainnet, 1: testnet
        List<ChildNumber> accountPath = List.of(
                new ChildNumber(44, true),        // purpose'
                new ChildNumber(coinType, true),  // coin_type'
                ChildNumber.ZERO_HARDENED,        // account'
                ChildNumber.ZERO                  // external chain
        );

        // 3. Derivar endereços
        List<Address> addresses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DeterministicKey childKey = hierarchy.deriveChild(accountPath, false, true, new ChildNumber(i));
            Address address = Address.fromKey(networkParameters, childKey, Script.ScriptType.P2PKH);
            addresses.add(address);
        }

        return addresses;
    }
}
