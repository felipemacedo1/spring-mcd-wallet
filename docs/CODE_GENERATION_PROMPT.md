# Prompt Completo para Agent Especialista em Geração de Código
## Objetivo: Desenvolver uma Carteira Bitcoin Funcional e Completa Integrada com Blockchain

---

## 📋 CONTEXTO DO PROJETO

Você é um agente especialista em desenvolvimento de aplicações blockchain e deve completar a implementação de um **wallet Bitcoin funcional e completo** usando Java 21, Spring Boot 3.4.4 e a biblioteca bitcoinj 0.16.3.

O projeto **spring-mcd-wallet** já possui uma base sólida com:
- Estrutura de pacotes bem definida
- Serviços básicos implementados (geração de mnemonic BIP39, derivação HD BIP32/44)
- API REST com endpoints para balance, transactions e send
- Sincronização blockchain via SPV (Simple Payment Verification)
- Suporte para mainnet, testnet e regtest

**Sua missão:** Completar a implementação para entregar uma carteira Bitcoin totalmente funcional, segura e pronta para uso educacional/demonstrativo, com todos os recursos esperados de uma wallet moderna.

---

## 🎯 OBJETIVOS ESPECÍFICOS

### 1. Completar Funcionalidades Core da Wallet

#### 1.1 Gerenciamento de Wallet
**Implementar:**

```java
// POST /api/wallet/create
CreateWalletRequest {
    String password;        // Senha para criptografar a wallet
    String walletName;      // Nome opcional da wallet
}

CreateWalletResponse {
    String walletId;
    String mnemonic;        // 12 palavras BIP39
    String firstAddress;    // Primeiro endereço derivado
    String warning;         // Aviso para guardar o mnemonic
}

// POST /api/wallet/restore
RestoreWalletRequest {
    String mnemonic;        // 12 ou 24 palavras
    String password;        // Senha para criptografar
    String walletName;
    Long creationTime;      // Unix timestamp (opcional)
}

RestoreWalletResponse {
    String walletId;
    String firstAddress;
    int addressesRecovered;
    String balance;
}

// GET /api/wallet/info
WalletInfoResponse {
    String walletId;
    String network;         // mainnet/testnet/regtest
    String balance;
    int addressCount;
    int transactionCount;
    boolean isEncrypted;
    long creationTime;
}

// DELETE /api/wallet/{walletId}
// Deleta a wallet (requer senha)
```

**Requisitos:**
- Implementar `WalletManagementService` para criação, restauração e exclusão
- Salvar múltiplas wallets em diretórios separados
- Criptografar arquivos de wallet com AES-256-GCM usando senha do usuário
- Derivar chave de criptografia usando PBKDF2 (100.000+ iterações)
- Validar força da senha (mínimo 8 caracteres, letras, números)
- Gerar salt aleatório por wallet

#### 1.2 Gerenciamento de Endereços
**Implementar:**

```java
// GET /api/wallet/addresses?page=0&size=20
AddressListResponse {
    List<AddressInfo> addresses;
    int totalAddresses;
    int currentPage;
}

AddressInfo {
    String address;
    String label;           // Label opcional
    String path;            // BIP44 path (m/44'/0'/0'/0/0)
    String balance;
    int transactionCount;
    boolean isUsed;         // True se recebeu alguma transação
}

// POST /api/wallet/addresses/generate
GenerateAddressRequest {
    String label;           // Opcional
    AddressType type;       // P2PKH, P2WPKH (SegWit)
}

GenerateAddressResponse {
    String address;
    String path;
    String publicKey;
    String qrCodeBase64;    // QR Code em base64
}

// PUT /api/wallet/addresses/{address}/label
UpdateAddressLabelRequest {
    String label;
}

// GET /api/wallet/addresses/{address}
AddressDetailResponse {
    String address;
    String label;
    String balance;
    List<TransactionSummary> transactions;
    String qrCodeBase64;
}
```

