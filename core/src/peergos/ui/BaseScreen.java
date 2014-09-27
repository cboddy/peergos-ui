package peergos.ui;

/**
 * Created by chris on 9/27/14.
 */
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import peergos.ui.screens.LoginScreen;
import peergos.ui.utils.Styles;


public abstract class BaseScreen extends Group {

    protected final Start app;

    /** a table that covers the whole screen by default */
    protected final Table mainTable = new Table();


    /** the duration of the screen transition for the screenOut method */
    protected float duration, defaultPad;

    public BaseScreen(Start app) {
        this.app = app;
        this.duration = app.defaultDur;
        defaultPad = Math.round(Math.max(app.h, app.w) * .02f);
        mainTable.defaults().pad(defaultPad);
        mainTable.setSize(app.w, app.h);
        this.addActor(mainTable);
    }

    /** override if you want, good for if you keep your screens around instead of creating new ones each time */
    public BaseScreen show() {
        return this;
    }

    /** override for custom screen transitions, otherwise current screen just slides to the left */
    protected void screenOut() {
        float xPos = -app.w;
        MoveToAction action = Actions.moveTo(xPos, 0f, duration);
        addAction(action);
    }

    /** what happens when the back button is pressed on Android */
    public abstract void onBackPress();

    public float duration(){return duration;}

    public float defaultPad(){return defaultPad;}

    public void hide() {
    }	

	protected BaseScreen getFirstScreen() {
        return new LoginScreen(app);
	}

    protected Label labelBuilder(String text)
    {
        return labelBuilder(text, Color.WHITE);
    }
    protected Label labelBuilder(String text, Color color) {
        Label label = new Label(text, app.skin);
        label.setAlignment(Align.center, Align.center);
        label.setColor(color);
        return label;
    }

    protected Dialog dialogBuilder(String text) {
        return dialogBuilder(text, "OK");
    }

    protected Dialog dialogBuilder(String text, String buttonText) {

        Label dialogLabel = labelBuilder(text);
        dialogLabel.setWrap(true);

        Dialog dialog = new Dialog("", app.skin);

        dialog.padTop(10).padBottom(10);
        dialog.getContentTable().add(dialogLabel).width((int) (app.w * 0.8)).row();
        dialog.getButtonTable().padTop(50);
        dialog.button(buttonText);

        dialog.invalidateHierarchy();
        dialog.invalidate();
        dialog.layout();
        return dialog;
    }
}
