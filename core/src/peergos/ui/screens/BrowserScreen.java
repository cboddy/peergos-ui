package peergos.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import peergos.crypto.SymmetricKey;
import peergos.crypto.SymmetricLocationLink;
import peergos.crypto.User;
import peergos.ui.BaseScreen;
import peergos.ui.Start;
import peergos.ui.utils.Styles;
import peergos.user.UserContext;
import peergos.user.fs.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

public class BrowserScreen extends BaseScreen {

    class FileView {
        final String name;
        final long size;
        final FileView parent;
        FileView(String name, long size, FileView parent) {
            this.name = name;
            this.size = size;
            this.parent = parent;
        }

        Actor toActor() {
            Table table = new Table();

            table.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    viewStack.add(FileView.this);
                    updateFileViews(FileView.this);
                }
            });

            Label nameLabel = app.labelBuilder(name);
            String sizeText = Styles.Size.MB.format(size);
            Label sizeLabel = app.labelBuilder(sizeText);
            TextButton downloadButton = new TextButton("download", app.skin);

            table.add(nameLabel);
            table.add(sizeLabel);
            table.add(downloadButton);

            return table;
        }

        @Override public boolean equals(Object o) {
            if (! (o instanceof  FileView))
                return false;
            FileView that = (FileView) o;
            return this.name.equals(that.name) && this.size == that.size;
        }
        @Override public int hashCode() {
            return (int) size + name.hashCode();
        }
    }

    class GetChildViewsTask implements Callable<FileView[]> {
        public final FileView parent;
        GetChildViewsTask(FileView parent) {
            this.parent = parent;
        }
        @Override public FileView[] call() throws IOException {
            List<FileView> views = new ArrayList<FileView>();

            if (parent == null) {
//                if (userContext.getRootFiles().isEmpty())
//                    System.out.println("No root files for user ");
//                for (Map.Entry<String, FileWrapper> entry : userContext.getRootFiles().entrySet()) {
//                    System.out.println("HERE !!!!");
//                    String sharer = entry.getKey();
//                    FileWrapper wrapper = entry.getValue();
//                    views.add(new FileView(wrapper, parent, sharer));
//                }
                Map<UserContext.StaticDataElement, DirAccess> roots = userContext.getRoots();
                for (UserContext.StaticDataElement dirPointer : roots.keySet()) {
                    SymmetricKey rootDirKey = ((UserContext.SharedRootDir) dirPointer).rootDirKey;
                    DirAccess dir = roots.get(dirPointer);
                    try {
                        Map<SymmetricLocationLink, Metadata> files = userContext.retrieveMetadata(dir.getFiles(), rootDirKey);
                        for (SymmetricLocationLink fileLoc : files.keySet()) {
                            FileAccess fileBlob = (FileAccess) files.get(fileLoc);
                            FileProperties fileProps = fileBlob.getProps(fileLoc.target(rootDirKey));
                            System.out.println("Adding file "+ fileProps.name +" to view.");
                            views.add(new FileView(fileProps.name, fileProps.getSize(), parent));

                        }
                    } catch (IOException e) {
                        System.err.println("Couldn't get File metadata!");
                        throw new IllegalStateException(e);
                    }
                }
            }

            return views.toArray(new FileView[0]);
        }
    }

    private final UserContext userContext;
    private final Deque<FileView> viewStack = new ArrayDeque<FileView>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private final Table filesViewContainer;

    public BrowserScreen(final Start app, UserContext userContext) {
        super(app);
        this.userContext = userContext;
        this.filesViewContainer = new Table();

        mainTable.defaults().pad(6f);
        mainTable.setBackground(app.skin.getDrawable("window1"));
        mainTable.setColor(app.skin.getColor("lt-blue"));

        mainTable.add(header());
        mainTable.row();
        mainTable.add(filesViewContainer);

        updateFileViews(null);
    }

    public Actor header() {
        Table table = new Table();
        final TextButton shareButton = new TextButton("Add File", app.skin);
        //TODO: implement this
        final TextButton searchButton = new TextButton("Search", app.skin);
        //TODO: implement this
        table.add(shareButton);
        table.add(searchButton);
        return table;
    }

    public Actor generateFileViewsTable(FileView[] views) {

        Table table = new Table();

        for (FileView view: views) {
            table.add(view.toActor());
            table.row();
        }

        ScrollPane scrollPane = new ScrollPane(table);
        return scrollPane;
    }


    public void onBackPress() {
        if (viewStack.isEmpty())
            return;

        viewStack.pollLast();

        FileView view = null;
        if (! viewStack.isEmpty())
            view  = viewStack.getLast();

        updateFileViews(view);
    }

    public void updateFileViews(FileView view) {
        filesViewContainer.clear();

        Future<FileView[]> future = threadPool.submit(new GetChildViewsTask(view));
        FileView[] children = null;

        //TODO: add  some sort of dialog spinner thing here
        while (!future.isDone())
            try {
                children = future.get();
            } catch (Throwable t) {
                t.printStackTrace();
            }

        if (children != null)
            filesViewContainer.add(generateFileViewsTable(children));
        else {
            //TODO: add some sort of dialog error message
        }
    }
}
