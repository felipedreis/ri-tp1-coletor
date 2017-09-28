package crawler;

import com.trigonic.jrobotx.Record;
import com.trigonic.jrobotx.RobotExclusion;
import crawler.escalonadorCurtoPrazo.Escalonador;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by felipe on 27/09/17.
 */
public class PageFetcher implements Runnable{

    private Escalonador escalonador;

    public PageFetcher(Escalonador escalonador) {
        this.escalonador = escalonador;
    }

    @Override
    public void run() {
        URLAddress url = escalonador.getURL();
        boolean noIndex = false, noFollow = false;
        try {
            if(url != null) {
                Record robots = escalonador.getRecordAllowRobots(url);

                if (robots == null) {
                    RobotExclusion robotExclusion = new RobotExclusion();
                    robots = robotExclusion.get(new URL(url.getAddress()), "noindex");
                    escalonador.putRecorded(url.getDomain(), robots);
                }

                if (robots != null || robots.allows(url.getPath())) {
                    InputStream urlStream;
                    urlStream = ColetorUtil.getUrlStream("", url.toJavaURL());
                    HtmlCleaner cleaner = new HtmlCleaner();
                    TagNode node = cleaner.clean(urlStream);
                    TagNode [] metaTags = node.getElementsByAttValue("name", "robots", true, false);

                    for (TagNode metaTag : metaTags) {
                        if (metaTag.getAttributeByName("content").contains("nofollow"))
                            noFollow = true;
                        if (metaTag.getAttributeByName("content").contains("noindex"))
                            noIndex = true;
                    }

                    if (!noIndex && !noFollow) {
                        TagNode [] links = node.getElementsByName("a", true);

                        for (TagNode link : links) {
                            String href = link.getAttributeByName("href");
                            URLAddress address = new URLAddress(href, url.getDepth() + 1);
                            escalonador.adicionaNovaPagina(address);
                        }
                    }
                }
            }

        } catch (Exception ex) {

        }
    }
}
