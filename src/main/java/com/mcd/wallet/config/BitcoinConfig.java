package com.mcd.wallet.config;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class BitcoinConfig {

    @Value("${bitcoin.network:testnet}")
    private String network;

    @Bean
    public NetworkParameters networkParameters() {
        return "mainnet".equalsIgnoreCase(network) ? MainNetParams.get() : TestNet3Params.get();
    }
}
