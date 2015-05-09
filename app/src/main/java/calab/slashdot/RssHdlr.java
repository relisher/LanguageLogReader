package calab.slashdot;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class RssHdlr extends DefaultHandler
{
    Boolean isTitle = false;
    Boolean isSummary = false;
    Boolean isItem = false;
    Boolean isLink = false;
    Boolean isDate = false;
    Vector<Story> stories = new Vector<Story>();
    String cur_title, cur_summary, cur_link;
    String cur_date;

    public RssHdlr ()
    {
        super();
    }
    public void startDocument ()
    {
        isTitle = false;
        isSummary = false;
        isItem = false;
    }

    public void characters(char[] ch, int start, int length)
    {
        if (isItem)
        {
            if (isTitle)
            {
                cur_title = new String(ch, start, length);
                cur_summary = null;
            }
            if (isSummary)
            {
                if (cur_summary == null)
                {
                    cur_summary = new String(ch, start, length);
                }
                else
                {
                    cur_summary += new String(ch, start, length);
                }
            }
            if (isLink)
            {
                cur_link = new String(ch, start, length);
            }
            if (isDate)
            {
                cur_date = new String(ch, start, length);
            }
        }
    }

    public void endElement (String uri, String name, String qName)
    {
        if ("title".equals(name))
        {
            isTitle = false;
        }
        if ("description".equals(name))
        {
            isSummary = false;
        }
        if ("link".equals(name))
        {
            isLink = false;
        }
        if ("date".equals(name))
        {
            isDate = false;
        }
        if ("item".equals(name))
        {
            isItem = false;
            Story cur_story = new Story();
            cur_story.title = cur_title;
            cur_story.summary = cur_summary;
            cur_story.link = cur_link;
            try
            {
                java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00");
                java.util.Date date = df.parse(cur_date);
                Date today = new Date();

            }
            catch( java.text.ParseException e )
            {
                Log.i("SD", "Parse Error");
            }
            stories.add(cur_story);
        }

    }

    public void startElement (String uri, String name,
            String qName, Attributes atts)
    {
        if ("title".equals(name))
            isTitle = true;
        if ("description".equals(name))
            isSummary =true;
        if ("item".equals(name))
            isItem = true;
        if ("link".equals(name))
            isLink = true;
        if ("date".equals(name))
            isDate = true;
    }
}
