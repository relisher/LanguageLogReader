package calab.slashdot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Slashdot extends ListActivity implements OnKeyListener, OnGesturePerformedListener {
    long start, stop;
    private ProgressDialog m_ProgressDialog = null;
    private Runnable viewStories;
    private Runnable viewComment;
    private StoryAdapter m_adapter;
    private CommentAdapter c_adapter;
    private ArrayList<Story> m_stories = null;
    private ArrayList<String> m_comments = null;
    RssHdlr handler = new RssHdlr();
    private int m_pos;
    private GestureLibrary mLibrary;

    private class StoryAdapter extends ArrayAdapter<Story> {
        private ArrayList<Story> items;
        private int[] colors = new int[] { 0x30ffffff, 0x30808080 };

        public StoryAdapter(Context context, int textViewResourceId,
                ArrayList<Story> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
            }
            Story o = items.get(position);
            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);

            }

            int colorPos = position % colors.length;
            v.setBackgroundColor(colors[colorPos]);
            return v;
        }
    }
    private class CommentAdapter extends ArrayAdapter<String> {
        private ArrayList<String> items;
        private int[] colors = new int[] { 0x30ffffff, 0x30808080 };

        public CommentAdapter(Context context, int textViewResourceId,
                ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
            }
            String o = items.get(position);
            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                if (tt != null) {
                    tt.setText(Html.fromHtml(o));
                }
            }

            int colorPos = position % colors.length;
            v.setBackgroundColor(colors[colorPos]);
            return v;
        }
    }

    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            if (m_stories != null && m_stories.size() > 0) {
                m_adapter.clear();
                m_adapter.notifyDataSetChanged();
                for (int i = 0; i < m_stories.size(); i++)
                    m_adapter.add(m_stories.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
    };
    private Runnable returnCom = new Runnable() {

        @Override
        public void run() {
            if (m_comments != null && m_comments.size() > 0) {
                c_adapter.clear();
                c_adapter.notifyDataSetChanged();
                for (int i = 0; i < m_comments.size(); i++)
                    c_adapter.add(m_comments.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
    };

    private void getStories() {
        try {
            System.setProperty("org.xml.sax.driver",
                    "org.xmlpull.v1.sax2.Driver");
            // URL url = new URL("http://rss.slashdot.org/Slashdot/slashdot");
            // classic feed
            URL url = new URL("http://languagelog.ldc.upenn.edu/nll/?feed=rss2");
            InputStreamReader reader = new InputStreamReader(url.openStream());

            start = System.currentTimeMillis();

            XMLReader xr;
            xr = XMLReaderFactory.createXMLReader();

            handler = new RssHdlr();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            xr.parse(new InputSource(reader));

            m_stories = new ArrayList<Story>();

            for (Story story : handler.stories) {
                m_stories.add(story);
            }

            stop = System.currentTimeMillis();

            Log.i("ARRAY", "" + m_stories.size());
        } catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }
    private void getComments() {
        try {
            URL url = new URL(handler.stories.get(m_pos).link);

            long start = System.currentTimeMillis();
            Document doc = Jsoup.parse(url, 3 * 1000);
            long elapsed = System.currentTimeMillis() - start;
            Log.i("SD", "Jsoup Parse: " + elapsed + "ms");

            m_comments = new ArrayList<String>();

            start = System.currentTimeMillis();
            Elements comments = doc.select("div[id^=comment_3]");
            elapsed = System.currentTimeMillis() - start;
            Log.i("SD", "Jsoup Select: " + elapsed + "ms");

        } catch (Exception e) {
            Log.e("COMMENT_PROC", e.getMessage());
        }
        runOnUiThread(returnCom);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!mLibrary.load()) {
            finish();
        }

        // GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        // gestures.addOnGesturePerformedListener(this);

        m_stories = new ArrayList<Story>();
        this.m_adapter = new StoryAdapter(this, R.layout.row, m_stories);
        setListAdapter(this.m_adapter);

        m_comments = new ArrayList<String>();
        this.c_adapter = new CommentAdapter(this, R.layout.row, m_comments);
        ListView commentView = (ListView) findViewById(R.id.CommentView);
        commentView.setAdapter(this.c_adapter);
        commentView.setVisibility(View.INVISIBLE);

        viewComment = new Runnable() {
            @Override
            public void run() {
                getComments();
            }
        };

        viewStories = new Runnable() {
            @Override
            public void run() {
                getStories();
            }
        };

        Thread thread = new Thread(null, viewStories, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(Slashdot.this, "Please wait...",
                "Retrieving data ...", true);

        WebView webView = (WebView) findViewById(R.id.WebView);
        webView.setVisibility(View.INVISIBLE);

        /* setup key listener */
        webView.setOnKeyListener(this);
        commentView.setOnKeyListener(this);

        ListView lv = getListView();
        lv.setOnKeyListener(this);
        lv.setVisibility(View.VISIBLE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                onListItemClick(v, pos, id);
            }
        });
    }

    void LoadStory(int pos) {
        setTitle(handler.stories.get(pos).title);
        WebView webView = (WebView) findViewById(R.id.WebView);
        webView.loadDataWithBaseURL("http://slashdot.org", "<html><body>"
                + "<b>" + handler.stories.get(pos).title + "</b><br>"
                + "<HR>"
                + handler.stories.get(pos).summary + "</body></html>",
                "text/html", "utf-8", null);
        webView.setVisibility(View.VISIBLE);
    }

    protected void onListItemClick(View v, int pos, long id) {
        m_pos = pos;
        LoadStory(m_pos);
        ListView lv = getListView();
        lv.setVisibility(View.INVISIBLE);
    }

    @Override
    /* shared by WebView & ListView */
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            ListView lv = getListView();
            WebView webView = (WebView) findViewById(R.id.WebView);
            if (webView.getVisibility() == View.VISIBLE)
            {
                /* WebView */
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) // nextstory
                {
                    if (m_pos < handler.stories.size() - 1)
                        m_pos++;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) // prevstory
                {
                    if (m_pos > 0)
                        m_pos--;
                }
                setSelection(m_pos);

                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                        | keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    LoadStory(m_pos);
                        }

                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    showStoryList();
                }

                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    //jump to comment listview
                    webView.setVisibility(View.INVISIBLE);
                    ListView commentView = (ListView) findViewById(R.id.CommentView);
                    commentView.setVisibility(View.VISIBLE);

                    Thread thread = new Thread(null, viewComment, "MagentoBackground");
                    thread.start();
                    m_ProgressDialog = ProgressDialog.show(Slashdot.this, "Please wait...",
                            "Parsing Comments ...", true);
                }
            }else if (lv.getVisibility() == View.VISIBLE)
            {
                /* ListView */
                ListView commentView = (ListView) findViewById(R.id.CommentView);
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    Thread thread = new Thread(null, viewStories, "MagentoBackground");
                    thread.start();
                    m_ProgressDialog = ProgressDialog.show(Slashdot.this, "Please wait...",
                            "Retrieving data ...", true);
                }
                else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    Log.i("PROFILE", "" + (stop - start));
                    Toast.makeText(this, "Profile: " + (stop - start), Toast.LENGTH_SHORT).show();
                }
            }else
            {
                /* Comment View */
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    ListView commentView = (ListView) findViewById(R.id.CommentView);
                    commentView.setVisibility(View.INVISIBLE);
                    LoadStory(m_pos);
                }
            }
            return true; // key is handled, propagate no further
        }

    void showStoryList() {
        setTitle("Linguist List");
        // hide webview, show listview
        WebView webView = (WebView) findViewById(R.id.WebView);
        webView.setVisibility(View.INVISIBLE);
        ListView lv = getListView();
        lv.setVisibility(View.VISIBLE);
    }

    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

        WebView webView = (WebView) findViewById(R.id.WebView);
        ListView lv = getListView();
        if (webView.getVisibility() == View.VISIBLE)
        {
            //WebView

            if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
                String action = predictions.get(0).name;
                if ("liststory".equals(action)) {
                    Toast.makeText(this, "Story List", Toast.LENGTH_SHORT).show();
                    showStoryList();
                } else if ("nextstory".equals(action)) {
                    Toast.makeText(this, "Next Story", Toast.LENGTH_SHORT).show();
                    if (m_pos < handler.stories.size() - 1)
                        m_pos++;
                    setSelection(m_pos);
                    LoadStory(m_pos);
                } else if ("prevstory".equals(action)) {
                    Toast.makeText(this, "Prev Story", Toast.LENGTH_SHORT).show();
                    if (m_pos > 0)
                        m_pos--;
                    setSelection(m_pos);
                    LoadStory(m_pos);
                } else if ("comments".equals(action)) {
                    Toast.makeText(this, "Read Comments", Toast.LENGTH_SHORT).show();
                    //jump to comment listview
                    webView.setVisibility(View.INVISIBLE);
                    ListView commentView = (ListView) findViewById(R.id.CommentView);
                    commentView.setVisibility(View.VISIBLE);

                    Thread thread = new Thread(null, viewComment, "MagentoBackground");
                    thread.start();
                    m_ProgressDialog = ProgressDialog.show(Slashdot.this, "Please wait...",
                            "Parsing Comments ...", true);
                }
            }

        }
        else if (lv.getVisibility() == View.VISIBLE)
        {
            /* ListView */
            if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
                String action = predictions.get(0).name;
                if ("reload".equals(action)) {
                    Toast.makeText(this, "Reload", Toast.LENGTH_SHORT).show();
                    Thread thread = new Thread(null, viewStories, "MagentoBackground");
                    thread.start();
                    m_ProgressDialog = ProgressDialog.show(Slashdot.this, "Please wait...",
                            "Retrieving data ...", true);
                }
            }
        }
    }
}
