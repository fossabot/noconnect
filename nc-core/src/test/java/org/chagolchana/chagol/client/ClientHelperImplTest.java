package org.chagolchana.chagol.client;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.client.ClientHelper;
import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.data.BdfEntry;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.data.BdfReader;
import org.chagolchana.chagol.api.data.BdfReaderFactory;
import org.chagolchana.chagol.api.data.BdfWriter;
import org.chagolchana.chagol.api.data.BdfWriterFactory;
import org.chagolchana.chagol.api.data.MetadataEncoder;
import org.chagolchana.chagol.api.data.MetadataParser;
import org.chagolchana.chagol.api.db.DatabaseComponent;
import org.chagolchana.chagol.api.db.Metadata;
import org.chagolchana.chagol.api.db.Transaction;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.Message;
import org.chagolchana.chagol.api.sync.MessageFactory;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.chagol.test.BrambleTestCase;
import org.chagolchana.chagol.test.TestUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.chagolchana.chagol.test.TestUtils.getRandomBytes;
import static org.chagolchana.chagol.test.TestUtils.getRandomId;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ClientHelperImplTest extends BrambleTestCase {

	private final Mockery context = new Mockery();
	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final MessageFactory messageFactory =
			context.mock(MessageFactory.class);
	private final BdfReaderFactory bdfReaderFactory =
			context.mock(BdfReaderFactory.class);
	private final BdfWriterFactory bdfWriterFactory =
			context.mock(BdfWriterFactory.class);
	private final MetadataParser metadataParser =
			context.mock(MetadataParser.class);
	private final MetadataEncoder metadataEncoder =
			context.mock(MetadataEncoder.class);
	private final CryptoComponent cryptoComponent =
			context.mock(CryptoComponent.class);
	private final ClientHelper clientHelper;

	private final GroupId groupId = new GroupId(getRandomId());
	private final BdfDictionary dictionary = new BdfDictionary();
	private final long timestamp = 42L;
	private final byte[] rawMessage = getRandomBytes(42);
	private final MessageId messageId = new MessageId(getRandomId());
	private final Message message =
			new Message(messageId, groupId, timestamp, rawMessage);
	private final Metadata metadata = new Metadata();
	private final BdfList list = BdfList.of("Sign this!", getRandomBytes(42));
	private final String label = TestUtils.getRandomString(5);

	public ClientHelperImplTest() {
		clientHelper =
				new ClientHelperImpl(db, messageFactory, bdfReaderFactory,
						bdfWriterFactory, metadataParser, metadataEncoder,
						cryptoComponent);
	}

	@Test
	public void testAddLocalMessage() throws Exception {
		final boolean shared = true;
		final Transaction txn = new Transaction(null, false);

		context.checking(new Expectations() {{
			oneOf(db).startTransaction(false);
			will(returnValue(txn));
			oneOf(metadataEncoder).encode(dictionary);
			will(returnValue(metadata));
			oneOf(db).addLocalMessage(txn, message, metadata, shared);
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
		}});

		clientHelper.addLocalMessage(message, dictionary, shared);
		context.assertIsSatisfied();
	}

	@Test
	public void testCreateMessage() throws Exception {
		final byte[] bytes = expectToByteArray(list);

		context.checking(new Expectations() {{
			oneOf(messageFactory).createMessage(groupId, timestamp, bytes);
		}});

		clientHelper.createMessage(groupId, timestamp, list);
		context.assertIsSatisfied();
	}

	@Test
	public void testGetMessageAsList() throws Exception {
		final Transaction txn = new Transaction(null, true);

		expectToList(true);
		context.checking(new Expectations() {{
			oneOf(db).startTransaction(true);
			will(returnValue(txn));
			oneOf(db).getRawMessage(txn, messageId);
			will(returnValue(rawMessage));
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
		}});

		clientHelper.getMessageAsList(messageId);
		context.assertIsSatisfied();
	}

	@Test
	public void testGetGroupMetadataAsDictionary() throws Exception {
		final Transaction txn = new Transaction(null, true);

		context.checking(new Expectations() {{
			oneOf(db).startTransaction(true);
			will(returnValue(txn));
			oneOf(db).getGroupMetadata(txn, groupId);
			will(returnValue(metadata));
			oneOf(metadataParser).parse(metadata);
			will(returnValue(dictionary));
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
		}});

		assertEquals(dictionary,
				clientHelper.getGroupMetadataAsDictionary(groupId));
		context.assertIsSatisfied();
	}

	@Test
	public void testGetMessageMetadataAsDictionary() throws Exception {
		final Transaction txn = new Transaction(null, true);

		context.checking(new Expectations() {{
			oneOf(db).startTransaction(true);
			will(returnValue(txn));
			oneOf(db).getMessageMetadata(txn, messageId);
			will(returnValue(metadata));
			oneOf(metadataParser).parse(metadata);
			will(returnValue(dictionary));
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
		}});

		assertEquals(dictionary,
				clientHelper.getMessageMetadataAsDictionary(messageId));
		context.assertIsSatisfied();
	}

	@Test
	public void testGetMessageMetadataAsDictionaryMap() throws Exception {
		final Map<MessageId, BdfDictionary> map =
				new HashMap<MessageId, BdfDictionary>();
		map.put(messageId, dictionary);
		final Transaction txn = new Transaction(null, true);

		context.checking(new Expectations() {{
			oneOf(db).startTransaction(true);
			will(returnValue(txn));
			oneOf(db).getMessageMetadata(txn, groupId);
			will(returnValue(Collections.singletonMap(messageId, metadata)));
			oneOf(metadataParser).parse(metadata);
			will(returnValue(dictionary));
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
		}});

		assertEquals(map,
				clientHelper.getMessageMetadataAsDictionary(groupId));
		context.assertIsSatisfied();
	}

	@Test
	public void testGetMessageMetadataAsDictionaryQuery() throws Exception {
		final Map<MessageId, BdfDictionary> map =
				new HashMap<MessageId, BdfDictionary>();
		map.put(messageId, dictionary);
		final BdfDictionary query =
				BdfDictionary.of(new BdfEntry("query", "me"));
		final Metadata queryMetadata = new Metadata();
		queryMetadata.put("query", getRandomBytes(42));
		final Transaction txn = new Transaction(null, true);

		context.checking(new Expectations() {{
			oneOf(db).startTransaction(true);
			will(returnValue(txn));
			oneOf(metadataEncoder).encode(query);
			will(returnValue(queryMetadata));
			oneOf(db).getMessageMetadata(txn, groupId, queryMetadata);
			will(returnValue(Collections.singletonMap(messageId, metadata)));
			oneOf(metadataParser).parse(metadata);
			will(returnValue(dictionary));
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
		}});

		assertEquals(map,
				clientHelper.getMessageMetadataAsDictionary(groupId, query));
		context.assertIsSatisfied();
	}

	@Test
	public void testMergeGroupMetadata() throws Exception {
		final Transaction txn = new Transaction(null, false);

		context.checking(new Expectations() {{
			oneOf(db).startTransaction(false);
			will(returnValue(txn));
			oneOf(metadataEncoder).encode(dictionary);
			will(returnValue(metadata));
			oneOf(db).mergeGroupMetadata(txn, groupId, metadata);
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
		}});

		clientHelper.mergeGroupMetadata(groupId, dictionary);
		context.assertIsSatisfied();
	}

	@Test
	public void testMergeMessageMetadata() throws Exception {
		final Transaction txn = new Transaction(null, false);

		context.checking(new Expectations() {{
			oneOf(db).startTransaction(false);
			will(returnValue(txn));
			oneOf(metadataEncoder).encode(dictionary);
			will(returnValue(metadata));
			oneOf(db).mergeMessageMetadata(txn, messageId, metadata);
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
		}});

		clientHelper.mergeMessageMetadata(messageId, dictionary);
		context.assertIsSatisfied();
	}

	@Test
	public void testToByteArray() throws Exception {
		byte[] bytes = expectToByteArray(list);

		assertArrayEquals(bytes, clientHelper.toByteArray(list));
		context.assertIsSatisfied();
	}

	@Test
	public void testToList() throws Exception {
		expectToList(true);

		assertEquals(list, clientHelper.toList(rawMessage));
		context.assertIsSatisfied();
	}

	@Test
	public void testToListWithNoEof() throws Exception {
		expectToList(false); // no EOF after list

		try {
			clientHelper.toList(rawMessage);
			fail();
		} catch (FormatException e) {
			// expected
			context.assertIsSatisfied();
		}
	}

	@Test
	public void testSign() throws Exception {
		final byte[] privateKey = getRandomBytes(42);
		final byte[] signed = getRandomBytes(42);

		final byte[] bytes = expectToByteArray(list);
		context.checking(new Expectations() {{
			oneOf(cryptoComponent).sign(label, bytes, privateKey);
			will(returnValue(signed));
		}});

		assertArrayEquals(signed, clientHelper.sign(label, list, privateKey));
		context.assertIsSatisfied();
	}

	@Test
	public void testVerifySignature() throws Exception {
		final byte[] publicKey = getRandomBytes(42);
		final byte[] bytes = expectToByteArray(list);

		context.checking(new Expectations() {{
			oneOf(cryptoComponent).verify(label, bytes, publicKey, rawMessage);
			will(returnValue(true));
		}});

		clientHelper.verifySignature(label, rawMessage, publicKey, list);
		context.assertIsSatisfied();
	}

	@Test
	public void testVerifyWrongSignature() throws Exception {
		final byte[] publicKey = getRandomBytes(42);
		final byte[] bytes = expectToByteArray(list);

		context.checking(new Expectations() {{
			oneOf(cryptoComponent).verify(label, bytes, publicKey, rawMessage);
			will(returnValue(false));
		}});

		try {
			clientHelper
					.verifySignature(label, rawMessage, publicKey, list);
			fail();
		} catch (GeneralSecurityException e) {
			// expected
			context.assertIsSatisfied();
		}
	}

	private byte[] expectToByteArray(final BdfList list) throws Exception {
		final BdfWriter bdfWriter = context.mock(BdfWriter.class);

		context.checking(new Expectations() {{
			oneOf(bdfWriterFactory)
					.createWriter(with(any(ByteArrayOutputStream.class)));
			will(returnValue(bdfWriter));
			oneOf(bdfWriter).writeList(list);
		}});
		return new byte[0];
	}

	private void expectToList(final boolean eof) throws Exception {
		final BdfReader bdfReader = context.mock(BdfReader.class);

		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory)
					.createReader(with(any(InputStream.class)));
			will(returnValue(bdfReader));
			oneOf(bdfReader).readList();
			will(returnValue(list));
			oneOf(bdfReader).eof();
			will(returnValue(eof));
		}});
	}

}
