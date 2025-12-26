package com.mediaserver.application.port.out;

import com.mediaserver.application.model.CurrentUser;

public interface CurrentUserProvider {
    CurrentUser getCurrentUser();

    default String getCurrentUserId() {
        return getCurrentUser().userId();
    }
}