**Requisitos:**
- Implementar gap limit (BIP44): parar de derivar após 20 endereços não usados
- Suportar P2PKH (Pay-to-PubKey-Hash) e P2WPKH (SegWit)
- Gerar QR Codes usando biblioteca ZXing
- Persistir labels em metadata file (JSON)
- Implementar paginação eficiente

#### 1.3 Gerenciamento de Transações Avançado
**Implementar:**

```java
// GET /api/wallet/transactions?page=0&size=20&type=ALL
TransactionListResponse {
    List<TransactionDetail> transactions;
    int totalCount;
    int currentPage;
}

TransactionDetail {
    String txId;
    String type;            // RECEIVED, SENT, PENDING
    String amount;
    String fee;
    String fromAddress;
    String toAddress;
    int confirmations;
    long timestamp;
    TransactionStatus status; // PENDING, CONFIRMED, FAILED
    String note;            // Nota opcional
}

// GET /api/wallet/transaction/{txId}
TransactionDetailResponse {
    String txId;
    String rawTx;           // Hex da transação
    String blockHash;
    int blockHeight;
    long blockTime;
    String amount;
    String fee;
    int confirmations;
    List<TransactionInput> inputs;
    List<TransactionOutput> outputs;
    String note;
}

// POST /api/wallet/transaction/send-advanced
SendAdvancedRequest {
    List<OutputSpec> outputs;  // Multi-output
    FeeOption feeOption;       // LOW, MEDIUM, HIGH, CUSTOM
    long customFeeRate;        // sat/vByte
    String note;               // Nota opcional
}

OutputSpec {
    String address;
    String amount;
}

// GET /api/wallet/transaction/fee-estimate
FeeEstimateResponse {
    long lowFee;            // sat/vByte
    long mediumFee;
    long highFee;
    long estimatedTime;     // Minutos
}

// POST /api/wallet/transaction/{txId}/note
AddTransactionNoteRequest {
    String note;
}
```

**Requisitos:**
- Implementar fee estimation usando médias da mempool
- Suportar múltiplas saídas (batch transactions)
- Calcular fee automaticamente baseado em tamanho estimado
- Persistir notas de transação em metadata
- Implementar RBF (Replace-By-Fee) para transações não confirmadas
- Adicionar histórico de transações em banco de dados (H2 embedded)

### 2. Segurança e Criptografia

#### 2.1 Criptografia de Wallet
**Implementar:**
- AES-256-GCM para criptografar wallet files
- PBKDF2-HMAC-SHA256 com 100.000+ iterações
- Salt único por wallet (16 bytes)
- IV (Initialization Vector) único por operação de criptografia
- Verificação de integridade com authentication tag

**Classe a criar:**
```java
@Service
public class WalletEncryptionService {
    
    byte[] deriveKey(String password, byte[] salt);
    
    EncryptedData encrypt(byte[] plaintext, String password);
    
    byte[] decrypt(EncryptedData encryptedData, String password) 
        throws WalletDecryptionException;
    
    boolean verifyPassword(String password, byte[] salt, byte[] encryptedWallet);
}

record EncryptedData(
    byte[] ciphertext,
    byte[] salt,
    byte[] iv,
    byte[] authTag
) {}
```

#### 2.2 Autenticação e Autorização
**Implementar:**
- API Key authentication para proteger endpoints
- JWT tokens para sessões de usuário
- Rate limiting (5 tentativas de senha incorreta)
- Timeout de sessão (15 minutos de inatividade)

**Endpoints de autenticação:**
```java
// POST /api/auth/unlock
UnlockWalletRequest {
    String walletId;
    String password;
}

UnlockWalletResponse {
    String sessionToken;    // JWT
    long expiresAt;
}

// POST /api/auth/lock
// Invalida sessão e bloqueia wallet

// POST /api/auth/change-password
ChangePasswordRequest {
    String currentPassword;
    String newPassword;
}
```

