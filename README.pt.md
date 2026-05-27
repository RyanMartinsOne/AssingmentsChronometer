# Controle do Tempo (Assignments Chronometer)

🇺🇸 [English](README.md)

Aplicativo Android para cronometrar e registrar designações em reuniões das Testemunhas de Jeová — tanto as do fim de semana (Discurso Público e A Sentinela) quanto as de meio de semana (Tesouros da Palavra de Deus, Joias Espirituais e demais partes da *Nossa Vida Cristã*).

---

## Funcionalidades

### Cronômetro
- Cronômetro em tempo real com estados **Iniciar / Pausar / Continuar / Reiniciar**
- Indicação visual de tempo excedido (fundo vermelho ao ultrapassar a duração prevista)
- Sobreposição flutuante (*overlay*) que permanece visível sobre outros aplicativos
- Modo de sobreposição simplificada: toque para pausar/continuar
- Contagem regressiva de comentários para a parte de Joias Espirituais (slots de 30 s)

### Registros de Partes Semanais
- Importação automática via **OCR** a partir de foto da câmera ou arquivo PDF/imagem
- Parser inteligente que identifica data, título, designado, sala e duração de cada parte
- Adição e edição manual de partes via diálogo
- Registro do tempo realizado e cálculo automático de atraso
- Agrupamento das partes por semana com cabeçalho de data fixo (*sticky header*)
- Exportação e importação dos registros em formato `.acdata` (JSON)
- Compartilhamento individual de partes via texto (WhatsApp, etc.)

### Designações Rápidas
- Atalho para Discurso Público, A Sentinela, Tesouros e Joias Espirituais com duração pré-definida

### Configurações
- Tema: Sistema / Claro / Escuro
- Cores dinâmicas (Material You — Android 12+)
- Controle de opacidade, largura e altura da sobreposição (8 níveis cada)
- Presets rápidos: Compacto, Padrão, Grande
- Mostrar/ocultar contagem de comentários na sobreposição
- Ativar/desativar sobreposição simplificada
- Gerenciamento de dados: exportar, importar e limpar registros
- Acesso direto à permissão de sobreposição pela tela de configurações
- Tela de licenças de código aberto

---

## Arquitetura

O projeto segue a arquitetura **MVVM** com separação clara em camadas:

```
app/
├── data/
│   ├── model/          # Modelos de domínio (Assignment, WeeklyPart)
│   └── repository/     # Acesso a dados (Settings, Records, PdfOcr)
├── overlay/            # Serviço e ciclo de vida da sobreposição
├── navigation/         # NavHost e definição de rotas
├── ui/
│   ├── components/     # Componentes reutilizáveis (cards, dialogs, etc.)
│   ├── screens/        # Telas da aplicação
│   └── theme/          # Tema Material3, cores e tipografia
├── util/               # Utilitários (OCR parser, formatadores de data)
├── viewmodel/          # ViewModels compartilhados via Application
├── App.kt              # Application com ViewModelStore global
└── MainActivity.kt     # Ponto de entrada, deep links e controle do overlay
```

### Fluxo de dados

```
UI (Compose) ──► ViewModel ──► Repository ──► DataStore / ContentResolver
                     ▲
                     │ state (Compose State / StateFlow)
```

Os ViewModels (`SharedViewModel`, `WeeklyPartsViewModel`, `SettingsViewModel`) são instanciados na classe `App` e compartilhados entre `MainActivity` e `ChronometerOverlayService`, garantindo estado consistente mesmo quando o overlay está visível sobre outros apps.

---

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| UI | Jetpack Compose + Material3 |
| Navegação | Navigation Compose |
| Estado | Compose State + StateFlow |
| Persistência | DataStore Preferences |
| OCR | ML Kit Text Recognition (Latin) |
| PDF → Bitmap | PdfRenderer (Android nativo) |
| Serialização | Kotlinx Serialization JSON |
| Concorrência | Kotlin Coroutines |
| Overlay | WindowManager + ComposeView customizado |

---

