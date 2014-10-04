package peergos.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import peergos.ui.BaseScreen;
import peergos.ui.Start;
import peergos.ui.utils.Styles;
import peergos.user.UserContext;
import peergos.user.fs.FileWrapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BrowserScreen extends BaseScreen {

    class FileView {
        final FileWrapper fileWrapper;
        final String sharer;
        final FileView parent;
        FileView(FileWrapper wrapper, FileView parent) {
            this(wrapper, parent, parent.sharer);
        }
        FileView(FileWrapper wrapper, FileView parent, String sharer)
        {
            this.fileWrapper = wrapper;
            this.sharer = sharer;
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

            Label nameLabel = app.labelBuilder(fileWrapper.props().name);
            String sizeText = Styles.Size.MB.format(fileWrapper.props().getSize());
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
            FileView v = (FileView) o;
            return v.sharer.equals(sharer) && fileWrapper.equals(v.fileWrapper) && parent.equals(v.parent);
        }
        @Override public int hashCode() {
            return sharer.hashCode() + fileWrapper.hashCode() * 31 + parent.hashCode() * 31 * 31;
        }
    }

    class GetChildViewsTask implements Callable<FileView[]> {
        public final FileView parent;
        GetChildViewsTask(FileView parent) {
            this.parent = parent;
        }
        @Override public FileView[] call() throws IOException {
            List<FileView> views = new ArrayList<FileView>();

            if (parent == null)
                for (Map.Entry<String, FileWrapper> entry: userContext.getRootFiles().entrySet())
                {
                    String sharer = entry.getKey();
                    FileWrapper wrapper = entry.getValue();
                    views.add(new FileView(wrapper, parent, sharer));
                }
            else
                for (FileWrapper wrapper: parent.fileWrapper.getChildren())
                    views.add(new FileView(wrapper, parent));

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
        while (! future.isDone())
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