**Requisitos:**
- Implementar `@Secured` annotations nos endpoints sensíveis
- Criar filtro de autenticação para validar JWT
- Implementar rate limiting com Bucket4j ou similar
- Logar todas as tentativas de autenticação

### 3. Integração Blockchain Aprimorada

#### 3.1 Monitoramento e Sincronização
**Implementar:**

```java
// GET /api/blockchain/status
BlockchainStatusResponse {
    String network;
    int currentHeight;
    int syncedHeight;
    double syncProgress;    // 0.0 a 1.0
    int peerCount;
    boolean isSyncing;
    String bestBlockHash;
    long lastBlockTime;
}

// GET /api/blockchain/peers
PeerListResponse {
    List<PeerInfo> peers;
}

PeerInfo {
    String address;
    int port;
    String userAgent;
    long pingTime;
    int blockHeight;
}

// POST /api/blockchain/resync
// Força ressincronização completa
```

**Requisitos:**
- Implementar health checks para verificar conectividade
- Adicionar timeout e retry logic para conexões de peers
- Usar checkpoints para acelerar sincronização inicial
- Implementar fallback para múltiplos peers

#### 3.2 UTXO Management
**Implementar:**

```java
// GET /api/wallet/utxos
UTXOListResponse {
    List<UTXO> utxos;
    String totalValue;
    int count;
}

UTXO {
    String txId;
    int outputIndex;
    String address;
    String value;
    int confirmations;
    String scriptPubKey;
}

// Coin selection strategy
@Service
public class CoinSelectionService {
    
    // Estratégia: Largest First
    List<UTXO> selectCoinsLargestFirst(List<UTXO> available, long targetValue);
    
    // Estratégia: Privacy-focused (misturar UTXOs)
    List<UTXO> selectCoinsPrivacy(List<UTXO> available, long targetValue);
    
    // Estratégia: Minimize Fee
    List<UTXO> selectCoinsMinimizeFee(List<UTXO> available, long targetValue);
}
```

### 4. Interface de Usuário (Web UI)

#### 4.1 Frontend com HTML/CSS/JavaScript
**Criar páginas:**

1. **Dashboard** (`src/main/resources/static/index.html`)
   - Card com saldo total
   - Lista de transações recentes
   - Botões: Enviar, Receber, Configurações

2. **Enviar Bitcoin** (`send.html`)
   - Formulário: endereço destinatário, valor, taxa
   - Botão para escanear QR code (se webcam disponível)
   - Confirmação antes de enviar
   - Feedback de sucesso/erro

3. **Receber Bitcoin** (`receive.html`)
   - Exibir endereço atual
   - QR Code grande
   - Botão copiar endereço
   - Botão gerar novo endereço

4. **Endereços** (`addresses.html`)
   - Tabela com todos os endereços
   - Filtros: usados/não usados
   - Editar labels
   - Gerar novo endereço

5. **Transações** (`transactions.html`)
   - Histórico completo com paginação
   - Filtros: tipo, data, valor
   - Ver detalhes de transação
   - Adicionar notas

6. **Configurações** (`settings.html`)
   - Trocar senha
   - Backup do mnemonic (com avisos)
   - Exportar wallet
   - Configurações de rede
   - Configurações de taxa padrão

**Tecnologias sugeridas:**
- Bootstrap 5 para layout responsivo
- jQuery para AJAX calls
- Chart.js para gráficos (opcional)
- QRious ou similar para gerar QR codes
- HTML5 WebRTC para escanear QR codes

#### 4.2 API Controller para UI
```java
@Controller
public class WebUIController {
    
    @GetMapping("/")
    public String index() {
        return "index.html";
    }
    
    @GetMapping("/send")
    public String send() {
        return "send.html";
    }
    
    // ... outros endpoints
}
```

### 5. Testes Completos

#### 5.1 Testes Unitários
**Criar testes para:**

