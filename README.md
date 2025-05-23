
# spring-mcd-wallet

⚠️ Disclaimer: This is an educational project.

The goal of this project is to study and experiment with the architecture of a Bitcoin wallet using Java, Spring Boot, and the bitcoinj library. It serves as a learning platform for exploring key concepts such as BIP39 mnemonic generation, HD wallet structures (BIP32/44), transaction signing, and blockchain synchronization.

A secure Bitcoin wallet built with **Java**, **Spring Boot**, and **bitcoinj**.

This project supports:
- **Mnemonic phrase generation (BIP-39)**
- **HD wallet structure (BIP-32/44)**
- **Address management**
- **Transaction signing and broadcasting**
- **Blockchain synchronization via SPV (Simple Payment Verification)**
- **Offline-first operation**

---

## Features

- Generate and restore wallets using **mnemonic phrases**.
- Derive multiple **Bitcoin addresses**.
- Synchronize with the **Bitcoin Testnet/Mainnet**.
- Sign and send **Bitcoin transactions** securely.
- Encrypt wallet data with **AES + PBKDF2** for secure storage.
- RESTful API with **Spring Boot**.
- Option for **CLI** or **JavaFX GUI**.

---

## Technologies

- **Java 21**
- **Spring Boot**
- **bitcoinj**
- **JavaFX (optional GUI)**
- **Lombok**
- **Spring Security (optional)**

---

## Getting Started

To set up and run the project locally (including Bitcoin regtest configuration), check out the [Getting Started Guide](docs/GETTING_STARTED.md).

## License

This project is licensed under the [MIT License](LICENSE)
