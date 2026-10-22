# Companions — 1.20.1 Forge

Rebuild limpo contendo apenas COMPANIONS, portado para Minecraft **1.20.1 / Forge**.
Base: 1.12.2.

## ⚠️ Status: Milestone 1 (primeiro build testável)

Esta versão é a **fundação**. O objetivo dela é só: as duas aparecerem no jogo, renderizarem com o modelo + animação, e funcionarem como companheiras básicas. **Ainda não foi compilada/testada por mim** (não rodo Minecraft no meu ambiente) — você é quem compila e testa, e a gente ajusta junto.

### O que esta Milestone 1 inclui
- As entidades registradas.
- Renderização via **GeckoLib 4** (modelo + texturas + animações).
- Animações: `idle`, `walk`, `sit` em uso (as demais já estão nos arquivos, prontas pra usar depois).
- **Spawn eggs** das duas + uma **aba no modo criativo**.
- IA básica de companheira: **domesticar**, **seguir o dono**, **sentar ao comando**, atacar quem ataca o dono.

### Ainda NÃO incluso (próximas milestones)
- Menu de ação (follow/stop/go home/set new home/equipment) como no original
- Sistema de equipamento (armadura/armas) e "dress up"
- Combate com arco, "downed"/reviver, ride, throw pearl
- Hug/dash, emotes
- Sons, estruturas (casas), potion

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