```java
// WalletEncryptionServiceTest
@Test void shouldEncryptAndDecryptWallet()
@Test void shouldFailWithWrongPassword()
@Test void shouldUseDifferentIVForEachEncryption()

// CoinSelectionServiceTest
@Test void shouldSelectOptimalUTXOs()
@Test void shouldMinimizeChange()

// FeeEstimationServiceTest
@Test void shouldEstimateReasonableFees()

// AddressDerivationTest
@Test void shouldRespectGapLimit()
@Test void shouldDeriveBothP2PKHAndP2WPKH()

// TransactionBuilderTest
@Test void shouldBuildValidTransaction()
@Test void shouldCalculateCorrectFee()
@Test void shouldSupportMultipleOutputs()
```

#### 5.2 Testes de Integração
**Criar testes com Bitcoin Core regtest:**

```java
@SpringBootTest
@Testcontainers
class BitcoinRegtestIntegrationTest {
    
    @Container
    static BitcoindContainer bitcoind = new BitcoindContainer()
        .withRegtest(true);
    
    @Test
    void shouldCreateWalletAndReceiveBitcoin() {
        // 1. Criar wallet
        // 2. Gerar endereço
        // 3. Enviar BTC via bitcoin-cli
        // 4. Aguardar confirmação
        // 5. Verificar saldo
    }
    
    @Test
    void shouldSendBitcoinSuccessfully() {
        // Teste completo de envio
    }
}
```

#### 5.3 Testes de Segurança
**Implementar:**
- Teste de força bruta de senha (deve bloquear após N tentativas)
- Teste de SQL injection nos endpoints
- Teste de XSS nas páginas web
- Teste de validação de inputs maliciosos
- Teste de criptografia (verificar aleatoriedade dos IVs)

### 6. Documentação

#### 6.1 OpenAPI/Swagger
**Adicionar dependência:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Anotar controllers:**
```java
@RestController
@RequestMapping("/api/wallet")
@Tag(name = "Wallet Management", description = "APIs for wallet operations")
public class WalletController {
    
    @Operation(summary = "Create new wallet", 
               description = "Creates a new Bitcoin wallet with BIP39 mnemonic")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Wallet created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/create")
    public ResponseEntity<CreateWalletResponse> createWallet(
        @Valid @RequestBody CreateWalletRequest request) {
        // ...
    }
}
```

#### 6.2 README Completo
**Atualizar README.md com:**
- Guia de instalação passo a passo
- Exemplos de uso da API (curl)
- Screenshots da UI
- Arquitetura do sistema (diagrama)
- FAQ
- Troubleshooting comum
- Como contribuir

#### 6.3 Guia de Segurança
**Criar SECURITY.md:**
- Melhores práticas para backup
- Como reportar vulnerabilidades
- Avisos sobre uso em produção
- Recomendações de senha
- Guia de recuperação de wallet

### 7. Configuração e Deploy

#### 7.1 Docker
**Criar Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/spring-mcd-wallet-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Criar docker-compose.yml:**
```yaml
version: '3.8'

services:
  wallet:
    build: .
    ports:
      - "8080:8080"
    environment:
      - BITCOIN_NETWORK=regtest
      - SPRING_PROFILES_ACTIVE=prod
    volumes:
      - wallet-data:/app/wallet-data

  bitcoin-core:
    image: ruimarinho/bitcoin-core:latest
    command:
      - -regtest
      - -server
      - -rpcbind=0.0.0.0
      - -rpcallowip=0.0.0.0/0
      - -rpcuser=dev
      - -rpcpassword=dev
    ports:
      - "18443:18443"
    volumes:
      - bitcoin-data:/home/bitcoin/.bitcoin

volumes:
  wallet-data:
  bitcoin-data:
```

#### 7.2 Profiles de Configuração
**application-dev.yml:**
```yaml
bitcoin:
  network: regtest

logging:
  level:
    com.mcd.wallet: DEBUG
```

**application-prod.yml:**
```yaml
bitcoin:
  network: testnet

logging:
  level:
    root: WARN
    com.mcd.wallet: INFO

security:
  require-authentication: true
  rate-limit:
    enabled: true
    requests-per-minute: 60
```

