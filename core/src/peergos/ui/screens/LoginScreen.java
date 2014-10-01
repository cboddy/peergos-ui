package peergos.ui.screens;

import peergos.corenode.AbstractCoreNode;
import peergos.corenode.HTTPCoreNode;
import peergos.crypto.User;
import peergos.ui.Start;
import peergos.user.DHTUserAPI;
import peergos.user.HttpsUserAPI;
import peergos.user.UserContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;


public class LoginScreen extends AbstractLoginScreen {

    public LoginScreen(final Start app) {
        super(app);
    }

    public UserContext buildUserContext(String username, String password, URL coreNodeUrl, URL dhtUrl) throws IOException
    {
        InetSocketAddress dhtAddress = new InetSocketAddress(InetAddress.getByName(dhtUrl.getHost()), dhtUrl.getPort());
        AbstractCoreNode coreNode = new HTTPCoreNode(coreNodeUrl);
        User user = User.generateUserCredentials(username, password);
        DHTUserAPI dht = new HttpsUserAPI(dhtAddress);
        return new UserContext(username, user, dht, coreNode);

    }
}
