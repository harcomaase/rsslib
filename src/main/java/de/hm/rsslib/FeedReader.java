package de.hm.rsslib;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public class FeedReader {

    private static final Logger LOGGER = Logger.getLogger(FeedReader.class.getName());

    private final HttpClient httpClient;

    public FeedReader(HttpClient httpClient) throws RssException {
        this.httpClient = httpClient;
    }

    public Feed readFeed(String url) throws RssException {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(url))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode >= 400) {
                throw new RssException("unsuccessful server response: " + statusCode);
            }
            String payload = response.body();

            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            var xmlReader = xmlInputFactory.createXMLStreamReader(new StringReader(payload));

            Feed feed = null;
            Entry currentEntry = null;
            String currentCharacters = "";
            Map<String, String> currentAttributes = new HashMap<>();

            while (xmlReader.hasNext()) {
                int eventId = xmlReader.next();
                switch (eventId) {
                    case XMLEvent.START_ELEMENT -> {
                        String element = xmlReader.getName().getLocalPart();
                        if ("feed".equals(element) || "channel".equals(element)) {
                            if (feed != null) {
                                throw new RssException("malformed input: nested feeds");
                            }
                            feed = new Feed();
                            feed.setEntries(new LinkedList<>());
                        }

                        if ("entry".equals(element) || "item".equals(element)) {
                            if (currentEntry != null) {
                                throw new RssException("malformed input: nested entries");
                            }
                            currentEntry = new Entry();
                        }

                        parseAttribute(currentAttributes, xmlReader);
                    }
                    case XMLEvent.CHARACTERS -> {
                        currentCharacters += xmlReader.getText();
                    }
                    case XMLEvent.END_ELEMENT -> {
                        String element = xmlReader.getName().getLocalPart();
                        currentCharacters = currentCharacters.trim();
                        if ("title".equals(element)) {
                            if (currentEntry != null) {
                                currentEntry.setTitle(currentCharacters);
                            } else if (feed != null) {
                                feed.setTitle(currentCharacters);
                            } else {
                                throw new RssException("malformed input: unexpected title");
                            }
                        }
                        if ("id".equals(element) || "guid".equals(element)) {
                            if (currentEntry != null) {
                                currentEntry.setUid(currentCharacters);
                            }
                        }

                        if ("link".equals(element)) {
                            if (currentEntry != null) {
                                String linkText = currentCharacters;
                                if (linkText.isEmpty()) {
                                    linkText = currentAttributes.get("href");
                                }
                                currentEntry.setLink(linkText);
                            }
                        }

                        if ("summary".equals(element)) {
                            if (currentEntry != null) {
                                currentEntry.setSummary(currentCharacters);
                            }
                        }
                        if ("content".equals(element)) {
                            if (currentEntry != null) {
                                currentEntry.setContent(currentCharacters);
                            }
                        }

                        if ("entry".equals(element) || "item".equals(element)) {
                            feed.getEntries().add(currentEntry);
                            currentEntry = null;
                        }

                        currentCharacters = "";
                        currentAttributes.clear();
                    }
                    default -> {
                    }
                }
            }

            return feed;

        } catch (URISyntaxException | IOException | InterruptedException | IllegalArgumentException | XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new RssException(ex);
        }
    }

    private void parseAttribute(Map<String, String> attributeMap, XMLStreamReader xmlReader) {
        for (int i = 0; i < xmlReader.getAttributeCount(); i += 1) {
            String attributeName = xmlReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlReader.getAttributeValue(i);

            attributeMap.put(attributeName, attributeValue);
        }
    }
}
