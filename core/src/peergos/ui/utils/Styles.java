package peergos.ui.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;


public class Styles {

	public void styleSkin(Skin skin, TextureAtlas atlas) {
		BitmapFont font = new BitmapFont(Gdx.files.internal("data/lucida-console-21.fnt"), false);
		skin.add("default", font);

		skin.add("lt-blue", new Color(.6f, .8f, 1f, 1f));
		skin.add("lt-green", new Color(.6f, .9f, .6f, 1f));
		skin.add("dark-blue", new Color(.1f, .3f, 1f, 1f));

		NinePatchDrawable btn1up = new NinePatchDrawable(atlas.createPatch("patchThick"));
		NinePatchDrawable btn1down = new NinePatchDrawable(atlas.createPatch("patchThickDown"));
		NinePatch window1patch = atlas.createPatch("window1");
		skin.add("btn1up", btn1up);
		skin.add("btn1down", btn1down);
		skin.add("window1", window1patch);
		skin.add("white-pixel", atlas.findRegion("white-pixel"), TextureRegion.class);

		LabelStyle lbs = new LabelStyle();
		lbs.font = font;
		lbs.fontColor = Color.WHITE;
		skin.add("default", lbs);

		TextButtonStyle tbs = new TextButtonStyle(btn1up, btn1down, btn1down, font);
		tbs.fontColor = skin.getColor("dark-blue");
		tbs.pressedOffsetX = Math.round(1f * Gdx.graphics.getDensity());
		tbs.pressedOffsetY = tbs.pressedOffsetX * -1f;
		skin.add("default", tbs);


        TextField.TextFieldStyle tfs = new TextField.TextFieldStyle(font, tbs.fontColor, btn1up, btn1down, btn1up);
        skin.add("default", tfs);

        Window.WindowStyle windowStyle = new Window.WindowStyle(font, tbs.fontColor, btn1up);
        skin.add("default", windowStyle);

	}


    public String sizeString(long length) {
        return Double.toString((double) length / 1e6) + " MB";
    }

    public enum Size {
        MB(1e6f, "MB"), KB(1e6f, "KB");
        final float normalization;
        final String units;
        Size(float normalization, String units)
        {
            this.normalization = normalization;
            this.units = units;
        }

        public String format(long length) {
            float f =  length / normalization;
            return String.format("%.2f", f) + units;
        }
    }
}
