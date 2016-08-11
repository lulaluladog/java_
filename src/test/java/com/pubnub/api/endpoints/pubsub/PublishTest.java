package com.pubnub.api.endpoints.pubsub;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.awaitility.Awaitility;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PublishTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private PubNub pubnub;
    private Publish instance;


    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
        instance = pubnub.publish();
    }

    @Test
    public void testFireSuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.fire().channel("coolChannel").message("hi").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("true", requests.get(0).queryParameter("norep").firstValue());
        assertEquals("0", requests.get(0).queryParameter("store").firstValue());
    }

    @Test
    public void testNoRepSuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message("hi").replicate(false).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("true", requests.get(0).queryParameter("norep").firstValue());
    }

    @Test
    public void testRepDefaultSuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message("hirep").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertNull(requests.get(0).queryParameter("norep"));
    }

    @Test
    public void testSuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message("hi").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
    }

    @Test
    public void testSuccessSequenceSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message("hi").sync();
        instance.channel("coolChannel").message("hi").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(2, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("1", requests.get(0).queryParameter("seqn").firstValue());
        assertEquals("2", requests.get(1).queryParameter("seqn").firstValue());


    }

    @Test
    public void testSuccessPostSync() throws PubNubException, InterruptedException {
        stubFor(post(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").usePOST(true).message(Arrays.asList("m1", "m2")).sync();

        List<LoggedRequest> requests = findAll(postRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("[\"m1\",\"m2\"]", new String(requests.get(0).getBody()));
    }

    @Test
    public void testSuccessStoreFalseSync() throws PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message("hi").shouldStore(false).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("0", requests.get(0).queryParameter("store").firstValue());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
    }

    @Test
    public void testSuccessStoreTrueSync() throws PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message("hi").shouldStore(true).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("1", requests.get(0).queryParameter("store").firstValue());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
    }

    @Test
    public void testSuccessMetaSync() throws PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("meta", matching("%5B%22m1%22%2C%22m2%22%5D"))
                .withQueryParam("store", matching("0"))
                .withQueryParam("seqn", matching("1"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message("hi").meta(Arrays.asList("m1", "m2")).shouldStore(false).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }

    @Test
    public void testSuccessAuthKeySync() throws PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setAuthKey("authKey");
        instance.channel("coolChannel").message("hi").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("authKey", requests.get(0).queryParameter("auth").firstValue());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testSuccessIntSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/10"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message(10).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testSuccessArraySync() throws PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%5B%22a%22%2C%22b%22%2C%22c%22%5D?"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message(Arrays.asList("a", "b", "c")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testSuccessArrayEncryptedSync() throws PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22HFP7V6bDwBLrwc1t8Rnrog%3D%3D%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setCipherKey("testCipher");
        instance.channel("coolChannel").message(Arrays.asList("m1", "m2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testSuccessPostEncryptedSync() throws PubNubException, InterruptedException {
        stubFor(post(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setCipherKey("testCipher");

        instance.channel("coolChannel").usePOST(true).message(Arrays.asList("m1", "m2")).sync();

        List<LoggedRequest> requests = findAll(postRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("\"HFP7V6bDwBLrwc1t8Rnrog==\"", new String(requests.get(0).getBody()));
    }

    @Test
    public void testSuccessHashMapSync() throws PubNubException, InterruptedException {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 10);
        params.put("z", "test");

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%7B%22a%22%3A10%2C%22z%22%3A%22test%22%7D"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message(params).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testSuccessPOJOSync() throws PubNubException, InterruptedException {

        @AllArgsConstructor
        @Getter
        class TestPojo {
            String field1;
            String field2;
        }

        TestPojo testPojo = new TestPojo("10", "20");

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%7B%22field1%22%3A%2210%22%2C%22field2%22%3A%2220%22%7D"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message(testPojo).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @org.junit.Test(expected=PubNubException.class)
    public void testMissingChannel() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.message("hi").sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testEmptyChannel() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.message("hi").channel("").sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testMissingMessage() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").sync();
    }

    @org.junit.Test
    public void testOperationTypeSuccessAsync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        final AtomicInteger atomic = new AtomicInteger(0);

        instance.async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult result, PNStatus status) {
                if (status != null && status.getOperation()== PNOperationType.PNPublishOperation) {
                    atomic.incrementAndGet();
                }

            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testNullSubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setSubscribeKey(null);
        instance.channel("coolChannel").message("hirep").sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testEmptySubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setSubscribeKey("");
        instance.channel("coolChannel").message("hirep").sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testNullPublishKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setPublishKey(null);
        instance.channel("coolChannel").message("hirep").sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testEmptyPublishKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setPublishKey("");
        instance.channel("coolChannel").message("hirep").sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testInvalidMessage() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message(new Object()).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertNull(requests.get(0).queryParameter("norep"));
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testInvalidMeta() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel("coolChannel").message("hi").meta(new Object()).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertNull(requests.get(0).queryParameter("norep"));
    }
}