package org.chagolchana.noconnect.sharing;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.client.ClientHelper;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.data.MetadataEncoder;
import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.identity.AuthorFactory;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.noconnect.api.blog.Blog;
import org.chagolchana.noconnect.api.blog.BlogFactory;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static org.chagolchana.chagol.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static org.chagolchana.chagol.api.identity.AuthorConstants.MAX_PUBLIC_KEY_LENGTH;
import static org.chagolchana.chagol.util.ValidationUtils.checkLength;
import static org.chagolchana.chagol.util.ValidationUtils.checkSize;

@Immutable
@NotNullByDefault
class BlogSharingValidator extends SharingValidator {

	private final BlogFactory blogFactory;
	private final AuthorFactory authorFactory;

	@Inject
	BlogSharingValidator(MessageEncoder messageEncoder,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, BlogFactory blogFactory, AuthorFactory authorFactory) {
		super(messageEncoder, clientHelper, metadataEncoder, clock);
		this.blogFactory = blogFactory;
		this.authorFactory = authorFactory;
	}

	@Override
	protected GroupId validateDescriptor(BdfList descriptor)
			throws FormatException {
		checkSize(descriptor, 3);
		String name = descriptor.getString(0);
		checkLength(name, 1, MAX_AUTHOR_NAME_LENGTH);
		byte[] publicKey = descriptor.getRaw(1);
		checkLength(publicKey, 1, MAX_PUBLIC_KEY_LENGTH);
		boolean rssFeed = descriptor.getBoolean(2);

		Author author = authorFactory.createAuthor(name, publicKey);
		Blog blog;
		if (rssFeed) {
			blog = blogFactory.createFeedBlog(author);
		} else {
			blog = blogFactory.createBlog(author);
		}
		return blog.getId();
	}

}
