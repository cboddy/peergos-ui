package peergos.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import peergos.ui.BaseScreen;
import peergos.ui.Start;
import peergos.user.UserContext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public abstract class AbstractLoginScreen extends BaseScreen {

    public AbstractLoginScreen(final Start app) {
        super(app);

        final TextField coreNodeField = new TextField("http://localhost:8080", app.skin);
        final TextField dhtField = new TextField("https://localhost:8081", app.skin);
        final TextField usernameField = new TextField("", app.skin);
        final TextField passwordField = new TextField("", app.skin);

        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter(PASSWORD_CHAR);


        final TextButton loginButton = new TextButton("Login", app.skin);
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                String user = usernameField.getText();
                String passwd = passwordField.getText();

                if (isEmpty(user)) {
                    Dialog dialog = app.dialogBuilder("Please enter a username");
                    dialog.show(app.stage);
                    loginButton.setChecked(false);
                    return;
                }
                else if (isEmpty(passwd)) {
                    app.dialogBuilder("Please enter a password").show(app.stage);
                    loginButton.setChecked(false);
                    return;
                }


                URL coreNodeUrl = null, dhtUrl = null;

                String coreNodeAddress = coreNodeField.getText();
                try {
                    coreNodeUrl = new URL(coreNodeAddress);
                } catch (MalformedURLException me) {
                    me.printStackTrace();
                    app.dialogBuilder("Invalid coreNode URL: " + coreNodeAddress).show(app.stage);
                    loginButton.setChecked(false);
                    return;
                }

                String dhtAddress = dhtField.getText();
                try {
                    dhtUrl = new URL(dhtAddress);
                } catch (MalformedURLException me) {
                    me.printStackTrace();
                    app.dialogBuilder("Invalid DHT URL: " + dhtAddress).show(app.stage);
                    loginButton.setChecked(false);
                    return;
                }


                try {
//                    UserContext userContext = PeergosUtils.buildUserContext(user, passwd, coreNodeUrl, dhtSocketAddress);
                    UserContext userContext = buildUserContext(user, passwd, coreNodeUrl, dhtUrl);
                    loginButton.setChecked(false);
                    //TODO go to main UI screen
                } catch (IOException ioe) {
                    app.dialogBuilder("Connection Error").show(app.stage);
                    ioe.printStackTrace();
                }
            }


        });


        mainTable.defaults().pad(6f);
        mainTable.setBackground(app.skin.getDrawable("window1"));
        mainTable.setColor(app.skin.getColor("lt-blue"));

        Label titleLabel = app.labelBuilder("Peergos");
        Label usernameLabel = app.labelBuilder("Username: ");
        Label passwordLabel = app.labelBuilder("Password: ");
        Label coreNodeLabel = app.labelBuilder("coreNodeURL: ");
        Label dhtLabel = app.labelBuilder("DHT URL: ");

        mainTable.add(titleLabel);
        mainTable.row();
        mainTable.add(usernameLabel);
        mainTable.add(usernameField);
        mainTable.row();
        mainTable.add(passwordLabel);
        mainTable.add(passwordField);
        mainTable.row();
        mainTable.add(coreNodeLabel);
        mainTable.add(coreNodeField);
        mainTable.row();
        mainTable.add(dhtLabel);
        mainTable.add(dhtField);
        mainTable.row();
        mainTable.add(loginButton);
    }

    @Override
    public void onBackPress() {
        Gdx.app.exit();
    }

    private static final char PASSWORD_CHAR = "*".charAt(0);
    private static boolean isEmpty(String s) {return s.equals("");}

    public abstract UserContext buildUserContext(String user, String password, URL coreNodeUrl, URL dhtUrl) throws IOException;

}
