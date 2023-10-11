package com.amazon.inspector.teamcity.exception;

import static com.amazon.inspector.teamcity.ScanBuildProcessAdapter.publicProgressLogger;

public class BomermanNotFoundException extends Exception {
    public BomermanNotFoundException(String message) {
        super(message);

        publicProgressLogger.message(message);
    }
}
