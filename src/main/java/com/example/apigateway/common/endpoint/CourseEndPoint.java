package com.example.apigateway.common.endpoint;

import com.example.apigateway.common.endpoint.common.EndPoint;
import com.example.apigateway.common.type.Role;
import lombok.Getter;

@Getter
public enum CourseEndPoint implements EndPoint {
    LIST("/list", Role.USER),
    OWN_LIST("/own-list"),
    CREATE("/create"),
    UPDATE("/update/**"),
    DELETE("/delete"),
    STUDENT_LIST("/student/info/list/**"),
    STUDENT_INFO("/student/info/**"),
    DOWNLOAD_SAMPLE_FILE("/invite/sample/download"),
    INVITE("/invite/**"),
    INVITE_BY_FILE("/invite-file/**"),
    KICK("/kick/**"),
    ALL_GRADE("/all/grade/**"),
    PROBLEM_GRADE("/problem/grade/**");

    private final String path;
    private final Role role;

    private static final String PREFIX = "/course";

    CourseEndPoint(String path) {
        this(path, Role.MANAGER);
    }

    CourseEndPoint(String path, Role role) {
        this.path = PREFIX + path;
        this.role = role;
    }
}
