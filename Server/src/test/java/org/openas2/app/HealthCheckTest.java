package org.openas2.app;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openas2.TestResource;
import org.openas2.processor.ActiveModule;
import org.openas2.processor.receiver.NetModule;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(MockitoJUnitRunner.class)

public class HealthCheckTest {
	private static final TestResource RESOURCE = TestResource.forGroup("SingleServerTest");
	// private static File openAS2AHome;
	private static OpenAS2Server serverInstance;
	private static ExecutorService executorService;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@BeforeClass
	public static void startServer() throws Exception {
		try {
			System.setProperty("org.apache.commons.logging.Log", "org.openas2.logging.Log");
			//System.setProperty("org.openas2.logging.defaultlog", "TRACE");
			executorService = Executors.newFixedThreadPool(20);

			HealthCheckTest.serverInstance = new OpenAS2Server.Builder()
					.run(RESOURCE.get("MyCompany", "config", "config.xml").getAbsolutePath());
		} catch (Throwable e) {
			// aid for debugging JUnit tests
			System.err.println("ERROR occurred: " + ExceptionUtils.getStackTrace(e));
			throw new Exception(e);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		serverInstance.shutdown();
		executorService.shutdown();
	}

	@Test
	public void shouldRespondToHealthCheckOnNetModule() throws Exception {
		Class<NetModule> listenerClass = NetModule.class;
		List<ActiveModule> listeners = serverInstance.getSession().getProcessor().getActiveModulesByClass(listenerClass);
		for (ActiveModule listener : listeners) {
			List<String> failures = new ArrayList<String>();
			NetModule m = (NetModule) listener;
			assertThat("Verify healthcheck URI is working on active NetModule instance", m.healthcheck(failures),
					is(true));
		}
	}

}