### 8. Monitoramento e Observabilidade

#### 8.1 Métricas com Micrometer
**Adicionar dependências:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Configurar endpoints:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Métricas customizadas:**
```java
@Service
public class MetricsService {
    
    private final Counter walletsCreated;
    private final Counter transactionsSent;
    private final Gauge walletBalance;
    
    public MetricsService(MeterRegistry registry) {
        this.walletsCreated = Counter.builder("wallet.created")
            .description("Number of wallets created")
            .register(registry);
            
        this.transactionsSent = Counter.builder("transaction.sent")
            .description("Number of transactions sent")
            .register(registry);
            
        // ...
    }
}
```

#### 8.2 Health Checks
```java
@Component
public class BlockchainHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Verificar conectividade com peers
        // Verificar sincronização
        // Retornar UP ou DOWN
    }
}
```

### 9. Recursos Extras (Bônus)

#### 9.1 CLI Interface
**Criar usando Spring Shell:**
```xml
<dependency>
    <groupId>org.springframework.shell</groupId>
    <artifactId>spring-shell-starter</artifactId>
    <version>3.2.0</version>
</dependency>
```

**Comandos:**
```java
@ShellComponent
public class WalletCommands {
    
    @ShellMethod("Create a new wallet")
    public String createWallet(String password) {
        // ...
    }
    
    @ShellMethod("Get wallet balance")
    public String balance() {
        // ...
    }
    
    @ShellMethod("Send bitcoin")
    public String send(String address, String amount) {
        // ...
    }
}
```

#### 9.2 Export/Import de Wallet
```java
// POST /api/wallet/export
ExportWalletRequest {
    String password;
    ExportFormat format; // JSON, BIP32_EXTENDED_KEY
}

ExportWalletResponse {
    String exportData;  // Encrypted JSON ou xprv
    String checksum;    // SHA256 hash
}

// POST /api/wallet/import
ImportWalletRequest {
    String importData;
    String password;
    ExportFormat format;
}
```

#### 9.3 Multi-signature (Avançado)
```java
// POST /api/wallet/multisig/create
CreateMultisigRequest {
    int requiredSignatures;     // m
    List<String> publicKeys;    // n public keys
}

CreateMultisigResponse {
    String multisigAddress;
    String redeemScript;
}
```

---

## 🏗️ ARQUITETURA TÉCNICA DETALHADA

