package br.mackenzie.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Skin simples criada por código, para não depender de arquivos de skin externos.
 */
public final class UiSkin {
    private UiSkin() {
    }

    public static Skin create() {
        Skin skin = new Skin();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.5f);
        skin.add("default", font);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        skin.add("title", titleFont);

        Drawable up = skin.newDrawable("white", new Color(0.18f, 0.45f, 0.75f, 1f));
        Drawable over = skin.newDrawable("white", new Color(0.25f, 0.55f, 0.88f, 1f));
        Drawable down = skin.newDrawable("white", new Color(0.12f, 0.32f, 0.55f, 1f));
        skin.add("panel", skin.newDrawable("white", new Color(0f, 0f, 0f, 0.7f)), Drawable.class);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(up, down, up, font);
        buttonStyle.over = over;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        skin.add("default", new Label.LabelStyle(font, Color.WHITE));
        skin.add("title", new Label.LabelStyle(titleFont, Color.WHITE));
        return skin;
    }
}
