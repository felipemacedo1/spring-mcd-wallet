package com.mcd.wallet.config;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
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
        switch (network.toLowerCase()) {
            case "mainnet":
                return MainNetParams.get();
            case "regtest":
                return RegTestParams.get(); // você pode precisar importar/implementar
            case "testnet":
            default:
                return TestNet3Params.get();
        }
    }
}

