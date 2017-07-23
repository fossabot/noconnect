package org.chagolchana.chagol.transport;

import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.transport.TransportKeys;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
class MutableTransportKeys {

	private final TransportId transportId;
	private final MutableIncomingKeys inPrev, inCurr, inNext;
	private final MutableOutgoingKeys outCurr;

	MutableTransportKeys(TransportKeys k) {
		transportId = k.getTransportId();
		inPrev = new MutableIncomingKeys(k.getPreviousIncomingKeys());
		inCurr = new MutableIncomingKeys(k.getCurrentIncomingKeys());
		inNext = new MutableIncomingKeys(k.getNextIncomingKeys());
		outCurr = new MutableOutgoingKeys(k.getCurrentOutgoingKeys());
	}

	TransportKeys snapshot() {
		return new TransportKeys(transportId, inPrev.snapshot(),
				inCurr.snapshot(), inNext.snapshot(), outCurr.snapshot());
	}

	TransportId getTransportId() {
		return transportId;
	}

	MutableIncomingKeys getPreviousIncomingKeys() {
		return inPrev;
	}

	MutableIncomingKeys getCurrentIncomingKeys() {
		return inCurr;
	}

	MutableIncomingKeys getNextIncomingKeys() {
		return inNext;
	}

	MutableOutgoingKeys getCurrentOutgoingKeys() {
		return outCurr;
	}
}
