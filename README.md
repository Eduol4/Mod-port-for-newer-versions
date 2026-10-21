# Jenny & Ellie Companions (SFW) — 1.20.1 Forge

Rebuild limpo (SFW) contendo apenas **Jenny** e **Ellie**, portado para Minecraft **1.20.1 / Forge**.
Base: *SchnurriTV's Sexmod (Jenny Mod)* 1.12.2 — todo o conteúdo NSFW foi deixado de fora.

## ⚠️ Status: Milestone 1 (primeiro build testável)

Esta versão é a **fundação**. O objetivo dela é só: as duas aparecerem no jogo, renderizarem com o modelo vestido + animação, e funcionarem como companheiras básicas. **Ainda não foi compilada/testada por mim** (não rodo Minecraft no meu ambiente) — você é quem compila e testa, e a gente ajusta junto.

### O que esta Milestone 1 inclui
- As entidades **Jenny** e **Ellie** registradas.
- Renderização via **GeckoLib 4** (modelo `dressed` + texturas + animações SFW).
- Animações: `idle`, `walk`, `sit` em uso (as demais SFW já estão nos arquivos, prontas pra usar depois).
- **Spawn eggs** das duas + uma **aba no modo criativo** ("Jenny & Ellie").
- IA básica de companheira: **domesticar**, **seguir o dono**, **sentar ao comando**, atacar quem ataca o dono.

### Ainda NÃO incluso (próximas milestones)
- Menu de ação (follow/stop/go home/set new home/equipment) como no original
- Sistema de equipamento (armadura/armas) e "dress up"
- Combate com arco, "downed"/reviver, ride, throw pearl
- Hug/dash (Ellie), emotes (Jenny)
- Sons (vozes SFW), estruturas (casas), horny potion

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
3. **Domesticar:** segure um **diamante** e clique com o botão direito nela (chance por tentativa — item provisório, fácil de trocar depois).
4. Depois de domesticada: **shift + botão direito** alterna sentar/levantar. Ela segue você quando está de pé.

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
