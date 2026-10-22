package net.jennyellie;

import net.minecraftforge.common.ForgeConfigSpec;

/** Configuração editável em config/jennyellie-common.toml (HP, dano, velocidade, alcance). */
public class Config {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue JENNY_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue JENNY_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue JENNY_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue JENNY_RUN_SPEED;
    public static final ForgeConfigSpec.DoubleValue JENNY_FOLLOW_RANGE;

    public static final ForgeConfigSpec.DoubleValue ELLIE_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue ELLIE_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ELLIE_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue ELLIE_RUN_SPEED;
    public static final ForgeConfigSpec.DoubleValue ELLIE_FOLLOW_RANGE;

    public static final ForgeConfigSpec.BooleanValue NATURAL_SPAWN_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue SPAWN_SEARCH_RADIUS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Spawn natural perto de aldeões (limite de 1 Jenny e 1 Ellie por mundo).").push("spawning");
        NATURAL_SPAWN_ENABLED = b.comment("Ligar/desligar o spawn natural perto de aldeões.")
                .define("naturalSpawnEnabled", true);
        SPAWN_CHANCE = b.comment("Chance por verificação (~a cada 10s) de spawnar, quando há aldeão por perto e a vaga está livre.")
                .defineInRange("spawnChance", 0.15, 0.0, 1.0);
        SPAWN_SEARCH_RADIUS = b.comment("Raio (blocos) em volta dos jogadores para procurar aldeões.")
                .defineInRange("searchRadius", 48, 8, 128);
        b.pop();

        b.comment("Atributos da Jenny").push("jenny");
        JENNY_MAX_HEALTH = b.comment("HP máximo (1 coração = 2).")
                .defineInRange("maxHealth", 20.0, 1.0, 1024.0);
        JENNY_ATTACK_DAMAGE = b.comment("Dano de ataque.")
                .defineInRange("attackDamage", 3.0, 0.0, 1024.0);
        JENNY_MOVEMENT_SPEED = b.comment("Velocidade ANDANDO (aceita decimais, ex.: 0.25). Jogador ~0.1.")
                .defineInRange("movementSpeed", 0.25, 0.0, 2.0);
        JENNY_RUN_SPEED = b.comment("Velocidade CORRENDO (ao perseguir alvo ou seguir jogador em sprint).")
                .defineInRange("runSpeed", 0.4, 0.0, 2.0);
        JENNY_FOLLOW_RANGE = b.comment("Alcance de percepção / de alvo (blocos).")
                .defineInRange("followRange", 24.0, 1.0, 256.0);
        b.pop();

        b.comment("Atributos da Ellie").push("ellie");
        ELLIE_MAX_HEALTH = b.comment("HP máximo (1 coração = 2).")
                .defineInRange("maxHealth", 20.0, 1.0, 1024.0);
        ELLIE_ATTACK_DAMAGE = b.comment("Dano de ataque.")
                .defineInRange("attackDamage", 3.0, 0.0, 1024.0);
        ELLIE_MOVEMENT_SPEED = b.comment("Velocidade ANDANDO (aceita decimais, ex.: 0.25). Jogador ~0.1.")
                .defineInRange("movementSpeed", 0.25, 0.0, 2.0);
        ELLIE_RUN_SPEED = b.comment("Velocidade CORRENDO (ao perseguir alvo ou seguir jogador em sprint).")
                .defineInRange("runSpeed", 0.4, 0.0, 2.0);
        ELLIE_FOLLOW_RANGE = b.comment("Alcance de percepção / de alvo (blocos).")
                .defineInRange("followRange", 24.0, 1.0, 256.0);
        b.pop();

        SPEC = b.build();
    }
}
