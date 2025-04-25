package com.mcd.wallet.service;

public interface WalletService {
    String generateMnemonic();
    byte[] deriveSeed(String mnemonic, String passphrase);
}
