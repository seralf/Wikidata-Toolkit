package org.wikidata.wdtk.client;

/*
 * #%L
 * Wikidata Toolkit Command-line Tool
 * %%
 * Copyright (C) 2014 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wikidata.wdtk.datamodel.interfaces.Sites;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;

public class ClientTest {

	DumpProcessingController mockDpc;

	@Before
	public void setup() throws IOException {
		mockDpc = Mockito.mock(DumpProcessingController.class);

		MwDumpFile mockDump = Mockito.mock(MwDumpFile.class);
		Mockito.when(mockDump.getProjectName()).thenReturn("wikidata");
		Mockito.when(mockDump.getDateStamp()).thenReturn("20150303");

		Mockito.when(mockDpc.getMostRecentDump(DumpContentType.JSON))
				.thenReturn(mockDump);

		Sites mockSites = Mockito.mock(Sites.class);
		Mockito.when(mockDpc.getSitesInformation()).thenReturn(mockSites);
	}

	@Test
	public void testDefaultLoggingConfig() throws ParseException, IOException {
		String[] args = new String[] {};
		Client client = new Client(mockDpc, args);
		client.performActions(); // print help

		assertEquals(Client.consoleAppender.getThreshold(), Level.INFO);
		assertEquals(Client.errorAppender.getThreshold(), Level.WARN);
	}

	@Test
	public void testQuietStdOutLoggingConfig() throws ParseException,
			IOException {
		String[] args = new String[] { "-a", "json", "-s" };
		new Client(mockDpc, args);

		assertEquals(Client.consoleAppender.getThreshold(), Level.OFF);
		assertEquals(Client.errorAppender.getThreshold(), Level.WARN);
	}

	@Test
	public void testQuietLoggingConfig() throws ParseException, IOException {
		String[] TEST_ARGS = new String[] { "-a", "json", "-q" };
		new Client(mockDpc, TEST_ARGS);

		assertEquals(Client.consoleAppender.getThreshold(), Level.OFF);
		assertEquals(Client.errorAppender.getThreshold(), Level.WARN);
	}

	@Test
	public void testNonReadyActionWithDumps() throws ParseException,
			IOException {
		String[] args = new String[] { "-a", "rdf", "--dumps", "/tmp" };
		Client client = new Client(mockDpc, args);
		client.performActions(); // print help

		Mockito.verify(mockDpc, Mockito.never()).processDump(
				Mockito.<MwDumpFile> any());
		Mockito.verify(mockDpc, Mockito.never()).getSitesInformation();
		Mockito.verify(mockDpc).setDownloadDirectory("/tmp");
	}

	@Test
	public void testSitesAction() throws ParseException, IOException {
		String[] args = new String[] { "-a", "rdf", "--rdftasks",
				"items,labels" };
		Client client = new Client(mockDpc, args);
		client.performActions();

		Mockito.verify(mockDpc).processDump(Mockito.<MwDumpFile> any());
		Mockito.verify(mockDpc).getSitesInformation();
	}

	@Test
	public void testSetDumpsDirectoryException() throws ParseException,
			IOException {
		Mockito.doThrow(new IOException()).when(mockDpc)
				.setDownloadDirectory(Mockito.anyString());

		String[] args = new String[] { "-a", "rdf", "--rdftasks",
				"items,labels", "--dumps", "/tmp/" };
		Client client = new Client(mockDpc, args);
		client.performActions(); // print help

		Mockito.verify(mockDpc, Mockito.never()).processDump(
				Mockito.<MwDumpFile> any());
		Mockito.verify(mockDpc, Mockito.never()).getSitesInformation();
	}

	@Test
	public void testSitesActionException() throws ParseException, IOException {
		Mockito.doThrow(new IOException()).when(mockDpc).getSitesInformation();

		String[] args = new String[] { "-a", "rdf", "--rdftasks",
				"items,labels" };
		Client client = new Client(mockDpc, args);
		client.performActions(); // print help

		Mockito.verify(mockDpc, Mockito.never()).processDump(
				Mockito.<MwDumpFile> any());
		Mockito.verify(mockDpc).getSitesInformation();
	}

	@Test
	public void testNonSitesAction() throws ParseException, IOException {
		String[] args = new String[] { "-a", "json", "-q" };
		Client client = new Client(mockDpc, args);
		client.performActions(); // print help

		Mockito.verify(mockDpc).processDump(Mockito.<MwDumpFile> any());
		Mockito.verify(mockDpc, Mockito.never()).getSitesInformation();
	}
}
