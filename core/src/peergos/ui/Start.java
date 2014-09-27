package peergos.ui;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import peergos.ui.screens.LoginScreen;
import peergos.ui.utils.Styles;

/**
 * an ApplicationListener that is similar to gdx.Game but has screen transitions and built-in boilerplate code for a few
 * things that I use in pretty much every libgdx app
 *
 * @author trey miller
 */
public class Start implements ApplicationListener {
    public Stage stage;
    public TextureAtlas atlas;
    public Skin skin;

    /** add to this to have other inputs if you want */
    public static InputMultiplexer inputs = new InputMultiplexer();

    /** the width and height of the screen */
    public float w, h;

    private BaseScreen currentScreen, nextScreen;

    /** the duration of the screen transitions */
    public float defaultDur = .333f;
    private float durAccum = -420f;

    /** the color of glClearColor */

    protected String atlasPath() {
        return "data/tex.atlas";
    }

    protected String skinPath() {
        return null;
    }

    protected void styleSkin(Skin skin, TextureAtlas atlas) {
        new Styles().styleSkin(skin, atlas);
    }

    protected BaseScreen getFirstScreen() {
        return new LoginScreen(this);
    }

    @Override
    public void create() {
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();

        stage = new Stage(new StretchViewport(w, h));
        atlas = new TextureAtlas(atlasPath());
        skin = new Skin();
        skin.addRegions(atlas);
        String skinPath = skinPath();

        System.out.println("Using skin path "+ skinPath);


        if (skinPath != null)
            skin.load(Gdx.files.internal(skinPath));
        styleSkin(skin, atlas);

        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(true);
        stage.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
                    currentScreen.onBackPress();
                }
                return false;
            }
        });
        currentScreen = getFirstScreen().show();
        stage.addActor(currentScreen);
        Gdx.input.setInputProcessor(inputs);
        inputs.addProcessor(stage);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (durAccum > 0f) {
            durAccum -= delta;
            if (durAccum <= 0f) {
                currentScreen.hide();
                currentScreen.remove();
                currentScreen = nextScreen;
                currentScreen.setTouchable(Touchable.enabled);
                currentScreen.setPosition(0f, 0f);
                nextScreen = null;
            }
        }
        Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    /** glClearColor is called every frame and uses this color */
    public void setClearColor(Color color) {
        clearColor.set(color);
    }

    /** like Game#setScreen(Screen) but includes a screen transition */
    public void switchScreens(BaseScreen screen) {
        durAccum = currentScreen.duration();
        nextScreen = screen;
        nextScreen.setTouchable(Touchable.disabled);
        nextScreen.show();
        stage.addActor(nextScreen);
        if (currentScreen != null) {
            currentScreen.screenOut();
            currentScreen.setTouchable(Touchable.disabled);
            currentScreen.toFront();
        }
    }

    @Override
    public void resize(int width, int height) {
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        stage.getViewport().setWorldSize(w, h);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        atlas.dispose();
    }

    private static final Color clearColor = new Color(Color.BLACK);
}
