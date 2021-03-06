package org.chagolchana.chagol.api.sync;

import org.chagolchana.chagol.api.UniqueId;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies a
 * {@link Message}.
 */
@ThreadSafe
@NotNullByDefault
public class MessageId extends UniqueId {

	/**
	 * Label for hashing messages to calculate their identifiers.
	 */
	public static final String LABEL = "org.briarproject.bramble.MESSAGE_ID";

	public MessageId(byte[] id) {
		super(id);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MessageId && super.equals(o);
	}
}
