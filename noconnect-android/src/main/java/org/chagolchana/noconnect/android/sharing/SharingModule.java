package org.chagolchana.noconnect.android.sharing;

import org.chagolchana.noconnect.android.activity.ActivityScope;
import org.chagolchana.noconnect.android.activity.BaseActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class SharingModule {

	@ActivityScope
	@Provides
	ShareForumController provideShareForumController(
			ShareForumControllerImpl shareForumController) {
		return shareForumController;
	}

	@ActivityScope
	@Provides
	BlogInvitationController provideInvitationBlogController(
			BaseActivity activity,
			BlogInvitationControllerImpl blogInvitationController) {
		activity.addLifecycleController(blogInvitationController);
		return blogInvitationController;
	}

	@ActivityScope
	@Provides
	ForumInvitationController provideInvitationForumController(
			BaseActivity activity,
			ForumInvitationControllerImpl forumInvitationController) {
		activity.addLifecycleController(forumInvitationController);
		return forumInvitationController;
	}

	@ActivityScope
	@Provides
	ShareBlogController provideShareBlogController(
			ShareBlogControllerImpl shareBlogController) {
		return shareBlogController;
	}

}
