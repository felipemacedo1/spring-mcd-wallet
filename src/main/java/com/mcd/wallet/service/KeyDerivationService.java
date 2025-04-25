package com.mcd.wallet.service;

import org.bitcoinj.core.Address;

import java.util.List;

public interface KeyDerivationService {
    List<Address> deriveAddresses(byte[] seed, int count);
}