## Classes Principais

### `App.kt`
Instância global dos três ViewModels via `ViewModelStoreOwner`. Permite que tanto a `MainActivity` quanto o `ChronometerOverlayService` compartilhem o mesmo estado.

### `SharedViewModel`
Controla o estado do cronômetro (tempo, running, paused) e a designação/parte atualmente ativa. Usa `SystemClock.elapsedRealtime()` para precisão independente do relógio do sistema.

### `WeeklyPartsViewModel`
Gerencia a lista de partes semanais. Coordena importação via OCR (câmera e PDF), exportação/importação de arquivos `.acdata` e ações de UI (navegação pendente, feedbacks de toast/snackbar).

### `SettingsViewModel`
Expõe `SettingsUiState` via `StateFlow` combinando as preferências do `SettingsRepository` com mensagens de validação das dimensões da sobreposição.

### `OcrParser`
Interpreta as linhas OCR extraídas pelo ML Kit, identifica partes por regex (`^\d+\.\s*.+\(\d+\s*min\)`), associa designados à coluna direita da página e filtra termos estruturais do programa.

### `ChronometerOverlayService`
Serviço que exibe a sobreposição usando `WindowManager` + `ComposeView` com ciclo de vida próprio (`OverlayLifecycleOwner`). Inicia quando o usuário sai do app com o cronômetro ativo; encerra ao retornar.

### `OverlaySizeRules`
Regras de validação cruzada entre largura e altura da sobreposição: certos níveis de altura exigem um nível mínimo de largura para garantir legibilidade.

---

## Navegação

```
Home (Cronômetro)
├── Assignments (Designações rápidas) ──► Home
├── Record (Registros semanais) ──► Home
└── Settings (Configurações) ──► Licenses
```

A navegação usa `NavHost` com transições sem animação (`EnterTransition.None`) para respostas imediatas. Deep links via esquema `chronometer://` permitem atalhos da tela inicial do Android:

| URI | Ação |
|---|---|
| `chronometer://start` | Inicia o cronômetro |
| `chronometer://import-media` | Abre importação de PDF/imagem |
| `chronometer://scan` | Abre a câmera |
| `chronometer://import-acdata` | Abre importação de arquivo de registros |

---

## Formato de Arquivo `.acdata`

Arquivo JSON com a seguinte estrutura:

```json
{
  "version": 1,
  "parts": [
    {
      "uid": "uuid-gerado",
      "id": "3",
      "title": "Joias Espirituais",
      "durationInMinutes": 10,
      "room": "Principal",
      "assignees": "João Silva",
      "dateText": "5 de junho de 2025",
      "realizedTimeOnSeconds": 623
    }
  ]
}
```

---

## Localização

O app possui suporte completo a duas localidades:

- **`values/strings.xml`** — Inglês (fallback padrão)
- **`values-pt/strings.xml`** — Português (Brasil)

O parser de datas (`DateUtils.parseOcrDate`) reconhece meses em português (jan, fev, mar…) e também o formato `DD/MM`.

---

## Permissões

| Permissão | Uso |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Exibir a sobreposição flutuante |
| `CAMERA` | Fotografar o programa da reunião |
| `READ_EXTERNAL_STORAGE` | Importar PDF/imagem da galeria |

---

## Bibliotecas Open Source

- Jetpack Compose 1.6+ — Apache 2.0
- Material3 for Compose 1.2+ — Apache 2.0
- Navigation Compose 2.7+ — Apache 2.0
- Lifecycle ViewModel Compose 2.7+ — Apache 2.0
- DataStore Preferences 1.1+ — Apache 2.0
- ML Kit Text Recognition 16+ — Apache 2.0
- Kotlinx Serialization JSON 1.6+ — Apache 2.0
- Kotlin Coroutines 1.8+ — Apache 2.0
- AndroidX Core KTX 1.13+ — Apache 2.0
- AndroidX Activity Compose 1.9+ — Apache 2.0
- AndroidX SavedState 1.2+ — Apache 2.0
