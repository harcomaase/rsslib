package de.hm.rsslib;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FeedReaderTest {

    private final FeedReader instance;
    private final HttpClient httpClientMock;

    public FeedReaderTest() throws Exception {
        httpClientMock = mock(HttpClient.class);
        this.instance = new FeedReader(httpClientMock);
    }

    @Test
    public void testReadFeed() throws Exception {
        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponseMock);

        when(httpResponseMock.statusCode()).thenReturn(
                503, 404, 400, 200
        );

        when(httpResponseMock.body()).thenReturn(
                inputStreamToString(getClass().getClassLoader().getResourceAsStream("test-atom.xml")),
                inputStreamToString(getClass().getClassLoader().getResourceAsStream("test-rss.xml"))
        );

        RssException rssException = assertThrows(RssException.class, () -> instance.readFeed("http://doesnt-matter"));
        assertEquals("unsuccessful server response: 503", rssException.getMessage());
        rssException = assertThrows(RssException.class, () -> instance.readFeed("http://doesnt-matter"));
        assertEquals("unsuccessful server response: 404", rssException.getMessage());
        rssException = assertThrows(RssException.class, () -> instance.readFeed("http://doesnt-matter"));
        assertEquals("unsuccessful server response: 400", rssException.getMessage());

        Feed feed = instance.readFeed("http://still-doesnt-matter");
        assertNotNull(feed);
        assertEquals("test-atom feed", feed.getTitle());
        assertEquals(2, feed.getEntries().size());
        assertFeedEquals(feed.getEntries().get(0), "Entry 1: interesting news", "the id of the first entry", "the link to the first entry as href", "The summary of the first entry", "<a href=\"the link to the first entry\"><img src=\"an image to the first entry\" alt=\"the alt text for the image of the first entry\" /></a><p>Some content of the first entry</p>");
        assertFeedEquals(feed.getEntries().get(1), "Entry 2: older interesting news", "the id of the second entry", "the link to the second entry as href", "The summary of the second entry", "<a href=\"the link to the second entry\"><img src=\"an image to the second entry\" alt=\"the alt text for the image of the second entry\" /></a><p>Some content of the second entry</p>");

        feed = instance.readFeed("http://still-doesnt-matter");
        assertNotNull(feed);
        assertEquals("rss-test channel", feed.getTitle());
        assertEquals(2, feed.getEntries().size());
        assertFeedEquals(feed.getEntries().get(0), "This is the title of the first entry. It's rather short", "the link to the first entry", "the link to the first entry", null, null);
        assertFeedEquals(feed.getEntries().get(1), "This is the title of the second entry. It's rather long: Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. At elementum eu facilisis sed odio morbi quis commodo odio. Et netus et malesuada fames ac turpis egestas. Dictumst vestibulum rhoncus est pellentesque elit ullamcorper dignissim cras tincidunt. Morbi tincidunt ornare massa eget egestas purus viverra accumsan. Donec ultrices tincidunt arcu non. Facilisis mauris sit amet massa vitae tortor condimentum lacinia quis. Nisl nisi scelerisque eu ultrices vitae. Rutrum tellus pellentesque eu tincidunt tortor aliquam nulla. Elit ut aliquam purus sit amet. Urna et pharetra pharetra massa massa ultricies mi quis. Sed velit dignissim sodales ut eu sem integer vitae. Neque ornare aenean euismod elementum nisi quis. Tellus elementum sagittis vitae et leo duis ut diam. Et ultrices neque ornare aenean euismod elementum. Odio ut sem nulla pharetra. Malesuada fames ac turpis egestas. Iaculis urna id volutpat lacus. Diam sollicitudin tempor id eu nisl nunc mi ipsum. Ultricies lacus sed turpis tincidunt id aliquet risus feugiat. Pharetra massa massa ultricies mi quis hendrerit dolor. Integer vitae justo eget magna fermentum iaculis eu non diam. Faucibus vitae aliquet nec ullamcorper sit. Enim praesent elementum facilisis leo vel fringilla est ullamcorper eget. Adipiscing bibendum est ultricies integer quis auctor elit sed. In nulla posuere sollicitudin aliquam ultrices. Sapien nec sagittis aliquam malesuada bibendum arcu vitae. In nibh mauris cursus mattis. Turpis egestas pretium aenean pharetra magna ac placerat. Nibh tellus molestie nunc non blandit massa enim nec dui. Adipiscing tristique risus nec feugiat in fermentum posuere urna nec. Pharetra diam sit amet nisl suscipit adipiscing bibendum est ultricies. Nec sagittis aliquam malesuada bibendum arcu vitae. Ut tellus elementum sagittis vitae et leo duis ut diam. Molestie nunc non blandit massa enim nec dui. Morbi tempus iaculis urna id volutpat lacus laoreet non. Arcu risus quis varius quam quisque. Congue quisque egestas diam in. Lectus nulla at volutpat diam ut venenatis tellus in metus. Eget est lorem ipsum dolor sit amet consectetur adipiscing. Accumsan sit amet nulla facilisi morbi tempus. Eget dolor morbi non arcu risus quis. Ornare aenean euismod elementum nisi quis. Sed risus pretium quam vulputate dignissim suspendisse in est ante. Pellentesque pulvinar pellentesque habitant morbi. Convallis aenean et tortor at risus viverra adipiscing. Sed faucibus turpis in eu. Sed arcu non odio euismod lacinia at quis risus sed. Nisl condimentum id venenatis a condimentum vitae sapien pellentesque habitant. Mattis rhoncus urna neque viverra justo nec ultrices. Consectetur libero id faucibus nisl tincidunt eget. Tellus pellentesque eu tincidunt tortor aliquam nulla facilisi cras fermentum. Donec massa sapien faucibus et molestie ac feugiat sed. Facilisi cras fermentum odio eu feugiat pretium nibh ipsum consequat. Turpis egestas sed tempus urna et pharetra pharetra massa. Mi quis hendrerit dolor magna eget est lorem ipsum. Egestas tellus rutrum tellus pellentesque eu tincidunt. Laoreet id donec ultrices tincidunt arcu non sodales neque. Ac felis donec et odio pellentesque diam volutpat. A diam sollicitudin tempor id eu nisl nunc mi. Quam pellentesque nec nam aliquam sem et tortor consequat id. Vitae tempus quam pellentesque nec nam aliquam sem et. Bibendum arcu vitae elementum curabitur vitae nunc sed velit. Praesent tristique magna sit amet purus gravida. Dictum sit amet justo donec enim diam vulputate. Egestas fringilla phasellus faucibus scelerisque eleifend donec pretium vulputate sapien. Eleifend mi in nulla posuere sollicitudin aliquam ultrices sagittis. Adipiscing bibendum est ultricies integer quis auctor elit. Dolor morbi non arcu risus quis varius quam quisque id. Elementum facilisis leo vel fringilla est ullamcorper eget nulla. Integer feugiat scelerisque varius morbi enim nunc faucibus a. Nunc scelerisque viverra mauris in. Hendrerit gravida rutrum quisque non. Eu volutpat odio facilisis mauris sit amet massa vitae. Aliquet lectus proin nibh nisl condimentum id venenatis. Sit amet dictum sit amet justo donec.",
                "the link to the first entry", "the link to the first entry", null, null);
    }

    private String inputStreamToString(InputStream is) throws IOException {
        try (is) {
            return new String(is.readAllBytes());
        }
    }

    private void assertFeedEquals(Entry entry, String title, String id, String link, String summary, String content) {
        assertEquals(title, entry.getTitle());
        assertEquals(id, entry.getUid());
        assertEquals(link, entry.getLink());
        assertEquals(summary, entry.getSummary());
        assertEquals(content, entry.getContent());
    }

}