### Camadas da Aplicação

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  - REST Controllers                     │
│  - Web UI (HTML/JS)                     │
│  - DTO Validation                       │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│         Service Layer                   │
│  - WalletManagementService              │
│  - TransactionService                   │
│  - AddressService                       │
│  - EncryptionService                    │
│  - BlockchainService                    │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│         Repository Layer                │
│  - WalletRepository (File-based)        │
│  - TransactionRepository (H2)           │
│  - MetadataRepository (JSON)            │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│         Integration Layer               │
│  - bitcoinj WalletAppKit                │
│  - Bitcoin Core RPC (optional)          │
│  - External Fee API                     │
└─────────────────────────────────────────┘
```

### Fluxo de Criação de Wallet

```
1. User Request → POST /api/wallet/create { password }
2. WalletController → WalletManagementService.createWallet()
3. Generate Mnemonic → BIP39 (12 palavras)
4. Derive Seed → PBKDF2 from mnemonic
5. Create HD Wallet → BIP32 master key
6. Derive First Address → BIP44 m/44'/0'/0'/0/0
7. Encrypt Wallet → AES-256-GCM with password
8. Save to Disk → wallet-{id}/wallet.dat
9. Return Response → { walletId, mnemonic, firstAddress }
```

### Fluxo de Envio de Transação

```
1. User Request → POST /api/wallet/send { address, amount }
2. Authenticate → Verify JWT/session
3. Validate → Address format, sufficient balance
4. Estimate Fee → Based on transaction size
5. Select UTXOs → Coin selection algorithm
6. Build Transaction → Create inputs/outputs
7. Sign Transaction → Using private keys from HD wallet
8. Broadcast → To Bitcoin network via peers
9. Monitor → Wait for confirmation
10. Update DB → Store transaction details
11. Return Response → { txId, status }
```

---

## 🔧 ESTRUTURA DE PACOTES COMPLETA

```
com.mcd.wallet/
├── SpringMcdWalletApplication.java
│
├── config/
│   ├── BitcoinConfig.java
│   ├── SecurityConfig.java
│   ├── CryptoConfig.java
│   ├── SwaggerConfig.java
│   └── WebConfig.java
│
├── controller/
│   ├── WalletController.java
│   ├── AddressController.java
│   ├── TransactionController.java
│   ├── BlockchainController.java
│   ├── AuthController.java
│   └── WebUIController.java
│
├── controller/dto/
│   ├── request/
│   │   ├── CreateWalletRequest.java
│   │   ├── RestoreWalletRequest.java
│   │   ├── SendBitcoinRequest.java
│   │   ├── SendAdvancedRequest.java
│   │   └── ...
│   ├── response/
│   │   ├── CreateWalletResponse.java
│   │   ├── TransactionResponse.java
│   │   ├── AddressListResponse.java
│   │   └── ...
│   └── ErrorResponse.java
│
├── service/
│   ├── WalletManagementService.java
│   ├── AddressService.java
│   ├── TransactionService.java
│   ├── EncryptionService.java
│   ├── BlockchainService.java
│   ├── FeeEstimationService.java
│   ├── CoinSelectionService.java
│   ├── QRCodeService.java
│   ├── BackupService.java
│   └── MetricsService.java
│
├── service/impl/
│   └── (implementations...)
│
├── repository/
│   ├── WalletRepository.java
│   ├── TransactionRepository.java
│   ├── MetadataRepository.java
│   └── UTXORepository.java
│
├── domain/
│   ├── Wallet.java
│   ├── Address.java
│   ├── Transaction.java
│   ├── UTXO.java
│   └── WalletMetadata.java
│
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── RateLimitFilter.java
│   └── PasswordValidator.java
│
├── exception/
│   ├── WalletNotFoundException.java
│   ├── InsufficientBalanceException.java
│   ├── InvalidPasswordException.java
│   ├── WalletAlreadyExistsException.java
│   └── GlobalExceptionHandler.java
│
└── util/
    ├── BitcoinUtils.java
    ├── CryptoUtils.java
    ├── ValidationUtils.java
    └── DateTimeUtils.java
