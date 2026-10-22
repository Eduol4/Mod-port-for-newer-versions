# Jenny & Ellie Companions (SFW) — 1.20.1 Forge

Rebuild limpo (SFW) contendo apenas **Jenny** e **Ellie**, portado para Minecraft **1.20.1 / Forge**.
Base: *SchnurriTV's Sexmod (Jenny Mod)* 1.12.2 — todo o conteúdo NSFW foi deixado de fora.

## ⚠️ Status: Milestone 5

**Ainda não compilada/testada por mim** (não rodo Minecraft no meu ambiente) — você compila e testa, e a gente ajusta junto.

### Novidades da Milestone 5
- **Som + mensagem nas ações do menu:** ao clicar Seguir / Parar / Definir casa / Ir pra casa, ela fala no chat (texto do mod original) e toca uma voz SFW (giggle/happyoh/sigh/sadoh).
- **Animação de ataque:** toca (attack0/1/2, aleatória) quando ela acerta um golpe.
- **Animação de correr:** toca quando ela persegue um alvo ou quando segue um jogador que está correndo (sprint).
- **Agachar (Ellie):** toca a animação de agachar quando ela passa por um espaço de 2 blocos de altura.

Pendente (separado, por ser complexo): **montar na garupa** quando o jogador anda de cavalo/barco.

### Novidades da Milestone 4
- **Config de atributos:** arquivo `config/jennyellie-common.toml` (gerado no 1º run) com HP, dano, velocidade e alcance de cada uma — edite sem mexer no `.jar`.
- **Para e encara ao clicar:** clique direito → ela para e vira de frente pra você (~5s, aguardando comando), exceto se estiver em combate.
- **Etiqueta fixa:** nome "Jenny"/"Ellie" sempre visível em qualquer spawn; name tag não renomeia (só abre o menu).

### Novidades da Milestone 3
- **Ossos escondidos por padrão:** o modelo original traz ossos que o renderizador do mod controlava manualmente. Agora são escondidos: o boneco masculino (`steve`) que aparecia na frente, a "armadura de diamante" cosmética (ossos `armor*`), os itens (`items`) e peças anatômicas NSFW (`cum`/`vagina`). Quando implementarmos equipamento, os ossos de armadura voltam a aparecer de forma condicional.

### Novidades da Milestone 2
- **Correção de modelo/textura:** os geo declaravam `texture_width=64` mas a textura é 4096×4096. Ajustado (UVs reescalados) para o GeckoLib 4 mapear certo.
- **Sempre "domesticadas" por padrão:** não precisa mais de item pra domesticar. A mecânica de item continua no código, porém **desativada** (`CompanionEntity.TAMING_ITEM_ENABLED = false`) para uso futuro.
- **Menu de opções (clique direito):** abre uma tela com botões à esquerda — **Seguir / Parar de seguir / Definir nova casa / Ir para casa** (as ações NSFW que ficavam à direita no original foram removidas).

### Base (Milestone 1)
- Entidades **Jenny** e **Ellie**, render via **GeckoLib 4**, **spawn eggs** + aba criativa, IA de companheira (seguir, voltar pra casa, atacar quem ataca o dono).

### Ainda NÃO incluso (próximas milestones)
- **Inventário/equipamento** (armadura e armas) — o botão será adicionado depois
- Combate com arco, "downed"/reviver, ride, throw pearl
- Hug/dash (Ellie), emotes (Jenny), sentar
- Sons (vozes SFW), estruturas (casas), spawn natural, horny potion

## Como buildar

Pré-requisitos: **JDK 17** instalado.

```bash
# na raiz do projeto
./gradlew build          # Linux/macOS
gradlew.bat build        # Windows
```

O `.jar` sai em `build/libs/jennyellie-0.1.0.jar`. Copie pra pasta `mods` junto com o **GeckoLib** (1.20.1 Forge).

Para testar no ambiente de desenvolvimento:
```bash
./gradlew runClient
```

### Como usar no jogo
1. Pegue os **spawn eggs** na aba criativa "Jenny & Ellie" (ou `/give`).
2. Spawne a Jenny/Ellie.
3. **Clique com o botão direito** nela → abre o menu de opções à esquerda.
4. Use os botões: **Seguir** (ela te segue), **Parar de seguir**, **Definir nova casa** (marca o lugar atual como casa dela), **Ir para casa** (ela volta pra casa).

## Pontos que podem precisar de ajuste no primeiro build
- **Versão do GeckoLib:** `gradle.properties` usa `geckolib_version=4.4.9`. Se o Gradle não achar essa versão, troque pela build mais recente do GeckoLib para 1.20.1 Forge (repo já configurado no `build.gradle`).
- **Compatibilidade dos modelos:** os `.geo.json` (format 1.12.0) e animações (1.8.0) vêm do mod 1.12.2. O GeckoLib 4 deve carregá-los, mas é o primeiro ponto a verificar se algo não renderizar.
- **Item de domesticar:** diamante é só placeholder.

## Estrutura
```
src/main/java/net/jennyellie/
  JennyEllieMod.java            # classe principal
  registry/                     # entidades, itens (spawn eggs), aba criativa, atributos
  entity/                       # CompanionEntity (base) + JennyEntity + EllieEntity
  client/                       # renderers + models (GeckoLib) + registro de renderers
src/main/resources/assets/jennyellie/
  geo/entity/                   # modelos (jenny.geo.json, ellie.geo.json) — só "dressed"
  animations/                   # animações SFW filtradas
  textures/entity/              # jenny.png, ellie.png
  models/item/                  # modelos dos spawn eggs
  lang/                         # en_us, pt_br
```
