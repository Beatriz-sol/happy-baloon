package br.mackenzie.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import java.util.EnumMap;

/**
 * Efeitos sonoros do jogo. A opção de som ligado ou desligado fica salva entre as partidas.
 */
public class SoundEffects implements Disposable {
    public enum Effect {
        COIN("moeda.wav"),
        BIRD("passaro.wav"),
        COUNTDOWN("contagem.wav"),
        GO("largada.wav"),
        FINISH("fim.wav"),
        STAR("estrela.wav");

        private final String file;

        Effect(String file) {
            this.file = file;
        }
    }

    private static final float VOLUME = 0.6f;

    private final EnumMap<Effect, Sound> sounds = new EnumMap<>(Effect.class);
    private final Preferences preferences;
    private boolean enabled;

    public SoundEffects() {
        preferences = Gdx.app.getPreferences("jogo-do-balao");
        enabled = preferences.getBoolean("som", true);
        for (Effect effect : Effect.values()) {
            sounds.put(effect, Gdx.audio.newSound(Gdx.files.internal("sons/" + effect.file)));
        }
    }

    public void play(Effect effect) {
        if (enabled) sounds.get(effect).play(VOLUME);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        preferences.putBoolean("som", enabled);
        preferences.flush();
    }

    @Override
    public void dispose() {
        for (Sound sound : sounds.values()) sound.dispose();
    }
}
