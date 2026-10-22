# Companions — 1.20.1 Forge

Rebuild limpo portado para Minecraft **1.20.1 / Forge**.
Base: 1.12.2.

## ⚠️ Status: Milestone 12

**Ainda não compilada/testada por mim** (não rodo Minecraft no meu ambiente) — você compila e testa, e a gente ajusta junto.

### Novidades da Milestone 12
- **Teleport pra casa sem o atraso de 1s** — teletransporta assim que a animação da pearl termina.
- **Balde dá buff:** cada balde de leite também lhe concede **Força I + Regeneração I por 30s**, reiniciando a cada novo balde.

### Novidades da Milestone 11
- **Casa é individual** (cada uma tem a sua) — já era assim, confirmado.
- **"Go home" desliga o seguir na hora** — já era assim, confirmado.
- **Teletransporte pra casa:** se a casa estiver longe (>24 blocos) ou inacessível (chunk não carregado, caminho bloqueado), ela para de tentar andar, faz a **animação de jogar ender pearl** e se **teletransporta pra casa ~1s após a animação** (com partículas e som). Perto e acessível, continua indo a pé.

### Novidades da Milestone 10
- **Abrem e fecham portas** como aldeões.
- **Defesa própria (não domesticadas):** revidam quem as ataca (inclusive o jogador) por ~8-10s e depois se acalmam. Domesticar durante isso faz parar de atacar na hora.
- **Ao domesticar, já entram em "seguir" por padrão.**
- **Botão único Seguir/Parar:** o mesmo botão troca de rótulo conforme o estado.
- **Só defendem o dono quando estão seguindo** (a defesa própria é sempre).
- **Domesticadas não levam golpes do jogador** (botão esquerdo não causa dano).
- **Não atacam animais domesticados** (ex.: se o dono acerta um lobo sem querer).
- **Regeneração:** não domesticadas +1 HP/5s; domesticadas +2 HP/4s.

### Novidades da Milestone 9
- **Som ao domesticar com sucesso** (happyoh) para as duas.
- **1:** + Torchflower (100%).
- **2:** + Pérola de Ender (22%), Olho de Ender (30%), Pitcher Plant (100%).
- **Balde:** clicar nela com um **balde vazio** domestica na hora (100%), vira **balde de leite**, solta corações e toca um som (giggle). Funciona mesmo já domesticada — e nesse caso **não abre o menu**. (Em modo criativo o balde não vira leite, como no comportamento padrão do jogo, mas a domesticação/efeitos acontecem.)

### Novidades da Milestone 8
- **Domesticação de verdade:** elas nascem **não domesticadas** — clicar não faz nada (sem menu/comandos/proteção). É preciso domesticar primeiro, clicando com itens específicos (cada um com sua chance). Falha → fumaça; sucesso → corações (como o lobo). Já domesticada, esses itens só abrem o menu.
  - **1:** flores (dandelion 5%, poppy 5.5%, allium 7%, lily of the valley 10%, wither rose 50%, …), barra de ouro 25%, pérola de ender 33%, olho de ender 40%, diamante 100%.
  - **2:** lilac 10%, rose bush 12%, peony 10%, sunflower 10%, jarro de mel 15%, favo de mel 15%.

### Novidades da Milestone 7
- **Spawn natural perto de aldeões:** onde há aldeões (num raio dos jogadores), há chance de aparecerem. Limite de **1 de cada por mundo**; a vaga só reabre quando a respectiva morre. Ovo/comando não contam no limite. Configurável em `[spawning]` do config.
- **Agora tem som no "parar de seguir".**
- **Nunca desaparecem:** persistência garantida em todos os spawns (natural, ovo, comando) — não dependem só da etiqueta.
- Estruturas, "potion" e "wand" ficaram **fora do escopo** (nunca chegaram a entrar nesta versão).

### Novidades da Milestone 6
- **Sons agora tocam:** os `.ogg` estavam a 192 kHz (o Minecraft não toca); reamostrados pra 44.1 kHz. Agora: toca som em "definir casa" e "ir pra casa"; Em "parar de seguir", "definir casa" e "ir pra casa".
- **Velocidade separada andar/correr:** config tem `movementSpeed` (andando) e `runSpeed` (correndo). Padrões novos: 0.25 andando, 0.4 correndo.
- **Config aceita decimais:** use ponto, ex.: `movementSpeed = 0.25` (não vírgula).

### Novidades da Milestone 5
- **Som + mensagem nas ações do menu:** ao clicar Seguir / Parar / Definir casa / Ir pra casa, ela fala no chat (texto do mod original) e toca uma voz (giggle/happyoh/sigh/sadoh).
- **Animação de ataque:** toca (attack0/1/2, aleatória) quando ela acerta um golpe.
- **Animação de correr:** toca quando ela persegue um alvo ou quando segue um jogador que está correndo (sprint).
- **Agachar:** toca a animação de agachar quando ela passa por um espaço de 2 blocos de altura.

Pendente (separado, por ser complexo): **montar na garupa** quando o jogador anda de cavalo/barco.

### Novidades da Milestone 4
- **Config de atributos:** arquivo (gerado no 1º run) com HP, dano, velocidade e alcance de cada uma — edite sem mexer no `.jar`.
- **Para e encara ao clicar:** clique direito → ela para e vira de frente pra você (~5s, aguardando comando), exceto se estiver em combate.
- **Etiqueta fixa:** nome sempre visível em qualquer spawn; name tag não renomeia (só abre o menu).

### Novidades da Milestone 3
- **Ossos escondidos por padrão:** o modelo original traz ossos que o renderizador do mod controlava manualmente. Agora são escondidos: o boneco masculino (`steve`) que aparecia na frente, a "armadura de diamante" cosmética (ossos `armor*`), os itens (`items`). Quando implementarmos equipamento, os ossos de armadura voltam a aparecer de forma condicional.

### Novidades da Milestone 2
- **Correção de modelo/textura:** os geo declaravam `texture_width=64` mas a textura é 4096×4096. Ajustado (UVs reescalados) para o GeckoLib 4 mapear certo.
- **Sempre "domesticadas" por padrão:** não precisa mais de item pra domesticar. A mecânica de item continua no código, porém **desativada** (`CompanionEntity.TAMING_ITEM_ENABLED = false`) para uso futuro.
- **Menu de opções (clique direito):** abre uma tela com botões à esquerda — **Seguir / Parar de seguir / Definir nova casa / Ir para casa** (as ações NSFW que ficavam à direita no original foram removidas).

### Base (Milestone 1)
- Entidades, render via **GeckoLib 4**, **spawn eggs** + aba criativa, IA de companheira (seguir, voltar pra casa, atacar quem ataca o dono).

### Ainda NÃO incluso (próximas milestones)
- **Inventário/equipamento** (armadura e armas) — o botão será adicionado depois
- Combate com arco, "downed"/reviver, ride, throw pearl
- Hug/dash, emotes, sentar
- Sons, estruturas (casas), spawn natural, potion

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
