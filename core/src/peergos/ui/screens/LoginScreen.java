package peergos.ui.screens;

import peergos.corenode.AbstractCoreNode;
import peergos.corenode.HTTPCoreNode;
import peergos.crypto.User;
import peergos.ui.Start;
import peergos.user.DHTUserAPI;
import peergos.user.MemoryDHTUserAPI;
import peergos.user.UserContext;
import peergos.util.ArrayOps;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;


public class LoginScreen extends AbstractLoginScreen {

    private static Map<String, File> KEYPAIR_CACHE = new HashMap<String, File>();
    static {
        File chris = new File("cache.chris.key");
        File ian = new File("cache.ian.key");
        try {
            if (! chris.exists())
                User.KeyPairUtils.serialize(User.generateKeyPair(), chris);
            if (! ian.exists())
                User.KeyPairUtils.serialize(User.generateKeyPair(), ian);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        KEYPAIR_CACHE.put("chris", chris);
        KEYPAIR_CACHE.put("ian", ian);
    }
    public LoginScreen(final Start app) {
        super(app);
    }

    public UserContext buildUserContext(String username, String password, URL coreNodeUrl, URL dhtUrl) throws IOException {
        InetSocketAddress dhtAddress = new InetSocketAddress(InetAddress.getByName(dhtUrl.getHost()), dhtUrl.getPort());
//        AbstractCoreNode coreNode = new HTTPCoreNode(coreNodeUrl);
        AbstractCoreNode coreNode = AbstractCoreNode.getDefault();
        DHTUserAPI dht = new MemoryDHTUserAPI();


//        User user = User.generateUserCredentials(username, password);
        User user = new User(User.KeyPairUtils.deserialize(KEYPAIR_CACHE.get("chris")));
        UserContext context = new UserContext(username, user, dht, coreNode);

        String receiverName = "chris";
//        User receiverUser = User.generateUserCredentials(receiverName, "blah");
        User receiverUser = new User(User.KeyPairUtils.deserialize(KEYPAIR_CACHE.get("ian")));
        UserContext receiverContext = new UserContext(receiverName, receiverUser, dht, coreNode);

        System.out.println("Username "+ username + ", receiver name "+ receiverName);

        if (!context.isRegistered())
            context.register();
        if (!receiverContext.isRegistered())
            receiverContext.register();


        receiverContext.sendFollowRequest(username);
        UserContext.SharedRootDir root = context.decodeFollowRequest(context.getFollowRequests().get(0));

        User sharer = new User(root.priv, root.pub);

//        int frags = 120;
//        for (int i = 0; i < frags; i++) {
//            byte[] frag = ArrayOps.random(32);
//            byte[] message = ArrayOps.concat(sharer.getPublicKey(), frag);
//            byte[] signature = sharer.hashAndSignMessage(message);
//            if (! coreNode.registerFragmentStorage(receiverName,
//                    new InetSocketAddress("localhost", 666),
//                    receiverName,
//                    sharer.getPublicKey(),
//                    frag,
//                    signature)) {
//                System.out.println("Failed to register fragment storage!");
//            }
//        }
//        long quota = coreNode.getQuota(receiverName);
//        System.out.println("Generated quota: " + quota/1024 + " KiB");
        UserContext.Test test = new UserContext.Test();
        test.mediumFileTest(receiverName, sharer, root.priv, receiverContext, context);
        return context;
    }
}
