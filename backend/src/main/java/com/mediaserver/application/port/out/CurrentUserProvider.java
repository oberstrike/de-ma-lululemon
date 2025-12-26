package com.mediaserver.application.port.out;

public interface CurrentUserProvider {
    CurrentUser getCurrentUser();

    default String getCurrentUserId() {
        return getCurrentUser().userId();
    }
}
