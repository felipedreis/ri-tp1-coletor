package crawler;

import com.trigonic.jrobotx.Constants;
import com.trigonic.jrobotx.Record;
import com.trigonic.jrobotx.RobotExclusion;
import crawler.escalonadorCurtoPrazo.Escalonador;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
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
        System.out.println("Got url " + url.getAddress());
        boolean noIndex = false, noFollow = false;
        try {
            Record robots = getRobots(url);

            if (robots != null || robots.allows(url.getPath())) {
                InputStream urlStream;
                urlStream = ColetorUtil.getUrlStream(Constants.USER_AGENT, url.toJavaURL());
                String html = ColetorUtil.consumeStream(urlStream);

                HtmlCleaner cleaner = new HtmlCleaner();
                TagNode node = cleaner.clean(html);
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
                        if(href != null) {
                            if(href.startsWith("/"))
                                href = url.getAddress() + href;
                            URLAddress address = new URLAddress(href, url.getDepth() + 1);
                            escalonador.adicionaNovaPagina(address);
                        }
                    }
                    escalonador.countFetchedPage();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Record getRobots(URLAddress url) throws MalformedURLException {
        Record robots = escalonador.getRecordAllowRobots(url);

        if (robots == null) {
            RobotExclusion robotExclusion = new RobotExclusion();
            robots = robotExclusion.get(new URL(url.getAddress()), Constants.USER_AGENT);
            escalonador.putRecorded(url.getDomain(), robots);
        }

        return robots;
    }
}
