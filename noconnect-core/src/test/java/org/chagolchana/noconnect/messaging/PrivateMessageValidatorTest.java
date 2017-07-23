package org.chagolchana.noconnect.messaging;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.client.BdfMessageContext;
import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.test.TestUtils;
import org.chagolchana.chagol.test.ValidatorTestCase;
import org.junit.Test;

import static org.chagolchana.noconnect.api.messaging.MessagingConstants.MAX_PRIVATE_MESSAGE_BODY_LENGTH;
import static org.chagolchana.noconnect.client.MessageTrackerConstants.MSG_KEY_READ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PrivateMessageValidatorTest extends ValidatorTestCase {

	@Test(expected = FormatException.class)
	public void testRejectsTooShortBody() throws Exception {
		PrivateMessageValidator v = new PrivateMessageValidator(clientHelper,
				metadataEncoder, clock);
		v.validateMessage(message, group, new BdfList());
	}

	@Test(expected = FormatException.class)
	public void testRejectsTooLongBody() throws Exception {
		PrivateMessageValidator v = new PrivateMessageValidator(clientHelper,
				metadataEncoder, clock);
		v.validateMessage(message, group, BdfList.of("", 123));
	}

	@Test(expected = FormatException.class)
	public void testRejectsNullContent() throws Exception {
		PrivateMessageValidator v = new PrivateMessageValidator(clientHelper,
				metadataEncoder, clock);
		v.validateMessage(message, group, BdfList.of((String) null));
	}

	@Test(expected = FormatException.class)
	public void testRejectsNonStringContent() throws Exception {
		PrivateMessageValidator v = new PrivateMessageValidator(clientHelper,
				metadataEncoder, clock);
		v.validateMessage(message, group, BdfList.of(123));
	}

	@Test(expected = FormatException.class)
	public void testRejectsTooLongContent() throws Exception {
		PrivateMessageValidator v = new PrivateMessageValidator(clientHelper,
				metadataEncoder, clock);
		String invalidContent =
				TestUtils.getRandomString(MAX_PRIVATE_MESSAGE_BODY_LENGTH + 1);
		v.validateMessage(message, group, BdfList.of(invalidContent));
	}

	@Test
	public void testAcceptsMaxLengthContent() throws Exception {
		PrivateMessageValidator v = new PrivateMessageValidator(clientHelper,
				metadataEncoder, clock);
		String content =
				TestUtils.getRandomString(MAX_PRIVATE_MESSAGE_BODY_LENGTH);
		BdfMessageContext messageContext =
				v.validateMessage(message, group, BdfList.of(content));
		assertExpectedContext(messageContext);
	}

	@Test
	public void testAcceptsMinLengthContent() throws Exception {
		PrivateMessageValidator v = new PrivateMessageValidator(clientHelper,
				metadataEncoder, clock);
		BdfMessageContext messageContext =
				v.validateMessage(message, group, BdfList.of(""));
		assertExpectedContext(messageContext);
	}

	private void assertExpectedContext(BdfMessageContext messageContext)
			throws FormatException {
		BdfDictionary meta = messageContext.getDictionary();
		assertEquals(3, meta.size());
		assertEquals(timestamp, meta.getLong("timestamp").longValue());
		assertFalse(meta.getBoolean("local"));
		assertFalse(meta.getBoolean(MSG_KEY_READ));
		assertEquals(0, messageContext.getDependencies().size());
	}
}