```

---

## ✅ CHECKLIST DE IMPLEMENTAÇÃO

### Fase 1: Core Wallet (Prioridade Alta)
- [ ] Implementar WalletManagementService
  - [ ] Create wallet
  - [ ] Restore wallet
  - [ ] Delete wallet
  - [ ] List wallets
- [ ] Implementar WalletEncryptionService
  - [ ] AES-256-GCM encryption
  - [ ] PBKDF2 key derivation
  - [ ] Password validation
- [ ] Implementar WalletRepository
  - [ ] Save/load encrypted wallet
  - [ ] Multi-wallet support
- [ ] Testes unitários para wallet management
- [ ] Testes de criptografia

### Fase 2: Address Management (Prioridade Alta)
- [ ] Implementar AddressService
  - [ ] Generate address (P2PKH)
  - [ ] Generate SegWit address (P2WPKH)
  - [ ] List addresses with pagination
  - [ ] Update address labels
- [ ] Implementar QRCodeService
  - [ ] Generate QR code for address
  - [ ] Encode payment request (BIP21)
- [ ] Implementar gap limit logic
- [ ] Testes de derivação de endereços

### Fase 3: Transaction Management (Prioridade Alta)
- [ ] Implementar TransactionService
  - [ ] Send transaction (single output)
  - [ ] Send transaction (multiple outputs)
  - [ ] Get transaction history
  - [ ] Get transaction details
  - [ ] Add transaction notes
- [ ] Implementar FeeEstimationService
  - [ ] Estimate fees (low/medium/high)
  - [ ] Calculate transaction size
- [ ] Implementar CoinSelectionService
  - [ ] Largest first strategy
  - [ ] Minimize fee strategy
  - [ ] Privacy strategy
- [ ] Implementar TransactionRepository (H2)
- [ ] Testes de transação end-to-end

### Fase 4: Security (Prioridade Alta)
- [ ] Implementar AuthController
  - [ ] Unlock wallet endpoint
  - [ ] Lock wallet endpoint
  - [ ] Change password endpoint
- [ ] Implementar JWT authentication
  - [ ] Token generation
  - [ ] Token validation
  - [ ] Token refresh
- [ ] Implementar rate limiting
  - [ ] Per-endpoint limits
  - [ ] IP-based throttling
- [ ] Adicionar audit logging
- [ ] Testes de segurança

### Fase 5: Web UI (Prioridade Média)
- [ ] Criar layout base (index.html)
  - [ ] Navbar
  - [ ] Sidebar
  - [ ] Dashboard cards
- [ ] Página de envio (send.html)
  - [ ] Formulário de envio
  - [ ] Validação de endereço
  - [ ] Seleção de taxa
  - [ ] Confirmação
- [ ] Página de recebimento (receive.html)
  - [ ] Exibir endereço atual
  - [ ] QR code
  - [ ] Botão copiar
- [ ] Página de endereços (addresses.html)
  - [ ] Tabela de endereços
  - [ ] Filtros
  - [ ] Editar labels
- [ ] Página de transações (transactions.html)
  - [ ] Histórico paginado
  - [ ] Filtros
  - [ ] Detalhes de transação
- [ ] Página de configurações (settings.html)
  - [ ] Trocar senha
  - [ ] Backup
  - [ ] Preferências

### Fase 6: Blockchain Integration (Prioridade Média)
- [ ] Implementar BlockchainController
  - [ ] Get sync status
  - [ ] List peers
  - [ ] Trigger resync
- [ ] Melhorar BlockchainService
  - [ ] Add checkpoints
  - [ ] Implement peer fallback
  - [ ] Add retry logic
- [ ] Implementar health checks
- [ ] Testes de integração com regtest

### Fase 7: Documentation (Prioridade Média)
- [ ] Configurar Swagger/OpenAPI
  - [ ] Anotar todos os endpoints
  - [ ] Adicionar exemplos
  - [ ] Documentar DTOs
- [ ] Atualizar README.md
  - [ ] Installation guide
  - [ ] API examples
  - [ ] Screenshots
- [ ] Criar SECURITY.md
- [ ] Criar CONTRIBUTING.md

### Fase 8: Testing (Prioridade Média)
- [ ] Testes unitários (>80% coverage)
  - [ ] Services
  - [ ] Controllers
  - [ ] Security components
- [ ] Testes de integração
  - [ ] Bitcoin Core regtest
  - [ ] Database operations
  - [ ] API endpoints
- [ ] Testes de segurança
  - [ ] Penetration testing
  - [ ] Vulnerability scanning

### Fase 9: Deployment (Prioridade Baixa)
- [ ] Criar Dockerfile
- [ ] Criar docker-compose.yml
- [ ] Configurar profiles (dev/prod)
- [ ] Setup CI/CD
- [ ] Deploy to staging

### Fase 10: Advanced Features (Prioridade Baixa - Bônus)
- [ ] CLI interface com Spring Shell
- [ ] Export/Import wallet
- [ ] Multi-signature support
- [ ] Hardware wallet integration
- [ ] Lightning Network support

---

## 📚 DEPENDÊNCIAS ADICIONAIS NECESSÁRIAS

Adicionar ao `pom.xml`:

```xml
<!-- OpenAPI/Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- QR Code Generation -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Rate Limiting -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>

<!-- Database (H2 for embedded) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Metrics -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- CLI (Optional) -->
<dependency>
    <groupId>org.springframework.shell</groupId>
    <artifactId>spring-shell-starter</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
