package org.chagolchana.noconnect.api.client;

import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.identity.Author.Status;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.sync.MessageId;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public abstract class PostHeader {

	private final MessageId id;
	@Nullable
	private final MessageId parentId;
	private final long timestamp;
	private final Author author;
	private final Status authorStatus;
	private final boolean read;

	public PostHeader(MessageId id, @Nullable MessageId parentId,
			long timestamp, Author author, Status authorStatus, boolean read) {
		this.id = id;
		this.parentId = parentId;
		this.timestamp = timestamp;
		this.author = author;
		this.authorStatus = authorStatus;
		this.read = read;
	}

	public MessageId getId() {
		return id;
	}

	public Author getAuthor() {
		return author;
	}

	public Status getAuthorStatus() {
		return authorStatus;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isRead() {
		return read;
	}

	@Nullable
	public MessageId getParentId() {
		return parentId;
	}
}