```

---

## 🎓 REFERÊNCIAS TÉCNICAS

### Bitcoin Standards (BIPs)
- **BIP39**: Mnemonic code for generating deterministic keys
- **BIP32**: Hierarchical Deterministic Wallets
- **BIP44**: Multi-Account Hierarchy for Deterministic Wallets
- **BIP21**: URI Scheme for Bitcoin payments
- **BIP141**: Segregated Witness (SegWit)
- **BIP125**: Replace-By-Fee (RBF)

### Bibliotecas e Ferramentas
- **bitcoinj**: https://bitcoinj.org/
- **Spring Boot**: https://spring.io/projects/spring-boot
- **ZXing**: https://github.com/zxing/zxing (QR Codes)
- **jjwt**: https://github.com/jwtk/jjwt (JWT)
- **Bucket4j**: https://bucket4j.com/ (Rate Limiting)

### Segurança
- **OWASP Top 10**: https://owasp.org/www-project-top-ten/
- **NIST Cryptographic Standards**: https://csrc.nist.gov/
- **CWE/SANS Top 25**: https://cwe.mitre.org/

---

## 🚀 CRITÉRIOS DE SUCESSO

### Funcionalidade
- ✅ Criar, restaurar e gerenciar múltiplas wallets
- ✅ Gerar e gerenciar endereços Bitcoin (P2PKH e P2WPKH)
- ✅ Enviar e receber Bitcoin em testnet/regtest
- ✅ Visualizar histórico de transações
- ✅ Estimar taxas automaticamente
- ✅ Interface web funcional e intuitiva

### Segurança
- ✅ Criptografia AES-256-GCM de wallet files
- ✅ Derivação de chave com PBKDF2 (100k+ iterações)
- ✅ Autenticação via JWT
- ✅ Rate limiting implementado
- ✅ Sem logs de informações sensíveis
- ✅ Validação de inputs em todos os endpoints

### Qualidade de Código
- ✅ Cobertura de testes > 80%
- ✅ Documentação completa (README, Swagger, JavaDoc)
- ✅ Código limpo e bem estruturado
- ✅ Tratamento de erros adequado
- ✅ Logging apropriado

### Usabilidade
- ✅ API RESTful intuitiva
- ✅ UI web responsiva e amigável
- ✅ Mensagens de erro claras
- ✅ Documentação de uso completa

### Performance
- ✅ Sincronização blockchain eficiente (SPV)
- ✅ Transações enviadas em < 30 segundos
- ✅ API responde em < 1 segundo (95th percentile)

---

## 💡 DICAS DE IMPLEMENTAÇÃO

### 1. Comece pela base
- Primeiro implemente WalletManagementService
- Teste criação e restauração de wallet
- Depois adicione criptografia

### 2. Use TDD (Test-Driven Development)
- Escreva testes antes de implementar
- Garante código testável e robusto

### 3. Segurança em primeiro lugar
- Nunca logue informações sensíveis
- Use constantes para algoritmos de criptografia
- Implemente validação rigorosa de inputs

### 4. Tratamento de erros
- Crie exceptions customizadas
- Use @ControllerAdvice para tratamento global
- Retorne mensagens de erro amigáveis

### 5. Performance
- Use cache para endereços derivados
- Implemente paginação em todas as listas
- Use async onde apropriado

### 6. Documentação
- Documente enquanto desenvolve
- Use JavaDoc para métodos públicos
- Mantenha README atualizado

---

## 📝 NOTAS FINAIS

Este prompt fornece um roteiro completo para transformar o `spring-mcd-wallet` de um projeto educacional básico em uma carteira Bitcoin funcional e completa. Siga as fases na ordem sugerida, priorizando funcionalidades core e segurança.

Lembre-se: Este é um projeto educacional. Para uso em produção com fundos reais, auditorias de segurança profissionais são essenciais.

**Boa codificação! 🚀💰**
